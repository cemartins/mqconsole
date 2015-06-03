package net.sf.juffrou.mq.websphere.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.error.CannotReadQueuesException;
import net.sf.juffrou.mq.messages.task.AbstractQueueFetchingTask;

import com.ibm.mq.MQException;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.CMQC;
import com.ibm.mq.pcf.CMQCFC;
import com.ibm.mq.pcf.PCFConstants;
import com.ibm.mq.pcf.PCFMessage;
import com.ibm.mq.pcf.PCFMessageAgent;

public class WebsphereQueueFetchingTask extends AbstractQueueFetchingTask {

	private String brokerHostname;

	private Integer brokerPort;

	private String brokerChannel;
	
	public WebsphereQueueFetchingTask(String brokerHostname, Integer brokerPort, String brokerChannel) {
		this.brokerHostname = brokerHostname;
		this.brokerPort = brokerPort;
		this.brokerChannel = brokerChannel;
	}

	@Override
	public List<QueueDescriptor> fetchQueues() {
		List<QueueDescriptor> queueList = new ArrayList<QueueDescriptor>();
		try {
			PCFMessageAgent agent;

			// Client connection (host, port, channel).
			updateMessage("Connectiong to websphere broker at " + brokerHostname + ":" + brokerPort.toString() + " (channel=" + brokerChannel + ")...");
			agent = new PCFMessageAgent(brokerHostname, brokerPort, brokerChannel);

			updateMessage("Querying broker to obtain queues...");

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

					updateMessage("Found queue " + qName);
					
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
			
			updateMessage("Fetched all queues");

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
