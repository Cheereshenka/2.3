package edu.labs;

import javafx.application.Application;

/**
 * Точка входа для запуска из IDE через classpath.
 * Обычный класс позволяет JavaFX самостоятельно инициализировать runtime.
 */
public final class Launcher {
    private Launcher() {}

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
