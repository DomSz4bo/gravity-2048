package szabo.game;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.*;
import java.util.ArrayList;


public class Leaderboard extends BorderPane {
    private static final String FILE_PATH = "leaderboard.dat";
    private static final int LIMIT = 10;

    private final ArrayList<LeaderboardEntry> entries = new ArrayList<>();
    private final VBox entryHolder = new VBox();
    private final DoubleBinding minDimension;

    public Leaderboard() {
        loadEntries();
        updateLeaderboard();
        entryHolder.setAlignment(Pos.TOP_CENTER);
        setCenter(entryHolder);

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> setVisible(false));
        var btnPane = new HBox(closeButton);
        btnPane.setAlignment(Pos.CENTER);
        setBottom(btnPane);
        setAlignment(btnPane, Pos.CENTER);

        Label leaderboardLabel = new Label("Leaderboard");
        setTop(leaderboardLabel);
        setAlignment(leaderboardLabel, Pos.CENTER);

        minDimension = Bindings.createDoubleBinding(
                () -> Math.min(getWidth(), getHeight()),
                widthProperty(), heightProperty()
        );

        paddingProperty().bind(Bindings.createObjectBinding(
                () -> new Insets(minDimension.get() / 20),
                widthProperty(), heightProperty()
        ));

        entryHolder.spacingProperty().bind(minDimension.divide(60));
        leaderboardLabel.prefHeightProperty().bind(heightProperty().multiply(0.15));
        leaderboardLabel.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(minDimension.get() / 20), minDimension
        ));
//        leaderboardLabel.setBackground(Background.fill(Color.LIGHTGRAY));
        btnPane.prefHeightProperty().bind(heightProperty().multiply(0.15));
        closeButton.paddingProperty().bind(Bindings.createObjectBinding(
                () -> {
                    double verticalPadding = minDimension.get() / 20;
                    double horizontalPadding = minDimension.get() / 100;
                    return new Insets(horizontalPadding, verticalPadding,
                            horizontalPadding, verticalPadding);
                },
                minDimension
        ));
        closeButton.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(minDimension.get() / 35),
                minDimension
        ));

        setBackground(Background.fill(Color.web("rgba(0, 0, 0, 0.9)")));
    }

    public boolean isNewHighScore(int score) {
        return entries.getLast().score < score;
    }

    public void addEntry(String username, int score) {
        if (!isNewHighScore(score))
            throw new IllegalArgumentException("Score is too low");
        var newEntry = new LeaderboardEntry(username, score, entryHolder);
        insertEntry(newEntry);
    }

    private void updateLeaderboard() {
        entryHolder.getChildren().clear();
        entryHolder.getChildren().addAll(entries);
    }

    private void insertEntry(LeaderboardEntry entry) {
        if (entries.size() == LIMIT) {
            entries.removeLast();
        }
        int insertionPoint = 0;
        for (LeaderboardEntry leaderboardEntry : entries) {
            if (leaderboardEntry.score < entry.score) {
                break;
            }
            insertionPoint++;
        }
        entries.add(insertionPoint, entry);
    }

    private void loadEntries() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                int sep = line.indexOf('#');
                int score = Integer.parseInt(line.substring(0, sep));
                String name = line.substring(sep + 1);
                entries.add(new LeaderboardEntry(name, score, entryHolder));
            }
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (entries.size() > LIMIT) {
            throw new IllegalStateException("Too many entries");
        }
    }

    private void saveEntries() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (LeaderboardEntry leaderboardEntry : entries) {
                bw.write(leaderboardEntry.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class LeaderboardEntry extends HBox {
        private final String name;
        private final int score;

        public LeaderboardEntry(String name, int score, Pane holder) {
            super();
            this.name = name.trim();
            this.score = score;
            Label nameLabel = new Label(this.name);
            Label scoreLabel = new Label(Integer.toString(this.score));
            Region spacer = new Region();
            setHgrow(spacer, Priority.ALWAYS);
            getChildren().addAll(nameLabel, spacer, scoreLabel);
            nameLabel.fontProperty().bind(Bindings.createObjectBinding(
                    () -> Font.font(Math.min(holder.getWidth(), holder.getHeight()) / 30 ),
                    holder.widthProperty(), holder.heightProperty()
            ));
            scoreLabel.fontProperty().bind(Bindings.createObjectBinding(
                    () -> Font.font(Math.min(holder.getWidth(), holder.getHeight()) / 30 ),
                    holder.widthProperty(), holder.heightProperty()
            ));
        }

        @Override
        public String toString() {
            return score + "#" + name;
        }
    }


}
