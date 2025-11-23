package com.rentas.properties.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información pública de una propiedad para compartir")
public class PublicPropertyResponse {

    @Schema(description = "ID de la propiedad")
    private UUID id;

    @Schema(description = "Código único de la propiedad", example = "CASA-001")
    private String propertyCode;

    @Schema(description = "Tipo de propiedad", example = "CASA")
    private String propertyType;

    @Schema(description = "Numero de pisos", example = "1")
    private  Integer floors;

    @Schema(description = "Dirección completa")
    private String address;

    @Schema(description = "Renta mensual")
    private BigDecimal monthlyRent;

    @Schema(description = "Cuota de agua mensual")
    private BigDecimal waterFee;

    @Schema(description = "Número de recámaras")
    private Integer bedrooms;

    @Schema(description = "Número de baños completos")
    private Integer bathrooms;

    @Schema(description = "Número de medios baños")
    private Integer halfBathrooms;

    @Schema(description = "Tiene sala")
    private Boolean hasLivingRoom;

    @Schema(description = "Tiene comedor")
    private Boolean hasDiningRoom;

    @Schema(description = "Tiene cocina")
    private Boolean hasKitchen;

    @Schema(description = "Tiene área de servicio")
    private Boolean hasServiceArea;

    @Schema(description = "Espacios de estacionamiento")
    private Integer parkingSpaces;

    @Schema(description = "Área total en metros cuadrados")
    private BigDecimal totalAreaM2;

    @Schema(description = "Incluye agua en la renta")
    private Boolean includesWater;

    @Schema(description = "Incluye electricidad en la renta")
    private Boolean includesElectricity;

    @Schema(description = "Incluye gas en la renta")
    private Boolean includesGas;

    @Schema(description = "Incluye internet en la renta")
    private Boolean includesInternet;

    @Schema(description = "Notas adicionales")
    private String notes;

    @Schema(description = "URLs de imágenes de la propiedad")
    private List<String> imageUrls;

    @Schema(description = "Nombre de la ubicación")
    private String locationName;

    @Schema(description = "Ciudad")
    private String city;

    @Schema(description = "Estado")
    private String state;

    @Schema(description = "Nombre de la organización propietaria")
    private String organizationName;

    @Schema(description = "Logo de la organización")
    private String organizationLogo;

    @Schema(description = "Color principal de la organización")
    private String organizationPrimaryColor;
}