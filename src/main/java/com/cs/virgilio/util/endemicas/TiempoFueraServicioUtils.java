package com.cs.virgilio.util.endemicas;

import java.time.Duration;
import java.time.OffsetDateTime;

public class TiempoFueraServicioUtils {

    public static String calcularTiempoTranscurrido(String inicio) {
        OffsetDateTime fechaInicio = OffsetDateTime.parse(inicio);
        OffsetDateTime ahora = OffsetDateTime.now();
        return formatear(Duration.between(fechaInicio, ahora));
    }

    public static String calcularTiempoTranscurrido(String inicio, String fin) {
        OffsetDateTime fechaInicio = OffsetDateTime.parse(inicio);
        OffsetDateTime fechaFin = OffsetDateTime.parse(fin);
        return formatear(Duration.between(fechaInicio, fechaFin));
    }

    private static String formatear(Duration duracion) {
        long horas = duracion.toHours();
        long minutos = duracion.toMinutes() % 60;
        return horas + ":" + minutos;
    }
}
