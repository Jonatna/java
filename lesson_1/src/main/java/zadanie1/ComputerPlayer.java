package zadanie1;

import java.util.Random;

/**
 * Klasa reprezentująca gracza komputerowego z algorytmem bisekcji
 */
public class ComputerPlayer extends Player {
    private static final long serialVersionUID = 1L;

    private transient Random random;
    private transient int min;
    private transient int max;

    public ComputerPlayer(String name) {
        super(name);
        this.isComputer = true;
        this.random = new Random();
    }

    // Inicjalizacja zakresu przed grą
    public void initRange(int min, int max) {
        this.min = min;
        this.max = max;
        if (this.random == null) {
            this.random = new Random();
        }
    }

    // Zgadnij liczbę używając bisekcji
    public int makeGuess() {
        if (random == null) {
            random = new Random();
        }
        return (min + max) / 2;
    }

    // Aktualizuj zakres na podstawie podpowiedzi
    public void updateRange(int guess, boolean tooLow) {
        if (tooLow) {
            min = guess + 1;
        } else {
            max = guess;
        }
    }

    // Czy zakres jest wyczerpany
    public boolean isRangeExhausted() {
        return min >= max;
    }

    public int getForcedGuess() {
        return min;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
