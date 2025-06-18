package board;

import core.Color;
import core.Piece;
import core.Player;

import java.util.*;

public class Board {
    public static final int FINAL_PATH_LENGTH = 6;

    private final int mainPathSize;
    private final int lastMainPathIndex;
    private String lastEventMessage = "";

    private final List<MainPathSquare> mainPath = new ArrayList<>();
    private final Map<Color, HomeBaseSquare> homeBaseSquares = new HashMap<>();
    private final Map<Color, List<FinalPathSquare>> finalPaths = new HashMap<>();

    public Board(List<Player> players, int mainPathSize) {
        if (mainPathSize <= 0) {
            throw new IllegalArgumentException("mainPathSize debe ser un número positivo.");
        }
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("La lista de jugadores no puede ser nula o vacía.");
        }

        this.mainPathSize = mainPathSize;
        this.lastMainPathIndex = mainPathSize - 1;

        Set<Integer> safeIndices = Set.of(1, 15, 29, 43);

        for (int i = 0; i < this.mainPathSize; i++) {
            boolean isSpecial = false;
            if (!players.isEmpty() && (this.mainPathSize / players.size() > 0)) { // Evitar división por cero
                if (i % (this.mainPathSize / players.size()) == 0) {
                    isSpecial = true;
                }
            }
            if (safeIndices.contains(i)) {
                isSpecial = true;
            }
            mainPath.add(new MainPathSquare(i, isSpecial));
        }

