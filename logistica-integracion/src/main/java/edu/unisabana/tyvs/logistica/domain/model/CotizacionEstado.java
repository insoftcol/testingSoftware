package edu.unisabana.tyvs.logistica.domain.model;

/**
 * Estados posibles al procesar una solicitud de cotización de envío.
 * Cada valor corresponde a una clase de equivalencia del dominio logístico.
 */
public enum CotizacionEstado {

    /** Cotización calculada exitosamente; {@code Cotizacion.valorTotal} contiene el precio. */
    EXITOSA,

    /** Peso ≤ 0 o supera el límite máximo permitido. */
    PESO_INVALIDO,

    /** Ciudad de origen nula, vacía o no registrada en el sistema. */
    CIUDAD_ORIGEN_INVALIDA,

    /** Ciudad de destino nula, vacía o no registrada en el sistema. */
    CIUDAD_DESTINO_INVALIDA,

    /** Ciudad de origen igual a la ciudad de destino. */
    MISMA_CIUDAD,

    /** La ruta origen–destino no tiene tarifa configurada. */
    RUTA_NO_DISPONIBLE,

    /** La solicitud completa es nula. */
    DATOS_INVALIDOS
}
