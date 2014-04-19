package net.sf.juffrou.mq;

import java.io.File;
import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.sf.juffrou.mq.queues.presenter.AbstractQueuesListPresenterImpl;
import net.sf.juffrou.mq.ui.ConsolePreloader;
import net.sf.juffrou.mq.ui.SpringFxmlLoader;
import net.sf.juffrou.mq.ui.ConsolePreloader.SharedScene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MQConsole extends Application implements ConsolePreloader.SharedScene {
	
	private AbstractQueuesListPresenterImpl mainController;

	static {
		if (System.getProperty("os.name").startsWith("Mac"))
			System.setProperty("mq.console.log.dir", System.getProperty("user.home")
					+ "/Library/Application Support/MQConsole");
		else
			System.setProperty("mq.console.log.dir", System.getenv("APPDATA") + "/MQConsole");
	}
	private static final Logger log = LoggerFactory.getLogger(MQConsole.class);
	public static ApplicationContext applicationContext;

	private Parent parentNode;

	public static void main(String[] args) {
		System.setProperty("mq.console.dir", System.getProperty("user.dir") + File.separator + "src" + File.separator
				+ "main" + File.separator + "deploy" + File.separator + "package");
		launch(args);
	}

	private static void setMQConsoleDir() {
		String mqConsoleDir = System.getProperty("user.dir") + File.separator;
		if (System.getProperty("os.name").startsWith("Mac")) {
			URL mySource = MQConsole.class.getProtectionDomain().getCodeSource().getLocation();
			mqConsoleDir = mySource.getPath().substring(0, mySource.getPath().lastIndexOf('/'));
		}
		System.setProperty("mq.console.dir", mqConsoleDir);
	}

	@Override
	public void init() {
		if (System.getProperty("mq.console.dir") == null)
			setMQConsoleDir();
		if (log.isDebugEnabled()) {
			log.debug("Console started");
			log.debug("user.dir=" + System.getProperty("user.dir"));
			log.debug("mq.console.dir=" + System.getProperty("mq.console.dir"));
			log.debug("mq.console.log.dir=" + System.getProperty("mq.console.log.dir"));
		}
		try {
			applicationContext = new ClassPathXmlApplicationContext(
					new String[] { "META-INF/context/mq-console-application-context.xml" });
		} catch (BeansException be) {
			if (log.isErrorEnabled()) {
				log.error("Cannot start application.");
				log.error(be.getMessage());
			}
			throw be;
		}
		if (log.isDebugEnabled())
			log.debug("Console context loaded");
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
		mainController = springFxmlLoader.<AbstractQueuesListPresenterImpl> getController();
		mainController.setStage(primaryStage);
		Scene scene = new Scene(parentNode, 800, 480);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Websphere-MQ Queues");

		// Terminate application upon main window closing
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent arg0) {
				Platform.exit();
			}
			
		});
		primaryStage.show();
	}

	@Override
	public Parent getParentNode() {
		return parentNode;
	}

	@Override
	public void stop() throws Exception {
		mainController.getMessageListener().stopMessageListener();
		super.stop();
	}
}
