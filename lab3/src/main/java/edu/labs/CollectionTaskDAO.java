package edu.labs;

import java.util.*;
public final class CollectionTaskDAO implements TaskDAO {
    private final Map<UUID, Task> tasks = new LinkedHashMap<>();
    public List<Task> findAll() { return List.copyOf(tasks.values()); }
    public void upsert(Task task) { tasks.put(task.id(), task); }
    public void replaceAll(List<Task> values) { tasks.clear(); values.forEach(this::upsert); }
}
