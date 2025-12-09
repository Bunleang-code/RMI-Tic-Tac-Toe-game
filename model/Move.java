package model;

import java.io.Serializable;

public class Move implements Serializable {

    private final String playerId;
    private final int row;
    private final int col;
    
    public Move(String playerId, int row, int col) {
        this.playerId = playerId;
        this.row = row;
        this.col = col;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
