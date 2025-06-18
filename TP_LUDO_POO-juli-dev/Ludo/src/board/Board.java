package board;

import core.Color;
import core.Piece;
import core.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {
    public static final int FINAL_PATH_LENGTH = 4;

    private final int mainPathSize;
    private final int lastMainPathIndex;

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

        for (int i = 0; i < this.mainPathSize; i++) {
            boolean isSpecial = false;
            if (!players.isEmpty() && (this.mainPathSize / players.size() > 0)) { // Evitar división por cero
                if (i % (this.mainPathSize / players.size()) == 0) {
                    isSpecial = true;
                }
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

    private void moveOnMainPath(Piece piece, MainPathSquare currentMpSquare, int roll) {
        int currentMainPathPos = currentMpSquare.getPosition();
        Color pieceColor = piece.getColor();
        int potentialNextMainPathPos = currentMainPathPos + roll;

        if (potentialNextMainPathPos > this.lastMainPathIndex) {
            int stepsIntoFinalPath = potentialNextMainPathPos - this.lastMainPathIndex;

            if (stepsIntoFinalPath >= 1 && stepsIntoFinalPath <= FINAL_PATH_LENGTH) {
                List<FinalPathSquare> playerFinalPath = finalPaths.get(pieceColor);
                if (playerFinalPath != null && !playerFinalPath.isEmpty()) {
                    FinalPathSquare targetFps = playerFinalPath.get(stepsIntoFinalPath - 1);

                    boolean canLand = true;
                    if (!targetFps.isEmpty()) {
                        if (targetFps.getPieces().getFirst().getColor() == pieceColor) {
                            // No se puede moverse a una casilla de tu final path si ya está ocupada por OTRA de tus fichas.
                            if (targetFps.getPieces().getFirst() != piece) { // comprobar que no es la misma ficha
                                canLand = false;
                            }
                        }
                    }

                    if (canLand) {
                        handleLanding(piece, targetFps, false);
                        return;
                    } else {
                        // No puede entrar al FinalPath (casilla ocupada por propia ficha), aterriza al final del MainPath.
                        System.out.println("Ficha " + piece.getId() + " ("+pieceColor+") no pudo entrar a FinalPath (casilla " + targetFps.getPosition() +" ocupada). Aterriza en MainPath " + this.lastMainPathIndex);
                        handleLanding(piece, mainPath.get(this.lastMainPathIndex), false);
                        return;
                    }
                }
            }
            // Si stepsIntoFinalPath es inválido o el FinalPath no está configurado, la ficha cae en la última casilla del MainPath.
            System.out.println("Ficha " + piece.getId() + " ("+pieceColor+") con tiro efectivo " + stepsIntoFinalPath + " para FinalPath es inválido. Aterriza en MainPath " + this.lastMainPathIndex);
            handleLanding(piece, mainPath.get(this.lastMainPathIndex), false);
        } else {
            MainPathSquare nextSquareOnMainPath = mainPath.get(potentialNextMainPathPos % this.mainPathSize);
            handleLanding(piece, nextSquareOnMainPath, false);
        }
    }

    private void moveOnFinalPath(Piece piece, FinalPathSquare currentFpSquare, int roll) {
        Color pieceColor = piece.getColor();
        List<FinalPathSquare> playerFinalPath = finalPaths.get(pieceColor);

        int currentLocalPos = currentFpSquare.getPosition();
        int nextLocalPos = currentLocalPos + roll;
        FinalPathSquare targetFps;

        if (nextLocalPos >= FINAL_PATH_LENGTH - 1) {
            targetFps = playerFinalPath.get(FINAL_PATH_LENGTH - 1);
        } else if (nextLocalPos >= 0) {
            targetFps = playerFinalPath.get(nextLocalPos);
        } else {
            System.err.println("Error: Posición negativa ("+ nextLocalPos +") calculada en camino final para ficha " + piece.getId());
            currentFpSquare.addPiece(piece);
            return;
        }

        boolean canLand = true;
        if (!targetFps.isEmpty()) {
            if (targetFps.getPieces().getFirst().getColor() == pieceColor && targetFps.getPieces().getFirst() != piece) {
                canLand = false; // Ocupada por OTRA ficha del mismo color
            }
        }

        if (canLand) {
            handleLanding(piece, targetFps, false);
        } else {
            System.out.println("Ficha " + piece.getId() + " ("+pieceColor+") no pudo moverse a FinalPath casilla " + targetFps.getPosition() +" (ocupada). Se queda en " + currentLocalPos);
            currentFpSquare.addPiece(piece); // Se queda donde estaba
        }
    }

    private void handleLanding(Piece movingPiece, AbstractSquare targetSquare, boolean isBoardEntryFromBase) {
        if (targetSquare instanceof MainPathSquare && !isBoardEntryFromBase) {
            MainPathSquare mpTarget = (MainPathSquare) targetSquare;
            if (!mpTarget.isEmpty() && !mpTarget.isSpecial()) {
                List<Piece> piecesOnTargetCopy = new ArrayList<>(targetSquare.getPieces());
                for (Piece existingPiece : piecesOnTargetCopy) {
                    if (existingPiece != movingPiece && existingPiece.getColor() != movingPiece.getColor()) {
                        System.out.println("¡Se captura la Ficha " + movingPiece.getId() + "(" + movingPiece.getColor() +
                                ") captura a  la ficha " + existingPiece.getId() + "(" + existingPiece.getColor() +
                                ") en la casilla del MainPath " + mpTarget.getPosition());
                        targetSquare.removePiece(existingPiece);
                        existingPiece.moveTo(null);
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


}