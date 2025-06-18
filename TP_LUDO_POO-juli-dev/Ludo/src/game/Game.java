package game;

import board.AbstractSquare;
import board.Board;
import board.MainPathSquare;
import board.FinalPathSquare;
import board.HomeBaseSquare;
import core.Piece;
import core.Player;
import utils.Dice;

import java.util.List;
import java.util.stream.Collectors;

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

    /** Devuelve el tablero para uso en UIs. */
    public Board getBoard() { return board; }

    public void startGame() {
        if (state == GameState.NOT_STARTED) {
            state = GameState.IN_PROGRESS;
            currentPlayer = turn.nextTurn();
            System.out.println(GAME_STARTED_MESSAGE);
            if (currentPlayer == null) {
                System.err.println("Error: No se pudo obtener el primer jugador.");
                state = GameState.FINISHED;
            }
        }
    }

    /** Ejecuta un turno completo: lanzamiento, elección y movimiento de ficha. */
    public void playTurn() {
        if (state != GameState.IN_PROGRESS) return;
        if (currentPlayer == null) return;

        int roll = dice.roll();
        System.out.println("\n--- Turno de " + currentPlayer.getName() + " (" + currentPlayer.getColor() + ") ---");
        System.out.println(currentPlayer.getName() + " sacó un " + roll + "!");

        // Filtrar piezas no terminadas
        List<Piece> notFinished = currentPlayer.getPieces().stream()
                .filter(p -> !p.isFinished())
                .collect(Collectors.toList());
        // Lista en base y en tablero
        List<Piece> inBase = notFinished.stream().filter(Piece::isInBase).collect(Collectors.toList());
        List<Piece> onBoard = notFinished.stream().filter(p -> !p.isInBase()).collect(Collectors.toList());

        Piece chosen = null;
        AbstractSquare origin = null;

        if (!inBase.isEmpty() && onBoard.isEmpty()) {
            // Todas en base: solo se puede sacar con 6
            if (roll == 6) {
                chosen = choosePieceFromBase(inBase);
                MainPathSquare entry = board.getBoardEntrySquareForColor(currentPlayer.getColor());
                board.placePieceOnBoard(chosen, entry);
            } else {
                System.out.println(currentPlayer.getName() + " necesita un 6 para sacar ficha.");
            }
        } else if (!inBase.isEmpty() && roll == 6) {
            // Ambas opciones: base o mover en tablero
            chosen = choosePieceOnSix(inBase, onBoard);
            if (chosen.isInBase()) {
                MainPathSquare entry = board.getBoardEntrySquareForColor(currentPlayer.getColor());
                board.placePieceOnBoard(chosen, entry);
            } else {
                origin = chosen.getCurrentSquare();
                board.move(chosen, roll);
            }
        } else if (!onBoard.isEmpty()) {
            // Mover en tablero
            chosen = choosePieceToMove(onBoard);
            origin = chosen.getCurrentSquare();
            board.move(chosen, roll);
        } else {
            System.out.println(currentPlayer.getName() + " no tiene movimientos disponibles.");
        }

        // Mensajes post-movimiento
        if (chosen != null) {
            if (origin != null) {
                System.out.println(currentPlayer.getName() + " movió ficha " + chosen.getId() + " de "
                        + origin.getPosition() + " a "
                        + (chosen.getCurrentSquare() != null ? chosen.getCurrentSquare().getPosition() : "base") + ".");
            } else {
                System.out.println(currentPlayer.getName() + " sacó ficha " + chosen.getId() + " al tablero.");
            }
            if (chosen.isFinished()) {
                System.out.println("¡Ficha " + chosen.getId() + " llegó a la meta!");
            }
        }

        // Avanzar turno y fin de juego
        advanceToNextValidPlayer();
        endGameIfNoActivePlayers();
    }

    // Métodos privados para turno
    private void advanceToNextValidPlayer() {
        if (players.stream().allMatch(p -> p.hasWon() || p.isRendido())) {
            state = GameState.FINISHED; return;
        }
        Player next;
        do { next = turn.nextTurn(); } while ((next.hasWon() || next.isRendido()) && state == GameState.IN_PROGRESS);
        currentPlayer = next;
    }

    private void endGameIfNoActivePlayers() {
        long active = players.stream().filter(p -> !p.hasWon() && !p.isRendido()).count();
        if (active == 0) state = GameState.FINISHED;
        else if (active == 1) {
            Player last = players.stream().filter(p -> !p.hasWon() && !p.isRendido()).findFirst().orElse(null);
            System.out.println("Jugador restante: " + (last != null ? last.getName() : ""));
            state = GameState.FINISHED;
        }
    }

    public Player getCurrentPlayer() { return currentPlayer; }
    public GameState getState() { return state; }
    public void skipTurn() { advanceToNextValidPlayer(); endGameIfNoActivePlayers(); }

    // Métodos abstractos para elección de ficha
    protected abstract Piece choosePieceFromBase(List<Piece> piecesInBase);
    protected abstract Piece choosePieceOnSix(List<Piece> piecesInBase, List<Piece> piecesOnBoard);
    protected abstract Piece choosePieceToMove(List<Piece> piecesOnBoard);
}
