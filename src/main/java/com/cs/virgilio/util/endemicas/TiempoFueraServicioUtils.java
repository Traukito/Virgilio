package com.cs.virgilio.util.endemicas;

import java.time.Duration;
import java.time.OffsetDateTime;

public class TiempoFueraServicioUtils {

    public static String calcularTiempoTranscurrido(String inicio) {
        OffsetDateTime fechaInicio = OffsetDateTime.parse(inicio);
        OffsetDateTime ahora = OffsetDateTime.now();
        return formatear(fechaInicio, ahora);
    }

    public static String calcularTiempoTranscurrido(String inicio, String fin) {
        OffsetDateTime fechaInicio = OffsetDateTime.parse(inicio);
        OffsetDateTime fechaFin = OffsetDateTime.parse(fin);
        return formatear(fechaInicio, fechaFin);
    }

    private static String formatear(OffsetDateTime inicio, OffsetDateTime fin) {
        long totalSegundos = Duration.between(inicio, fin).getSeconds();
        long horas = totalSegundos / 3600;
        long minutos = (totalSegundos % 3600) / 60;
        return String.format("%02d:%02d", horas, minutos);
    }

    public static String formatoHorasMinutos(long segundosTotales) {
        long horas = segundosTotales / 3600;
        long minutos = (segundosTotales % 3600) / 60;
        return String.format("%02d:%02d", horas, minutos);
    }
}
