package com.rentas.properties.api.controller;

import com.rentas.properties.api.dto.request.CreateContractRequest;
import com.rentas.properties.api.dto.request.UpdateContractRequest;
import com.rentas.properties.api.dto.request.UpdateDepositStatusRequest;
import com.rentas.properties.api.dto.response.ContractDetailResponse;
import com.rentas.properties.api.dto.response.ContractResponse;
import com.rentas.properties.api.dto.response.ContractSummaryResponse;
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

@Tag(name = "Contracts", description = "Endpoints para gestión de contratos de arrendamiento")
public interface ContractController {

    @Operation(
            summary = "Crear nuevo contrato",
            description = "Crea un nuevo contrato de arrendamiento. Valida que la propiedad y arrendatarios pertenezcan a la misma organización. " +
                    "Cambia el estado de la propiedad a RENTADA y genera pagos automáticos."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contrato creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ContractDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o la propiedad ya está rentada"),
            @ApiResponse(responseCode = "404", description = "Propiedad o arrendatarios no encontrados"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<ContractDetailResponse> createContract(@Valid @RequestBody CreateContractRequest request);

    @Operation(
            summary = "Obtener todos los contratos",
            description = "Lista todos los contratos de la organización del usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = ContractResponse.class))
            )
    })
    ResponseEntity<List<ContractResponse>> getAllContracts(
            @Parameter(description = "Incluir contratos inactivos")
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    );

    @Operation(
            summary = "Obtener contrato por ID",
            description = "Obtiene los detalles completos de un contrato específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contrato encontrado",
                    content = @Content(schema = @Schema(implementation = ContractDetailResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Contrato no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes acceso a este contrato")
    })
    ResponseEntity<ContractDetailResponse> getContractById(@PathVariable UUID id);

    @Operation(
            summary = "Obtener contrato por número",
            description = "Busca un contrato por su número único"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contrato encontrado",
                    content = @Content(schema = @Schema(implementation = ContractDetailResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Contrato no encontrado")
    })
    ResponseEntity<ContractDetailResponse> getContractByNumber(@RequestParam String contractNumber);

    @Operation(
            summary = "Actualizar contrato",
            description = "Actualiza la información de un contrato existente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contrato actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ContractDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Contrato no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<ContractDetailResponse> updateContract(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateContractRequest request
    );

    @Operation(
            summary = "Eliminar contrato",
            description = "Desactiva un contrato (soft delete). Cambia el estado de la propiedad a DISPONIBLE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contrato eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Contrato no encontrado"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar porque tiene pagos pendientes"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<Void> deleteContract(@PathVariable UUID id);

    @Operation(
            summary = "Listar contratos por organización",
            description = "Obtiene todos los contratos de una organización específica"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = ContractResponse.class))
            )
    })
    ResponseEntity<List<ContractResponse>> getContractsByOrganization(@PathVariable UUID organizationId);

    @Operation(
            summary = "Obtener contratos por estado",
            description = "Filtra contratos por estado (ACTIVO, VENCIDO, RENOVADO, CANCELADO)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = ContractResponse.class))
            )
    })
    ResponseEntity<List<ContractResponse>> getContractsByStatus(@RequestParam String status);

    @Operation(
            summary = "Obtener contratos activos",
            description = "Lista todos los contratos con estado ACTIVO de la organización"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = ContractResponse.class))
            )
    })
    ResponseEntity<List<ContractResponse>> getActiveContracts();

    @Operation(
            summary = "Obtener contratos próximos a vencer",
            description = "Lista contratos que vencen en los próximos N días"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = ContractResponse.class))
            )
    })
    ResponseEntity<List<ContractResponse>> getExpiringContracts(
            @Parameter(description = "Días hasta el vencimiento")
            @RequestParam(required = false, defaultValue = "30") int days
    );

    @Operation(
            summary = "Obtener contratos con depósito pendiente",
            description = "Lista contratos activos que aún no han pagado el depósito"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = ContractResponse.class))
            )
    })
    ResponseEntity<List<ContractResponse>> getContractsWithPendingDeposit();

    @Operation(
            summary = "Obtener contrato activo de una propiedad",
            description = "Obtiene el contrato activo asociado a una propiedad específica"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contrato encontrado",
                    content = @Content(schema = @Schema(implementation = ContractDetailResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "No hay contrato activo para esta propiedad")
    })
    ResponseEntity<ContractDetailResponse> getActiveContractByProperty(@PathVariable UUID propertyId);

    @Operation(
            summary = "Actualizar estado del depósito",
            description = "Actualiza el estado del depósito de un contrato (PAGADO, RETENIDO, DEVUELTO, USADO_REPARACIONES)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado del depósito actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ContractDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Contrato no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<ContractDetailResponse> updateDepositStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDepositStatusRequest request
    );

    @Operation(
            summary = "Renovar contrato",
            description = "Renueva un contrato vencido creando uno nuevo con los mismos datos"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contrato renovado exitosamente",
                    content = @Content(schema = @Schema(implementation = ContractDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "El contrato no puede ser renovado"),
            @ApiResponse(responseCode = "404", description = "Contrato no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<ContractDetailResponse> renewContract(@PathVariable UUID id);

    @Operation(
            summary = "Cancelar contrato",
            description = "Cancela un contrato antes de su vencimiento. Cambia estado a CANCELADO y libera la propiedad."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contrato cancelado exitosamente",
                    content = @Content(schema = @Schema(implementation = ContractDetailResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Contrato no encontrado"),
            @ApiResponse(responseCode = "400", description = "No se puede cancelar un contrato ya finalizado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<ContractDetailResponse> cancelContract(@PathVariable UUID id);

    @Operation(
            summary = "Obtener resumen de contratos",
            description = "Obtiene estadísticas y resumen de contratos de la organización"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumen obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = ContractSummaryResponse.class))
            )
    })
    ResponseEntity<ContractSummaryResponse> getContractsSummary();
}