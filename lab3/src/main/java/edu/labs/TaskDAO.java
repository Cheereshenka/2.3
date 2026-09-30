package edu.labs;

import java.util.*;
public interface TaskDAO {
    /** Включает метки удаления, необходимые для синхронизации. */
    List<Task> findAll();
    void upsert(Task task);
    void replaceAll(List<Task> tasks);
    default Optional<Task> findById(UUID id) { return findAll().stream().filter(t -> t.id().equals(id)).findFirst(); }
}
