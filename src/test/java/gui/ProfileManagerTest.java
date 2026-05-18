package gui;

import org.junit.Assert;
import org.junit.Test;
import javax.swing.JInternalFrame;

public class ProfileManagerTest {

    @Test
    public void testProfileManagerSaveAndLoad() {
        WindowProfileManager manager = new WindowProfileManager();
        JInternalFrame testFrame = new JInternalFrame("Test", true, true, true, true);
        
        // Задаем тестовые координаты и размеры
        testFrame.setBounds(150, 250, 350, 450);
        manager.saveFrameState("testWindow", testFrame);
        
        // Создаем другое окно и применяем к нему сохраненное состояние
        JInternalFrame restoredFrame = new JInternalFrame("Restored", true, true, true, true);
        manager.restoreFrameState("testWindow", restoredFrame);
        
        // Проверяем, что всё восстановилось один в один
        Assert.assertEquals(150, restoredFrame.getX());
        Assert.assertEquals(250, restoredFrame.getY());
        Assert.assertEquals(350, restoredFrame.getWidth());
        Assert.assertEquals(450, restoredFrame.getHeight());
    }
}