package client;

import model.GameState;
import model.Move;
import model.Player;
import model.GameMatch;
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

    private String playerName;

    private JButton[][] buttons = new JButton[3][3];
    private JLabel lblStatus = new JLabel("Waiting another player...");
    private JLabel lblPlayerInfo = new JLabel();

    private JButton btnPlayAgain = new JButton("Play Again");

    private Color X_COLOR = new Color(220, 20, 60);   // Red
    private Color O_COLOR = new Color(30, 144, 255);  // Blue

    private Timer gameUpdateTimer;

    public GameClientGUI() {

        try {
            String serverIP = JOptionPane.showInputDialog("Enter Server IP:");
            service = (GameService) Naming.lookup("rmi://" + serverIP + "/GameService");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Cannot connect to server.");
            System.exit(0);
        }

        // ---------- MAIN WINDOW ----------
        setTitle("Tic Tac Toe — RMI Multiplayer");
        setSize(520, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        Font titleFont = new Font("SansSerif", Font.BOLD, 20);
        Font infoFont = new Font("SansSerif", Font.PLAIN, 16);

        lblStatus.setFont(titleFont);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        lblYou.setFont(infoFont);
        lblOpponent.setFont(infoFont);
        lblYou.setHorizontalAlignment(SwingConstants.CENTER);
        lblOpponent.setHorizontalAlignment(SwingConstants.CENTER);

        // -------- TOP PANEL --------
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        topPanel.setBackground(new Color(250, 250, 250));

        topPanel.add(lblStatus);
        topPanel.add(Box.createVerticalStrut(8));
        topPanel.add(lblYou);
        topPanel.add(lblOpponent);

        add(topPanel, BorderLayout.NORTH);

        // -------- BOARD --------
        JPanel board = new JPanel(new GridLayout(3, 3, 8, 8));
        board.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        board.setBackground(new Color(230, 230, 230));

        Font font = new Font("SansSerif", Font.BOLD, 48);

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                JButton btn = createAnimatedButton();
                btn.setFont(font);

                int rr = r, cc = c;
                btn.addActionListener(e -> makeMove(rr, cc));

                buttons[r][c] = btn;
                board.add(btn);
            }
        }

        add(board, BorderLayout.CENTER);

        // -------- BOTTOM --------
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(250, 250, 250));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));

        btnPlayAgain.setFont(new Font("SansSerif", Font.BOLD, 18));
        btnPlayAgain.setBackground(new Color(70, 130, 180));
        btnPlayAgain.setForeground(Color.WHITE);
        btnPlayAgain.setFocusPainted(false);
        btnPlayAgain.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnPlayAgain.setVisible(false);
        btnPlayAgain.addActionListener(e -> resetGame());

        bottomPanel.add(btnPlayAgain);
        add(bottomPanel, BorderLayout.SOUTH);

        // Auto-refresh
        gameUpdateTimer = new Timer(800, e -> updateBoard());

        SwingUtilities.invokeLater(this::startMenu);
        setVisible(true);
    }

    // ------------------------------------------------
    //   BEAUTIFUL ROUNDED BUTTON WITH ALL ANIMATIONS
    // ------------------------------------------------
    private JButton createAnimatedButton() {

        JButton b = new JButton() {

            private float scale = 1.0f;
            private Color hoverColor = new Color(245, 245, 245);
            private Color normalColor = Color.white;

            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBackground(normalColor);

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (!isEnabled()) return;

                        // Background fade
                        setBackground(hoverColor);

                        // Border highlight
                        setBorder(BorderFactory.createLineBorder(new Color(100, 149, 237), 2));

                        // Glow effect
                        setShadow(true);

                        // Scale animation
                        new Timer(10, evt -> {
                            scale += 0.02f;
                            if (scale >= 1.08f) scale = 1.08f;
                            repaint();
                        }).start();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (!isEnabled()) return;

                        setBackground(normalColor);
                        setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));
                        setShadow(false);

                        new Timer(10, evt -> {
                            scale -= 0.02f;
                            if (scale <= 1.0f) scale = 1.0f;
                            repaint();
                        }).start();
                    }
                });
            }

            private boolean shadow = false;

            public void setShadow(boolean v) {
                shadow = v;
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Apply scale animation
                int newW = (int) (w * scale);
                int newH = (int) (h * scale);
                int x = (w - newW) / 2;
                int y = (h - newH) / 2;

                // Glow shadow
                if (shadow) {
                    g2.setColor(new Color(100, 149, 237, 70));
                    g2.fillRoundRect(x - 4, y - 4, newW + 8, newH + 8, 25, 25);
                }

                // Button background
                g2.setColor(getBackground());
                g2.fillRoundRect(x, y, newW, newH, 20, 20);

                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Border already drawn above
            }
        };

        b.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));
        return b;
    }

    // ------------------ MENU ------------------
    private void startMenu() {

        if (gameUpdateTimer != null && gameUpdateTimer.isRunning()) {
            gameUpdateTimer.stop();
        }

        if (this.playerName == null) {
            String inputName = JOptionPane.showInputDialog(this, "Enter your name:");

            if (inputName == null || inputName.trim().isEmpty()) {
                System.exit(0);
            }
            this.playerName = inputName;
        }

        int choice = JOptionPane.showConfirmDialog(
            this, "Ready to find a match, " + this.playerName + "?",
            "Matchmaking", JOptionPane.OK_CANCEL_OPTION
        );

        if (choice != JOptionPane.OK_OPTION) System.exit(0);

        try {
            GameMatch match = service.findOrCreateGame(this.playerName);
            gameId = match.getGameId();
            me = match.getPlayer();

            if (service.getGameState(gameId).getPlayers().size() < 2) {
                me = waitForSecondPlayer(this.playerName);
            }

            List<Player> players = service.getGameState(gameId).getPlayers();
            lblYou.setText("You: " + me.getName() + " (" + me.getSymbol() + ")");

            Player opp = players.stream().filter(p -> !p.getId().equals(me.getId())).findFirst().orElse(null);
            lblOpponent.setText("Opponent: " + opp.getName() + " (" + opp.getSymbol() + ")");

            lblStatus.setText("Waiting for turn...");
            gameUpdateTimer.start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Matchmaking error: " + ex.getMessage());
            System.exit(0);
        }
    }

    private Player waitForSecondPlayer(String name) throws Exception {
        lblStatus.setText("Waiting for opponent...");

        while (true) {
            GameState gs = service.getGameState(gameId);

            if (gs.getPlayers().size() == 2) {
                Player opp = gs.getPlayers().stream().filter(p -> !p.getName().equals(name)).findFirst().orElse(null);
                lblOpponent.setText("Opponent: " + opp.getName() + " (" + opp.getSymbol() + ")");

                return gs.getPlayers().stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
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

    // ------------------ UPDATE BOARD ------------------
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

        btnPlayAgain.setVisible(true);
    }

    private void resetGame() {

        gameId = null;
        me = null;

        lblYou.setText("");
        lblOpponent.setText("");
        lblPlayerInfo.setText("");
        lblStatus.setText("Waiting another player...");
        btnPlayAgain.setVisible(false);

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                buttons[r][c].setText("");
                buttons[r][c].setEnabled(true);
            }
        }

        startMenu();
    }

    public static void main(String[] args) {
        new GameClientGUI();
    }
}
