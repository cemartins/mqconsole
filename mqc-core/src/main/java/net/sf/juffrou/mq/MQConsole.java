package net.sf.juffrou.mq;

import java.io.File;
import java.net.URL;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import net.sf.juffrou.mq.queues.presenter.QueuesListPresenter;
import net.sf.juffrou.mq.queues.presenter.QueuesListView;
import net.sf.juffrou.mq.ui.ConsolePreloader;
import net.sf.juffrou.mq.ui.ExceptionDialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MQConsole extends Application implements ConsolePreloader.SharedScene {
	
	public static final String APPLICATION_ICON =
            "/images/mqconsole_01.png";
//	public static final String SPLASH_IMAGE =
//            "http://fxexperience.com/wp-content/uploads/2010/06/logo.png";
	public static final String SPLASH_IMAGE =
            "/images/mqconsole_01.png";
	
    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;
    private Stage mainStage;
    private static final int SPLASH_WIDTH = 350;
    private static final int SPLASH_HEIGHT = 80;

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
			System.setProperty("mq.console.dir", System.getProperty("user.dir") + File.separator + "src" + File.separator
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
		
        ImageView splash = new ImageView(new Image(SPLASH_IMAGE));
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH - 20);
        progressText = new Label("Will find friends for peanuts . . .");
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.setStyle(
                "-fx-padding: 5; " +
                "-fx-background-color: cornsilk; " +
                "-fx-border-width:5; " +
                "-fx-border-color: " +
                    "linear-gradient(" +
                        "to bottom, " +
                        "chocolate, " +
                        "derive(chocolate, 50%)" +
                    ");"
        );
        splashLayout.setEffect(new DropShadow());

	}

	@SuppressWarnings("restriction")
	public void old_start(Stage primaryStage) throws Exception {
		try {

			applicationContext = new ClassPathXmlApplicationContext(new String[] { "classpath*:context/*-context.xml" });

			QueuesListView queuesListView = applicationContext.getBean(QueuesListView.class);
			parentNode = queuesListView.getView();
			mainController = (QueuesListPresenter) queuesListView.getPresenter();
			mainController.setStage(primaryStage);
			Scene scene = new Scene(parentNode, 326, 73);
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

			ExceptionDialog alert = new ExceptionDialog();
			alert.setTitle("Error Dialog");
			alert.setHeaderText("MQConsole Cannot Start");
			alert.setContentText(e.getMessage());
			alert.setException(e);
			alert.showAndWait();

			Platform.exit();
			System.exit(1);
		}

		primaryStage.show();
	}
	
	
    @Override
    public void start(final Stage initStage) throws Exception {
        final Task<QueuesListView> friendTask = new Task<QueuesListView>() {
            @Override
            protected QueuesListView call() throws InterruptedException {
                ObservableList<String> foundFriends =
                        FXCollections.<String>observableArrayList();
                ObservableList<String> availableFriends =
                        FXCollections.observableArrayList(
                                "Fili", "Kili", "Oin", "Gloin", "Thorin",
                                "Dwalin", "Balin", "Bifur", "Bofur",
                                "Bombur", "Dori", "Nori", "Ori"
                        );
 
                updateMessage("Finding friends . . .");
                for (int i = 0; i < availableFriends.size(); i++) {
                    Thread.sleep(400);
                    updateProgress(i + 1, availableFriends.size());
                    String nextFriend = availableFriends.get(i);
                    foundFriends.add(nextFriend);
                    updateMessage("Finding friends . . . found " + nextFriend);
                }
                Thread.sleep(400);
                updateMessage("All friends found.");
 
                applicationContext = new ClassPathXmlApplicationContext(new String[] { "classpath*:context/*-context.xml" });
                
                QueuesListView queuesListView = applicationContext.getBean(QueuesListView.class);
                // load the view to throw eventual controller initialization exceptions
                queuesListView.getView();
                
                return queuesListView;
            }
        };
 
        showSplash(
                initStage,
                friendTask,
                () -> showMainStage(friendTask.valueProperty())
        );
        new Thread(friendTask).start();
    }
 
    private void showMainStage(ReadOnlyObjectProperty<QueuesListView> context) {
        mainStage = new Stage(StageStyle.DECORATED);
        mainStage.getIcons().add(new Image(APPLICATION_ICON));
 
		QueuesListView queuesListView = context.get();
		parentNode = queuesListView.getView();
		mainController = (QueuesListPresenter) queuesListView.getPresenter();
		mainController.setStage(mainStage);
		Scene scene = new Scene(parentNode, 326, 73);
		mainStage.setScene(scene);
		mainStage.setTitle("MQ Queues");

		// Terminate application upon main window closing
		mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent arg0) {
				Platform.exit();
				System.exit(0);
			}
			
		});

        mainStage.show();
        
    }
 
    private void showSplash(
            final Stage initStage,
            Task<?> task,
            InitCompletionHandler initCompletionHandler
    ) {
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadProgress.progressProperty().unbind();
                loadProgress.setProgress(1);
                initStage.toFront();
                FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
                fadeSplash.setFromValue(1.0);
                fadeSplash.setToValue(0.0);
                fadeSplash.setOnFinished(actionEvent -> initStage.hide());
                fadeSplash.play();
 
                initCompletionHandler.complete();
            }
            else if(newState == Worker.State.FAILED) {
            	Throwable e = task.getException();
    			if (log.isErrorEnabled()) {
    				log.error("MQConsole Cannot Start", e);
    			}

    			ExceptionDialog alert = new ExceptionDialog();
    			alert.setTitle("Error Dialog");
    			alert.setHeaderText("MQConsole Cannot Start");
    			alert.setContentText(e.getMessage());
    			alert.setException(e);
    			alert.showAndWait();

    			Platform.exit();
    			System.exit(1);
            }
        });
 
        Scene splashScene = new Scene(splashLayout);
        initStage.initStyle(StageStyle.UNDECORATED);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.show();
    }
 
    public interface InitCompletionHandler {
        public void complete();
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
