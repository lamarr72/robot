package log;

/**
 * Интерфейс для слушателей изменений логов.<br>
 * Имеет метод {@link #onLogChanged} для уведобмления о новых сообщениях.
 */
public interface LogChangeListener
{
    public void onLogChanged(); 
}
