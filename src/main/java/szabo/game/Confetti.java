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

/**
 * Creates and manages a confetti effect.
 */
public class Confetti {
    private static final double DURATION = 600;
    private final Random random = new Random();
    private final double MAX_DISTANCE;
    private final List<ConfettiParticle> particles = new ArrayList<>();
    private final Pane parent;

    /**
     * Creates a confetti effect that can be triggered by the {@link #start()} method.
     *
     * @param particleWidth  the width of the particles
     * @param particleHeight the height of the particles
     * @param count          the number of particle to generate
     * @param parent         the Pane where the effect should show
     * @param x              the horizontal center of the effect
     * @param y              the vertical center of the effect
     * @param scatter        the amount to scatter the particles at generation
     * @param maxTravel      the maximum distance travel
     * @param color          the color of the effect, if it's {@code null}, random colors are used
     */
    public Confetti(double particleWidth, double particleHeight, int count, Pane parent,
                    double x, double y, double scatter, double maxTravel, Color color) {
        MAX_DISTANCE = maxTravel;
        this.parent = parent;
        for (int i = 0; i < count; i++) {
            double px = x + random.nextDouble(-scatter, scatter);
            double py = y + random.nextDouble(-scatter, scatter);
            var particle = new ConfettiParticle(px, py, particleWidth, particleHeight, color);
            particle.onFinishedRemoveFrom(parent);
            particles.add(particle);
        }
    }

    /**
     * Starts the animation of the effect. Particles are added to and later
     * removed from the children of the {@code parent} Pane when the animation ends.
     */
    public void start() {
        parent.getChildren().addAll(particles);
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