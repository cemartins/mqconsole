package net.sf.juffrou.mq.util;

import javafx.stage.Stage;
import net.sf.juffrou.mq.dom.MessageDescriptor;

public interface MessageReceivedHandler {

	void messageReceived(MessageDescriptor messageDescriptor);

	Stage getStage();

}
