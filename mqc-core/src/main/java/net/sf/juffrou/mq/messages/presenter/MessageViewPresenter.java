package net.sf.juffrou.mq.messages.presenter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import net.sf.juffrou.mq.dom.HeaderDescriptor;
import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.messages.MessageViewController;
import net.sf.juffrou.mq.ui.XmlViewer;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MessageViewPresenter implements MessageViewController, DisposableBean {

	private static final String SCRIPT_SET_TEXT_PREFIX = "editor.setValue(\"";
	private static final String SCRIPT_SET_TEXT_SUFFIX = "\");";

	@FXML
	private Accordion messageAccordion;

	@FXML
	private TitledPane payloadPane;

	@FXML
	private TableView<HeaderDescriptor> headersTable;

	@FXML
	private AnchorPane payloadAnchorPane;
	
	private ExecutorService executor = null; // Executor service for XML Viewer
	
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

		if(executor == null)
			executor = Executors.newSingleThreadExecutor();
		
		XmlViewer xmlViewer = new XmlViewer(executor);
		xmlViewer.setEditable(false);
		
		
		payloadAnchorPane.getStylesheets().add(XmlViewer.class.getResource("xml-highlighting.css").toExternalForm());
		payloadAnchorPane.getChildren().add(xmlViewer);
		payloadAnchorPane.setTopAnchor(xmlViewer, 0.0);
		payloadAnchorPane.setBottomAnchor(xmlViewer, 0.0);
		payloadAnchorPane.setLeftAnchor(xmlViewer, 0.0);
		payloadAnchorPane.setRightAnchor(xmlViewer, 0.0);
		
		if (messageDescriptor != null) {
			
			// set the text in the text editor (after the page loads completely)
			xmlViewer.replaceText(messageDescriptor.getText());

			headersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

			ObservableList<HeaderDescriptor> rows = FXCollections.observableArrayList();
			rows.addAll(messageDescriptor.getHeaders());
			headersTable.setItems(rows);
			payloadPane.setExpanded(true);
		}
		
	}

	@Override
	public void destroy() throws Exception {
		executor.shutdownNow();
		executor = null;
	}

}
