package co.za.ecommerce.api.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public landing endpoint for the service root.
 *
 * <p>Without this, a request to {@code /} falls through to {@code anyRequest().authenticated()}
 * and returns 401 — which shows up as noisy "Unauthorized access attempt to: /" warnings
 * whenever a platform (e.g. Render) or a human opens the base URL. A plain 200 here gives
 * those probes a sane response. Deeper liveness/readiness checks still live at
 * {@code /actuator/health}.
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(Map.of(
                "service", "ecommerce backend",
                "status", "UP"
        ));
    }
}
