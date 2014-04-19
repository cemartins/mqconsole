package net.sf.juffrou.mq.messages.presenter;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.ContextMenuEvent;
import javafx.stage.Stage;
import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.messages.MessageViewPresenter;
import net.sf.juffrou.mq.messages.MessageViewView;
import net.sf.juffrou.mq.messages.MessagesListPresenter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public abstract class AbstractMessagesListPresenterImpl implements Initializable, MessagesListPresenter {

	protected static final Logger LOG = LoggerFactory.getLogger(AbstractMessagesListPresenterImpl.class);

	protected static final int MAX_TEXT_LEN_DISPLAY = 160;

	@FXML
	private TableView<MessageDescriptor> table;
	
	@Autowired
	private MessageViewView messageViewView;

	private String queueName;

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public void contextMenuRequested(ContextMenuEvent event) {
		System.out.println("menu requested");
	}

	public void openMessage(ActionEvent event) {
		ObservableList<TablePosition> cells = table.getSelectionModel().getSelectedCells();
		for (TablePosition<?, ?> cell : cells) {
			MessageDescriptor message = table.getItems().get(cell.getRow());
			
			Parent root = messageViewView.getView();

			MessageViewPresenter presenter = (MessageViewPresenter) messageViewView.getPresenter();
			presenter.setMessageDescriptor(message);
			presenter.initialize();

			Scene scene = new Scene(root, 768, 480);
			Stage stage = new Stage();
			stage.setScene(scene);
			stage.setTitle("Message");
			stage.show();
		}
		System.out.println("Context menu clicked");
	}

	protected abstract List<MessageDescriptor> listMessages();
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}

	public void initialize() {
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		ObservableList<MessageDescriptor> rows = FXCollections.observableArrayList();
		rows.addAll(listMessages());
		table.setItems(rows);
	}

	protected Stage getStage() {
		return (Stage) table.getScene().getWindow();
	}
}
