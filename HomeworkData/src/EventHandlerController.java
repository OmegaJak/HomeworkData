/**
 * Credit for the original basis of this goes to this blog:
 * http://code.makery.ch/blog/javafx-2-event-handlers-and-change-listeners/
 * 
 * Credit for the alert dialogues goes to this blog:
 * http://code.makery.ch/blog/javafx-dialogs-official/
 */

import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class EventHandlerController {

	@FXML private GridPane mainGrid;
	@FXML private TextField dateField;
	@FXML private ComboBox classField;
	@FXML private ComboBox typeField;
	@FXML private ComboBox unitField;
	@FXML private TextField numUnitField;
	@FXML private TextField timeUnitField;
	@FXML private TextField startedField;
	@FXML private TextField spentField;
	@FXML private TextField endedField;
	@FXML private TextField predictedField;
	@FXML private TextField musicField;
	@FXML private TextField preAlertField;
	@FXML private TextField postAlertField;
	@FXML private TextField preMoodField;
	@FXML private TextField postMoodField;
	@FXML private TextField focusField;
	@FXML private TextArea consoleLog;
	@FXML private Button newRowButton;
	@FXML private Button saveRowButton;
	@FXML private ChoiceBox graphPicker;
	@FXML private AnchorPane graphDisplay;
	@FXML private Tab inputTab;
	@FXML private Tab consoleTab;
	@FXML private Tab graphTab;
	@FXML private TabPane tabPane;
	
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
		Control[] inputFields = { dateField, classField, typeField, unitField, numUnitField, timeUnitField, startedField, spentField, endedField, predictedField, musicField, preAlertField,
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
		
		numUnitField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when the startedField comes out of focus
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
				if (!newPropertyValue) {
					checkForTimePerUnit();
					checkForTimePrediction();
					if (!startedField.getText().equals("")) { //If there's something in the time started field
						checkForEndPrediction();
					}
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
							checkForEndPrediction();
						}
					}
				});

		
		ChangeListener<Boolean> endedAutoFillListener = new ChangeListener<Boolean>() { // Add a listener for when the endedField goes out of focus
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
				if (!newPropertyValue) {
					autoFillEndTime();
					checkForTimePerUnit();
				}
			}
		};
		
		ChangeListener<Boolean> endedCalculateListener = new ChangeListener<Boolean>() { // Add a listener for when the endedField goes out of focus
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
				if (!newPropertyValue) {
					checkForTimePerUnit();
				}
			}
		};
		endedField.focusedProperty().addListener(endedAutoFillListener);

		mainGrid.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode() == KeyCode.CONTROL) {
					endedField.focusedProperty().removeListener(endedAutoFillListener);
					endedField.focusedProperty().addListener(endedCalculateListener);
				}
			}
		});
		
		mainGrid.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode() == KeyCode.CONTROL) {
					endedField.focusedProperty().addListener(endedAutoFillListener);
					endedField.focusedProperty().removeListener(endedCalculateListener);
				}
			}
		});

		spentField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when the endedField comes into focus
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
						if (newPropertyValue && spentField.getText().equals("")) {
							autoFillWasted();
						} else if (!newPropertyValue) {
							checkForTimePerUnit();
						}
					}
				});
		
		classField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when the classField goes out of focus
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
				if (!newPropertyValue) {
					String[] types = handler.getCellsMeetingCriteria(new int[] {1}, new String[] {classField.getEditor().getText()}, "And", new int[] {2}, false, handler.csvDir, handler.csvName).get(0);
					ObservableList<String> typeOptions = FXCollections.observableArrayList(types);
					typeField.setItems(typeOptions);
					checkForTimePrediction();
				}
			}
		});
		
		unitField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when the classField goes out of focus
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
				if (!newPropertyValue) {
					checkForTimePrediction();
				}
			}
		});
		
		typeField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when typeField goes out of focus
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
				if (!newPropertyValue) {
					String[] units = handler.getCellsMeetingCriteria(new int[] {2}, new String[] {typeField.getEditor().getText()}, "And", new int[] {3}, false, handler.csvDir, handler.csvName).get(0);
					ObservableList<String> unitOptions = FXCollections.observableArrayList(units);
					unitField.setItems(unitOptions);
					checkForTimePrediction();
				}
			}
		});
		
		
		tabPane.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			GraphTabListener graphListener = null;
			
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
				System.out.println(newValue.intValue());
				if (newValue.intValue() == 2) { // Changed to graphTab
					graphListener = new GraphTabListener(graphDisplay, graphPicker, handler);
				} else if (oldValue.intValue() == 2) { // Just left the graphTab
					graphListener.unload();
				}
			}
		});	
		
		new AutoCompleteComboBoxListener(classField);
		String[] classes = handler.getCellsMeetingCriteria(new int[] {0}, new String[] {""}, "Not", new int[] {1}, false, handler.csvDir, handler.csvName).get(0);
		ObservableList<String> classOptions = FXCollections.observableArrayList(classes);
		classField.setItems(classOptions);
		
		new AutoCompleteComboBoxListener(typeField);
		String[] types = handler.getCellsMeetingCriteria(new int[] {0}, new String[] {""}, "Not", new int[] {2}, false, handler.csvDir, handler.csvName).get(0);
		ObservableList<String> typeOptions = FXCollections.observableArrayList(types);
		typeField.setItems(typeOptions);
		
		new AutoCompleteComboBoxListener(unitField);
		String[] units = handler.getCellsMeetingCriteria(new int[] {0}, new String[] {""}, "Not", new int[] {3}, false, handler.csvDir, handler.csvName).get(0);
		ObservableList<String> unitOptions = FXCollections.observableArrayList(units);
		unitField.setItems(unitOptions);
		
		
		PrintStream ps = System.out;
		System.setOut(new PrintStream(new StreamCapturer("STDOUT", consoleLog, ps)));
		
		System.out.println("Hello World");
	}

	@FXML
	private void newRow() {
		try {
			this.handler.insertNewRow(-2, 16, handler.csvDir, handler.csvName);
			//			clearInputs(); // I decided this was too dangerous
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void checkForTimePrediction() {
		TextField[] neededInputs = {classField.getEditor(), typeField.getEditor(), unitField.getEditor(), numUnitField};
		if (checkIfAllFilled(neededInputs)) {
			//System.out.println("The right things were filled");
			String averageTimeSpent = handler.averageTimeSpent(handler.readFile(handler.csvDir, handler.csvName, false), classField.getEditor().getText(), typeField.getEditor().getText(), unitField.getEditor().getText());
			String predictedTimeSpent = handler.multiplyTime(averageTimeSpent, Integer.parseInt(numUnitField.getText()));
			predictedField.setText(predictedTimeSpent);
			Tooltip averageTime = new Tooltip("The average time spent on a unit is: " + averageTimeSpent);
			predictedField.setTooltip(averageTime);
		}
	}
	
	private void checkForEndPrediction() {
		TextField[] neededInputs = {startedField, predictedField};
		if (checkIfAllFilled(neededInputs)) {
			String[] timesToAdd = {startedField.getText(), predictedField.getText()};
			Tooltip prediction = new Tooltip("The predicted end time is: " + handler.addTimes(timesToAdd));
			endedField.setTooltip(prediction);
		}
	}

	private void checkForTimePerUnit() { // This is a different methodology than timePerUnit in DataHandler, but it produces the same result. Might as well leave this in.
		try {
			TextField[] neededInputs = {numUnitField, startedField, endedField, spentField};
			if (checkIfAllFilled(neededInputs)) {
				timeUnitField.setText(handler.convertTime(handler.divideTime(handler.subtractTime(spentField.getText(), handler.subtractTime(startedField.getText(), endedField.getText())), Integer.parseInt(numUnitField.getText())), "H:MM:SS", "MM:SS", true));
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
	
	public DataHandler getDataHandler() {
		return this.handler;
	}

	@FXML
	private void saveRow() {
		try {
			ArrayList<String[]> dataSheet = handler.readFile(handler.csvDir, handler.csvName, true);// Get the current data sheet
			int numLines = handler.getNumberOfLines(handler.csvDir, handler.csvName, true) - 1; // Get the length of said sheet, subtract 1 because things start at 0
			String[] currentRow = dataSheet.get(numLines); // Get the last row of the sheet
			boolean shouldSave = true;
			
			boolean isEmpty = true;
			for (String item : currentRow) {
				if (!item.equals("")) isEmpty = false;
			}
			
			if (!isEmpty) {
				Alert confirmationAlert  = new Alert(AlertType.CONFIRMATION); // Creates a confirmation alert, to ensure that you want to overwrite the data
				confirmationAlert.setTitle("Are You Sure?");
				confirmationAlert.setHeaderText("Are you sure you want to save?");
				confirmationAlert.setContentText("You will overwrite existing data:\n    " + Arrays.toString(currentRow));
				
				Optional<ButtonType> result = confirmationAlert.showAndWait();
				if (result.get() != ButtonType.OK) { // If the user says no to the dialogue and cancels
					shouldSave = false;
					System.out.println("Save canceled!");
				} else { // Otherwise by default the data is saved
					System.out.print("The following data was overwritten: ");
					System.out.println(Arrays.toString(currentRow));
				}
			}

			if (shouldSave) {
				// Save the data
				if (currentRow.length >= inputFields.length) {
					for (int i = 0; i < inputFields.length; i++) {
						if (i == 0 || i >= 4) {// Special conditions for the TextFields
							currentRow[i] = ((TextField)inputFields[i]).getText(); // Set the cells to the input fields
						} else if (i > 0 && i <= 3) {// Special conditons for the ComboBoxes
							currentRow[i] = ((ComboBox)inputFields[i]).getEditor().getText();
						}
					}
				}

				dataSheet.set(numLines, currentRow); // Set the data sheet line to the modified line
				handler.writeStringArray(dataSheet, handler.csvDir, handler.csvName); // Write the modified file (Array) to the file on disk

				// Save confirmation stuff
				
				boolean didSave = true;
				ArrayList<String[]> data = handler.readFile(handler.csvDir, handler.csvName, false);
				for (int i = 0; i < inputFields.length; i++) {
					String inputText = "";
					if (inputFields[i] instanceof TextField) {
						inputText = ((TextField)inputFields[i]).getText();
					} else if (inputFields[i] instanceof ComboBox) {
						inputText = ((ComboBox)inputFields[i]).getEditor().getText();
					}
					
					if (!data.get(data.size() - 1)[i].equals(inputText)) {
						didSave = false;
						break;
					}
				}
				
				if (didSave) {
					System.out.println("Saved");
					
					FadeTransition transition = new FadeTransition(Duration.millis(80), saveRowButton);
					transition.setFromValue(1.0);
					transition.setToValue(0.0);
					transition.setCycleCount(2);
					transition.setAutoReverse(true);
					transition.play();
				} else {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Save Failure!");
					alert.setHeaderText("Your data was not saved!");
					alert.setContentText("You should ensure that the data is not lost somehow, and try again.\nIf it fails again, save data in some other way, reopen the program again, and try again.");
					
					alert.showAndWait();
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("The data did not save! Shit... Outputting data to console(hopefully)...");
			System.out.print("[");
			for (int i = 0; i < inputFields.length; i++) {
				System.out.print(inputFields[i].getId() + ":");
				if (inputFields[i] instanceof TextField) {
					System.out.print(((TextField)inputFields[i]).getText() + (i != inputFields.length - 1 ? ", " : ""));
				} else if (inputFields[i] instanceof ComboBox) {
					System.out.print(((ComboBox)inputFields[i]).getEditor().getText() + (i != inputFields.length - 1 ? ", " : ""));
				}
			}
			System.out.print("]");
		}
	}

	@FXML
	private void autoFillDate() {
		DateFormat dateFormat = new SimpleDateFormat("d-MMM-yy");
		Date date = new Date();
		String settingTo = dateFormat.format(date);
		System.out.println("Setting the date to: \"" + settingTo + "\". (It used to be \"" + dateField.getText() + "\")");
		dateField.setText(settingTo);
	}

	@FXML
	private void autoFillStartTime() {
		DateFormat dateFormat = new SimpleDateFormat("kk:mm");
		Date date = new Date();
		String settingTo = dateFormat.format(date);
		System.out.println("Setting the starting time to: \"" + settingTo + "\". (It used to be \"" + startedField.getText() + "\")");
		startedField.setText(settingTo);
	}

	@FXML
	private void autoFillEndTime() {
		DateFormat dateFormat = new SimpleDateFormat("kk:mm");
		Date date = new Date();
		String settingTo = dateFormat.format(date);
		System.out.println("Setting the end time to: " + settingTo + "\". (It used to be \"" + endedField.getText() + "\")");
		endedField.setText(settingTo);
	}

	@FXML
	private void autoFillWasted() {
		spentField.setText("0:00");
	}
	
	@FXML
	private void showPreferences() {
		try {
			Stage prefStage = new Stage();
			FXMLLoader loader = new FXMLLoader(GUI.class.getResource("Preferences.fxml"));
			VBox page = (VBox)loader.load();
			page.requestFocus(); // Just so it doesn't focus some textField by default
			Scene scene = new Scene(page);
			prefStage.setScene(scene);
			prefStage.show();
			((PreferencesHandler)loader.getController()).setPrefArrays(this.handler.prefKeys, this.handler.prefDefs); // Just so it knows what the pref keys and defs are, coming from DataHandler
			((PreferencesHandler)loader.getController()).setDefaultPrefValues();
			((PreferencesHandler)loader.getController()).handler = this.handler;
		} catch (IOException e) {
			System.out.println("Something went wrong with loading the preferences window in showPreferences()");
			e.printStackTrace();
		}
	}
}