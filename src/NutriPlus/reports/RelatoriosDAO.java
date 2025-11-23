package NutriPlus.reports;

import NutriPlus.connection.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RelatoriosDAO {

    public void relatorioCaloriasPorDia(long usuario_id, LocalDate from, LocalDate to) throws SQLException {
        String sql = """
                SELECT usuario_id, dia AS dia_mes, ROUND(SUM(kcal),1) AS kcal,
                       ROUND(SUM(prot_g),1) AS prot_g,
                       ROUND(SUM(carb_g),1) AS carb_g,
                       ROUND(SUM(gord_g),1) AS gord_g
                FROM v_kcal_por_consumo
                WHERE usuario_id=? AND dia BETWEEN ? AND ?
                GROUP BY usuario_id, dia
                ORDER BY dia;
                """;
        try (var c = MySQLConnection.get(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, usuario_id);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            try (var rs = ps.executeQuery()) {
                System.out.println("\n----- Calorias por dia -----");
                while (rs.next()) {
                    System.out.printf("%s  | kcal: %.1f  P: %.1f g  C: %.1f g  G: %.1f g%n",
                            rs.getDate("dia_mes").toLocalDate(),
                            rs.getDouble("kcal"),
                            rs.getDouble("prot_g"),
                            rs.getDouble("carb_g"),
                            rs.getDouble("gord_g"));
                }
            }
        }
    }

    public void relatorioAguaPorDia(long usuario_id, LocalDate from, LocalDate to) throws SQLException {
        String sql = """
                SELECT usuario_id, DATE(logado_em) AS dia, SUM(ml) AS total_ml
                FROM registros_agua
                WHERE usuario_id=? AND DATE(logado_em) BETWEEN ? AND ?
                GROUP BY usuario_id, DATE(logado_em)
                ORDER BY dia;
                """;
        try (var c = MySQLConnection.get(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, usuario_id);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            try (var rs = ps.executeQuery()) {
                System.out.println("\n----- Água por dia -----");
                while (rs.next()) {
                    System.out.printf("%s  | água: %d ml%n", rs.getDate("dia").toLocalDate(), rs.getInt("total_ml"));
                }
            }
        }
    }

    public void relatorioItensRefeicao(long refeicao_id) throws SQLException {
        String sql = """
                SELECT c.id AS cons_id, f.nome AS alimento, c.quantidade_g,
                       ROUND(c.quantidade_g * f.kcal_100g / 100.0,1) AS kcal,
                       ROUND(c.quantidade_g * f.prot_100g / 100.0,1) AS prot_g,
                       ROUND(c.quantidade_g * f.carb_100g / 100.0,1) AS carb_g,
                       ROUND(c.quantidade_g * f.gord_100g / 100.0,1) AS gord_g
                FROM consumos c
                JOIN alimentos f ON f.id=c.alimento_id
                WHERE c.refeicao_id=?
                ORDER BY c.id;
                """;
        try (var c = MySQLConnection.get(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, refeicao_id);
            try (var rs = ps.executeQuery()) {
                System.out.println("\n----- Alimentos por refeição -----");
                while (rs.next()) {
                    System.out.printf("#%d  %-30s  %6.1fg  | kcal: %6.1f  P: %5.1fg  C: %5.1fg  G: %5.1fg%n",
                            rs.getLong("cons_id"),
                            rs.getString("alimento"),
                            rs.getDouble("quantidade_g"),
                            rs.getDouble("kcal"),
                            rs.getDouble("prot_g"),
                            rs.getDouble("carb_g"),
                            rs.getDouble("gord_g"));
                }
            }
        }
    }

    public List<String> relatorioMacrosPorDia(long usuario_id) throws SQLException {
        String sql = """
                    SELECT DATE(c.logado_em) AS dia,
                        ROUND(SUM(c.quantidade_g * a.kcal_100g / 100.0), 2) AS kcal,
                        ROUND(SUM(c.quantidade_g * a.prot_100g / 100.0), 2) AS prot_g,
                        ROUND(SUM(c.quantidade_g * a.carb_100g / 100.0), 2) AS carb_g,
                        ROUND(SUM(c.quantidade_g * a.gord_100g / 100.0), 2) AS gord_g
                    FROM consumos c
                    JOIN alimentos a ON a.id = c.alimento_id
                    JOIN refeicoes r ON r.id = c.refeicao_id
                    WHERE r.usuario_id = ?
                    GROUP BY DATE(c.logado_em)
                    ORDER BY dia DESC
                """;
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement(sql)) {
            ps.setLong(1, usuario_id);
            try (var rs = ps.executeQuery()) {
                var out = new ArrayList<String>();
                while (rs.next()) {
                    out.add(String.format("%s | kcal=%s prot=%s carb=%s gord=%s",
                            rs.getDate("dia"),
                            rs.getBigDecimal("kcal"),
                            rs.getBigDecimal("prot_g"),
                            rs.getBigDecimal("carb_g"),
                            rs.getBigDecimal("gord_g")));
                }
                return out;
            }
        }
    }

    public List<String> relatorioItensPorRefeicao(long refeicaoId) throws SQLException {
        String sql = """
                    SELECT c.id,
                        a.nome AS alimento,
                        c.quantidade_g,
                        ROUND(c.quantidade_g * a.kcal_100g / 100.0, 2) AS kcal
                    FROM consumos c
                    JOIN alimentos a ON a.id = c.alimento_id
                    WHERE c.refeicao_id = ?
                    ORDER BY c.id
                """;
        try (var c = MySQLConnection.get();
                var ps = c.prepareStatement(sql)) {
            ps.setLong(1, refeicaoId);
            try (var rs = ps.executeQuery()) {
                var out = new ArrayList<String>();
                while (rs.next()) {
                    out.add(String.format("#%d | %-30s | %sg | %skcal",
                            rs.getLong("id"),
                            rs.getString("alimento"),
                            rs.getBigDecimal("quantidade_g"),
                            rs.getBigDecimal("kcal")));
                }
                return out;
            }
        }
    }
}
