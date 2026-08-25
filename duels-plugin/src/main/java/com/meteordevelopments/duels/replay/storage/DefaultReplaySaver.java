package com.meteordevelopments.duels.replay.storage;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.replay.Replay;
import com.meteordevelopments.duels.replay.data.DuelReplayMetadata;
import com.meteordevelopments.duels.replay.data.ReplayData;
import com.meteordevelopments.duels.replay.util.LogUtils;
import com.meteordevelopments.duels.replay.util.fetcher.Acceptor;
import com.meteordevelopments.duels.replay.util.fetcher.Consumer;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DefaultReplaySaver implements IReplaySaver {

    public static File DIR;

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");

    private final Map<String, DuelReplayMetadata> metadataCache = new ConcurrentHashMap<>();

    private boolean reformatting;

    private final ExecutorService pool = Executors.newCachedThreadPool();

    public DefaultReplaySaver() {
        DIR = new File(DuelsPlugin.getInstance().getDataFolder(), "replays");
        if (!DIR.exists()) {
            DIR.mkdirs();
        }
        loadAllMetadata();
    }

    public static boolean isValidName(String replayName) {
        return NAME_PATTERN.matcher(replayName).matches();
    }

    public void loadAllMetadata() {
        if (!DIR.exists()) return;

        File[] metaFiles = DIR.listFiles((dir, name) -> name.endsWith(".meta"));
        if (metaFiles != null) {
            for (File file : metaFiles) {
                try (FileInputStream fis = new FileInputStream(file);
                     ObjectInputStream ois = new ObjectInputStream(fis)) {
                    DuelReplayMetadata metadata = (DuelReplayMetadata) ois.readObject();
                    if (metadata != null && metadata.getReplayId() != null) {
                        metadataCache.put(metadata.getReplayId(), metadata);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    public DuelReplayMetadata getMetadata(String replayId) {
        DuelReplayMetadata meta = metadataCache.get(replayId);
        if (meta != null) return meta;

        File metaFile = new File(DIR, replayId + ".meta");
        if (metaFile.exists()) {
            try (FileInputStream fis = new FileInputStream(metaFile);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                meta = (DuelReplayMetadata) ois.readObject();
                if (meta != null) {
                    metadataCache.put(replayId, meta);
                    return meta;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public List<DuelReplayMetadata> getAllMetadata() {
        List<DuelReplayMetadata> list = new ArrayList<>(metadataCache.values());
        list.sort((a, b) -> Long.compare(b.getStartTime(), a.getStartTime()));
        return list;
    }

    public List<DuelReplayMetadata> getMetadataForPlayer(UUID uuid) {
        List<DuelReplayMetadata> list = new ArrayList<>();
        for (DuelReplayMetadata meta : metadataCache.values()) {
            if (meta.involvesPlayer(uuid)) {
                list.add(meta);
            }
        }
        list.sort((a, b) -> Long.compare(b.getStartTime(), a.getStartTime()));
        return list;
    }

    @Override
    public void saveReplay(Replay replay) {
        if (!DIR.exists()) DIR.mkdirs();

        File file = new File(DIR, replay.getId() + ".replay");

        try {
            if (!file.exists()) file.createNewFile();

            try (FileOutputStream fileOut = new FileOutputStream(file);
                 GZIPOutputStream gOut = new GZIPOutputStream(fileOut);
                 ObjectOutputStream objectOut = new ObjectOutputStream(gOut)) {

                objectOut.writeObject(replay.getData());
                objectOut.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save metadata
        if (replay.getMetadata() != null) {
            saveMetadata(replay.getMetadata());
        }
    }

    public void saveMetadata(DuelReplayMetadata metadata) {
        if (metadata == null || metadata.getReplayId() == null) return;
        if (!DIR.exists()) DIR.mkdirs();

        metadataCache.put(metadata.getReplayId(), metadata);

        File metaFile = new File(DIR, metadata.getReplayId() + ".meta");
        try (FileOutputStream fos = new FileOutputStream(metaFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(metadata);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadReplay(String replayName, Consumer<Replay> consumer) {
        this.pool.execute(new Acceptor<Replay>(consumer) {

            @Override
            public Replay getValue() {
                File file = new File(DIR, replayName + ".replay");

                try (FileInputStream fileIn = new FileInputStream(file);
                     GZIPInputStream gIn = new GZIPInputStream(fileIn);
                     ObjectInputStream objectIn = new ObjectInputStream(gIn)) {

                    ReplayData data = (ReplayData) objectIn.readObject();
                    Replay replay = new Replay(replayName, data);
                    replay.setMetadata(getMetadata(replayName));
                    return replay;

                } catch (ClassNotFoundException | IOException e) {
                    if (!reformatting) e.printStackTrace();
                }

                return null;
            }
        });
    }

    @Override
    public boolean replayExists(String replayName) {
        if (!isValidName(replayName)) return false;

        File file = new File(DIR, replayName + ".replay");
        return file.exists();
    }

    @Override
    public void deleteReplay(String replayName) {
        File file = new File(DIR, replayName + ".replay");
        if (file.exists()) file.delete();

        File metaFile = new File(DIR, replayName + ".meta");
        if (metaFile.exists()) metaFile.delete();

        metadataCache.remove(replayName);
    }

    public void reformatAll() {
        this.reformatting = true;
        if (DIR.exists()) {
            Arrays.stream(DIR.listFiles())
                    .filter(file -> (file.isFile() && file.getName().endsWith(".replay")))
                    .map(File::getName)
                    .collect(Collectors.toList())
                    .forEach(file -> reformat(file.replaceAll("\\.replay", "")));
        }

        this.reformatting = false;
    }

    private void reformat(String replayName) {
        loadReplay(replayName, old -> {
            if (old == null) {
                LogUtils.log("Reformatting: " + replayName);

                try {
                    File file = new File(DIR, replayName + ".replay");

                    FileInputStream fileIn = new FileInputStream(file);
                    ObjectInputStream objectIn = new ObjectInputStream(fileIn);

                    ReplayData data = (ReplayData) objectIn.readObject();

                    objectIn.close();
                    fileIn.close();

                    deleteReplay(replayName);
                    saveReplay(new Replay(replayName, data));

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public List<String> getReplays() {
        List<String> files = new ArrayList<>();

        if (DIR.exists()) {
            for (File file : DIR.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".replay")) {
                    files.add(file.getName().replaceAll("\\.replay", ""));
                }
            }
        }
        return files;
    }

    public Map<String, DuelReplayMetadata> getMetadataCache() {
        return metadataCache;
    }
}
