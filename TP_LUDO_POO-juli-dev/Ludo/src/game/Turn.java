package game;

import core.Player;

import java.util.List;

public class Turn {
    private final List<Player> players;
    private int nextPlayerIndex = 0;

    public Turn(List<Player> players) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("La lista de jugadores no puede ser nula o vacía.");
        }
        this.players = players;
    }


    /*
     * Devuelve el proximo jugador
     * */
    public Player nextTurn() {
        Player playerToPlay = players.get(nextPlayerIndex);
        nextPlayerIndex = (nextPlayerIndex + 1) % players.size();
        return playerToPlay;
    }


}