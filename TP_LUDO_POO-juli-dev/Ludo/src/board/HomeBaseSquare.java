package board;

import core.Color;

public class HomeBaseSquare extends AbstractSquare {
    private final Color associatedColor;

    public HomeBaseSquare(Color associatedColor) {
        this.associatedColor = associatedColor;
    }

    @Override
    public Color getAssociatedColor() {
        return associatedColor;
    }


}