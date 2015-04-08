package net.sf.juffrou.mq.websphere.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.error.CannotReadQueuesException;
import net.sf.juffrou.mq.queues.QueuesListController;
import net.sf.juffrou.mq.queues.presenter.QueuesListPresenter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ibm.mq.MQException;
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

					queue.setId(qName.trim());
					queue.setName(qName.trim());
					queue.setDescription(qDesc.trim());
					Long dept = new Long(response.getParameterValue(CMQC.MQIA_CURRENT_Q_DEPTH).toString());
					queue.setDept(dept);
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
			throw new CannotReadQueuesException(mqe + ": " + PCFConstants.lookupReasonCode(mqe.reasonCode), mqe);
		}

		catch (IOException ioe) {
			if (LOG.isErrorEnabled())
				LOG.error(ioe.getMessage());
			throw new CannotReadQueuesException(ioe.getMessage(), ioe);
		}

		return queueList;
	}

}
