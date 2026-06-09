package edu.unisabana.tyvs.logistica.domain.model;

/**
 * Modalidades de envío disponibles en el sistema logístico.
 * Cada tipo aplica un multiplicador diferente sobre la tarifa base.
 *
 * <ul>
 *   <li>NORMAL      – sin prioridad, multiplicador ×1.0</li>
 *   <li>EXPRESO     – entrega prioritaria, multiplicador ×1.5</li>
 *   <li>REFRIGERADO – cadena de frío requerida, multiplicador ×2.0</li>
 * </ul>
 */
public enum TipoEnvio {
    NORMAL,
    EXPRESO,
    REFRIGERADO
}
