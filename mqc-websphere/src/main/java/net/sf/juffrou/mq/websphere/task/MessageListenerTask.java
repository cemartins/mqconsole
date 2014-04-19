package net.sf.juffrou.mq.websphere.task;

import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.messages.task.AbstractMessageListenerTask;
import net.sf.juffrou.mq.util.MessageReceivedHandler;
import net.sf.juffrou.mq.websphere.util.MessageDescriptorHelper;

import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.PCFConstants;

public class MessageListenerTask extends AbstractMessageListenerTask {

	private final MQQueueManager qm;

	public MessageListenerTask(final MessageReceivedHandler handler, MQQueueManager qm, String queueNameReceive) {
		super(handler, queueNameReceive);
		this.qm = qm;
	}


	@Override
	protected MessageDescriptor call() throws Exception {
		MQQueue inboundQueue = null;
		MessageDescriptor replyMessageDescriptor = null;
		try {
			// Construct new MQGetMessageOptions object
			MQGetMessageOptions gmo = new MQGetMessageOptions();

			// Set the get message options.. specify that we want to wait
			// for reply message
			// AND *** SET OPTION TO CONVERT CHARS TO RIGHT CHAR SET ***
			gmo.options = MQConstants.MQGMO_WAIT;

			gmo.waitInterval = MQConstants.MQWI_UNLIMITED;

			gmo.options |= MQConstants.MQGMO_PROPERTIES_FORCE_MQRFH2;
			gmo.options |= MQConstants.MQGMO_CONVERT;

			gmo.matchOptions = MQConstants.MQMO_MATCH_CORREL_ID;

			// If the name of the request queue is the same as the reply
			// queue...(again...)
			int openOptions;
			//			openOptions = MQConstants.MQOO_INPUT_AS_Q_DEF; // in bound options only
			openOptions = MQConstants.MQOO_INPUT_SHARED;
			// openOptions |= MQConstants.MQOO_READ_AHEAD;
			inboundQueue = qm.accessQueue(getQueueNameReceive(), openOptions, null, // default q manager
					null, // no dynamic q name
					null); // no alternate user id

			if (isCancelled())
				try {
					inboundQueue.close();
					return null;
				} catch (MQException e) {
					if (LOG.isErrorEnabled())
						LOG.error("Error closing queue " + e + ": " + PCFConstants.lookupReasonCode(e.reasonCode));
				}

			// Following test lines will cause any message on the queue to
			// be received regardless of
			// whatever message ID or correlation ID it might have
			// replyMessage.messageId = MQConstants.MQMI_NONE;
			// replyMessage.correlationId = MQConstants.MQCI_NONE;

			MQMessage inboundMessage = new MQMessage();
			inboundMessage.messageId = null;
			inboundMessage.correlationId = null;
			inboundMessage.format = MQConstants.MQFMT_RF_HEADER_2;
			// replyMessage.setBooleanProperty(MQConstants.WMQ_MQMD_READ_ENABLED,
			// true);

			// The replyMessage will have the correct correlation id for the
			// message we want to get.
			// Get the message off the queue..
			inboundQueue.get(inboundMessage, gmo);
			if (isCancelled())
				try {
					inboundQueue.close();
					return null;
				} catch (MQException e) {
					if (LOG.isErrorEnabled())
						LOG.error("Error closing queue " + e + ": " + PCFConstants.lookupReasonCode(e.reasonCode));
				}
			// And prove we have the message by displaying the message text
			if (LOG.isDebugEnabled())
				LOG.debug("The receive message character set is: " + inboundMessage.characterSet);

			replyMessageDescriptor = MessageDescriptorHelper.createMessageDescriptor(inboundMessage);

		} catch (MQException mqe) {
			if(!isCancelled()) {
				if (LOG.isErrorEnabled())
					LOG.error("Error receiving message " + mqe + ": " + PCFConstants.lookupReasonCode(mqe.reasonCode));
				updateMessage("Error receiving message " + mqe + ": " + PCFConstants.lookupReasonCode(mqe.reasonCode));
			}
			throw mqe;
		} catch (java.io.IOException ex) {
			if (LOG.isErrorEnabled())
				LOG.error("Error receiving message " + ex.getMessage());
			updateMessage(ex.getMessage());
			throw ex;
		} catch (Exception ex) {
			if (LOG.isErrorEnabled())
				LOG.error("Error receiving message " + ex.getMessage());
			updateMessage(ex.getMessage());
			throw ex;
		} finally {
			if (inboundQueue != null)
				try {
					inboundQueue.close();
				} catch (MQException e) {
					if (LOG.isErrorEnabled())
						LOG.error("Error closing queue " + e + ": " + PCFConstants.lookupReasonCode(e.reasonCode));
				}
		}

		return replyMessageDescriptor;
	}

}
