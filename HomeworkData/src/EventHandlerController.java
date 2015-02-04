/**
 * Credit for this goes to this blog:
 * http://code.makery.ch/blog/javafx-2-event-handlers-and-change-listeners/
 */

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class EventHandlerController {

	@FXML private TextField dateField;
	@FXML private TextField classField;
	@FXML private TextField typeField;
	@FXML private TextField unitField;
	@FXML private TextField numUnitField;
	@FXML private TextField timeUnitField;
	@FXML private TextField startedField;
	@FXML private TextField spentField;
	@FXML private TextField endedField;
	@FXML private TextField varianceField;
	@FXML private TextField musicField;
	@FXML private TextField preAlertField;
	@FXML private TextField postAlertField;
	@FXML private TextField preMoodField;
	@FXML private TextField postMoodField;
	@FXML private TextField focusField;

	private DataHandler handler;
	private TextField[] inputFields;

	/**
	 * The constructor. The constructor is called before the initialize() method.
	 */
	public EventHandlerController() {
		this.handler = new DataHandler();
	}

	/**
	 * Initializes the controller class. This method is automatically called after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		TextField[] inputFields = { dateField, classField, typeField, unitField, numUnitField, timeUnitField, startedField, spentField, endedField,
				varianceField, musicField, preAlertField, postAlertField, preMoodField, postMoodField, focusField }; // Ewwwww
		this.inputFields = inputFields; // Ewwwwww

		dateField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when the dateField comes into focus
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
						if (newPropertyValue) {
							autoFillDate();
						}
					}
				});

		startedField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when the startedField comes into focus
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
						if (newPropertyValue) {
							autoFillStartTime();
						}
					}
				});

		endedField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when the endedField goes out of focus
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
						if (!newPropertyValue) {
							//autoFillEndTime();
							try {
								TextField[] neededInputs = { numUnitField, startedField, endedField };
								if (checkIfAllFilled(neededInputs)) {
									timeUnitField.setText(handler.divideTime(handler.subtractTime(startedField.getText(), endedField.getText()),
											Integer.parseInt(numUnitField.getText())));
								}
							} catch (NumberFormatException e) {
								System.out.println("There was an error converting the text in \"Time Per Unit\" to an integer. Try again.");
							}
						}
					}
				});

		spentField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when the endedField comes into focus
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
						if (newPropertyValue) {
							autoFillWasted();
						}
					}
				});
	}

	@FXML
	private void newRow() {
		try {
			this.handler.insertNewRow(-2, 16, handler.csvDir, "HomeworkDataSem2.csv");
			//			clearInputs(); // I decided this was too dangerous
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void clearInputs() {
		for (int i = 0; i < inputFields.length; i++) {
			if (i != 0) {
				inputFields[i].setText("");
			}
		}
	}

	/**
	 * Checks if every TextField provided contains text
	 * 
	 * @param fields - An array of the TextFields to check
	 * @return Whether or not they all contain something
	 */
	private boolean checkIfAllFilled(TextField[] fields) {
		boolean allFilled = true;
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].getText().length() == 0) {
				allFilled = false;
			}
		}
		return allFilled;
	}

	@FXML
	private void saveRow() {
		try {
			ArrayList<String[]> dataSheet = handler.readFile(handler.csvDir, handler.csvName, true);// Get the current data sheet
			int numLines = handler.getNumberOfLines(handler.csvDir, handler.csvName, true) - 1; // Get the length of said sheet, subtract 1 because things start at 0
			String[] currentRow = dataSheet.get(numLines); // Get the last row of the sheet

			if (currentRow.length >= inputFields.length) {
				for (int i = 0; i < inputFields.length; i++) {
					currentRow[i] = inputFields[i].getText(); // Set the cells to the input fields
				}
			}

			dataSheet.set(numLines, currentRow); // Set the data sheet line to the modified line
			handler.writeStringArray(dataSheet, handler.csvDir, handler.csvName); // Write the modified file (Array) to the file on disk
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	private void autoFillDate() {
		DateFormat dateFormat = new SimpleDateFormat("d-MMM-yy");
		Date date = new Date();
		dateField.setText(dateFormat.format(date));
	}

	@FXML
	private void autoFillStartTime() {
		DateFormat dateFormat = new SimpleDateFormat("kk:mm");
		Date date = new Date();
		startedField.setText(dateFormat.format(date));
	}

	@FXML
	private void autoFillEndTime() {
		DateFormat dateFormat = new SimpleDateFormat("kk:mm");
		Date date = new Date();
		endedField.setText(dateFormat.format(date));
	}

	@FXML
	private void autoFillWasted() {
		spentField.setText("0");
	}
}