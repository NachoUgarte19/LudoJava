package game;

import board.AbstractSquare;
import board.Board;
import board.MainPathSquare;
import core.Piece;
import core.Player;
import utils.Dice;

import java.util.List;

public abstract class Game {
    private static final String GAME_STARTED_MESSAGE = "El juego ha comenzado.";

    private final Board board;
    private final Dice dice = new Dice();
    private final Turn turn;
    private final List<Player> players;

    private Player currentPlayer;
    private GameState state = GameState.NOT_STARTED;

    public Game(List<Player> players, int mainPathSize) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("La lista de jugadores no puede ser nula o vacía.");
        }
        this.players = players;
        this.board = new Board(players, mainPathSize);
        this.turn = new Turn(players);

    }

    /**
     * Devuelve el tablero para uso en UIs.
     */
    public Board getBoard() {
        return board;
    }

    public void startGame() {
        if (state == GameState.NOT_STARTED) {
            state = GameState.IN_PROGRESS;
            currentPlayer = turn.nextTurn();
            System.out.println(GAME_STARTED_MESSAGE);
            if (currentPlayer == null) { // Seguridad si la lista de turnos está vacía
                System.err.println("Error: No se pudo obtener el primer jugador.");
                state = GameState.FINISHED;
            }
        } else {
            System.out.println("El juego ya ha comenzado o ha terminado.");
        }
    }

    public void playTurn() {
        if (state != GameState.IN_PROGRESS) {
            // System.out.println("El juego no está en progreso."); // Puede ser muy verboso
            return;
        }
        if (currentPlayer == null) {
            System.err.println("Error: Jugador actual es nulo en playTurn.");
            endGameIfNoActivePlayers();
            return;
        }
        if (currentPlayer.hasWon() || currentPlayer.isRendido()) {
            System.out.println("Saltando turno de " + currentPlayer.getName() + " (ya ganó o se rindió).");
            advanceToNextValidPlayer();
            return;
        }

        System.out.println("\n--- Turno de " + currentPlayer.getName() + " (" + currentPlayer.getColor() + ") ---");
        int roll = dice.roll();
        System.out.println(currentPlayer.getName() + " sacó un " + roll + "!");

        List<Piece> piecesNotFinished = currentPlayer.getPieces().stream()
                .filter(p -> !p.isFinished())
                .toList();

        if (piecesNotFinished.isEmpty()) {
            System.out.println(currentPlayer.getName() + " no tiene más fichas para mover (todas terminaron).");
            // Aunque hasWon() ya lo cubre, esto es una confirmación.
            if (!currentPlayer.hasWon()) { // Debería haber ganado si todas sus fichas terminaron
                // Esto podría indicar un problema si hasWon() no se actualiza correctamente
                System.err.println("Advertencia: Todas las fichas de " + currentPlayer.getName() + " terminaron, pero hasWon() es falso.");
            }
            advanceToNextValidPlayer();
            return;
        }

        boolean allAvailableInBase = piecesNotFinished.stream().allMatch(Piece::isInBase);

        if (allAvailableInBase) {
            if (roll == 6) {
                if (!piecesNotFinished.isEmpty()) {
                    Piece pieceFromBase = piecesNotFinished.getFirst();
                    MainPathSquare entrySquare = board.getBoardEntrySquareForColor(currentPlayer.getColor());
                    board.placePieceOnBoard(pieceFromBase, entrySquare);
                    System.out.println(currentPlayer.getName() + " sacó la ficha " + pieceFromBase.getId() + " al tablero (casilla " +
                            (entrySquare != null ? entrySquare.getPosition() : "N/A") + ").");
                } else {
                    // Este caso no debería ocurrir si piecesNotFinished no estaba vacía
                    System.out.println(currentPlayer.getName() + " no tiene fichas en base para sacar.");
                }
            } else {
                System.out.println(currentPlayer.getName() + " no puede mover. Necesita un 6 para sacar una ficha de base (y todas están en base).");
            }
        } else {
            List<Piece> piecesOnBoardAndNotFinished = piecesNotFinished.stream()
                    .filter(p -> !p.isInBase())
                    .toList();

            if (!piecesOnBoardAndNotFinished.isEmpty()) {
                // Simplificación: tomar la primera ficha disponible en el tablero.
                // En un juego real, el jugador debería poder elegir.
                Piece pieceToMove = piecesOnBoardAndNotFinished.getFirst();

                String originalSquareStr = "ERROR: Casilla original nula";
                if (pieceToMove.getCurrentSquare() != null) {
                    originalSquareStr = pieceToMove.getCurrentSquare().getClass().getSimpleName() + " " + pieceToMove.getCurrentSquare().getPosition();
                }
                System.out.println(currentPlayer.getName() + " intenta mover la ficha " + pieceToMove.getId() + " desde " + originalSquareStr);

                AbstractSquare originalSquareObject = pieceToMove.getCurrentSquare(); // Guardar la referencia
                board.move(pieceToMove, roll);

                // La comparación que mencionaste:
                if (pieceToMove.getCurrentSquare() != originalSquareObject) {
                    System.out.println(currentPlayer.getName() + " movió la ficha " + pieceToMove.getId() +
                            " a " + (pieceToMove.getCurrentSquare() != null ?
                            (pieceToMove.getCurrentSquare().getClass().getSimpleName() + " " + pieceToMove.getCurrentSquare().getPosition()) : "Base/Fuera del Tablero") + ".");
                } else {
                    System.out.println(currentPlayer.getName() + ", la ficha " + pieceToMove.getId() + " no cambió de casilla (misma casilla o movimiento no permitido).");
                }

                if (pieceToMove.isFinished()) {
                    System.out.println("¡Felicidades! La ficha " + pieceToMove.getId() + " de " + currentPlayer.getName() + " llegó a la meta 🎉");
                }

            } else if (roll == 6) { // No hay fichas en tablero, pero hay en base y sacó 6
                Piece pieceFromBase = piecesNotFinished.stream().filter(Piece::isInBase).findFirst().orElse(null);
                if (pieceFromBase != null) {
                    MainPathSquare entrySquare = board.getBoardEntrySquareForColor(currentPlayer.getColor());
                    board.placePieceOnBoard(pieceFromBase, entrySquare);
                    System.out.println(currentPlayer.getName() + " sacó la ficha " + pieceFromBase.getId() + " al tablero (casilla " +
                            (entrySquare != null ? entrySquare.getPosition() : "N/A") + ").");
                } else {
                    System.out.println(currentPlayer.getName() + " no tiene fichas en base para sacar (aunque no hay fichas en tablero).");
                }
            } else {
                System.out.println(currentPlayer.getName() + " no tiene fichas en el tablero para mover y no sacó un 6 (o no hay fichas válidas en base).");
            }
        }

        if (currentPlayer.hasWon()) {
            System.out.println("🎉 ¡" + currentPlayer.getName() + " ha ganado el juego! 🎉");
            // No cambiar state aquí, dejar que advanceToNextValidPlayer y endGameIfNoActivePlayers lo manejen
            // para permitir que otros terminen en la misma "ronda" si es necesario.
        }

        // Avanzar al siguiente jugador o terminar el juego
        if (state == GameState.IN_PROGRESS) { // Solo avanzar si el juego no ha sido marcado como terminado por el jugador actual
            advanceToNextValidPlayer();
        }
        endGameIfNoActivePlayers(); // Chequeo final
    }

    private void advanceToNextValidPlayer() {
        if (players.stream().allMatch(p -> p.hasWon() || p.isRendido())) {
            state = GameState.FINISHED;
            return;
        }

        Player nextPlayerCandidate;
        int attempts = 0;
        int maxAttempts = players.size() * 2;

        do {
            if (players.isEmpty()) { // Seguridad adicional
                state = GameState.FINISHED; return;
            }
            nextPlayerCandidate = turn.nextTurn();
            attempts++;
            if (attempts > maxAttempts && state != GameState.FINISHED) {
                System.err.println("Error: Bucle en advanceToNextValidPlayer. Forzando fin del juego.");
                state = GameState.FINISHED;
                return;
            }
            // Si después de varios intentos, volvemos al jugador actual y él es el único activo, el juego debería terminar.
            if (nextPlayerCandidate == currentPlayer && players.stream().filter(p-> !p.hasWon() && !p.isRendido()).count() == 1 && !currentPlayer.hasWon() && !currentPlayer.isRendido()){
                state = GameState.FINISHED; // El jugador actual es el único que queda
                return;
            }

        } while ((nextPlayerCandidate.hasWon() || nextPlayerCandidate.isRendido()) && state != GameState.FINISHED);

        if (state != GameState.FINISHED) {
            currentPlayer = nextPlayerCandidate;
        }
    }

    private void endGameIfNoActivePlayers() {
        if (state == GameState.IN_PROGRESS) {
            long activeAndNotWonCount = players.stream().filter(p -> !p.isRendido() && !p.hasWon()).count();

            if (activeAndNotWonCount == 0 && !players.isEmpty()) {
                // Todos han ganado o se han rendido (y había jugadores para empezar)
                System.out.println("Todos los jugadores han terminado o se han rendido. El juego ha finalizado.");
                state = GameState.FINISHED;
            } else if (activeAndNotWonCount == 1 && players.size() > 1) {
                // Solo queda un jugador que no ha ganado ni se ha rendido, y había más de un jugador.
                Player lastPlayer = players.stream().filter(p -> !p.isRendido() && !p.hasWon()).findFirst().orElse(null);
                if (lastPlayer != null) {
                    System.out.println("Solo queda " + lastPlayer.getName() + " en juego. ¡" + lastPlayer.getName() + " es el ganador por defecto!");
                }
                state = GameState.FINISHED;
            }
        }
    }


    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public GameState getState() {
        return state;
    }

    public void skipTurn() {
        if (state == GameState.IN_PROGRESS && currentPlayer != null) {
            System.out.println("El jugador " + currentPlayer.getName() + " salta su turno.");
            advanceToNextValidPlayer();
            endGameIfNoActivePlayers();
        }
    }

    protected abstract Piece choosePieceFromBase(List<Piece> piecesInBase);

    protected abstract Piece choosePieceOnSix(List<Piece> piecesInBase, List<Piece> piecesOnBoard);

    protected abstract Piece choosePieceToMove(List<Piece> piecesOnBoard);
}