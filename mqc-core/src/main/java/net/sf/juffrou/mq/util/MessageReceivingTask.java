package net.sf.juffrou.mq.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.ui.NotificationPopup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.PCFConstants;

public class MessageReceivingTask extends Task<MessageDescriptor> {

	private static final Logger log = LoggerFactory.getLogger(MessageReceivingTask.class);

	private final MQQueueManager qm;
	private final MQMessage replyMessage;
	private final String queueNameReceive;
	private final String queueNameSend;
	private final Integer brokerTimeout;

	public MessageReceivingTask(final MessageReceivedHandler handler, MQQueueManager qm, String queueNameReceive, Integer brokerTimeout, MQMessage replyMessage, String queueNameSent) {
		super();
		this.qm = qm;
		this.queueNameReceive = queueNameReceive;
		this.brokerTimeout = brokerTimeout;
		this.replyMessage = replyMessage;
		this.queueNameSend = queueNameSent;

		stateProperty().addListener(new ChangeListener<Worker.State>() {
			@Override
			public void changed(ObservableValue<? extends javafx.concurrent.Worker.State> observable, javafx.concurrent.Worker.State oldValue, javafx.concurrent.Worker.State newState) {
				switch (newState) {
				case SUCCEEDED:
					MessageReceivingTask.super.succeeded();
					handler.messageReceived(getValue());
					break;
				case FAILED:
					MessageReceivingTask.super.failed();
					MessageDescriptor messageDescriptor = new MessageDescriptor();
					messageDescriptor.setText(getMessage());
					handler.messageReceived(messageDescriptor);
					NotificationPopup popup = new NotificationPopup(handler.getStage());
					popup.display(getMessage());
					break;
				case CANCELLED:
					MessageReceivingTask.super.cancelled();
					MessageDescriptor canceledDescriptor = new MessageDescriptor();
					canceledDescriptor.setText(getMessage());
					handler.messageReceived(canceledDescriptor);
					break;
				}
			}
		});
	}

	@Override
	protected MessageDescriptor call() throws Exception {
		MQQueue inboundQueue = null;
		try {
			// Construct new MQGetMessageOptions object
			MQGetMessageOptions gmo = new MQGetMessageOptions();

			// Set the get message options.. specify that we want to wait
			// for reply message
			// AND *** SET OPTION TO CONVERT CHARS TO RIGHT CHAR SET ***
			gmo.options = MQConstants.MQGMO_WAIT;

			gmo.options |= MQConstants.MQGMO_PROPERTIES_FORCE_MQRFH2;
			gmo.options |= MQConstants.MQGMO_CONVERT;
			
			gmo.matchOptions = MQConstants.MQMO_MATCH_CORREL_ID;

			// Specify the wait interval for the message in milliseconds
			gmo.waitInterval = brokerTimeout.intValue();

			if (log.isDebugEnabled()) {
				log.debug("Current Msg ID used for receive: '" + new String(replyMessage.messageId) + "'");
				log.debug("Correlation ID to use for receive: '" + new String(replyMessage.correlationId) + "'");
				log.debug("Supported character set to use for receive: " + replyMessage.characterSet);
			}

			int openOptions;
			if (queueNameSend.equals(queueNameReceive)) {
				openOptions = MQConstants.MQOO_INPUT_SHARED | MQConstants.MQOO_OUTPUT;
			} else {
				openOptions = MQConstants.MQOO_INPUT_SHARED; // in bound options only
			}
//			openOptions = MQConstants.MQOO_INPUT_SHARED;
			// openOptions |= MQConstants.MQOO_READ_AHEAD;
			inboundQueue = qm.accessQueue(queueNameReceive, openOptions, null, // default q manager
					null, // no dynamic q name
					null); // no alternate user id

			if (isCancelled())
				return null;

			// Following test lines will cause any message on the queue to
			// be received regardless of
			// whatever message ID or correlation ID it might have
			// replyMessage.messageId = MQConstants.MQMI_NONE;
			// replyMessage.correlationId = MQConstants.MQCI_NONE;

			//				replyMessage.characterSet = 1208; // UTF-8 (will be charset=819 when the msg has Portuguese accented chars)

			replyMessage.format = MQConstants.MQFMT_RF_HEADER_2;
			// replyMessage.setBooleanProperty(MQConstants.WMQ_MQMD_READ_ENABLED,
			// true);

			// The replyMessage will have the correct correlation id for the
			// message we want to get.
			// Get the message off the queue..
			inboundQueue.get(replyMessage, gmo);
			if (isCancelled())
				return null;
			// And prove we have the message by displaying the message text
			if (log.isDebugEnabled())
				log.debug("The receive message character set is: " + replyMessage.characterSet);

			MessageDescriptor replyMessageDescriptor = MessageDescriptorHelper.createMessageDescriptor(replyMessage);

			return replyMessageDescriptor;

		} catch (MQException mqe) {
			if (log.isErrorEnabled())
				log.error("Error receiving message " + mqe + ": " + PCFConstants.lookupReasonCode(mqe.reasonCode));
			updateMessage("Error receiving message " + mqe + ": " + PCFConstants.lookupReasonCode(mqe.reasonCode));
			throw mqe;
		} catch (java.io.IOException ex) {
			if (log.isErrorEnabled())
				log.error("Error receiving message " + ex.getMessage());
			updateMessage(ex.getMessage());
			throw ex;
		} catch (Exception ex) {
			if (log.isErrorEnabled())
				log.error("Error receiving message " + ex.getMessage());
			updateMessage(ex.getMessage());
			throw ex;
		} finally {
			if (inboundQueue != null)
				try {
					inboundQueue.close();
				} catch (MQException e) {
					if (log.isErrorEnabled())
						log.error("Error closing queue " + e.getMessage());
				}
		}
	}

}
