package com.awn.tn.identity.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHash {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String generate(String plainText){
        return encoder.encode(plainText);
    }

    public static boolean verify(String plainText, String hashed){
        return encoder.matches(plainText, hashed);
    }

}
