package edu.unisabana.tyvs.pjba.delivery.rest;

import edu.unisabana.tyvs.pjba.application.usecase.ServicioTransferencia;
import edu.unisabana.tyvs.pjba.domain.model.Cuenta;
import edu.unisabana.tyvs.pjba.domain.model.SolicitudTransferencia;
import edu.unisabana.tyvs.pjba.domain.model.TipoCuenta;
import edu.unisabana.tyvs.pjba.domain.model.TransferenciaResult;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones de transferencia bancaria.
 *
 * <p>Endpoint: {@code POST /transferencia}</p>
 * <ul>
 *   <li>HTTP 200 → {@code EXITOSA}</li>
 *   <li>HTTP 400 → cualquier resultado de validación fallida</li>
 * </ul>
 */
@RestController
@RequestMapping("/transferencia")
public class TransferenciaController {

    private final ServicioTransferencia servicio;

    public TransferenciaController(ServicioTransferencia servicio) {
        this.servicio = servicio;
    }

    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> transferir(@RequestBody TransferenciaDTO dto) {
        TipoCuenta tipo;
        try {
            tipo = TipoCuenta.valueOf(dto.getTipoCuenta().toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(TransferenciaResult.DATOS_INVALIDOS.name());
        }

        Cuenta origen = new Cuenta(
            dto.getNumeroCuentaOrigen(),
            tipo,
            dto.getEntidadBancaria(),
            dto.getSaldoOrigen()
        );

        SolicitudTransferencia solicitud = new SolicitudTransferencia(
            origen, dto.getNumeroCuentaDestino(), dto.getMonto()
        );

        TransferenciaResult result = servicio.procesarTransferencia(solicitud);

        if (result == TransferenciaResult.EXITOSA) {
            return ResponseEntity.ok(result.name());
        }
        return ResponseEntity.badRequest().body(result.name());
    }
}
