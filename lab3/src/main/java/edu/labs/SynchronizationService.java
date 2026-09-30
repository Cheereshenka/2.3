package edu.labs;

import java.util.*;
public final class SynchronizationService {
    private final List<TaskDAO> sources;
    public SynchronizationService(List<TaskDAO> sources) { this.sources = List.copyOf(sources); }
    public int synchronize() {
        AppLogger.LOG.info("Начало синхронизации");
        Map<UUID, Task> merged = new TreeMap<>();
        // Сначала читаем все источники: ошибка чтения не вызывает частичную запись.
        for (TaskDAO source : sources) for (Task task : source.findAll()) {
            Task old = merged.get(task.id());
            if (old == null || task.updatedAt().isAfter(old.updatedAt()) || (task.updatedAt().equals(old.updatedAt()) && task.deleted())) merged.put(task.id(), task);
        }
        List<Task> result = List.copyOf(merged.values());
        for (TaskDAO source : sources) source.replaceAll(result);
        AppLogger.LOG.info("Синхронизация завершена: " + result.size() + " записей");
        return (int) result.stream().filter(t -> !t.deleted()).count();
    }
}
