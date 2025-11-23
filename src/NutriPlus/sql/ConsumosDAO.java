package NutriPlus.sql;

import NutriPlus.connection.*;
import NutriPlus.model.*;

import java.sql.*;
import java.util.*;

public class ConsumosDAO {

    public long count() throws SQLException {
        try (var c = MySQLConnection.get();
                var st = c.createStatement();
                var rs = st.executeQuery("SELECT COUNT(*) FROM consumos")) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public List<Consumo> listByRefeicao(long refeicao_id) throws SQLException {
        var lista = new ArrayList<Consumo>();
        String sql = "SELECT * FROM consumos WHERE refeicao_id=? ORDER BY id";
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement(sql)) {
            ps.setLong(1, refeicao_id);
            try (var rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(map(rs));
            }
        }
        return lista;
    }

    public long insert(Consumo co) throws SQLException {
        String sql = "INSERT INTO consumos (refeicao_id, alimento_id, quantidade_g, logado_em) VALUES (?,?,?,?)";
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, co.refeicao_id);
            ps.setLong(2, co.alimento_id);
            ps.setDouble(3, co.quantidade_g);
            ps.setTimestamp(4, Timestamp.valueOf(co.logado_em));
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public void update(Consumo co) throws SQLException {
        String sql = "UPDATE consumos SET refeicao_id=?, alimento_id=?, quantidade_g=?, logado_em=? WHERE id=?";
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement(sql)) {
            ps.setLong(1, co.refeicao_id);
            ps.setLong(2, co.alimento_id);
            ps.setDouble(3, co.quantidade_g);
            ps.setTimestamp(4, Timestamp.valueOf(co.logado_em));
            ps.setLong(5, co.id);
            ps.executeUpdate();
        }
    }

    public boolean delete(long id) throws SQLException {
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement("DELETE FROM consumos WHERE id=?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Consumo map(ResultSet rs) throws SQLException {
        Consumo co = new Consumo();
        co.id = rs.getLong("id");
        co.refeicao_id = rs.getLong("refeicao_id");
        co.alimento_id = rs.getLong("alimento_id");
        co.quantidade_g = rs.getDouble("quantidade_g");
        Timestamp ts = rs.getTimestamp("logado_em");
        co.logado_em = ts.toLocalDateTime();
        return co;
    }
}
