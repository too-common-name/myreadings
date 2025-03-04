package modules.user.domain;

import java.util.UUID;


public interface User {

    UUID getUserId();

    String getFirstName();

    String getLastName();

    String getUsername();

    String getEmail();

    UiTheme getThemePreference();

    void updateUiTheme(UiTheme newTheme);

}
