package com.sprint.deokhugam.user.adapter.out.security;

import com.sprint.deokhugam.user.application.port.out.PasswordHasher;

public class BCryptPasswordHasher implements PasswordHasher {
    @Override
    public String hash(String rawPassword) {
        return rawPassword;
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return false;
    }
}
