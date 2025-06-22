module com.example.game_saga_fx_beta {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.game_saga_fx_beta to javafx.fxml;
    exports com.example.game_saga_fx_beta;
}