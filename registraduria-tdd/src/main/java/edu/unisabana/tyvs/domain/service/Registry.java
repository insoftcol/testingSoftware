package edu.unisabana.tyvs.domain.service;

import edu.unisabana.tyvs.domain.model.Person;
import edu.unisabana.tyvs.domain.model.RegisterResult;

import java.util.HashSet;
import java.util.Set;

/**
 * Servicio de dominio: gestiona el registro de votantes en la Registraduría.
 *
 * <h3>Reglas de negocio (en orden de evaluación)</h3>
 * <ol>
 *   <li>La persona no puede ser {@code null} → {@code INVALID}</li>
 *   <li>El id debe ser positivo (id &gt; 0) → {@code INVALID}</li>
 *   <li>La persona debe estar viva → {@code DEAD}</li>
 *   <li>La edad debe estar en [0, {@value #MAX_AGE}] → {@code INVALID_AGE}</li>
 *   <li>La edad debe ser al menos {@value #MIN_AGE} años → {@code UNDERAGE}</li>
 *   <li>El id no debe estar ya registrado → {@code DUPLICATED}</li>
 *   <li>Todos los criterios cumplidos → {@code VALID}</li>
 * </ol>
 *
 * <p>Mantiene estado interno (ids registrados) para detectar duplicados.
 * Cada instancia representa una sesión de registro independiente.</p>
 */
public class Registry {

    /** Edad mínima para votar. */
    public static final int MIN_AGE = 18;

    /** Edad máxima válida para un ser humano. */
    public static final int MAX_AGE = 120;

    /** Identificadores ya registrados en esta sesión. */
    private final Set<Integer> registeredIds = new HashSet<>();

    /**
     * Intenta registrar a una persona como votante.
     *
     * @param p persona a registrar (puede ser {@code null})
     * @return resultado del intento de registro
     */
    public RegisterResult registerVoter(Person p) {

        // CICLO 2 GREEN — validación defensiva: persona nula
        if (p == null) {
            return RegisterResult.INVALID;
        }

        // CICLO 3 GREEN — id inválido (cero o negativo)
        if (p.getId() <= 0) {
            return RegisterResult.INVALID;
        }

        // CICLO 1 GREEN — persona muerta
        if (!p.isAlive()) {
            return RegisterResult.DEAD;
        }

        // CICLO 4 GREEN — edad fuera de rango
        if (p.getAge() < 0 || p.getAge() > MAX_AGE) {
            return RegisterResult.INVALID_AGE;
        }

        // CICLO 5 GREEN — menor de edad
        if (p.getAge() < MIN_AGE) {
            return RegisterResult.UNDERAGE;
        }

        // CICLO 6 GREEN — id duplicado
        if (registeredIds.contains(p.getId())) {
            return RegisterResult.DUPLICATED;
        }

        // Registro exitoso
        registeredIds.add(p.getId());
        return RegisterResult.VALID;
    }
}
