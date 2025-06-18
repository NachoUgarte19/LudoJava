package board;

public class MainPathSquare extends AbstractSquare {
    private final int position;       // Índice en el camino principal
    private final boolean isSpecial; // Indica si esta casilla es especial (por ejemplo, punto seguro)

    public MainPathSquare(int position, boolean isSpecial) {
        this.position = position;
        this.isSpecial = isSpecial;
    }

    @Override
    public int getPosition() {
        return position;
    }

    public boolean isSpecial() {
        return isSpecial;
    }
}
