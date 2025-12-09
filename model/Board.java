package model;

import java.io.Serializable;

public class Board implements Serializable {

    private final char[][] cells = new char[3][3];
    
    public Board() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                cells[i][j] = ' ';
            }
        }
    }

    public char[][] getCells() {
        return cells;
    }

    public boolean place(int row, int col, char symbol) {
        if (row < 0 || row > 2 || col < 0 || col > 2)
            return false;

        if (cells[row][col] != ' ')
            return false;

        cells[row][col] = symbol;
        return true;
    }

    public boolean isFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (cells[i][j] == ' ')
                    return false;
            }
        }
        return true;
    }

    public Character checkWinner() {

        // Check rows
        for (int i = 0; i < 3; i++) {
            if (cells[i][0] != ' ' &&
                cells[i][0] == cells[i][1] &&
                cells[i][1] == cells[i][2]) {
                return cells[i][0];
            }
        }

        // Check columns
        for (int j = 0; j < 3; j++) {
            if (cells[0][j] != ' ' &&
                cells[0][j] == cells[1][j] &&
                cells[1][j] == cells[2][j]) {
                return cells[0][j];
            }
        }

        // Check main diagonal
        if (cells[0][0] != ' ' &&
            cells[0][0] == cells[1][1] &&
            cells[1][1] == cells[2][2]) {
            return cells[0][0];
        }

        // Check anti-diagonal
        if (cells[0][2] != ' ' &&
            cells[0][2] == cells[1][1] &&
            cells[1][1] == cells[2][0]) {
            return cells[0][2];
        }

        // No winner yet
        return null;
    }
}
