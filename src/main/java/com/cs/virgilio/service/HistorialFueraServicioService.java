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

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;

@RequiredArgsConstructor
@Service
public class HistorialFueraServicioService {

    private final FracttalRequestService fracttalRequestService;

    public Optional<List<ActivoFueraServicioDto>> obtenerHistorialDeFueraDeServicioPorActivo(String code) {
        try {
            FracttalResponseDto<ActivoFueraServicioDto> response = fracttalRequestService.realizarPeticionFracttal(
                    "/api/items_availability/" + code,
                    new ParameterizedTypeReference<>() {}
            );
            List<ActivoFueraServicioDto> historial = procesarHistorial(code, response);
            historial.sort(Comparator.comparing(dto -> OffsetDateTime.parse(dto.getInitial_date()), Comparator.reverseOrder()));
            return Optional.of(historial);
        } catch (HttpClientErrorException.NotFound e) {
            throw new CodigoNoEncontradoException("Código no encontrado: " + code);
        } catch (Exception e) {
            throw new HawkServiceException("Sin historial o error en el código del Activo", e);
        }
    }



    private List<ActivoFueraServicioDto> procesarHistorial(String code, FracttalResponseDto<ActivoFueraServicioDto> body) {
        List<ActivoFueraServicioDto> datos = Optional.ofNullable(body.getData()).orElseThrow(
                () -> new CodigoSinHistorialException("No hay historial para el código: " + code)
        );

        for (ActivoFueraServicioDto dto : datos) {
            String ini = dto.getInitial_date();
            String fin = dto.getFinal_date();
            if (ini != null && fin != null) {
                dto.setTiempoTranscurrido(TiempoFueraServicioUtils.calcularTiempoTranscurrido(ini, fin));
            } else if (ini != null) {
                dto.setTiempoTranscurrido(TiempoFueraServicioUtils.calcularTiempoTranscurrido(ini));
            }
        }

        return datos;
    }



    public Optional<TotalTiempoFueraServicioDto> obtenerHistorialYTotalTiempoPorActivo(String code) {
        List<ActivoFueraServicioDto> historial = obtenerHistorialDeFueraDeServicioPorActivo(code)
                .orElse(Collections.emptyList());

        long totalSegundos = historial.stream()
                .mapToLong(this::duracionEvento)
                .sum();

        String tiempoFormateado = TiempoFueraServicioUtils.formatoHorasMinutos(totalSegundos);
        return Optional.of(new TotalTiempoFueraServicioDto(historial, tiempoFormateado));
    }

    public Optional<TotalTiempoFueraServicioDto> obtenerHistorialFiltradoPorFechas(
            String code, OffsetDateTime fechaInicio, OffsetDateTime fechaFin) {

        List<ActivoFueraServicioDto> historial = obtenerHistorialDeFueraDeServicioPorActivo(code)
                .orElse(Collections.emptyList());

        List<ActivoFueraServicioDto> filtrados = new ArrayList<>();
        long totalSegundos = 0;

        for (ActivoFueraServicioDto dto : historial) {
            try {
                OffsetDateTime ini = OffsetDateTime.parse(dto.getInitial_date());
                OffsetDateTime fin = (dto.getFinal_date() != null) ? OffsetDateTime.parse(dto.getFinal_date()) : OffsetDateTime.now();

                if (!(fin.isBefore(fechaInicio) || ini.isAfter(fechaFin))) {
                    OffsetDateTime realIni = ini.isBefore(fechaInicio) ? fechaInicio : ini;
                    OffsetDateTime realFin = fin.isAfter(fechaFin) ? fechaFin : fin;

                    long seg = Duration.between(realIni, realFin).getSeconds();
                    dto.setTiempoEnRango(TiempoFueraServicioUtils.formatoHorasMinutos(seg));
                    filtrados.add(dto);
                    totalSegundos += seg;
                }

            } catch (DateTimeParseException ignored) {}
        }

        String tiempoFormateado = TiempoFueraServicioUtils.formatoHorasMinutos(totalSegundos);
        return Optional.of(new TotalTiempoFueraServicioDto(filtrados, tiempoFormateado));
    }



    public Optional<TotalTiempoFueraServicioDto> obtenerHistorialYTotalTiempoPorActivoEnRango(
            String code, LocalDate inicio, LocalDate fin) {

        ZoneId zona = ZoneId.of("America/Santiago");

        ZonedDateTime filtroInicio = inicio.atStartOfDay(zona);
        ZonedDateTime filtroFin = fin.atTime(23, 59, 59).atZone(zona);

        List<ActivoFueraServicioDto> historial = obtenerHistorialDeFueraDeServicioPorActivo(code)
                .orElse(Collections.emptyList());

        List<ActivoFueraServicioDto> filtrados = new ArrayList<>();
        long totalSegundos = 0;

        for (ActivoFueraServicioDto dto : historial) {
            try {
                ZonedDateTime ini = OffsetDateTime.parse(dto.getInitial_date()).atZoneSameInstant(zona);
                ZonedDateTime finZ = (dto.getFinal_date() != null)
                        ? OffsetDateTime.parse(dto.getFinal_date()).atZoneSameInstant(zona)
                        : ZonedDateTime.now(zona);

                if (!(finZ.isBefore(filtroInicio) || ini.isAfter(filtroFin))) {
                    ZonedDateTime realIni = ini.isBefore(filtroInicio) ? filtroInicio : ini;
                    ZonedDateTime realFin = finZ.isAfter(filtroFin) ? filtroFin : finZ;

                    long seg = Duration.between(realIni, realFin).getSeconds();
                    if (seg > 0) {
                        dto.setTiempoEnRango(TiempoFueraServicioUtils.formatoHorasMinutos(seg));
                        filtrados.add(dto);
                        totalSegundos += seg;
                    }
                }

            } catch (DateTimeParseException ignored) {}
        }

        String tiempoFormateado = TiempoFueraServicioUtils.formatoHorasMinutos(totalSegundos);
        return Optional.of(new TotalTiempoFueraServicioDto(filtrados, tiempoFormateado));
    }



    private long duracionEvento(ActivoFueraServicioDto dto) {
        try {
            OffsetDateTime ini = OffsetDateTime.parse(dto.getInitial_date());
            OffsetDateTime fin = (dto.getFinal_date() != null) ? OffsetDateTime.parse(dto.getFinal_date()) : OffsetDateTime.now();
            return Duration.between(ini, fin).getSeconds();
        } catch (DateTimeParseException e) {
            return 0;
        }
    }
}
