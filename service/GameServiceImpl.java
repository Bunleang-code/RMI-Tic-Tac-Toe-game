package service;

import model.*; // Assuming GameMatch, GameState, Player, Move, GameStatus are here
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class GameServiceImpl extends UnicastRemoteObject implements GameService {

    private final Map<String, GameState> games = new HashMap<>();

    public GameServiceImpl() throws RemoteException {
        super();
    }
    
    // -------------------------------------------------------
    // Helper: Generate Unique 4-Digit Game ID
    // -------------------------------------------------------
    private String generateUnique4DigitGameId() {
        // Generates a random number from 1000 to 9999
        Random random = new Random();
        String gameId;
        
        do {
            int num = random.nextInt(9000) + 1000;
            gameId = String.valueOf(num);
            
            // Check if this ID is already in use by an active game
        } while (games.containsKey(gameId)); 
        
        return gameId;
    }


    // -------------------------------------------------------
    // Automated Matchmaking: Find or Create Game
    // -------------------------------------------------------
    /**
     * Attempts to join an existing game waiting for a player. If none exists, 
     * it creates a new game.
     */
    @Override
    public synchronized GameMatch findOrCreateGame(String playerName) throws RemoteException {
        
        // 1. Check for any waiting games (WAITING_FOR_PLAYER)
        String openGameId = null;
        for (Map.Entry<String, GameState> entry : games.entrySet()) {
            if (entry.getValue().getStatus() == GameStatus.WAITING_FOR_PLAYER) {
                openGameId = entry.getKey();
                break; // Found one, exit loop
            }
        }
        
        if (openGameId != null) {
            // 2. Found an open game, JOIN it
            // This is Player O
            Player p = joinGame(openGameId, playerName); 
            return new GameMatch(openGameId, p);
            
        } else {
            // 3. No open game, CREATE a new one
            // This is Player X
            String newGameId = createGame(playerName);
            
            // Find the Player object created for the game creator
            GameState gs = games.get(newGameId);
            Player p = gs.getPlayers().get(0);
            
            return new GameMatch(newGameId, p);
        }
    }
    
    // -------------------------------------------------------
    // Create Game (Used internally by findOrCreateGame)
    // -------------------------------------------------------
    @Override
    public synchronized String createGame(String playerName) throws RemoteException {
        String gameId = generateUnique4DigitGameId();
        GameState gs = new GameState(gameId);

        // First player is always 'X'
        Player p = new Player(playerName, 'X');
        gs.getPlayers().add(p);

        gs.setStatus(GameStatus.WAITING_FOR_PLAYER);
        gs.setCurrentTurnPlayerId(p.getId());

        games.put(gameId, gs);
        return gameId;
    }

    // -------------------------------------------------------
    // Join Game (Used internally by findOrCreateGame)
    // -------------------------------------------------------
    @Override
    public synchronized Player joinGame(String gameId, String playerName) throws RemoteException {
        GameState gs = games.get(gameId);

        if (gs == null)
            throw new RemoteException("Game not found");

        if (gs.getPlayers().size() >= 2)
            throw new RemoteException("Game full");

        // Assign opposite symbol (Second player is always 'O')
        char symbol = (gs.getPlayers().get(0).getSymbol() == 'X') ? 'O' : 'X';
        Player p = new Player(playerName, symbol);

        gs.getPlayers().add(p);
        gs.setStatus(GameStatus.IN_PROGRESS);

        return p;
    }

    // -------------------------------------------------------
    // List Open Games (Cleanup Logic)
    // -------------------------------------------------------
    @Override
    public synchronized List<String> listOpenGames() throws RemoteException {
        List<String> open = new ArrayList<>();
        
        // Use an Iterator to safely remove finished games while iterating
        Iterator<Map.Entry<String, GameState>> iterator = games.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, GameState> entry = iterator.next();
            GameState gs = entry.getValue();

            if (gs.getStatus() == GameStatus.WAITING_FOR_PLAYER) {
                // Game is still open and waiting for a second player
                open.add(entry.getKey());
            } else if (gs.getStatus() == GameStatus.FINISHED) {
                // Game is finished, remove it to free up the 4-digit ID
                iterator.remove();
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