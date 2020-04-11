import javafx.application.Application;
import javafx.stage.Stage;

public class SketchIt extends Application {

    @Override
    public void start(Stage stage) {
        Model model = new Model();
        Presenter presenter = new Presenter(stage, model);
        presenter.start();
    }
}