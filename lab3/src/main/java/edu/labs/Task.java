package edu.labs;

import java.time.*;
import java.util.UUID;
public record Task(UUID id, String title, String description, String executor, LocalDate deadline,
                   Status status, Category category, Instant updatedAt, boolean deleted) {
    public enum Status {
        NEW("Новая"), IN_PROGRESS("В работе"), DONE("Выполнена");
        private final String label; Status(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }
    public enum Category {
        WORK("Работа"), STUDY("Учёба"), PERSONAL("Личное");
        private final String label; Category(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }
    public Task {
        if (id == null || updatedAt == null || status == null || category == null || deadline == null)
            throw new IllegalArgumentException("Заполните дату, статус и категорию");
        if (title == null || title.isBlank() || executor == null || executor.isBlank())
            throw new IllegalArgumentException("Название и исполнитель обязательны");
        title = title.trim(); executor = executor.trim(); description = description == null ? "" : description;
    }
    public Task revised(String title, String description, String executor, LocalDate deadline, Status status, Category category, boolean deleted) {
        Instant now = Instant.now(); if (!now.isAfter(updatedAt)) now = updatedAt.plusNanos(1);
        return new Task(id, title, description, executor, deadline, status, category, now, deleted);
    }
}
