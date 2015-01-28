/**
 * Credit for this goes to this blog:
 * http://code.makery.ch/blog/javafx-2-event-handlers-and-change-listeners/
 */

import java.io.IOException;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import com.sun.javafx.css.StyleCache.Key;

public class EventHandlerController {
	
	@FXML 
	private TextField dateField;
	@FXML 
	private TextField classField;
	@FXML 
	private TextField typeField;
	@FXML 
	private TextField unitField;
	@FXML 
	private TextField numUnitField;
	@FXML 
	private TextField timeUnitField;
	@FXML 
	private TextField startedField;
	@FXML 
	private TextField spentField;
	@FXML 
	private TextField endedField;
	@FXML 
	private TextField varianceField;
	@FXML 
	private TextField musicField;
	@FXML 
	private TextField preAlertField;
	@FXML 
	private TextField postAlertField;
	@FXML 
	private TextField preMoodField;
	@FXML 
	private TextField postMoodField;
	@FXML 
	private TextField focusField;
	
	private DataHandler handler;
	
	/**
	 * The constructor. The constructor is called before the initialize()
	 * method.
	 */
	public EventHandlerController() {
		this.handler = new DataHandler();
	}
	
	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		
	}
	
	@FXML
	private void newRow() {
		try {
			this.handler.insertNewRow(-2, 16, handler.csvDir, "HomeworkDataSem2.csv", false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@FXML
	private void saveRow() {
		try {
			handler.writeCell(handler.getNumberOfLines(handler.csvDir, "HomeworkDataSem2.csv", false), 0, dateField.getText(), handler.csvDir, handler.csvName, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@FXML
	private void handleKeyReleased() {
		//???????
	}
}