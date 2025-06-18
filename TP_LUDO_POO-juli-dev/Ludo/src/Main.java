import javax.swing.JOptionPane;
import ui.ConsoleGameUI;
import ui.SwingGameConfigUI;

/**
 * Punto de entrada único que permite elegir entre versión consola o gráfica.
 */
public class Main {
    public static void main(String[] args) {
        // Diálogo para seleccionar modo de juego
        String[] modos = {"Consola", "Gráfica"};
        int seleccion = JOptionPane.showOptionDialog(
                null,
                "¿Cómo quieres jugar?",
                "Selecciona modo de juego",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                modos,
                modos[1]
        );

        if (seleccion == 0) {
            // Inicia la versión de consola
            ConsoleGameUI.main(args);
        } else {
            // Inicia la versión gráfica
            SwingGameConfigUI.main(args);
        }
    }
}
