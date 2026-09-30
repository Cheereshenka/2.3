package edu.labs;

import java.nio.file.Path;
public record FileDAOFactory(Path path) implements DAOFactory {
    public TaskDAO createTaskDAO() { return new FileTaskDAO(path); }
}
