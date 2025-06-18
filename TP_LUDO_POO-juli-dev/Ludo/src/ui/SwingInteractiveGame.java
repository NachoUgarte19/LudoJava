package ui;

import core.Piece;
import core.Player;
import game.Game;

import javax.swing.JOptionPane;
import java.util.List;

/**
 * Lógica de juego interactiva para Swing: extiende Game y pide fichas vía JOptionPane.
 */
public class SwingInteractiveGame extends Game {
    public SwingInteractiveGame(List<Player> players, int mainPathSize) {
        super(players, mainPathSize);
    }

    @Override
    protected Piece choosePieceFromBase(List<Piece> piecesInBase) {
        // Sale un 6 y solo hay fichas en base
        return askForPiece("¡Sacaste un 6! Elige ficha de base:", piecesInBase);
    }

    @Override
    protected Piece choosePieceOnSix(List<Piece> piecesInBase, List<Piece> piecesOnBoard) {
        // Sale un 6 y hay opción entre base o tablero
        String[] opciones = {"Sacar ficha de base", "Mover ficha en tablero"};
        int sel = JOptionPane.showOptionDialog(
                null,
                "¡Sacaste un 6! ¿Qué quieres hacer?",
                "Acción al 6",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]
        );
        if (sel == 0 && !piecesInBase.isEmpty()) {
            return askForPiece("Elige ficha de base:", piecesInBase);
        } else {
            return askForPiece("Elige ficha en tablero:", piecesOnBoard);
        }
    }

    @Override
    protected Piece choosePieceToMove(List<Piece> piecesOnBoard) {
        // Varias fichas en tablero, pide cuál mover
        return askForPiece("¿Cuál ficha quieres mover?", piecesOnBoard);
    }

    /**
     * Muestra un cuadro de diálogo con la lista de IDs y devuelve la pieza seleccionada.
     */
    private Piece askForPiece(String mensaje, List<Piece> lista) {
        String[] items = lista.stream()
                .map(p -> "Ficha " + p.getId())
                .toArray(String[]::new);
        String elegido = null;
        // Repetir hasta que el usuario elija algo
        while (elegido == null) {
            elegido = (String) JOptionPane.showInputDialog(
                    null,
                    mensaje,
                    "Selecciona ficha",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    items,
                    items[0]
            );
            if (elegido == null) {
                // El usuario cerró el diálogo o pulsó Cancelar: mostramos un mensaje y volvemos a pedir
                JOptionPane.showMessageDialog(
                        null,
                        "Debes seleccionar una ficha para continuar.",
                        "Selección obligatoria",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        }
        int id = Integer.parseInt(elegido.split(" ")[1]);
        return lista.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElseThrow();
    }
}
