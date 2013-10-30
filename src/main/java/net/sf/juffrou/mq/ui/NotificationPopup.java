package net.sf.juffrou.mq.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class NotificationPopup {

	private final Stage parent;
	private final Popup popup;
	private final Label messageLabel;

	public NotificationPopup(Stage parent) {

		this.parent = parent;
		this.popup = new Popup();

		HBox box = new HBox();
		this.messageLabel = new Label("");
		box.getChildren().add(this.messageLabel);
		//		box.setPrefSize(100, 100);
		box.setAlignment(Pos.CENTER);
		box.setStyle("-fx-background-color: gray;");
		popup.getContent().add(box);
		popup.setAutoFix(true);
		popup.setHideOnEscape(true);

		//		popup.setX(parent.getX());
		//		popup.setY(parent.getY());
	}

	public void display(String message) {
		messageLabel.setText(message);
		popup.show(parent);
	}
}
