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
	
	@FXML private ComboBox comboTest;

	private DataHandler handler;
	private ArrayList<Control[]> inputFields = new ArrayList<Control[]>();
	private Control[] inputs = new Control[5];

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
		TextField[] textFields = {dateField, numUnitField, timeUnitField, startedField, spentField, endedField, varianceField, musicField, preAlertField,
				postAlertField, preMoodField, postMoodField, focusField }; // Ewwwww
		ComboBox[] comboBoxes = {classField, typeField, unitField};
		this.inputFields.add(textFields);
		this.inputFields.add(comboBoxes);
		inputs[0] = dateField;

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
					//checkForTimePerUnit();
				}
			}
		});

		startedField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when the startedField comes into focus
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
						if (newPropertyValue) {
							autoFillStartTime();
						} else {
							//checkForTimePerUnit();
						}
					}
				});

		endedField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when the endedField goes out of focus
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
						if (!newPropertyValue) {
							autoFillEndTime();
							//checkForTimePerUnit();
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
		
		new AutoCompleteComboBoxListener(comboTest);
		String[] test = handler.getColumnArray(2, false, handler.csvDir, handler.csvName, false);
		ObservableList<String> options = FXCollections.observableArrayList(test);
		comboTest.setItems(options);
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
		try {
			for (int i = 0; i < inputFields.get(0).length; i++) { //This assumes that the first array is a TextField[]
				if (i != 0) {
					((TextField)(inputFields.get(0)[i])).setText("");
				}
			}
			for (int i = 0; i < inputFields.get(1).length; i++) { //This assumes that the second array is a ComboBox[]
				if (i != 0) {
					((ComboBox)(inputFields.get(1)[i])).getEditor().setText("");
				}
			}
		} catch (Exception e) {
			System.out.println("Check the casting, it probably got screwed up.");
			e.printStackTrace();
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
	
	private int getTotalInputs(ArrayList<Control[]> arr) {
		int total = 0 ;
		
		for (int i = 0; i < arr.size(); i++) {
			for (int k = 0; k < arr.get(i).length; k++) {
				total++;
			}
		}
		
		return total;
	}

	@FXML
	private void saveRow() { // THIS IS NOT FLEXIBLE AT ALL
		try {
			ArrayList<String[]> dataSheet = handler.readFile(handler.csvDir, handler.csvName, true);// Get the current data sheet
			int numLines = handler.getNumberOfLines(handler.csvDir, handler.csvName, true) - 1; // Get the length of said sheet, subtract 1 because things start at 0
			String[] currentRow = dataSheet.get(numLines); // Get the last row of the sheet
			
			int currentIndex = 0;
			int lingeringK = 0;
			if (currentRow.length >= getTotalInputs(inputFields)) {
				for (int i = 0; i < inputFields.size(); i++) {
					for (int k = lingeringK; k < inputFields.get(i).length; k++) {
						if (i == 0) { //Assuming it's a TextField[]
							if (k == 1 && lingeringK != 1) {
								break;
							} else if (k == inputFields.get(i).length - 1) {
								lingeringK = 3;
							} else {
								currentRow[currentIndex] = ((TextField)inputFields.get(i)[k]).getText(); // Set the cells to the input fields
								currentIndex++;
							}
						} else if (i == 1) { //Assuming it's a ComboBox[]
							currentRow[currentIndex] = ((ComboBox)inputFields.get(i)[k]).getEditor().getText();
							currentIndex++;
							
							if (k == 2) {
								i = -1;
								lingeringK = 1;
								break;
							}
						}
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