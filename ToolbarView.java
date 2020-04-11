import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;


public class ToolbarView {
    GridPane pane;
    Presenter presenter;
    GridPane tools;
    ToggleGroup toolGroup;
    VBox attributes;
    ColorPicker lineColor;
    ColorPicker fillColor;
    ToggleGroup lineWidth;
    ToggleGroup lineStyle;


    ToolbarView(GridPane pane, Presenter presenter) {
        this.pane = pane;
        this.presenter = presenter;
        this.tools = new GridPane();
        this.attributes = new VBox();
        pane.add(tools,0,1);
        pane.add(attributes, 0,2);
        this.initTools();
        this.initAttributes();
    }

    private void initTools() {
        toolGroup = new ToggleGroup();
        ToggleButton selection = createTool("Select");
        presenter.setCurTool(Tool.SELECTION);
        selection.setOnAction(actionEvent -> presenter.setCurTool(Tool.SELECTION));
        tools.add(selection,0,0);
        selection.setToggleGroup(toolGroup);
        selection.setSelected(true);

        ToggleButton erase = createTool("Erase");
        erase.setOnAction(actionEvent -> presenter.setCurTool(Tool.ERASE));
        tools.add(erase,1,0);
        erase.setToggleGroup(toolGroup);

        ToggleButton line = createTool("Line");
        line.setOnAction(actionEvent -> presenter.setCurTool(Tool.LINE));
        tools.add(line,0,1);
        line.setToggleGroup(toolGroup);

        ToggleButton ellipse = createTool("Circle");
        ellipse.setOnAction(actionEvent -> presenter.setCurTool(Tool.ELLIPSE));
        tools.add(ellipse,1,1);
        ellipse.setToggleGroup(toolGroup);

        ToggleButton rectangle = createTool("Rectangle");
        rectangle.setOnAction(actionEvent -> presenter.setCurTool(Tool.RECTANGLE));
        tools.add(rectangle,0,2);
        rectangle.setToggleGroup(toolGroup);

        ToggleButton fill = createTool("Fill");
        fill.setOnAction(actionEvent -> presenter.setCurTool(Tool.FILL));
        tools.add(fill,1,2);
        fill.setToggleGroup(toolGroup);
    }

    private void initAttributes() {
        Label lineColorLabel = new Label("Line colour:");
        lineColor = new ColorPicker(Color.BLUEVIOLET);
        presenter.setCurLineColor(lineColor.getValue());
        lineColor.setOnAction(actionEvent -> presenter.setCurLineColor(lineColor.getValue()));
        Label fillColorLabel = new Label("Fill colour:");
        fillColor = new ColorPicker(Color.CORAL);
        presenter.setCurFillColor(fillColor.getValue());
        fillColor.setOnAction(actionEvent -> presenter.setCurFillColor(fillColor.getValue()));

        Label lineWidthLabel = new Label("Line width:");
        lineWidth = new ToggleGroup();

        ToggleButton thin = new ToggleButton();
        thin.setOnAction(actionEvent -> presenter.setCurLineWidth(1));
        thin.setToggleGroup(lineWidth);
        thin.setMaxWidth(Double.MAX_VALUE);
        thin.setGraphic(new ImageView(new Image("image/thin.png")));
        thin.setSelected(true);
        presenter.setCurLineWidth(1);

        ToggleButton medium = new ToggleButton();
        medium.setOnAction(actionEvent -> presenter.setCurLineWidth(3));
        medium.setToggleGroup(lineWidth);
        medium.setMaxWidth(Double.MAX_VALUE);
        medium.setGraphic(new ImageView(new Image("image/medium.png")));

        ToggleButton thick = new ToggleButton();
        thick.setOnAction(actionEvent -> presenter.setCurLineWidth(5));
        thick.setToggleGroup(lineWidth);
        thick.setMaxWidth(Double.MAX_VALUE);
        thick.setGraphic(new ImageView(new Image("image/thick.png")));

        //width = new HBox(thin,medium,thick);

        Label lineStyleLabel = new Label("Line style:");
        lineStyle = new ToggleGroup();

        ToggleButton straight = new ToggleButton();
        straight.setOnAction(actionEvent -> presenter.setCurLineStyle(0));
        straight.setToggleGroup(lineStyle);
        straight.setMaxWidth(Double.MAX_VALUE);
        straight.setGraphic(new ImageView(new Image("image/thin.png")));
        straight.setSelected(true);
        presenter.setCurLineStyle(0);

        ToggleButton shortDotted = new ToggleButton();
        shortDotted.setOnAction(actionEvent -> presenter.setCurLineStyle(10));
        shortDotted.setToggleGroup(lineStyle);
        shortDotted.setMaxWidth(Double.MAX_VALUE);
        shortDotted.setGraphic(new ImageView(new Image("image/short.png")));

        ToggleButton dotted = new ToggleButton();
        dotted.setOnAction(actionEvent -> presenter.setCurLineStyle(20));
        dotted.setToggleGroup(lineStyle);
        dotted.setMaxWidth(Double.MAX_VALUE);
        dotted.setGraphic(new ImageView(new Image("image/long.png")));

        //style = new HBox(straight,shortDotted,dotted);

        attributes.getChildren().addAll(lineColorLabel,lineColor,fillColorLabel,fillColor,
                lineWidthLabel,thin,medium,thick,
                lineStyleLabel,straight,shortDotted,dotted);
        attributes.setAlignment(Pos.TOP_CENTER);
    }

    private ToggleButton createTool(String name) {
        ToggleButton tool = new ToggleButton(name);
        // tool.setGraphic();
        tool.setPrefHeight(50.0);
        tool.setMaxWidth(Double.MAX_VALUE);
        return tool;
    }

    void update(final Shape shape) {
        if (shape instanceof Line) {
            lineColor.setValue((Color) shape.getStroke());
            presenter.setCurLineColor((Color) shape.getStroke());

        }
        else if (shape instanceof Rectangle || shape instanceof Ellipse) {
            lineColor.setValue((Color) shape.getStroke());
            presenter.setCurLineColor((Color) shape.getStroke());
            fillColor.setValue((Color) shape.getFill());
            presenter.setCurFillColor((Color) shape.getFill());

        }
        else {
            return;
        }

        switch ((int) shape.getStrokeWidth()) {
            case 1:
                lineWidth.getToggles().get(0).setSelected(true);
                presenter.setCurLineWidth(1);
                break;
            case 3:
                lineWidth.getToggles().get(1).setSelected(true);
                presenter.setCurLineWidth(3);
                break;
            case 5:
                lineWidth.getToggles().get(2).setSelected(true);
                presenter.setCurLineWidth(5);
                break;
        }

        if (shape.getStrokeDashArray().size() == 0) {
            lineStyle.getToggles().get(0).setSelected(true);
            presenter.setCurLineStyle(0);
        }
        else if (shape.getStrokeDashArray().get(0).equals(10d)) {
            lineStyle.getToggles().get(1).setSelected(true);
            presenter.setCurLineStyle(10);
        }
        else if (shape.getStrokeDashArray().get(0).equals(20d)) {
            lineStyle.getToggles().get(2).setSelected(true);
            presenter.setCurLineStyle(20);
        }
    }
}
