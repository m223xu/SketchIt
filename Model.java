import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;


enum Tool {
    SELECTION,
    ERASE,
    LINE,
    RECTANGLE,
    ELLIPSE,
    FILL
}


public class Model {

    private HashMap<String,Double> settings;
    ArrayList<Shape> shapes;
    private Presenter presenter;
    private boolean modified;
    private double canvasWidth;
    private double canvasHeight;
    private Tool curTool;
    private Color curLineColor;
    private Color curFillColor;
    private double curLineWidth;
    private double curLineStyle;
    private Shape curSelected = null;
    private Shape preview = null;
    private MouseEvent startAction;
    private int dragType = 0;

    public Model() {
        this.shapes = new ArrayList<>();
        this.settings = new HashMap<>();
        this.modified = false;
        this.settings.put("MAX_WIDTH", 1920.0);
        this.settings.put("MAX_HEIGHT",  1440.0);
        this.settings.put("MIN_WIDTH", 640.0);
        this.settings.put("MIN_HEIGHT", 480.0);
        this.settings.put("CANVAS_WIDTH", 500.0);
        this.settings.put("CANVAS_HEIGHT", 440.0);
        this.canvasWidth = 500.0;
        this.canvasHeight = 440.0;
    }

    public double getValue(String key) {
        return settings.get(key);
    }

    public void updateCanvasWidth() {
        double oldWidth = this.canvasWidth;
        this.canvasWidth = this.presenter.getCanvasWidth();
        double newWidth = this.canvasWidth;
        double scale = newWidth/oldWidth;
        for (Shape shape : shapes) {
            if (shape instanceof Line) {
                Line line = (Line) shape;
                line.setStartX(line.getStartX()*scale);
                line.setEndX(line.getEndX()*scale);
            }
            else if (shape instanceof Rectangle) {
                Rectangle rectangle = (Rectangle) shape;
                rectangle.setX(rectangle.getX()*scale);
                rectangle.setWidth(rectangle.getWidth()*scale);
            }
            else if (shape instanceof Ellipse) {
                Ellipse ellipse = (Ellipse) shape;
                ellipse.setCenterX(ellipse.getCenterX()*scale);
                ellipse.setRadiusX(ellipse.getRadiusX()*scale);
            }
        }
    }

    public void updateCanvasHeight() {
        double oldHeight = this.canvasHeight;
        this.canvasHeight = this.presenter.getCanvasHeight();
        double newHeight = this.canvasHeight;
        double scale = newHeight/oldHeight;
        for (Shape shape : shapes) {
            if (shape instanceof Line) {
                Line line = (Line) shape;
                line.setStartY(line.getStartY()*scale);
                line.setEndY(line.getEndY()*scale);
            }
            else if (shape instanceof Rectangle) {
                Rectangle rectangle = (Rectangle) shape;
                rectangle.setY(rectangle.getY()*scale);
                rectangle.setHeight(rectangle.getHeight()*scale);
            }
            else if (shape instanceof Ellipse) {
                Ellipse ellipse = (Ellipse) shape;
                ellipse.setCenterY(ellipse.getCenterY()*scale);
                ellipse.setRadiusY(ellipse.getRadiusY()*scale);
            }
        }
    }

    public void addPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public void addShape(Shape shape) {
        addShapeClicked(shape);
        addShapePressed(shape);
        addShapeDragged(shape);
        addShapeReleased(shape);
        this.shapes.add(shape);
        presenter.draw(shape);
        this.modified = true;
    }

    private void addShapeClicked(Shape shape) {
        shape.setOnMouseClicked(mouseEvent -> {
            switch (curTool) {
                case SELECTION:
                    unselect();
                    select(shape);
                    mouseEvent.consume();
                    break;
                case ERASE:
                    removeShape(shape);
                    mouseEvent.consume();
                    break;
                case FILL:
                    if (shape instanceof Line) {
                        break;
                    }
                    shape.setFill(curFillColor);
                    modified = true;
                    mouseEvent.consume();
                    break;
            }
        });
    }

