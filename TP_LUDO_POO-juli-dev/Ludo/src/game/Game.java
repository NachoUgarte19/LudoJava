package game;

import board.AbstractSquare;
import board.Board;
import board.MainPathSquare;
import board.FinalPathSquare;
import board.HomeBaseSquare;
import core.Piece;
import core.Player;
import utils.Dice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Game {
    private static final String GAME_STARTED_MESSAGE = "El juego ha comenzado.";

    private final Board board;
    private final Dice dice = new Dice();
    private final Turn turn;
    private final List<Player> players;
    private int lastRoll = 0;

    private Player currentPlayer;
    private GameState state = GameState.NOT_STARTED;

    public Game(List<Player> players, int mainPathSize) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("La lista de jugadores no puede ser nula o vacía.");
        }
        this.players = players;
        this.board = new Board(players, mainPathSize);
        // Colocar todas las fichas en su HomeBaseSquare tras inicializar el tablero
        for (Player p : players) {
            HomeBaseSquare base = board.getHomeBaseSquares().get(p.getColor());
            for (Piece piece : p.getPieces()) {
                base.addPiece(piece);
                piece.moveTo(base);
            }
        }
        this.turn = new Turn(players);
    }

    public int getLastRoll() {
        return lastRoll;
    }

    /** Devuelve el tablero para uso en UIs. */
    public Board getBoard() {
        return board;
    }

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
        if (state != GameState.IN_PROGRESS || currentPlayer == null) {
            return;
        }

        lastRoll = dice.roll();
        int roll = lastRoll;
        System.out.println("\n--- Turno de " + currentPlayer.getName() + " (" + currentPlayer.getColor() + ") ---");
        System.out.println(currentPlayer.getName() + " sacó un " + roll + "!");

        // Filtrar piezas no terminadas
        List<Piece> notFinished = currentPlayer.getPieces().stream()
                .filter(p -> !p.isFinished())
                .collect(Collectors.toList());
        // Obtener las piezas que están en base según el board
        List<Piece> inBase = new ArrayList<>(board.getHomeBaseSquares().get(currentPlayer.getColor()).getPieces());
        // Las demás están en el tablero
        List<Piece> onBoard = notFinished.stream()
                .filter(p -> !inBase.contains(p))
                .collect(Collectors.toList());

        // Caso A: todas en base
        if (onBoard.isEmpty()) {
            if (roll == 6) {
                Piece chosen = choosePieceFromBase(inBase);
                HomeBaseSquare base = board.getHomeBaseSquares().get(currentPlayer.getColor());
                base.removePiece(chosen);
                chosen.moveTo(null);
                MainPathSquare entry = board.getBoardEntrySquareForColor(currentPlayer.getColor());
                board.placePieceOnBoard(chosen, entry);
                System.out.println(currentPlayer.getName() + " sacó ficha " + chosen.getId() + " al tablero.");
            } else {
                System.out.println(currentPlayer.getName() + " necesita un 6 para sacar ficha.");
                advanceToNextValidPlayer();
                endGameIfNoActivePlayers();
            }
            return;
        }

        // Caso B: hay fichas en tablero
        Piece chosen;
        AbstractSquare origin;

        // Si salió 6 y aún hay fichas en base, siempre sacar de base primero
        if (roll == 6 && !inBase.isEmpty()) {
            chosen = choosePieceFromBase(inBase);
            HomeBaseSquare base = board.getHomeBaseSquares().get(currentPlayer.getColor());
            base.removePiece(chosen);
            chosen.moveTo(null);
            MainPathSquare entry = board.getBoardEntrySquareForColor(currentPlayer.getColor());
            board.placePieceOnBoard(chosen, entry);
            System.out.println(currentPlayer.getName() + " sacó ficha " + chosen.getId() + " al tablero.");
            advanceToNextValidPlayer();
            endGameIfNoActivePlayers();
            return;
        }
        // Si no entra en extracción, mover ficha en tablero
        chosen = choosePieceToMove(onBoard);
        origin = chosen.getCurrentSquare();
        board.move(chosen, roll);
        System.out.println(currentPlayer.getName() + " movió ficha " + chosen.getId()
                + " de " + origin.getPosition()
                + " a " + (chosen.getCurrentSquare() != null
                ? chosen.getCurrentSquare().getPosition()
                : "base") + ".");
        if (chosen.isFinished()) {
            System.out.println("¡Ficha " + chosen.getId() + " llegó a la meta!");
        }

        // Avanzar turno y fin de juego
        advanceToNextValidPlayer();
        endGameIfNoActivePlayers();
    }

    // Métodos privados para turno
    private void advanceToNextValidPlayer() {
        if (players.stream().allMatch(p -> p.hasWon() || p.isRendido())) {
            state = GameState.FINISHED;
            return;
        }
        Player next;
        do {
            next = turn.nextTurn();
        } while ((next.hasWon() || next.isRendido()) && state == GameState.IN_PROGRESS);
        currentPlayer = next;
    }

    private void endGameIfNoActivePlayers() {
        long active = players.stream().filter(p -> !p.hasWon() && !p.isRendido()).count();
        if (active == 0) {
            state = GameState.FINISHED;
        } else if (active == 1) {
            Player last = players.stream().filter(p -> !p.hasWon() && !p.isRendido()).findFirst().orElse(null);
            System.out.println("Jugador restante: " + (last != null ? last.getName() : ""));
            state = GameState.FINISHED;
        }
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public GameState getState() {
        return state;
    }

    public void skipTurn() {
        advanceToNextValidPlayer();
        endGameIfNoActivePlayers();
    }

    // Métodos abstractos para elección de ficha
    protected abstract Piece choosePieceFromBase(List<Piece> piecesInBase);
    protected abstract Piece choosePieceOnSix(List<Piece> piecesInBase, List<Piece> piecesOnBoard);
    protected abstract Piece choosePieceToMove(List<Piece> piecesOnBoard);

    /** Permite consultar la lista de jugadores desde la UI */
    public List<Player> getPlayers() {
        return players;
    }


}
