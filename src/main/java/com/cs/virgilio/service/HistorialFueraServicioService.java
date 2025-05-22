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
            try {
                OffsetDateTime inicio = OffsetDateTime.parse(dto.getInitial_date());
                OffsetDateTime fin = (dto.getFinal_date() != null) ?
                        OffsetDateTime.parse(dto.getFinal_date()) : OffsetDateTime.now();

                totalSegundos += Duration.between(inicio, fin).getSeconds();
            } catch (DateTimeParseException ignored) {}
        }

        String tiempoFormateado = TiempoFueraServicioUtils.formatoHorasMinutos(totalSegundos);
        return Optional.of(new TotalTiempoFueraServicioDto(historial, tiempoFormateado));
    }

    public Optional<TotalTiempoFueraServicioDto> obtenerHistorialFiltradoPorFechas(
            String code, OffsetDateTime fechaInicio, OffsetDateTime fechaFin) {

        List<ActivoFueraServicioDto> historialCompleto = obtenerHistorialDeFueraDeServicioPorActivo(code)
                .orElse(Collections.emptyList());

        List<ActivoFueraServicioDto> historialFiltrado = new ArrayList<>();
        long totalSegundos = 0;

        for (ActivoFueraServicioDto dto : historialCompleto) {
            try {
                OffsetDateTime inicio = OffsetDateTime.parse(dto.getInitial_date());
                OffsetDateTime fin = (dto.getFinal_date() != null) ? OffsetDateTime.parse(dto.getFinal_date()) : OffsetDateTime.now();

                boolean dentroDelRango = !(fin.isBefore(fechaInicio) || inicio.isAfter(fechaFin));

                if (dentroDelRango) {
                    historialFiltrado.add(dto);

                    OffsetDateTime inicioReal = inicio.isBefore(fechaInicio) ? fechaInicio : inicio;
                    OffsetDateTime finReal = fin.isAfter(fechaFin) ? fechaFin : fin;

                    totalSegundos += Duration.between(inicioReal, finReal).getSeconds();
                }
            } catch (DateTimeParseException ignored) {}
        }

        String tiempoFormateado = TiempoFueraServicioUtils.formatoHorasMinutos(totalSegundos);
        return Optional.of(new TotalTiempoFueraServicioDto(historialFiltrado, tiempoFormateado));
    }



    /**
     * Este método obtiene el historial de eventos en los que un activo estuvo fuera de servicio
     * y calcula el tiempo total fuera de servicio dentro de un rango de fechas específico.
     *
     * @param code   El código del activo (por ejemplo, un identificador único del equipo).
     * @param inicio Fecha de inicio del rango a evaluar (tipo LocalDate, sin hora).
     * @param fin    Fecha de fin del rango a evaluar (tipo LocalDate, sin hora).
     * @return Un Optional que contiene un DTO con la lista de eventos filtrados por rango
     *         y el tiempo total fuera de servicio dentro de ese rango, en formato "hh:mm".
     */
    public Optional<TotalTiempoFueraServicioDto> obtenerHistorialYTotalTiempoPorActivoEnRango(
            String code, LocalDate inicio, LocalDate fin) {

        boolean debug = true; // Cambia a false para desactivar los prints

        List<ActivoFueraServicioDto> historialCompleto = obtenerHistorialDeFueraDeServicioPorActivo(code)
                .orElse(Collections.emptyList());

        List<ActivoFueraServicioDto> historialFiltrado = new ArrayList<>();
        long totalSegundos = 0;

        for (ActivoFueraServicioDto dto : historialCompleto) {
            try {
                ZoneId zonaSantiago = ZoneId.of("America/Santiago");

                OffsetDateTime inicioOffset = OffsetDateTime.parse(dto.getInitial_date());
                ZonedDateTime inicioDto = inicioOffset.atZoneSameInstant(zonaSantiago);

                ZonedDateTime finDto = (dto.getFinal_date() != null)
                        ? OffsetDateTime.parse(dto.getFinal_date()).atZoneSameInstant(zonaSantiago)
                        : ZonedDateTime.now(zonaSantiago);

                ZonedDateTime inicioFiltro = inicio.atStartOfDay(zonaSantiago);
                ZonedDateTime finFiltro = fin.atTime(23, 59, 59).atZone(zonaSantiago);

                if (debug) {
                    System.out.println("\n==================== EVENTO ====================");
//                    System.out.println("ID: " + dto.getId());
                    System.out.println("Initial_date (UTC): " + dto.getInitial_date());
                    System.out.println("Final_date   (UTC): " + dto.getFinal_date());
                    System.out.println("Initial_date (Chile): " + inicioDto);
                    System.out.println("Final_date   (Chile): " + finDto);
                    System.out.println("Rango filtro inicio (Chile): " + inicioFiltro);
                    System.out.println("Rango filtro fin    (Chile): " + finFiltro);
                }

                if (finDto.isBefore(inicioFiltro) || inicioDto.isAfter(finFiltro)) {
                    if (debug) System.out.println("Evento fuera del rango, se descarta.");
                    continue;
                }

                ZonedDateTime realInicio = inicioDto.isBefore(inicioFiltro) ? inicioFiltro : inicioDto;
                ZonedDateTime realFin = finDto.isAfter(finFiltro) ? finFiltro : finDto;

                long segundosEventoEnRango = Duration.between(realInicio, realFin).getSeconds();

                if (debug) {
                    System.out.println("Inicio considerado: " + realInicio);
                    System.out.println("Fin considerado:    " + realFin);
                    System.out.println("Duración (seg):     " + segundosEventoEnRango);
                    System.out.println("Duración (hh:mm):   " + TiempoFueraServicioUtils.formatoHorasMinutos(segundosEventoEnRango));
                }

                if (segundosEventoEnRango > 0) {
                    dto.setTiempoEnRango(TiempoFueraServicioUtils.formatoHorasMinutos(segundosEventoEnRango));
                    historialFiltrado.add(dto);
                    totalSegundos += segundosEventoEnRango;
                }

            } catch (DateTimeParseException e) {
                if (debug) {
                    System.err.println("ERROR al parsear fechas del evento ID:");
                    e.printStackTrace();
                }
            }
        }

        String tiempoFormateado = TiempoFueraServicioUtils.formatoHorasMinutos(totalSegundos);
        return Optional.of(new TotalTiempoFueraServicioDto(historialFiltrado, tiempoFormateado));
    }



}
