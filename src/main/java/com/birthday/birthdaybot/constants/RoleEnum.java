package com.birthday.birthdaybot.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum RoleEnum {
    USER("USER"),
    ADMIN("ADMIN");

    private final String role;
}
