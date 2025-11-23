package NutriPlus.model;

public class Alimento {
    public Long id;
    public String nome;
    public double kcal_100g;
    public double prot_100g;
    public double carb_100g;
    public double gord_100g;

    public String toString() {
        return String.format("[%d] %s (kcal/100g=%.1f, P=%.1f C=%.1f G=%.1f)",
                id, nome, kcal_100g, prot_100g, carb_100g, gord_100g);
    }
}
