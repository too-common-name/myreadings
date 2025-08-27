package org.modular.playground.user.core.domain;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserRegistrationEvent {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}