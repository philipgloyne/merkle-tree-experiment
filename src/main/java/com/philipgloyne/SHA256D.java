package com.philipgloyne;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SHA256D implements HashTransform {

    private final String SHA_256 = "SHA-256";

    @Override
    public String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(SHA_256);
            byte[] digest = md.digest(md.digest(input.getBytes(UTF_8))); // SHA256d
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // should never happen, usually it would be safer to fail fast
            throw new RuntimeException(e);
        }
    }
}
