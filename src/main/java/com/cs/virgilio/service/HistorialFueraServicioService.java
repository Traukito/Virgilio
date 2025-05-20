package com.cs.virgilio.service;

import com.cs.virgilio.exception.CodigoNoEncontradoException;
import com.cs.virgilio.exception.CodigoSinHistorialException;
import com.cs.virgilio.exception.HawkServiceException;
import com.cs.virgilio.model.ActivoFueraServicioDto;
import com.cs.virgilio.model.FracttalResponseDto;
import com.cs.virgilio.model.TotalTiempoFueraServicioDto;
import com.cs.virgilio.util.authservice.FracttalRequestService;
import com.cs.virgilio.util.endemicas.TiempoFueraServicioUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
public class HistorialFueraServicioService {

    private final FracttalRequestService fracttalRequestService;

    public Optional<List<ActivoFueraServicioDto>> obtenerHistorialDeFueraDeServicioPorActivo(String code) {
        String route = "/api/items_availability/" + code;

        try {
            FracttalResponseDto<ActivoFueraServicioDto> responseDto = fracttalRequestService.realizarPeticionFracttal(
                    route, new ParameterizedTypeReference<>() {}
            );
            List<ActivoFueraServicioDto> datos = procesarHistorial(code, responseDto);

            datos.sort(Comparator.comparing(dto -> OffsetDateTime.parse(dto.getInitial_date()), Comparator.reverseOrder()));

            return Optional.of(datos);

        } catch (HttpClientErrorException.NotFound e) {
            throw new CodigoNoEncontradoException("Código no encontrado: " + code);
        } catch (Exception e) {
            throw new HawkServiceException("Sin historial o error en el código del Activo", e);
        }
    }

    private List<ActivoFueraServicioDto> procesarHistorial(String code, FracttalResponseDto<ActivoFueraServicioDto> body) {
        List<ActivoFueraServicioDto> datos = body.getData();
        if (datos == null || datos.isEmpty()) {
            throw new CodigoSinHistorialException("No hay historial para el código: " + code);
        }

        for (ActivoFueraServicioDto dto : datos) {
            if (dto.getInitial_date() != null && dto.getFinal_date() != null) {
                dto.setTiempoTranscurrido(
                        TiempoFueraServicioUtils.calcularTiempoTranscurrido(dto.getInitial_date(), dto.getFinal_date())
                );
            } else if (dto.getInitial_date() != null) {
                dto.setTiempoTranscurrido(
                        TiempoFueraServicioUtils.calcularTiempoTranscurrido(dto.getInitial_date())
                );
            }
        }

        return datos;
    }





    public Optional<TotalTiempoFueraServicioDto> obtenerHistorialYTotalTiempoPorActivo(String code) {
        List<ActivoFueraServicioDto> historial = obtenerHistorialDeFueraDeServicioPorActivo(code)
                .orElse(Collections.emptyList());

        long totalSegundos = 0;

        for (ActivoFueraServicioDto dto : historial) {
            if (dto.getInitial_date() != null && dto.getFinal_date() != null) {
                OffsetDateTime inicio = OffsetDateTime.parse(dto.getInitial_date());
                OffsetDateTime fin = OffsetDateTime.parse(dto.getFinal_date());
                totalSegundos += Duration.between(inicio, fin).getSeconds();
            } else if (dto.getInitial_date() != null) {
                OffsetDateTime inicio = OffsetDateTime.parse(dto.getInitial_date());
                OffsetDateTime ahora = OffsetDateTime.now();
                totalSegundos += Duration.between(inicio, ahora).getSeconds();
            }
        }

        String tiempoFormateado = formatoHorasMinutos(totalSegundos);

        return Optional.of(new TotalTiempoFueraServicioDto(historial, tiempoFormateado));
    }





    private String formatoHorasMinutos(long segundosTotales) {
        // División estricta sin redondeos para evitar errores de +1 minuto
        long horas = (segundosTotales) / 3600;
        long minutos = (segundosTotales % 3600) / 60;

        return String.format("%02d:%02d", horas, (--minutos) );
    }
}
