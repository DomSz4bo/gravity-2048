package szabo.game;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Confetti {
    private static final double DURATION = 600;
    private final Random random = new Random();
    private final double MAX_DISTANCE;
    private final List<ConfettiParticle> particles = new ArrayList<>();

    public Confetti(double particleWidth, double particleHeight, int count, Pane parent,
                    double x, double y, double radius, double maxTravel, Color color) {
        MAX_DISTANCE = maxTravel;
        for (int i = 0; i < count; i++) {
            double px = x + random.nextDouble(-radius, radius);
            double py = y + random.nextDouble(-radius, radius);
            var particle = new ConfettiParticle(px, py, particleWidth, particleHeight, color);
            parent.getChildren().add(particle);
            particle.onFinishedRemoveFrom(parent);
            particles.add(particle);
        }
    }

    public void start() {
        particles.forEach(ConfettiParticle::animate);
    }

    private class ConfettiParticle extends Rectangle {
        private final TranslateTransition translateTransition;
        private final FadeTransition fadeTransition;

        private ConfettiParticle(double x, double y, double width, double height, Color color) {
            super(x, y, width, height);
            setFill((color == null) ? getRandomColor() : color);
            setRotate(random.nextInt(360));
            int direction = random.nextInt(360);
            double travelDistance = random.nextDouble(0.2, 1.0) * MAX_DISTANCE;
            double travelX = Math.cos(direction) * travelDistance;
            double travelY = Math.sin(direction) * travelDistance;

            translateTransition = new TranslateTransition(Duration.millis(DURATION), this);
            translateTransition.setByX(travelX);
            translateTransition.setByY(travelY);
            translateTransition.setInterpolator(Interpolator.EASE_OUT);

            fadeTransition = new FadeTransition(Duration.millis(DURATION * 0.3), this);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);
            fadeTransition.setDelay(Duration.millis(DURATION * 0.7));
        }

        private void onFinishedRemoveFrom(Pane parent) {
            translateTransition.setOnFinished(event -> parent.getChildren().remove(this));
        }

        private void animate() {
            translateTransition.play();
            fadeTransition.play();
        }

        private Color getRandomColor() {
            return Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255));
        }
    }
}