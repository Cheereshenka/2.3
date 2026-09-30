package edu.labs;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
public final class FileTaskDAO implements TaskDAO {
    private final Path path;
    public FileTaskDAO(Path path) { this.path = path.toAbsolutePath(); }
    public List<Task> findAll() {
        if (!Files.exists(path)) return List.of();
        Properties p = new Properties();
        try (Reader in = Files.newBufferedReader(path)) {
            p.load(in); List<Task> tasks = new ArrayList<>();
            int count = Integer.parseInt(p.getProperty("count"));
            if (count < 0 || count > 1000000) throw new IllegalArgumentException("Некорректное количество задач");
            for (int i = 0; i < count; i++) {
                String k = i + ".";
                tasks.add(new Task(UUID.fromString(p.getProperty(k + "id")), p.getProperty(k + "title"), p.getProperty(k + "description"),
                    p.getProperty(k + "executor"), LocalDate.parse(p.getProperty(k + "deadline")), Task.Status.valueOf(p.getProperty(k + "status")),
                    Task.Category.valueOf(p.getProperty(k + "category")), Instant.parse(p.getProperty(k + "updated")), Boolean.parseBoolean(p.getProperty(k + "deleted"))));
            }
            return tasks;
        } catch (Exception e) { throw new IllegalStateException("Ошибка чтения " + path + ": " + e.getMessage(), e); }
    }
    public void upsert(Task task) {
        List<Task> tasks = new ArrayList<>(findAll()); tasks.removeIf(t -> t.id().equals(task.id())); tasks.add(task); replaceAll(tasks);
    }
    public void replaceAll(List<Task> tasks) {
        Properties p = new Properties(); p.setProperty("count", "" + tasks.size());
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i); String k = i + ".";
            p.setProperty(k + "id", t.id().toString()); p.setProperty(k + "title", t.title()); p.setProperty(k + "description", t.description());
            p.setProperty(k + "executor", t.executor()); p.setProperty(k + "deadline", t.deadline().toString()); p.setProperty(k + "status", t.status().name());
            p.setProperty(k + "category", t.category().name()); p.setProperty(k + "updated", t.updatedAt().toString()); p.setProperty(k + "deleted", "" + t.deleted());
        }
        Path temp = null;
        try {
            Files.createDirectories(path.getParent()); temp = Files.createTempFile(path.getParent(), "tasks-", ".tmp");
            try (Writer out = Files.newBufferedWriter(temp)) { p.store(out, "Organizer v1"); }
            try { Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) { throw new IllegalStateException("Ошибка записи " + path, e); }
        finally { if (temp != null) try { Files.deleteIfExists(temp); } catch (IOException ignored) {} }
    }
}
