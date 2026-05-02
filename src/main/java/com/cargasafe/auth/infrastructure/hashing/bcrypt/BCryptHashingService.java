package com.cargasafe.auth.infrastructure.hashing.bcrypt;

import org.springframework.security.crypto.password.PasswordEncoder;

public interface BCryptHashingService extends PasswordEncoder {
    String encode(CharSequence rawPassword);
    boolean matches(CharSequence rawPassword, String encodedPassword);
}
