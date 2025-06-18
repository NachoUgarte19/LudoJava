package core;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String name;
    private final Color color;
    private final List<Piece> pieces = new ArrayList<>();
    private boolean rendido = false;

    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public void initializePieces( int quantity) {
        for (int i = 0; i < quantity; i++) {
            pieces.add(new Piece(this.color, i + 1)); // ID de ficha desde 1
        }
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    /*
    * Devuelve la copia de la lista de fichas del jugador
    * */
    public List<Piece> getPieces() {
        return new ArrayList<>(pieces);
    }

    public boolean hasWon() {
        if (pieces.isEmpty()) return false;
        return pieces.stream().allMatch(Piece::isFinished);
    }

    public boolean isRendido() {
        return rendido;
    }

    public void rendirse() {
        this.rendido = true;
    }
}