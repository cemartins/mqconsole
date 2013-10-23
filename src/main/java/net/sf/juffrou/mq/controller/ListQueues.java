package net.sf.juffrou.mq.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import javax.annotation.Resource;

import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.ui.Main;
import net.sf.juffrou.mq.ui.SpringFxmlLoader;

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
public class ListQueues implements Initializable {

	@FXML
	private TableView table;

	@Resource(name = "mqQueueManagerOptions")
	private Map<String, Object> mqQueueManagerOptions;

	@Value("${broker_hostname}")
	private String brokerHostname;

	@Value("${broker_port}")
	private Integer brokerPort;

	@Value("${broker_channel}")
	private String brokerChannel;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		ObservableList<QueueDescriptor> rows = FXCollections.observableArrayList();
		rows.addAll(getQueues());
		table.setItems(rows);

	}

	public void tableClick(MouseEvent event) {
		//		EventType<? extends Event> eventType = actionEvent.getEventType();
		//		EventTarget target = actionEvent.getTarget();
		if (event.getClickCount() > 1) {
			TableView tableView = (TableView) event.getSource();
			ObservableList<TablePosition> cells = tableView.getSelectionModel().getSelectedCells();
			for (TablePosition<?, ?> cell : cells) {
				QueueDescriptor queue = (QueueDescriptor) tableView.getItems().get(cell.getRow());

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

				System.out.println("Show messages in queue " + queue.getName());
			}
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

			System.out.println(responses.length + (responses.length == 1 ? " active queue" : " active queues"));
		}

		catch (MQException mqe) {
			System.err.println(mqe + ": " + PCFConstants.lookupReasonCode(mqe.reasonCode));
		}

		catch (IOException ioe) {
			System.err.println(ioe);
		}

		catch (ArrayIndexOutOfBoundsException abe) {
			System.err.println("Usage: java " + ListQueues.class.getName()
					+ " local-queue-manager-name | host port channel");
		}

		return queueList;
	}
}
