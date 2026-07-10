package co.za.ecommerce.filter;

import co.za.ecommerce.dto.GlobalApiErrorResponse;
import co.za.ecommerce.security.CustomUserDetailsService;
import co.za.ecommerce.security.JwtTokenProvider;
import co.za.ecommerce.utils.DateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("JWT Filter hit: {} {}", request.getMethod(), request.getRequestURI());

        String authHead = request.getHeader("Authorization");
        log.info("Authorization header: {}", authHead);
        String token = null;
        String userEmail = null;

        if (authHead != null && authHead.startsWith("Bearer ")) {
            token = authHead.substring(7);
            try {
                userEmail = jwtTokenProvider.extractUsername(token);
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.warn("Expired JWT token for request: {} {}", request.getMethod(), request.getRequestURI());
                writeExpiredTokenResponse(response, request.getRequestURI());
                return;
            }
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(userEmail);
            if (jwtTokenProvider.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }

    private void writeExpiredTokenResponse(HttpServletResponse response, String path) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        GlobalApiErrorResponse body = GlobalApiErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.toString())
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .message("Your session has expired. Please log in again.")
                .path(path)
                .timestamp(DateUtil.now())
                .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.writeValue(response.getOutputStream(), body);
    }
}
