package szabo.game;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


/**
 * Class representing the logical part of the leaderboard.
 * Keeps track of leaderboard entries and handles saving and loading
 * entries from files.
 */
public class Leaderboard {
    private final List<ScoreEntry> entries = new ArrayList<>();
    private final int scoreLimit;
    private Runnable onChange = null;

    /**
     * Creates a new {@link Leaderboard} and loads entries from the given file.
     *
     * @param filepath path to the file. If it's {@code null}, the returned leaderboard will be empty.
     * @param limit a limit on the number of entries to load, must be greater than 0.
     */
    public Leaderboard(String filepath, int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be greater than 0");
        }
        scoreLimit = limit;
        if (filepath != null) {
            loadEntries(filepath);
        }
    }

    /**
     * Returns an unmodifiable List of {@link ScoreEntry} representing the leaderboards
     * current scores sorted from the highest to lowest.
     *
     * @return the current scores
     */
    public List<ScoreEntry> getScores() {
        return List.copyOf(entries);
    }

    /**
     * Determines whether the given score would have a place on this leaderboard.
     * A score is eligible for a place if the leaderboard is not full
     * or has an existing score lower than the provided score.
     *
     * @param score the score to consider
     * @return {@code true} if {@code score} is eligible for a place on this leaderboard
     */
    public boolean isNewLeaderboardScore(int score) {
        if (entries.isEmpty() || entries.size() < scoreLimit)
            return true;
        return entries.getLast().score < score;
    }


    /**
     * Add a new entry to this leaderboard with the provided information.
     *
     * @param username name of the player who achieved the score
     * @param score the achieved score
     * @throws IllegalArgumentException if score is too low or username format is incorrect format
     */
    public void addEntry(String username, int score) throws IllegalArgumentException {
        if (!isNewLeaderboardScore(score))
            throw new IllegalArgumentException("Score is too low");
        ScoreEntry newEntry = new ScoreEntry(username.trim(), score);
        insertEntry(newEntry);
    }

    /**
     * Set the action to perform when a change occurs on this leaderboard.
     *
     * @param onChange the action to perform
     */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    /**
     * Insert the provided entry into this leaderboard.
     * This method doesn't check whether the score is
     * actually eligible for a place on this leaderboard.
     *
     * @param entry the new entry
     */
    private void insertEntry(ScoreEntry entry) {
        if (entries.size() == scoreLimit) {
            entries.removeLast();
        }
        entries.add(entry);
        entries.sort(Comparator.comparingInt(ScoreEntry::score).reversed());
        if (onChange != null)
            onChange.run();
    }

    /**
     * Loads entries from the given file. Existing entries are removed.
     * Limits the amount of entries to load to {@link #scoreLimit}.
     *
     * @param filepath the file to load
     */
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

    /**
     * Saves this leaderboard's current entries to the given file.
     *
     * @param filepath the file to save to
     */
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

    /**
     * Class representing a single leaderboard entry.
     *
     * @param name
     * @param score
     */
    public record ScoreEntry(String name, int score) {
        private static final int MAX_USERNAME_LENGTH = 15;

        /**
         * Creates a leaderboard score entry with the given information.
         * Checks whether the name is not longer than {@link #MAX_USERNAME_LENGTH}
         *
         * @param name the name of the player
         * @param score the achieved score
         */
        public ScoreEntry {
            Objects.requireNonNull(name);
            if (name.length() > MAX_USERNAME_LENGTH) {
                throw new IllegalArgumentException("Username is too long, max length is " + MAX_USERNAME_LENGTH);
            }
        }
    }

}
