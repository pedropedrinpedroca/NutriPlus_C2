package NutriPlus.model;

import java.time.LocalDate;

public class MetaDiaria {
    public Long id;
    public Long usuario_id;
    public LocalDate dia_mes;
    public double tdee_kcal;
    public double meta_kcal;
    public double meta_prot_g;
    public double meta_carb_g;
    public double meta_gord_g;
    public int meta_agua_ml;
}
