package edu.labs;

import java.nio.file.Path;
public record DatabaseDAOFactory(Path path) implements DAOFactory {
    public TaskDAO createTaskDAO() { return new DatabaseTaskDAO(path); }
}
