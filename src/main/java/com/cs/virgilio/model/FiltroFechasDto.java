package com.cs.virgilio.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class FiltroFechasDto {

    @NotNull(message = "El código del activo es obligatorio.")
    private String code;

    @NotNull(message = "La fecha inicial es obligatoria.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicial;

    @NotNull(message = "La fecha final es obligatoria.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFinal;
}
