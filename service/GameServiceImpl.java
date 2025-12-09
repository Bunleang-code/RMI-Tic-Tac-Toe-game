package service;

import model.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class GameServiceImpl extends UnicastRemoteObject implements GameService {

    private final Map<String, GameState> games = new HashMap<>();

    public GameServiceImpl() throws RemoteException {
        super();
    }

    // -------------------------------------------------------
    // Create Game
    // -------------------------------------------------------
    @Override
    public synchronized String createGame(String playerName) throws RemoteException {
        String gameId = UUID.randomUUID().toString();
        GameState gs = new GameState(gameId);

        Player p = new Player(playerName, 'X');
        gs.getPlayers().add(p);

        gs.setStatus(GameStatus.WAITING_FOR_PLAYER);
        gs.setCurrentTurnPlayerId(p.getId());

        games.put(gameId, gs);
        return gameId;
    }

    // -------------------------------------------------------
    // Join Game
    // -------------------------------------------------------
    @Override
    public synchronized Player joinGame(String gameId, String playerName) throws RemoteException {
        GameState gs = games.get(gameId);

        if (gs == null)
            throw new RemoteException("Game not found");

        if (gs.getPlayers().size() >= 2)
            throw new RemoteException("Game full");

        // Assign opposite symbol
        char symbol = (gs.getPlayers().get(0).getSymbol() == 'X') ? 'O' : 'X';
        Player p = new Player(playerName, symbol);

        gs.getPlayers().add(p);
        gs.setStatus(GameStatus.IN_PROGRESS);

        return p;
    }

    // -------------------------------------------------------
    // List Open Games
    // -------------------------------------------------------
    @Override
    public synchronized List<String> listOpenGames() throws RemoteException {
        List<String> open = new ArrayList<>();

        for (Map.Entry<String, GameState> entry : games.entrySet()) {
            if (entry.getValue().getStatus() == GameStatus.WAITING_FOR_PLAYER) {
                open.add(entry.getKey());
            }
        }

        return open;
    }

    // -------------------------------------------------------
    // Make a Move
    // -------------------------------------------------------
    @Override
    public GameState makeMove(String gameId, Move move) throws RemoteException {
        GameState gs = games.get(gameId);

        if (gs == null)
            throw new RemoteException("Game not found");

        synchronized (gs) {

            // Validate game state
            if (gs.getStatus() != GameStatus.IN_PROGRESS &&
                gs.getStatus() != GameStatus.WAITING_FOR_PLAYER) {
                throw new RemoteException("Game not in progress");
            }

            // Find the player making the move
            Player mover = gs.getPlayers()
                    .stream()
                    .filter(p -> p.getId().equals(move.getPlayerId()))
                    .findFirst()
                    .orElse(null);

            if (mover == null)
                throw new RemoteException("Player not in game");

            // Check turn
            if (!mover.getId().equals(gs.getCurrentTurnPlayerId()))
                throw new RemoteException("Not your turn");

            // Try placing move
            boolean placed = gs.getBoard().place(move.getRow(), move.getCol(), mover.getSymbol());
            if (!placed)
                throw new RemoteException("Invalid move");

            // Check winner
            Character winner = gs.getBoard().checkWinner();

            if (winner != null) {
                // Find winning player
                for (Player p : gs.getPlayers()) {
                    if (p.getSymbol() == winner) {
                        gs.setWinnerPlayerId(p.getId());
                        gs.setStatus(GameStatus.FINISHED);
                        break;
                    }
                }
            }
            else if (gs.getBoard().isFull()) {
                gs.setStatus(GameStatus.FINISHED);
                gs.setWinnerPlayerId(null); // draw
            }
            else {
                // Switch turn
                for (Player p : gs.getPlayers()) {
                    if (!p.getId().equals(mover.getId())) {
                        gs.setCurrentTurnPlayerId(p.getId());
                        break;
                    }
                }
            }

            return gs;
        }
    }

    // -------------------------------------------------------
    // Get Game State
    // -------------------------------------------------------
    @Override
    public GameState getGameState(String gameId) throws RemoteException {
        GameState gs = games.get(gameId);

        if (gs == null)
            throw new RemoteException("Game not found");

        return gs;
    }
}
