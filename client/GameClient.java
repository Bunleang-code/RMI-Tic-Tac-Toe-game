package client;

import model.GameState;
import model.Move;
import model.Player;
import service.GameService;

import java.rmi.Naming;
import java.util.List;
import java.util.Scanner;

public class GameClient {

    public static void main(String[] args) throws Exception {

        GameService service = (GameService) Naming.lookup("rmi://localhost/GameService");
        Scanner sc = new Scanner(System.in);

        System.out.println("Welcome to RMI Tic-Tac-Toe");
        System.out.print("Enter your name: ");
        String name = sc.nextLine().trim();

        System.out.println("1) Create Game\n2) Join Game\nChoose: ");
        int choice = Integer.parseInt(sc.nextLine());

        String gameId = null;
        Player me = null;

        // -------------------------------------------------------
        // Create Game
        // -------------------------------------------------------
        if (choice == 1) {
            gameId = service.createGame(name);
            System.out.println("Created game with id: " + gameId);
            System.out.println("Waiting for another player to join...");

            // Poll until second player joins
            while (true) {
                GameState gs = service.getGameState(gameId);

                if (gs.getPlayers().size() >= 2) {
                    me = gs.getPlayers()
                           .stream()
                           .filter(p -> p.getName().equals(name))
                           .findFirst()
                           .orElse(null);
                    break;
                }

                Thread.sleep(1000);
            }
        }

        // -------------------------------------------------------
        // Join Game
        // -------------------------------------------------------
        else {
            List<String> open = service.listOpenGames();

            if (open.isEmpty()) {
                System.out.println("No open games. Try creating one.");
                return;
            }

            System.out.println("Open games:");
            for (int i = 0; i < open.size(); i++) {
                System.out.println((i + 1) + ") " + open.get(i));
            }

            System.out.print("Choose game number: ");
            int gchoice = Integer.parseInt(sc.nextLine());

            gameId = open.get(gchoice - 1);
            me = service.joinGame(gameId, name);

            System.out.println("Joined game as " + me.getSymbol());
        }

        // -------------------------------------------------------
        // Game Loop
        // -------------------------------------------------------
        while (true) {
            GameState gs = service.getGameState(gameId);
            printBoard(gs);

            // Check game finished
            if (gs.getStatus().toString().equals("FINISHED")) {

                if (gs.getWinnerPlayerId() == null) {
                    System.out.println("Game ended in a draw.");
                }
                else if (gs.getWinnerPlayerId().equals(me.getId())) {
                    System.out.println("You win!");
                }
                else {
                    System.out.println("You lose.");
                }

                break;
            }

            // Not player's turn
            if (!gs.getCurrentTurnPlayerId().equals(me.getId())) {
                System.out.println("Waiting for opponent's move...");
                Thread.sleep(1000);
                continue;
            }

            // Player makes move
            System.out.print("Your move. Enter row (0-2): ");
            int row = Integer.parseInt(sc.nextLine());

            System.out.print("Enter col (0-2): ");
            int col = Integer.parseInt(sc.nextLine());

            try {
                Move mv = new Move(me.getId(), row, col);
                gs = service.makeMove(gameId, mv);
            } 
            catch (Exception ex) {
                System.out.println("Move failed: " + ex.getMessage());
            }
        }

        sc.close();
    }

    // -------------------------------------------------------
    // Print Board Helper
    // -------------------------------------------------------
    private static void printBoard(GameState gs) {
        char[][] c = gs.getBoard().getCells();

        System.out.println("Current board:");
        for (int i = 0; i < 3; i++) {
            System.out.println(" " + c[i][0] + " | " + c[i][1] + " | " + c[i][2]);
            if (i < 2) System.out.println("-----------");
        }
    }
}
