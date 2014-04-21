package net.sf.juffrou.mq.activemq.util;

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Message;

import net.sf.juffrou.mq.dom.HeaderDescriptor;
import net.sf.juffrou.mq.dom.MessageDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveMqMessageDescriptorHelper {

	private static final Logger log = LoggerFactory.getLogger(ActiveMqMessageDescriptorHelper.class);

	public static MessageDescriptor createMessageDescriptor(Message message) throws JMSException {
		MessageDescriptor messageDescriptor = new MessageDescriptor();

		readMessageHeaders(messageDescriptor, message);
		messageDescriptor.setText(message.toString());
		
		return messageDescriptor;
	}

	public static void setMessageHeaders(Message message, MessageDescriptor messageDescriptor) throws JMSException {

		if(messageDescriptor.getHeaders() != null) {
			for(HeaderDescriptor hdesc : messageDescriptor.getHeaders()) {
				String key = hdesc.getName();
				if(key.isEmpty())
					continue;
				int folderPos = key.indexOf('.');
				if(folderPos != -1) {
					String folder = key.substring(0, folderPos);
					if( !folder.equalsIgnoreCase("usr") )
						continue;
					key = key.substring(folderPos + 1);
				}
				
				if(key.equals(HeaderDescriptor.HEADER_MESSAGE_ID) || key.equals(HeaderDescriptor.HEADER_PUT_DATETIME))
					continue;
				
				if(key.equals(HeaderDescriptor.HEADER_CORRELATION_ID))
					message.setJMSCorrelationID(hdesc.getValueAsString());
				else {
					try {
						message.setIntProperty(key, Integer.parseInt(hdesc.getValueAsString()));
					}
					catch(NumberFormatException e) {
						try {
							message.setDoubleProperty(key, Double.parseDouble(hdesc.getValueAsString()));
						}
						catch(NumberFormatException ee) {
								message.setStringProperty(key, hdesc.getValueAsString());
						}
					}
				}
			}
		}
	}

	private static void readMessageHeaders(MessageDescriptor messageDescriptor, Message message) throws JMSException {

		if (message != null) {
			messageDescriptor.setMessageId(message.getJMSMessageID());
			messageDescriptor.setCorrelationId(message.getJMSCorrelationID());
			Enumeration propertyNames = message.getPropertyNames();
			if(propertyNames != null)
				while(propertyNames.hasMoreElements()) {
					String propertyName = (String) propertyNames.nextElement();
					messageDescriptor.addHeader(propertyName, message.getObjectProperty(propertyName));
				}
		}
	}

}
