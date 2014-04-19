package net.sf.juffrou.mq.websphere.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.messages.presenter.AbstractMessagesListPresenterImpl;
import net.sf.juffrou.mq.ui.NotificationPopup;
import net.sf.juffrou.mq.websphere.util.MessageDescriptorHelper;

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
public class WebsphereMessagesListPresenterImpl extends AbstractMessagesListPresenterImpl {

	@Autowired
	@Qualifier("mqQueueManager")
	private MQQueueManager qm;


	public MQQueueManager getQm() {
		return qm;
	}

	public void setQm(MQQueueManager qm) {
		this.qm = qm;
	}


	protected List<MessageDescriptor> listMessages() {
		List<MessageDescriptor> messageList = new ArrayList<MessageDescriptor>();

		try {
			MQException.log = null;
			MQQueue queue = qm.accessQueue(getQueueName(), MQConstants.MQOO_BROWSE | MQConstants.MQOO_FAIL_IF_QUIESCING);
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
				NotificationPopup popup = new NotificationPopup(getStage());
				popup.display(mqe + ": " + PCFConstants.lookupReasonCode(mqe.reasonCode));
			}
		} catch (IOException e) {
			if (LOG.isErrorEnabled())
				LOG.error(e.getMessage());
			NotificationPopup popup = new NotificationPopup(getStage());
			popup.display(e.getMessage());
		} catch (MQDataException e) {
			if (LOG.isErrorEnabled())
				LOG.error(e + ": " + PCFConstants.lookupReasonCode(e.reasonCode));
			NotificationPopup popup = new NotificationPopup(getStage());
			popup.display(e + ": " + PCFConstants.lookupReasonCode(e.reasonCode));
		}

		return messageList;
	}

}
