package net.sf.juffrou.mq.activemq.util;

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Message;

import net.sf.juffrou.mq.dom.HeaderDescriptor;
import net.sf.juffrou.mq.dom.MessageDescriptor;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveMqMessageDescriptorHelper {

	private static final Logger log = LoggerFactory.getLogger(ActiveMqMessageDescriptorHelper.class);
	
	private static final String ACTIVEMQ_HEADER_SEQUENCE_ID = "JMSXGroupID";
	private static final String ACTIVEMQ_HEADER_SEQUENCE_NUMBER = "JMSXGroupSeq";

	public static MessageDescriptor createMessageDescriptor(Message message) throws JMSException {
		MessageDescriptor messageDescriptor = new MessageDescriptor();

		readMessageHeaders(messageDescriptor, message);
		if(message != null) {
			if(message instanceof ActiveMQTextMessage)
				messageDescriptor.setText(((ActiveMQTextMessage)message).getText());
			else
				messageDescriptor.setText(message.toString());
		}

		return messageDescriptor;
	}

	public static void setMessageHeaders(Message message,
			MessageDescriptor messageDescriptor) throws JMSException {

		if (messageDescriptor.getHeaders() != null) {
			for (HeaderDescriptor hdesc : messageDescriptor.getHeaders()) {
				String key = hdesc.getName();
				if (key.isEmpty())
					continue;
				int folderPos = key.indexOf('.');
				if (folderPos != -1) {
					String folder = key.substring(0, folderPos);
					if (!folder.equalsIgnoreCase("usr"))
						continue;
					key = key.substring(folderPos + 1);
				}

				if (key.equals(HeaderDescriptor.HEADER_MESSAGE_ID)
						|| key.equals(HeaderDescriptor.HEADER_PUT_DATETIME))
					continue;

				if (key.equals(HeaderDescriptor.HEADER_CORRELATION_ID))
					message.setJMSCorrelationID(hdesc.getValueAsString());
				else if (key.equals(HeaderDescriptor.HEADER_SEQUENCE_ID))
					message.setStringProperty(ACTIVEMQ_HEADER_SEQUENCE_ID, hdesc.getValueAsString());
				else if (key.equals(HeaderDescriptor.HEADER_SEQUENCE_NUMBER))
					message.setIntProperty(ACTIVEMQ_HEADER_SEQUENCE_NUMBER, Integer.parseInt(hdesc.getValueAsString()));
				else {
					try {
						message.setIntProperty(key,	Integer.parseInt(hdesc.getValueAsString()));
					} catch (NumberFormatException e) {
						try {
							message.setDoubleProperty(key, Double.parseDouble(hdesc.getValueAsString()));
						} catch (NumberFormatException ee) {
							message.setStringProperty(key, hdesc.getValueAsString());
						}
					}
				}
			}
		}
	}

	private static void readMessageHeaders(MessageDescriptor messageDescriptor,
			Message message) throws JMSException {

		if (message != null) {
			messageDescriptor.setMessageId(message.getJMSMessageID());
			messageDescriptor.setCorrelationId(message.getJMSCorrelationID());
			Enumeration propertyNames = message.getPropertyNames();
			if (propertyNames != null)
				while (propertyNames.hasMoreElements()) {
					String propertyName = (String) propertyNames.nextElement();
					if(ACTIVEMQ_HEADER_SEQUENCE_ID.equals(propertyName))
						messageDescriptor.addHeader(HeaderDescriptor.HEADER_SEQUENCE_ID, message.getObjectProperty(propertyName));
					else
						if(ACTIVEMQ_HEADER_SEQUENCE_NUMBER.equals(propertyName))
							messageDescriptor.addHeader(HeaderDescriptor.HEADER_SEQUENCE_NUMBER, message.getObjectProperty(propertyName));
						else
							messageDescriptor.addHeader(propertyName, message.getObjectProperty(propertyName));
				}
		}
	}

}
