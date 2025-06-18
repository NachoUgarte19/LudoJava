package board;

import core.Color;

public class FinalPathSquare extends AbstractSquare {
    private final Color associatedColor;
    private final int position;
    private final boolean isTerminal;

    public FinalPathSquare(Color associatedColor, int position, boolean isTerminal) {
        this.associatedColor = associatedColor;
        this.position = position;
        this.isTerminal = isTerminal;
    }

    @Override
    public Color getAssociatedColor() {
        return associatedColor;
    }

    @Override
    public int getPosition() {
        return position;
    }

    public boolean isTerminalSquare() {
        return isTerminal;
    }
}