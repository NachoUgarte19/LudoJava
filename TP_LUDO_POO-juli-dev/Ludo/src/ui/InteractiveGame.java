package ui;

import core.Piece;
import core.Player;
import game.Game;

import java.util.List;
import java.util.Scanner;

/**
 * Logica de juego interactiva para consola: extiende Game y pide fichas vía Scanner.
 */
public class InteractiveGame extends Game {
    private final Scanner scanner = new Scanner(System.in);

    public InteractiveGame(List<Player> players, int mainPathSize) {
        super(players, mainPathSize);
    }

    @Override
    protected Piece choosePieceFromBase(List<Piece> piecesInBase) {
        System.out.println("\n¡Sacaste un 6! Elige qué ficha sacar de la base:");
        return promptForPiece(piecesInBase);
    }

    @Override
    protected Piece choosePieceOnSix(List<Piece> piecesInBase, List<Piece> piecesOnBoard) {
        System.out.println("\n¡Sacaste un 6! ¿Qué quieres hacer?");
        System.out.println("  1) Sacar ficha de base");
        System.out.println("  2) Mover ficha en tablero");
        int option;
        do {
            System.out.print("Opción (1/2): ");
            while (!scanner.hasNextInt()) scanner.next();
            option = scanner.nextInt();
            scanner.nextLine();
        } while (option != 1 && option != 2);

        if (option == 1 && !piecesInBase.isEmpty()) {
            return promptForPiece(piecesInBase);
        } else {
            return promptForPiece(piecesOnBoard);
        }
    }

    @Override
    protected Piece choosePieceToMove(List<Piece> piecesOnBoard) {
        System.out.println("\nTienes varias fichas en el tablero. Elige cuál mover:");
        return promptForPiece(piecesOnBoard);
    }

    // elegir ficha por id
    private Piece promptForPiece(List<Piece> list) {
        System.out.println("Fichas disponibles:");
        for (Piece p : list) {
            System.out.println(" - ID " + p.getId());
        }
        Piece selected = null;
        do {
            System.out.print("Ingresa ID de ficha: ");
            while (!scanner.hasNextInt()) scanner.next();
            int id = scanner.nextInt();
            scanner.nextLine();
            for (Piece p : list) {
                if (p.getId() == id) {
                    selected = p;
                    break;
                }
            }
            if (selected == null) {
                System.out.println("ID inválido, intenta de nuevo.");
            }
        } while (selected == null);
        return selected;
    }
}
