package net.sf.juffrou.mq.messages.presenter;

import java.util.Iterator;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import jfxtras.labs.dialogs.MonologFX;
import net.sf.juffrou.mq.dom.HeaderDescriptor;
import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.messages.MessageSendController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MessageSendPresenter {

	protected static final Logger LOG = LoggerFactory.getLogger(MessageSendPresenter.class);

	@FXML
	private Accordion messageAccordionSend;

	@FXML
	private Accordion messageAccordionReceive;

	@FXML
	private TitledPane sendHeadersPane;

	@FXML
	private TitledPane sendPayloadPane;

	@FXML
	private TitledPane receivePayloadPane;

	@FXML
	private TableView<HeaderDescriptor> sendHeadersTable;
	
	@FXML
	private TableColumn<HeaderDescriptor, String> sendHeadersTableName;

	@FXML
	private TableColumn<HeaderDescriptor, Object> sendHeadersTableValue;

	@FXML
	private TableView<HeaderDescriptor> receiveHeadersTable;

	@FXML
	private TextArea sendPayload;

	@FXML
	private TextArea receivePayload;

	@FXML
	private ComboBox<QueueDescriptor> replyQueueCB;

	@FXML
	private TabPane messageTabs;

	@FXML
	private Tab responseTab;

	@Value("${broker_timeout}")
	private Integer brokerTimeout;

	private String queueNameSend;
	
	@Autowired
	private MessageSendController messageSendController;

	public String getQueueNameSend() {
		return queueNameSend;
	}

	public void setQueueNameSend(String queueNameSend) {
		this.queueNameSend = queueNameSend;
	}

	public MessageDescriptor getSendMessage() {
		MessageDescriptor messageDescriptor = new MessageDescriptor();
		messageDescriptor.setText(sendPayload.getText());

		ObservableList<HeaderDescriptor> rows = sendHeadersTable.getItems();
		if (rows != null) {
			Iterator<HeaderDescriptor> iterator = rows.iterator();
			while (iterator.hasNext())
				messageDescriptor.addHeader(iterator.next());
		}
		return messageDescriptor;
	}

	public void setSentMessage(MessageDescriptor messageDescriptor) {
		sendPayload.setText(messageDescriptor.getText());

		ObservableList<HeaderDescriptor> rows = FXCollections.observableArrayList();
		rows.addAll(messageDescriptor.getHeaders());
		sendHeadersTable.getItems().clear();
		sendHeadersTable.setItems(rows);
		sendHeadersPane.setExpanded(true);
	}

	public void setReceiveMessage(MessageDescriptor messageDescriptor) {
		receivePayload.setText(messageDescriptor.getText());

		ObservableList<HeaderDescriptor> rows = FXCollections.observableArrayList();
		rows.addAll(messageDescriptor.getHeaders());
		receiveHeadersTable.getItems().clear();
		receiveHeadersTable.setItems(rows);
		receivePayloadPane.setExpanded(true);
	}

	public void setQueueDescriptors(ObservableList<QueueDescriptor> queues) {
		replyQueueCB.setItems(queues);
	}

	public void initialize() {
		sendHeadersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		sendHeadersTableName.setOnEditCommit(
				new EventHandler<CellEditEvent<HeaderDescriptor, String>>() {
	                public void handle(CellEditEvent<HeaderDescriptor, String> t) {
	                    ((HeaderDescriptor) t.getTableView().getItems().get(
	                        t.getTablePosition().getRow())
	                        ).setName(t.getNewValue());
	                }
	            }
	        );
		sendHeadersTableValue.setOnEditCommit(
				new EventHandler<CellEditEvent<HeaderDescriptor, Object>>() {
	                public void handle(CellEditEvent<HeaderDescriptor, Object> t) {
	                    ((HeaderDescriptor) t.getTableView().getItems().get(
	                        t.getTablePosition().getRow())
	                        ).setValue(t.getNewValue());
	                }
	            }
	        );
		receiveHeadersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		final Callback<ListView<QueueDescriptor>, ListCell<QueueDescriptor>> factory = new Callback<ListView<QueueDescriptor>, ListCell<QueueDescriptor>>() {
			@Override
			public ListCell<QueueDescriptor> call(ListView<QueueDescriptor> arg0) {
				return new QueueDescriptorCell();
			}

		};
		replyQueueCB.setCellFactory(factory);
		replyQueueCB.setButtonCell(new QueueDescriptorCell());
		sendPayloadPane.setExpanded(true);
		
		// prevent all panes inside the accordion to collapse
		messageAccordionSend.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
	        @Override public void changed(ObservableValue<? extends TitledPane> property, final TitledPane oldPane, final TitledPane newPane) {
	          if (oldPane != null) oldPane.setCollapsible(true);
	          if (newPane != null) Platform.runLater(new Runnable() { @Override public void run() { 
	            newPane.setCollapsible(false); 
	          }});
	        }
	      });

		messageAccordionSend.setExpandedPane(sendPayloadPane);

		// prevent all panes inside the accordion to collapse
		messageAccordionReceive.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
	        @Override public void changed(ObservableValue<? extends TitledPane> property, final TitledPane oldPane, final TitledPane newPane) {
	          if (oldPane != null) oldPane.setCollapsible(true);
	          if (newPane != null) Platform.runLater(new Runnable() { @Override public void run() { 
	            newPane.setCollapsible(false); 
	          }});
	        }
	      });

		messageAccordionReceive.setExpandedPane(receivePayloadPane);

	}

	private static class QueueDescriptorCell extends ListCell<QueueDescriptor> {
		@Override
		protected void updateItem(QueueDescriptor item, boolean empty) {
			super.updateItem(item, empty);
			if (item != null)
				setText(item.getName());
		}
	}
	
	@FXML
	private void addHeader(ActionEvent event) {
		// user selected Add Row from the context menu on the send headers table
		
		HeaderDescriptor hd = new HeaderDescriptor();
		hd.setName("header_name (double click to change)");
		hd.setValue("value (double click to change)");
		ObservableList<HeaderDescriptor> items = sendHeadersTable.getItems();
		if(items != null)
			items.add(hd);
		else {
			items = FXCollections.observableArrayList();
			items.add(hd);
			sendHeadersTable.setItems(items);
		}
	}

	@FXML
	private void removeHeader(ActionEvent event) {
		
		ObservableList<TablePosition> cells = sendHeadersTable.getSelectionModel().getSelectedCells();
		int row = cells.get(0).getRow();
		sendHeadersTable.getItems().remove(row);
	}

	@FXML
	private void sendButton(ActionEvent actionEvent) {
		QueueDescriptor queue = replyQueueCB.getValue();
		if (queue == null) {
//			NotificationPopup popup = new NotificationPopup(getStage());
//			popup.display("Please select a response queue");
			MonologFX dialog = new MonologFX(MonologFX.Type.ERROR);
			dialog.setMessage("Please select a response queue");
			dialog.setModal(true);
			dialog.showDialog();
			return;
		}
		MessageDescriptor messageDescriptor = getSendMessage();
		
		messageSendController.sendMessage(this, messageDescriptor, queueNameSend, queue.getName());
	}


	public void displayMessageReceived(MessageDescriptor messageDescriptor) {
		setReceiveMessage(messageDescriptor);
		receivePayloadPane.setExpanded(true);
		messageTabs.getSelectionModel().clearAndSelect(1);
	}
	
	public Stage getStage() {
		return (Stage) messageAccordionSend.getScene().getWindow();
	}
}
