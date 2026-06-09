package edu.unisabana.tyvs.pjba.infrastructure.persistence;

import edu.unisabana.tyvs.pjba.application.port.out.TransferenciaRepositoryPort;
import edu.unisabana.tyvs.pjba.domain.model.SolicitudTransferencia;
import edu.unisabana.tyvs.pjba.domain.model.TransferenciaResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador de infraestructura: persiste transferencias en H2 via JDBC puro.
 * No usa Spring Data JPA para mantener el ejemplo didáctico y sin magia implícita.
 */
public class TransferenciaRepository implements TransferenciaRepositoryPort {

    private final String jdbcUrl;

    public TransferenciaRepository(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, "", "");
    }

    @Override
    public void initSchema() throws Exception {
        String ddl = "CREATE TABLE IF NOT EXISTS transferencia_log (" +
                     "  id            BIGINT AUTO_INCREMENT PRIMARY KEY," +
                     "  cuenta_origen VARCHAR(50) NOT NULL," +
                     "  cuenta_destino VARCHAR(50) NOT NULL," +
                     "  monto         DOUBLE NOT NULL," +
                     "  entidad       VARCHAR(50) NOT NULL," +
                     "  resultado     VARCHAR(30) NOT NULL" +
                     ")";
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            st.execute(ddl);
        }
    }

    @Override
    public void save(SolicitudTransferencia solicitud, TransferenciaResult resultado) throws Exception {
        String sql = "INSERT INTO transferencia_log" +
                     "(cuenta_origen, cuenta_destino, monto, entidad, resultado)" +
                     " VALUES(?, ?, ?, ?, ?)";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            String origen  = solicitud != null && solicitud.getCuentaOrigen() != null
                             ? solicitud.getCuentaOrigen().getNumeroCuenta() : "N/A";
            String destino = solicitud != null ? solicitud.getNumeroCuentaDestino() : "N/A";
            double monto   = solicitud != null ? solicitud.getMonto() : 0;
            String entidad = solicitud != null && solicitud.getCuentaOrigen() != null
                             ? solicitud.getCuentaOrigen().getEntidadBancaria() : "N/A";

            ps.setString(1, origen);
            ps.setString(2, destino != null ? destino : "N/A");
            ps.setDouble(3, monto);
            ps.setString(4, entidad);
            ps.setString(5, resultado.name());
            ps.executeUpdate();
        }
    }

    @Override
    public List<TransferenciaRecord> findAll() throws Exception {
        List<TransferenciaRecord> list = new ArrayList<>();
        String sql = "SELECT id, cuenta_origen, cuenta_destino, monto, entidad, resultado" +
                     " FROM transferencia_log";
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new TransferenciaRecord(
                    rs.getLong("id"),
                    rs.getString("cuenta_origen"),
                    rs.getString("cuenta_destino"),
                    rs.getDouble("monto"),
                    rs.getString("entidad"),
                    rs.getString("resultado")
                ));
            }
        }
        return list;
    }

    @Override
    public void deleteAll() throws Exception {
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("DELETE FROM transferencia_log");
        }
    }

    @Override
    public long countExitosas() throws Exception {
        String sql = "SELECT COUNT(*) FROM transferencia_log WHERE resultado = 'EXITOSA'";
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }
}
