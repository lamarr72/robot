package log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class LogWindowSource
{
    private final int m_iQueueLength;
    //использую потокобозеопасный список с ограниченным размером
    private final List<LogEntry> m_messages;
    //CopyOnWriteArrayList для слушателей: чтение частое, запись редкая
    private final CopyOnWriteArrayList<LogChangeListener> m_listeners;

    ///
    public LogWindowSource(int iQueueLength) {
        m_iQueueLength = iQueueLength;
        //синхронизованный список для хранения сообщений
        m_messages = Collections.synchronizedList(new ArrayList<>());
        m_listeners = new CopyOnWriteArrayList<>();  //инициализация списка слушателей
    }
    ///
    public void registerListener(LogChangeListener listener)
    {
        m_listeners.add(listener);
    }

    ///
    public void unregisterListener(LogChangeListener listener)
    {
        m_listeners.remove(listener);
    }

    ///
    public void append(LogLevel logLevel, String strMessage)
    {
        LogEntry entry = new LogEntry(logLevel, strMessage);
        synchronized (m_messages) {
            if (m_messages.size() >= m_iQueueLength) {
                m_messages.remove(0); //удаление самого старого сообщения
            }
            m_messages.add(entry);
        }
        for (LogChangeListener listener : m_listeners) {
            listener.onLogChanged();
        }

    }
    
    public int size()
    {
        return m_messages.size();
    }

    ///
    public Iterable<LogEntry> all() {
        synchronized (m_messages) {
            return new ArrayList<>(m_messages);
        }
    }
}
