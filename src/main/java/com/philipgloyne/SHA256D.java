package com.philipgloyne;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256D implements HashAlgorithm {

    private final String SHA_256 = "SHA-256";

    @Override
    public byte[] hash(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance(SHA_256);
            return md.digest(md.digest(input)); // SHA256d
        } catch (NoSuchAlgorithmException e) {
            // should never happen, usually it would be safer to fail fast
            throw new RuntimeException(e);
        }
    }
}
