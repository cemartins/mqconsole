package net.sf.juffrou.mq.messages;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.messages.presenter.MessageViewView;
import net.sf.juffrou.mq.messages.task.AbstractMessageListenerTask;
import net.sf.juffrou.mq.messages.task.MessageListenerTaskFactory;
import net.sf.juffrou.mq.util.MessageReceivedHandler;

@Component
public class MessageListenerImpl implements MessageListener {

	@Autowired
	private MessageListenerTaskFactory messageListenerTaskFactory;
	
	@Autowired
	private MessageViewView messageViewView;
	
	private AbstractMessageListenerTask currentListeningTask;
	private ExecutorService currentListeningThread = null;
	private String currentListeningQueue;

	public void startMessageListener(Stage parentStage, String listeningQueue) {
		
		if (currentListeningThread != null) {
			currentListeningTask.cancel(true);
			currentListeningThread.shutdownNow(); // cannot have more than one active listener
		}

		IncomingMessageHandler handler = new IncomingMessageHandler(parentStage, listeningQueue);
		currentListeningTask = messageListenerTaskFactory.createMessageListenerTask(handler, listeningQueue);

		currentListeningQueue = listeningQueue;
		currentListeningThread = Executors.newSingleThreadExecutor();//new Thread(currentListeningTask, "Message listening task");
		currentListeningThread.execute(currentListeningTask);
	}

	public boolean isCurrentListeningQueue(String listeningQueue) {
		return currentListeningThread != null && listeningQueue.equals(currentListeningQueue);
	}

	public void stopMessageListener() {
		if (currentListeningThread != null) {
			currentListeningTask.cancel(true);
			currentListeningThread.shutdownNow(); // cannot have more than one active listener
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

			Parent root = messageViewView.getView();

			MessageViewController presenter = (MessageViewController) messageViewView.getPresenter();
			presenter.setMessageDescriptor(messageDescriptor);
			presenter.initialize();

			Scene scene = new Scene(root, 768, 480);
			Stage stage = new Stage();
			stage.setScene(scene);
			stage.setTitle("Message Event");
			stage.show();

			currentListeningQueue = null;
			startMessageListener(parentStage, listeningQueue);
		}

		@Override
		public Stage getStage() {
			return parentStage;
		}

	}
}
