package gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.JInternalFrame;
import log.Logger;

/**
 * Класс для сохранения и восстановления состояния внутренних окон (профиля).
 */
public class WindowProfileManager {
    
    //файл конфигурации в юзер папке 
    private static final File PROFILE_FILE = new File(System.getProperty("user.home"), ".robots_profile");
    private final Properties properties = new Properties();

    public WindowProfileManager() {
        load();
    }

    /**
     * Проверка физического существования файла профиля на диске.
     */
    public boolean hasSavedProfile() {
        return PROFILE_FILE.exists() && !properties.isEmpty();
    }

    /**
     * Извлечение состояния окна и запись его в свойства.
     */
    public void saveFrameState(String key, JInternalFrame frame) {
        java.awt.Rectangle bounds;
        
        //если это baseinternalframe окно, берет нормальные координаты
        if (frame instanceof BaseInternalFrame) {
            bounds = ((BaseInternalFrame) frame).getStoredNormalBounds();
        } else {
            bounds = frame.getBounds();
        }

        properties.setProperty(key + ".x", String.valueOf(bounds.x));
        properties.setProperty(key + ".y", String.valueOf(bounds.y));
        properties.setProperty(key + ".width", String.valueOf(bounds.width));
        properties.setProperty(key + ".height", String.valueOf(bounds.height));
        properties.setProperty(key + ".isIcon", String.valueOf(frame.isIcon()));
        properties.setProperty(key + ".isMaximum", String.valueOf(frame.isMaximum()));
    }

    /**
     * Применение сохраненных свойств к окну.
     */
    public void restoreFrameState(String key, JInternalFrame frame) {
        if (!properties.containsKey(key + ".x")) {
            return; //если для этого окна нет сохраненного состояния, остается дефолт
        }

        try {
            int x = Integer.parseInt(properties.getProperty(key + ".x"));
            int y = Integer.parseInt(properties.getProperty(key + ".y"));
            int width = Integer.parseInt(properties.getProperty(key + ".width"));
            int height = Integer.parseInt(properties.getProperty(key + ".height"));
            boolean isIcon = Boolean.parseBoolean(properties.getProperty(key + ".isIcon"));
            boolean isMaximum = Boolean.parseBoolean(properties.getProperty(key + ".isMaximum"));

            frame.setBounds(x, y, width, height);
            
            //восстановление свернутого/развернутого состояния

            if (isIcon) {
                frame.setIcon(true);
            } else if (isMaximum) {
                frame.setMaximum(true);
            }
            // if (isMaximum) {
            //     frame.setMaximum(true);
            // } else if (isIcon) {
            //     frame.setIcon(true);
            // }
        } catch (Exception e) {
            Logger.error("Ошибка при восстановлении состояния окна " + key + ": " + e.getMessage());
        }
    }

    /**
     * Запись накопленных свойств в файл.
     */
    public void saveToFile() {
        try (FileOutputStream out = new FileOutputStream(PROFILE_FILE)) {
            properties.store(out, "Robots Application Window Profile");
        } catch (IOException e) {
            Logger.error("Не удалось сохранить профиль в файл: " + e.getMessage());
        }
    }

    /**
     * Считывание свойств из файла.
     */
    private void load() {
        if (PROFILE_FILE.exists()) {
            try (FileInputStream in = new FileInputStream(PROFILE_FILE)) {
                properties.load(in);
            } catch (IOException e) {
                Logger.error("Не удалось загрузить профиль из файла: " + e.getMessage());
            }
        }
    }
}