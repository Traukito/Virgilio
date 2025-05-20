package com.cs.virgilio.util.authservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class HawkAuthService implements AuthService {

    @Value("${app.fracttal.userId}")
    private String userId;

    @Value("${app.fracttal.secretKey}")
    private String secretKey;

    @Value("${app.fracttal.host}")
    private String host;

    @Override
    public HttpHeaders createAuthHeaders(String method, String route) {
        return createAuthHeaders(method, route, "");
    }

    public HttpHeaders createAuthHeaders(String method, String route, String payload) {
        method = method.toUpperCase();
        long timespan = System.currentTimeMillis() / 1000;
        String nonce = HawkAuthUtils.generateNonce(5);
        String port = "443";
        String contentType = "application/json";

        String hash = payload.isEmpty() ? "" : HawkAuthUtils.calculatePayloadHash(payload, contentType);
        String mac = HawkAuthUtils.createHMACAuthHeader(nonce, method, route, host, timespan, port, secretKey, hash);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", contentType);

        String authHeader = String.format(
                "Hawk id=\"%s\", ts=\"%d\", nonce=\"%s\", mac=\"%s\"%s",
                userId, timespan, nonce, mac,
                hash.isEmpty() ? "" : String.format(", hash=\"%s\"", hash)
        );
        headers.set("Authorization", authHeader);

        return headers;
    }
}
