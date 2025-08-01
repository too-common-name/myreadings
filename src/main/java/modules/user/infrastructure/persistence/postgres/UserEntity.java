package modules.user.infrastructure.persistence.postgres;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import modules.user.core.domain.UiTheme;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends PanacheEntityBase {

    @Id
    @Column(name = "keycloak_user_id", unique = true, nullable = false)
    private UUID keycloakUserId;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "username", unique = true, nullable = false, length = 80)
    private String username;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "theme_preference")
    @Enumerated(EnumType.STRING)
    private UiTheme themePreference;

}