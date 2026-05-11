package log;

/**
 * Статический класс для логирования.<br>
 * Имеет статический иссточнико логов ({@link LogWondowSource}) и методы {@link #debug(String)} 
 * и {@link #error(String)} для добавления сообщений.
 */
public final class Logger
{
    private static final LogWindowSource defaultLogSource;
    static {
        defaultLogSource = new LogWindowSource(100);
    }
    
    private Logger()
    {
    }

    public static void debug(String strMessage)
    {
        defaultLogSource.append(LogLevel.Debug, strMessage);
    }
    
    public static void error(String strMessage)
    {
        defaultLogSource.append(LogLevel.Error, strMessage);
    }

    public static LogWindowSource getDefaultLogSource()
    {
        return defaultLogSource;
    }
}
