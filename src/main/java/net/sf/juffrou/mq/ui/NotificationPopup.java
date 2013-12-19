package net.sf.juffrou.mq.ui;

import javafx.stage.Stage;
import jfxtras.labs.dialogs.MonologFX;

public class NotificationPopup {

	private final Stage parent;
	private final MonologFX dialog;

	public NotificationPopup(Stage parent) {

		this.parent = parent;
		this.dialog = new MonologFX(MonologFX.Type.ERROR);
		this.dialog.setModal(true);
		this.dialog.setScene(parent.getScene());
	}

	public void display(String message) {
		dialog.setMessage("Please select a response queue");
		dialog.showDialog();
	}
}
