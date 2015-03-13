package net.sf.juffrou.mq.messages.task;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.util.MessageReceivedHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMessageListenerTask extends Task<MessageDescriptor> {

	protected static final Logger LOG = LoggerFactory.getLogger(AbstractMessageListenerTask.class);

	private final String queueNameReceive;

	protected AbstractMessageListenerTask(final MessageReceivedHandler handler, String queueNameReceive) {
		super();
		this.queueNameReceive = queueNameReceive;

		stateProperty().addListener(new ChangeListener<Worker.State>() {
			@Override
			public void changed(ObservableValue<? extends javafx.concurrent.Worker.State> observable, javafx.concurrent.Worker.State oldValue, javafx.concurrent.Worker.State newState) {
				switch (newState) {
				case SUCCEEDED:
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							handler.messageReceived(getValue());
						}
					});
					break;
				case FAILED:
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error Dialog");
					alert.setHeaderText("Failed to receive message");
					alert.setContentText(getMessage());
					alert.showAndWait();
					break;
				case CANCELLED:
					break;
				case SCHEDULED:
					break;
				case READY:
					break;
				case RUNNING:
					break;
				}
			}
		});
	}
	
	public String getQueueNameReceive() {
		return queueNameReceive;
	}



	@Override
	protected abstract MessageDescriptor call() throws Exception;
	
}
