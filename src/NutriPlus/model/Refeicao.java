package NutriPlus.model;

import java.time.LocalDate;

public class Refeicao {
    public Long id;
    public Long usuario_id;
    public LocalDate dia_mes;
    public NomeRefeicao nome;

    public String toString() {
        return String.format("[%d] %s %s", id, dia_mes, nome);
    }
}
