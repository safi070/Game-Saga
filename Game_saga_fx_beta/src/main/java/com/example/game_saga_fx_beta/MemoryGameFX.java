package com.example.game_saga_fx_beta;


import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;

public class MemoryGameFX extends Application {

    private int SIZE = 4;
    private char[][] board;
    private Button[][] buttons;
    private boolean[][] revealed;

    private int[] flippedPosRow = new int[2];
    private int[] flippedPosCol = new int[2];
    private int flipCount = 0;
    private int matchedPairs = 0;
    private int moveCount = 0;
    private int seconds = 0;
    private boolean timerRunning = false;
    private boolean gameEnded = false;

    private Label moveLabel;
    private Label timerLabel;
    private Button undoButton;
    private Timeline timeline;

    private SimpleStack stackUndo = new SimpleStack(100);

    @Override
    public void start(Stage primaryStage) {
        showDifficultyMenu(primaryStage);
    }

    private void showDifficultyMenu(Stage stage) {
        Button easyBtn = new Button("Easy (4x4)");
        Button hardBtn = new Button("Hard (6x6)");

        easyBtn.setFont(Font.font(20));
        hardBtn.setFont(Font.font(20));

        easyBtn.setOnAction(e -> {
            SIZE = 4;
            setupAndStartGame(stage);
        });
        hardBtn.setOnAction(e -> {
            SIZE = 6;
            setupAndStartGame(stage);
        });

        VBox menu = new VBox(20, new Label("Select Difficulty:"), easyBtn, hardBtn);
        menu.setAlignment(Pos.CENTER);
        menu.setStyle("-fx-background-color: linear-gradient(to bottom, #000000, #333333); -fx-padding: 50;");
        Scene scene = new Scene(menu, 800, 800);

        stage.setScene(scene);
        stage.setTitle("Memory Game - Difficulty");
        stage.show();
        stage.setMaximized(true);
    }

