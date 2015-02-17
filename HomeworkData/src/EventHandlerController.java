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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;

public class EventHandlerController {

	@FXML private TextField dateField;
	@FXML private ComboBox classField;
	@FXML private ComboBox typeField;
	@FXML private ComboBox unitField;
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
	private Control[] inputFields = new Control[16];

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
		Control[] inputFields = { dateField, classField, typeField, unitField, numUnitField, timeUnitField, startedField, spentField, endedField, varianceField, musicField, preAlertField,
				postAlertField, preMoodField, postMoodField, focusField };//Ewwwwww
		this.inputFields = inputFields;

		dateField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when the dateField comes into focus
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
						if (newPropertyValue) {
							autoFillDate();
						}
					}
				});
		
		numUnitField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when the startedField comes into focus
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
				if (!newPropertyValue) {
					checkForTimePerUnit();
				}
			}
		});

		startedField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when the startedField comes into or out of focus
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
						if (newPropertyValue) { //Into focus
							autoFillStartTime();
						} else { //Out of focus
							checkForTimePerUnit();
						}
					}
				});

		endedField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when the endedField goes out of focus
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
						if (!newPropertyValue) {
							autoFillEndTime();
							checkForTimePerUnit();
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
		
		classField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when the classField goes out of focus
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
				if (!newPropertyValue) {
					String[] types = handler.getColumnArray(2, false, handler.csvDir, handler.csvName, false, 1, classField.getEditor().getText());
					ObservableList<String> typeOptions = FXCollections.observableArrayList(types);
					typeField.setItems(typeOptions);
				}
			}
		});
		
		typeField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when typeField goes out of focus
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
				if (!newPropertyValue) {
					String[] units = handler.getColumnArray(3, false, handler.csvDir, handler.csvName, false, 2, typeField.getEditor().getText());
					ObservableList<String> unitOptions = FXCollections.observableArrayList(units);
					unitField.setItems(unitOptions);
				}
			}
		});
		
		new AutoCompleteComboBoxListener(classField);
		String[] classes = handler.getColumnArray(1, false, handler.csvDir, handler.csvName, false, 0, "");
		ObservableList<String> classOptions = FXCollections.observableArrayList(classes);
		classField.setItems(classOptions);
		
		new AutoCompleteComboBoxListener(typeField);
		String[] types = handler.getColumnArray(2, false, handler.csvDir, handler.csvName, false, 1, "");
		ObservableList<String> typeOptions = FXCollections.observableArrayList(types);
		typeField.setItems(typeOptions);
		
		new AutoCompleteComboBoxListener(unitField);
		String[] units = handler.getColumnArray(3, false, handler.csvDir, handler.csvName, false, 2, "");
		ObservableList<String> unitOptions = FXCollections.observableArrayList(units);
		unitField.setItems(unitOptions);
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
			if (i == 0 || i > 3) {
				((TextField)inputFields[i]).setText("");
			} else if (i > 0 && i < 3) {
				((ComboBox)inputFields[i]).getEditor().setText("");
			}
		}
	}

	private void checkForTimePerUnit() {
		try {
			TextField[] neededInputs = { numUnitField, startedField, endedField };
			if (checkIfAllFilled(neededInputs)) {
				timeUnitField.setText(handler.divideTime(handler.subtractTime(startedField.getText(), endedField.getText()), Integer.parseInt(numUnitField.getText())));
			}
		} catch (NumberFormatException e) {
			System.out.println("There was an error converting the text in \"Time Per Unit\" to an integer. Try again.");
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
					if (i == 0 || i >= 4) {
						currentRow[i] = ((TextField)inputFields[i]).getText(); // Set the cells to the input fields
					} else if (i > 0 && i <= 3) {
						currentRow[i] = ((ComboBox)inputFields[i]).getEditor().getText();
					}
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