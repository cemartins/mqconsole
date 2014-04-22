package net.sf.juffrou.mq.ui;

import javafx.stage.Stage;

import org.controlsfx.dialog.Dialogs;

public class NotificationPopup {

	private final Dialogs dialogs;

	public NotificationPopup(Stage parent) {
		dialogs = Dialogs.create().owner( parent ).title("MQConsole Message");
		
	}

	public void display(String message) {
		dialogs.message( message ).showError();
	}
}
