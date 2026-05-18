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
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import log.Logger;

/**
 * Главный класс приложения.
 */
public class MainApplicationFrame extends JFrame
{
    private final JDesktopPane desktopPane = new JDesktopPane();
    private final WindowProfileManager profileManager = new WindowProfileManager();
    
    //сохранение ссылок на внутренние окна для доступа к ним при выходе
    private LogWindow logWindow;
    private GameWindow gameWindow;
    
    public MainApplicationFrame() {
        //установка размеров окна исходя из размеров экрана
        int inset = 50;        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
            screenSize.width  - inset*2,
            screenSize.height - inset*2);

        setContentPane(desktopPane);
        
        ///инициализация окон
        logWindow = createLogWindow();
        gameWindow = createGameWindow();
        
        addWindow(logWindow);
        addWindow(gameWindow);
        
        // проверка профиля: Вывод диалога, если найден файл настроек
        if (profileManager.hasSavedProfile()) {
            Object[] options = {"Да", "Нет"};
            int response = JOptionPane.showOptionDialog(this,
                    "Обнаружен сохранённый профиль окон. Восстановить его положение?",
                    "Восстановление профиля",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            if (response == JOptionPane.YES_OPTION) {
                profileManager.restoreFrameState("logWindow", logWindow);
                profileManager.restoreFrameState("gameWindow", gameWindow);
            }
        }
        
        /// Установка меню
        setJMenuBar((generateMenuBar()));

        /// Запрещаем Swing закрывать приложение автоматически
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Вешаем слушатель на крестик главного окна
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmExit(); 
            }
        });
    }
    
    protected LogWindow createLogWindow() {
        LogWindow window = new LogWindow(Logger.getDefaultLogSource());
        window.setLocation(10,10);
        window.setSize(300, 800);
        setMinimumSize(window.getSize());
        window.pack();
        Logger.debug("Протокол работает");
        return window;
    }
    
    protected GameWindow createGameWindow() {
        GameWindow window = new GameWindow();
        window.setSize(400, 400);
        window.setLocation(320, 10);
        return window;
    }

    protected void addWindow(JInternalFrame frame) {
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    private JMenuBar generateMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createLookAndFeelMenu());
        menuBar.add(createTestMenu());
        menuBar.add(createExitMenu());
        return menuBar;
    }

    private JMenu createLookAndFeelMenu() {
        JMenu lookAndFeelMenu = new JMenu("Режим отображения");
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.add(createMenuItem("Системная схема", KeyEvent.VK_S, () -> setLookAndFeel(UIManager.getSystemLookAndFeelClassName())));
        lookAndFeelMenu.add(createMenuItem("Универсальная схема", KeyEvent.VK_U, () -> setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())));
        return lookAndFeelMenu;
    }
    
    private JMenu createTestMenu() {
        JMenu testMenu = new JMenu("Тесты");
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.add(createMenuItem("Сообщение в лог", KeyEvent.VK_M, () -> Logger.debug("Тестовое сообщение")));
        return testMenu;
    } 

    /**
     * Единый метод подтверждения выхода. Перед закрытием сохраняет профиль.
     */
    private void confirmExit() {
        Object[] options = {"Да", "Нет"};
        int n = JOptionPane.showOptionDialog(this,
            "Вы действительно хотите выйти?", 
            "Подтверждение выхода", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);
        
        if (n == 0) {
            ///сохранение профиля перед выходом
            profileManager.saveFrameState("logWindow", logWindow);
            profileManager.saveFrameState("gameWindow", gameWindow);
            profileManager.saveToFile();
            
            System.exit(0);
        }
    }

    private JMenu createExitMenu() {
        JMenu exitMenu = new JMenu("Файл");
        exitMenu.setMnemonic(KeyEvent.VK_F);
        exitMenu.add(createMenuItem("Выход", KeyEvent.VK_X, this::confirmExit));
        return exitMenu;
    }

    private JMenuItem createMenuItem(String text, int mnemonic, Runnable action) {
        JMenuItem item = new JMenuItem(text, mnemonic);
        item.addActionListener(event -> {
            action.run();
            this.invalidate();  
        });
        return item;
    }

    private void setLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        } catch (ClassNotFoundException | InstantiationException
            | IllegalAccessException | UnsupportedLookAndFeelException e) {
            Logger.error("Ошибка смены темы: " + e.getMessage());
        }
    }
}