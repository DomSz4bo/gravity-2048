module szabo.game {
    requires javafx.controls;
    requires javafx.fxml;


    opens szabo.game to javafx.fxml;
    exports szabo.game;
}