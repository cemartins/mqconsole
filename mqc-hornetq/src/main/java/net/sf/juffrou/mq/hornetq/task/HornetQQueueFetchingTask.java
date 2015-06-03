package net.sf.juffrou.mq.hornetq.task;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueRequestor;

import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.management.JMSManagementHelper;
import org.hornetq.jms.client.HornetQConnection;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.hornetq.jms.client.HornetQSession;

import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.error.CannotReadQueuesException;
import net.sf.juffrou.mq.messages.task.AbstractQueueFetchingTask;

public class HornetQQueueFetchingTask extends AbstractQueueFetchingTask {
	
	private HornetQConnectionFactory connectionFactory;
	
	public HornetQQueueFetchingTask(HornetQConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	@Override
	public List<QueueDescriptor> fetchQueues() {
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
					queue.setId(queueName);
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
