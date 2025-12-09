package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameState implements Serializable {

    private final String gameId;
    private final List<Player> players = new ArrayList<>();
    private final Board board = new Board();

    private GameStatus status = GameStatus.WAITING_FOR_PLAYER;

    // ID of the player whose turn it is
    private String currentTurnPlayerId;

    // Winner's playerId; null if draw or not finished
    private String winnerPlayerId;

    public GameState(String gameId) {
        this.gameId = gameId;
    }

    public String getGameId() {
        return gameId;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Board getBoard() {
        return board;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public String getCurrentTurnPlayerId() {
        return currentTurnPlayerId;
    }

    public void setCurrentTurnPlayerId(String id) {
        this.currentTurnPlayerId = id;
    }

    public String getWinnerPlayerId() {
        return winnerPlayerId;
    }

    public void setWinnerPlayerId(String id) {
        this.winnerPlayerId = id;
    }
}
