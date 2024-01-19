package me.realized.duels.util.io;

import java.io.File;
import java.io.IOException;

public final class FileUtil {

    private FileUtil() {
    }

    public static boolean checkNonEmpty(final File file, final boolean create) throws IOException {
        if (!file.exists()) {
            if (create) {
                file.createNewFile();
            }
            return false;
        }

        return file.length() > 0;
    }
}
