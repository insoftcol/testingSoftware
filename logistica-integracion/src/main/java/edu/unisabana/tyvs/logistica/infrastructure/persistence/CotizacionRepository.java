package edu.unisabana.tyvs.logistica.infrastructure.persistence;

import edu.unisabana.tyvs.logistica.application.port.out.CotizacionRepositoryPort;
import edu.unisabana.tyvs.logistica.domain.model.Cotizacion;
import edu.unisabana.tyvs.logistica.domain.model.SolicitudEnvio;

import java.sql.*;
import java.util.*;

public class CotizacionRepository implements CotizacionRepositoryPort {

    private final String jdbcUrl;

    public CotizacionRepository(String jdbcUrl) { this.jdbcUrl = jdbcUrl; }

    private Connection conn() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, "", "");
    }

    @Override
    public void initSchema() throws Exception {
        String ddl = "CREATE TABLE IF NOT EXISTS cotizacion_log (" +
                     "  id            BIGINT AUTO_INCREMENT PRIMARY KEY," +
                     "  ciudad_origen VARCHAR(50)  NOT NULL," +
                     "  ciudad_destino VARCHAR(50) NOT NULL," +
                     "  peso_kg       DOUBLE       NOT NULL," +
                     "  tipo_envio    VARCHAR(20)  NOT NULL," +
                     "  estado        VARCHAR(30)  NOT NULL," +
                     "  valor_total   DOUBLE       NOT NULL" +
                     ")";
        try (Connection c = conn(); Statement s = c.createStatement()) { s.execute(ddl); }
    }

    @Override
    public void save(SolicitudEnvio sol, Cotizacion cot) throws Exception {
        String sql = "INSERT INTO cotizacion_log" +
                     "(ciudad_origen,ciudad_destino,peso_kg,tipo_envio,estado,valor_total)" +
                     " VALUES(?,?,?,?,?,?)";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sol != null ? sol.getCiudadOrigen()  : "N/A");
            ps.setString(2, sol != null ? sol.getCiudadDestino() : "N/A");
            ps.setDouble(3, sol != null ? sol.getPesoKg()        : 0.0);
            ps.setString(4, sol != null && sol.getTipoEnvio() != null
                            ? sol.getTipoEnvio().name() : "N/A");
            ps.setString(5, cot.getEstado().name());
            ps.setDouble(6, cot.getValorTotal());
            ps.executeUpdate();
        }
    }

    @Override
    public List<CotizacionRecord> findAll() throws Exception {
        List<CotizacionRecord> list = new ArrayList<>();
        try (Connection c = conn(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM cotizacion_log")) {
            while (rs.next()) {
                list.add(new CotizacionRecord(
                    rs.getLong("id"),       rs.getString("ciudad_origen"),
                    rs.getString("ciudad_destino"), rs.getDouble("peso_kg"),
                    rs.getString("tipo_envio"),      rs.getString("estado"),
                    rs.getDouble("valor_total")));
            }
        }
        return list;
    }

    @Override
    public void deleteAll() throws Exception {
        try (Connection c = conn(); Statement s = c.createStatement()) {
            s.executeUpdate("DELETE FROM cotizacion_log");
        }
    }

    @Override
    public long countExitosas() throws Exception {
        try (Connection c = conn(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(
                 "SELECT COUNT(*) FROM cotizacion_log WHERE estado='EXITOSA'")) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }
}
