package net.sf.juffrou.mq.activemq.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.JMSException;

import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.error.CannotReadQueuesException;
import net.sf.juffrou.mq.queues.QueuesListController;
import net.sf.juffrou.mq.queues.presenter.QueuesListPresenter;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.BrokerInfo;
import org.apache.activemq.management.JMSConnectionStatsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class ActiveMqQueuesListControllerImpl implements QueuesListController {
	
	protected static final Logger LOG = LoggerFactory.getLogger(QueuesListController.class);

	@Resource(name = "mqQueueManagerOptions")
	private Map<String, Object> mqQueueManagerOptions;

	@Value("${broker_hostname}")
	private String brokerHostname;

	@Value("${broker_port}")
	private Integer brokerPort;

	@Autowired
	private JmsTemplate jmsTemplate;
	
	@Autowired
	ActiveMQConnectionFactory connectionFactory;

	public List<QueueDescriptor> getQueues(QueuesListPresenter presenter) {

		List<QueueDescriptor> queueList = new ArrayList<QueueDescriptor>();
		
		ActiveMQConnection jmsConnection = null;
		try {
			Connection connection = connectionFactory.createConnection();
			jmsConnection = (ActiveMQConnection) connection;
			jmsConnection.start();
			BrokerInfo brokerInfo = jmsConnection.getBrokerInfo();
			Set<ActiveMQQueue> queues;
			JMSConnectionStatsImpl connectionStats = jmsConnection.getConnectionStats();
			String[] statisticNames = connectionStats.getStatisticNames();
			DestinationSource destinationSource = jmsConnection.getDestinationSource();
			destinationSource.start();
			queues = destinationSource.getQueues();
			for(ActiveMQQueue mqQueue : queues) {
				
				QueueDescriptor queue = new QueueDescriptor();
				queue.setName(mqQueue.getQueueName());
				queue.setDescription(mqQueue.getQualifiedName());
				Properties properties = mqQueue.getProperties();
				
				
				/*
				queue.setDept((Integer) response.getParameterValue(CMQC.MQIA_CURRENT_Q_DEPTH));
				Integer sharability = (Integer) response.getParameterValue(CMQC.MQIA_SHAREABILITY); // CMQC.MQQA_NOT_SHAREABLE = 0 / CMQC.MQQA_SHAREABLE = 1;
				if(sharability.intValue() == CMQC.MQQA_SHAREABLE)
					queue.setIsSherable(Boolean.TRUE);
				else
					queue.setIsSherable(Boolean.FALSE);
					
					*/

				queueList.add(queue);
				
			}
		} catch (JMSException e) {
			if(LOG.isErrorEnabled())
				LOG.error("Cannot read list of Queues", e);
			throw new CannotReadQueuesException("Cannot read list of Queues", e);
		}
		finally {
			if(jmsConnection != null)
				try {
					jmsConnection.stop();
					jmsConnection.close();
				} catch (JMSException e) {
				}
		}
		
		return queueList;
	}

}
