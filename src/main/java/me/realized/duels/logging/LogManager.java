/*
 * This file is part of Duels, licensed under the MIT License.
 *
 * Copyright (c) Realized
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.realized.duels.logging;

import java.io.File;
import java.io.IOException;
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
import me.realized.duels.util.Log.LogSource;

// TODO: 02/06/2018 Fix, currrently generates multiple log files in a single day
public class LogManager implements Loadable, LogSource {

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

    @Override
    public void log(final Level level, final String s) {
        getLogger().log(level, s);
    }
}
