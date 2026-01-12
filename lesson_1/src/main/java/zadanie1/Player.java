package zadanie1;

import java.io.*;
import java.util.*;

/**
 * Klasa reprezentująca gracza (człowieka lub komputer)
 */
public class Player implements Serializable {
    private static final long serialVersionUID = 2L; // Zwiększona wersja dla nowych pól

    protected String name;
    protected boolean isComputer;

    // Statystyki single: difficulty -> najlepszy wynik
    protected Map<String, Integer> singleRecords;

    // Statystyki multi: difficulty -> [wygrane, przegrane]
    protected Map<String, int[]> multiStats;

    // === TYTUŁY ===
    protected boolean isLeader = false; // Lider (najwięcej wygranych multi)
    protected boolean isMaster = false; // Mistrz (zwycięzca turnieju)
    protected int totalMultiWins = 0; // Suma wszystkich wygranych multi
    protected int tournamentWins = 0; // Ilość wygranych turniejów

    // Przywileje
    protected boolean hasSecondChance = false; // Druga szansa w rundzie (dla mistrza)

    public Player(String name) {
        this.name = name;
        this.isComputer = false;
        this.singleRecords = new HashMap<>();
        this.multiStats = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        String title = "";
        if (isMaster)
            title = "🏆 ";
        else if (isLeader)
            title = "⭐ ";
        return title + name;
    }

    public boolean isComputer() {
        return isComputer;
    }

    // === TYTUŁY ===

    public boolean isLeader() {
        return isLeader;
    }

    public void setLeader(boolean leader) {
        this.isLeader = leader;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        this.isMaster = master;
    }

    public int getTotalMultiWins() {
        return totalMultiWins;
    }

    public int getTournamentWins() {
        return tournamentWins;
    }

    public void addTournamentWin() {
        this.tournamentWins++;
        this.isMaster = true;
    }

    // === PRZYWILEJE ===

    public boolean hasSecondChance() {
        return hasSecondChance;
    }

    public void grantSecondChance() {
        if (isMaster) {
            this.hasSecondChance = true;
        }
    }

    public void useSecondChance() {
        this.hasSecondChance = false;
    }

    // Przywilej lidera: podpowiedź ciepło/zimno
    public String getHint(int guess, int target, int range) {
        if (!isLeader)
            return null;

        int distance = Math.abs(guess - target);
        double ratio = (double) distance / range;

        if (ratio < 0.1)
            return "🔥 Gorąco!";
        else if (ratio < 0.25)
            return "☀️ Ciepło";
        else if (ratio < 0.5)
            return "❄️ Chłodno";
        else
            return "🥶 Zimno";
    }

    // Przywilej mistrza poza turniejem: zawężony zakres (-5%)
    public int getMasterBonusRange(int originalRange) {
        if (!isMaster)
            return originalRange;
        return (int) (originalRange * 0.95);
    }

    // Single records
    public Integer getSingleRecord(String difficulty) {
        return singleRecords.get(difficulty);
    }

    public void updateSingleRecord(String difficulty, int score) {
        Integer current = singleRecords.get(difficulty);
        if (current == null || score < current) {
            singleRecords.put(difficulty, score);
        }
    }

    // Multi stats
    public int[] getMultiStats(String difficulty) {
        return multiStats.getOrDefault(difficulty, new int[] { 0, 0 });
    }

    public void addMultiWin(String difficulty) {
        int[] stats = multiStats.getOrDefault(difficulty, new int[] { 0, 0 });
        stats[0]++;
        multiStats.put(difficulty, stats);
        totalMultiWins++;
    }

    public void addMultiLoss(String difficulty) {
        int[] stats = multiStats.getOrDefault(difficulty, new int[] { 0, 0 });
        stats[1]++;
        multiStats.put(difficulty, stats);
    }

    public Map<String, Integer> getAllSingleRecords() {
        return singleRecords;
    }

    public Map<String, int[]> getAllMultiStats() {
        return multiStats;
    }

    // Zapis do pliku
    public void saveToFile(String directory) {
        try {
            new File(directory).mkdirs();
            String filename = directory + "/" + name.replaceAll("[^a-zA-Z0-9]", "_") + ".dat";
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
                oos.writeObject(this);
            }
        } catch (IOException e) {
            System.out.println("Błąd zapisu gracza: " + e.getMessage());
        }
    }

    // Odczyt z pliku
    public static Player loadFromFile(String directory, String playerName) {
        String filename = directory + "/" + playerName.replaceAll("[^a-zA-Z0-9]", "_") + ".dat";
        File file = new File(filename);

        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (Player) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Błąd odczytu gracza: " + e.getMessage());
            }
        }
        return null;
    }

    public void resetStats() {
        singleRecords.clear();
        multiStats.clear();
        totalMultiWins = 0;
        tournamentWins = 0;
        isLeader = false;
        isMaster = false;
    }

    @Override
    public String toString() {
        String titles = "";
        if (isMaster)
            titles += "[MISTRZ]";
        if (isLeader)
            titles += "[LIDER]";
        return name + (isComputer ? " (Komputer)" : " (Gracz)") + " " + titles;
    }
}
