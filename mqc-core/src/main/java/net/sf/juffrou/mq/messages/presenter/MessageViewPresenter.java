package net.sf.juffrou.mq.messages.presenter;

import java.net.URL;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import net.sf.juffrou.mq.dom.HeaderDescriptor;
import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.messages.MessageViewController;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MessageViewPresenter implements MessageViewController {

	private static final String SCRIPT_SET_TEXT_PREFIX = "editor.setValue(\"";
	private static final String SCRIPT_SET_TEXT_SUFFIX = "\");";

	@FXML
	private Accordion messageAccordion;

	@FXML
	private TitledPane payloadPane;

	@FXML
	private TableView<HeaderDescriptor> headersTable;

	@FXML
	private WebView payload;

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

		final WebEngine engine = payload.getEngine();
		engine.setJavaScriptEnabled(true);

		if (messageDescriptor != null) {
			
			// set the text in the text editor (after the page loads completely)
			engine.getLoadWorker().stateProperty().addListener(
			        new ChangeListener<State>() {
			            public void changed(ObservableValue ov, State oldState, State newState) {
			                if (newState == State.SUCCEEDED) {
			                	String text = messageDescriptor.getText();
//			                	text = text.replaceAll(">\\s+<", "><");
			                	text = text.replaceAll("\\\n", "\\\\n");
			                	text = text.replaceAll("\"", "\\\\\"");
			                	String script = SCRIPT_SET_TEXT_PREFIX + text + SCRIPT_SET_TEXT_SUFFIX;
			                	engine.executeScript(script);
//			                	script = "editor.getSession().setMode(\"ace/mode/xml\");";
//			                	engine.executeScript(script);
//			                	script = "editor.getSession().updateText()";
//			                	engine.executeScript(script);
			                	script = "editor.setReadOnly(true);";
			                	engine.executeScript(script);
			                }
			            }
			        });


			headersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

			ObservableList<HeaderDescriptor> rows = FXCollections.observableArrayList();
			rows.addAll(messageDescriptor.getHeaders());
			headersTable.setItems(rows);
			payloadPane.setExpanded(true);
		}
		
		URL resource = getClass().getResource("AceEditor.html");
		engine.load(resource.toString());

	}

}
