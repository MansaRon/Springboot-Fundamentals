package co.za.ecommerce.security;

import co.za.ecommerce.model.AccountStatus;
import co.za.ecommerce.model.User;
import org.bson.types.ObjectId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {

    private final ObjectId id;
    private final String email;
    private final String password;
    private final AccountStatus status;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.status = user.getStatus();
        this.authorities = user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    public ObjectId getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != AccountStatus.SUSPENDED;
    }

    @Override
    public boolean isEnabled() {
        return status == AccountStatus.ACTIVE;
    }
}
