package szabo.game;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


public class Leaderboard {
    private final List<ScoreEntry> entries = new ArrayList<>();
    private final int scoreLimit;
    private Runnable onChange = null;

    public Leaderboard(String filepath, int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be greater than 0");
        }
        scoreLimit = limit;
        if (filepath != null) {
            loadEntries(filepath);
        }
    }

    public List<ScoreEntry> getScores() {
        return List.copyOf(entries);
    }

    public int getHighScore() {
        return (entries.isEmpty()) ? 0 : entries.getFirst().score;
    }

    public boolean isNewLeaderboardScore(int score) {
        if (entries.isEmpty() || entries.size() < scoreLimit)
            return true;
        return entries.getLast().score < score;
    }

    public void addEntry(String username, int score) {
        if (!isNewLeaderboardScore(score))
            throw new IllegalArgumentException("Score is too low");
        ScoreEntry newEntry = new ScoreEntry(username.trim(), score);
        insertEntry(newEntry);
        if (onChange != null)
            onChange.run();
    }

    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    private void insertEntry(ScoreEntry entry) {
        if (entries.size() == scoreLimit) {
            entries.removeLast();
        }
        entries.add(entry);
        entries.sort(Comparator.comparingInt(ScoreEntry::score).reversed());
    }

    private void loadEntries(String filepath) {
        entries.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                int sep = line.indexOf('#');
                int score = Integer.parseInt(line.substring(0, sep));
                String name = line.substring(sep + 1);
                entries.add(new ScoreEntry(name, score));
            }
            entries.sort(Comparator.comparingInt(ScoreEntry::score).reversed());
            if (entries.size() > scoreLimit) {
                entries.subList(scoreLimit, entries.size()).clear();
            }
        } catch (IOException e) {
            System.err.println("Failed to load Leaderboard: " + e.getMessage());
        }
        if (entries.size() > scoreLimit) {
            entries.subList(scoreLimit, entries.size()).clear();
        }
    }

    public void save(String filepath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filepath))) {
            for (ScoreEntry entry : entries) {
                bw.write(entry.score() + "#" + entry.name());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save Leaderboard: " + e.getMessage());
        }
    }

    public record ScoreEntry(String name, int score) {
        private static final int MAX_USERNAME_LENGTH = 15;

        public ScoreEntry {
            Objects.requireNonNull(name);
            if (name.length() > MAX_USERNAME_LENGTH) {
                throw new IllegalArgumentException("Username is too long, max length is " + MAX_USERNAME_LENGTH);
            }
        }
    }

}
