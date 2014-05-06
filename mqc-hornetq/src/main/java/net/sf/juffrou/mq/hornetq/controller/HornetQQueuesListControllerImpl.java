package net.sf.juffrou.mq.hornetq.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueRequestor;

import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.error.CannotReadQueuesException;
import net.sf.juffrou.mq.queues.QueuesListController;
import net.sf.juffrou.mq.queues.presenter.QueuesListPresenter;

import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.management.JMSManagementHelper;
import org.hornetq.jms.client.HornetQConnection;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.hornetq.jms.client.HornetQSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class HornetQQueuesListControllerImpl implements QueuesListController {

	protected static final Logger LOG = LoggerFactory.getLogger(QueuesListController.class);
	
	@Resource(name = "mqQueueManagerOptions")
	private Map<String, Object> mqQueueManagerOptions;

	@Value("${broker_hostname}")
	private String brokerHostname;

	@Value("${broker_port}")
	private Integer brokerPort;

	@Value("${broker_timeout}")
	private Integer brokerTimeout;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	HornetQConnectionFactory connectionFactory;

	public List<QueueDescriptor> getQueues(QueuesListPresenter presenter) {

		List<QueueDescriptor> queueList = new ArrayList<QueueDescriptor>();
		
		HornetQConnection jmsConnection = null;
		HornetQSession session = null;
		try {
			Connection connection = connectionFactory.createConnection();
			jmsConnection = (HornetQConnection) connection;
			
			session = (HornetQSession) jmsConnection.createSession();

			Queue managementQueue = HornetQJMSClient.createQueue("hornetq.management");

			QueueRequestor requestor = new QueueRequestor(session, managementQueue);

			jmsConnection.start();

			Message message = session.createMessage();
			JMSManagementHelper.putOperationInvocation(message, "core.server", "getQueueNames");
			Message reply = requestor.request(message);
			boolean success = JMSManagementHelper.hasOperationSucceeded(reply);
			if(!success)
				throw new CannotReadQueuesException("Cannot read list of Queues");
			
			Object result = JMSManagementHelper.getResult(reply);
			Object[] queueNames = (Object[]) result;
			for(Object queueNameObj : queueNames) {
				
				String queueName = (String) queueNameObj;
				
				boolean isTemporary = queueName.startsWith("jms.tempqueue");
				
				if(!isTemporary) {

					message = session.createMessage();
					JMSManagementHelper.putAttribute(message, queueName, "name");
					reply = requestor.request(message);
					success = JMSManagementHelper.hasOperationSucceeded(reply);
					if(!success) continue;
					String name = (String)JMSManagementHelper.getResult(reply);

					message = session.createMessage();
					JMSManagementHelper.putAttribute(message, queueName, "messageCount");
					reply = requestor.request(message);
					success = JMSManagementHelper.hasOperationSucceeded(reply);
					if(!success) continue;
					int messageCount = (Integer)JMSManagementHelper.getResult(reply);
					
					QueueDescriptor queue = new QueueDescriptor();
					queue.setName(name);
					queue.setDescription(queueName);
					queue.setDept(new Long(messageCount));
					queueList.add(queue);
				}
				
			}
			
		} catch (JMSException e) {
			if (LOG.isErrorEnabled())
				LOG.error("Cannot read list of Queues", e);
			throw new CannotReadQueuesException("Cannot read list of Queues", e);
		} catch (Exception e) {
			if (LOG.isErrorEnabled())
				LOG.error("Cannot read list of Queues", e);
			throw new CannotReadQueuesException("Cannot read list of Queues", e);
		} finally {
			try {
				if(session != null)
					session.close();
				if (jmsConnection != null) {
						jmsConnection.stop();
						jmsConnection.close();
				}
			} catch (JMSException e) {
			}
		}

		return queueList;
	}

}
