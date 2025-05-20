package com.cs.virgilio.controller;

import com.cs.virgilio.model.ActivoFueraServicioDto;
import com.cs.virgilio.model.TotalTiempoFueraServicioDto;
import com.cs.virgilio.service.ActivosFueraServicioActualesService;
import com.cs.virgilio.service.HistorialFueraServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ct/virgilio")
public class OutOfServiceController {

    private final HistorialFueraServicioService historialService;
    private final ActivosFueraServicioActualesService actualesService;



    /**
     * GET: Muestra activos fuera de servicio actuales o historial por activo si se especifica un código.
     */
    @GetMapping("/inoperativos")
    public String mostrarVistaInoperativos(@RequestParam(required = false) String code, Model model) {
        boolean tieneCodigoActivo = esCodigoValido(code);

        if (tieneCodigoActivo) {
            // Obtenemos historial y total de tiempo fuera de servicio
            TotalTiempoFueraServicioDto resultado = historialService.obtenerHistorialYTotalTiempoPorActivo(code)
                    .orElse(new TotalTiempoFueraServicioDto(Collections.emptyList(), "00:00"));

            model.addAttribute("historial", resultado.getHistorial());
            model.addAttribute("totalTiempoFueraDeServicio", resultado.getTotalTiempo());
            model.addAttribute("esHistorial", true);
        } else {
            List<ActivoFueraServicioDto> resultados = actualesService.obtenerActivosActualmenteFueraDeServicio()
                    .orElse(Collections.emptyList());

            model.addAttribute("historial", resultados);
            model.addAttribute("esHistorial", false);
            model.addAttribute("totalFueraDeServicio", resultados.size());
        }

        return "virgilio/inoperativos";
    }





    /**
     * POST: Procesa búsqueda por código de activo, delegando al método GET.
     */
    @PostMapping("/inoperativos")
    public String buscarHistorialPorActivo(@RequestParam(required = false) String code, Model model) {
        return mostrarVistaInoperativos(code, model);
    }




    /**
     * Valida que el código no sea nulo ni vacío.
     */
    private boolean esCodigoValido(String code) {
        return code != null && !code.trim().isEmpty();
    }
}
