package gui;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * Базовый класс для внутренних окон с общей логикой подтверждения закрытия.
 */
public class BaseInternalFrame extends JInternalFrame {

    public BaseInternalFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
        super(title, resizable, closable, maximizable, iconifiable);
        
        //перехватываем управление закрытием фрейма
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        //общий слушатель для всех окон-наследников
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                Object[] options = {"Да", "Нет"};
                int n = javax.swing.JOptionPane.showOptionDialog(BaseInternalFrame.this,
                        "Вы действительно хотите закрыть окно \"" + getTitle() + "\"",
                        "Подтверждение",
                        javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);

                if (n == 0) {
                    beforeClose(); //вызываем метод-заглушку перед уничтожением окна
                    dispose();     //теперь окно закрывается корректно и освобождает ресурсы
                }
            }
        });
    }

    /**
     * Метод-крючок (hook). Наследники переопределяют его, 
     * если им нужно выполнить действия перед уничтожением окна.
     */
    protected void beforeClose() {
        // По умолчанию базовый класс ничего не делает
    }
}