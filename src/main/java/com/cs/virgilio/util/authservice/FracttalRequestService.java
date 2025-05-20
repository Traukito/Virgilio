package com.cs.virgilio.util.authservice;

import com.cs.virgilio.exception.HawkServiceException;
import com.cs.virgilio.model.FracttalResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class FracttalRequestService {

    private final RestTemplate restTemplate;
    private final AuthService authService;

    @Value("${app.fracttal.host}")
    private String host;

    public <T> FracttalResponseDto<T> realizarPeticionFracttal(String route, ParameterizedTypeReference<FracttalResponseDto<T>> typeReference) {
        String url = "https://" + host + route;
        HttpHeaders headers = authService.createAuthHeaders("GET", route);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<FracttalResponseDto<T>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                typeReference
        );

        FracttalResponseDto<T> body = response.getBody();
        if (body == null || !body.isSuccess()) {
            throw new HawkServiceException("Respuesta no válida desde Fracttal");
        }

        return body;
    }
}
