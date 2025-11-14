package com.rentas.properties.api.controller.impl;

import com.rentas.properties.api.controller.UserController;
import com.rentas.properties.api.dto.request.JoinOrganizationRequest;
import com.rentas.properties.api.dto.request.UpdateUserRequest;
import com.rentas.properties.api.dto.response.UserDetailResponse;
import com.rentas.properties.api.dto.response.UserResponse;
import com.rentas.properties.business.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserControllerImpl implements UserController {

    private final UserService userService;

    @Override
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    ) {
        log.info("Obteniendo todos los usuarios - includeInactive: {}", includeInactive);
        List<UserResponse> users = userService.getAllUsers(includeInactive);
        log.info("Se encontraron {} usuarios", users.size());
        return ResponseEntity.ok(users);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<UserDetailResponse> getUserById(@PathVariable UUID id) {
        log.info("Obteniendo usuario con ID: {}", id);
        UserDetailResponse response = userService.getUserById(id);
        log.info("Usuario encontrado: {}", response.getEmail());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<UserDetailResponse> getCurrentUserProfile() {
        log.info("Obteniendo perfil del usuario actual");
        UserDetailResponse response = userService.getCurrentUserProfile();
        log.info("Perfil obtenido: {}", response.getEmail());
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<UserDetailResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        log.info("Actualizando usuario con ID: {}", id);
        UserDetailResponse response = userService.updateUser(id, request);
        log.info("Usuario actualizado exitosamente: {}", response.getEmail());
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID id) {
        log.info("Desactivando usuario con ID: {}", id);
        userService.deactivateUser(id);
        log.info("Usuario desactivado exitosamente");
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable UUID id) {
        log.info("Activando usuario con ID: {}", id);
        userService.activateUser(id);
        log.info("Usuario activado exitosamente");
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/join-organization")
    public ResponseEntity<UserDetailResponse> joinOrganization(@Valid @RequestBody JoinOrganizationRequest request) {
        log.info("Usuario intentando unirse a organización con código: {}", request.getInvitationCode());
        UserDetailResponse response = userService.joinOrganization(request);
        log.info("Usuario {} unido exitosamente a organización", response.getEmail());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/organization")
    public ResponseEntity<List<UserResponse>> getUsersByOrganization() {
        log.info("Obteniendo usuarios de la organización");
        List<UserResponse> users = userService.getUsersByOrganization();
        log.info("Se encontraron {} usuarios en la organización", users.size());
        return ResponseEntity.ok(users);
    }

    @Override
    @GetMapping("/without-organization")
    public ResponseEntity<List<UserResponse>> getUsersWithoutOrganization() {
        log.info("Obteniendo usuarios sin organización");
        List<UserResponse> users = userService.getUsersWithoutOrganization();
        log.info("Se encontraron {} usuarios sin organización", users.size());
        return ResponseEntity.ok(users);
    }

    @Override
    @GetMapping("/by-status")
    public ResponseEntity<List<UserResponse>> getUsersByAccountStatus(@RequestParam String status) {
        log.info("Obteniendo usuarios por estado: {}", status);
        List<UserResponse> users = userService.getUsersByAccountStatus(status);
        log.info("Se encontraron {} usuarios con estado {}", users.size(), status);
        return ResponseEntity.ok(users);
    }
}