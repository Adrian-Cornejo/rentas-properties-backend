package com.rentas.properties.api.controller.impl;

import com.rentas.properties.api.controller.DashboardController;
import com.rentas.properties.api.dto.response.DashboardResponse;
import com.rentas.properties.business.services.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardControllerImpl implements DashboardController {

    private final DashboardService dashboardService;

    @Override
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboardData() {
        log.info("Obteniendo datos del dashboard");
        DashboardResponse response = dashboardService.getDashboardData();
        log.info("Datos del dashboard obtenidos exitosamente");
        return ResponseEntity.ok(response);
    }
}