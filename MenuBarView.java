import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;

public class MenuBarView {
    MenuBar menuBar;
    Presenter presenter;

    MenuBarView(GridPane pane, Presenter presenter) {
        this.menuBar = new MenuBar();
        this.presenter = presenter;
        pane.add(menuBar,0,0);
        initMenus();
    }

    private void initMenus() {
        Menu fileMenu = new Menu("File");
        menuBar.getMenus().add(fileMenu);
        MenuItem _new = createMenuItem("New",
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        MenuItem load = createMenuItem("Load",
                new KeyCodeCombination(KeyCode.L,KeyCombination.CONTROL_DOWN));
        MenuItem save = createMenuItem("Save",
                new KeyCodeCombination(KeyCode.S,KeyCombination.CONTROL_DOWN));
        MenuItem quit = createMenuItem("Quit",
                new KeyCodeCombination(KeyCode.Q,KeyCombination.CONTROL_DOWN));
        fileMenu.getItems().addAll(_new, load, save, quit);

        Menu editMenu = new Menu("Edit");
        menuBar.getMenus().add(editMenu);
        MenuItem undo = createMenuItem("Undo",
                new KeyCodeCombination(KeyCode.Y,KeyCombination.CONTROL_DOWN));
        MenuItem redo = createMenuItem("Redo",
                new KeyCodeCombination(KeyCode.Z,KeyCombination.CONTROL_DOWN));
        editMenu.getItems().addAll(undo,redo);
    }

    private MenuItem createMenuItem(String name, KeyCodeCombination shortCut) {
        MenuItem item = new MenuItem(name);
        if (shortCut != null) {
            item.setAccelerator(shortCut);
        }
        switch(name) {
            case "New":
                item.setOnAction(actionEvent -> presenter._new());
                break;
            case "Load":
                item.setOnAction(actionEvent -> presenter.load());
                break;
            case "Save":
                item.setOnAction(actionEvent -> presenter.save());
                break;
            case "Quit":
                item.setOnAction(actionEvent -> presenter.quit());
                break;
        }
        return item;
    }
}
