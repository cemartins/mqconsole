package net.sf.juffrou.mq.ui;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main extends Application {

	public static ApplicationContext applicationContext;

	public static void main(String[] args) {
		System.setProperty("mq.console.dir", ".");
		launch(args);
	}

	@Override
	public void init() {
		System.out.println("PCF started");
		applicationContext = new ClassPathXmlApplicationContext(
				new String[] { "META-INF/context/mq-console-application-context.xml" });
		System.out.println("PCF context loaded");
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		SpringFxmlLoader springFxmlLoader = new SpringFxmlLoader(applicationContext);
		Parent root = (Parent) springFxmlLoader.load("/net/sf/juffrou/mq/ui/list-queues.fxml");
		Scene scene = new Scene(root, 768, 480);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Websphere-MQ Queues");
		primaryStage.show();
	}

}
