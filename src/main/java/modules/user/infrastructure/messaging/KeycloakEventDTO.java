package modules.user.infrastructure.messaging;

import jakarta.json.bind.annotation.JsonbProperty;
import lombok.Data;

@Data
public class KeycloakEventDTO {
    
    @JsonbProperty("userId")
    private String userId;

    @JsonbProperty("details")
    private Details details;

    @Data
    public static class Details {
        private String username;
        private String email;
        
        @JsonbProperty("first_name")
        private String firstName;
        
        @JsonbProperty("last_name")
        private String lastName;
    }
}