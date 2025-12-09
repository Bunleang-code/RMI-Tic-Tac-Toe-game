package service;

import model.GameState;
import model.Move;
import model.Player;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


public interface GameService extends Remote {
// Create a new game. Returns gameId and the created Player (creator).
String createGame(String playerName) throws RemoteException;


// Join an existing game. Returns Player object assigned to joined player (with symbol).
Player joinGame(String gameId, String playerName) throws RemoteException;


// List open games (gameId strings)
List<String> listOpenGames() throws RemoteException;


// Make a move. Returns updated GameState.
GameState makeMove(String gameId, Move move) throws RemoteException;


// Get current game state
GameState getGameState(String gameId) throws RemoteException;
}
