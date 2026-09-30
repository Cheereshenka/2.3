package edu.labs;

import java.nio.file.*;
import java.util.logging.*;
public final class AppLogger {
    public static final Logger LOG = Logger.getLogger("organizer");
    public static void initialize() {
        try {
            Files.createDirectories(Path.of("logs"));
            FileHandler file = new FileHandler("logs/organizer-%g.log", 1000000, 3, true);
            file.setEncoding("UTF-8"); file.setFormatter(new SimpleFormatter()); LOG.addHandler(file);
        } catch (Exception e) { throw new IllegalStateException("Не удалось открыть журнал", e); }
    }
}
