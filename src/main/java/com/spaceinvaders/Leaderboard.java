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
        if (score < 0) {
            return;
        }
        entries.add(new Entry(score, LocalDateTime.now()));
        Collections.sort(entries);
        if (entries.size() > MAX_ENTRIES) {
            entries.subList(MAX_ENTRIES, entries.size()).clear();
        }
        save();
    }

    public synchronized List<Entry> getEntries() {
        return new ArrayList<>(entries);
    }

    private void load() {
        Path path = Paths.get(LEADERBOARD_FILENAME);
        if (!Files.exists(path)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 2);
                if (parts.length != 2) {
                    continue;
                }
                int score;
                try {
                    score = Integer.parseInt(parts[0]);
                } catch (NumberFormatException e) {
                    continue;
                }
                LocalDateTime timestamp;
                try {
                    timestamp = LocalDateTime.parse(parts[1], TIMESTAMP_FORMAT);
                } catch (Exception e) {
                    continue;
                }
                entries.add(new Entry(score, timestamp));
            }
            Collections.sort(entries);
            if (entries.size() > MAX_ENTRIES) {
                entries.subList(MAX_ENTRIES, entries.size()).clear();
            }
        } catch (IOException ignored) {
        }
    }

    private void save() {
        Path path = Paths.get(LEADERBOARD_FILENAME);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (Entry entry : entries) {
                writer.write(entry.score + "|" + entry.timestamp.format(TIMESTAMP_FORMAT));
                writer.newLine();
            }
        } catch (IOException ignored) {
        }
    }

    public static class Entry implements Comparable<Entry> {
        private final int score;
        private final LocalDateTime timestamp;

        public Entry(int score, LocalDateTime timestamp) {
            this.score = score;
            this.timestamp = timestamp;
        }

        public int getScore() {
            return score;
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
