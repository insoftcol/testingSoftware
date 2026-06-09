package edu.unisabana.tyvs.domain.model;

/**
 * Resultados posibles al registrar una persona en la Registraduría.
 *
 * <p>Los valores se fueron agregando ciclo a ciclo durante el TDD:</p>
 * <ul>
 *   <li>Ciclo 1 (camino feliz): {@code VALID, DUPLICATED, INVALID}</li>
 *   <li>Ciclo 2 (persona muerta): {@code DEAD}</li>
 *   <li>Ciclo 3 (edad inválida): {@code INVALID_AGE}</li>
 *   <li>Ciclo 4 (menor de edad):  {@code UNDERAGE}</li>
 * </ul>
 */
public enum RegisterResult {

    /** Registro aprobado: la persona cumple todos los requisitos. */
    VALID,

    /** El número de documento ya fue registrado anteriormente. */
    DUPLICATED,

    /** Datos inválidos: persona nula, id ≤ 0 o formato incorrecto. */
    INVALID,

    /** La persona no está viva y no puede ser registrada. */
    DEAD,

    /** La edad está fuera del rango permitido (negativa o mayor a 120). */
    INVALID_AGE,

    /** La persona es menor de 18 años y no puede votar. */
    UNDERAGE
}
