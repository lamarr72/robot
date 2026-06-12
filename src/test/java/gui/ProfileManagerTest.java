package gui;

import org.junit.Assert;
import org.junit.Test;
import javax.swing.JInternalFrame;
import java.awt.event.ComponentEvent;

public class ProfileManagerTest {

    @Test
    public void testProfileManagerSaveAndLoad() {
        WindowProfileManager manager = new WindowProfileManager();
        JInternalFrame testFrame = new JInternalFrame("Test", true, true, true, true);
        
        // pадается тестовые координаты и размеры
        testFrame.setBounds(150, 250, 350, 450);
        manager.saveFrameState("testWindow", testFrame);
        
        // сощдается другое окно и применяем к нему сохраненное состояние
        JInternalFrame restoredFrame = new JInternalFrame("Restored", true, true, true, true);
        manager.restoreFrameState("testWindow", restoredFrame);
        
        // провека, что всё восстановилось один в один
        Assert.assertEquals(150, restoredFrame.getX());
        Assert.assertEquals(250, restoredFrame.getY());
        Assert.assertEquals(350, restoredFrame.getWidth());
        Assert.assertEquals(450, restoredFrame.getHeight());
    }

    @Test
    public void testMaximizedFrameSavesNormalBounds() throws java.beans.PropertyVetoException {
        WindowProfileManager manager = new WindowProfileManager();
        
        BaseInternalFrame smartFrame = new BaseInternalFrame("Smart Window", true, true, true, true);
        
        // иммитацичя нормальные, удобные пользователю размеры
        smartFrame.setBounds(50, 50, 300, 400);
        
        // дергается слушатель, сообщая окну, что размер изменился 
        smartFrame.dispatchEvent(new ComponentEvent(smartFrame, ComponentEvent.COMPONENT_RESIZED));
        
        // имитация развертывание окна на "весь экран"
        smartFrame.setMaximum(true);
        smartFrame.setBounds(0, 0, 1920, 1080); // Гигантские искаженные размеры
        
        // сохранение состояние развернутого окна в профиль
        manager.saveFrameState("smartWindow", smartFrame);
        
        // создание "чистое" окно и пытаемся восстановить на него профиль
        JInternalFrame restoredFrame = new JInternalFrame();
        manager.restoreFrameState("smartWindow", restoredFrame);
        
        // менеджер должен был проигнорировать 1920x1080 и сохранить 300x400
        Assert.assertEquals("Ширина должна быть сохранена до максимизации", 300, restoredFrame.getWidth());
        Assert.assertEquals("Высота должна быть сохранена до максимизации", 400, restoredFrame.getHeight());
        Assert.assertEquals("Координата X должна быть сохранена до максимизации", 50, restoredFrame.getX());
        Assert.assertEquals("Координата Y должна быть сохранена до максимизации", 50, restoredFrame.getY());
        
        // проверка, что само состояние "было развернуто" тоже сохранилось успешно
        Assert.assertTrue("Окно должно восстановиться в развернутом состоянии", restoredFrame.isMaximum());
    }

    @Test
    public void testIconifiedFrameRestoresCorrectly() throws java.beans.PropertyVetoException {
        WindowProfileManager manager = new WindowProfileManager();
        
        // сооздание окна-наследника BaseInternalFrame
        BaseInternalFrame smartFrame = new BaseInternalFrame("Icon Window", true, true, true, true);
        
        // задача нормальные размеры
        smartFrame.setBounds(80, 80, 250, 350);
        
        // инициализация слушаетля, который запоминает эти размеры
        smartFrame.dispatchEvent(new ComponentEvent(smartFrame, ComponentEvent.COMPONENT_RESIZED));
        
        // сворачивание окна
        smartFrame.setIcon(true);
        
        //сохранение свернутого окна в профиль
        manager.saveFrameState("iconWindow", smartFrame);
        
        //создание чистого окна и попытка восстановить профиль
        JInternalFrame restoredFrame = new JInternalFrame();
        manager.restoreFrameState("iconWindow", restoredFrame);
        
        // окно должно восстановиться в свернутом виде
        Assert.assertTrue("Окно должно восстановиться свернутым в иконку", restoredFrame.isIcon());
        
        // моенеджер должен был сохранить нормальные координаты окна (80, 80, 250, 350),
        // а не системные координаты маленькой иконки в левом нижнем углу экрана
        Assert.assertEquals("Ширина должна быть сохранена до сворачивания", 250, restoredFrame.getWidth());
        Assert.assertEquals("Высота должна быть сохранена до сворачивания", 350, restoredFrame.getHeight());
        Assert.assertEquals("Координата X должна быть сохранена до сворачивания", 80, restoredFrame.getX());
        Assert.assertEquals("Координата Y должна быть сохранена до сворачивания", 80, restoredFrame.getY());
    }
}