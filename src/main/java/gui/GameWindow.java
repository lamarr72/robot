package gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;

/**
 * Внутреннее окно для игрового поля.
 */
public class GameWindow extends BaseInternalFrame
{
    private final GameVisualizer m_visualizer;
    public GameWindow() 
    {
        //вызов конструктора базового класса BaseInternalFrame
        super("Игровое поле", true, true, true, true);
        m_visualizer = new GameVisualizer();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
    }
}