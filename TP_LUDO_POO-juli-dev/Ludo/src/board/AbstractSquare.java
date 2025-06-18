package board;

import core.Color;
import core.Piece;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSquare {
    protected List<Piece> pieces = new ArrayList<>();

    public void addPiece(Piece piece) {
        if (piece != null && !this.pieces.contains(piece)) {
            this.pieces.add(piece);
        }
    }

    public void removePiece(Piece piece) {
        this.pieces.remove(piece);
    }

    public boolean isEmpty() {
        return this.pieces.isEmpty();
    }

    public List<Piece> getPieces() {
        return new ArrayList<>(this.pieces);
    }

    public int getPosition() {
        return 0; // en los otros los sobreescrimimos
    }

    public Color getAssociatedColor() {
        if (this instanceof HomeBaseSquare) {
            return this.getAssociatedColor();
        }
        if (this instanceof FinalPathSquare) {
            return this.getAssociatedColor();
        }
        return null;
    }
}