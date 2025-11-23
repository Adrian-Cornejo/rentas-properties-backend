package com.rentas.properties.api.controller.impl;

import com.rentas.properties.api.controller.PublicPropertyController;
import com.rentas.properties.api.dto.response.PublicPropertyResponse;
import com.rentas.properties.business.services.PublicPropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/properties")
@RequiredArgsConstructor
@Slf4j
public class PublicPropertyControllerImpl implements PublicPropertyController {

    private final PublicPropertyService publicPropertyService;

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<PublicPropertyResponse> getPublicProperty(@PathVariable UUID id) {
        log.info("Solicitud pública de propiedad con ID: {}", id);
        PublicPropertyResponse response = publicPropertyService.getPublicPropertyById(id);
        log.info("Propiedad pública enviada: {}", response.getPropertyCode());
        return ResponseEntity.ok(response);
    }
}