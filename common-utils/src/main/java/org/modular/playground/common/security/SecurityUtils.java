package org.modular.playground.common.security;

import org.eclipse.microprofile.jwt.JsonWebToken;

public class SecurityUtils {

    public static boolean isAdmin(JsonWebToken principal) {
        if (principal == null || principal.getClaim("realm_access") == null) {
            return false;
        }
        
        if (principal.getClaim("realm_access") instanceof jakarta.json.JsonObject) {
            jakarta.json.JsonObject realmAccess = principal.getClaim("realm_access");
            jakarta.json.JsonArray roles = realmAccess.getJsonArray("roles");
            if (roles != null) {
                return roles.stream().anyMatch(role -> "admin".equals(((jakarta.json.JsonString) role).getString()));
            }
        }
        return false;
    }
}