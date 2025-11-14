package com.rentas.properties.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePropertyRequest {

    private UUID locationId;

    @NotBlank(message = "El código de propiedad es obligatorio")
    @Size(max = 50, message = "El código no debe exceder 50 caracteres")
    @Pattern(
            regexp = "^[A-Z0-9-]+$",
            message = "El código solo puede contener letras mayúsculas, números y guiones"
    )
    private String propertyCode;

    @NotBlank(message = "El tipo de propiedad es obligatorio")
    @Pattern(
            regexp = "^(CASA|DEPARTAMENTO|LOCAL_COMERCIAL)$",
            message = "El tipo debe ser: CASA, DEPARTAMENTO o LOCAL_COMERCIAL"
    )
    private String propertyType;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 500, message = "La dirección no debe exceder 500 caracteres")
    private String address;

    @NotNull(message = "La renta mensual es obligatoria")
    @DecimalMin(value = "0.01", message = "La renta mensual debe ser mayor a 0")
    @DecimalMax(value = "999999.99", message = "La renta mensual no debe exceder 999,999.99")
    private BigDecimal monthlyRent;

    @DecimalMin(value = "0.00", message = "El costo del agua no puede ser negativo")
    @DecimalMax(value = "9999.99", message = "El costo del agua no debe exceder 9,999.99")
    private BigDecimal waterFee;

    @Min(value = 1, message = "Los pisos deben ser al menos 1")
    @Max(value = 10, message = "Los pisos no pueden exceder 10")
    private Integer floors;

    @Min(value = 0, message = "Las recámaras no pueden ser negativas")
    @Max(value = 20, message = "Las recámaras no pueden exceder 20")
    private Integer bedrooms;

    @Min(value = 0, message = "Los baños no pueden ser negativos")
    @Max(value = 20, message = "Los baños no pueden exceder 20")
    private Integer bathrooms;

    @Min(value = 0, message = "Los medios baños no pueden ser negativos")
    @Max(value = 20, message = "Los medios baños no pueden exceder 20")
    private Integer halfBathrooms;

    private Boolean hasLivingRoom;

    private Boolean hasDiningRoom;

    private Boolean hasKitchen;

    private Boolean hasServiceArea;

    @Min(value = 0, message = "Los espacios de estacionamiento no pueden ser negativos")
    @Max(value = 20, message = "Los espacios de estacionamiento no pueden exceder 20")
    private Integer parkingSpaces;

    @DecimalMin(value = "0.00", message = "El área no puede ser negativa")
    @DecimalMax(value = "99999.99", message = "El área no debe exceder 99,999.99 m²")
    private BigDecimal totalAreaM2;

    private Boolean includesWater;

    private Boolean includesElectricity;

    private Boolean includesGas;

    private Boolean includesInternet;

    @Size(max = 2000, message = "Las notas no deben exceder 2000 caracteres")
    private String notes;
}