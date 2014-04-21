package net.sf.juffrou.mq.activemq.controller;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import net.sf.juffrou.mq.activemq.util.ActiveMqMessageDescriptorHelper;
import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.messages.MessagesListController;
import net.sf.juffrou.mq.messages.presenter.MessagesListPresenter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class ActiveMqMessagesListControllerImpl implements MessagesListController {

	protected static final Logger LOG = LoggerFactory.getLogger(ActiveMqMessagesListControllerImpl.class);
	
	@Autowired
	private JmsTemplate jmsTemplate;



	public List<MessageDescriptor> listMessages(MessagesListPresenter presenter, String queueName) {
				
		List<MessageDescriptor> messageList = jmsTemplate.browse(new MyCallback());

		return messageList;
	}
	
	public static class MyCallback implements BrowserCallback<List<MessageDescriptor>> {

		@Override
		public List<MessageDescriptor> doInJms(Session session, QueueBrowser browser) throws JMSException {
			
			List<MessageDescriptor> messageList = new ArrayList<MessageDescriptor>();
			
			Enumeration enumeration = browser.getEnumeration();
			while(enumeration.hasMoreElements()) {
				Message msg = (Message) enumeration.nextElement();
				MessageDescriptor msgDescriptor = ActiveMqMessageDescriptorHelper.createMessageDescriptor(msg);
				messageList.add(msgDescriptor);
			}
			
			return messageList;
		}
		
	}

}