    private void addShapePressed(Shape shape) {
        shape.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (curTool.equals(Tool.SELECTION)) {
                    mouseEvent.consume();
                    unselect();
                    select(shape);
                    startAction = mouseEvent;
                    if (shape instanceof Line) {
                        Line line = (Line) shape;

                        if (startAction.getX() <= line.getEndX() + 5 && startAction.getY() <= line.getEndY() + 5
                                && startAction.getX() >= line.getEndX() - 5 && startAction.getY() >= line.getEndY() - 5) {
                            dragType = 1;
                        }
                        else if (startAction.getX() <= line.getStartX() + 5 && startAction.getY() <= line.getStartY() + 5
                                && startAction.getX() >= line.getStartX() - 5 && startAction.getY() >= line.getStartY() - 5) {
                            dragType = 2;
                        }
                        else {
                            dragType = 3;
                        }
                    }
                    else if (shape instanceof Rectangle) {
                        Rectangle rectangle = (Rectangle) shape;
                        if (startAction.getX() <= rectangle.getX() + rectangle.getWidth() + 5
                                && startAction.getX() >= rectangle.getX() + rectangle.getWidth() - 5
                                && startAction.getY() <= rectangle.getY() + rectangle.getHeight() + 5
                                && startAction.getY() >= rectangle.getY() + rectangle.getHeight() - 5) {
                            dragType = 1;
                        }
                        else {
                            dragType = 2;
                        }
                    }
                    else if (shape instanceof Ellipse) {
                        Ellipse ellipse = (Ellipse) shape;
                        if (startAction.getX() <= ellipse.getCenterX() + ellipse.getRadiusX() + 5
                                && startAction.getX() >= ellipse.getCenterX() + ellipse.getRadiusX() - 5
                                && startAction.getY() <= ellipse.getCenterY() + 5
                                && startAction.getY() >= ellipse.getCenterY() - 5) {
                            dragType = 1;
                        }
                        else if (startAction.getX() <= ellipse.getCenterX() + 5
                                && startAction.getX() >= ellipse.getCenterX() - 5
                                && startAction.getY() <= ellipse.getCenterY() + ellipse.getRadiusY() + 5
                                && startAction.getY() >= ellipse.getCenterY() + ellipse.getRadiusY() - 5) {
                            dragType = 2;
                        }
                        else {
                            dragType = 3;
                        }
                    }
                }
            }
        });
    }

    private void addShapeDragged(Shape shape) {
        shape.setOnMouseDragged(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (curTool.equals(Tool.SELECTION)) {
                    mouseEvent.consume();
                    preview = shape;
                    double deltaX = mouseEvent.getX() - startAction.getX();
                    double deltaY = mouseEvent.getY() - startAction.getY();
                    startAction = mouseEvent;

                    if (shape instanceof Line) {
                        Line line = (Line) shape;
                        switch (dragType) {
                            case 1:
                                deltaX = Math.max(deltaX, -line.getEndX());
                                deltaX = Math.min(deltaX, canvasWidth - line.getEndX());
                                deltaY = Math.max(deltaY, -line.getEndY());
                                deltaY = Math.min(deltaY, canvasHeight - line.getEndY());

                                line.setEndX(line.getEndX()+deltaX);
                                line.setEndY(line.getEndY()+deltaY);
                                break;
                            case 2:
                                deltaX = Math.max(deltaX, -line.getStartX());
                                deltaX = Math.min(deltaX, canvasWidth - line.getStartX());
                                deltaY = Math.max(deltaY, -line.getStartY());
                                deltaY = Math.min(deltaY, canvasHeight - line.getStartY());

                                line.setStartX(line.getStartX()+deltaX);
                                line.setStartY(line.getStartY()+deltaY);
                                break;
                            case 3:
                                deltaX = Math.max(deltaX, -line.getStartX());
                                deltaX = Math.max(deltaX, -line.getEndX());
                                deltaX = Math.min(deltaX, canvasWidth - line.getStartX());
                                deltaX = Math.min(deltaX, canvasWidth - line.getEndX());
                                deltaY = Math.max(deltaY, -line.getStartY());
                                deltaY = Math.max(deltaY, -line.getEndY());
                                deltaY = Math.min(deltaY, canvasHeight - line.getStartY());
                                deltaY = Math.min(deltaY, canvasHeight - line.getEndY());

                                line.setStartX(line.getStartX()+deltaX);
                                line.setStartY(line.getStartY()+deltaY);
                                line.setEndX(line.getEndX()+deltaX);
                                line.setEndY(line.getEndY()+deltaY);
                                break;
                        }
                    }
                    else if (shape instanceof Rectangle) {
                        Rectangle rectangle = (Rectangle) shape;
                        switch (dragType) {
                            case 1:
                                double width = Math.max(0, mouseEvent.getX() - rectangle.getX());
                                width = Math.min(width, canvasWidth - rectangle.getX());
                                double height = Math.max(0, mouseEvent.getY() - rectangle.getY());
                                height = Math.min(height, canvasHeight - rectangle.getY());

                                rectangle.setWidth(width);
                                rectangle.setHeight(height);
                                break;
                            case 2:
                                deltaX = Math.max(deltaX, -rectangle.getX());
                                deltaX = Math.min(deltaX, canvasWidth - rectangle.getWidth() - rectangle.getX());
                                deltaY = Math.max(deltaY, -rectangle.getY());
                                deltaY = Math.min(deltaY, canvasHeight - rectangle.getHeight() - rectangle.getY());

                                rectangle.setX(rectangle.getX()+deltaX);
                                rectangle.setY(rectangle.getY()+deltaY);
                                break;
                        }
                    }
                    else if (shape instanceof Ellipse) {
                        Ellipse ellipse = (Ellipse) shape;
                        switch (dragType) {
                            case 1:
                                double radiusX = Math.max(0, mouseEvent.getX() - ellipse.getCenterX());
                                radiusX = Math.min(radiusX, canvasWidth - ellipse.getCenterX());
                                radiusX = Math.min(radiusX, ellipse.getCenterX());

                                ellipse.setRadiusX(radiusX);
                                break;
                            case 2:
                                double radiusY = Math.max(0, mouseEvent.getY() - ellipse.getCenterY());
                                radiusY = Math.min(radiusY, canvasHeight - ellipse.getCenterY());
                                radiusY = Math.min(radiusY, ellipse.getCenterY());

                                ellipse.setRadiusY(radiusY);
                                break;
                            case 3:
                                deltaX = Math.max(deltaX,ellipse.getRadiusX()-ellipse.getCenterX());
                                deltaX = Math.min(deltaX,canvasWidth - ellipse.getRadiusX() - ellipse.getCenterX());
                                deltaY = Math.max(deltaY,ellipse.getRadiusY()-ellipse.getCenterY());
                                deltaY = Math.min(deltaY,canvasHeight - ellipse.getRadiusY() - ellipse.getCenterY());

                                ellipse.setCenterX(ellipse.getCenterX()+deltaX);
                                ellipse.setCenterY(ellipse.getCenterY()+deltaY);
                                break;
                        }
                    }
                }
            }
        });
    }

    private void addShapeReleased(Shape shape) {
        shape.setOnMouseReleased(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (curTool.equals(Tool.SELECTION)) {
                    mouseEvent.consume();
                    startAction = null;
                    dragType = 0;
                    preview = null;
                    modified = true;
                }
            }
        });
    }

    public void removeShape(Shape shape) {
        this.shapes.remove(shape);
        presenter.undraw(shape);
        this.modified = true;
    }

    public void setCurTool(Tool tool) {
        this.curTool = tool;
        if (curSelected != null) {
            if (curTool.equals(Tool.SELECTION)) {
                curSelected.setEffect(new DropShadow());
                presenter.updateToolBar(curSelected);
            }
            else {
                unselect();
            }
        }
    }

    public void setCurLineColor(Color color) {
        Color oldColor = this.curLineColor;
        this.curLineColor = color;
        if (curTool.equals(Tool.SELECTION) && curSelected != null) {
            curSelected.setStroke(color);
            if (!oldColor.equals(color)) {
                modified = true;
            }
        }
    }

    public void setCurFillColor(Color color) {
        Color oldColor = this.curFillColor;
        this.curFillColor = color;
        if (curTool.equals(Tool.SELECTION) && curSelected != null) {
            curSelected.setFill(color);
            if (!oldColor.equals(color)) {
                modified = true;
            }
        }
    }

    public void setCurLineWidth(double width) {
        double oldWidth = this.curLineWidth;
        this.curLineWidth = width;
        if (curTool.equals(Tool.SELECTION) && curSelected != null) {
            curSelected.setStrokeWidth(width);
            if (oldWidth != width) {
                modified = true;
            }
        }
    }

    public void setCurLineStyle(double gap) {
        double oldGap = this.curLineStyle;
        this.curLineStyle = gap;
        if (curTool.equals(Tool.SELECTION) && curSelected != null) {
            curSelected.getStrokeDashArray().clear();
            if (gap != 0d) {
                curSelected.getStrokeDashArray().add(gap);
            }
            if (oldGap != gap) {
                modified = true;
            }
        }
    }

    public void startDrag(MouseEvent mousePress) {
        if (mousePress.getButton().equals(MouseButton.PRIMARY)) {
            startAction = mousePress;
            double startX = mousePress.getX();
            double startY = mousePress.getY();
            switch (curTool) {
                case SELECTION:
                case ERASE:
                case FILL:
                    return;
                case LINE:
                    preview = new Line(startX,startY,startX,startY);
                    break;
                case RECTANGLE:
                    preview = new Rectangle(startX,startY,0,0);
                    break;
                case ELLIPSE:
                    preview = new Ellipse(startX,startY,0,0);
                    break;
            }
            Color previewColor = new Color(
                    curLineColor.getRed(), curLineColor.getGreen(), curLineColor.getBlue(),0.2
            );
            preview.setStroke(previewColor);
            preview.setStrokeWidth(curLineWidth);
            if (curLineStyle != 0) {
                preview.getStrokeDashArray().add(curLineStyle);
            }
            previewColor = new Color(
                    curFillColor.getRed(), curFillColor.getGreen(), curFillColor.getBlue(), 0.2
            );
            preview.setFill(previewColor);
            addShape(preview);
        }

    }

    public void drag(MouseEvent mouseDrag) {
        if (mouseDrag.getButton().equals(MouseButton.PRIMARY)) {
            double startX = startAction.getX();
            double startY = startAction.getY();
            double endX;
            double endY;

            switch (curTool) {
                case SELECTION:
                case ERASE:
                case FILL:
                    break;
                case LINE:
                    endX = Math.max(mouseDrag.getX(),0);
                    endX = Math.min(endX,canvasWidth);
                    endY = Math.max(mouseDrag.getY(),0);
                    endY = Math.min(endY,canvasHeight);
                    Line line = (Line) preview;
                    line.setEndX(endX);
                    line.setEndY(endY);
                    break;
                case RECTANGLE:
                    endX = Math.max(mouseDrag.getX(),0);
                    endX = Math.min(endX, canvasWidth);
                    endY = Math.max(mouseDrag.getY(),0);
                    endY = Math.min(endY,canvasHeight);
                    double width = Math.abs(endX - startX);
                    double height = Math.abs(endY - startY);
                    Rectangle rectangle = (Rectangle) preview;
                    rectangle.setX(Math.min(startX,endX));
                    rectangle.setY(Math.min(startY,endY));
                    rectangle.setWidth(width);
                    rectangle.setHeight(height);
                    break;
                case ELLIPSE:
                    endX = Math.max(mouseDrag.getX(),0);
                    endX = Math.max(endX,2*startX-canvasWidth);
                    endX = Math.min(endX,canvasWidth);
                    endX = Math.min(endX,2*startX);
                    endY = Math.max(mouseDrag.getY(),0);
                    endY = Math.max(endY,2*startY-canvasHeight);
                    endY = Math.min(endY,canvasHeight);
                    endY = Math.min(endY,2*startY);
                    double radiusX = Math.abs(endX - startX);
                    double radiusY = Math.abs(endY - startY);
                    Ellipse ellipse = (Ellipse) preview;
                    ellipse.setRadiusX(radiusX);
                    ellipse.setRadiusY(radiusY);
                    break;
            }
        }
    }

    public void endDrag(MouseEvent mouseRelease) {
        if (mouseRelease.getButton().equals(MouseButton.PRIMARY)) {
            if (preview == null) {
                return;
            }
            switch (curTool) {
                case SELECTION:
                case ERASE:
                case FILL:
                    break;
                case LINE:
                    preview.setStroke(curLineColor);
                    break;
                case RECTANGLE:
                case ELLIPSE:
                    preview.setStroke(curLineColor);
                    preview.setFill(curFillColor);
                    break;
            }
            modified = true;
            preview = null;
        }
    }

    public void keyPress(KeyEvent keyPress) {
        if (curTool == Tool.SELECTION) {
            switch (keyPress.getCode()) {
                case ESCAPE:
                    unselect();
                    break;
                case BACK_SPACE:
                    if (curSelected != null) {
                        removeShape(curSelected);
                        curSelected = null;
                    }
                    break;
            }
        }
    }

    private void select(Shape shape) {
        curSelected = shape;
        if (curSelected == null) {
            return;
        }
        curSelected.setEffect(new DropShadow());
        presenter.updateToolBar(curSelected);
    }

    private void unselect() {
        if (curSelected == null) {
            return;
        }
        curSelected.setEffect(null);
        curSelected = null;
    }

    private void clear() {
        for (Shape shape : shapes) {
            presenter.undraw(shape);
        }
        shapes = new ArrayList<>();
    }

    public void _new() {
        if (!modified) {
            this.clear();
            modified = false;
            return;
        }
        String answer = this.saveAlert();
        if (answer == null) return;
        if (answer.equals("Yes")) {
            if (this.saveDialog()) {
                this.clear();
                modified = false;
            }
        }
        else if (answer.equals("No")) {
            this.clear();
            modified = false;
        }
    }

    public void load() {
        if (this.loadDialog()) {
            modified = false;
        }
    }

    public void quit() {
        if (!modified) {
            System.exit(0);
        }
        String answer = this.saveAlert();
        if (answer == null) return;
        if (answer.equals("Yes")) {
            if (this.saveDialog()) {
                System.exit(0);
            }
        }
        else if (answer.equals("No")) {
            System.exit(0);
        }
    }

    public void save() {
        if (modified) {
            if (this.saveDialog()) {
                //success
                modified = false;
            }
        }
    }

    private boolean loadDialog() {
        File source = presenter.fileChooserDialog("Load");
        try {
            this.loadData(source);
        }
        catch (FileNotFoundException | NullPointerException e) {
            return false;
        } catch (NoSuchElementException e) {
            presenter.displayMessage("Fail to load " + source.getName() + ".");
            return false;
        }
        return true;
    }

    private void loadData(File file) throws FileNotFoundException, NoSuchElementException, NullPointerException {
        Scanner scanner;
        scanner = new Scanner(file);
        double canvasWidth = scanner.nextDouble();
        double canvasHeight = scanner.nextDouble();
        double scaleX = this.canvasWidth/canvasWidth;
        double scaleY = this.canvasHeight/canvasHeight;
        ArrayList<Shape> newShapes = new ArrayList<>();
        while(scanner.hasNext()) {
            String shape = scanner.next();
            double red, green, blue, lineWidth, lineStyle;
            switch (shape) {
                case "Line":
                    double startX = scanner.nextDouble()*scaleX;
                    double startY = scanner.nextDouble()*scaleY;
                    double endX = scanner.nextDouble()*scaleX;
                    double endY = scanner.nextDouble()*scaleY;
                    Line newLine = new Line(startX,startY,endX,endY);

                    red = scanner.nextDouble();
                    green = scanner.nextDouble();
                    blue = scanner.nextDouble();
                    newLine.setStroke(Color.color(red,green,blue));

                    lineWidth = scanner.nextDouble();
                    lineStyle = scanner.nextDouble();
                    newLine.setStrokeWidth(lineWidth);
                    if (lineStyle != 0d) {
                        newLine.getStrokeDashArray().add(lineStyle);
                    }

                    newShapes.add(newLine);
                    break;
                case "Rectangle":
                    double x = scanner.nextDouble()*scaleX;
                    double y = scanner.nextDouble()*scaleY;
                    double width = scanner.nextDouble()*scaleX;
                    double height = scanner.nextDouble()*scaleY;
                    Rectangle newRectangle = new Rectangle(x,y,width,height);

                    red = scanner.nextDouble();
                    green = scanner.nextDouble();
                    blue = scanner.nextDouble();
                    newRectangle.setStroke(Color.color(red,green,blue));

                    red = scanner.nextDouble();
                    green = scanner.nextDouble();
                    blue = scanner.nextDouble();
                    newRectangle.setFill(Color.color(red,green,blue));

                    lineWidth = scanner.nextDouble();
                    lineStyle = scanner.nextDouble();
                    newRectangle.setStrokeWidth(lineWidth);
                    if (lineStyle != 0d) {
                        newRectangle.getStrokeDashArray().add(lineStyle);
                    }

                    newShapes.add(newRectangle);
                    break;
                case "Ellipse":
                    double centerX = scanner.nextDouble()*scaleX;
                    double centerY = scanner.nextDouble()*scaleY;
                    double radiusX = scanner.nextDouble()*scaleX;
                    double radiusY = scanner.nextDouble()*scaleY;
                    Ellipse newEllipse = new Ellipse(centerX,centerY,radiusX,radiusY);

                    red = scanner.nextDouble();
                    green = scanner.nextDouble();
                    blue = scanner.nextDouble();
                    newEllipse.setStroke(Color.color(red,green,blue));

                    red = scanner.nextDouble();
                    green = scanner.nextDouble();
                    blue = scanner.nextDouble();
                    newEllipse.setFill(Color.color(red,green,blue));

                    lineWidth = scanner.nextDouble();
                    lineStyle = scanner.nextDouble();
                    newEllipse.setStrokeWidth(lineWidth);
                    if (lineStyle != 0d) {
                        newEllipse.getStrokeDashArray().add(lineStyle);
                    }

                    newShapes.add(newEllipse);
                    break;
                default:
                    break;
            }
        }
        scanner.close();

        this.clear();
        newShapes.forEach(this::addShape);
    }

    private boolean saveDialog() {
        File target = presenter.fileChooserDialog("Save");
        try {
            this.saveData(target);
        }
        catch (FileNotFoundException | NullPointerException e) {
            return false;
        }
        return true;
    }

    private String saveAlert() {
        return presenter.saveAlert();
    }

    private void saveData(File file) throws FileNotFoundException, NullPointerException {
        PrintWriter writer;
        writer = new PrintWriter(file);
        writer.println(canvasWidth + " " + canvasHeight);
        for (Shape shape : shapes) {
            writer.println(getInfo(shape));
        }
        writer.close();
    }

    private String getInfo(Shape shape) {
        if (shape instanceof Line) {
            Line line = (Line) shape;
            return "Line " + line.getStartX() + " " + line.getStartY() + " " + line.getEndX() + " " + line.getEndY()
                    + " " + ((Color)line.getStroke()).getRed()
                    + " " + ((Color)line.getStroke()).getGreen()
                    + " " + ((Color)line.getStroke()).getBlue()
                    + " " + line.getStrokeWidth()
                    + " " + (line.getStrokeDashArray().size() == 0 ? 0 : line.getStrokeDashArray().get(0));
        }
        if (shape instanceof Rectangle) {
            Rectangle rectangle = (Rectangle) shape;
            return "Rectangle " + rectangle.getX() + " " + rectangle.getY()
                    + " " + rectangle.getWidth() + " " + rectangle.getHeight()
                    + " " + ((Color) rectangle.getStroke()).getRed()
                    + " " + ((Color) rectangle.getStroke()).getGreen()
                    + " " + ((Color) rectangle.getStroke()).getBlue()
                    + " " + ((Color) rectangle.getFill()).getRed()
                    + " " + ((Color) rectangle.getFill()).getGreen()
                    + " " + ((Color) rectangle.getFill()).getBlue()
                    + " " + rectangle.getStrokeWidth()
                    + " " + (rectangle.getStrokeDashArray().size() == 0 ? 0 : rectangle.getStrokeDashArray().get(0));
        }
        if (shape instanceof Ellipse) {
            Ellipse ellipse = (Ellipse) shape;
            return "Ellipse " + ellipse.getCenterX() + " " + ellipse.getCenterY()
                    + " " + ellipse.getRadiusX() + " " + ellipse.getRadiusY()
                    + " " + ((Color) ellipse.getStroke()).getRed()
                    + " " + ((Color) ellipse.getStroke()).getGreen()
                    + " " + ((Color) ellipse.getStroke()).getBlue()
                    + " " + ((Color) ellipse.getFill()).getRed()
                    + " " + ((Color) ellipse.getFill()).getGreen()
                    + " " + ((Color) ellipse.getFill()).getBlue()
                    + " " + ellipse.getStrokeWidth()
                    + " " + (ellipse.getStrokeDashArray().size() == 0 ? 0 : ellipse.getStrokeDashArray().get(0));
        }
        return null;
    }
}