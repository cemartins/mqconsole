package net.sf.juffrou.mq.ui;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main extends Application implements ConsolePreloader.SharedScene {

	public static ApplicationContext applicationContext;

	private Parent parentNode;

	public static void main(String[] args) {
		System.setProperty("mq.console.dir", ".");
		launch(args);
	}

	@Override
	public void init() {
		System.out.println("Console started");
		applicationContext = new ClassPathXmlApplicationContext(
				new String[] { "META-INF/context/mq-console-application-context.xml" });
		System.out.println("Console context loaded");
		//prepare application scene
		//		Rectangle rect = new Rectangle(0, 0, 40, 40);
		//		rect.setArcHeight(10);
		//		rect.setArcWidth(10);
		//		rect.setFill(Color.ORANGE);
		//		parentNode = new Group(rect);
		//		System.out.println("Parent loaded");
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		SpringFxmlLoader springFxmlLoader = new SpringFxmlLoader(applicationContext);
		parentNode = (Parent) springFxmlLoader.load("/net/sf/juffrou/mq/ui/list-queues.fxml");
		Scene scene = new Scene(parentNode, 768, 480);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Websphere-MQ Queues");
		primaryStage.show();
	}

	@Override
	public Parent getParentNode() {
		return parentNode;
	}

}