        configureHomeBaseSquares(players);
        configureFinalPaths(players);
    }

    // Añadir getters para la UI:
    /**
     * Devuelve la lista de casillas del camino principal.
     */
    public List<MainPathSquare> getMainPath() {
        return mainPath;
    }

    /**
     * Devuelve el mapa de originales de casillas de base por color.
     */
    public Map<Color, HomeBaseSquare> getHomeBaseSquares() {
        return homeBaseSquares;
    }

    /**
     * Devuelve los caminos finales por color.
     */
    public Map<Color, List<FinalPathSquare>> getFinalPaths() {
        return finalPaths;
    }

    private void configureHomeBaseSquares(List<Player> players) {
        for (Player player : players) {
            homeBaseSquares.put(player.getColor(), new HomeBaseSquare(player.getColor()));
        }
    }

    private void configureFinalPaths(List<Player> players) {
        for (Player player : players) {
            Color color = player.getColor();
            List<FinalPathSquare> playerSpecificFinalPath = new ArrayList<>();
            for (int i = 0; i < FINAL_PATH_LENGTH; i++) {
                boolean isTerminalSquare = (i == FINAL_PATH_LENGTH - 1);
                playerSpecificFinalPath.add(new FinalPathSquare(color, i, isTerminalSquare));
            }
            finalPaths.put(color, playerSpecificFinalPath);
        }
    }

    public void placePieceOnBoard(Piece piece, MainPathSquare targetSquare) {
        // Si la ficha estaba en alguna casilla (p. e. base), la quitamos de allí:
        if (piece.getCurrentSquare() != null) {
            piece.getCurrentSquare().removePiece(piece);
        }
        // Ahora la colocamos en el tablero:
        handleLanding(piece, targetSquare, true);
    }

    public void move(Piece piece, int roll) {
        AbstractSquare currentSquare = piece.getCurrentSquare();
        if (currentSquare == null) {
            System.err.println("Error: Board.move() fue llamada con una ficha que está en base.");
            return;
        }
        currentSquare.removePiece(piece);

        if (currentSquare instanceof MainPathSquare) {
            moveOnMainPath(piece, (MainPathSquare) currentSquare, roll);
        } else if (currentSquare instanceof FinalPathSquare) {
            moveOnFinalPath(piece, (FinalPathSquare) currentSquare, roll);
        } else if (currentSquare instanceof HomeBaseSquare) {
            System.err.println("Error: Board.move() fue llamada con una ficha en HomeBaseSquare.");
            currentSquare.addPiece(piece);
        } else {
            System.err.println("Error: Ficha en un tipo de casilla desconocido: " + currentSquare.getClass().getName());
            currentSquare.addPiece(piece);
        }
    }

    private void moveOnMainPath(Piece piece, MainPathSquare current, int roll) {
        int entry = ENTRY_POS.get(piece.getColor());
        int pathSize = mainPathSize;   // 56 en tu caso

        // posición actual en [0..pathSize-1]
        int pos = current.getPosition();

        // calculamos cuánto hemos avanzado desde la entrada, en modo circular:
        int rel = (pos - entry + pathSize) % pathSize;
        int relNext = rel + roll;

        if (relNext >= pathSize) {
            // ¡completaste la vuelta! pasas al primer FinalPathSquare
            FinalPathSquare fps = finalPaths.get(piece.getColor()).get(0);
            handleLanding(piece, fps, false);
        } else {
            // aún no diste la vuelta: nueva posición circular
            int newPos = (entry + relNext) % pathSize;
            MainPathSquare target = mainPath.get(newPos);
            handleLanding(piece, target, false);
        }
    }


    private void moveOnFinalPath(Piece piece, FinalPathSquare current, int roll) {
        List<FinalPathSquare> fp = finalPaths.get(piece.getColor());
        int pos = current.getPosition();
        int next = pos + roll;

        if (next >= 0 && next < FINAL_PATH_LENGTH - 1) {
            // Avanza normalmente dentro del tramo
            handleLanding(piece, fp.get(next), false);
        } else if (next == FINAL_PATH_LENGTH - 1) {
            // Llegada exacta a la última casilla (meta)
            handleLanding(piece, fp.get(FINAL_PATH_LENGTH - 1), false);
        } else {
            // Roll no válido para avanzar: permanece en la misma casilla
            System.out.println("Tiro no exacto en FinalPath (" + roll + "). Ficha "
                    + piece.getId() + " se queda en pos " + pos + ".");
            handleLanding(piece, current, false);
        }
    }


    private void handleLanding(Piece movingPiece, AbstractSquare targetSquare, boolean isBoardEntryFromBase) {
        if (targetSquare instanceof MainPathSquare && !isBoardEntryFromBase) {
            MainPathSquare mpTarget = (MainPathSquare) targetSquare;
            if (!mpTarget.isEmpty() && !mpTarget.isSpecial()) {
                List<Piece> piecesOnTargetCopy = new ArrayList<>(targetSquare.getPieces());
                for (Piece existingPiece : piecesOnTargetCopy) {
                    if (existingPiece != movingPiece && existingPiece.getColor() != movingPiece.getColor()) {
                        // Construyo el mensaje y lo guardo en el modelo
                        lastEventMessage = movingPiece.getColor() + " capturó ficha " + existingPiece.getId() +
                                " de color " + existingPiece.getColor();
                        // 1) Retirar de la casilla:
                        targetSquare.removePiece(existingPiece);

                        // 2) Añadir a la base de su color:
                        HomeBaseSquare home = homeBaseSquares.get(existingPiece.getColor());
                        home.addPiece(existingPiece);

                        // 3) Decirle a la pieza dónde está ahora:
                        existingPiece.moveTo(home);
                    }
                }
            }
        }
        targetSquare.addPiece(movingPiece);
        movingPiece.moveTo(targetSquare);
    }

    public MainPathSquare getBoardEntrySquareForColor(Color color) {
        if (this.mainPath.isEmpty()) {
            throw new IllegalStateException("El camino principal no está inicializado.");
        }
        // Entradas por color: Rojo=1, Verde=15, Amarillo=29, Azul=43
        switch (color) {
            case RED:
                return mainPath.get(1);
            case GREEN:
                return mainPath.get(15);
            case YELLOW:
                return mainPath.get(29);
            case BLUE:
                return mainPath.get(43);
            default:
                return mainPath.get(1);
        }
    }

    /**
     * Devuelve la casilla central del tablero (donde convergen todas las fichas al final).
     */
    public MainPathSquare getCentralSquare() {
        // Usa el índice medio del mainPath
        int centerIdx = mainPathSize / 2;
        return mainPath.get(centerIdx);
    }

    private static final Map<core.Color,Integer> ENTRY_POS = Map.of(
            core.Color.RED,    1,
            core.Color.GREEN,  15,
            core.Color.YELLOW, 29,
            core.Color.BLUE,   43
    );

    /** Devuelve y limpia el último evento ocurrido en el tablero */
    public String fetchLastEvent() {
        String msg = lastEventMessage;
        lastEventMessage = "";
        return msg;
    }

}