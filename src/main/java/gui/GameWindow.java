package gui;

import java.awt.BorderLayout;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

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
        setJMenuBar(createDifficultyMenuBar());
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
    }

    /**
     * Создает строку меню с выбором уровня сложности (Паттерн Strategy).
     */
    private JMenuBar createDifficultyMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu difficultyMenu = new JMenu("Сложность");
        
        //ButtonGroup гарантирует, что активным может быть только один пункт
        ButtonGroup group = new ButtonGroup();
        
        JRadioButtonMenuItem easyItem = new JRadioButtonMenuItem("Легко");
        easyItem.addActionListener(e -> m_visualizer.setDifficulty(DifficultyLevel.EASY));
        
        JRadioButtonMenuItem normalItem = new JRadioButtonMenuItem("Нормально");
        normalItem.setSelected(true);
        normalItem.addActionListener(e -> m_visualizer.setDifficulty(DifficultyLevel.NORMAL));
        
        JRadioButtonMenuItem hardItem = new JRadioButtonMenuItem("Сложно");
        hardItem.addActionListener(e -> m_visualizer.setDifficulty(DifficultyLevel.HARD));
        
        //группировка элементов 
        group.add(easyItem);
        group.add(normalItem);
        group.add(hardItem);
        
        //добавление элементов в меню
        difficultyMenu.add(easyItem);
        difficultyMenu.add(normalItem);
        difficultyMenu.add(hardItem);
        
        menuBar.add(difficultyMenu);
        
        return menuBar;
    }
}