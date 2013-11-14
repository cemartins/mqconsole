package net.sf.juffrou.mq.util;

import java.io.IOException;

import net.sf.juffrou.mq.dom.MessageDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.mq.MQMessage;
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
	}

}
