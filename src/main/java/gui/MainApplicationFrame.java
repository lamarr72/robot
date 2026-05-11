package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import log.Logger;

/** 
 * Главный класс приложения. <br>
 * Создает главное окно ({@link JFrame} с рабочим столом ({@link JDesktopPane}), на котором размещаются внутренние онкна. <br>
 * Добавляет окно логов ({@link LogWindow}) и игровое окно ({@link GameWindow}).
 */
public class MainApplicationFrame extends JFrame
{
    private final JDesktopPane desktopPane = new JDesktopPane();
    
    public MainApplicationFrame() {
        //установка размеров окна исходя из размеров экрана
        int inset = 50;        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
            screenSize.width  - inset*2,
            screenSize.height - inset*2);

        setContentPane(desktopPane);
        
        ///инициализация окон
        addWindow(createLogWindow());
        addWindow(createGameWindow());
        
        ///установка меню
        setJMenuBar((generateMenuBar()));

        ///операция при закрытии
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
    /**
     * Создает окно лога.
     */
    protected LogWindow createLogWindow() {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10,10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }
    
    /**
     * Создает игровое окно ///
     */
    protected GameWindow createGameWindow() {
        GameWindow gameWindow = new GameWindow();
        gameWindow.setSize(400, 400);
        gameWindow.setLocation(320, 10);
        return gameWindow;
    }

    /**
     * Добавляет внутреннее окно на рабочую область
     */
    protected void addWindow(JInternalFrame frame)
    {
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    /**
     * Основной метод генерации меню.
     * Собирает меню из отдельных частей
     */
        
    private JMenuBar generateMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(createLookAndFeelMenu());
        menuBar.add(createTestMenu());
        menuBar.add(createExitMenu());

        return menuBar;
    }

    /**
     * Создает меню управления внешним видом.
     * 
     */
    private JMenu createLookAndFeelMenu() {
        JMenu lookAndFeelMenu = new JMenu("Режим отображения");
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);

        lookAndFeelMenu.add(createMenuItem("Систменая схема", KeyEvent.VK_S, () -> setLookAndFeel(UIManager.getSystemLookAndFeelClassName())));
        lookAndFeelMenu.add(createMenuItem("Универсальная схема", KeyEvent.VK_U, () -> setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())));

        return lookAndFeelMenu;
    }
    
    /**
     * Создает меню с тестами.
     */
    private JMenu createTestMenu() {
        JMenu testMenu = new JMenu("Тесты");
        testMenu.setMnemonic(KeyEvent.VK_T);

        testMenu.add(createMenuItem("Сообщение в лог", KeyEvent.VK_M, () -> Logger.debug("Тестовое сообщение")));

        return testMenu;
    } 

    /**
     * Создает меню выхода.
     * + подтверждение выхода.
     */
    private JMenu createExitMenu() {
        JMenu exitMenu = new JMenu("Файл");
        exitMenu.setMnemonic(KeyEvent.VK_F);

        exitMenu.add(createMenuItem("Выход", KeyEvent.VK_X, () -> {
            Object[] options = {"Да", "Нет"};
            int n = javax.swing.JOptionPane.showOptionDialog(this,
                "Вы действиетльно хотите выйти?", 
                "Подтвреждение", 
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
            if (n==0) System.exit(0);
        }));

        return exitMenu;
    }

    /**
     * Вспомогательный метод для создания пунктов меню.
     * Убирает дублирование кода addActionListener и создания объектов.
     */
    private JMenuItem createMenuItem(String text, int mnemonic, Runnable action) {
        JMenuItem item = new JMenuItem(text, mnemonic);
        item.addActionListener(event -> {
            action.run();
            this.invalidate();  //перерисовка окна после действий
        });
        return item;
    }

    /**
     * Устанавливает LookAndFell и обновляет UI всех компонентов.
     */
    private void setLookAndFeel(String className)
    {
        try
        {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (ClassNotFoundException | InstantiationException
            | IllegalAccessException | UnsupportedLookAndFeelException e) {
            ///
            Logger.error("Ошибка смены темы: " + e.getMessage());
        }
    }
}
