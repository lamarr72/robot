package gui;

import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * Базовый класс для внутренних окон с общей логикой подтверждения закрытия.
 */
public class BaseInternalFrame extends JInternalFrame {

    //хранение координат до развертывания
    private Rectangle normalBounds = new Rectangle();

    public BaseInternalFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
        super(title, resizable, closable, maximizable, iconifiable);
        
        // Перехватываем управление закрытием окна
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        //слушатель для отслеживания изменения размеров и перемещения
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Запоминаем размер только если окно в оконном режиме
                if (!isMaximum() && !isIcon()) {
                    normalBounds = getBounds();
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                //запоминает положение только если окно в оконном режиме
                if (!isMaximum() && !isIcon()) {
                    normalBounds = getBounds();
                }
            }
        });

        //общий слушатель для подтверждения закрытия
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
                    beforeClose(); 
                    dispose();     
                }
            }
        });
    }

    /**
     * Возвращает координаты окна в его "нормальном" состоянии.
     */
    public Rectangle getStoredNormalBounds() {
        //если окно еще ни разу не двигали и рамка пустая, возвращаем текущие
        if (normalBounds.width == 0) {
            return getBounds();
        }
        return normalBounds;
    }

    protected void beforeClose() {
    }
}