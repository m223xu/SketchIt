import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.lang.Double;

public class Presenter {
    private Stage stage;
    private Model model;
    private MenuBarView menuBarView;
    private ToolbarView toolbarView;
    private CanvasView canvasView;
    private double canvasWidth;
    private double canvasHeight;

    Presenter(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
        this.stage.setTitle("SketchIt");
        this.stage.setMaxWidth(model.getValue("MAX_WIDTH"));
        this.stage.setMaxHeight(model.getValue("MAX_HEIGHT"));
        this.stage.setMinWidth(model.getValue("MIN_WIDTH"));
        this.stage.setMinHeight(model.getValue("MIN_HEIGHT"));
        //this.stage.setWidth(this.stage.getMinWidth());
        //this.stage.setHeight(this.stage.getMinHeight());
        GridPane pane = new GridPane();
        Scene scene = new Scene(pane, model.getValue("MIN_WIDTH"), model.getValue("MIN_HEIGHT"), Color.WHITE);
        this.menuBarView = new MenuBarView(pane, this);
        this.toolbarView = new ToolbarView(pane, this);
        this.canvasView = new CanvasView(pane, this);
        this.model.addPresenter(this);
        this.canvasWidth = canvasView.getWidth();
        this.canvasHeight = canvasView.getHeight();

        stage.setScene(scene);
    }

    public void start() {
        //this.pane.setGridLinesVisible(true);

        stage.widthProperty().addListener((observableValue, oldVal, newVal) -> {
            if (Double.isNaN(oldVal.doubleValue())) {
                return;
            }
            canvasView.resize((double)newVal-(double)oldVal,0);
            canvasWidth = canvasView.getWidth();
            model.updateCanvasWidth();
        });
        stage.heightProperty().addListener((observableValue, oldVal, newVal) -> {
            if (Double.isNaN(oldVal.doubleValue())) {
                return;
            }
            canvasView.resize(0, (double)newVal-(double)oldVal);
            canvasHeight = canvasView.getHeight();
            model.updateCanvasHeight();
        });
        stage.show();
    }

    public void draw(Shape shape) {
        canvasView.draw(shape);
    }

    public void undraw(Shape shape) {
        canvasView.undraw(shape);
    }

    public void updateToolBar(final Shape shape) {
        toolbarView.update(shape);
    }

    public String saveAlert() {
        return ConfirmBox.Display("Do you want to save the \n changes you have made?");
    }

    public void displayMessage(String message) {
        MessageBox.Display(message);
    }

    public File fileChooserDialog(String mode) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setResizable(false);

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        switch (mode) {
            case "Save":
                return fileChooser.showSaveDialog(window);
            case "Load":
                return fileChooser.showOpenDialog(window);
            default:
                return null;
        }

    }

    public double getCanvasWidth() {
        return canvasWidth;
    }
    public double getCanvasHeight() {
        return canvasHeight;
    }

    public double getValue(String key) {
        return model.getValue(key);
    }

    public void startDrag(MouseEvent mousePress) {
        model.startDrag(mousePress);
    }

    public void drag(MouseEvent mouseDrag) {
        model.drag(mouseDrag);
    }

    public void endDrag(MouseEvent mouseRelease) {
        model.endDrag(mouseRelease);
    }

    public void keyPress(KeyEvent keyPress) {
        model.keyPress(keyPress);
    }

    public void setCurTool(Tool tool) {
        model.setCurTool(tool);
    }

    public void setCurLineColor(Color color) {
        model.setCurLineColor(color);
    }

    public void setCurFillColor(Color color) {
        model.setCurFillColor(color);
    }

    public void _new() {
        model._new();
    }

    public void load() {
        model.load();
    }

    public void save() {
        model.save();
    }

    public void quit() {
        model.quit();
    }

    public void setCurLineWidth(double width) {
        model.setCurLineWidth(width);
    }

    public void setCurLineStyle(double gap) {
        model.setCurLineStyle(gap);
    }

}

class MessageBox {
    public static void Display(String message) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setResizable(false);

        Label msg = new Label(message);
        Button okButton = new Button("Ok");
        okButton.setOnAction(event -> window.close());

        VBox vBox = new VBox(msg,okButton);
        vBox.setSpacing(10);
        vBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vBox, 200, 150);
        window.setScene(scene);
        window.showAndWait();
    }
}

class ConfirmBox {
    static String answer;

    public static String Display(String message) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setResizable(false);

        Label label = new Label(message);
        label.setWrapText(true);
        Button yesButton= new Button("Yes");
        yesButton.setOnAction(event -> {
            answer = "Yes";
            window.close();
        });
        Button noButton= new Button("No");
        noButton.setOnAction(event -> {
            answer = "No";
            window.close();
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> {
            answer = "Cancel";
            window.close();
        });

        HBox hBox = new HBox();
        hBox.getChildren().addAll(yesButton,noButton,cancelButton);
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(label, hBox);
        vBox.setSpacing(30);
        vBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vBox, 200, 150);
        window.setScene(scene);
        window.showAndWait();
        return answer;
    }
}
