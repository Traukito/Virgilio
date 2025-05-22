package com.cs.virgilio.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class ActivoFueraServicioDto {
    private String description;
    private String code;
    private String initial_date;
    private String final_date;
    private Integer out_of_service_sec;

    @JsonIgnore
    private String tiempoTranscurrido;
    // ActivoFueraServicioDto.java
    private String tiempoEnRango; // formato "HH:mm"


    public String getInitialDateFormateada() {
        try {
            OffsetDateTime fechaUtc = OffsetDateTime.parse(initial_date);
            ZonedDateTime fechaLocal = fechaUtc.atZoneSameInstant(ZoneId.of("America/Santiago"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return fechaLocal.format(formatter);
        } catch (Exception e) {
            return initial_date;
        }
    }

    public String getFinalDateFormateada() {
        try {
            if (final_date == null) return "";
            OffsetDateTime fechaUtc = OffsetDateTime.parse(final_date);
            ZonedDateTime fechaLocal = fechaUtc.atZoneSameInstant(ZoneId.of("America/Santiago"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return fechaLocal.format(formatter);
        } catch (Exception e) {
            return final_date;
        }
    }

    /**
     * Retorna la descripción sin los bloques entre llaves, eliminando espacios
     * y limitando el resultado a un máximo de 50 caracteres.
     * Si el texto es recortado, se añade "..." al final.
     *
     * @return Descripción limpia y truncada si es necesario.
     */
    public String getDescriptionSinLlaves() {
        final int MAX_LENGTH = 70;
        final String SUFIJO_TRUNCADO = "...";

        if (description == null) return "";

        // Elimina bloques entre llaves y espacios sobrantes
        String cleaned = description.replaceAll("\\s*\\{[^}]*\\}\\s*", "").trim();

        if (cleaned.length() <= MAX_LENGTH) {
            return cleaned;
        }

        // Recorta y agrega sufijo "..." indicando truncamiento
        return cleaned.substring(0, MAX_LENGTH - SUFIJO_TRUNCADO.length()) + SUFIJO_TRUNCADO;
    }

}
