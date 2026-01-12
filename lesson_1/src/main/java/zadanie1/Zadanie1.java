package zadanie1;

import java.util.*;
import java.io.*;

public class Zadanie1 {
    public static Scanner scanner = new Scanner(System.in);
    public static Random random = new Random();

    // Gracze
    public static List<Player> players = new ArrayList<>();
    public static ComputerPlayer computer;

    // Katalog zapisu
    public static final String SAVE_DIR = System.getProperty("user.dir") + "/saves";

    public static void cls() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // Bezpieczne wczytywanie liczby (obsługa błędów)
    public static int safeNextInt() {
        while (true) {
            try {
                return scanner.nextInt();
            } catch (java.util.InputMismatchException e) {
                scanner.nextLine(); // wyczyść bufor
                System.out.print("Nieprawidłowa wartość. Podaj liczbę: ");
            }
        }
    }

    // ========== POZIOMY TRUDNOŚCI ==========

    public static class Difficulty {
        public static final String EASY = "EASY";
        public static final String NORMAL = "NORMAL";
        public static final String HARD = "HARD";
        public static final String CUSTOM = "CUSTOM";

        public int min;
        public int max;
        public String name;
        public String key;

        public Difficulty(int min, int max, String name, String key) {
            this.min = min;
            this.max = max;
            this.name = name;
            this.key = key;
        }

        public static Difficulty easy() {
            return new Difficulty(0, 101, "Łatwy (0-100)", EASY);
        }

        public static Difficulty normal() {
            return new Difficulty(0, 10001, "Normalny (0-10000)", NORMAL);
        }

        public static Difficulty hard() {
            return new Difficulty(0, 1000001, "Trudny (0-1000000)", HARD);
        }

        public static Difficulty custom(int min, int max) {
            return new Difficulty(min, max + 1, "Custom (" + min + "-" + max + ")", CUSTOM + "_" + min + "_" + max);
        }
    }

    // ========== MENU I KONFIGURACJA ==========

    public static void main(String[] args) {
        cls();
        System.out.println("=== GRA W ZGADYWANIE LICZB ===\n");

        // Konfiguracja komputera
        System.out.print("Podaj nazwę dla komputera (Enter dla 'Komputer'): ");
        String computerName = scanner.nextLine().trim();
        if (computerName.isEmpty()) {
            computerName = "Komputer";
        }
        computer = new ComputerPlayer(computerName);

        // Wczytaj zapisane dane komputera
        Player savedComputer = Player.loadFromFile(SAVE_DIR, computerName);
        if (savedComputer != null && savedComputer instanceof ComputerPlayer) {
            computer = (ComputerPlayer) savedComputer;
            System.out.println("Wczytano dane komputera: " + computer.getName());
        }

        // Konfiguracja liczby graczy
        System.out.print("\nIlość graczy (1-4): ");
        int numPlayers = Math.max(1, Math.min(4, safeNextInt()));
        scanner.nextLine();

        for (int i = 0; i < numPlayers; i++) {
            System.out.print("Podaj nick gracza " + (i + 1) + ": ");
            String playerName = scanner.nextLine().trim();
            if (playerName.isEmpty()) {
                playerName = "Gracz" + (i + 1);
            }

            // Sprawdź czy gracz ma zapisane dane
            Player existingPlayer = Player.loadFromFile(SAVE_DIR, playerName);
            if (existingPlayer != null) {
                players.add(existingPlayer);
                System.out.println("Wczytano dane gracza: " + playerName);
            } else {
                players.add(new Player(playerName));
            }
        }

        // Główna pętla gry
        Difficulty currentDifficulty = null;
        int lastGameMode = 0;

        while (true) {
            cls();
            System.out.println("=== MENU GŁÓWNE ===");
            System.out.println("Gracze: " + getPlayerNames());
            System.out.println("Komputer: " + computer.getName());
            if (currentDifficulty != null) {
                System.out.println("Aktualny poziom: " + currentDifficulty.name);
            }

            System.out.println("\nWybierz tryb gry:");
            System.out.println("1. Single - Gracze zgadują");
            System.out.println("2. Single - Komputer zgaduje");
            System.out.println("3. Versus - Gracze vs Komputer");
            System.out.println("4. Multiplayer - Wszyscy gracze");
            System.out.println("5. Turniej (BO1/BO3/BO5)");
            System.out.println("6. Statystyki");
            System.out.println("7. Zmień poziom trudności");
            System.out.println("8. Zapisz i wyjdź");

            int choice = safeNextInt();

            // Tryby gry (1-5)
            if (choice >= 1 && choice <= 5) {
                if (currentDifficulty == null) {
                    currentDifficulty = selectDifficulty();
                }
                lastGameMode = choice;

                // Pętla "zagraj ponownie" w tym samym trybie
                do {
                    switch (lastGameMode) {
                        case 1:
                            currentDifficulty = playersGuess(currentDifficulty);
                            saveAllPlayers(); // Auto-zapis
                            break;
                        case 2:
                            currentDifficulty = computerGuesses(currentDifficulty);
                            saveAllPlayers(); // Auto-zapis
                            break;
                        case 3:
                            currentDifficulty = versusMode(currentDifficulty);
                            updateLeader();
                            saveAllPlayers(); // Auto-zapis
                            break;
                        case 4:
                            currentDifficulty = multiplayerMode(currentDifficulty);
                            updateLeader();
                            saveAllPlayers(); // Auto-zapis
                            break;
                        case 5:
                            tournamentMode(currentDifficulty);
                            updateLeader();
                            saveAllPlayers(); // Auto-zapis
                            break;
                    }
                } while (askPlayAgain());
            } else {
                switch (choice) {
                    case 6:
                        displayAllStats();
                        break;
                    case 7:
                        currentDifficulty = selectDifficulty();
                        break;
                    case 8:
                        saveAllPlayers();
                        System.out.println("Zapisano! Do zobaczenia!");
                        return;
                }
            }
        }
    }

