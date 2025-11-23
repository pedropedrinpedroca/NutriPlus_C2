package NutriPlus.model;

public class Usuario {
    public Long id;
    public String nome;
    public char sexo; // 'M' ou 'F'
    public int idade;
    public int altura_cm;
    public double peso_kg;
    public NivelAtividade atividade;
    public Objetivo objetivo;
    public Double perc_gordura; // Opcional

    public String toString() {
        return String.format("[%d] %s (%s) %d a / %d cm / %.1f kg - %s - %s",
                id, nome, sexo == 'M' ? "M" : "F", idade, altura_cm, peso_kg, atividade, objetivo);
    }
}