    private void setupAndStartGame(Stage primaryStage) {
        board = new char[SIZE][SIZE];
        buttons = new Button[SIZE][SIZE];
        revealed = new boolean[SIZE][SIZE];

        char[] symbols = generateSymbols(SIZE);
        manualShuffle(symbols);

        int k = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = symbols[k++];
            }
        }

        //BorderPane mainLayout = new BorderPane();
        BorderPane mainLayout = new BorderPane();

        FileInputStream bgImagePath;
        try {
            bgImagePath = new FileInputStream("D:\\Java Programes\\DSA\\IntelliJ Idea\\Game_saga_fx_beta\\Images\\bg2.jpg");
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


        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setStyle("-fx-background-color: linear-gradient(to bottom, #222, #444); -fx-padding: 20;");

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Button btn = createCardButton(i, j);
                buttons[i][j] = btn;
                grid.add(btn, j, i);
            }
        }

        undoButton = new Button("Undo Last Flip");
        undoButton.setFont(Font.font(16));
        undoButton.setStyle("-fx-background-color: #FF6F61; -fx-text-fill: white;");
        undoButton.setOnAction(e -> undoFlip());

        moveLabel = new Label("Moves: 0");
        moveLabel.setFont(Font.font(18));
        moveLabel.setTextFill(Color.WHITE);

        timerLabel = new Label("Time: 0s");
        timerLabel.setFont(Font.font(18));
        timerLabel.setTextFill(Color.WHITE);

        startTimer();

        HBox bottomPanel = new HBox(30, undoButton, moveLabel, timerLabel);
        bottomPanel.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, grid, bottomPanel);
        root.setAlignment(Pos.CENTER);

        mainLayout.setCenter(root);
        Scene scene = new Scene(mainLayout, 800, 900);
        primaryStage.setTitle("Memory Game");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setMaximized(true);
    }

    private Button createCardButton(int row, int col) {
        Button btn = new Button("❓");
        btn.setFont(Font.font(24));
        btn.setPrefSize(100, 100);
        btn.setStyle("-fx-background-color: #336699; -fx-text-fill: white;");
        btn.setOnAction(e -> {
            if (gameEnded || revealed[row][col] || flipCount == 2) return;

            if (!timerRunning) timerRunning = true;

            revealCard(row, col, btn);

            flippedPosRow[flipCount] = row;
            flippedPosCol[flipCount] = col;
            stackUndo.push(row);
            stackUndo.push(col);
            flipCount++;

            if (flipCount == 2) {
                moveCount++;
                moveLabel.setText("Moves: " + moveCount);

                PauseTransition pause = new PauseTransition(Duration.seconds(0.8));
                pause.setOnFinished(ev -> checkMatch());
                pause.play();
            }
        });
        return btn;
    }

    private void revealCard(int row, int col, Button btn) {
        revealed[row][col] = true;
        ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
        st.setFromX(1);
        st.setToX(0);
        st.setOnFinished(e -> {
            btn.setText(Character.toString(board[row][col]));
            btn.setStyle("-fx-background-color: #FFD700; -fx-text-fill: black;");
            ScaleTransition st2 = new ScaleTransition(Duration.millis(150), btn);
            st2.setFromX(0);
            st2.setToX(1);
            st2.play();
        });
        st.play();
    }

    private void hideCard(int row, int col, Button btn) {
        revealed[row][col] = false;
        btn.setText("❓");
        btn.setStyle("-fx-background-color: #336699; -fx-text-fill: white;");
    }

    private void checkMatch() {
        int r1 = flippedPosRow[0], c1 = flippedPosCol[0];
        int r2 = flippedPosRow[1], c2 = flippedPosCol[1];

        if (board[r1][c1] == board[r2][c2]) {
            buttons[r1][c1].setStyle("-fx-background-color: #66BB6A; -fx-text-fill: black;");
            buttons[r2][c2].setStyle("-fx-background-color: #66BB6A; -fx-text-fill: black;");
            matchedPairs++;

            if (matchedPairs == (SIZE * SIZE) / 2) {
                stopTimer();
                showGameWon();
            }
        } else {
            hideCard(r1, c1, buttons[r1][c1]);
            hideCard(r2, c2, buttons[r2][c2]);
        }

        flipCount = 0;
    }

    private void undoFlip() {
        if (gameEnded || stackUndo.size() < 2) return;

        int col = stackUndo.pop();
        int row = stackUndo.pop();

        hideCard(row, col, buttons[row][col]);
        if (flipCount > 0) flipCount--;
    }

    private void manualShuffle(char[] arr) {
        SimpleRandom rand = new SimpleRandom();
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            char tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }

    private char[] generateSymbols(int size) {
        char[] base = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        char[] symbols = new char[size * size];
        int index = 0;
        for (int i = 0; i < (size * size) / 2; i++) {
            symbols[index++] = base[i];
            symbols[index++] = base[i];
        }
        return symbols;
    }

    private void startTimer() {
        seconds = 0;
        timerRunning = false;

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (timerRunning) {
                seconds++;
                timerLabel.setText("Time: " + seconds + "s");
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void stopTimer() {
        timeline.stop();
        timerRunning = false;
    }

    private void showGameWon() {
        gameEnded = true;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("YOU WIN!");
        alert.setHeaderText("You matched all pairs!");
        alert.setContentText("Moves: " + moveCount + ", Time: " + seconds + "s");

        alert.setOnHidden(e -> {
            Stage stage = (Stage) timerLabel.getScene().getWindow();
            showDifficultyMenu(stage);
        });
        alert.show();
    }
}

// Simple Random Generator (no built-in Random)
class SimpleRandom {
    private long seed = System.nanoTime();

    public int nextInt(int bound) {
        seed = (seed * 6364136223846793005L + 1) & 0xFFFFFFFFFFFFL;
        return (int) (Math.abs(seed) % bound);
    }
}

// Simple Stack for Undo
class SimpleStack {
    private int[] stack;
    private int top;

    public SimpleStack(int capacity) {
        stack = new int[capacity];
        top = -1;
    }

    public void push(int value) {
        if (top < stack.length - 1) {
            stack[++top] = value;
        }
    }

    public int pop() {
        if (top >= 0) {
            return stack[top--];
        }
        return -1;
    }

    public int size() {
        return top + 1;
    }
}

//import javafx.animation.*;
//import javafx.application.Application;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.Alert;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.layout.*;
//import javafx.scene.paint.Color;
//import javafx.scene.text.Font;
//import javafx.stage.Stage;
//import javafx.util.Duration;
//
//import java.io.*;
//import java.util.Random;
//
//public class MemoryGameFX extends Application {
//
//    private int SIZE = 4;
//
//    private String[] symbols;
//    private String[][] board;
//    private Button[][] buttons;
//    private boolean[][] revealed;
//
//    private int[][] undoStack = new int[100][2]; // stack to store positions of flips
//    private int top = -1;
//
//    private int matchedPairs = 0;
//    private int moveCount = 0;
//    private int seconds = 0;
//    private boolean timerRunning = false;
//    private boolean gameEnded = false;
//
//    private Label moveLabel;
//    private Label timerLabel;
//    private Button undoButton;
//    private Timeline timeline;
//
//    @Override
//    public void start(Stage primaryStage) {
//        showDifficultyMenu(primaryStage);
//    }
//
//    private void showDifficultyMenu(Stage stage) {
//        Button easyBtn = new Button("Easy (4x4)");
//        Button hardBtn = new Button("Hard (6x6)");
//
//        easyBtn.setFont(Font.font(20));
//        hardBtn.setFont(Font.font(20));
//
//        easyBtn.setOnAction(e -> {
//            SIZE = 4;
//            setupAndStartGame(stage);
//        });
//        hardBtn.setOnAction(e -> {
//            SIZE = 6;
//            setupAndStartGame(stage);
//        });
//
//        VBox menu = new VBox(20, new Label("Select Difficulty:"), easyBtn, hardBtn);
//        menu.setAlignment(Pos.CENTER);
//        menu.setStyle("-fx-background-color: linear-gradient(to bottom, #000000, #333333); -fx-padding: 50;");
//        Scene scene = new Scene(menu, 800, 800);
//
//        stage.setScene(scene);
//        stage.setTitle(" Memory Game - Difficulty");
//        stage.show();
//        stage.setMaximized(true);
//    }
//
//    private void setupAndStartGame(Stage primaryStage) {
//        symbols = generateSymbols(SIZE);
//        manualShuffle(symbols);
//
//        board = new String[SIZE][SIZE];
//        buttons = new Button[SIZE][SIZE];
//        revealed = new boolean[SIZE][SIZE];
//
//        int k = 0;
//        for (int i = 0; i < SIZE; i++)
//            for (int j = 0; j < SIZE; j++)
//                board[i][j] = symbols[k++];
//
//        BorderPane mainLayout = new BorderPane();
//
//        try {
//            FileInputStream bgImagePath = new FileInputStream("D:\\Java Programes\\DSA\\IntelliJ Idea\\Game_saga_alpha\\Images\\bg2.jpg");
//            Image backgroundImage = new Image(bgImagePath);
//            BackgroundImage bgImage = new BackgroundImage(backgroundImage,
//                    BackgroundRepeat.NO_REPEAT,
//                    BackgroundRepeat.NO_REPEAT,
//                    BackgroundPosition.CENTER,
//                    BackgroundSize.DEFAULT);
//            mainLayout.setBackground(new Background(bgImage));
//        } catch (Exception ex) {
//            mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #000000, #333333);");
//        }
//
//        GridPane grid = new GridPane();
//        grid.setAlignment(Pos.CENTER);
//        grid.setVgap(10);
//        grid.setHgap(10);
//        grid.setStyle("-fx-background-color: linear-gradient(to bottom, #222, #444); -fx-padding: 20;");
//
//        for (int i = 0; i < SIZE; i++)
//            for (int j = 0; j < SIZE; j++) {
//                Button btn = createCardButton(i, j);
//                buttons[i][j] = btn;
//                grid.add(btn, j, i);
//            }
//
//        undoButton = new Button("Undo Last Flip");
//        undoButton.setFont(Font.font(16));
//        undoButton.setStyle("-fx-background-color: #FF6F61; -fx-text-fill: white;");
//        undoButton.setOnAction(e -> undoFlip());
//
//        moveLabel = new Label("Moves: 0");
//        moveLabel.setFont(Font.font(18));
//        moveLabel.setTextFill(Color.WHITE);
//
//        timerLabel = new Label("Time: 0s");
//        timerLabel.setFont(Font.font(18));
//        timerLabel.setTextFill(Color.WHITE);
//
//        startTimer();
//
//        HBox bottomPanel = new HBox(30, undoButton, moveLabel, timerLabel);
//        bottomPanel.setAlignment(Pos.CENTER);
//
//        VBox root = new VBox(20, grid, bottomPanel);
//        root.setAlignment(Pos.CENTER);
//
//        mainLayout.setCenter(root);
//        Scene scene = new Scene(mainLayout, 800, 900);
//        primaryStage.setTitle(" Memory Game");
//        primaryStage.setScene(scene);
//        primaryStage.show();
//        primaryStage.setMaximized(true);
//    }
//
//    private Button createCardButton(int row, int col) {
//        Button btn = new Button("❓");
//        btn.setFont(Font.font(24));
//        btn.setPrefSize(100, 100);
//        btn.setStyle("-fx-background-color: #336699; -fx-text-fill: white;");
//        btn.setOnAction(e -> {
//            if (gameEnded || revealed[row][col] || top >= 1) return;
//
//            if (!timerRunning) timerRunning = true;
//            revealCard(row, col, btn);
//
//            top++;
//            undoStack[top][0] = row;
//            undoStack[top][1] = col;
//
//            if (top == 1) {
//                moveCount++;
//                moveLabel.setText("Moves: " + moveCount);
//
//                PauseTransition pause = new PauseTransition(Duration.seconds(0.8));
//                pause.setOnFinished(ev -> checkMatch());
//                pause.play();
//            }
//        });
//        return btn;
//    }
//
//    private void revealCard(int row, int col, Button btn) {
//        revealed[row][col] = true;
//
//        ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
//        st.setFromX(1);
//        st.setToX(0);
//        st.setOnFinished(e -> {
//            btn.setText(board[row][col]);
//            btn.setStyle("-fx-background-color: #FFD700; -fx-text-fill: black;");
//            ScaleTransition st2 = new ScaleTransition(Duration.millis(150), btn);
//            st2.setFromX(0);
//            st2.setToX(1);
//            st2.play();
//        });
//        st.play();
//    }
//
//    private void hideCard(int row, int col, Button btn) {
//        revealed[row][col] = false;
//        btn.setText("❓");
//        btn.setStyle("-fx-background-color: #336699; -fx-text-fill: white;");
//    }
//
//    private void checkMatch() {
//        int r1 = undoStack[0][0], c1 = undoStack[0][1];
//        int r2 = undoStack[1][0], c2 = undoStack[1][1];
//
//        if (board[r1][c1].equals(board[r2][c2])) {
//            buttons[r1][c1].setStyle("-fx-background-color: #66BB6A; -fx-text-fill: black;");
//            buttons[r2][c2].setStyle("-fx-background-color: #66BB6A; -fx-text-fill: black;");
//            matchedPairs++;
//
//            if (matchedPairs == (SIZE * SIZE) / 2) {
//                stopTimer();
//                showGameWon();
//            }
//        } else {
//            hideCard(r1, c1, buttons[r1][c1]);
//            hideCard(r2, c2, buttons[r2][c2]);
//        }
//
//        top = -1;
//    }
//
//    private void undoFlip() {
//        if (gameEnded) return;
//
//        if (top == 0) {
//            int r = undoStack[top][0], c = undoStack[top][1];
//            hideCard(r, c, buttons[r][c]);
//            top = -1;
//        }
//    }
//
//    private void manualShuffle(String[] arr) {
//        Random rand = new Random();
//        for (int i = arr.length - 1; i > 0; i--) {
//            int j = rand.nextInt(i + 1);
//            String tmp = arr[i];
//            arr[i] = arr[j];
//            arr[j] = tmp;
//        }
//    }
//
//    private String[] generateSymbols(int size) {
//        String[] base = {
//                "A", "B", "C", "D", "E", "F", "G", "H",
//                "I", "J", "K", "L", "M", "N", "O", "P",
//                "Q", "R", "S", "T", "U", "V"
//        };
//        String[] symbols = new String[size * size];
//        int index = 0;
//        for (int i = 0; i < (size * size) / 2; i++) {
//            symbols[index++] = base[i];
//            symbols[index++] = base[i];
//        }
//        return symbols;
//    }
//
//    private void startTimer() {
//        seconds = 0;
//        timerRunning = false;
//
//        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
//            if (timerRunning) {
//                seconds++;
//                timerLabel.setText("Time: " + seconds + "s");
//            }
//        }));
//        timeline.setCycleCount(Animation.INDEFINITE);
//        timeline.play();
//    }
//
//    private void stopTimer() {
//        timeline.stop();
//        timerRunning = false;
//    }
//
//    private void showGameWon() {
//        gameEnded = true;
//
//        String record = readHighScore();
//        String currentScore = "Moves: " + moveCount + ", Time: " + seconds + "s";
//
//        boolean newHigh = false;
//        if (record == null || compareScores(currentScore, record)) {
//            writeHighScore(currentScore);
//            record = currentScore;
//            newHigh = true;
//        }
//
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle(" YOU WIN!");
//        alert.setHeaderText("You matched all pairs!");
//        alert.setContentText(currentScore + "\nHigh Score: " + record +
//                (newHigh ? " (NEW!)" : ""));
//
//        alert.setOnHidden(e -> {
//            Stage stage = (Stage) timerLabel.getScene().getWindow();
//            showDifficultyMenu(stage);
//        });
//        alert.show();
//    }
//
//    private boolean compareScores(String current, String record) {
//        int currentTime = Integer.parseInt(current.split(",")[1].trim().replace("Time: ", "").replace("s", "").trim());
//        int recordTime = Integer.parseInt(record.split(",")[1].trim().replace("Time: ", "").replace("s", "").trim());
//        return currentTime < recordTime;
//    }
//
//    private String readHighScore() {
//        try (BufferedReader reader = new BufferedReader(new FileReader("highscore_" + SIZE + "x" + SIZE + ".txt"))) {
//            return reader.readLine();
//        } catch (IOException e) {
//            return null;
//        }
//    }
//
//    private void writeHighScore(String score) {
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter("highscore_" + SIZE + "x" + SIZE + ".txt"))) {
//            writer.write(score);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
