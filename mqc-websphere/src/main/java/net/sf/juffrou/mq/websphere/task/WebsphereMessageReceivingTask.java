package net.sf.juffrou.mq.websphere.task;

import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.messages.task.AbstractMessageReceivingTask;
import net.sf.juffrou.mq.util.MessageReceivedHandler;
import net.sf.juffrou.mq.websphere.util.MessageDescriptorHelper;

import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.PCFConstants;

public class WebsphereMessageReceivingTask extends AbstractMessageReceivingTask {

	private final MQQueueManager qm;
	private final MQMessage replyMessage;

	public WebsphereMessageReceivingTask(final MessageReceivedHandler handler, MQQueueManager qm, String queueNameReceive, Integer brokerTimeout, MQMessage replyMessage, String queueNameSent) {
		super(handler, queueNameReceive, brokerTimeout, queueNameSent);
		this.qm = qm;
		this.replyMessage = replyMessage;
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
			gmo.waitInterval = getBrokerTimeout().intValue();

			if (LOG.isDebugEnabled()) {
				LOG.debug("Current Msg ID used for receive: '" + new String(replyMessage.messageId) + "'");
				LOG.debug("Correlation ID to use for receive: '" + new String(replyMessage.correlationId) + "'");
				LOG.debug("Supported character set to use for receive: " + replyMessage.characterSet);
			}

			int openOptions;
			if (getQueueNameSend().equals(getQueueNameReceive())) {
				openOptions = MQConstants.MQOO_INPUT_SHARED | MQConstants.MQOO_OUTPUT;
			} else {
				openOptions = MQConstants.MQOO_INPUT_SHARED; // in bound options only
			}
//			openOptions = MQConstants.MQOO_INPUT_SHARED;
			// openOptions |= MQConstants.MQOO_READ_AHEAD;
			inboundQueue = qm.accessQueue(getQueueNameReceive(), openOptions, null, // default q manager
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
			if (LOG.isDebugEnabled())
				LOG.debug("The receive message character set is: " + replyMessage.characterSet);

			MessageDescriptor replyMessageDescriptor = MessageDescriptorHelper.createMessageDescriptor(replyMessage);

			return replyMessageDescriptor;

		} catch (MQException mqe) {
			if (LOG.isErrorEnabled())
				LOG.error("Error receiving message " + mqe + ": " + PCFConstants.lookupReasonCode(mqe.reasonCode));
			updateMessage("Error receiving message " + mqe + ": " + PCFConstants.lookupReasonCode(mqe.reasonCode));
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
						LOG.error("Error closing queue " + e.getMessage());
				}
		}
	}

}
