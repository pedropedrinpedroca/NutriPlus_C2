package NutriPlus.controller;

import NutriPlus.model.*;
import NutriPlus.sql.*;
import NutriPlus.reports.*;
import NutriPlus.utils.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AppController {
    private final UsuariosDAO usuariosDAO = new UsuariosDAO();
    private final AlimentosDAO alimentosDAO = new AlimentosDAO();
    private final RefeicoesDAO refeicoesDAO = new RefeicoesDAO();
    private final ConsumosDAO consumosDAO = new ConsumosDAO();
    private final RegistrosAguaDAO registrosAguaDAO = new RegistrosAguaDAO();
    private final MetasDiariasDAO metasDiariasDAO = new MetasDiariasDAO();
    private final RelatoriosDAO relatoriosDAO = new RelatoriosDAO();
    private final InputUtils in = new InputUtils();

    private Long currentUserId = null;

    public void mostrarSplash() throws SQLException {
        long f = alimentosDAO.count();
        long c = consumosDAO.count();
        long w = registrosAguaDAO.count();
        long d = metasDiariasDAO.count();

        String banner = """
                ###################################################
                #                                                 #
                #                N U T R I P L U S                #
                #                                                 #
                ###################################################
                """;
        System.out.print(banner);
        System.out.printf(
                "# TOTAL DE REGISTROS EXISTENTES%n" +
                "# 1 - ALIMENTOS:      %d%n" +
                "# 2 - CONSUMOS:       %d%n" +
                "# 3 - ÁGUA (logs):    %d%n" +
                "# 4 - METAS DIÁRIAS:  %d%n%n",
                f, c, w, d);

        System.out.println("# CRIADO POR: Bruna Santos Soares Aguiar");
        System.out.println("#             Pedro Henrique da Silva Teixeira");
        System.out.println("#             Pedro Henrique Pontes Pereira");
        System.out.println("# DISCIPLINA: Banco de Dados  |  Período: 2025/2");
        System.out.println("# PROFESSOR:  Howard Cruz Roatti");
        System.out.println("###################################################\n");
    }

    public void run() throws SQLException {
        while (true) {
            if (currentUserId == null) {
                escolherCriarUsuario();
            }

            mostrarHome();
            System.out.println("\n===== MENU PRINCIPAL =====");
            System.out.println("1) Relatórios");
            System.out.println("2) Inserir registros");
            System.out.println("3) Remover registros");
            System.out.println("4) Atualizar registros");
            System.out.println("5) Sair");
            int op = in.askInt("Escolha:", 1, 5);
            switch (op) {
                case 1 -> menuRelatorios();
                case 2 -> menuInserir();
                case 3 -> menuRemover();
                case 4 -> menuAtualizar();
                case 5 -> {
                    System.out.println("Saindo...");
                    return;
                }
            }
        }
    }

    private void mostrarHome() throws SQLException {
        long uid = currentUserId;
        LocalDate today = in.today();
        ensureTargets(uid, today);

        var dt = metasDiariasDAO.findByUsuarioAndData(uid, today);
        double kcal = kcalTotal(uid, today);
        double p = macroTotal(uid, today, "prot_g");
        double c = macroTotal(uid, today, "carb_g");
        double g = macroTotal(uid, today, "gord_g");
        int agua = aguaTotal(uid, today);

        System.out.println("\n====================== HOJE ======================");
        System.out.printf("Meta Kcal: %.0f | Consumidos: %.0f | Faltam: %.0f%n", dt.meta_kcal, kcal, Math.max(0, dt.meta_kcal - kcal));
        System.out.printf("Proteínas: %.0f/%.0f g | Carbo: %.0f/%.0f g | Gord: %.0f/%.0f g%n", p, dt.meta_prot_g, c, dt.meta_carb_g, g, dt.meta_gord_g);
        System.out.printf("Água: %d/%d ml%n", agua, dt.meta_agua_ml);
        System.out.println("==================================================");
    }

    private double kcalTotal(long usuario_id, LocalDate dia) throws SQLException {
        String sql = "SELECT ROUND(SUM(kcal),1) FROM v_kcal_por_consumo WHERE usuario_id=? AND dia=?";
        try (var c = NutriPlus.connection.MySQLConnection.get();
                var ps = c.prepareStatement(sql)) {
            ps.setLong(1, usuario_id);
            ps.setDate(2, java.sql.Date.valueOf(dia));
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }
        }
    }

    private double macroTotal(long usuario_id, LocalDate dia, String col) throws SQLException {
        String sql = "SELECT ROUND(SUM(" + col + "),1) FROM v_kcal_por_consumo WHERE usuario_id=? AND dia=?";
        try (var c = NutriPlus.connection.MySQLConnection.get();
                var ps = c.prepareStatement(sql)) {
            ps.setLong(1, usuario_id);
            ps.setDate(2, java.sql.Date.valueOf(dia));
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }
        }
    }

    private int aguaTotal(long usuario_id, LocalDate dia) throws SQLException {
        String sql = "SELECT COALESCE(SUM(ml),0) FROM registros_agua WHERE usuario_id=? AND DATE(logado_em)=?";
        try (var c = NutriPlus.connection.MySQLConnection.get();
                var ps = c.prepareStatement(sql)) {
            ps.setLong(1, usuario_id);
            ps.setDate(2, java.sql.Date.valueOf(dia));
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void ensureTargets(long usuario_id, LocalDate day) throws SQLException {
        var u = usuariosDAO.findById(usuario_id);
        var existing = metasDiariasDAO.findByUsuarioAndData(usuario_id, day);
        if (existing == null) {
            var dt = TDEEUtils.constroeMetaDiaria(u, day);
            metasDiariasDAO.upsert(dt);
        }
    }

    private void escolherCriarUsuario() throws SQLException {
        while (true) {
            long total = usuariosDAO.count();

            if (total == 0) {
                System.out.println("Nenhum usuário cadastrado. Vamos criar o usuário.");
                var u = readUserFromConsole();
                long newId = usuariosDAO.insert(u);
                currentUserId = newId;
                ensureTargets(newId, in.today());
                System.out.println("Usuário criado com sucesso.");
                return;
            }

            List<Usuario> usuarios = usuariosDAO.findAll();
            if (total == 1) {
                Usuario unico = usuarios.get(0);
                currentUserId = unico.id;
                System.out.println("Usuário: " + unico);
                return;
            }
        }
    }

    private Usuario readUserFromConsole() {
        Usuario u = new Usuario();
        u.nome = in.askString("Nome:", 2, 120);
        u.sexo = in.askSexo();
        u.idade = in.askInt("Idade (anos):", 12, 100);
        u.altura_cm = in.askInt("Altura (cm):", 120, 230);
        u.peso_kg = in.askDouble("Peso (kg):", 30, 300);
        int a = in.askInt("Atividade: 1) Sedentário | 2) Levemente ativo | 3) Moderadamente ativo | 4) Altamente ativo | 5) Atleta", 1, 5);
        u.atividade = NivelAtividade.values()[a - 1];
        int g = in.askInt("Objetivo: 1) Manutenção | 2) Bulking | 3) Cutting", 1, 3);
        u.objetivo = switch (g) {
            case 2 -> Objetivo.BULK;
            case 3 -> Objetivo.CUT;
            default -> Objetivo.MANUT;
        };
        if (in.askYesNo("Informar % de gordura corporal?")) {
            u.perc_gordura = in.askDouble("% de gordura corporal (opcional):", 2, 65);
        }
        return u;
    }

    private long criarAlimentoInterativo() throws SQLException {
        Alimento f = new Alimento();
        f.nome = in.askString("Nome do alimento:", 2, 160);
        f.kcal_100g = in.askDouble("Kcal/100g:", 0, 1200);
        f.prot_100g = in.askDouble("Proteína/100g (g):", 0, 100);
        f.carb_100g = in.askDouble("Carbo/100g (g):", 0, 100);
        f.gord_100g = in.askDouble("Gordura/100g (g):", 0, 100);
        long id = alimentosDAO.insert(f);
        System.out.println("Alimento criado (ID " + id + ").");
        return id;
    }

    private NomeRefeicao pedirNomeRefeicao() {
        System.out.println("Tipo de refeição:");
        System.out.println("1) Café da Manhã");
        System.out.println("2) Almoço");
        System.out.println("3) Jantar");
        System.out.println("4) Lanche");
        System.out.println("5) Pré-treino");
        System.out.println("6) Pós-treino");
        System.out.println("7) Outro");
        int op = in.askInt("Escolha:", 1, 7);
        return NomeRefeicao.values()[op - 1];
    }

    private Refeicao obterOuCriarRefeicao(long usuarioId, LocalDate dia, NomeRefeicao nome) throws SQLException {
        var existentesNoDia = refeicoesDAO.listByUsuarioAndData(usuarioId, dia);
        for (Refeicao r : existentesNoDia) {
            if (r.nome == nome) {
                return r;
            }
        }

        Refeicao nova = new Refeicao();
        nova.usuario_id = usuarioId;
        nova.dia_mes = dia;
        nova.nome = nome;
        long id = refeicoesDAO.insert(nova);
        nova.id = id;
        System.out.println("Refeição criada (ID " + id + ").");
        return nova;
    }

    private void menuInserir() throws SQLException {
        boolean stay;
        do {
            System.out.println("\n----- Inserir -----");
            int op = in.askInt("0) Voltar | 1) Alimento | 2) Consumo | 3) Água", 0, 3);
            switch (op) {
                case 0 -> {
                    stay = false;
                    continue;
                }
                case 1 -> {
                    do {
                        criarAlimentoInterativo();
                    } while (in.askYesNo("Inserir outro alimento?"));
                    stay = in.askYesNo("Deseja inserir outro tipo?");
                }
                case 2 -> {
                    do {
                        LocalDate dia = in.askData("Data da refeição");
                        NomeRefeicao nomeRef = pedirNomeRefeicao();
                        Refeicao ref = obterOuCriarRefeicao(currentUserId, dia, nomeRef);

                        System.out.println("Refeição selecionada: " + ref);

                        long alimentoId;
                        int opAli = in.askInt("Alimento: 1) Escolher Existente | 2) Cadastrar Novo", 1, 2);
                        if (opAli == 1) {
                            alimentoId = escolherAlimento();
                        } else {
                            alimentoId = criarAlimentoInterativo();
                        }

                        Consumo co = new Consumo();
                        co.refeicao_id = ref.id;
                        co.alimento_id = alimentoId;
                        co.quantidade_g = in.askDouble("Quantidade (g):", 1, 2000);
                        co.logado_em = java.time.LocalDateTime.now();
                        long id = consumosDAO.insert(co);
                        System.out.println("Consumo registrado (ID " + id + ").");
                        ensureTargets(currentUserId, ref.dia_mes);
                    } while (in.askYesNo("Registrar outro consumo?"));
                    stay = in.askYesNo("Deseja inserir outro tipo?");
                }
                case 3 -> {
                    do {
                        RegistroAgua w = new RegistroAgua();
                        w.usuario_id = currentUserId;
                        w.ml = in.askInt("Volume (ml):", 50, 5000);
                        w.logado_em = java.time.LocalDateTime.now();
                        long id = registrosAguaDAO.insert(w);
                        System.out.println("Água registrada (ID " + id + ").");
                        ensureTargets(currentUserId, in.today());
                    } while (in.askYesNo("Inserir outro registro de água?"));
                    stay = in.askYesNo("Deseja inserir outro tipo?");
                }
                default -> stay = true;
            }
        } while (stay);
    }

    private void menuRemover() throws SQLException {
        boolean stay;
        do {
            System.out.println("\n----- Remover -----");
            int op = in.askInt("0) Voltar | 1) Usuário | 2) Alimento | 3) Consumo | 4) Água", 0, 4);
            switch (op) {
                case 0 -> {
                    stay = false;
                    continue;
                }
                case 1 -> removerUsuario();
                case 2 -> removerAlimento();
                case 3 -> {
                    long id = escolherConsumo();
                    if (in.askYesNo("Tem certeza que deseja remover o consumo (ID " + id + ")?")) {
                        if (consumosDAO.delete(id))
                            System.out.println("Removido.");
                    } else
                        System.out.println("Cancelado.");
                }
                case 4 -> {
                    long id = escolherAgua();
                    if (in.askYesNo("Tem certeza que deseja remover o registro de água (ID " + id + ")?")) {
                        if (registrosAguaDAO.delete(id))
                            System.out.println("Removido.");
                    } else
                        System.out.println("Cancelado.");
                }
            }
            stay = in.askYesNo("Deseja remover mais?");
        } while (stay);
    }

    private void menuAtualizar() throws SQLException {
        boolean stay;
        do {
            System.out.println("\n----- Atualizar -----");
            int op = in.askInt("0) Voltar | 1) Usuário | 2) Alimento | 3) Consumo | 4) Água", 0, 4);
            switch (op) {
                case 0 -> {
                    stay = false;
                    continue;
                }
                case 1 -> {
                    long id = escolherUsuario();
                    var u = usuariosDAO.findById(id);
                    if (u == null) {
                        System.out.println("Não encontrado.");
                        break;
                    }
                    System.out.println("Atualizando: " + u);
                    u.nome = in.askString("Nome:", 2, 120);
                    u.sexo = in.askSexo();
                    u.idade = in.askInt("Idade:", 12, 100);
                    u.altura_cm = in.askInt("Altura (cm):", 120, 230);
                    u.peso_kg = in.askDouble("Peso (kg):", 30, 300);
                    int a = in.askInt("Atividade: 1) Sedentário | 2) Levemente ativo | 3) Moderadamente ativo | 4) Altamente ativo | 5) Atleta", 1, 5);
                    u.atividade = NivelAtividade.values()[a - 1];
                    int g = in.askInt("Objetivo: 1) Manutenção | 2) Bulking | 3) Cutting", 1, 3);
                    u.objetivo = switch (g) {
                        case 1 -> Objetivo.MANUT;
                        case 2 -> Objetivo.BULK;
                        default -> Objetivo.CUT;
                    };
                    if (in.askYesNo("Informar %% de gordura corporal?")) {
                        u.perc_gordura = in.askDouble("% de gordura corporal:", 2, 65);
                    } else
                        u.perc_gordura = null;

                    usuariosDAO.update(u);
                    ensureTargets(u.id, in.today());
                    var u2 = usuariosDAO.findById(u.id);
                    System.out.println("Atualizado: " + u2);
                }
                case 2 -> {
                    long id = escolherAlimento();
                    var f = alimentosDAO.findById(id);
                    if (f == null) {
                        System.out.println("Não encontrado.");
                        break;
                    }
                    System.out.println("Atualizando: " + f);
                    f.nome = in.askString("Nome do alimento:", 2, 160);
                    f.kcal_100g = in.askDouble("Kcal/100g:", 0, 1200);
                    f.prot_100g = in.askDouble("Proteína/100g (g):", 0, 100);
                    f.carb_100g = in.askDouble("Carbo/100g (g):", 0, 100);
                    f.gord_100g = in.askDouble("Gordura/100g (g):", 0, 100);
                    alimentosDAO.update(f);
                    var f2 = alimentosDAO.findById(f.id);
                    System.out.println("Atualizado: " + f2);
                }
                case 3 -> {
                    long id = escolherConsumo();
                    var co = consumosDAO.listByRefeicao(escolherRefeicao(currentUserId))
                            .stream().filter(c -> c.id == id).findFirst().orElse(null);
                    if (co == null) {
                        co = new Consumo();
                        co.id = id;
                    }
                    System.out.println("Atualizando consumo (ID " + id + ").");
                    co.refeicao_id = escolherRefeicao(currentUserId);
                    co.alimento_id = escolherAlimento();
                    co.quantidade_g = in.askDouble("Quantidade (g):", 1, 2000);
                    co.logado_em = java.time.LocalDateTime.now();
                    consumosDAO.update(co);
                    System.out.println("Consumo atualizado (ID " + co.id + ").");
                }
                case 4 -> {
                    long id = escolherAgua();
                    RegistroAgua w = new RegistroAgua();
                    w.id = id;
                    w.usuario_id = currentUserId;
                    w.ml = in.askInt("Novo volume (ml):", 50, 5000);
                    w.logado_em = java.time.LocalDateTime.now();
                    registrosAguaDAO.update(w);
                    System.out.println("Água atualizada (ID " + w.id + ").");
                }
            }
            stay = in.askYesNo("Deseja atualizar mais?");
        } while (stay);
    }

    private void menuRelatorios() throws SQLException {
        boolean stay;
        do {
            System.out.println("\n----- Relatórios -----");
            int op = in.askInt("0) Voltar | 1) Calorias por dia | 2) Água por dia | 3) Itens de uma refeição", 0, 3);
            switch (op) {
                case 0 -> stay = false;
                case 1 -> {
                    var from = in.askData("De");
                    var to = in.askData("Até");
                    relatoriosDAO.relatorioCaloriasPorDia(currentUserId, from, to);
                    stay = in.askYesNo("Deseja gerar outro relatório?");
                }
                case 2 -> {
                    var from = in.askData("De");
                    var to = in.askData("Até");
                    relatoriosDAO.relatorioAguaPorDia(currentUserId, from, to);
                    stay = in.askYesNo("Deseja gerar outro relatório?");
                }
                case 3 -> {
                    long refeicao_id = escolherRefeicao(currentUserId);
                    relatoriosDAO.relatorioItensRefeicao(refeicao_id);
                    stay = in.askYesNo("Deseja gerar outro relatório?");
                }
                default -> stay = true;
            }
        } while (stay);
    }

    private long escolherUsuario() throws SQLException {
        var lista = usuariosDAO.findAll();
        lista.forEach(u -> System.out.println(u.toString()));
        return in.askInt("ID:", 1, Integer.MAX_VALUE);
    }

    private long escolherAlimento() throws SQLException {
        var lista = alimentosDAO.findAll();
        lista.forEach(f -> System.out.println(f.toString()));
        return in.askInt("ID do alimento:", 1, Integer.MAX_VALUE);
    }

    private long escolherRefeicao(long usuario_id) throws SQLException {
        var lista = refeicoesDAO.listByUsuario(usuario_id);
        for (Refeicao m : lista)
            System.out.printf("[%d] %s %s%n", m.id, m.dia_mes, m.nome);
        return in.askInt("ID da refeição:", 1, Integer.MAX_VALUE);
    }

    private long escolherConsumo() throws SQLException {
        long refeicao_id = escolherRefeicao(currentUserId);
        var lista = consumosDAO.listByRefeicao(refeicao_id);
        for (var c : lista)
            System.out.printf("[%d] food=%d  qty=%.1fg%n", c.id, c.alimento_id, c.quantidade_g);
        return in.askInt("ID do consumo:", 1, Integer.MAX_VALUE);
    }

    private long escolherAgua() throws SQLException {
        var today = in.today();
        var lista = registrosAguaDAO.listByUsuarioAndData(currentUserId, today);
        for (var w : lista)
            System.out.printf("[%d] %s  %d ml%n", w.id, w.logado_em, w.ml);
        return in.askInt("ID do registro de água:", 1, Integer.MAX_VALUE);
    }

    private void removerUsuario() throws SQLException {
        if (currentUserId == null) {
            System.out.println("Nenhum usuário cadastrado para remover.");
            return;
        }

        long id = currentUserId;
        var u = usuariosDAO.findById(id);
        if (u == null) {
            System.out.println("Usuário atual não encontrado. Nada a remover.");
            currentUserId = null;
            return;
        }

        System.out.println("Usuário atual: " + u);
        if (!in.askYesNo(
                "Tem certeza que deseja remover o usuário e TODOS os seus registros (consumos, água, metas)?")) {
            System.out.println("Cancelado.");
            return;
        }

        removerUsuarioCascata(id);
        System.out.println("Usuário e todos os dados removidos.");
        currentUserId = null;
    }

    private void removerAlimento() throws SQLException {
        long id = escolherAlimento();
        if (!in.askYesNo("Tem certeza que deseja remover o alimento (ID " + id + ")?")) {
            System.out.println("Cancelado.");
            return;
        }
        try {
            if (alimentosDAO.delete(id)) {
                System.out.println("Alimento removido.");
                return;
            }
        } catch (SQLException e) {
            System.out.println("Não foi possível remover: existem consumos dependentes.");
            if (in.askYesNo("Remover em CASCATA todos os consumos deste alimento?")) {
                var c = NutriPlus.connection.MySQLConnection.get();
                c.setAutoCommit(false);
                try (var ps = c.prepareStatement("DELETE FROM consumos WHERE alimento_id=?")) {
                    ps.setLong(1, id);
                    ps.executeUpdate();
                    alimentosDAO.delete(id);
                    c.commit();
                    System.out.println("Removido em cascata.");
                } catch (SQLException ex) {
                    c.rollback();
                    throw ex;
                } finally {
                    c.setAutoCommit(true);
                }
            } else
                System.out.println("Cancelado.");
        }
    }

    private void removerUsuarioCascata(long userId) throws SQLException {
        var c = NutriPlus.connection.MySQLConnection.get();
        c.setAutoCommit(false);
        try {
            try (var ps1 = c.prepareStatement(
                    "DELETE FROM consumos WHERE refeicao_id IN (SELECT id FROM refeicoes WHERE usuario_id=?)");
                    var ps2 = c.prepareStatement("DELETE FROM refeicoes WHERE usuario_id=?");
                    var ps3 = c.prepareStatement("DELETE FROM registros_agua WHERE usuario_id=?");
                    var ps4 = c.prepareStatement("DELETE FROM metas_diarias WHERE usuario_id=?");
                    var ps5 = c.prepareStatement("DELETE FROM usuarios WHERE id=?")) {
                ps1.setLong(1, userId);
                ps1.executeUpdate();
                ps2.setLong(1, userId);
                ps2.executeUpdate();
                ps3.setLong(1, userId);
                ps3.executeUpdate();
                ps4.setLong(1, userId);
                ps4.executeUpdate();
                ps5.setLong(1, userId);
                ps5.executeUpdate();
            }
            c.commit();
        } catch (SQLException e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(true);
        }
    }
}
