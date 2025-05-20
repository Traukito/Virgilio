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
        List<ActivoFueraServicioDto> acumulado = new ArrayList<>();
        int start = 0;
        int limit = 100;

        try {
            while (true) {
                String route = "/api/items_availability/?start=" + start + "&limit=" + limit;
                FracttalResponseDto<ActivoFueraServicioDto> responseDto = fracttalRequestService.realizarPeticionFracttal(
                        route, new ParameterizedTypeReference<>() {}
                );
                List<ActivoFueraServicioDto> data = responseDto.getData();

                if (data == null || data.isEmpty()) break;

                data.stream()
                        .filter(dto -> dto.getFinal_date() == null)
                        .peek(dto -> dto.setTiempoTranscurrido(
                                TiempoFueraServicioUtils.calcularTiempoTranscurrido(dto.getInitial_date())))
                        .forEach(acumulado::add);


                if (data.size() < limit) break;
                start += limit;
            }

            acumulado.sort(Comparator.comparing(ActivoFueraServicioDto::getInitial_date).reversed());
            return Optional.of(acumulado);

        } catch (Exception e) {
            throw new HawkServiceException("Error obteniendo activos fuera de servicio", e);
        }
    }
}
