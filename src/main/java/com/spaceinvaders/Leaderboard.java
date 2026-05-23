package com.spaceinvaders;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Leaderboard {
    private static final String LEADERBOARD_FILENAME = "leaderboard.txt";
    private static final int MAX_ENTRIES = 10;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final List<Entry> entries = new ArrayList<>();

    public Leaderboard() {
        load();
    }

    public synchronized void addScore(int score) {
        addScore(score, false);
    }

    public synchronized void addScore(int score, boolean twoPlayer) {
        if (score < 0) {
            return;
        }
        entries.add(new Entry(score, twoPlayer, LocalDateTime.now()));
        normalizeEntries();
        save();
    }

    public synchronized List<Entry> getEntries() {
        return new ArrayList<>(entries);
    }

    public synchronized List<Entry> getEntries(boolean twoPlayer) {
        List<Entry> filtered = new ArrayList<>();
        for (Entry entry : entries) {
            if (entry.isTwoPlayer() == twoPlayer) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    private void normalizeEntries() {
        Collections.sort(entries);
        List<Entry> normalized = new ArrayList<>();
        int singleCount = 0;
        int twoCount = 0;
        for (Entry entry : entries) {
            if (entry.isTwoPlayer()) {
                if (twoCount < MAX_ENTRIES) {
                    normalized.add(entry);
                    twoCount++;
                }
            } else {
                if (singleCount < MAX_ENTRIES) {
                    normalized.add(entry);
                    singleCount++;
                }
            }
        }
        entries.clear();
        entries.addAll(normalized);
    }

    private void load() {
        Path path = Paths.get(LEADERBOARD_FILENAME);
        if (!Files.exists(path)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 2) {
                    continue;
                }
                int score;
                try {
                    score = Integer.parseInt(parts[0]);
                } catch (NumberFormatException e) {
                    continue;
                }
                boolean twoPlayer = false;
                LocalDateTime timestamp;
                if (parts.length == 2) {
                    timestamp = LocalDateTime.parse(parts[1], TIMESTAMP_FORMAT);
                } else {
                    String type = parts[1].trim();
                    if ("T".equalsIgnoreCase(type) || "2".equals(type) || "TWO".equalsIgnoreCase(type) || "TWO_PLAYER".equalsIgnoreCase(type) || "TWO PLAYER".equalsIgnoreCase(type)) {
                        twoPlayer = true;
                    }
                    timestamp = LocalDateTime.parse(parts[2], TIMESTAMP_FORMAT);
                }
                entries.add(new Entry(score, twoPlayer, timestamp));
            }
            normalizeEntries();
        } catch (IOException ignored) {
        }
    }

    private void save() {
        Path path = Paths.get(LEADERBOARD_FILENAME);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (Entry entry : entries) {
                String typeCode = entry.isTwoPlayer() ? "T" : "S";
                writer.write(entry.score + "|" + typeCode + "|" + entry.timestamp.format(TIMESTAMP_FORMAT));
                writer.newLine();
            }
        } catch (IOException ignored) {
        }
    }

    public static class Entry implements Comparable<Entry> {
        private final int score;
        private final LocalDateTime timestamp;

        private final boolean twoPlayer;

        public Entry(int score, boolean twoPlayer, LocalDateTime timestamp) {
            this.score = score;
            this.twoPlayer = twoPlayer;
            this.timestamp = timestamp;
        }

        public int getScore() {
            return score;
        }

        public boolean isTwoPlayer() {
            return twoPlayer;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        @Override
        public int compareTo(Entry other) {
            return Integer.compare(other.score, score);
        }
    }
}
