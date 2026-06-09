package edu.unisabana.tyvs.domain.model;

/**
 * Resultados posibles al procesar una transferencia bancaria.
 *
 * Usados en las pruebas TDD para cubrir todas las clases de equivalencia
 * del dominio de transferencias del sistema PJBA / INSOFTCOL
 */
public enum TransferenciaResult {

    /** Transferencia procesada exitosamente. */
    EXITOSA,

    /** Saldo insuficiente en la cuenta origen. */
    SALDO_INSUFICIENTE,

    /** El monto a transferir es inválido (cero, negativo o excede el límite diario). */
    MONTO_INVALIDO,

    /** La cuenta destino no existe o no está registrada. */
    CUENTA_DESTINO_INVALIDA,

    /** El número de cuenta origen está vacío o nulo. */
    CUENTA_ORIGEN_INVALIDA,

    /** La transferencia fue rechazada por la entidad bancaria destino. */
    ENTIDAD_NO_SOPORTADA,

    /** Datos de entrada nulos. */
    DATOS_INVALIDOS
}
