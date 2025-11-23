package NutriPlus.sql;

import NutriPlus.connection.*;
import NutriPlus.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RefeicoesDAO {

    public long count() throws SQLException {
        try (var c = MySQLConnection.get();
                var st = c.createStatement();
                var rs = st.executeQuery("SELECT COUNT(*) FROM refeicoes")) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public Refeicao findById(long id) throws SQLException {
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement("SELECT * FROM refeicoes WHERE id=?")) {
            ps.setLong(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next())
                    return map(rs);
            }
        }
        return null;
    }

    public List<Refeicao> listByUsuarioAndData(long usuario_id, LocalDate dia_mes) throws SQLException {
        var lista = new ArrayList<Refeicao>();
        String sql = "SELECT * FROM refeicoes WHERE usuario_id=? AND dia_mes=? ORDER BY nome";
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement(sql)) {
            ps.setLong(1, usuario_id);
            ps.setDate(2, Date.valueOf(dia_mes));
            try (var rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(map(rs));
            }
        }
        return lista;
    }

    public List<Refeicao> listByUsuario(long usuario_id) throws SQLException {
        var lista = new ArrayList<Refeicao>();
        String sql = "SELECT * FROM refeicoes WHERE usuario_id=? ORDER BY dia_mes DESC, nome";
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement(sql)) {
            ps.setLong(1, usuario_id);
            try (var rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(map(rs));
            }
        }
        return lista;
    }

    public long insert(Refeicao m) throws SQLException {
        String sql = "INSERT INTO refeicoes (usuario_id, dia_mes, nome) VALUES (?,?,?)";
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, m.usuario_id);
            ps.setDate(2, Date.valueOf(m.dia_mes));
            ps.setString(3, m.nome.name());
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public void update(Refeicao m) throws SQLException {
        String sql = "UPDATE refeicoes SET usuario_id=?, dia_mes=?, nome=? WHERE id=?";
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement(sql)) {
            ps.setLong(1, m.usuario_id);
            ps.setDate(2, Date.valueOf(m.dia_mes));
            ps.setString(3, m.nome.name());
            ps.setLong(4, m.id);
            ps.executeUpdate();
        }
    }

    public boolean delete(long id) throws SQLException {
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement("DELETE FROM refeicoes WHERE id=?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Refeicao map(ResultSet rs) throws SQLException {
        Refeicao m = new Refeicao();
        m.id = rs.getLong("id");
        m.usuario_id = rs.getLong("usuario_id");
        m.dia_mes = rs.getDate("dia_mes").toLocalDate();
        m.nome = NomeRefeicao.valueOf(rs.getString("nome"));
        return m;
    }
}
