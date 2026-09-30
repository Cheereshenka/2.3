package edu.labs;

public final class CollectionDAOFactory implements DAOFactory {
    public TaskDAO createTaskDAO() { return new CollectionTaskDAO(); }
}
