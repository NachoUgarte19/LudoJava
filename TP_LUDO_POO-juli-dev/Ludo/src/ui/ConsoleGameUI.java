package ui;

import board.AbstractSquare;
import board.FinalPathSquare;
import board.HomeBaseSquare;
import board.MainPathSquare;
import core.Piece;
import core.Player;
import core.Color;
import game.Game;
import game.GameState;
import ui.InteractiveGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Interfaz de consola para jugar Ludo con configuración fija y bucle de juego.
 */
public class ConsoleGameUI {
    private final Game game;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleGameUI(Game game) {
        this.game = game;
    }

    /**
     * Inicia el bucle de juego en consola.
     */
    public void start() {
        game.startGame();
        while (game.getState() == GameState.IN_PROGRESS) {
            Player current = game.getCurrentPlayer();
            System.out.println("\n--- Turno de " + current.getName() + " (" + current.getColor() + ") ---");
            displayBoard();
            System.out.print("Presiona 't' para tirar dado, 'r' para rendirte: ");

            String input = scanner.nextLine().trim().toLowerCase();
            if ("t".equals(input)) {
                game.playTurn();
            } else if ("r".equals(input)) {
                current.rendirse();
                System.out.println(current.getName() + " se rindió.");
                game.skipTurn();
            } else {
                System.out.println("Opción no válida. Intenta de nuevo.");
            }
        }
        System.out.println("\n=== JUEGO TERMINADO ===");
        if (game.getState() == GameState.FINISHED) {
            System.out.println("¡Gracias por jugar!");
        }
    }

    /**
     * Muestra el tablero en consola: casillas principales, finales y base.
     */
    private void displayBoard() {
        System.out.println("Tablero:");
        for (Player p : game.getPlayers()) {
            System.out.printf("%s (%s): ", p.getName(), p.getColor());
            for (Piece piece : p.getPieces()) {
                AbstractSquare sq = piece.getCurrentSquare();
                String position;
                if (sq == null || sq instanceof HomeBaseSquare) {
                    position = "B"; // Base
                } else if (sq instanceof MainPathSquare) {
                    position = String.valueOf(((MainPathSquare) sq).getPosition());
                } else if (sq instanceof FinalPathSquare) {
                    position = "F" + ((FinalPathSquare) sq).getPosition();
                } else {
                    position = "?";
                }
                String label = String.format("%d(%s)", piece.getId(), position);
                System.out.print(label + " ");
            }
            System.out.println();
        }
    }

    /**
     * Configuracion previa y arranque del juego en consola
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("--- ¡Bienvenido a Ludo! (Consola) ---");

        int numPlayers;
        do {
            System.out.print("Número de jugadores (2-" + Color.values().length + "): ");
            while (!scanner.hasNextInt()) scanner.next();
            numPlayers = scanner.nextInt();
            scanner.nextLine();
        } while (numPlayers < 2 || numPlayers > Color.values().length);

        List<Player> players = new ArrayList<>();
        Color[] colors = Color.values();
        for (int i = 0; i < numPlayers; i++) {
            System.out.print("Nombre Jugador " + (i+1) + ": ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) name = "Jugador" + (i+1);
            Player p = new Player(name, colors[i]);
            p.initializePieces(4);
            players.add(p);
        }

        Game game = new InteractiveGame(players, 56);

        // inicio
        new ConsoleGameUI(game).start();
    }
}
