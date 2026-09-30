package edu.labs;

import java.time.*;
import java.util.*;
public final class TaskService {
    private final TaskDAO dao;
    public TaskService(TaskDAO dao) { this.dao = dao; }
    public List<Task> find(String query, Task.Status status, Task.Category category) {
        String q = query == null ? "" : query.toLowerCase(Locale.ROOT).trim();
        return dao.findAll().stream().filter(t -> !t.deleted())
            .filter(t -> (t.title() + " " + t.executor() + " " + t.description()).toLowerCase(Locale.ROOT).contains(q))
            .filter(t -> status == null || t.status() == status).filter(t -> category == null || t.category() == category)
            .sorted(Comparator.comparing(Task::deadline).thenComparing(Task::title)).toList();
    }
    public Task save(Task original, String title, String description, String executor, LocalDate deadline, Task.Status status, Task.Category category) {
        Task task = original == null ? new Task(UUID.randomUUID(), title, description, executor, deadline, status, category, Instant.now(), false)
            : original.revised(title, description, executor, deadline, status, category, false);
        dao.upsert(task); AppLogger.LOG.info("Сохранена задача " + task.id()); return task;
    }
    public void delete(Task task) {
        Objects.requireNonNull(task, "Выберите задачу");
        dao.upsert(task.revised(task.title(), task.description(), task.executor(), task.deadline(), task.status(), task.category(), true));
        AppLogger.LOG.info("Удалена задача " + task.id());
    }
    public void complete(Task task) {
        Objects.requireNonNull(task, "Выберите задачу");
        save(task, task.title(), task.description(), task.executor(), task.deadline(), Task.Status.DONE, task.category());
    }
}
