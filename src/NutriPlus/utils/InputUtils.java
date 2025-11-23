package NutriPlus.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Scanner;

public class InputUtils {
    private final Scanner sc = new Scanner(System.in);

    public int askInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt + " ");
            String s = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                if (v < min || v > max) {
                    System.out.println("Valor fora do intervalo.");
                    continue;
                }
                return v;
            } catch (Exception e) {
                System.out.println("Número inválido.");
            }
        }
    }

    public double askDouble(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt + " ");
            String s = sc.nextLine().trim().replace(",", ".");
            try {
                double v = Double.parseDouble(s);
                if (v < min || v > max) {
                    System.out.println("Valor fora do intervalo.");
                    continue;
                }
                return v;
            } catch (Exception e) {
                System.out.println("Número inválido.");
            }
        }
    }

    public String askString(String prompt, int minLen, int maxLen) {
        while (true) {
            System.out.print(prompt + " ");
            String s = sc.nextLine().trim();
            if (s.length() < minLen || s.length() > maxLen) {
                System.out.println("Tamanho inválido.");
                continue;
            }
            return s;
        }
    }

    public char askSexo() {
        while (true) {
            String s = askString("Sexo (M/F):", 1, 1).toUpperCase(Locale.ROOT);
            if (s.equals("M") || s.equals("F"))
                return s.charAt(0);
            System.out.println("Digite 'M' ou 'F'.");
        }
    }

    public boolean askYesNo(String prompt) {
        while (true) {
            System.out.print(prompt + " [s/n] ");
            String s = sc.nextLine().trim().toLowerCase(Locale.ROOT);
            if (s.equals("s") || s.equals("sim"))
                return true;
            if (s.equals("") || s.equals("n") || s.equals("nao") || s.equals("não"))
                return false;
            System.out.println("Digite 's' ou 'n'.");
        }
    }

    public LocalDate askData(String prompt) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (true) {
            System.out.print(prompt + " (yyyy-MM-dd): ");
            try {
                return LocalDate.parse(sc.nextLine().trim(), f);
            } catch (Exception e) {
                System.out.println("Data inválida.");
            }
        }
    }

    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    public LocalDate today() {
        return LocalDate.now();
    }
}
