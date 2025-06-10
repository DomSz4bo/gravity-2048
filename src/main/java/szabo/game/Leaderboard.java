package szabo.game;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


public class Leaderboard {
    private static final String FILE_PATH = "leaderboard.dat";
    private static final int SCORE_LIMIT = 10;

    private final List<ScoreEntry> entries = new ArrayList<>();

    public Leaderboard() {
        loadEntries();

    }

    public List<ScoreEntry> getScores() {
        return List.copyOf(entries);
    }

    public int getHighScore() {
        return (entries.isEmpty()) ? 0 : entries.getFirst().score;
    }

    public boolean isNewLeaderboardScore(int score) {
        if (entries.isEmpty() || entries.size() < SCORE_LIMIT)
            return true;
        return entries.getLast().score < score;
    }

    public void addEntry(String username, int score) {
        if (!isNewLeaderboardScore(score))
            throw new IllegalArgumentException("Score is too low");
        ScoreEntry newEntry = new ScoreEntry(username, score);
        insertEntry(newEntry);
    }

    private void insertEntry(ScoreEntry entry) {
        if (entries.size() == SCORE_LIMIT) {
            entries.removeLast();
        }
        entries.add(entry);
        entries.sort(Comparator.comparingInt(ScoreEntry::score).reversed());
    }

    private void loadEntries() {
        entries.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                int sep = line.indexOf('#');
                int score = Integer.parseInt(line.substring(0, sep));
                String name = line.substring(sep + 1);
                entries.add(new ScoreEntry(name, score));
            }
            entries.sort(Comparator.comparingInt(ScoreEntry::score).reversed());
            if (entries.size() > SCORE_LIMIT) {
                entries.subList(SCORE_LIMIT, entries.size()).clear();
            }
        } catch (IOException e) {
            System.err.println("Failed to load Leaderboard: " + e.getMessage());
        }
        System.out.println("Loaded " + entries.size() + " entries");
        if (entries.size() > SCORE_LIMIT) {
            throw new IllegalStateException("Too many entries");
        }
    }

    private void saveEntries() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (ScoreEntry entry : entries) {
                bw.write(entry.score() + "#" + entry.name());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save Leaderboard: " + e.getMessage());
//            throw new RuntimeException(e);
        }
    }

    public record ScoreEntry(String name, int score) {
        private static final int MAX_USERNAME_LENGTH = 12;

        public ScoreEntry {
            Objects.requireNonNull(name);
            if (name.length() > MAX_USERNAME_LENGTH) {
                throw new IllegalArgumentException("Username is too long, max length is " + MAX_USERNAME_LENGTH);
            }
            if (name.indexOf('#') != -1) {
                throw new IllegalArgumentException("Invalid name: Cannot contain '#'.");
            }
        }
    }

}
