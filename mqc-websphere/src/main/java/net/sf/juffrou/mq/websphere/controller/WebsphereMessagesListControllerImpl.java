package net.sf.juffrou.mq.websphere.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.error.BrokerSpecificException;
import net.sf.juffrou.mq.messages.MessagesListController;
import net.sf.juffrou.mq.messages.presenter.MessagesListPresenter;
import net.sf.juffrou.mq.websphere.util.MessageDescriptorHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ibm.mq.MQC;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.pcf.PCFConstants;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WebsphereMessagesListControllerImpl implements MessagesListController {

	protected static final Logger LOG = LoggerFactory.getLogger(WebsphereMessagesListControllerImpl.class);
	
	@Autowired
	@Qualifier("mqQueueManager")
	private MQQueueManager qm;


	public MQQueueManager getQm() {
		return qm;
	}

	public void setQm(MQQueueManager qm) {
		this.qm = qm;
	}


	public List<MessageDescriptor> listMessages(MessagesListPresenter presenter, String queueName) throws IOException, BrokerSpecificException {
		List<MessageDescriptor> messageList = new ArrayList<MessageDescriptor>();

		try {
			MQException.log = null;
			MQQueue queue = qm.accessQueue(queueName, MQConstants.MQOO_BROWSE | MQConstants.MQOO_FAIL_IF_QUIESCING);
			MQMessage message = new MQMessage();
			MQGetMessageOptions gmo = new MQGetMessageOptions();
			
			String i = MQC.TRANSPORT_MQSERIES_CLIENT;

			gmo.options = MQConstants.MQGMO_BROWSE_NEXT | MQConstants.MQGMO_NO_WAIT | MQConstants.MQGMO_CONVERT;

			while (true) {
				message.messageId = null;
				message.correlationId = null;
				queue.get(message, gmo);

				MessageDescriptor messageDescriptor = MessageDescriptorHelper.createMessageDescriptor(message);

				messageList.add(messageDescriptor);

				// Parse the message content using a PCFMessage object and print out the result.
				//				PCFMessage pcf = new PCFMessage(message);
				//				System.out.println("Message " + ++messageCount + ": " + pcf + "\n");
			}
		}

		catch (MQException mqe) {
			if (mqe.reasonCode == MQException.MQRC_NO_MSG_AVAILABLE) {
				if (LOG.isDebugEnabled())
					LOG.debug(messageList.size() + (messageList.size() == 1 ? " message." : " messages."));
			} else {
				if (LOG.isErrorEnabled())
					LOG.error(mqe + ": " + PCFConstants.lookupReasonCode(mqe.reasonCode));
				
				throw new BrokerSpecificException(mqe.getMessage() + ": " + PCFConstants.lookupReasonCode(mqe.reasonCode), mqe);
			}
		} catch (MQDataException e) {
			if (LOG.isErrorEnabled())
				LOG.error(e + ": " + PCFConstants.lookupReasonCode(e.reasonCode));
			throw new BrokerSpecificException(e.getMessage() + ": " + PCFConstants.lookupReasonCode(e.reasonCode), e);
		}

		return messageList;
	}

}
