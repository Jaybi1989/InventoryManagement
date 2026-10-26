package ch.jacqueline.inventory.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger; // Import hinzufügen
import org.slf4j.LoggerFactory; // Import hinzufügen

@RestController
public class CsrfTokenController {

    private static final Logger logger = LoggerFactory.getLogger(CsrfTokenController.class);

    @GetMapping("/api/csrf-token")
    public CsrfToken csrf(CsrfToken csrfToken) {
        logger.info("CSRF-Token-Endpoint aufgerufen. HeaderName: {}, Token: {}", csrfToken.getHeaderName(), csrfToken.getToken());
        return csrfToken;
    }
}