package edu.labs;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.function.Function;

public class App extends Application {
    private final Map<String, TaskDAO> sources = new LinkedHashMap<>();
    private TaskService service;
    private final TableView<Task> table = new TableView<>();
    private final TextField title = new TextField(), executor = new TextField(), search = new TextField();
    private final TextArea description = new TextArea();
    private final DatePicker deadline = new DatePicker(LocalDate.now().plusDays(1));
    private final ComboBox<Task.Status> state = new ComboBox<>(), stateFilter = new ComboBox<>();
    private final ComboBox<Task.Category> category = new ComboBox<>(), categoryFilter = new ComboBox<>();
    private final ComboBox<String> source = new ComboBox<>();
    private final Label status = new Label("Готово"), summary = Ui.label("", "section");
    @Override public void start(Stage stage) throws Exception {
        AppLogger.initialize(); Properties config = new Properties();
        try (var in = getClass().getResourceAsStream("/config.properties")) { config.load(in); }
            Path customConfig = Path.of(System.getProperty("organizer.config", "config.properties"));
            if (Files.exists(customConfig)) try (var in = Files.newInputStream(customConfig)) { config.load(in); }
        sources.put("collection", new CollectionDAOFactory().createTaskDAO());
        sources.put("file", new FileDAOFactory(Path.of(config.getProperty("file.path"))).createTaskDAO());
        sources.put("database", new DatabaseDAOFactory(Path.of(config.getProperty("database.path"))).createTaskDAO());
        String initial = config.getProperty("datasource"); if (!sources.containsKey(initial)) throw new IllegalArgumentException("Неизвестный datasource: " + initial);
        service = new TaskService(sources.get(initial));
        source.getItems().addAll(sources.keySet()); source.setValue(initial);
        source.setOnAction(e -> Ui.guard(() -> { service = new TaskService(sources.get(source.getValue())); clear(); refresh(); }));
        state.getItems().setAll(Task.Status.values()); state.setValue(Task.Status.NEW);
        category.getItems().setAll(Task.Category.values()); category.setValue(Task.Category.STUDY);
        stateFilter.getItems().add(null); stateFilter.getItems().addAll(Task.Status.values()); stateFilter.setPromptText("Все статусы");
        categoryFilter.getItems().add(null); categoryFilter.getItems().addAll(Task.Category.values()); categoryFilter.setPromptText("Все категории");
        search.setPromptText("Название, исполнитель, описание"); search.textProperty().addListener((o,a,b) -> Ui.guard(this::refresh));
        stateFilter.setOnAction(e -> Ui.guard(this::refresh)); categoryFilter.setOnAction(e -> Ui.guard(this::refresh));
        column("Название", Task::title, 220); column("Исполнитель", Task::executor, 140); column("Срок", t -> t.deadline().toString(), 110);
        column("Статус", t -> t.status().toString(), 120); column("Категория", t -> t.category().toString(), 100);
        table.setPlaceholder(new Label("Пока нет задач. Добавьте первую справа."));
        table.setRowFactory(v -> new TableRow<>() {
            @Override protected void updateItem(Task t, boolean empty) {
                super.updateItem(t, empty); setStyle(!empty && t != null && t.status() != Task.Status.DONE && t.deadline().isBefore(LocalDate.now()) ? "-fx-background-color: #fff0e3; -fx-text-background-color: #713b12;" : "");
            }
        });
        table.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> { if (b != null) fill(b); });
            description.setPrefRowCount(2); description.setWrapText(true);
        Button delete = Ui.button("Удалить", () -> { service.delete(selected()); clear(); refresh(); }); delete.getStyleClass().add("danger");
            Button sync = Ui.button("Синхронизировать все", () -> { int n = new SynchronizationService(new ArrayList<>(sources.values())).synchronize(); clear(); refresh(); status.setText("Синхронизировано: " + n + " активных задач"); });
            VBox left = Ui.card(summary, new HBox(10, new Label("Хранилище:"), source, sync), search, new HBox(10, stateFilter, categoryFilter, Ui.button("Сброс", () -> { search.clear(); stateFilter.setValue(null); categoryFilter.setValue(null); refresh(); })), table,
            new HBox(10, Ui.button("Выполнено", () -> { service.complete(selected()); clear(); refresh(); }), delete));
        VBox.setVgrow(table, Priority.ALWAYS); HBox.setHgrow(left, Priority.ALWAYS);
        VBox form = Ui.card(Ui.label("Редактор задачи", "section"), Ui.field("Название", title), Ui.field("Описание", description), Ui.field("Исполнитель", executor),
            Ui.field("Срок", deadline), new HBox(10, state, category),
            Ui.primary("Сохранить задачу", () -> { service.save(table.getSelectionModel().getSelectedItem(), title.getText(), description.getText(), executor.getText(), deadline.getValue(), state.getValue(), category.getValue()); clear(); refresh(); status.setText("Задача сохранена в " + source.getValue()); }),
                Ui.button("Новая / очистить форму", this::clear), new Separator(),
            Ui.label("Оранжевым отмечены просроченные задачи. Нажмите заголовок столбца для сортировки.", "muted"));
        form.setPrefWidth(320); form.setMinWidth(300);
        ScrollPane formScroll = new ScrollPane(form); formScroll.setFitToWidth(true); formScroll.setPrefWidth(350); formScroll.setMinWidth(330);
        Ui.show(stage, "Органайзер", "Лабораторная 03 / DAO · Abstract Factory · Синхронизация", new HBox(20, left, formScroll), status); refresh();
    }
    private void column(String name, Function<Task,String> getter, double width) {
        TableColumn<Task,String> c = new TableColumn<>(name); c.setCellValueFactory(v -> new ReadOnlyStringWrapper(getter.apply(v.getValue()))); c.setPrefWidth(width); table.getColumns().add(c);
    }
    private Task selected() { Task t = table.getSelectionModel().getSelectedItem(); if (t == null) throw new IllegalArgumentException("Выберите задачу в таблице"); return t; }
    private void fill(Task t) { title.setText(t.title()); description.setText(t.description()); executor.setText(t.executor()); deadline.setValue(t.deadline()); state.setValue(t.status()); category.setValue(t.category()); }
    private void clear() { table.getSelectionModel().clearSelection(); title.clear(); description.clear(); executor.clear(); deadline.setValue(LocalDate.now().plusDays(1)); state.setValue(Task.Status.NEW); category.setValue(Task.Category.STUDY); }
    private void refresh() {
        var tasks = service.find(search.getText(), stateFilter.getValue(), categoryFilter.getValue()); table.getItems().setAll(tasks); table.sort();
        summary.setText("Задач: " + tasks.size() + " · Завершено: " + tasks.stream().filter(t -> t.status() == Task.Status.DONE).count());
    }
    public static void main(String[] args) { launch(args); }
}
