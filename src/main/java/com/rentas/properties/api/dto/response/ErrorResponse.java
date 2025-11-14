package com.rentas.properties.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta de error estándar de la API")
public class ErrorResponse {

    @Schema(description = "Timestamp del error", example = "2025-11-14T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Código de estado HTTP", example = "400")
    private Integer status;

    @Schema(description = "Mensaje de error principal", example = "Validation error")
    private String error;

    @Schema(description = "Mensaje descriptivo del error", example = "El email ya está registrado")
    private String message;

    @Schema(description = "Path del endpoint donde ocurrió el error", example = "/api/v1/properties")
    private String path;

    @Schema(description = "Errores de validación por campo")
    private Map<String, String> validationErrors;

    @Schema(description = "Lista de errores adicionales")
    private List<String> errors;

    @Schema(description = "Código de error interno", example = "PROPERTY_NOT_FOUND")
    private String errorCode;

    @Schema(description = "Información adicional de debug (solo en desarrollo)")
    private String debugInfo;

    public static ErrorResponse of(Integer status, String error, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }

    public static ErrorResponse withValidationErrors(
            Integer status,
            String error,
            String message,
            String path,
            Map<String, String> validationErrors
    ) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .validationErrors(validationErrors)
                .build();
    }
}