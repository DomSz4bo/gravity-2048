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

    // TODO
    //  * make travel a radius so that it has a circular shape
    //  * make it so that the Color is corresponding to the Block being created or merged
    public Confetti(double particleWidth, double particleHeight, int count, Pane parent, double x, double y, double radius, double maxTravel) {
        MAX_DISTANCE = maxTravel;
        for (int i = 0; i < count; i++) {
            double px = x + random.nextDouble(-radius, radius);
            double py = y + random.nextDouble(-radius, radius);
            var particle = new ConfettiParticle(px, py, particleWidth, particleHeight);
            parent.getChildren().add(particle);
            particle.onFinishedRemoveFrom(parent);
            particles.add(particle);
        }
    }
    private Color getRandomColor() {
        return Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }
    public void runEffect() {
        particles.forEach(ConfettiParticle::animate);
    }

    private class ConfettiParticle extends Rectangle {
        private final TranslateTransition translateTransition;
        private final FadeTransition fadeTransition;
        private ConfettiParticle(double x, double y, double width, double height) {
            super(x, y, width, height);
            setFill(getRandomColor());
            setRotate(random.nextInt(360));

            translateTransition = new TranslateTransition(Duration.millis(DURATION), this);
            translateTransition.setByX(random.nextDouble(-1.0, 1.0) * MAX_DISTANCE);
            translateTransition.setByY(random.nextDouble(-1.0, 1.0) * MAX_DISTANCE);
            translateTransition.setInterpolator(Interpolator.LINEAR);

            fadeTransition = new FadeTransition(Duration.millis(DURATION), this);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);
            fadeTransition.setInterpolator(Interpolator.EASE_IN);
        }
        private void onFinishedRemoveFrom(Pane parent) {
            translateTransition.setOnFinished(event -> parent.getChildren().remove(this));
        }

        private void animate() {
            translateTransition.play();
            fadeTransition.play();
        }
    }
}