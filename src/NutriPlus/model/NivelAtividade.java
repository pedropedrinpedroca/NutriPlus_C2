package NutriPlus.model;

public enum NivelAtividade {
    SED(1.20),  // Sedentário
    LEV(1.375), // Levemente ativo (1–2x/semana)
    MOD(1.55),  // Regularmente ativo (3–5x/semana)
    ALT(1.725), // Altamente ativo (6–7x/semana)
    ATH(1.90);  // Atleta (2x/dia)

    public final double multiplier;

    NivelAtividade(double m) {
        this.multiplier = m;
    }
}
