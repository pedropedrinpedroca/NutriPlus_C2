package NutriPlus.sql;

import NutriPlus.connection.*;
import NutriPlus.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RegistrosAguaDAO {

    public long count() throws SQLException {
        try (var c = MySQLConnection.get();
                var st = c.createStatement();
                var rs = st.executeQuery("SELECT COUNT(*) FROM registros_agua")) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public List<RegistroAgua> listByUsuarioAndData(long usuario_id, LocalDate dia) throws SQLException {
        var lista = new ArrayList<RegistroAgua>();
        String sql = "SELECT * FROM registros_agua WHERE usuario_id=? AND DATE(logado_em)=? ORDER BY logado_em";
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement(sql)) {
            ps.setLong(1, usuario_id);
            ps.setDate(2, Date.valueOf(dia));
            try (var rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(map(rs));
            }
        }
        return lista;
    }

    public long insert(RegistroAgua w) throws SQLException {
        String sql = "INSERT INTO registros_agua (usuario_id, ml, logado_em) VALUES (?,?,?)";
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, w.usuario_id);
            ps.setInt(2, w.ml);
            ps.setTimestamp(3, Timestamp.valueOf(w.logado_em));
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public void update(RegistroAgua w) throws SQLException {
        String sql = "UPDATE registros_agua SET usuario_id=?, ml=?, logado_em=? WHERE id=?";
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement(sql)) {
            ps.setLong(1, w.usuario_id);
            ps.setInt(2, w.ml);
            ps.setTimestamp(3, Timestamp.valueOf(w.logado_em));
            ps.setLong(4, w.id);
            ps.executeUpdate();
        }
    }

    public boolean delete(long id) throws SQLException {
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement("DELETE FROM registros_agua WHERE id=?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private RegistroAgua map(ResultSet rs) throws SQLException {
        RegistroAgua w = new RegistroAgua();
        w.id = rs.getLong("id");
        w.usuario_id = rs.getLong("usuario_id");
        w.ml = rs.getInt("ml");
        w.logado_em = rs.getTimestamp("logado_em").toLocalDateTime();
        return w;
    }
}
