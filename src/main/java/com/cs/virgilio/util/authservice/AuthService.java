package com.cs.virgilio.util.authservice;

import org.springframework.http.HttpHeaders;

public interface AuthService {
    HttpHeaders createAuthHeaders(String method, String route);
}