package net.sf.juffrou.mq.dom;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TreeMap;

import com.ibm.mq.MQMessage;
import com.ibm.mq.headers.MQHeader;
import com.ibm.mq.headers.MQHeaderIterator;
import com.ibm.mq.headers.MQRFH2;
import com.ibm.mq.headers.MQRFH2.Element;

/**
 * Mini class to hold the information of the message
 */
public class ReturnInfo {

	public MQMessage replyMessage;
	public Map<String, Object> headers;
	public String messageText;

	/**
	 * Transform the jms message headers into a map
	 * 
	 * @param message
	 * @return
	 */
	public static Map<String, Object> getMessageHeaders(MQMessage message, MQRFH2 rfh2) {
		Map<String, Object> headers = new TreeMap<String, Object>();

		if (rfh2 != null) {
			try {
				try {
					String nmr = rfh2.getStringFieldValue("usr", "norma_message_request_type");
					headers.put("norma_message_request_type", nmr);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				for (Element folder : rfh2.getFolders()) {
					String prefix = folder.getName() + ".";
					for (Element element : folder.getChildren()) {
						headers.put(prefix + element.getName(), element.getValue());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (message != null) {
			MQHeaderIterator it = new MQHeaderIterator(message);
			while (it.hasNext()) {
				MQHeader header = it.nextHeader();
				String key = header.toString();
				headers.put(key, key);
			}
		}

		if (message != null) {
			headers.put("MQMDmessageId", message.messageId == null ? "null" : "'" + new String(message.messageId) + "'");
			GregorianCalendar putDateTime = message.putDateTime;
			headers.put("MQMDputDateTime", putDateTime == null ? "null" : putDateTime.getTime().toString());
			headers.put("MQMDcorrelationId", message.correlationId == null ? "null" : "'"
					+ new String(message.correlationId) + "'");
		}
		return headers;
	}

}
