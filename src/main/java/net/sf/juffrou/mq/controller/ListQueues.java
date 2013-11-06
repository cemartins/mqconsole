package net.sf.juffrou.mq.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.annotation.Resource;

import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.ui.Main;
import net.sf.juffrou.mq.ui.NotificationPopup;
import net.sf.juffrou.mq.ui.SpringFxmlLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ibm.mq.MQC;
import com.ibm.mq.MQException;
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

	public void initialize() {
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		ObservableList<QueueDescriptor> rows = FXCollections.observableArrayList();
		rows.addAll(getQueues());
		table.setItems(rows);
	}

	public void listenToNewMessages(ActionEvent event) {
		ObservableList<TablePosition> cells = table.getSelectionModel().getSelectedCells();
		for (TablePosition<?, ?> cell : cells) {
			QueueDescriptor queue = table.getItems().get(cell.getRow());
			messageListenerController.startMessageListener(getStage(), queue.getName());
		}

	}

	public void openMessageList(ActionEvent event) {
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

	public void sendMessage(ActionEvent event) {
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

	private List<QueueDescriptor> getQueues() {

		List<QueueDescriptor> queueList = new ArrayList<QueueDescriptor>();
		try {
			PCFMessageAgent agent;

			// Client connection (host, port, channel).

			agent = new PCFMessageAgent(brokerHostname, brokerPort, brokerChannel);

			PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q);

			request.addParameter(CMQC.MQCA_Q_NAME, "*");
			request.addParameter(CMQC.MQIA_Q_TYPE, MQC.MQQT_LOCAL);
			//			request.addFilterParameter(CMQC.MQIA_CURRENT_Q_DEPTH, CMQCFC.MQCFOP_GREATER, 0);

			PCFMessage[] responses = agent.send(request);

			for (int i = 0; i < responses.length; i++) {
				PCFMessage response = responses[i];

				QueueDescriptor queue = new QueueDescriptor();
				String qName = (String) response.getParameterValue(CMQC.MQCA_Q_NAME);
				if (qName != null) {

					queue.setName(qName.trim());
					queue.setDept((Integer) response.getParameterValue(CMQC.MQIA_CURRENT_Q_DEPTH));

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

	public void setStage(Stage stage) {
		this.stage = stage;
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent we) {
				messageListenerController.stopMessageListener();
			}
		});
	}

	private Stage getStage() {
		return stage;
	}
}
