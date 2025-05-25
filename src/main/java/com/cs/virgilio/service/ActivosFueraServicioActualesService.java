package com.cs.virgilio.service;

import com.cs.virgilio.exception.HawkServiceException;
import com.cs.virgilio.model.ActivoFueraServicioDto;
import com.cs.virgilio.model.FracttalResponseDto;
import com.cs.virgilio.util.authservice.FracttalRequestService;
import com.cs.virgilio.util.endemicas.TiempoFueraServicioUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class ActivosFueraServicioActualesService {

    private final FracttalRequestService fracttalRequestService;

    public Optional<List<ActivoFueraServicioDto>> obtenerActivosActualmenteFueraDeServicio() {
        List<ActivoFueraServicioDto> resultados = new ArrayList<>();
        int start = 0;
        int limit = 100;

        try {
            while (true) {
                String ruta = "/api/items_availability/?start=" + start + "&limit=" + limit;
                FracttalResponseDto<ActivoFueraServicioDto> respuesta = fracttalRequestService.realizarPeticionFracttal(
                        ruta, new ParameterizedTypeReference<>() {}
                );

                List<ActivoFueraServicioDto> data = respuesta.getData();
                if (data == null || data.isEmpty()) break;

                data.stream()
                        .filter(dto -> dto.getFinal_date() == null)
                        .peek(dto -> dto.setTiempoTranscurrido(
                                TiempoFueraServicioUtils.calcularTiempoTranscurrido(dto.getInitial_date())))
                        .forEach(resultados::add);

                if (data.size() < limit) break;
                start += limit;
            }

            resultados.sort(Comparator.comparing(ActivoFueraServicioDto::getInitial_date).reversed());
            return Optional.of(resultados);

        } catch (Exception e) {
            throw new HawkServiceException("Error al obtener activos fuera de servicio", e);
        }
    }
}
