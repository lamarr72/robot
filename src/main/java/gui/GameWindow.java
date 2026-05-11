package gui;

import java.awt.BorderLayout;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

/**
 * внутреннее окно ({@link JInternalFrame}) для игрового пола. 
 * Содержит {@link GameVisualizer} и зазмещается на рабочем столе главоного фрейма.
 */
public class GameWindow extends JInternalFrame
{
    private final GameVisualizer m_visualizer;
    public GameWindow() 
    {
        super("Игровое поле", true, true, true, true);
        m_visualizer = new GameVisualizer();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        this.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent e) {
                Object[] options = {"Да", "Нет"};
                int n = javax.swing.JOptionPane.showOptionDialog(GameWindow.this,
                    "Закрыть игровое поле?",
                    "Подтверждение",
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
            
                 if (n == 0) {
                    dispose();
                }
            }
        });
        pack();
    }
}
