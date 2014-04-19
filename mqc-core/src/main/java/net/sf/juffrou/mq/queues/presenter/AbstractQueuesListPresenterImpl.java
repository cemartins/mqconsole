package net.sf.juffrou.mq.queues.presenter;

import java.util.List;

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
import net.sf.juffrou.mq.MQConsole;
import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.messages.MessageListener;
import net.sf.juffrou.mq.messages.presenter.AbstractMessageSendPresenterImpl;
import net.sf.juffrou.mq.messages.presenter.AbstractMessagesListPresenterImpl;
import net.sf.juffrou.mq.queues.QueuesListPresenter;
import net.sf.juffrou.mq.ui.SpringFxmlLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractQueuesListPresenterImpl implements QueuesListPresenter {

	protected static final Logger LOG = LoggerFactory.getLogger(AbstractQueuesListPresenterImpl.class);

	private Stage stage;

	@FXML
	private TableView<QueueDescriptor> table;
	
	@FXML
	private MenuItem miListenToNewMessages;

	@Autowired
	private MessageListener messageListener;

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
			if(messageListener.isCurrentListeningQueue(queue.getName()))
				messageListener.stopMessageListener();
			else
				messageListener.startMessageListener(getStage(), queue.getName());
		}

	}
	
	@FXML
	private void contextMenuOnShowingAction() {
		String listenerText = "Listen to New Messages";
		ObservableList<TablePosition> cells = table.getSelectionModel().getSelectedCells();
		for (TablePosition<?, ?> cell : cells) {
			QueueDescriptor queue = table.getItems().get(cell.getRow());
			if(messageListener.isCurrentListeningQueue(queue.getName()))
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
//			if( ! doMQSet(queue))
//				queue.setIsSherable(new Boolean( ! queue.getIsSherable().booleanValue() ));
		}

	}

	@FXML
	private void openMessageList(ActionEvent event) {
		ObservableList<TablePosition> cells = table.getSelectionModel().getSelectedCells();
		for (TablePosition<?, ?> cell : cells) {
			QueueDescriptor queue = table.getItems().get(cell.getRow());

			SpringFxmlLoader springFxmlLoader = new SpringFxmlLoader(MQConsole.applicationContext);
			Parent root = (Parent) springFxmlLoader.load("/net/sf/juffrou/mq/ui/list-messages.fxml");

			AbstractMessagesListPresenterImpl controller = springFxmlLoader.<AbstractMessagesListPresenterImpl> getController();
			controller.setQueueName(queue.getName());
			controller.initialize();

			Scene scene = new Scene(root, 768, 480);
			Stage stage = new Stage();
			stage.setScene(scene);
			stage.setTitle(queue.getName() + " Messages");
			stage.show();

			if (LOG.isDebugEnabled())
				LOG.debug("Show messages in queue " + queue.getName());
		}
	}

	@FXML
	private void sendMessage(ActionEvent event) {
		ObservableList<TablePosition> cells = table.getSelectionModel().getSelectedCells();
		for (TablePosition<?, ?> cell : cells) {
			QueueDescriptor queue = table.getItems().get(cell.getRow());

			SpringFxmlLoader springFxmlLoader = new SpringFxmlLoader(MQConsole.applicationContext);
			Parent root = (Parent) springFxmlLoader.load("/net/sf/juffrou/mq/ui/message-send.fxml");

			AbstractMessageSendPresenterImpl controller = springFxmlLoader.<AbstractMessageSendPresenterImpl> getController();
			controller.setQueueNameSend(queue.getName());
			controller.setQueueDescriptors(table.getItems());

			Scene scene = new Scene(root, 768, 480);
			Stage stage = new Stage();
			stage.setScene(scene);
			stage.setTitle("Send new message to queue " + queue.getName());
			stage.show();

			if (LOG.isDebugEnabled())
				LOG.debug("Send new message to queue " + queue.getName());
		}
	}

	@FXML
	private void refreshButtonAction(ActionEvent event) {
		ObservableList<QueueDescriptor> rows = FXCollections.observableArrayList();
		rows.addAll(getQueues());
		table.setItems(rows);
	}
	
	protected abstract List<QueueDescriptor> getQueues();
	
	public MessageListener getMessageListener() {
		return messageListener;
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	protected Stage getStage() {
		return stage;
	}
}
