package com.cs.virgilio.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TotalTiempoFueraServicioDto {
    private List<ActivoFueraServicioDto> historial;
    private String totalTiempo; // en formato "HH:mm"
}
