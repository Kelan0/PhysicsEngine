package main.core;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

/**
 * @author Kelan
 */
public class LogHandler extends OutputStream
{
    private static final String logFile = "log/";
    private static final boolean showSource = false;
    private static Logger logger;
    private static DateFormat dateFormat = new SimpleDateFormat("'['dd:MM:yyyy']['hh:mm:ss.SSS']'");

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    static
    {
        File directory = new File(logFile);

        if (!directory.exists())
            directory.mkdirs();

        if (directory.isFile())
            directory = directory.getParentFile();

        File file = new File(directory, new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".log");

        logger = Logger.getLogger(LogHandler.class.getName());
        logger.setUseParentHandlers(false);

        try
        {
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new LogFormatter(false, true));
            consoleHandler.setLevel(Level.FINEST);
            logger.addHandler(consoleHandler);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            FileHandler fileHandler = new FileHandler(file.getAbsolutePath());
            fileHandler.setFormatter(new LogFormatter(true, false));
            fileHandler.setLevel(Level.FINEST);
            logger.addHandler(fileHandler);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        logger.setLevel(Level.ALL);

        System.setOut(new PrintStream(new LogHandler(Level.INFO)));
        System.setErr(new PrintStream(new LogHandler(Level.SEVERE)));
    }

    private StringBuilder stringBuilder;
    private Level level;

    private LogHandler(Level level)
    {
        this.level = level;
    }

    public static Logger getLogger()
    {
        return logger;
    }

    @Override
    public void write(int b)
    {
        synchronized (this)
        {
            if (stringBuilder == null)
                stringBuilder = new StringBuilder();

            char c = (char) b;
            if (c == '\r' || c == '\n')
            {
                if (stringBuilder.length() > 0)
                {
                    getLogger().log(level, stringBuilder.toString());
                    stringBuilder = null;
                }
            } else
                stringBuilder.append(c);
        }
    }

    private static class LogFormatter extends Formatter
    {
        private boolean showSource;
        private boolean enableColour;

        public LogFormatter(boolean showSource, boolean enableColour)
        {
            this.showSource = showSource;
            this.enableColour = enableColour;
        }

        @Override
        public String format(LogRecord record)
        {
            String source = showSource ? "[" + record.getSourceClassName() + "." + record.getSourceMethodName() + "]" : "";
            String time = dateFormat.format(new Date(record.getMillis()));
            String thread = "[" + Thread.currentThread().getName() + "]";
            String level = "[" + record.getLevel() + "]";
            String message = formatMessage(record);

            String colour = ANSI_RESET;

            if (record.getLevel().equals(Level.INFO))
                colour = ANSI_BLACK;
            if (record.getLevel().equals(Level.SEVERE))
                colour = ANSI_RED;
            if (record.getLevel().equals(Level.WARNING))
                colour = ANSI_YELLOW;
            if (record.getLevel().equals(Level.CONFIG))
                colour = ANSI_BLUE;
            if (record.getLevel().equals(Level.FINE))
                colour = ANSI_PURPLE;
            if (record.getLevel().equals(Level.FINER))
                colour = ANSI_PURPLE;
            if (record.getLevel().equals(Level.FINEST))
                colour = ANSI_PURPLE;

            return (enableColour ? colour : "") + time + source + thread + level + ": " + message + (enableColour ? ANSI_RESET : "") + "\n";
        }
    }
}
