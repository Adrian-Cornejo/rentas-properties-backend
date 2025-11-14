package com.rentas.properties.api.controller;

import com.rentas.properties.api.dto.request.JoinOrganizationRequest;
import com.rentas.properties.api.dto.request.UpdateUserRequest;
import com.rentas.properties.api.dto.response.UserDetailResponse;
import com.rentas.properties.api.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(name = "Users", description = "Endpoints para gestión de usuarios")
public interface UserController {

    @Operation(
            summary = "Obtener todos los usuarios",
            description = "Lista todos los usuarios de la organización. Solo ADMIN puede ver todos los usuarios del sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            )
    })
    ResponseEntity<List<UserResponse>> getAllUsers(
            @Parameter(description = "Incluir usuarios inactivos")
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    );

    @Operation(
            summary = "Obtener usuario por ID",
            description = "Obtiene los detalles de un usuario específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = UserDetailResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes acceso a este usuario")
    })
    ResponseEntity<UserDetailResponse> getUserById(@PathVariable UUID id);

    @Operation(
            summary = "Obtener perfil del usuario actual",
            description = "Obtiene los detalles del usuario autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = UserDetailResponse.class))
            )
    })
    ResponseEntity<UserDetailResponse> getCurrentUserProfile();

    @Operation(
            summary = "Actualizar usuario",
            description = "Actualiza la información de un usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = UserDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<UserDetailResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    );

    @Operation(
            summary = "Desactivar usuario",
            description = "Desactiva un usuario. Solo ADMIN puede desactivar usuarios."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario desactivado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<Void> deactivateUser(@PathVariable UUID id);

    @Operation(
            summary = "Activar usuario",
            description = "Activa un usuario previamente desactivado. Solo ADMIN puede activar usuarios."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario activado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<Void> activateUser(@PathVariable UUID id);

    @Operation(
            summary = "Unirse a una organización",
            description = "Permite a un usuario unirse a una organización mediante un código de invitación"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario unido exitosamente a la organización",
                    content = @Content(schema = @Schema(implementation = UserDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Código inválido o usuario ya tiene organización"),
            @ApiResponse(responseCode = "409", description = "La organización alcanzó el límite de usuarios")
    })
    ResponseEntity<UserDetailResponse> joinOrganization(@Valid @RequestBody JoinOrganizationRequest request);

    @Operation(
            summary = "Obtener usuarios de la organización",
            description = "Lista todos los usuarios de la organización del usuario autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            )
    })
    ResponseEntity<List<UserResponse>> getUsersByOrganization();

    @Operation(
            summary = "Obtener usuarios sin organización",
            description = "Lista usuarios pendientes sin organización asignada. Solo ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<List<UserResponse>> getUsersWithoutOrganization();

    @Operation(
            summary = "Obtener usuarios por estado de cuenta",
            description = "Lista usuarios según su estado de cuenta (pending, active, suspended)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            )
    })
    ResponseEntity<List<UserResponse>> getUsersByAccountStatus(@RequestParam String status);
}