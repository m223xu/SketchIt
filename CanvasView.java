import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;



public class CanvasView {
    private GridPane gridPane;
    private Presenter presenter;
    private Pane drawArea;

    CanvasView(GridPane gridPane, Presenter presenter) {
        this.gridPane = gridPane;
        this.presenter = presenter;
        this.drawArea = new Pane();
        drawArea.setMinSize(presenter.getValue("CANVAS_WIDTH"),presenter.getValue("CANVAS_HEIGHT"));
        drawArea.setMaxSize(presenter.getValue("CANVAS_WIDTH"),presenter.getValue("CANVAS_HEIGHT"));
        this.drawArea.setStyle("-fx-border-color: black;" +
                             "-fx-border-width: 2;");
        this.gridPane.add(drawArea,1,1,2,2);

        drawArea.setOnMousePressed(presenter::startDrag);
        drawArea.setOnMouseDragged(presenter::drag);
        drawArea.setOnMouseReleased(presenter::endDrag);
        gridPane.setOnKeyPressed(presenter::keyPress);
    }


    public void resize(double dw, double dh) {
        drawArea.setMinSize(drawArea.getMinWidth() + dw,drawArea.getMinHeight() + dh);
        drawArea.setMaxSize(drawArea.getMaxWidth() + dw,drawArea.getMaxHeight() + dh);
    }

    public void draw(Shape shape) {
        drawArea.getChildren().add(shape);
    }

    public void undraw(Shape shape) {
        drawArea.getChildren().remove(shape);
    }

    public double getWidth() {
        return drawArea.getMaxWidth();
    }

    public double getHeight() {
        return drawArea.getMaxHeight();
    }
}
