package client;

import model.GameState;
import model.Move;
import model.Player;
import service.GameService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.Naming;
import java.util.List;

public class GameClientGUI extends JFrame { 

    private JLabel lblYou = new JLabel();
    private JLabel lblOpponent = new JLabel();


    private GameService service;
    private Player me;
    private String gameId;

    private JButton[][] buttons = new JButton[3][3];
    private JLabel lblStatus = new JLabel("Waiting another player...");
    private JLabel lblPlayerInfo = new JLabel();

    private Color X_COLOR = new Color(220, 20, 60);      // Red
    private Color O_COLOR = new Color(30, 144, 255);     // Blue
    private Color BOARD_BG = new Color(240, 240, 240);

    public GameClientGUI() {

        // ---------- Connect to RMI Server ----------
        try {
            String serverIP = JOptionPane.showInputDialog("Enter Server IP:");
            service = (GameService) Naming.lookup("rmi://" + serverIP + "/GameService");    

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Cannot connect to server.");
            System.exit(0);
        }

        // ---------- MAIN WINDOW ----------
        setTitle("Tic Tac Toe — RMI Multiplayer");
        setSize(480, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        lblStatus.setFont(new Font("Arial", Font.BOLD, 18));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        lblPlayerInfo.setFont(new Font("Arial", Font.PLAIN, 14));
        lblPlayerInfo.setHorizontalAlignment(SwingConstants.CENTER);


        JPanel topPanel = new JPanel(new GridLayout(3, 1));
        topPanel.add(lblStatus);
        topPanel.add(lblYou);
        topPanel.add(lblOpponent);
        add(topPanel, BorderLayout.NORTH);

        // ----------- BOARD -----------
        JPanel board = new JPanel(new GridLayout(3, 3, 5, 5));
        board.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        board.setBackground(BOARD_BG);


    

        Font font = new Font("Arial", Font.BOLD, 48);

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                JButton btn = new JButton("");
                btn.setFont(font);
                btn.setFocusPainted(false);
                btn.setBackground(Color.white);

                int rr = r, cc = c;
                btn.addActionListener(e -> makeMove(rr, cc));

                buttons[r][c] = btn;
                board.add(btn);
            }
        }

        add(board, BorderLayout.CENTER);

        // ----------- MENU CHOICE -----------
        SwingUtilities.invokeLater(this::startMenu);

        // Auto-refresh
        new Timer(800, e -> updateBoard()).start();

        setVisible(true);
    }

    // ------------------ MENU ------------------
private void startMenu() {

    String name = JOptionPane.showInputDialog(this, "Enter your name:");

    if (name == null || name.trim().isEmpty())
        System.exit(0);

    String[] menu = {"Create Game", "Join Game"};
    int choice = JOptionPane.showOptionDialog(
            this, "Choose an option:", "Tic Tac Toe Menu",
            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
            null, menu, menu[0]
    );

    try {
        if (choice == 0) {
            gameId = service.createGame(name);
            me = waitForSecondPlayer(name);
        } else {
            List<String> openGames = service.listOpenGames();

            if (openGames.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No games available.");
                System.exit(0);
            }

            Object selected = JOptionPane.showInputDialog(
                    this, "Select game to join:",
                    "Join Game",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    openGames.toArray(),
                    openGames.get(0)
            );

            gameId = selected.toString();
            me = service.joinGame(gameId, name);
        }

        lblPlayerInfo.setText("You are: " + me.getSymbol() + " | Game ID: " + gameId);

        // ---------------- SHOW PLAYER NAMES ----------------
        List<Player> players = service.getGameState(gameId).getPlayers();

        lblYou.setText("You: " + me.getName() + " (" + me.getSymbol() + ")");

        Player opponent = players.stream()
                .filter(p -> !p.getId().equals(me.getId()))
                .findFirst()
                .orElse(null);

        if (opponent != null) {
            lblOpponent.setText("Opponent: " + opponent.getName() + " (" + opponent.getSymbol() + ")");
        } else {
            lblOpponent.setText("Opponent: waiting...");
        }

        lblStatus.setText("Waiting for turn...");

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        System.exit(0);
    }
}


    private Player waitForSecondPlayer(String name) throws Exception {
        lblStatus.setText("Waiting for opponent...");

        while (true) {
            GameState gs = service.getGameState(gameId);

            if (gs.getPlayers().size() == 2) {
                return gs.getPlayers().stream()
                        .filter(p -> p.getName().equals(name))
                        .findFirst()
                        .orElse(null);
            }
            Thread.sleep(1000);
        }
    }

    // ------------------ MAKE MOVE ------------------
    private void makeMove(int r, int c) {
        try {
            GameState gs = service.getGameState(gameId);

            if (!gs.getCurrentTurnPlayerId().equals(me.getId())) {
                JOptionPane.showMessageDialog(this, "Not your turn!");
                return;
            }

            Move mv = new Move(me.getId(), r, c);
            service.makeMove(gameId, mv);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Move failed: " + e.getMessage());
        }
    }

    // ------------------ UPDATE UI ------------------
    private void updateBoard() {
        try {
            if (gameId == null) return;

            GameState gs = service.getGameState(gameId);
            char[][] board = gs.getBoard().getCells();

            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {

                    String text = String.valueOf(board[r][c]);
                    JButton btn = buttons[r][c];

                    btn.setText(text.equals(" ") ? "" : text);

                    if (text.equals("X"))
                        btn.setForeground(X_COLOR);
                    else if (text.equals("O"))
                        btn.setForeground(O_COLOR);
                }
            }

            // Show game status
            if (gs.getStatus().toString().equals("FINISHED")) {
                if (gs.getWinnerPlayerId() == null)
                    lblStatus.setText("Draw!");
                else if (gs.getWinnerPlayerId().equals(me.getId()))
                    lblStatus.setText("You WIN!");
                else
                    lblStatus.setText("You LOSE!");

                disableBoard();
            } else {
                lblStatus.setText(
                        gs.getCurrentTurnPlayerId().equals(me.getId())
                        ? "Your Turn"
                        : "Opponent's Turn"
                );
            }

        } catch (Exception ignored) {}
    }

    private void disableBoard() {
        for (JButton[] row : buttons)
            for (JButton btn : row)
                btn.setEnabled(false);
    }

    // ------------------ MAIN ------------------
    public static void main(String[] args) {
        new GameClientGUI();
    }
}
