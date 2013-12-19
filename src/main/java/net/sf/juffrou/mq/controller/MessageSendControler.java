package net.sf.juffrou.mq.controller;

import java.util.GregorianCalendar;
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
import net.sf.juffrou.mq.ui.NotificationPopup;
import net.sf.juffrou.mq.util.MessageDescriptorHelper;
import net.sf.juffrou.mq.util.MessageReceivedHandler;
import net.sf.juffrou.mq.util.MessageReceivingTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.PCFConstants;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MessageSendControler {

	private static final Logger log = LoggerFactory.getLogger(MessageSendControler.class);

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

	@Autowired
	@Qualifier("mqQueueManager")
	private MQQueueManager qm;

	@Value("${broker_timeout}")
	private Integer brokerTimeout;

	private String queueNameSend;

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
		sendMessage(messageDescriptor, queue.getName());
	}

	// This method called to send MQ message to the norma messaging server
	// RECEIVES a message STRING and returns a message object (used as a
	// reference for the reply)
	public void sendMessage(MessageDescriptor messageDescriptor, String queueNameReceive) {

		MQQueue requestQueue = null;
		try {
			MQMessage sendMessage = null;

			try {
				int openOptions;

				// If the name of the request queue is the same as the reply
				// queue...
				if (queueNameSend.equals(queueNameReceive)) {
					openOptions = MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_OUTPUT;
				} else {
					openOptions = MQConstants.MQOO_OUTPUT; // Open queue to perform MQPUTs
				}

				// Now specify the queue that we wish to open, and the open
				// options...
				requestQueue = qm.accessQueue(queueNameSend, openOptions, null, // default q manager
						null, // no dynamic q name
						null); // no alternate user id

				// Create new MQMessage object
				sendMessage = new MQMessage();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

			sendMessage.format = MQConstants.MQFMT_STRING; // Set message format
															// to
															// MQC.MQFMT_STRING
															// for use without
															// MQCIH header

			// NB. Change to 'MQCICS ' if using header !!!
			//			sendMessage.characterSet = 1208; // UTF-8

			// String str = "AMQ!NEW_SESSION_CORRELID";
			// byte byteArray[] = str.getBytes();
			// sendMessage.correlationId = byteArray;//str;

			// Set request type
			sendMessage.messageType = MQConstants.MQMT_REQUEST;
			//			sendMessage.messageType = MQConstants.MQMT_DATAGRAM;

			// Set reply queue
			sendMessage.replyToQueueName = queueNameReceive;

			// Set message text
			// String buffer = new String(bufferFront + messageText +
			// bufferEnd);
			String buffer = messageDescriptor.getText();
			sendMessage.writeString(buffer);
			
			// set the message headers
			MessageDescriptorHelper.setMessageHeaders(sendMessage, messageDescriptor);

			// Specify the message options...(default)
			MQPutMessageOptions pmo = new MQPutMessageOptions();
			pmo.options = MQConstants.MQPMO_ASYNC_RESPONSE | MQConstants.MQPMO_NEW_MSG_ID;

			// Put the message on the queue using default options
			try {
				requestQueue.put(sendMessage, pmo);
			} catch (NullPointerException e) {
				if (log.isErrorEnabled())
					log.error("Request Q is null - cannot put message");
			}
			if (log.isDebugEnabled())
				log.debug("Message placed on queue");

			//Put the sent message with updated headers from the broker to the request tab
			messageDescriptor.addHeader(HeaderDescriptor.HEADER_MESSAGE_ID, sendMessage.messageId == null ? "null" : "'"
					+ new String(sendMessage.messageId) + "'");
			GregorianCalendar putDateTime = sendMessage.putDateTime;
			messageDescriptor.addHeader(HeaderDescriptor.HEADER_PUT_DATETIME, putDateTime == null ? "null"
					: putDateTime.getTime().toString());
			messageDescriptor.addHeader(HeaderDescriptor.HEADER_CORRELATION_ID, sendMessage.correlationId == null ? "null" : "'"
					+ new String(sendMessage.correlationId) + "'");

			setSentMessage(messageDescriptor);

			// Store the messageId for future use...
			// Define a MQMessage object to store the message ID as a
			// correlation ID
			// so we can retrieve the correct reply message later.
			MQMessage storedMessage = new MQMessage();

			// Copy current message ID across to the correlation ID
			storedMessage.correlationId = sendMessage.messageId;
			// storedMessage.characterSet = 1208; // UTF-8

			if (log.isDebugEnabled()) {
				log.debug("Message ID for sent message = '" + new String(sendMessage.messageId) + "'");
				log.debug("Correlation ID stored = '" + new String(storedMessage.correlationId) + "'");
			}

			// activate the receiving thread
			MessageReceivedHandler handler = new MessageReceivedHandler() {
				@Override
				public void messageReceived(MessageDescriptor messageDescriptor) {
					setReceiveMessage(messageDescriptor);
					receivePayloadPane.setExpanded(true);
					messageTabs.getSelectionModel().clearAndSelect(1);
				}

				@Override
				public Stage getStage() {
					return (Stage) messageAccordionSend.getScene().getWindow();
				}
			};
			MessageReceivingTask task = new MessageReceivingTask(handler, qm, queueNameReceive, brokerTimeout,
					storedMessage, queueNameSend);

			new Thread(task).start();

		} catch (MQException ex) {
			if (log.isErrorEnabled())
				log.error(ex + ": " + PCFConstants.lookupReasonCode(ex.reasonCode));
			NotificationPopup popup = new NotificationPopup(getStage());
			popup.display(ex + ": " + PCFConstants.lookupReasonCode(ex.reasonCode));
		} catch (java.io.IOException ex) {
			if (log.isErrorEnabled())
				log.error(ex.getMessage());
			NotificationPopup popup = new NotificationPopup(getStage());
			popup.display(ex.getMessage());
		} catch (Exception ex) {
			if (log.isErrorEnabled())
				log.error(ex.getMessage());
			NotificationPopup popup = new NotificationPopup(getStage());
		}
	}

	private Stage getStage() {
		return (Stage) messageAccordionSend.getScene().getWindow();
	}
}
