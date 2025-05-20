package com.cs.virgilio.util.authservice;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Random;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HawkAuthUtils {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generateNonce(int length) {
        StringBuilder result = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            result.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return result.toString();
    }

    public static String calculatePayloadHash(String payload, String contentType) {
        try {
            String normalized = "hawk.1.payload\n" +
                    contentType.toLowerCase() + "\n" +
                    payload + "\n";
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate payload hash", e);
        }
    }

    public static String createHMACBase64Hash(String data, String secretKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC hash", e);
        }
    }

    public static String createHMACAuthHeader(
            String nonce,
            String method,
            String route,
            String host,
            long timespan,
            String port,
            String secretKey,
            String payloadHash
    ) {
        String payloadHeader = "hawk.1.header";
        String macString = String.format(
                "%s\n%d\n%s\n%s\n%s\n%s\n%s\n%s\n\n",
                payloadHeader,
                timespan,
                nonce,
                method,
                route,
                host,
                port,
                payloadHash == null ? "" : payloadHash
        );
        return createHMACBase64Hash(macString, secretKey);
    }
}
