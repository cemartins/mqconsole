package net.sf.juffrou.mq.hornetq.util;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.springframework.jms.core.JmsTemplate;

import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.error.CannotFindDestinationException;

public class HornetQDestinationResolver {

	
	public static Destination resolveDestination(JmsTemplate template, QueueDescriptor destinationDescriptor) {
		return resolveDestination(template, destinationDescriptor.getName());
	}

	public static Destination resolveDestination(JmsTemplate template, String destinationName) {
		Connection connection = null;
		Session session = null;
		Destination destination = null;
		try {
			connection = template.getConnectionFactory().createConnection();
			session = connection.createSession();
			destination = resolveDestination(template, session, destinationName);
		} catch (JMSException e) {
			e.printStackTrace();
		} finally {
			try {
				if(session != null)
					session.close();
				if(connection != null)
					connection.close();
			} catch (JMSException e) {
				e.printStackTrace();
				throw new CannotFindDestinationException("Cannot find " + destinationName, e);
			}
		}
		return destination;
	}

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
