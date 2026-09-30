package edu.labs;

import java.nio.file.*;
import java.sql.*;
import java.time.*;
import java.util.*;
public final class DatabaseTaskDAO implements TaskDAO {
    private final String url;
    public DatabaseTaskDAO(Path path) {
        try { Files.createDirectories(path.toAbsolutePath().getParent()); } catch (Exception e) { throw new IllegalStateException("Не удалось создать папку базы", e); }
        url = "jdbc:sqlite:" + path.toAbsolutePath();
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS tasks (id TEXT PRIMARY KEY, title TEXT NOT NULL, description TEXT, executor TEXT NOT NULL, deadline TEXT NOT NULL, status TEXT NOT NULL, category TEXT NOT NULL, updated TEXT NOT NULL, deleted INTEGER NOT NULL)");
        } catch (SQLException e) { throw error(e); }
    }
    private Connection connect() throws SQLException { return DriverManager.getConnection(url); }
    private RuntimeException error(Exception e) { return new IllegalStateException("Ошибка SQLite: " + e.getMessage(), e); }
    public List<Task> findAll() {
        try (Connection c = connect(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT * FROM tasks ORDER BY id")) {
            List<Task> tasks = new ArrayList<>();
            while (r.next()) tasks.add(new Task(UUID.fromString(r.getString("id")), r.getString("title"), r.getString("description"), r.getString("executor"), LocalDate.parse(r.getString("deadline")), Task.Status.valueOf(r.getString("status")), Task.Category.valueOf(r.getString("category")), Instant.parse(r.getString("updated")), r.getInt("deleted") != 0));
            return tasks;
        } catch (SQLException e) { throw error(e); }
    }
    private void write(Connection c, Task t) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("INSERT OR REPLACE INTO tasks VALUES (?,?,?,?,?,?,?,?,?)")) {
            s.setString(1, t.id().toString()); s.setString(2, t.title()); s.setString(3, t.description()); s.setString(4, t.executor());
            s.setString(5, t.deadline().toString()); s.setString(6, t.status().name()); s.setString(7, t.category().name()); s.setString(8, t.updatedAt().toString()); s.setInt(9, t.deleted() ? 1 : 0); s.executeUpdate();
        }
    }
    public void upsert(Task task) { try (Connection c = connect()) { write(c, task); } catch (SQLException e) { throw error(e); } }
    public void replaceAll(List<Task> tasks) {
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                try (Statement s = c.createStatement()) { s.executeUpdate("DELETE FROM tasks"); }
                for (Task task : tasks) write(c, task); c.commit();
            } catch (SQLException e) { c.rollback(); throw e; }
        } catch (SQLException e) { throw error(e); }
    }
}
