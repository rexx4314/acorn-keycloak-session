package com.acorn.keycloak.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionController {
    private final SessionService sessionService;

    @Autowired
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping(value = "/call")
    public String checkKeycloakUserSessions() {
        int activeSessions = sessionService.checkUserSessions();
        return "Keycloak active user sessions: " + activeSessions + " (check the logs for more details)";
    }
}
