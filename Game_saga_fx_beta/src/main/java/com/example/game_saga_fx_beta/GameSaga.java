package com.example.game_saga_fx_beta;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class GameSaga extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("🎮 Game Saga");
        BorderPane mainLayout=new BorderPane();

        FileInputStream bgImagePath = null;
        try {
            bgImagePath = new FileInputStream("D:\\Java Programes\\DSA\\IntelliJ Idea\\Game_saga_fx_beta\\Images\\bg1.jpg");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Image backgroundImage = new Image(bgImagePath);
        BackgroundImage bgImage = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT);
        mainLayout.setBackground(new Background(bgImage));

        ImageView bg1Image=new ImageView(backgroundImage);
        bg1Image.setFitHeight(600);
        bg1Image.setFitWidth(1600);

        Label titleLabel = new Label("Welcome to Game Saga!");
        titleLabel.setFont(Font.font("Chiller", 50));
        titleLabel.setStyle(" -fx-text-fill: Red;");

        Button ticTacToeButton = new Button("Play Tic-Tac-Toe");
        ticTacToeButton.setFont(Font.font("Chiller", 30)); // Change font size to 20
        ticTacToeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Button memoryGameButton = new Button("Play Memory Game");
        memoryGameButton.setFont(Font.font("Chiller", 30)); // Change font size to 20
        memoryGameButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        ticTacToeButton.setOnAction(e -> {
            TicTacToe ticTacToe = new TicTacToe();
            ticTacToe.start(primaryStage);
        });

        memoryGameButton.setOnAction(e -> {
            MemoryGameFX memoryGame = new MemoryGameFX();
            memoryGame.start(primaryStage);
        });

        VBox layout = new VBox(20, titleLabel, ticTacToeButton, memoryGameButton);
        layout.setAlignment(Pos.CENTER);
       // layout.setStyle("-fx-background-color: linear-gradient(to bottom, #1e3c72, #2a5298); -fx-padding: 50;");

        mainLayout.setCenter(layout);
        Scene scene = new Scene(mainLayout, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setMaximized(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
