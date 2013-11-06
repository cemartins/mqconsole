package net.sf.juffrou.mq.controller;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.ui.Main;
import net.sf.juffrou.mq.ui.SpringFxmlLoader;
import net.sf.juffrou.mq.util.MessageListenerTask;
import net.sf.juffrou.mq.util.MessageReceivedHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.ibm.mq.MQQueueManager;

@Component
public class MessageListenerController {

	@Autowired
	@Qualifier("mqListeningQueueManager")
	private MQQueueManager qm;

	private Thread currentListeningThread = null;
	private String currentListeningQueue;

	public void startMessageListener(Stage parentStage, String listeningQueue) {

		if (currentListeningThread != null) {
			currentListeningThread.interrupt(); // cannot have more than one active listener
		}

		IncomingMessageHandler handler = new IncomingMessageHandler(parentStage, listeningQueue);
		MessageListenerTask task = new MessageListenerTask(handler, qm, listeningQueue);

		currentListeningQueue = listeningQueue;
		currentListeningThread = new Thread(task);
		currentListeningThread.start();
	}

	public boolean isCurrentListeningQueue(String listeningQueue) {
		return listeningQueue.equals(currentListeningQueue);
	}

	public void stopMessageListener() {
		if (currentListeningThread != null) {
			currentListeningThread.interrupt(); // cannot have more than one active listener
		}

		currentListeningQueue = null;
	}

	private class IncomingMessageHandler implements MessageReceivedHandler {

		private final Stage parentStage;
		private final String listeningQueue;

		public IncomingMessageHandler(Stage parentStage, String listeningQueue) {
			this.parentStage = parentStage;
			this.listeningQueue = listeningQueue;
		}

		@Override
		public void messageReceived(MessageDescriptor messageDescriptor) {

			SpringFxmlLoader springFxmlLoader = new SpringFxmlLoader(Main.applicationContext);
			Parent root = (Parent) springFxmlLoader.load("/net/sf/juffrou/mq/ui/message-read.fxml");

			MessageViewControler controller = springFxmlLoader.<MessageViewControler> getController();
			controller.setMessageDescriptor(messageDescriptor);
			controller.initialize();

			Scene scene = new Scene(root, 768, 480);
			Stage stage = new Stage();
			stage.setScene(scene);
			stage.setTitle("Message Event");
			stage.show();

			startMessageListener(parentStage, listeningQueue);
		}

		@Override
		public Stage getStage() {
			return parentStage;
		}

	}

}
