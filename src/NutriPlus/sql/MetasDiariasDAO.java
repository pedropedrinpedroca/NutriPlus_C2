package NutriPlus.sql;

import NutriPlus.connection.*;
import NutriPlus.model.*;

import java.sql.*;
import java.time.LocalDate;

public class MetasDiariasDAO {

    public long count() throws SQLException {
        try (var c = MySQLConnection.get();
                var st = c.createStatement();
                var rs = st.executeQuery("SELECT COUNT(*) FROM metas_diarias")) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public MetaDiaria findByUsuarioAndData(long usuario_id, LocalDate dia_mes) throws SQLException {
        String sql = "SELECT * FROM metas_diarias WHERE usuario_id=? AND dia_mes=?";
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement(sql)) {
            ps.setLong(1, usuario_id);
            ps.setDate(2, Date.valueOf(dia_mes));
            try (var rs = ps.executeQuery()) {
                if (rs.next())
                    return map(rs);
            }
        }
        return null;
    }

    public long upsert(MetaDiaria dt) throws SQLException {
        String sql = """
                INSERT INTO metas_diarias (usuario_id, dia_mes, tdee_kcal, meta_kcal, meta_prot_g, meta_carb_g, meta_gord_g, meta_agua_ml)
                VALUES (?,?,?,?,?,?,?,?)
                ON DUPLICATE KEY UPDATE
                   tdee_kcal=VALUES(tdee_kcal),
                   meta_kcal=VALUES(meta_kcal),
                   meta_prot_g=VALUES(meta_prot_g),
                   meta_carb_g=VALUES(meta_carb_g),
                   meta_gord_g=VALUES(meta_gord_g),
                   meta_agua_ml=VALUES(meta_agua_ml)
                """;
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, dt.usuario_id);
            ps.setDate(2, Date.valueOf(dt.dia_mes));
            ps.setDouble(3, dt.tdee_kcal);
            ps.setDouble(4, dt.meta_kcal);
            ps.setDouble(5, dt.meta_prot_g);
            ps.setDouble(6, dt.meta_carb_g);
            ps.setDouble(7, dt.meta_gord_g);
            ps.setInt(8, dt.meta_agua_ml);
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    return rs.getLong(1);
            }
        }
        return -1;
    }

    private MetaDiaria map(ResultSet rs) throws SQLException {
        MetaDiaria d = new MetaDiaria();
        d.id = rs.getLong("id");
        d.usuario_id = rs.getLong("usuario_id");
        d.dia_mes = rs.getDate("dia_mes").toLocalDate();
        d.tdee_kcal = rs.getDouble("tdee_kcal");
        d.meta_kcal = rs.getDouble("meta_kcal");
        d.meta_prot_g = rs.getDouble("meta_prot_g");
        d.meta_carb_g = rs.getDouble("meta_carb_g");
        d.meta_gord_g = rs.getDouble("meta_gord_g");
        d.meta_agua_ml = rs.getInt("meta_agua_ml");
        return d;
    }
}
