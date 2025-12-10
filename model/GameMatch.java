// model/GameMatch.java
package model;

import java.io.Serializable;

public class GameMatch implements Serializable {
    private String gameId;
    private Player player;

    public GameMatch(String gameId, Player player) {
        this.gameId = gameId;
        this.player = player;
    }

    // Getters and Setters must be present for RMI to work smoothly
    public String getGameId() { return gameId; }
    public Player getPlayer() { return player; }
    // ... (you may add setters, but getters are essential)
}