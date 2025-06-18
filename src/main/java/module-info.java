module szabo.game {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.dyn4j;

    opens szabo.game to javafx.fxml;
    exports szabo.game;
}