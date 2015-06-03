package net.sf.juffrou.mq.websphere.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.error.CannotReadQueuesException;
import net.sf.juffrou.mq.messages.task.AbstractQueueFetchingTask;
import net.sf.juffrou.mq.queues.QueuesListController;
import net.sf.juffrou.mq.queues.presenter.QueuesListPresenter;
import net.sf.juffrou.mq.websphere.task.WebsphereQueueFetchingTask;

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

		WebsphereQueueFetchingTask websphereQueueFetchingTask = new WebsphereQueueFetchingTask(brokerHostname, brokerPort, brokerChannel);
		List<QueueDescriptor> queues = websphereQueueFetchingTask.fetchQueues();
		
		return queues;
	}

	public AbstractQueueFetchingTask getQueueFetchingTask(QueuesListPresenter presenter) {
		
		return new WebsphereQueueFetchingTask(brokerHostname, brokerPort, brokerChannel);
	}
}
