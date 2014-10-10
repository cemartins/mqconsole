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
import net.sf.juffrou.mq.queues.presenter.QueuesListPresenter;
import net.sf.juffrou.mq.queues.presenter.QueuesListView;
import net.sf.juffrou.mq.ui.ConsolePreloader;

import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MQConsole extends Application implements ConsolePreloader.SharedScene {
	
	private QueuesListPresenter mainController;

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
		if(args != null && args.length > 0 && "set_maven_path_to_broker_properties".equals(args[0])) {
			System.setProperty("mq.console.dir", System.getProperty("user.dir") + File.separator + "../mqc-assembler/src" + File.separator
					+ "main" + File.separator + "deploy" + File.separator + "package");
		}
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
		Parameters parameters = getParameters();
		if (System.getProperty("mq.console.dir") == null)
			setMQConsoleDir();
		if (log.isDebugEnabled()) {
			log.debug("Console started");
			log.debug("user.dir=" + System.getProperty("user.dir"));
			log.debug("mq.console.dir=" + System.getProperty("mq.console.dir"));
			log.debug("mq.console.log.dir=" + System.getProperty("mq.console.log.dir"));
		}
		try {
			applicationContext = new ClassPathXmlApplicationContext(new String[] { "classpath*:context/*-context.xml" });
		} catch (Exception be) {
			if (log.isErrorEnabled()) {
				log.error("Cannot start application.");
				log.error(be.getMessage());
			}
			/* Does not work because there is no javafx thread
			Dialogs.create()
		      .owner( null)
		      .title("MQConsole Cannot Start")
		      .message( "Spring Context could not be loaded. Is your \"broker.properties\" file in place?" )
		      .showException(be);
		      */
			
			throw be;
		}
		if (log.isDebugEnabled())
			log.debug("Console context loaded");
	}

	@SuppressWarnings("restriction")
	@Override
	public void start(Stage primaryStage) throws Exception {
		QueuesListView queuesListView = applicationContext.getBean(QueuesListView.class);
		try {
			parentNode = queuesListView.getView();
			mainController = (QueuesListPresenter) queuesListView.getPresenter();
			mainController.setStage(primaryStage);
			Scene scene = new Scene(parentNode, 800, 480);
			primaryStage.setScene(scene);
			primaryStage.setTitle("MQ Queues");

			// Terminate application upon main window closing
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

				@Override
				public void handle(WindowEvent arg0) {
					Platform.exit();
					System.exit(0);
				}
				
			});
		}
		catch (Exception e) {

			if (log.isErrorEnabled()) {
				log.error("MQConsole Cannot Start", e);
			}

			Dialogs.create()
		      .owner( null )
		      .title("MQConsole Cannot Start")
		      .message( e.getMessage() )
		      .showException(e);

			throw e;
		}

		primaryStage.show();
	}

	@Override
	public Parent getParentNode() {
		return parentNode;
	}

	@Override
	public void stop() throws Exception {
		if(mainController != null)
			mainController.getMessageListener().stopMessageListener();
		super.stop();
	}
}
