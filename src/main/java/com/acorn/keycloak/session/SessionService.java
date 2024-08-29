package com.acorn.keycloak.session;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionService {
    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private static final String SERVER_URL = "http://localhost:8080";
    private static final String REALM = "rex-realm";
    private static final String ADMIN_USERNAME = "rex-admin";
    private static final String ADMIN_PASSWORD = "rex-admin";
    private static final String CHECKING_USERNAME = "rex-user";

    public int checkUserSessions() {
        log.info(">>> Starting Keycloak user session check process");
        int activeSessions = -1;

        // Initialize Keycloak client
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(SERVER_URL)
                .realm("master") // admin realm
                .clientId("admin-cli")
                .username(ADMIN_USERNAME)
                .password(ADMIN_PASSWORD)
                .grantType("password")
                .build();

        if (keycloak == null) {
            log.error("### Keycloak client initialization failed");
            return activeSessions;
        }

        // Get Realm resource
        // GET /admin/realms/{realm}
        RealmResource realmResource = keycloak.realm(REALM);

        if (realmResource == null) {
            log.error("### Realm resource not found");
            return activeSessions;
        }

        // The username of the user you want to check
        String usernameToCheck = CHECKING_USERNAME;

        // Get the user by username
        // GET /admin/realms/{realm}/users
        List<UserRepresentation> users = realmResource.users().search(usernameToCheck);
        if (users.isEmpty()) {
            log.info("*** No user found with username: {}", usernameToCheck);
            return activeSessions;
        }

        // Get the first matching user
        UserRepresentation user = users.getFirst();

        if (user == null) {
            log.error("### User not found: '{}'", usernameToCheck);
            return activeSessions;
        }

        // Get the UserResource using the user ID
        UserResource userResource = realmResource.users().get(user.getId());

        if (userResource == null) {
            log.error("### UserResource not found for user: '{}'", usernameToCheck);
            return activeSessions;
        }

        // Get active sessions for the user
        // GET /admin/realms/{realm}/users/{user-id}/sessions
        List<UserSessionRepresentation> sessions = userResource.getUserSessions();

        // Check if there are multiple sessions
        if (sessions.size() > 1) {
            log.info("### User '{}' has multiple active sessions:", usernameToCheck);

            for (UserSessionRepresentation session : sessions) {
                log.info("[Multiple] Session ID: {}, IP Address: {}", session.getId(), session.getIpAddress());
            }
        } else if (sessions.size() == 1) {
            log.info("### User '{}' has a single active session.", usernameToCheck);
            UserSessionRepresentation session = sessions.getFirst();
            log.info("[Single] Session ID: {}, IP Address: {}", session.getId(), session.getIpAddress());

        } else {
            log.info("### No active sessions found for user: '{}'", usernameToCheck);
        }

        // Close the Keycloak client
        keycloak.close();

        log.info("<<< Finished Keycloak user session check process");
        return sessions.size();
    }
}
