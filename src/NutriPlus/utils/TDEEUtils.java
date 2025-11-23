package NutriPlus.utils;

import NutriPlus.model.*;

import java.time.LocalDate;

public class TDEEUtils {

    public static double bmrMifflin(Usuario u) {
        double base = 10 * u.peso_kg + 6.25 * u.altura_cm - 5 * u.idade;
        return (u.sexo == 'M') ? base + 5 : base - 161;
    }

    public static double tdee(Usuario u) {
        return bmrMifflin(u) * (u.atividade != null ? u.atividade.multiplier : NivelAtividade.SED.multiplier);
    }

    public static double metaCaloria(Usuario u, double tdee) {
        if (u.objetivo == Objetivo.BULK)
            return tdee * 1.10;
        if (u.objetivo == Objetivo.CUT)
            return tdee * 0.85;
        return tdee;
    }

    public static double[] percMacro(Objetivo g) {
        return switch (g) {
            case MANUT -> new double[] { 0.30, 0.40, 0.30 };
            case BULK -> new double[] { 0.25, 0.50, 0.25 };
            case CUT -> new double[] { 0.35, 0.40, 0.25 };
        };
    }

    public static int metaAguaMl(Usuario u) {
        return (int) Math.round(35.0 * u.peso_kg);
    }

    public static MetaDiaria constroeMetaDiaria(Usuario u, LocalDate dia) {
        double tdee = tdee(u);
        double meta_kcal = metaCaloria(u, tdee);
        double[] p = percMacro(u.objetivo);
        double protG = (meta_kcal * p[0]) / 4.0;
        double carbG = (meta_kcal * p[1]) / 4.0;
        double gordG = (meta_kcal * p[2]) / 9.0;

        MetaDiaria dt = new MetaDiaria();
        dt.usuario_id = u.id;
        dt.dia_mes = dia;
        dt.tdee_kcal = round1(tdee);
        dt.meta_kcal = round1(meta_kcal);
        dt.meta_prot_g = round1(protG);
        dt.meta_carb_g = round1(carbG);
        dt.meta_gord_g = round1(gordG);
        dt.meta_agua_ml = metaAguaMl(u);
        return dt;
    }

    public static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
