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
import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.messages.MessageListener;
import net.sf.juffrou.mq.messages.presenter.MessageSendPresenter;
import net.sf.juffrou.mq.messages.presenter.MessageSendView;
import net.sf.juffrou.mq.messages.presenter.MessagesListPresenter;
import net.sf.juffrou.mq.messages.presenter.MessagesListView;
import net.sf.juffrou.mq.queues.QueuesListController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QueuesListPresenter {

	protected static final Logger LOG = LoggerFactory.getLogger(QueuesListPresenter.class);

	private Stage stage;

	@FXML
	private TableView<QueueDescriptor> table;
	
	@FXML
	private MenuItem miListenToNewMessages;
	
	@Autowired
	private QueuesListController queuesListController;

	@Autowired
	private MessageListener messageListener;
	
	@Autowired
	private MessagesListView messagesListView;
	
	@Autowired
	private MessageSendView messageSendView;

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
	private void openMessageList(ActionEvent event) {
		ObservableList<TablePosition> cells = table.getSelectionModel().getSelectedCells();
		for (TablePosition<?, ?> cell : cells) {
			QueueDescriptor queue = table.getItems().get(cell.getRow());

			Parent root = messagesListView.getView();

			MessagesListPresenter controller = (MessagesListPresenter) messagesListView.getPresenter();
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

			Parent root = messageSendView.getView();
			MessageSendPresenter controller = (MessageSendPresenter) messageSendView.getPresenter();
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
	
	protected List<QueueDescriptor> getQueues() {
		return queuesListController.getQueues(this);
	}
	
	public MessageListener getMessageListener() {
		return messageListener;
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	public Stage getStage() {
		return stage;
	}
}
