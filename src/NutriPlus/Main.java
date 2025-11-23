package NutriPlus;

import NutriPlus.controller.*;

public class Main {
    public static void main(String[] args) {
        try {
            var app = new AppController();
            app.mostrarSplash();
            app.run();
        } catch (Exception e) {
            System.err.println("Erro fatal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
