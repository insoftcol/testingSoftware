package edu.unisabana.tyvs.logistica.application.usecase;

import edu.unisabana.tyvs.logistica.application.port.out.CotizacionRepositoryPort;
import edu.unisabana.tyvs.logistica.domain.model.*;

import java.util.*;

/**
 * Caso de uso: calcula la tarifa de envío y persiste la cotización.
 */
public class ServicioTarifa {

    public static final double MONTO_MINIMO  =   0.0;
    public static final double PESO_MAXIMO   = 500.0;
    public static final int    LIMITE_KM_CORTA = 200;
    public static final int    LIMITE_KM_MEDIA = 500;
    public static final double TARIFA_BASE_CORTA  = 15_000.0;
    public static final double TARIFA_BASE_MEDIA  = 30_000.0;
    public static final double TARIFA_BASE_LARGA  = 50_000.0;
    public static final double COSTO_KG_CORTA = 500.0;
    public static final double COSTO_KG_MEDIA = 800.0;
    public static final double COSTO_KG_LARGA = 1_200.0;

    private static final Set<String> CIUDADES_SOPORTADAS = new HashSet<>(Arrays.asList(
        "BOGOTA","MEDELLIN","CALI","BARRANQUILLA","CARTAGENA",
        "BUCARAMANGA","MANIZALES","PEREIRA"
    ));

    private static final Map<String, Integer> DISTANCIAS = new HashMap<>();
    static {
        DISTANCIAS.put("BARRANQUILLA-BOGOTA",1018); DISTANCIAS.put("BARRANQUILLA-BUCARAMANGA",584);
        DISTANCIAS.put("BARRANQUILLA-CALI",1200);   DISTANCIAS.put("BARRANQUILLA-CARTAGENA",125);
        DISTANCIAS.put("BARRANQUILLA-MANIZALES",788);DISTANCIAS.put("BARRANQUILLA-MEDELLIN",702);
        DISTANCIAS.put("BARRANQUILLA-PEREIRA",822);  DISTANCIAS.put("BOGOTA-BUCARAMANGA",394);
        DISTANCIAS.put("BOGOTA-CALI",462);           DISTANCIAS.put("BOGOTA-CARTAGENA",1046);
        DISTANCIAS.put("BOGOTA-MANIZALES",288);      DISTANCIAS.put("BOGOTA-MEDELLIN",415);
        DISTANCIAS.put("BOGOTA-PEREIRA",322);        DISTANCIAS.put("BUCARAMANGA-CALI",854);
        DISTANCIAS.put("BUCARAMANGA-CARTAGENA",612); DISTANCIAS.put("BUCARAMANGA-MANIZALES",377);
        DISTANCIAS.put("BUCARAMANGA-MEDELLIN",457);  DISTANCIAS.put("BUCARAMANGA-PEREIRA",401);
        DISTANCIAS.put("CALI-CARTAGENA",1210);       DISTANCIAS.put("CALI-MANIZALES",175);
        DISTANCIAS.put("CALI-MEDELLIN",416);         DISTANCIAS.put("CALI-PEREIRA",190);
        DISTANCIAS.put("CARTAGENA-MANIZALES",759);   DISTANCIAS.put("CARTAGENA-MEDELLIN",673);
        DISTANCIAS.put("CARTAGENA-PEREIRA",803);     DISTANCIAS.put("MANIZALES-MEDELLIN",128);
        DISTANCIAS.put("MANIZALES-PEREIRA",43);      DISTANCIAS.put("MEDELLIN-PEREIRA",148);
    }

    private final CotizacionRepositoryPort repo;

    public ServicioTarifa(CotizacionRepositoryPort repo) { this.repo = repo; }
    public ServicioTarifa() { this.repo = null; }

    public Cotizacion cotizar(SolicitudEnvio solicitud) {
        Cotizacion cotizacion = calcular(solicitud);

        if (repo != null) {
            try { repo.save(solicitud, cotizacion); }
            catch (Exception e) {
                throw new IllegalStateException("Error al persistir cotización: " + e.getMessage(), e);
            }
        }
        return cotizacion;
    }

    private Cotizacion calcular(SolicitudEnvio s) {
        if (s == null) return Cotizacion.invalida(CotizacionEstado.DATOS_INVALIDOS);

        if (s.getCiudadOrigen() == null || s.getCiudadOrigen().trim().isEmpty())
            return Cotizacion.invalida(CotizacionEstado.CIUDAD_ORIGEN_INVALIDA);

        String origen = s.getCiudadOrigen().trim().toUpperCase();
        if (!CIUDADES_SOPORTADAS.contains(origen))
            return Cotizacion.invalida(CotizacionEstado.CIUDAD_ORIGEN_INVALIDA);

        if (s.getCiudadDestino() == null || s.getCiudadDestino().trim().isEmpty())
            return Cotizacion.invalida(CotizacionEstado.CIUDAD_DESTINO_INVALIDA);

        String destino = s.getCiudadDestino().trim().toUpperCase();
        if (!CIUDADES_SOPORTADAS.contains(destino))
            return Cotizacion.invalida(CotizacionEstado.CIUDAD_DESTINO_INVALIDA);

        if (origen.equals(destino))
            return Cotizacion.invalida(CotizacionEstado.MISMA_CIUDAD);

        if (s.getPesoKg() <= MONTO_MINIMO || s.getPesoKg() > PESO_MAXIMO)
            return Cotizacion.invalida(CotizacionEstado.PESO_INVALIDO);

        String clave = claveRuta(origen, destino);
        if (!DISTANCIAS.containsKey(clave))
            return Cotizacion.invalida(CotizacionEstado.RUTA_NO_DISPONIBLE);

        int    dist   = DISTANCIAS.get(clave);
        double base   = calcularBase(dist, s.getPesoKg());
        double mult   = calcularMult(s.getTipoEnvio());
        return Cotizacion.exitosa(base * mult);
    }

    private double calcularBase(int km, double kg) {
        if (km < LIMITE_KM_CORTA)  return TARIFA_BASE_CORTA + COSTO_KG_CORTA * kg;
        if (km <= LIMITE_KM_MEDIA) return TARIFA_BASE_MEDIA + COSTO_KG_MEDIA * kg;
        return TARIFA_BASE_LARGA + COSTO_KG_LARGA * kg;
    }

    private double calcularMult(TipoEnvio tipo) {
        if (tipo == TipoEnvio.EXPRESO)     return 1.5;
        if (tipo == TipoEnvio.REFRIGERADO) return 2.0;
        return 1.0;
    }

    private String claveRuta(String a, String b) {
        return a.compareTo(b) <= 0 ? a + "-" + b : b + "-" + a;
    }
}
