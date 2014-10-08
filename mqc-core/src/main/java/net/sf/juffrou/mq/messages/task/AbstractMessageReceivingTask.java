package net.sf.juffrou.mq.messages.task;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.ui.NotificationPopup;
import net.sf.juffrou.mq.util.MessageReceivedHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMessageReceivingTask extends Task<MessageDescriptor> {

	protected static final Logger LOG = LoggerFactory.getLogger(AbstractMessageReceivingTask.class);

	private final QueueDescriptor queueNameReceive;
	private final QueueDescriptor queueNameSend;
	private final Integer brokerTimeout;

	public AbstractMessageReceivingTask(final MessageReceivedHandler handler, QueueDescriptor queueNameReceive, Integer brokerTimeout, QueueDescriptor queueNameSent) {
		super();
		this.queueNameReceive = queueNameReceive;
		this.brokerTimeout = brokerTimeout;
		this.queueNameSend = queueNameSent;

		stateProperty().addListener(new ChangeListener<Worker.State>() {
			@Override
			public void changed(ObservableValue<? extends javafx.concurrent.Worker.State> observable, javafx.concurrent.Worker.State oldValue, javafx.concurrent.Worker.State newState) {
				switch (newState) {
				case SUCCEEDED:
					AbstractMessageReceivingTask.super.succeeded();
					handler.messageReceived(getValue());
					break;
				case FAILED:
					AbstractMessageReceivingTask.super.failed();
					MessageDescriptor messageDescriptor = new MessageDescriptor();
					messageDescriptor.setText(getMessage());
					handler.messageReceived(messageDescriptor);
					NotificationPopup popup = new NotificationPopup(handler.getStage());
					popup.display(getMessage());
					break;
				case CANCELLED:
					AbstractMessageReceivingTask.super.cancelled();
					MessageDescriptor canceledDescriptor = new MessageDescriptor();
					canceledDescriptor.setText(getMessage());
					handler.messageReceived(canceledDescriptor);
					break;
				}
			}
		});
	}

	@Override
	protected abstract MessageDescriptor call() throws Exception;

	public QueueDescriptor getQueueNameReceive() {
		return queueNameReceive;
	}

	public QueueDescriptor getQueueNameSend() {
		return queueNameSend;
	}

	public Integer getBrokerTimeout() {
		return brokerTimeout;
	}
	
}
