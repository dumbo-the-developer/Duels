package me.realized.duels.logging;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.DateUtil;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log.LogSource;

public class LogManager implements Loadable, LogSource {

    private final File folder;

    @Getter
    private final Logger logger = Logger.getAnonymousLogger();
    private FileHandler handler;

    public LogManager(final DuelsPlugin plugin) {
        final File dataFolder = plugin.getDataFolder();

        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        this.folder = new File(dataFolder, "logs");

        if (!folder.exists()) {
            folder.mkdir();
        }

        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
    }

    @Override
    public void handleLoad() throws IOException {
        final File file = new File(folder, DateUtil.formatDate(new Date()) + ".log");

        if (!file.exists()) {
            file.createNewFile();
        }

        handler = new FileHandler(file.getCanonicalPath(), true);
        handler.setLevel(Level.ALL);
        handler.setFormatter(new Formatter() {
            @Override
            public String format(final LogRecord record) {
                return "[" + DateUtil.formatDatetime(record.getMillis()) + "] [" + record.getLevel().getLocalizedName() + "] " + record.getMessage() + '\n';
            }
        });
        logger.addHandler(handler);
    }

    @Override
    public void handleUnload() {
        if (handler == null) {
            return;
        }

        handler.close();
        logger.removeHandler(handler);
        handler = null;
    }

    @Override
    public void log(final Level level, final String s) {
        if (handler == null) {
            return;
        }

        getLogger().log(level, s);
    }
}
