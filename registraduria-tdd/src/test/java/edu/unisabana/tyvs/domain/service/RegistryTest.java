package edu.unisabana.tyvs.domain.service;

import edu.unisabana.tyvs.domain.model.Gender;
import edu.unisabana.tyvs.domain.model.Person;
import edu.unisabana.tyvs.domain.model.RegisterResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Pruebas unitarias del dominio de la Registraduría Electoral.
 *
 * <p>Aplica TDD (Red → Green → Refactor), patrón AAA y escenarios BDD,
 * cubriendo todas las clases de equivalencia y valores límite definidos
 * en la guía del taller para {@link Registry#registerVoter(Person)}.</p>
 *
 * <h3>Clases de equivalencia</h3>
 * <pre>
 * Persona  : nula, id ≤ 0, muerta, edad inválida, menor, duplicada, válida
 * Edad     : negativa, [0–17] menor, [18–120] válida, > 120 inválida
 * Id       : ≤ 0 inválido, > 0 único, > 0 duplicado
 * </pre>
 *
 * Integrantes: Fredy Orlando Pulido Quintero
 */
public class RegistryTest {

    private Registry registry;

    @Before
    public void setUp() {
        // Arrange compartido: instancia limpia antes de cada prueba
        registry = new Registry();
    }

    // =========================================================================
    // CICLO TDD 1 — Camino feliz: persona válida
    // (Tomado exactamente del README, sección "1. RED: primera prueba")
    //
    // BDD: Given persona viva de 30 años con id único;
    //      When intento registrarla; Then VALID
    // =========================================================================

    @Test
    public void shouldRegisterValidPerson() {
        // Arrange
        Person person = new Person("Ana", 1, 30, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        Assert.assertEquals(RegisterResult.VALID, result);
    }

    // =========================================================================
    // CICLO TDD 2 — Persona muerta → DEAD
    // (Tomado del README, sección "1. RED: persona muerta → DEAD")
    // Defecto 02 del defectos.md: alive=false devolvía VALID
    //
    // BDD: Given persona con alive=false;
    //      When intento registrarla; Then DEAD
    // =========================================================================

    @Test
    public void shouldRejectDeadPerson() {
        // Arrange
        Person dead = new Person("Carlos", 2, 40, Gender.MALE, false);

        // Act
        RegisterResult result = registry.registerVoter(dead);

        // Assert
        Assert.assertEquals(RegisterResult.DEAD, result);
    }

    // =========================================================================
    // CICLO TDD 3 — Persona nula → INVALID
    // (Del README, tabla BDD: "shouldReturnInvalidWhenPersonIsNull")
    //
    // BDD: Given la persona es null;
    //      When intento registrarla; Then INVALID
    // =========================================================================

    @Test
    public void shouldReturnInvalidWhenPersonIsNull() {
        // Arrange
        Person person = null;

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        Assert.assertEquals(RegisterResult.INVALID, result);
    }

    // =========================================================================
    // CICLO TDD 4 — Id cero o negativo → INVALID
    // (Del README, tabla BDD: "shouldRejectWhenIdIsZeroOrNegative")
    // =========================================================================

    /**
     * Valor límite: id = 0 (borde inferior inválido)
     *
     * BDD: Given persona con id=0;
     *      When intento registrarla; Then INVALID
     */
    @Test
    public void shouldRejectWhenIdIsZero() {
        // Arrange
        Person person = new Person("Luis", 0, 25, Gender.MALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        Assert.assertEquals(RegisterResult.INVALID, result);
    }

    /**
     * Clase inválida: id negativo
     *
     * BDD: Given persona con id=-5;
     *      When intento registrarla; Then INVALID
     */
    @Test
    public void shouldRejectWhenIdIsNegative() {
        // Arrange
        Person person = new Person("Laura", -5, 25, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        Assert.assertEquals(RegisterResult.INVALID, result);
    }

    // =========================================================================
    // CICLO TDD 5 — Edad inválida → INVALID_AGE
    // Defecto 01 del defectos.md: age=-1 devolvía VALID
    // (Del README, clase de equivalencia: "edad < 0 → INVALID_AGE")
    // =========================================================================

    /**
     * Clase inválida: edad negativa (Defecto 01 de defectos.md)
     *
     * BDD: Given persona con age=-1 (edad inválida);
     *      When intento registrarla; Then INVALID_AGE
     */
    @Test
    public void shouldRejectNegativeAge() {
        // Arrange — Defecto 01: Person(name="Juan", id=101, age=-1, gender=MALE, alive=true)
        Person person = new Person("Juan", 101, -1, Gender.MALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        Assert.assertEquals(RegisterResult.INVALID_AGE, result);
    }

    /**
     * Valor límite superior inválido: edad = 121
     * (Del README, tabla BDD: "shouldRejectInvalidAgeOver120")
     *
     * BDD: Given persona con age=121 (supera el máximo);
     *      When intento registrarla; Then INVALID_AGE
     */
    @Test
    public void shouldRejectInvalidAgeOver120() {
        // Arrange
        Person person = new Person("Mateo", 3, 121, Gender.MALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        Assert.assertEquals(RegisterResult.INVALID_AGE, result);
    }

    // =========================================================================
    // CICLO TDD 6 — Menor de edad → UNDERAGE
    // (Del README, tabla BDD: "shouldRejectUnderageAt17")
    // =========================================================================

    /**
     * Valor límite: edad = 17 (un año antes de la mayoría de edad)
     *
     * BDD: Given persona con age=17 (menor de edad);
     *      When intento registrarla; Then UNDERAGE
     */
    @Test
    public void shouldRejectUnderageAt17() {
        // Arrange
        Person person = new Person("Sofia", 4, 17, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        Assert.assertEquals(RegisterResult.UNDERAGE, result);
    }

    /**
     * Valor límite inferior válido: edad = 18 (mayoría de edad exacta)
     * (Del README, tabla BDD: "shouldAcceptAdultAt18")
     *
     * BDD: Given persona con age=18 (recién mayor de edad);
     *      When intento registrarla; Then VALID
     */
    @Test
    public void shouldAcceptAdultAt18() {
        // Arrange
        Person person = new Person("Diego", 5, 18, Gender.MALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        Assert.assertEquals(RegisterResult.VALID, result);
    }

    /**
     * Valor límite superior válido: edad = 120
     * (Del README, tabla BDD: "shouldAcceptMaxAge120")
     *
     * BDD: Given persona con age=120 (máximo permitido);
     *      When intento registrarla; Then VALID
     */
    @Test
    public void shouldAcceptMaxAge120() {
        // Arrange
        Person person = new Person("Rosa", 6, 120, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        Assert.assertEquals(RegisterResult.VALID, result);
    }

    // =========================================================================
    // CICLO TDD 7 — Registro duplicado → DUPLICATED
    // Defecto 03 del defectos.md: segunda persona con mismo id devolvía VALID
    // (Del README, clase de equivalencia: "mismo id ya registrado → DUPLICATED")
    //
    // BDD: Given primera persona con id=200 ya registrada;
    //      When segunda persona con id=200 intenta registrarse;
    //      Then DUPLICATED
    // =========================================================================

    @Test
    public void shouldRejectDuplicateId() {
        // Arrange — Defecto 03: Carlos y Carla con mismo id=200
        Person persona1 = new Person("Carlos", 200, 30, Gender.MALE,   true);
        Person persona2 = new Person("Carla",  200, 25, Gender.FEMALE, true);

        // Act
        RegisterResult resultadoPrimera  = registry.registerVoter(persona1);
        RegisterResult resultadoSegunda  = registry.registerVoter(persona2);

        // Assert
        Assert.assertEquals(RegisterResult.VALID,      resultadoPrimera);
        Assert.assertEquals(RegisterResult.DUPLICATED, resultadoSegunda);
    }

    // =========================================================================
    // CICLO TDD 8 — Segunda persona con id diferente → VALID (ambas)
    // Verifica que el registro de duplicados no afecta a ids distintos
    //
    // BDD: Given dos personas con ids diferentes;
    //      When registro ambas; Then ambas VALID
    // =========================================================================

    @Test
    public void shouldRegisterTwoDifferentPersonsWithDifferentIds() {
        // Arrange
        Person persona1 = new Person("Pedro", 300, 35, Gender.MALE,   true);
        Person persona2 = new Person("Paula", 301, 28, Gender.FEMALE, true);

        // Act
        RegisterResult resultado1 = registry.registerVoter(persona1);
        RegisterResult resultado2 = registry.registerVoter(persona2);

        // Assert
        Assert.assertEquals(RegisterResult.VALID, resultado1);
        Assert.assertEquals(RegisterResult.VALID, resultado2);
    }
}
