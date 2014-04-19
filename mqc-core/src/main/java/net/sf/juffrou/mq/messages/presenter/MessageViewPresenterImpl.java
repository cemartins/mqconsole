package net.sf.juffrou.mq.messages.presenter;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import net.sf.juffrou.mq.dom.HeaderDescriptor;
import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.messages.MessageViewPresenter;

import org.springframework.stereotype.Component;

@Component
public class MessageViewPresenterImpl implements MessageViewPresenter {

	@FXML
	private Accordion messageAccordion;

	@FXML
	private TitledPane payloadPane;

	@FXML
	private TableView<HeaderDescriptor> headersTable;

	@FXML
	private TextArea payload;

	private MessageDescriptor messageDescriptor;

	public MessageDescriptor getMessageDescriptor() {
		return messageDescriptor;
	}

	public void setMessageDescriptor(MessageDescriptor messageDescriptor) {
		this.messageDescriptor = messageDescriptor;
	}

	public void initialize() {
		
		// prevent all panes inside the accordion to collapse
		messageAccordion.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
	        @Override public void changed(ObservableValue<? extends TitledPane> property, final TitledPane oldPane, final TitledPane newPane) {
	          if (oldPane != null) oldPane.setCollapsible(true);
	          if (newPane != null) Platform.runLater(new Runnable() { @Override public void run() { 
	            newPane.setCollapsible(false); 
	          }});
	        }
	      });

		messageAccordion.setExpandedPane(payloadPane);

		if (messageDescriptor != null) {
			payload.setText(messageDescriptor.getText());

			headersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

			ObservableList<HeaderDescriptor> rows = FXCollections.observableArrayList();
			rows.addAll(messageDescriptor.getHeaders());
			headersTable.setItems(rows);
			payloadPane.setExpanded(true);
		}
	}

}
