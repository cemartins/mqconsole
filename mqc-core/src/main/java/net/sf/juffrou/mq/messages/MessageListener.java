package net.sf.juffrou.mq.messages;

import javafx.stage.Stage;

public interface MessageListener {

	boolean isCurrentListeningQueue(String listeningQueue);
	
	void stopMessageListener();
	
	void startMessageListener(Stage parentStage, String listeningQueue);
}
