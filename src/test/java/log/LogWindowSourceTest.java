package log;

import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

public class LogWindowSourceTest {

    @Test
    public void testQueueLengthLimitation() {
        //создание источника логов с лимитом в 3 сообщения
        LogWindowSource source = new LogWindowSource(3);

        //добавление 5 сообщений
        source.append(LogLevel.Debug, "Сообщение 1");
        source.append(LogLevel.Debug, "Сообщение 2");
        source.append(LogLevel.Debug, "Сообщение 3");
        source.append(LogLevel.Debug, "Сообщение 4");
        source.append(LogLevel.Debug, "Сообщение 5");

        //копирование элементы из Iterable в обычный ArrayList
        List<LogEntry> entries = new ArrayList<>();
        for (LogEntry entry : source.all()) {
            entries.add(entry);
        }

        //проверка на то, что размер не превысил 3
        Assert.assertEquals(3, entries.size());
        Assert.assertEquals("Сообщение 3", entries.get(0).getMessage());
        Assert.assertEquals("Сообщение 5", entries.get(2).getMessage());

        //проверка на то, что первые два (самые старые) удалились, а последние остались
        int i = 0;
        for (LogEntry entry : source.all()) {
            if (i == 0) Assert.assertEquals("Сообщение 3", entry.getMessage());
            if (i == 2) Assert.assertEquals("Сообщение 5", entry.getMessage());
            i++;
        }
    }
}