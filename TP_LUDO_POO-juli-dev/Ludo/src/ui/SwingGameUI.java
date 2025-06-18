package ui;

import board.Board;
import board.MainPathSquare;
import board.FinalPathSquare;
import board.HomeBaseSquare;
import core.Piece;
import core.Player;
import game.Game;
import game.GameState;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Interfaz grafica de Ludo: dibuja el tablero y muestra controles Swing.
 */
public class SwingGameUI extends JFrame {
    private static final int GRID_SIZE = 15;
    private static final int CENTER = GRID_SIZE / 2;

    private final Game game;
    private final JLabel currentPlayerLabel = new JLabel();
    private final JLabel rollResultLabel = new JLabel("Resultado dado: ");
    private final JLabel eventLabel = new JLabel(" ");
    private final JPanel infoPanel = new JPanel();
    private boolean winnerAnnounced = false;
    private final JPanel boardPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));
    private final JButton rollButton = new JButton("Tirar dado");
    private final JButton resignButton = new JButton("Rendirse");

    public SwingGameUI(Game game) {
        super("Ludo");
        this.game = game;
        game.startGame();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER));
        top.add(currentPlayerLabel);
        top.add(rollResultLabel);
        top.add(eventLabel);
        add(top, BorderLayout.NORTH);

        boardPanel.setPreferredSize(new Dimension(600, 600));
        add(boardPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottom.add(rollButton);
        bottom.add(resignButton);
        add(bottom, BorderLayout.SOUTH);

        rollButton.addActionListener(e -> {
            if (game.getState() == GameState.IN_PROGRESS) {
                game.playTurn();
                refreshUI();
            }
        });

        resignButton.addActionListener(e -> {
            if (game.getState() == GameState.IN_PROGRESS) {
                game.getCurrentPlayer().rendirse();
                game.skipTurn();
                refreshUI();
            }
        });

        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Estado de jugadores"));
        add(infoPanel, BorderLayout.WEST);

        refreshUI();
        pack();
        setLocationRelativeTo(null);
    }

    private static final java.util.Map<core.Color, Point[]> HOME_BASE_COORDS = java.util.Map.of(
            core.Color.RED,    new Point[]{ new Point(1,1),  new Point(1,3),  new Point(3,1),  new Point(3,3) },
            core.Color.GREEN,  new Point[]{ new Point(11,1), new Point(13,1), new Point(11,3), new Point(13,3) },
            core.Color.YELLOW, new Point[]{ new Point(11,11),new Point(13,11),new Point(11,13),new Point(13,13) },
            core.Color.BLUE,   new Point[]{ new Point(1,11), new Point(3,11), new Point(1,13), new Point(3,13) }
    );

    private void refreshUI() {
        // si el juego ya terminó y aún no hemos anunciado al ganador:
        if (game.getState() != GameState.IN_PROGRESS && !winnerAnnounced) {
            // buscamos al unico jugador que no se rindió
            Player winner = game.getPlayers().stream()
                    .filter(p -> !p.isRendido())
                    .findFirst()
                    .orElse(null);
            if (winner != null) {
                JOptionPane.showMessageDialog(
                        this,
                        "¡El ganador es " + winner.getName() + " (" + winner.getColor() + ")!",
                        "Fin de la partida",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
            winnerAnnounced = true;
        }

        infoPanel.removeAll();
        for (Player p : game.getPlayers()) {
            // contar fichas en meta
            long finishedCount = p.getPieces().stream().filter(Piece::isFinished).count();

            // panel por jugador
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            // circulo de color
            JLabel colorDot = new JLabel("  ");
            colorDot.setOpaque(true);
            colorDot.setBackground(toAwtColor(p.getColor()));
            colorDot.setPreferredSize(new Dimension(12, 12));
            // texto con nombre y contadores
            JLabel text = new JLabel(p.getName()
                    + " – Fichas en meta: " + finishedCount);
            row.add(colorDot);
            row.add(text);
            infoPanel.add(row);
        }
        infoPanel.revalidate();
        infoPanel.repaint();

        if (game.getState() == GameState.IN_PROGRESS) {
            var p = game.getCurrentPlayer();
            currentPlayerLabel.setText("Turno de: " + p.getName() + " (" + p.getColor() + ")");
            rollResultLabel.setText("Resultado dado: " + game.getLastRoll());
            rollButton.setEnabled(true);
            // leer evento del modelo
            String ev = game.getBoard().fetchLastEvent();
            eventLabel.setText(ev.isEmpty() ? " " : ev);
            resignButton.setEnabled(true);
        } else {
            currentPlayerLabel.setText("Juego terminado");
            rollResultLabel.setText("");
            rollButton.setEnabled(false);
            resignButton.setEnabled(false);
        }

        boardPanel.removeAll();
        JPanel[][] cells = new JPanel[GRID_SIZE][GRID_SIZE];
        Point[] mainCoords = generateMainPath();

        // crear celdas base
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                JPanel cell = new JPanel();
                cell.setBorder(BorderFactory.createLineBorder(java.awt.Color.DARK_GRAY));
                cell.setBackground(getCellColor(row, col));
                // resaltar casilla de entrada para cada color
                int[] entryIndices = {1, 15, 29, 43};
                core.Color[] entryColors = {
                        core.Color.RED, core.Color.GREEN, core.Color.YELLOW, core.Color.BLUE
                };
                for (int k = 0; k < entryIndices.length; k++) {
                    Point ep = mainCoords[entryIndices[k]];
                    if (ep.y == row && ep.x == col) {
                        cell.setBackground(toAwtColor(entryColors[k]));
                        break;
                    }
                }


                // Si es posición de base, forzar fondo blanco para ver fichas
                for (Point[] arr : HOME_BASE_COORDS.values()) {
                    for (Point b : arr) {
                        if (b.x == col && b.y == row) {
                            cell.setBackground(java.awt.Color.WHITE);
                            break;
                        }
                    }
                }

                boardPanel.add(cell);
                cells[row][col] = cell;
            }
        }

        // dibujar piezas en camino principal
        List<MainPathSquare> mainPath = game.getBoard().getMainPath();
        for (int i = 0; i < mainPath.size(); i++) {
            Point coord = mainCoords[i];
            JPanel cell = cells[coord.y][coord.x];
            for (Piece piece : mainPath.get(i).getPieces()) {
                cell.add(createPieceLabel(piece));
            }
        }

        // mostrar piezas en casas
        for (var entry : game.getBoard().getHomeBaseSquares().entrySet()) {
            core.Color color = entry.getKey();
            Point[] coords = HOME_BASE_COORDS.get(color);
            List<Piece> pieces = entry.getValue().getPieces();
            for (int i = 0; i < pieces.size() && i < coords.length; i++) {
                Point c = coords[i];
                cells[c.y][c.x].add(createPieceLabel(pieces.get(i)));
            }
        }

        // mostrar piezas en caminos finales
        for (Map.Entry<core.Color, List<FinalPathSquare>> entry : game.getBoard().getFinalPaths().entrySet()) {
            core.Color color = entry.getKey();
            for (FinalPathSquare square : entry.getValue()) {
                Point coord = getFinalPathCoord(color, square.getPosition());
                JPanel cell = cells[coord.y][coord.x];
                for (Piece piece : square.getPieces()) {
                    cell.add(createPieceLabel(piece));
                }
            }
        }

        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private JLabel createPieceLabel(Piece p) {
        JLabel lbl = new JLabel(String.valueOf(p.getId()));
        lbl.setOpaque(true);
        lbl.setBackground(toAwtColor(p.getColor()));
        lbl.setForeground(java.awt.Color.WHITE);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setPreferredSize(new Dimension(20, 20));
        lbl.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK));
        return lbl;
    }

    private static Point[] generateMainPath() {
        int[] indices = {
                90,91,92,93,94,95,96,81,66,51,36,21,6,7,8,
                23,38,53,68,83,98,99,100,101,102,103,104,
                119,134,133,132,131,130,129,128,143,158,
                173,188,203,218,217,216,201,186,171,156,
                141,126,125,124,123,122,121,120,105
        };
        Point[] path = new Point[indices.length];
        for (int i = 0; i < indices.length; i++) {
            int idx = indices[i];
            path[i] = new Point(idx % GRID_SIZE, idx / GRID_SIZE);
        }
        return path;
    }

    private Point getHomeBaseCoord(core.Color color) {
        return switch (color) {
            case RED -> new Point(1, 1);
            case GREEN -> new Point(GRID_SIZE - 2, 1);
            case YELLOW -> new Point(GRID_SIZE - 2, GRID_SIZE - 2);
            case BLUE -> new Point(1, GRID_SIZE - 2);
        };
    }

    private Point getFinalPathCoord(core.Color color, int position) {
        return switch (color) {
            case RED -> new Point(1 + position, CENTER);
            case GREEN -> new Point(CENTER, 1 + position);
            case YELLOW -> new Point(GRID_SIZE - 2 - position, CENTER);
            case BLUE -> new Point(CENTER, GRID_SIZE - 2 - position);
        };
    }

    private java.awt.Color toAwtColor(core.Color c) {
        return switch (c) {
            case RED -> java.awt.Color.RED;
            case GREEN -> java.awt.Color.GREEN;
            case BLUE -> java.awt.Color.BLUE;
            case YELLOW -> java.awt.Color.YELLOW;
        };
    }

    private java.awt.Color getCellColor(int row, int col) {
        int n = GRID_SIZE;
        if (row < 6 && col < 6)              return java.awt.Color.RED;
        if (row < 6 && col >= n - 6)         return java.awt.Color.GREEN;
        if (row >= n - 6 && col >= n - 6)    return java.awt.Color.YELLOW;
        if (row >= n - 6 && col < 6)         return java.awt.Color.BLUE;
        if (row == CENTER && col >= 1 && col <= 5)         return java.awt.Color.RED;
        if (col == CENTER && row >= 1 && row <= 5)         return java.awt.Color.GREEN;
        if (row == CENTER && col >= 9 && col <= 13)        return java.awt.Color.YELLOW;
        if (col == CENTER && row >= 9 && row <= 13)        return java.awt.Color.BLUE;
        if (row >= CENTER - 1 && row <= CENTER + 1 && col >= CENTER - 1 && col <= CENTER + 1) {
            return java.awt.Color.WHITE;
        }
        return java.awt.Color.WHITE;
    }
}
