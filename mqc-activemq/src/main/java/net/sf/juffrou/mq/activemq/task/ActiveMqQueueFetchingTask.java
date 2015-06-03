package net.sf.juffrou.mq.activemq.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQQueue;

import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.error.CannotReadQueuesException;
import net.sf.juffrou.mq.messages.task.AbstractQueueFetchingTask;

public class ActiveMqQueueFetchingTask extends AbstractQueueFetchingTask {

	private static final String ACTIVEMQ_STATISTICS_QUEUE_PREFIX = "ActiveMQ.Statistics.";
	
	private ActiveMQConnectionFactory connectionFactory;
	private Integer brokerTimeout;

	
	public ActiveMqQueueFetchingTask(ActiveMQConnectionFactory connectionFactory, Integer brokerTimeout) {
		this.connectionFactory = connectionFactory;
		this.brokerTimeout = brokerTimeout;
	}

	@Override
	public List<QueueDescriptor> fetchQueues() {
		List<QueueDescriptor> queueList = new ArrayList<QueueDescriptor>();
		
		
//		getStatistics("Broker");

		ActiveMQConnection jmsConnection = null;
		try {
			Connection connection = connectionFactory.createConnection();
			jmsConnection = (ActiveMQConnection) connection;
			jmsConnection.start();
			DestinationSource destinationSource = jmsConnection.getDestinationSource();
			destinationSource.start();
			Set<ActiveMQQueue> queues = destinationSource.getQueues();
			for (ActiveMQQueue mqQueue : queues) {

				MapMessage statistics = getStatistics("Destination." + mqQueue.getQueueName());
				
				QueueDescriptor queue = new QueueDescriptor();
				queue.setId(mqQueue.getQueueName());
				queue.setName(mqQueue.getQueueName());
				queue.setDescription(mqQueue.getQualifiedName());
				queue.setDept(statistics.getLong("size"));

				queueList.add(queue);

			}
		} catch (JMSException e) {
			if (LOG.isErrorEnabled())
				LOG.error("Cannot read list of Queues", e);
			throw new CannotReadQueuesException("Cannot read list of Queues", e);
		} finally {
			if (jmsConnection != null)
				try {
					jmsConnection.stop();
					jmsConnection.close();
				} catch (JMSException e) {
				}
		}

		return queueList;
	}

	/**
	 * Inquire the statistics plugin to obtain details about a target.
	 * Targets can be "Destination.<name>" or "Broker"
	 * The StatisticsPlugin must be activated in the ActiveMQ broker for this to work.
	 * @param target
	 * @return
	 */
	private MapMessage getStatistics(String target) {

		ActiveMQConnection jmsConnection = null;

		try {
			
			Connection connection = connectionFactory.createConnection();
			jmsConnection = (ActiveMQConnection) connection;
			jmsConnection.start();
			Session session = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	
			Queue replyTo = session.createTemporaryQueue();
			MessageConsumer consumer = session.createConsumer(replyTo);
			 
			String queueName = ACTIVEMQ_STATISTICS_QUEUE_PREFIX + target;
			Queue testQueue = session.createQueue(queueName);
			MessageProducer producer = session.createProducer(testQueue);
			Message msg = session.createMessage();
			msg.setJMSReplyTo(replyTo);
			producer.send(msg);
			 
			MapMessage reply = (MapMessage) consumer.receive(brokerTimeout);
			
			jmsConnection.destroyDestination((ActiveMQQueue)testQueue);
			
			if(reply == null)
				throw new CannotReadQueuesException("Cannot read list of Queues. StatisticsPlugin did not respond.");
			
			return reply;
			/*
			for (Enumeration e = reply.getMapNames();e.hasMoreElements();) {
			  String name = e.nextElement().toString();
			  System.out.println(name + "=" + reply.getObject(name));
			}
			*/
		}
		catch(JMSException e) {
			if (LOG.isErrorEnabled())
				LOG.error("Cannot read list of Queues", e);
			throw new CannotReadQueuesException("Cannot read list of Queues", e);
		} finally {
			if (jmsConnection != null)
				try {
					jmsConnection.stop();
					jmsConnection.close();
				} catch (JMSException e) {
				}
		}
	}

}
