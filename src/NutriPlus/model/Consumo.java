package NutriPlus.model;

import java.time.LocalDateTime;

public class Consumo {
    public Long id;
    public Long refeicao_id;
    public Long alimento_id;
    public double quantidade_g;
    public LocalDateTime logado_em;
}
