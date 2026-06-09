package edu.unisabana.tyvs.logistica.delivery.rest;

import edu.unisabana.tyvs.logistica.application.usecase.ServicioTarifa;
import edu.unisabana.tyvs.logistica.domain.model.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cotizacion")
public class CotizacionController {

    private final ServicioTarifa servicio;

    public CotizacionController(ServicioTarifa servicio) { this.servicio = servicio; }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CotizacionResponseDTO> cotizar(@RequestBody CotizacionRequestDTO dto) {
        TipoEnvio tipo;
        try { tipo = TipoEnvio.valueOf(dto.getTipoEnvio().toUpperCase()); }
        catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new CotizacionResponseDTO("DATOS_INVALIDOS", 0.0));
        }
        SolicitudEnvio sol = new SolicitudEnvio(
            dto.getCiudadOrigen(), dto.getCiudadDestino(), dto.getPesoKg(), tipo
        );
        Cotizacion c = servicio.cotizar(sol);
        CotizacionResponseDTO resp = new CotizacionResponseDTO(c.getEstado().name(), c.getValorTotal());
        return c.isExitosa() ? ResponseEntity.ok(resp) : ResponseEntity.badRequest().body(resp);
    }
}
