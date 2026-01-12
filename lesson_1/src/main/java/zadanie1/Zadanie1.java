package zadanie1;

import java.util.Random;
import java.util.Scanner;

import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;

public class Zadanie1 {
    public static Scanner scanner = new Scanner(System.in);
    public static Random random = new Random();
    public static Integer number;
    public static Integer attempts = 0;
    public static Integer record = 0;
    public static String nick;
    public static String log = "";

    public static void cls() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void saveScore(String player, Integer score) {
        File file = new File(System.getProperty("user.dir").toString() + "/" + player);

        try (FileWriter writer = new FileWriter(player)) {
            if (file.exists()) {
                if (score < record) {
                    file.delete();
                    writer.write(score.toString());
                }
            } else {
                writer.write(score.toString());
            }
        } catch (IOException e) {
        }
    }

    public static void deleteScore(String player) {
        File file = new File(System.getProperty("user.dir").toString() + "/" + player);

        if (file.exists())
            file.delete();
    }

    public static void main(String[] args) {
        System.out.print("Podaj Nick: ");
        nick = scanner.nextLine();
        cls();

        File file = new File(System.getProperty("user.dir").toString() + "/" + nick);

        if (file.exists()) {
            try (Scanner fileScanner = new Scanner(file)) {
                record = Integer.parseInt(fileScanner.nextLine());
            } catch (FileNotFoundException e) {
            }

            System.out.println("Witaj " + nick);
            System.out.println("Twój rekord to: " + record);

            System.out.println("\nCo chcesz zrobić?");
            System.out.println("1. Popraw wynik");
            System.out.println("2. Zacznij od nowa");
            Integer action = scanner.nextInt();

            switch (action) {
                case 2:
                    file.delete();
            }
        }

        while (true) {
            cls();
            System.out.println("Wybierz typ gry:");
            System.out.println("1. Ja zgaduje");
            System.out.println("2. System zgaduje");
            System.out.println("3. 1 vs 1");
            System.out.println("4. Wyjdź");

            switch (scanner.nextInt()) {
                case 1:
                    IGuess();
                    break;
                case 2:
                    SystemGuess();
                    break;
                case 3:
                    oneVersusOne();
                    break;
                default:
                    return;
            }
        }
    }

    public static void IGuess() {
        number = random.nextInt(0, 101);
        attempts = 0;

        cls();

        while (true) {
            System.out.print("\nZganij liczbę: ");
            Integer guessed = scanner.nextInt();

            attempts++;
            if (guessed < number) {
                cls();
                System.out.println("Większa");
                continue;
            }
            if (guessed > number) {
                cls();
                System.out.println("Mniejsza");
                continue;
            }
            if (guessed == number) {
                cls();
                System.out.println("Zgadłeś!");
                System.out.println("Twój wynik to: " + attempts);
                scanner.nextLine();
                scanner.nextLine();
                break;
            }
        }

    }

    public static void SystemGuess() {
        Integer min = 0;
        Integer max = 101;

        while (true) {
            Integer guessed = random.nextInt(min, max);
            cls();
            System.out.println("Czy twoja liczba to: " + guessed + "?");

            System.out.println("\n1. Mniejsza");
            System.out.println("2. Większa");
            System.out.println("3. Zgadłeś");

            attempts++;
            switch (scanner.nextInt()) {
                case 1:
                    max = guessed;
                    break;
                case 2:
                    min = guessed;
                    break;
                case 3:
                    cls();
                    saveScore("System", attempts);
                    System.out.print("System zgadł, jego wynik to: " + attempts);
                    scanner.nextLine();
                    scanner.nextLine();
                    return;
            }
        }
    }

    public static void oneVersusOne() {
        Integer systemAttempts = 0;
        Integer min = 0;
        Integer max = 101;
        Integer turn = 0;
        Integer coinToss = random.nextInt(0, 2);

        cls();
        switch (coinToss) {
            case 0:
                cls();
                log += "Wypadł orzeł, zaczyna " + nick;
                System.out.println(log);
                break;
            case 1:
                cls();
                log += "Wypadła reszka, zaczyna system";
                System.out.println(log);
                break;
        }

        number = random.nextInt(0, 101);
        attempts = 0;

        while (true) {
            if ((turn + coinToss) % 2 == 0) {
                log += "\n\nZganij liczbę: ";
                cls();
                System.out.print(log);
                Integer guessed = scanner.nextInt();
                log += guessed + "\n";

                attempts++;
                if (guessed < number) {
                    cls();
                    log += "Większa\n";
                    System.out.println(log);
                }
                if (guessed > number) {
                    cls();
                    log += "Mniejsza\n";
                    System.out.println(log);
                }
                if (guessed == number) {
                    cls();
                    System.out.println("Wygrałeś!");
                    System.out.print("Twój wynik to: " + attempts);
                    scanner.nextLine();
                    scanner.nextLine();
                    return;
                }
            } else {

                Integer systemGuessed = random.nextInt(min, max);
                cls();
                log += "\n\nCzy twoja liczba to: " + systemGuessed + "?";
                System.out.println(log);

                System.out.println("1. Mniejsza");
                System.out.println("2. Większa");
                System.out.println("3. Zgadłeś");

                systemAttempts++;
                switch (scanner.nextInt()) {
                    case 1:
                        log += "\nMniejsza";
                        max = systemGuessed;
                        break;
                    case 2:
                        log += "\nWiększa";
                        min = systemGuessed;
                        break;
                    case 3:
                        cls();
                        System.out.print("System wygrał, jego wynik to: " + systemAttempts);
                        scanner.nextLine();
                        scanner.nextLine();
                        return;
                }
            }

            turn++;
        }
    }
}
