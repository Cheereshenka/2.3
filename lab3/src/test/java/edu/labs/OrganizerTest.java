package edu.labs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class OrganizerTest {
    @TempDir Path dir;
    private Task task() { return new Task(UUID.randomUUID(), "Задача", "Описание", "Анна", LocalDate.now(), Task.Status.NEW, Task.Category.STUDY, Instant.now(), false); }
    @Test void serviceUsesDaoAndValidatesBeforeWriting() {
        TaskDAO dao = mock(TaskDAO.class); TaskService s = new TaskService(dao);
        assertThrows(IllegalArgumentException.class, () -> s.save(null, "", "", "А", LocalDate.now(), Task.Status.NEW, Task.Category.WORK)); verifyNoInteractions(dao);
        Task result = s.save(null, "Тест", "", "А", LocalDate.now(), Task.Status.NEW, Task.Category.WORK); verify(dao).upsert(result);
    }
    @Test void filtersComposeAndDeletionIsHidden() {
        TaskDAO dao = mock(TaskDAO.class); Task a = task(); Task b = a.revised("Удалена", "", "Анна", a.deadline(), a.status(), a.category(), true);
        when(dao.findAll()).thenReturn(List.of(a, b));
        assertEquals(List.of(a), new TaskService(dao).find("анна", Task.Status.NEW, Task.Category.STUDY));
        assertTrue(new TaskService(dao).find("анна", Task.Status.DONE, null).isEmpty());
    }
    @Test void allStoresRoundTripAndSyncDoesNotResurrectDeletedTasks() {
        TaskDAO memory = new CollectionTaskDAO(), file = new FileTaskDAO(dir.resolve("tasks.properties")), db = new DatabaseTaskDAO(dir.resolve("tasks.db"));
        Task a = task(); memory.upsert(a); SynchronizationService sync = new SynchronizationService(List.of(memory, file, db));
        assertEquals(1, sync.synchronize()); assertEquals(a, file.findAll().get(0)); assertEquals(a, db.findAll().get(0));
        new TaskService(file).delete(a); assertEquals(0, sync.synchronize());
        assertTrue(memory.findAll().get(0).deleted()); assertTrue(db.findAll().get(0).deleted());
        assertEquals(0, sync.synchronize());
    }
}
