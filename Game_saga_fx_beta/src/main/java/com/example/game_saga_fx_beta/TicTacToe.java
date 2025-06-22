package com.example.game_saga_fx_beta;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class TicTacToe extends Application {

    // Game board (buttons)
    Button[][] buttons = new Button[3][3];
    char[][] board = new char[3][3];
    BoardStack undoStack = new BoardStack(9);
    char currentPlayer = 'X';

    @Override
    public void start(Stage primaryStage) {
        initBoard();

        BorderPane mainLayout=new BorderPane();

        FileInputStream bgImagePath = null;
        try {
            bgImagePath = new FileInputStream("D:\\Java Programes\\DSA\\IntelliJ Idea\\Game_saga_fx_beta\\Images\\bg3.jpg");
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



        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-background-color: linear-gradient(to bottom right, #1e3c72, #2a5298); -fx-padding: 20;");

        Font font = Font.font("Arial", 36);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Button btn = new Button("");
                btn.setFont(font);
                btn.setPrefSize(100, 100);
                btn.setStyle("-fx-background-color: linear-gradient(#f2f2f2, #d6d6d6);" +
                        "-fx-border-color: #333333;" +
                        "-fx-border-width: 3;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.5, 0, 0);");
                int row = i;
                int col = j;

                btn.setOnAction(e -> {
                    if (btn.getText().equals("") && !checkWin('X') && !checkWin('O')) {
                        undoStack.push(board);
                        board[row][col] = currentPlayer;
                        btn.setText(String.valueOf(currentPlayer));
                        btn.setTextFill(currentPlayer == 'X' ? Color.RED : Color.BLUE);
                        btn.setStyle("-fx-background-color: #ffffff; -fx-border-color: black; -fx-border-width: 2;");

                        if (checkWin(currentPlayer)) {
                            highlightWin(currentPlayer);
                        } else if (isDraw()) {
                            System.out.println("It's a draw!");
                        } else {
                            currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
                        }
                    }
                });

                buttons[i][j] = btn;
                grid.add(btn, j, i);
            }
        }

        // Undo Button
        Button undoBtn = new Button("Undo");
        undoBtn.setFont(Font.font("Arial", 20));
        undoBtn.setPrefSize(150, 50);
        undoBtn.setStyle("-fx-background-color: linear-gradient(to top left, #ff512f, #dd2476);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 30;" +
                "-fx-border-radius: 30;" +
                "-fx-effect: dropshadow(gaussian, rgba(255, 87, 34, 0.8), 10, 0.5, 0, 0);");
        undoBtn.setOnAction(e -> {
            if (!undoStack.isEmpty()) {
                board = undoStack.pop();
                refreshBoard();
                currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
            }
        });

        VBox root = new VBox(20, grid, undoBtn);
        root.setAlignment(Pos.CENTER);

        mainLayout.setCenter(root);
        Scene scene = new Scene(mainLayout, 450, 600);
        primaryStage.setTitle("Tic-Tac-Toe FX (With Undo)");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setMaximized(true);
    }

    // Initialize the board
    public void initBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = ' ';
            }
        }
    }

    // Refresh GUI from board state
    public void refreshBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                char ch = board[i][j];
                buttons[i][j].setText(ch == ' ' ? "" : String.valueOf(ch));
                buttons[i][j].setTextFill(ch == 'X' ? Color.RED : Color.BLUE);
                buttons[i][j].setStyle("-fx-background-color: #ffffff; -fx-border-color: black; -fx-border-width: 2;");
            }
        }
    }

    // Check win condition
    public boolean checkWin(char player) {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == player && board[i][1] == player && board[i][2] == player) return true;
            if (board[0][i] == player && board[1][i] == player && board[2][i] == player) return true;
        }
        if (board[0][0] == player && board[1][1] == player && board[2][2] == player) return true;
        if (board[0][2] == player && board[1][1] == player && board[2][0] == player) return true;
        return false;
    }

    // Highlight winning line
    public void highlightWin(char player) {
        Color winColor = Color.LIMEGREEN;

        for (int i = 0; i < 3; i++) {
            // Row win
            if (board[i][0] == player && board[i][1] == player && board[i][2] == player) {
                for (int j = 0; j < 3; j++) {
                    buttons[i][j].setStyle("-fx-background-color: limegreen; -fx-text-fill: white;");
                }
                return;
            }
            // Column win
            if (board[0][i] == player && board[1][i] == player && board[2][i] == player) {
                for (int j = 0; j < 3; j++) {
                    buttons[j][i].setStyle("-fx-background-color: limegreen; -fx-text-fill: white;");
                }
                return;
            }
        }
        // Diagonal win
        if (board[0][0] == player && board[1][1] == player && board[2][2] == player) {
            buttons[0][0].setStyle("-fx-background-color: limegreen; -fx-text-fill: white;");
            buttons[1][1].setStyle("-fx-background-color: limegreen; -fx-text-fill: white;");
            buttons[2][2].setStyle("-fx-background-color: limegreen; -fx-text-fill: white;");
            return;
        }
        if (board[0][2] == player && board[1][1] == player && board[2][0] == player) {
            buttons[0][2].setStyle("-fx-background-color: limegreen; -fx-text-fill: white;");
            buttons[1][1].setStyle("-fx-background-color: limegreen; -fx-text-fill: white;");
            buttons[2][0].setStyle("-fx-background-color: limegreen; -fx-text-fill: white;");
        }
    }

    // Check draw
    public boolean isDraw() {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (board[i][j] == ' ')
                    return false;
        return true;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
