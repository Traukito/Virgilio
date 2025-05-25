package com.cs.virgilio.controller;

import com.cs.virgilio.model.ActivoFueraServicioDto;
import com.cs.virgilio.model.FiltroFechasDto;
import com.cs.virgilio.model.TotalTiempoFueraServicioDto;
import com.cs.virgilio.service.ActivosFueraServicioActualesService;
import com.cs.virgilio.service.HistorialFueraServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ct/virgilio")
public class OutOfServiceController {

    private final HistorialFueraServicioService historialService;
    private final ActivosFueraServicioActualesService actualesService;

    @GetMapping("/inoperativos")
    public String mostrarVistaInoperativos(@RequestParam(required = false) String code, Model model) {
        if (esCodigoValido(code)) {
            cargarHistorialPorCodigo(code, model);
        } else {
            cargarInoperativosActuales(model);
        }

        model.addAttribute("filtroFechasDto", new FiltroFechasDto()); // importante
        return "virgilio/inoperativos";
    }

    @PostMapping("/inoperativos")
    public String buscarHistorialPorActivo(@RequestParam(required = false) String code, Model model) {
        return mostrarVistaInoperativos(code, model);
    }

    @PostMapping("/inoperativos/filtrar")
    public String filtrarHistorialPorFechas(
            @ModelAttribute("filtroFechasDto") @Valid FiltroFechasDto filtro,
            BindingResult result,
            Model model) {

        if (filtro.getFechaInicial() != null && filtro.getFechaFinal() != null
                && filtro.getFechaInicial().isAfter(filtro.getFechaFinal())) {
            result.rejectValue("fechaInicial", "error.fechaInicial", "La fecha inicial no puede ser posterior a la final.");
        }

        if (result.hasErrors()) {
            model.addAttribute("codigoActivo", filtro.getCode());
            model.addAttribute("esHistorial", true);
            return "virgilio/inoperativos";
        }

        TotalTiempoFueraServicioDto resultado = historialService
                .obtenerHistorialYTotalTiempoPorActivoEnRango(
                        filtro.getCode(),
                        filtro.getFechaInicial(),
                        filtro.getFechaFinal())
                .orElse(new TotalTiempoFueraServicioDto(Collections.emptyList(), "00:00"));

        model.addAttribute("historial", resultado.getHistorial());
        model.addAttribute("totalTiempoFueraDeServicio", resultado.getTotalTiempo());
        model.addAttribute("esHistorial", true);
        model.addAttribute("codigoActivo", filtro.getCode());
        model.addAttribute("filtroFechasDto", filtro);

        return "virgilio/inoperativos";
    }

    private void cargarHistorialPorCodigo(String code, Model model) {
        TotalTiempoFueraServicioDto resultado = historialService
                .obtenerHistorialYTotalTiempoPorActivo(code)
                .orElse(new TotalTiempoFueraServicioDto(Collections.emptyList(), "00:00"));

        model.addAttribute("historial", resultado.getHistorial());
        model.addAttribute("totalTiempoFueraDeServicio", resultado.getTotalTiempo());
        model.addAttribute("esHistorial", true);
        model.addAttribute("codigoActivo", code);
    }

    private void cargarInoperativosActuales(Model model) {
        List<ActivoFueraServicioDto> resultados = actualesService
                .obtenerActivosActualmenteFueraDeServicio()
                .orElse(Collections.emptyList());

        model.addAttribute("historial", resultados);
        model.addAttribute("esHistorial", false);
        model.addAttribute("totalFueraDeServicio", resultados.size());
    }

    private boolean esCodigoValido(String code) {
        return code != null && !code.trim().isEmpty();
    }
}
