/**
 * Credit for this goes to this blog:
 * http://code.makery.ch/blog/javafx-2-event-handlers-and-change-listeners/
 */

import javafx.fxml.FXML;

public class EventHandlerController {
	
	/**
	 * The constructor. The constructor is called before the initialize()
	 * method.
	 */
	public EventHandlerController() {
	}
	
	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
	}
	
	@FXML
	private void handleButtonAction() {
		System.out.println("Button Action\n");
	}
}