    // Aktualizacja lidera (gracz z najwięcej wygranych multi)
    public static void updateLeader() {
        Player bestPlayer = null;
        int maxWins = 0;

        // Znajdź gracza z najwięcej wygranych
        for (Player p : players) {
            if (p.getTotalMultiWins() > maxWins) {
                maxWins = p.getTotalMultiWins();
                bestPlayer = p;
            }
        }

        // Ustaw lidera
        for (Player p : players) {
            p.setLeader(p == bestPlayer && maxWins > 0);
        }

        if (bestPlayer != null && maxWins > 0) {
            System.out.println("\n⭐ LIDER: " + bestPlayer.getName() + " (" + maxWins + " wygranych)");
        }
    }

    public static String getPlayerNames() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < players.size(); i++) {
            sb.append(players.get(i).getName());
            if (i < players.size() - 1)
                sb.append(", ");
        }
        return sb.toString();
    }

    public static Difficulty selectDifficulty() {
        cls();
        System.out.println("Wybierz poziom trudności:");
        System.out.println("1. Łatwy (0-100)");
        System.out.println("2. Normalny (0-10000)");
        System.out.println("3. Trudny (0-1000000)");
        System.out.println("4. Zaawansowany (własny zakres)");

        int choice = safeNextInt();

        switch (choice) {
            case 2:
                return Difficulty.normal();
            case 3:
                return Difficulty.hard();
            case 4:
                System.out.print("Podaj wartość minimalną: ");
                int min = safeNextInt();
                System.out.print("Podaj wartość maksymalną: ");
                int max = safeNextInt();
                if (max <= min) {
                    System.out.println("Nieprawidłowy zakres! Ustawiam domyślny.");
                    return Difficulty.easy();
                }
                return Difficulty.custom(min, max);
            default:
                return Difficulty.easy();
        }
    }

    public static boolean askPlayAgain() {
        System.out.println("\nCzy chcesz zagrać ponownie? (1-Tak, 2-Nie)");
        return safeNextInt() == 1;
    }

    public static Difficulty askWinnerChangeDifficulty(Player winner, Difficulty current) {
        System.out.println("\n" + winner.getName() + " wygrywa!");
        System.out.println("Czy " + winner.getName() + " chce zmienić poziom trudności? (1-Tak, 2-Nie)");
        if (safeNextInt() == 1) {
            return selectDifficulty();
        }
        return current;
    }

    // ========== TRYBY GRY ==========

    // Tryb 1: Gracze zgadują liczbę wylosowaną przez komputer
    public static Difficulty playersGuess(Difficulty difficulty) {
        int number = random.nextInt(difficulty.min, difficulty.max);
        Map<Player, Integer> attempts = new HashMap<>();

        cls();
        System.out.println("=== GRACZE ZGADUJĄ ===");
        System.out.println("Poziom: " + difficulty.name);
        System.out.println("Komputer wylosował liczbę...\n");

        for (Player player : players) {
            int playerAttempts = 0;
            System.out.println("\n--- Tura: " + player.getName() + " ---");

            while (true) {
                System.out.print("Zgadnij liczbę: ");
                int guess = safeNextInt();
                playerAttempts++;

                if (guess < number) {
                    System.out.println("Większa!");
                } else if (guess > number) {
                    System.out.println("Mniejsza!");
                } else {
                    System.out.println("Zgadłeś w " + playerAttempts + " próbach!");
                    attempts.put(player, playerAttempts);
                    player.updateSingleRecord(difficulty.key, playerAttempts);
                    break;
                }
            }
        }

        // Podsumowanie
        cls();
        System.out.println("=== WYNIKI ===");
        System.out.println("Liczba do zgadnięcia: " + number);

        Player winner = null;
        int bestScore = Integer.MAX_VALUE;

        for (Map.Entry<Player, Integer> entry : attempts.entrySet()) {
            System.out.println(entry.getKey().getName() + ": " + entry.getValue() + " prób");
            if (entry.getValue() < bestScore) {
                bestScore = entry.getValue();
                winner = entry.getKey();
            }
        }

        if (winner != null) {
            System.out.println("\nZwycięzca: " + winner.getName() + " z wynikiem " + bestScore);
            return askWinnerChangeDifficulty(winner, difficulty);
        }

        return difficulty;
    }

    // Tryb 2: Komputer zgaduje liczbę gracza
    public static Difficulty computerGuesses(Difficulty difficulty) {
        cls();
        System.out.println("=== KOMPUTER ZGADUJE ===");
        System.out.println("Poziom: " + difficulty.name);
        System.out.println("Pomyśl liczbę z zakresu " + difficulty.min + "-" + (difficulty.max - 1));
        System.out.println("\nNaciśnij Enter gdy będziesz gotowy...");
        scanner.nextLine();
        scanner.nextLine();

        computer.initRange(difficulty.min, difficulty.max);
        int attempts = 0;

        while (true) {
            int guess = computer.makeGuess();
            attempts++;

            cls();
            System.out.println(computer.getName() + " zgaduje: " + guess);
            System.out.println("\n1. Mniejsza");
            System.out.println("2. Większa");
            System.out.println("3. Zgadł!");

            int response = safeNextInt();

            switch (response) {
                case 1:
                    computer.updateRange(guess, false);
                    break;
                case 2:
                    computer.updateRange(guess, true);
                    break;
                case 3:
                    System.out.println("\n" + computer.getName() + " zgadł w " + attempts + " próbach!");
                    computer.updateSingleRecord(difficulty.key, attempts);
                    return askWinnerChangeDifficulty(computer, difficulty);
            }

            if (computer.isRangeExhausted()) {
                attempts++;
                System.out.println("\nMusi być: " + computer.getForcedGuess());
                computer.updateSingleRecord(difficulty.key, attempts);
                return askWinnerChangeDifficulty(computer, difficulty);
            }
        }
    }

    // Tryb 3: Gracze vs Komputer (na przemian)
    public static Difficulty versusMode(Difficulty difficulty) {
        int computerNumber = random.nextInt(difficulty.min, difficulty.max); // Liczba dla graczy
        int playerTurn = 0;

        // Losowanie kto zaczyna
        boolean computerStarts = random.nextBoolean();

        cls();
        System.out.println("=== VERSUS MODE ===");
        System.out.println("Poziom: " + difficulty.name);
        System.out.println(computerStarts ? "Zaczyna: " + computer.getName() : "Zaczyna: " + players.get(0).getName());
        System.out.println("\nPomyśl liczbę dla komputera z zakresu " + difficulty.min + "-" + (difficulty.max - 1));
        System.out.println("Naciśnij Enter gdy będziesz gotowy...");
        scanner.nextLine();
        scanner.nextLine();

        computer.initRange(difficulty.min, difficulty.max);
        int[] playerAttempts = new int[players.size()];
        int computerAttempts = 0;
        int turn = 0;

        while (true) {
            boolean isComputerTurn = (turn % 2 == 0) == computerStarts;

            if (isComputerTurn) {
                // Tura komputera
                int guess = computer.makeGuess();
                computerAttempts++;

                cls();
                System.out.println("--- TURA: " + computer.getName() + " ---");
                System.out.println("Zgaduje: " + guess);
                System.out.println("\n1. Mniejsza");
                System.out.println("2. Większa");
                System.out.println("3. Zgadł!");

                int response = safeNextInt();

                switch (response) {
                    case 1:
                        computer.updateRange(guess, false);
                        break;
                    case 2:
                        computer.updateRange(guess, true);
                        break;
                    case 3:
                        System.out.println("\n" + computer.getName() + " WYGRYWA!");
                        computer.addMultiWin(difficulty.key);
                        for (Player p : players)
                            p.addMultiLoss(difficulty.key);
                        return askWinnerChangeDifficulty(computer, difficulty);
                }
            } else {
                // Tura gracza
                Player currentPlayer = players.get(playerTurn % players.size());
                playerAttempts[playerTurn % players.size()]++;

                cls();
                System.out.println("--- TURA: " + currentPlayer.getName() + " ---");
                System.out.print("Zgadnij liczbę: ");
                int guess = safeNextInt();

                if (guess < computerNumber) {
                    System.out.println("Większa!");
                } else if (guess > computerNumber) {
                    System.out.println("Mniejsza!");
                } else {
                    System.out.println("\n" + currentPlayer.getName() + " WYGRYWA!");
                    currentPlayer.addMultiWin(difficulty.key);
                    computer.addMultiLoss(difficulty.key);
                    for (Player p : players) {
                        if (p != currentPlayer)
                            p.addMultiLoss(difficulty.key);
                    }
                    return askWinnerChangeDifficulty(currentPlayer, difficulty);
                }

                playerTurn++;
            }

            turn++;
        }
    }

    // Tryb 4: Multiplayer - wszyscy gracze zgadują tę samą liczbę - pierwszy
    // wygrywa!
    public static Difficulty multiplayerMode(Difficulty difficulty) {
        int number = random.nextInt(difficulty.min, difficulty.max);
        int[] attempts = new int[players.size()];

        cls();
        System.out.println("=== MULTIPLAYER ===");
        System.out.println("Poziom: " + difficulty.name);
        System.out.println("Komputer wylosował liczbę. Gracze zgadują po kolei.\n");

        Player winner = null;
        int winnerAttempts = 0;

        int round = 0;
        gameLoop: while (true) {
            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);
                System.out.println("\n--- Runda " + (round + 1) + ": " + player.getName() + " ---");
                System.out.print("Zgadnij: ");
                int guess = safeNextInt();
                attempts[i]++;

                if (guess < number) {
                    System.out.println("Większa!");
                } else if (guess > number) {
                    System.out.println("Mniejsza!");
                } else {
                    System.out.println(player.getName() + " ZGADŁ w " + attempts[i] + " próbach!");
                    winner = player;
                    winnerAttempts = attempts[i];
                    break gameLoop; // Kończymy grę natychmiast!
                }
            }
            round++;
        }

        // Podsumowanie
        cls();
        System.out.println("=== WYNIKI MULTIPLAYER ===");
        System.out.println("Liczba: " + number);
        System.out.println("\nZWYCIĘZCA: " + winner.getName() + " w " + winnerAttempts + " próbach!");

        winner.updateSingleRecord(difficulty.key, winnerAttempts);
        winner.addMultiWin(difficulty.key);
        for (Player p : players) {
            if (p != winner)
                p.addMultiLoss(difficulty.key);
        }

        return askWinnerChangeDifficulty(winner, difficulty);
    }

    // ========== TRYB TURNIEJOWY ==========

    public static void tournamentMode(Difficulty difficulty) {
        cls();
        System.out.println("=== TURNIEJ ===");
        System.out.println("Poziom: " + difficulty.name);

        // Sprawdź czy aktualny mistrz bierze udział
        Player currentMaster = null;
        for (Player p : players) {
            if (p.isMaster()) {
                currentMaster = p;
                System.out.println("🏆 Aktualny MISTRZ: " + p.getName() + " broni tytułu!");
                break;
            }
        }

        // Wybór formatu
        System.out.println("\nWybierz format turnieju:");
        System.out.println("1. Szybki (wszystko BO1)");
        System.out.println("2. Standardowy (ćwierćfinały BO1, półfinały BO3, finał BO5)");
        System.out.println("3. Długi (wszystko BO3, finał BO5)");

        int format = safeNextInt();

        // Przygotuj listę uczestników
        List<Player> participants = new ArrayList<>(players);

        if (participants.size() < 2) {
            System.out.println("Potrzeba minimum 2 graczy do turnieju!");
            waitForEnter();
            return;
        }

        // Dolosuj do potęgi 2
        while (participants.size() != 2 && participants.size() != 4 && participants.size() != 8) {
            if (participants.size() < 4) {
                // Dodaj komputer jako wypełnienie
                System.out.println("Dodano " + computer.getName() + " do turnieju (wyrównanie drabinki)");
                participants.add(computer);
            } else if (participants.size() < 8 && participants.size() > 4) {
                participants.add(computer);
            } else {
                break;
            }
        }

        // Losowanie drabinki
        Collections.shuffle(participants);

        cls();
        System.out.println("=== DRABINKA TURNIEJU ===\n");
        for (int i = 0; i < participants.size(); i += 2) {
            System.out.println("Mecz " + ((i / 2) + 1) + ": " + participants.get(i).getName() +
                    " vs " + participants.get(i + 1).getName());
        }
        System.out.println("\nNaciśnij Enter aby rozpocząć...");
        waitForEnter();

        // Rozgrywaj rundy
        int round = 1;
        String[] roundNames = { "Ćwierćfinały", "Półfinały", "FINAŁ" };

        while (participants.size() > 1) {
            List<Player> winners = new ArrayList<>();
            String roundName = participants.size() > 4 ? roundNames[0]
                    : (participants.size() > 2 ? roundNames[1] : roundNames[2]);

            // Określ format BO dla tej rundy
            int bestOf = 1;
            if (format == 2) { // Standardowy
                if (participants.size() == 2)
                    bestOf = 5; // Finał
                else if (participants.size() <= 4)
                    bestOf = 3; // Półfinały
            } else if (format == 3) { // Długi
                bestOf = participants.size() == 2 ? 5 : 3;
            }

            cls();
            System.out.println("=== " + roundName + " (BO" + bestOf + ") ===\n");

            for (int i = 0; i < participants.size(); i += 2) {
                Player p1 = participants.get(i);
                Player p2 = participants.get(i + 1);

                System.out.println("\n--- MECZ: " + p1.getName() + " vs " + p2.getName() + " ---");
                Player matchWinner = playMatch(p1, p2, bestOf, difficulty);
                winners.add(matchWinner);

                System.out.println("✓ Zwycięzca: " + matchWinner.getName());
            }

            participants = winners;
            round++;

            if (participants.size() > 1) {
                System.out.println("\nNaciśnij Enter aby kontynuować turniej...");
                waitForEnter();
            }
        }

        // Ogłoś mistrza
        Player champion = participants.get(0);
        cls();
        System.out.println("╔════════════════════════════════════╗");
        System.out.println("║     🏆 ZWYCIĘZCA TURNIEJU 🏆       ║");
        System.out.println("║                                    ║");
        System.out.println("║   " + centerText(champion.getName(), 32) + "   ║");
        System.out.println("║                                    ║");
        System.out.println("╚════════════════════════════════════╝");

        // Obsługa tytułu MISTRZA
        if (currentMaster != null && currentMaster != champion) {
            System.out.println("\n" + currentMaster.getName() + " traci tytuł MISTRZA!");
            currentMaster.setMaster(false);
        }

        champion.addTournamentWin();
        champion.addMultiWin(difficulty.key); // Dodatkowa wygrana multi
        System.out.println("\n🏆 " + champion.getName() + " otrzymuje tytuł MISTRZA!");
        System.out.println("   Przywileje: wybór kto zaczyna + druga szansa w turnieju");

        waitForEnter();
    }

    // Pojedynczy mecz BO1/3/5
    public static Player playMatch(Player p1, Player p2, int bestOf, Difficulty difficulty) {
        int winsNeeded = (bestOf / 2) + 1;
        int p1Wins = 0;
        int p2Wins = 0;
        int round = 1;

        // Przywilej mistrza: wybór kto zaczyna
        boolean p1Starts = random.nextBoolean();
        if (p1.isMaster()) {
            System.out.println(p1.getName() + " (MISTRZ) wybiera kto zaczyna: 1-Ja, 2-Przeciwnik");
            p1Starts = safeNextInt() == 1;
        } else if (p2.isMaster()) {
            System.out.println(p2.getName() + " (MISTRZ) wybiera kto zaczyna: 1-Ja, 2-Przeciwnik");
            p1Starts = safeNextInt() != 1;
        }

        // Resetuj drugą szansę na mecz
        p1.grantSecondChance();
        p2.grantSecondChance();

        while (p1Wins < winsNeeded && p2Wins < winsNeeded) {
            System.out.println("\n  Runda " + round + "/" + bestOf + " | Wynik: " + p1Wins + "-" + p2Wins);

            Player roundWinner = playRound(p1, p2, difficulty, p1Starts);

            if (roundWinner == p1)
                p1Wins++;
            else
                p2Wins++;

            p1Starts = !p1Starts; // Zmiana kto zaczyna
            round++;
        }

        return p1Wins > p2Wins ? p1 : p2;
    }

    // Pojedyncza runda - kto pierwszy zgadnie
    public static Player playRound(Player p1, Player p2, Difficulty difficulty, boolean p1Starts) {
        int number = random.nextInt(difficulty.min, difficulty.max);
        int range = difficulty.max - difficulty.min;

        int p1Attempts = 0;
        int p2Attempts = 0;
        boolean p1Turn = p1Starts;

        // Znajdź gracza-komputer i zainicjalizuj jego zakres
        ComputerPlayer comp = null;
        if (p1 instanceof ComputerPlayer) {
            comp = (ComputerPlayer) p1;
            comp.initRange(difficulty.min, difficulty.max);
        } else if (p2 instanceof ComputerPlayer) {
            comp = (ComputerPlayer) p2;
            comp.initRange(difficulty.min, difficulty.max);
        }

        while (true) {
            Player current = p1Turn ? p1 : p2;
            int attempts = p1Turn ? ++p1Attempts : ++p2Attempts;

            int guess;
            if (current.isComputer()) {
                // Komputer używa bisekcji (już zaktualizowany przez ruchy gracza)
                if (current instanceof ComputerPlayer) {
                    ComputerPlayer c = (ComputerPlayer) current;
                    guess = c.makeGuess();
                    System.out.println("    " + current.getName() + " zgaduje: " + guess);
                } else {
                    guess = random.nextInt(difficulty.min, difficulty.max);
                }
            } else {
                System.out.print("    " + current.getName() + " zgaduje: ");
                guess = safeNextInt();
            }

            if (guess == number) {
                System.out.println("    ✓ " + current.getName() + " zgadł w " + attempts + " próbach!");
                return current;
            }

            boolean tooLow = guess < number;
            String hint = tooLow ? "Większa!" : "Mniejsza!";
            System.out.println("    " + hint);

            // KOMPUTER UCZY SIĘ Z KAŻDEGO RUCHU (swojego i przeciwnika!)
            if (comp != null) {
                comp.updateRange(guess, tooLow);
            }

            // Przywilej lidera: podpowiedź ciepło/zimno
            if (current.isLeader()) {
                String leaderHint = current.getHint(guess, number, range);
                if (leaderHint != null) {
                    System.out.println("    [LIDER] " + leaderHint);
                }
            }

            // Przywilej mistrza: druga szansa
            if (current.isMaster() && current.hasSecondChance() && !current.isComputer()) {
                System.out.println("    [MISTRZ] Użyć drugą szansę? (1-Tak, 2-Nie)");
                if (safeNextInt() == 1) {
                    current.useSecondChance();
                    System.out.println("    Dodatkowa próba!");
                    continue; // Nie zmieniaj tury
                }
            }

            p1Turn = !p1Turn;
        }
    }

    public static String centerText(String text, int width) {
        if (text.length() >= width)
            return text;
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
    }

    public static void waitForEnter() {
        scanner.nextLine();
        scanner.nextLine();
    }

    // ========== STATYSTYKI ==========

    public static void displayAllStats() {
        cls();
        System.out.println("=== STATYSTYKI ===\n");

        // Statystyki graczy
        for (Player player : players) {
            String titles = "";
            if (player.isMaster())
                titles += " 🏆MISTRZ";
            if (player.isLeader())
                titles += " ⭐LIDER";

            System.out.println("--- " + player.getName() + titles + " ---");
            System.out.println("Wygrane multi: " + player.getTotalMultiWins());
            System.out.println("Wygrane turnieje: " + player.getTournamentWins());

            System.out.println("Single (rekordy):");
            Map<String, Integer> records = player.getAllSingleRecords();
            if (records.isEmpty()) {
                System.out.println("  Brak rekordów");
            } else {
                for (Map.Entry<String, Integer> e : records.entrySet()) {
                    System.out.println("  " + e.getKey() + ": " + e.getValue());
                }
            }

            System.out.println("Multi (W/L):");
            Map<String, int[]> stats = player.getAllMultiStats();
            if (stats.isEmpty()) {
                System.out.println("  Brak statystyk");
            } else {
                for (Map.Entry<String, int[]> e : stats.entrySet()) {
                    System.out.println("  " + e.getKey() + ": " + e.getValue()[0] + "W / " + e.getValue()[1] + "L");
                }
            }
            System.out.println();
        }

        // Statystyki komputera
        System.out.println("--- " + computer.getName() + " (Komputer) ---");
        System.out.println("Single (rekordy):");
        Map<String, Integer> compRecords = computer.getAllSingleRecords();
        if (compRecords.isEmpty()) {
            System.out.println("  Brak rekordów");
        } else {
            for (Map.Entry<String, Integer> e : compRecords.entrySet()) {
                System.out.println("  " + e.getKey() + ": " + e.getValue());
            }
        }

        System.out.println("Multi (W/L):");
        Map<String, int[]> compStats = computer.getAllMultiStats();
        if (compStats.isEmpty()) {
            System.out.println("  Brak statystyk");
        } else {
            for (Map.Entry<String, int[]> e : compStats.entrySet()) {
                System.out.println("  " + e.getKey() + ": " + e.getValue()[0] + "W / " + e.getValue()[1] + "L");
            }
        }

        System.out.println("\nNaciśnij Enter aby wrócić...");
        scanner.nextLine();
        scanner.nextLine();
    }

    // ========== ZAPIS/ODCZYT ==========

    public static void saveAllPlayers() {
        for (Player player : players) {
            player.saveToFile(SAVE_DIR);
        }
        computer.saveToFile(SAVE_DIR);
        System.out.println("Zapisano wszystkich graczy!");
    }
}
