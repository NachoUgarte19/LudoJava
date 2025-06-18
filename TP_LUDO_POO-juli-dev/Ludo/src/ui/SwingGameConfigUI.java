package ui;

import core.Color;
import core.Player;
import game.Game;
import ui.SwingInteractiveGame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana de configuración para la versión Swing de Ludo.
 */
public class SwingGameConfigUI extends JFrame {
    private final SpinnerNumberModel spinnerModel;
    private final JPanel namesPanel;
    private final List<JTextField> nameFields = new ArrayList<>();

    public SwingGameConfigUI() {
        super("Configuración de Ludo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        ((JComponent) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Configuración del Juego");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBorder(new EmptyBorder(10, 0, 10, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        center.add(new JLabel("Cantidad de jugadores:"), gbc);
        spinnerModel = new SpinnerNumberModel(2, 2, Color.values().length, 1);
        JSpinner spinner = new JSpinner(spinnerModel);
        gbc.gridx = 1;
        center.add(spinner, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        namesPanel = new JPanel(new GridBagLayout());
        namesPanel.setBorder(BorderFactory.createTitledBorder("Nombres de jugadores"));
        center.add(namesPanel, gbc);

        add(center, BorderLayout.CENTER);

        updateNameFields(spinnerModel.getNumber().intValue());
        spinner.addChangeListener(e -> updateNameFields(spinnerModel.getNumber().intValue()));

        JButton startBtn = new JButton("Iniciar Juego");
        startBtn.addActionListener(this::onStart);
        JPanel bottom = new JPanel();
        bottom.add(startBtn);
        add(bottom, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void updateNameFields(int count) {
        namesPanel.removeAll();
        nameFields.clear();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < count; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.gridwidth = 1;
            namesPanel.add(new JLabel("Jugador " + (i + 1) + ":"), gbc);
            gbc.gridx = 1;
            JTextField field = new JTextField(15);
            nameFields.add(field);
            namesPanel.add(field, gbc);
        }
        namesPanel.revalidate();
        namesPanel.repaint();
        pack();
    }

    private void onStart(ActionEvent ev) {
        int num = spinnerModel.getNumber().intValue();
        List<Player> players = new ArrayList<>();
        Color[] colors = Color.values();
        for (int i = 0; i < num; i++) {
            String name = nameFields.get(i).getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Por favor ingresa el nombre del jugador " + (i + 1),
                        "Campo vacío",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            Player p = new Player(name, colors[i]);
            p.initializePieces(4);
            players.add(p);
        }

        // Aquí instanciamos la versión Swing de la lógica interactiva
        // Después:
        Game game = new SwingInteractiveGame(players, 56);
        SwingUtilities.invokeLater(() -> {
            SwingGameUI ui = new SwingGameUI(game);
            ui.setVisible(true);
        });
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingGameConfigUI().setVisible(true));
    }
}
