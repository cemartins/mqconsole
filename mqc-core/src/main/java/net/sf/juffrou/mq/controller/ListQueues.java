package net.sf.juffrou.mq.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import javax.annotation.Resource;

import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.ui.Main;
import net.sf.juffrou.mq.ui.NotificationPopup;
import net.sf.juffrou.mq.ui.SpringFxmlLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.CMQC;
import com.ibm.mq.pcf.CMQCFC;
import com.ibm.mq.pcf.PCFConstants;
import com.ibm.mq.pcf.PCFMessage;
import com.ibm.mq.pcf.PCFMessageAgent;

@Component
public class ListQueues {

	private static final Logger log = LoggerFactory.getLogger(ListQueues.class);

	private Stage stage;

	@FXML
	private TableView<QueueDescriptor> table;
	
	@FXML
	private MenuItem miListenToNewMessages;

	@Resource(name = "mqQueueManagerOptions")
	private Map<String, Object> mqQueueManagerOptions;

	@Value("${broker_hostname}")
	private String brokerHostname;

	@Value("${broker_port}")
	private Integer brokerPort;

	@Value("${broker_channel}")
	private String brokerChannel;

	@Autowired
	private MessageListenerController messageListenerController;

	@Autowired
	@Qualifier("mqQueueManager")
	private MQQueueManager qm;

	public void initialize() {
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		ObservableList<QueueDescriptor> rows = FXCollections.observableArrayList();
		rows.addAll(getQueues());
		table.setItems(rows);
	}

	@FXML
	private void listenToNewMessagesAction(ActionEvent event) {
		ObservableList<TablePosition> cells = table.getSelectionModel().getSelectedCells();
		for (TablePosition<?, ?> cell : cells) {
			QueueDescriptor queue = table.getItems().get(cell.getRow());
			if(messageListenerController.isCurrentListeningQueue(queue.getName()))
				messageListenerController.stopMessageListener();
			else
				messageListenerController.startMessageListener(getStage(), queue.getName());
		}

	}
	
	@FXML
	private void contextMenuOnShowingAction() {
		String listenerText = "Listen to New Messages";
		ObservableList<TablePosition> cells = table.getSelectionModel().getSelectedCells();
		for (TablePosition<?, ?> cell : cells) {
			QueueDescriptor queue = table.getItems().get(cell.getRow());
			if(messageListenerController.isCurrentListeningQueue(queue.getName()))
				listenerText = "Stop Listening to New Messages";
		}
		miListenToNewMessages.setText(listenerText);
	}
	
	@FXML
	private void toggleShare(ActionEvent event) {

		ObservableList<TablePosition> cells = table.getSelectionModel().getSelectedCells();
		for (TablePosition<?, ?> cell : cells) {
			QueueDescriptor queue = table.getItems().get(cell.getRow());
			queue.setIsSherable(new Boolean( ! queue.getIsSherable().booleanValue() ));
			if( ! doMQSet(queue))
				queue.setIsSherable(new Boolean( ! queue.getIsSherable().booleanValue() ));
		}

	}

	@FXML
	private void openMessageList(ActionEvent event) {
		ObservableList<TablePosition> cells = table.getSelectionModel().getSelectedCells();
		for (TablePosition<?, ?> cell : cells) {
			QueueDescriptor queue = table.getItems().get(cell.getRow());

			SpringFxmlLoader springFxmlLoader = new SpringFxmlLoader(Main.applicationContext);
			Parent root = (Parent) springFxmlLoader.load("/net/sf/juffrou/mq/ui/list-messages.fxml");

			ListMessages controller = springFxmlLoader.<ListMessages> getController();
			controller.setQueueName(queue.getName());
			controller.initialize();

			Scene scene = new Scene(root, 768, 480);
			Stage stage = new Stage();
			stage.setScene(scene);
			stage.setTitle(queue.getName() + " Messages");
			stage.show();

			if (log.isDebugEnabled())
				log.debug("Show messages in queue " + queue.getName());
		}
	}

