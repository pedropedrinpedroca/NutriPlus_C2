package NutriPlus.sql;

import NutriPlus.connection.*;
import NutriPlus.model.*;

import java.sql.*;
import java.util.*;

public class AlimentosDAO {

    public long count() throws SQLException {
        try (var c = MySQLConnection.get();
                var st = c.createStatement();
                var rs = st.executeQuery("SELECT COUNT(*) FROM alimentos")) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public List<Alimento> findAll() throws SQLException {
        var list = new ArrayList<Alimento>();
        try (var c = MySQLConnection.get();
                var st = c.createStatement();
                var rs = st.executeQuery("SELECT * FROM alimentos ORDER BY id")) {
            while (rs.next())
                list.add(map(rs));
        }
        return list;
    }

    public Alimento findById(long id) throws SQLException {
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement("SELECT * FROM alimentos WHERE id=?")) {
            ps.setLong(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next())
                    return map(rs);
            }
        }
        return null;
    }

    public long insert(Alimento f) throws SQLException {
        String sql = """
                    INSERT INTO alimentos (nome, kcal_100g, prot_100g, carb_100g, gord_100g)
                    VALUES (?,?,?,?,?)
                """;
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, f.nome);
            ps.setDouble(2, f.kcal_100g);
            ps.setDouble(3, f.prot_100g);
            ps.setDouble(4, f.carb_100g);
            ps.setDouble(5, f.gord_100g);
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public void update(Alimento f) throws SQLException {
        String sql = """
                    UPDATE alimentos SET nome=?, kcal_100g=?, prot_100g=?, carb_100g=?, gord_100g=?
                    WHERE id=?
                """;
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement(sql)) {
            ps.setString(1, f.nome);
            ps.setDouble(2, f.kcal_100g);
            ps.setDouble(3, f.prot_100g);
            ps.setDouble(4, f.carb_100g);
            ps.setDouble(5, f.gord_100g);
            ps.setLong(6, f.id);
            ps.executeUpdate();
        }
    }

    public boolean delete(long id) throws SQLException {
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement("DELETE FROM alimentos WHERE id=?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Alimento map(ResultSet rs) throws SQLException {
        Alimento f = new Alimento();
        f.id = rs.getLong("id");
        f.nome = rs.getString("nome");
        f.kcal_100g = rs.getDouble("kcal_100g");
        f.prot_100g = rs.getDouble("prot_100g");
        f.carb_100g = rs.getDouble("carb_100g");
        f.gord_100g = rs.getDouble("gord_100g");
        return f;
    }
}
