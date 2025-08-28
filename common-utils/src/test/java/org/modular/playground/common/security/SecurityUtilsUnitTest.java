package org.modular.playground.common.security;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class SecurityUtilsUnitTest {

    @Test
    void shouldReturnTrueWhenRoleIsAdmin() {
        JsonWebToken principal = createMockTokenWithRoles("user", "admin");
        assertTrue(SecurityUtils.isAdmin(principal));
    }

    @Test
    void shouldReturnFalseWhenRoleIsNotAdmin() {
        JsonWebToken principal = createMockTokenWithRoles("user", "viewer");
        assertFalse(SecurityUtils.isAdmin(principal));
    }

    @Test
    void shouldReturnFalseWhenRolesArrayIsEmpty() {
        JsonWebToken principal = createMockTokenWithRoles();
        assertFalse(SecurityUtils.isAdmin(principal));
    }

    @Test
    void shouldReturnFalseWhenRolesClaimIsMissing() {
        JsonObject realmAccess = Json.createObjectBuilder().build();
        JsonWebToken principal = Mockito.mock(JsonWebToken.class);
        when(principal.getClaim("realm_access")).thenReturn(realmAccess);
        assertFalse(SecurityUtils.isAdmin(principal));
    }

    @Test
    void shouldReturnFalseWhenRealmAccessClaimIsMissing() {
        JsonWebToken principal = Mockito.mock(JsonWebToken.class);
        when(principal.getClaim("realm_access")).thenReturn(null);
        assertFalse(SecurityUtils.isAdmin(principal));
    }

    @Test
    void shouldReturnFalseWhenPrincipalIsNull() {
        assertFalse(SecurityUtils.isAdmin(null));
    }

    private JsonWebToken createMockTokenWithRoles(String... roles) {
        JsonArray rolesArray = Json.createArrayBuilder(Arrays.stream(roles)
                .map(Json::createValue)
                .collect(Collectors.toList()))
                .build();

        JsonObject realmAccess = Json.createObjectBuilder()
                .add("roles", rolesArray)
                .build();

        JsonWebToken principal = Mockito.mock(JsonWebToken.class);
        when(principal.getClaim("realm_access")).thenReturn(realmAccess);

        return principal;
    }
}