	@FXML
	private void sendMessage(ActionEvent event) {
		ObservableList<TablePosition> cells = table.getSelectionModel().getSelectedCells();
		for (TablePosition<?, ?> cell : cells) {
			QueueDescriptor queue = table.getItems().get(cell.getRow());

			SpringFxmlLoader springFxmlLoader = new SpringFxmlLoader(Main.applicationContext);
			Parent root = (Parent) springFxmlLoader.load("/net/sf/juffrou/mq/ui/message-send.fxml");

			MessageSendControler controller = springFxmlLoader.<MessageSendControler> getController();
			controller.setQueueNameSend(queue.getName());
			controller.setQueueDescriptors(table.getItems());

			Scene scene = new Scene(root, 768, 480);
			Stage stage = new Stage();
			stage.setScene(scene);
			stage.setTitle("Send new message to queue " + queue.getName());
			stage.show();

			if (log.isDebugEnabled())
				log.debug("Send new message to queue " + queue.getName());
		}
	}

	@FXML
	private void refreshButtonAction(ActionEvent event) {
		ObservableList<QueueDescriptor> rows = FXCollections.observableArrayList();
		rows.addAll(getQueues());
		table.setItems(rows);
	}
	
	private boolean doMQSet(QueueDescriptor queueDescriptor) {
		
		try {
			int shareability = queueDescriptor.getIsSherable().booleanValue() ? MQConstants.MQQA_SHAREABLE : MQConstants.MQQA_NOT_SHAREABLE;
			MQQueue queue = qm.accessQueue(queueDescriptor.getName(), MQConstants.MQOO_SET);
			queue.set(new int[] {MQConstants.MQIA_SHAREABILITY}, new int[] {shareability}, new byte[] {});
			queue.close();
			return true;
			
		} catch (MQException mqe) {
			if (log.isErrorEnabled())
				log.error(mqe + ": " + PCFConstants.lookupReasonCode(mqe.reasonCode));
			NotificationPopup popup = new NotificationPopup(getStage());
			popup.display(mqe + ": " + PCFConstants.lookupReasonCode(mqe.reasonCode));
			return false;
		}
	}
	
	private List<QueueDescriptor> getQueues() {

		List<QueueDescriptor> queueList = new ArrayList<QueueDescriptor>();
		try {
			PCFMessageAgent agent;

			// Client connection (host, port, channel).

			agent = new PCFMessageAgent(brokerHostname, brokerPort, brokerChannel);

			PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q);

			request.addParameter(CMQC.MQCA_Q_NAME, "*");
			request.addParameter(CMQC.MQIA_Q_TYPE, MQConstants.MQQT_LOCAL);
			//			request.addFilterParameter(CMQC.MQIA_CURRENT_Q_DEPTH, CMQCFC.MQCFOP_GREATER, 0);

			PCFMessage[] responses = agent.send(request);

			for (int i = 0; i < responses.length; i++) {
				PCFMessage response = responses[i];

				QueueDescriptor queue = new QueueDescriptor();
				String qName = (String) response.getParameterValue(CMQC.MQCA_Q_NAME);
				if (qName != null) {

					String qDesc = (String) response.getParameterValue(CMQC.MQCA_Q_DESC);

					queue.setName(qName.trim());
					queue.setDescription(qDesc.trim());
					queue.setDept((Integer) response.getParameterValue(CMQC.MQIA_CURRENT_Q_DEPTH));
					Integer sharability = (Integer) response.getParameterValue(CMQC.MQIA_SHAREABILITY); // CMQC.MQQA_NOT_SHAREABLE = 0 / CMQC.MQQA_SHAREABLE = 1;
					if(sharability.intValue() == CMQC.MQQA_SHAREABLE)
						queue.setIsSherable(Boolean.TRUE);
					else
						queue.setIsSherable(Boolean.FALSE);

					queueList.add(queue);
				}

				//				System.out.println("Queue " + response.getParameterValue(CMQC.MQCA_Q_NAME) + " depth "
				//						+ response.getParameterValue(CMQC.MQIA_CURRENT_Q_DEPTH));
			}

			if (log.isDebugEnabled())
				log.debug(responses.length + (responses.length == 1 ? " active queue" : " active queues"));
		}

		catch (MQException mqe) {
			if (log.isErrorEnabled())
				log.error(mqe + ": " + PCFConstants.lookupReasonCode(mqe.reasonCode));
			NotificationPopup popup = new NotificationPopup(getStage());
			popup.display(mqe + ": " + PCFConstants.lookupReasonCode(mqe.reasonCode));
		}

		catch (IOException ioe) {
			if (log.isErrorEnabled())
				log.error(ioe.getMessage());
			NotificationPopup popup = new NotificationPopup(getStage());
			popup.display(ioe.getMessage());
		}

		return queueList;
	}

	public MessageListenerController getMessageListenerController() {
		return messageListenerController;
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	private Stage getStage() {
		return stage;
	}
}
