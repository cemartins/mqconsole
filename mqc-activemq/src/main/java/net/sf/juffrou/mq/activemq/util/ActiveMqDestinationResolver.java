package net.sf.juffrou.mq.activemq.util;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.error.CannotFindDestinationException;

import org.springframework.jms.core.JmsTemplate;

public class ActiveMqDestinationResolver {

	
	public static Destination resolveDestination(JmsTemplate template, Session session, QueueDescriptor destination) {
		return resolveDestination(template, session, destination.getName());
	}

	public static Destination resolveDestination(JmsTemplate template, Session session, String destinationName) {
		
		Destination destination = null;
		try {
			destination = template.getDestinationResolver().resolveDestinationName(session, destinationName, false);
		} catch (JMSException e) {
			try {
				destination = template.getDestinationResolver().resolveDestinationName(session, destinationName, true);
			} catch (JMSException e1) {
				e1.printStackTrace();
				throw new CannotFindDestinationException("Cannot find " + destinationName, e);
			}
		}
		return destination;
	}

}
