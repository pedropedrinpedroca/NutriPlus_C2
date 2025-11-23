package NutriPlus.sql;

import NutriPlus.connection.*;
import NutriPlus.model.*;

import java.sql.*;
import java.util.*;

public class UsuariosDAO {

    public long count() throws SQLException {
        try (var c = MySQLConnection.get();
                var st = c.createStatement();
                var rs = st.executeQuery("SELECT COUNT(*) FROM usuarios")) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public List<Usuario> findAll() throws SQLException {
        var lista = new ArrayList<Usuario>();
        try (var c = MySQLConnection.get();
                var st = c.createStatement();
                var rs = st.executeQuery("SELECT * FROM usuarios ORDER BY id")) {
            while (rs.next())
                lista.add(map(rs));
        }
        return lista;
    }

    public Usuario findById(long id) throws SQLException {
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement("SELECT * FROM usuarios WHERE id=?")) {
            ps.setLong(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next())
                    return map(rs);
            }
        }
        return null;
    }

    public long insert(Usuario u) throws SQLException {
        String sql = """
                    INSERT INTO usuarios (nome, sexo, idade, altura_cm, peso_kg, atividade, objetivo, perc_gordura)
                    VALUES (?,?,?,?,?,?,?,?)
                """;
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.nome);
            ps.setString(2, String.valueOf(u.sexo));
            ps.setInt(3, u.idade);
            ps.setInt(4, u.altura_cm);
            ps.setDouble(5, u.peso_kg);
            ps.setString(6, u.atividade.name());
            ps.setString(7, u.objetivo.name());
            if (u.perc_gordura == null)
                ps.setNull(8, Types.DECIMAL);
            else
                ps.setDouble(8, u.perc_gordura);
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public void update(Usuario u) throws SQLException {
        String sql = """
                    UPDATE usuarios SET nome=?, sexo=?, idade=?, altura_cm=?, peso_kg=?, atividade=?, objetivo=?, perc_gordura=?
                    WHERE id=?
                """;
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement(sql)) {
            ps.setString(1, u.nome);
            ps.setString(2, String.valueOf(u.sexo));
            ps.setInt(3, u.idade);
            ps.setInt(4, u.altura_cm);
            ps.setDouble(5, u.peso_kg);
            ps.setString(6, u.atividade.name());
            ps.setString(7, u.objetivo.name());
            if (u.perc_gordura == null)
                ps.setNull(8, Types.DECIMAL);
            else
                ps.setDouble(8, u.perc_gordura);
            ps.setLong(9, u.id);
            ps.executeUpdate();
        }
    }

    public boolean delete(long id) throws SQLException {
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement("DELETE FROM usuarios WHERE id=?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Usuario map(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.id = rs.getLong("id");
        u.nome = rs.getString("nome");
        u.sexo = rs.getString("sexo").charAt(0);
        u.idade = rs.getInt("idade");
        u.altura_cm = rs.getInt("altura_cm");
        u.peso_kg = rs.getDouble("peso_kg");
        u.atividade = NivelAtividade.valueOf(rs.getString("atividade"));
        u.objetivo = Objetivo.valueOf(rs.getString("objetivo"));
        double pg = rs.getDouble("perc_gordura");
        u.perc_gordura = rs.wasNull() ? null : pg;
        return u;
    }
}
