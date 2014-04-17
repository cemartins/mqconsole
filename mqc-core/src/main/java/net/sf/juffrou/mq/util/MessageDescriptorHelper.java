package net.sf.juffrou.mq.util;

import java.io.IOException;

import net.sf.juffrou.mq.dom.HeaderDescriptor;
import net.sf.juffrou.mq.dom.MessageDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.MQHeader;
import com.ibm.mq.headers.MQHeaderIterator;
import com.ibm.mq.headers.MQHeaderList;
import com.ibm.mq.headers.MQRFH2;
import com.ibm.mq.headers.MQRFH2.Element;

public class MessageDescriptorHelper {

	private static final Logger log = LoggerFactory.getLogger(MessageDescriptorHelper.class);

	public static MessageDescriptor createMessageDescriptor(MQMessage message) throws IOException, MQDataException {
		MessageDescriptor messageDescriptor = new MessageDescriptor();
		messageDescriptor.setMessageId(new String(message.messageId));
		messageDescriptor.setCorrelationId(new String(message.correlationId));

		int totalMessageLength = message.getTotalMessageLength();
		int msglen = message.getMessageLength();
		MQRFH2 header = null;

		MQHeaderList list = new MQHeaderList(message);
		int indexOf = list.indexOf("MQRFH2");
		if (indexOf >= 0) {
			header = (MQRFH2) list.get(indexOf);
			msglen = msglen - header.size();
		}

		readMessageHeaders(messageDescriptor, message, header);

		try {
			String msgTest = message.readStringOfCharLength(msglen);
			messageDescriptor.setText(msgTest);
		} catch (IOException e) {
			if (log.isErrorEnabled())
				log.error("Cannot read message text: " + e.getMessage());
		}

		return messageDescriptor;
	}

	public static void setMessageHeaders(MQMessage message, MessageDescriptor messageDescriptor) throws MQException {

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
				
				if(key.equals(HeaderDescriptor.HEADER_SEQUENCE_ID))
					message.groupId = hdesc.getValue() != null ? hdesc.getValueAsString().getBytes() : MQConstants.MQGI_NONE;

				if(key.equals(HeaderDescriptor.HEADER_SEQUENCE_NUMBER))
					message.messageSequenceNumber = Integer.parseInt(hdesc.getValueAsString());
				
//				if(key.equals(HeaderDescriptor.HEADER_SEQUENCE_SIZE))
//					message.
				
				if(key.equals(HeaderDescriptor.HEADER_CORRELATION_ID))
					message.correlationId = hdesc.getValue() == null || hdesc.getValueAsString().isEmpty() ? MQConstants.MQCI_NONE : hdesc.getValueAsString().getBytes();
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

	private static void readMessageHeaders(MessageDescriptor messageDescriptor, MQMessage message, MQRFH2 rfh2) throws IOException {

		if (rfh2 != null) {
			for (Element folder : rfh2.getFolders()) {
				String prefix = folder.getName() + ".";
				for (Element element : folder.getChildren()) {
					messageDescriptor.addHeader(prefix + element.getName(), element.getValue());
				}
			}
		} else if (message != null) {
			MQHeaderIterator it = new MQHeaderIterator(message);
			while (it.hasNext()) {
				MQHeader header = it.nextHeader();
				String key = header.toString();
				messageDescriptor.addHeader(key, key);
			}
		}
		
		if (message != null) {
			messageDescriptor.addHeader(HeaderDescriptor.HEADER_MESSAGE_ID, new String(message.messageId));
			messageDescriptor.addHeader(HeaderDescriptor.HEADER_CORRELATION_ID, new String(message.correlationId));
		}
	}

}
