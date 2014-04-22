package net.sf.juffrou.mq.websphere.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.queues.QueuesListController;
import net.sf.juffrou.mq.queues.presenter.QueuesListPresenter;
import net.sf.juffrou.mq.ui.NotificationPopup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.CMQC;
import com.ibm.mq.pcf.CMQCFC;
import com.ibm.mq.pcf.PCFConstants;
import com.ibm.mq.pcf.PCFMessage;
import com.ibm.mq.pcf.PCFMessageAgent;

@Component
public class WebsphereQueuesListControllerImpl implements QueuesListController {
	
	protected static final Logger LOG = LoggerFactory.getLogger(QueuesListController.class);

	@Resource(name = "mqQueueManagerOptions")
	private Map<String, Object> mqQueueManagerOptions;

	@Value("${broker_hostname}")
	private String brokerHostname;

	@Value("${broker_port}")
	private Integer brokerPort;

	@Value("${broker_channel}")
	private String brokerChannel;

	@Autowired
	@Qualifier("mqQueueManager")
	private MQQueueManager qm;

	
	private boolean doMQSet(QueuesListPresenter presenter, QueueDescriptor queueDescriptor) {
		
		try {
			int shareability = queueDescriptor.getIsSherable().booleanValue() ? MQConstants.MQQA_SHAREABLE : MQConstants.MQQA_NOT_SHAREABLE;
			MQQueue queue = qm.accessQueue(queueDescriptor.getName(), MQConstants.MQOO_SET);
			queue.set(new int[] {MQConstants.MQIA_SHAREABILITY}, new int[] {shareability}, new byte[] {});
			queue.close();
			return true;
			
		} catch (MQException mqe) {
			if (LOG.isErrorEnabled())
				LOG.error(mqe + ": " + PCFConstants.lookupReasonCode(mqe.reasonCode));
			NotificationPopup popup = new NotificationPopup(presenter.getStage());
			popup.display(mqe + ": " + PCFConstants.lookupReasonCode(mqe.reasonCode));
			return false;
		}
	}
	
	public List<QueueDescriptor> getQueues(QueuesListPresenter presenter) {

		List<QueueDescriptor> queueList = new ArrayList<QueueDescriptor>();
		try {
			PCFMessageAgent agent;

			// Client connection (host, port, channel).

			agent = new PCFMessageAgent(brokerHostname, brokerPort, brokerChannel);

			PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q);

			request.addParameter(CMQC.MQCA_Q_NAME, "*");
			request.addParameter(CMQC.MQIA_Q_TYPE, MQConstants.MQQT_LOCAL);
			//			request.addFilterParameter(CMQC.MQIA_CURRENT_Q_DEPTH, CMQCFC.MQCFOP_GREATER, 0);

			PCFMessage[] responses = agent.send(request);

			for (int i = 0; i < responses.length; i++) {
				PCFMessage response = responses[i];

				QueueDescriptor queue = new QueueDescriptor();
				String qName = (String) response.getParameterValue(CMQC.MQCA_Q_NAME);
				if (qName != null) {

					String qDesc = (String) response.getParameterValue(CMQC.MQCA_Q_DESC);

					queue.setName(qName.trim());
					queue.setDescription(qDesc.trim());
					queue.setDept((Long) response.getParameterValue(CMQC.MQIA_CURRENT_Q_DEPTH));
					Integer sharability = (Integer) response.getParameterValue(CMQC.MQIA_SHAREABILITY); // CMQC.MQQA_NOT_SHAREABLE = 0 / CMQC.MQQA_SHAREABLE = 1;
					if(sharability.intValue() == CMQC.MQQA_SHAREABLE)
						queue.setIsSherable(Boolean.TRUE);
					else
						queue.setIsSherable(Boolean.FALSE);

					queueList.add(queue);
				}

				//				System.out.println("Queue " + response.getParameterValue(CMQC.MQCA_Q_NAME) + " depth "
				//						+ response.getParameterValue(CMQC.MQIA_CURRENT_Q_DEPTH));
			}

			if (LOG.isDebugEnabled())
				LOG.debug(responses.length + (responses.length == 1 ? " active queue" : " active queues"));
		}

		catch (MQException mqe) {
			if (LOG.isErrorEnabled())
				LOG.error(mqe + ": " + PCFConstants.lookupReasonCode(mqe.reasonCode));
			NotificationPopup popup = new NotificationPopup(presenter.getStage());
			popup.display(mqe + ": " + PCFConstants.lookupReasonCode(mqe.reasonCode));
		}

		catch (IOException ioe) {
			if (LOG.isErrorEnabled())
				LOG.error(ioe.getMessage());
			NotificationPopup popup = new NotificationPopup(presenter.getStage());
			popup.display(ioe.getMessage());
		}

		return queueList;
	}

}
