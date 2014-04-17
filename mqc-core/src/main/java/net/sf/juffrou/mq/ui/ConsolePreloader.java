package net.sf.juffrou.mq.ui;

import javafx.animation.FadeTransition;
import javafx.application.Preloader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ConsolePreloader extends Preloader {
	private Group topGroup;
	private Parent preloaderParent;
	private Stage stage;

	private Scene createPreloaderScene() {
		//our preloader is simple static green rectangle
		Rectangle r = new Rectangle(300, 150);
		r.setFill(Color.GREEN);
		preloaderParent = new Group(r);
		topGroup = new Group(preloaderParent);
		return new Scene(topGroup, 300, 150);
	}

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		stage.setScene(createPreloaderScene());
		stage.show();
	}

	@Override
	public void handleStateChangeNotification(StateChangeNotification evt) {
		if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
			//its time to start fading into application ...
			SharedScene appScene = (SharedScene) evt.getApplication();
			fadeInTo(appScene.getParentNode());
		}
	}

	private void fadeInTo(Parent p) {
		//add application scene to the preloader group
		// (visualized "behind" preloader at this point)
		//Note: list is back to front
		topGroup.getChildren().add(0, p);

		//setup fade transition for preloader part of scene
		// fade out over 5s
		FadeTransition ft = new FadeTransition(Duration.millis(5000), preloaderParent);
		ft.setFromValue(1.0);
		ft.setToValue(0.5);
		ft.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				//After fade is done, remove preloader content
				topGroup.getChildren().remove(preloaderParent);
			}
		});
		ft.play();
	}

	/* Contact interface between application and preloader */
	public interface SharedScene {
		/* Parent node of the application */
		Parent getParentNode();
	}

	@Override
	public boolean handleErrorNotification(ErrorNotification en) {
		// Display error
		Label l = new Label("This application needs elevated permissions to launch. "
				+ "Please reload the page and accept the security dialog.");
		stage.getScene().setRoot(l);

		System.out.println("Error details: " + en.getDetails());
		System.out.println("Error localtion: " + en.getLocation());
		en.getCause().printStackTrace(System.out);
		// Return true to prevent default error handler to take care of this error
		return true;
	}

}
