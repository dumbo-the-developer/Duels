package me.realized.duels.logging;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.DateUtil;
import me.realized.duels.util.Loadable;

public class LogManager implements Loadable {

    private final File folder;

    @Getter
    private final Logger logger = Logger.getAnonymousLogger();

    public LogManager(final DuelsPlugin plugin) {
        final File dataFolder = plugin.getDataFolder();

        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        this.folder = new File(dataFolder, "logs");

        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    @Override
    public void handleLoad() throws IOException {
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        closeHandlers();

        final File file = new File(folder, DateUtil.formatDate(new Date()) + ".log");

        if (!file.exists()) {
            file.createNewFile();
        }

        final FileHandler handler = new FileHandler(file.getCanonicalPath(), true);
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
        closeHandlers();
    }

    private void closeHandlers() {
        for (final Handler handler : logger.getHandlers()) {
            handler.close();
        }
    }
}
