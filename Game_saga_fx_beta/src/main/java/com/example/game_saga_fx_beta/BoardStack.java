package com.example.game_saga_fx_beta;

public class BoardStack {
    char[][][] stack;
    int top;

    public BoardStack(int size) {
        stack = new char[size][3][3];
        top = -1;
    }

    public void push(char[][] currentBoard) {
        if (top < stack.length - 1) {
            top++;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    stack[top][i][j] = currentBoard[i][j];
                }
            }
        }
    }

    public char[][] pop() {
        if (top >= 0) {
            char[][] prevBoard = new char[3][3];
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    prevBoard[i][j] = stack[top][i][j];
                }
            }
            top--;
            return prevBoard;
        }
        return null;
    }

    public boolean isEmpty() {
        return top == -1;
    }
}

