package core;

import board.AbstractSquare;
import board.FinalPathSquare;

public class Piece {
    private final Color color;
    private AbstractSquare currentSquare = null;
    private boolean isFinished = false;
    private final int id;

    public Piece(Color color, int id) {
        this.color = color;
        this.id = id;
    }

    public void moveTo(AbstractSquare newSquare) {
        this.currentSquare = newSquare;
        this.isFinished = false;

        if (newSquare instanceof FinalPathSquare) {
            FinalPathSquare fps = (FinalPathSquare) newSquare;
            if (fps.isTerminalSquare() && fps.getAssociatedColor() == this.color) {
                this.isFinished = true;
            }
        }
    }

    public AbstractSquare getCurrentSquare() {
        return currentSquare;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isInBase() {
        return currentSquare == null;
    }

    public int getId() {
        return id;
    }

    public Color getColor() {
        return color;
    }
}