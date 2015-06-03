package net.sf.juffrou.mq.activemq.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.juffrou.mq.activemq.task.ActiveMqQueueFetchingTask;
import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.messages.task.AbstractQueueFetchingTask;
import net.sf.juffrou.mq.queues.QueuesListController;
import net.sf.juffrou.mq.queues.presenter.QueuesListPresenter;

import org.apache.activemq.ActiveMQConnectionFactory;
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

	@Value("${broker_timeout}")
	private Integer brokerTimeout;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	ActiveMQConnectionFactory connectionFactory;

	public List<QueueDescriptor> getQueues(QueuesListPresenter presenter) {
		ActiveMqQueueFetchingTask queueFetchingTask = new ActiveMqQueueFetchingTask(connectionFactory, brokerTimeout);
		return queueFetchingTask.fetchQueues();
	}

	@Override
	public AbstractQueueFetchingTask getQueueFetchingTask(QueuesListPresenter presenter) {
		ActiveMqQueueFetchingTask queueFetchingTask = new ActiveMqQueueFetchingTask(connectionFactory, brokerTimeout);
		return queueFetchingTask;
	}

}
