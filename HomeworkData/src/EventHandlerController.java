/**
 * Credit for the original basis of this goes to this blog:
 * http://code.makery.ch/blog/javafx-2-event-handlers-and-change-listeners/
 * 
 * Credit for the alert dialogues goes to this blog:
 * http://code.makery.ch/blog/javafx-dialogs-official/
 */

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import CustomControls.NumberSpinner;
import CustomControls.RadialSpinner;
import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class EventHandlerController {

	@FXML private GridPane mainGrid;
	@FXML private DatePicker dateField;
	@FXML private Button leftArrow;
	@FXML private Button rightArrow;
	@FXML private ComboBox classField;
	@FXML private ComboBox typeField;
	@FXML private ComboBox unitField;
	@FXML private RadialSpinner numUnitRadial;
	@FXML private TextField timeUnitField;
	@FXML private TextField startedField;
	@FXML private TextField wastedField;
	@FXML private TextField endedField;
	@FXML private TextField predictedField;
	@FXML private TextArea consoleLog;
	@FXML private Button newRowButton;
	@FXML private Button saveRowButton;
	@FXML private ChoiceBox graphPicker;
	@FXML private AnchorPane graphDisplay;
	@FXML private HBox graphTabOptions;
	@FXML private Tab inputTab;
	@FXML private Tab consoleTab;
	@FXML private Tab graphTab;
	@FXML private TabPane tabPane;
	
	private DataHandler handler;
	public PastManager pastManager;
	private Control[] inputFields = new Control[16];
	
	private boolean hasPotentiallyBeenEdited = false;

	/**
	 * The constructor. The constructor is called before the initialize() method.
	 */
	public EventHandlerController() {
		this.handler = new DataHandler();
		this.pastManager = new PastManager();
	}

	/**
	 * Initializes the controller class. This method is automatically called after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		Control[] inputFields = {dateField, classField, typeField, unitField, numUnitRadial, timeUnitField, startedField, wastedField, endedField, predictedField};//Ewwwwww
		this.inputFields = inputFields;
		
		for (Control curControl : inputFields) {
			curControl.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
					if (newPropertyValue) { // Into focus
						hasPotentiallyBeenEdited = true;
						// TODO: Make RadialSpinner work with this
					}
				}
			});
		}
		
		numUnitRadial.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when the startedField comes out of focus or into focus
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
				if (!newPropertyValue) {
					checkForTimePerUnit();
					checkForTimePrediction();
					if (!startedField.getText().equals("")) { //If there's something in the time started field
						checkForEndPrediction();
					}
				} else {
					if (numUnitRadial.getValue() == 0) {
						numUnitRadial.setValue(1);
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

		wastedField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when the Time Wasted comes into/out of focus
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
						if (newPropertyValue && wastedField.getText().equals("")) {
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
					String[] types = handler.getCellsMeetingCriteria(new int[] {Columns.CLASS}, new String[] {classField.getEditor().getText()}, "And", new int[] {Columns.HOMEWORK_TYPE}, false, handler.csvDir, handler.csvName).get(0);
					ObservableList<String> typeOptions = FXCollections.observableArrayList(types);
					javafx.collections.FXCollections.reverse(typeOptions); // This reversal makes the most recent items be at the top of the list
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
		
		numUnitRadial.getEditor().textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				checkForTimePrediction();
				checkForTimePerUnit();
			}
		});
		
		typeField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when typeField goes out of focus
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
				if (!newPropertyValue) {
					String[] units = handler.getCellsMeetingCriteria(new int[] {Columns.CLASS, Columns.HOMEWORK_TYPE}, new String[] {classField.getEditor().getText(), typeField.getEditor().getText()}, "And", new int[] {Columns.UNIT}, false, handler.csvDir, handler.csvName).get(0);
					ObservableList<String> unitOptions = FXCollections.observableArrayList(units);
					javafx.collections.FXCollections.reverse(unitOptions);
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
					graphListener = new GraphTabListener(graphDisplay, graphPicker, graphTabOptions, handler);
				} else if (oldValue.intValue() == 2) { // Just left the graphTab
					graphListener.unload();
				}
			}
		});
		
		String pattern = "d-MMM-yy";
		dateField.setValue(LocalDate.now());
		dateField.setConverter(new StringConverter<LocalDate>() {
			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);
			
			@Override
			public String toString(LocalDate date) {
				if (date != null) {
					return dateFormatter.format(date);
				} else {
					return "";
				}
			}

			@Override
			public LocalDate fromString(String string) {
				if (string != null && !string.isEmpty()) {
					if (string.toLowerCase().equals("now") || string.toLowerCase().equals("today")) {
						return LocalDate.now();
					} else {
						return LocalDate.parse(string, dateFormatter);
					}
				} else {
					return null;
				}
			}
		});
		
		numUnitRadial.setMin(1.0);
		
		initAutoCompletes();
		
		PrintStream outStream = System.out;
		System.setOut(new PrintStream(new StreamCapturer("STDOUT", consoleLog, outStream, handler)));
		
		PrintStream errStream = System.err;
		System.setErr(new PrintStream(new StreamCapturer("ERR", consoleLog, errStream, handler)));
		
		DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy @ KK:mm a");
		Date date = new Date();
		System.out.println("Hello World! The current date and time is: " + dateFormat.format(date) + ".");
	}
	
	public void initAutoCompletes() {
		new AutoCompleteComboBoxListener(classField);
		String[] classes = handler.getCellsMeetingCriteria(new int[] {Columns.CLASS}, new String[] {"Class"}, "Not", new int[] {Columns.CLASS}, false, handler.csvDir, handler.csvName).get(0);
		ObservableList<String> classOptions = FXCollections.observableArrayList(classes);
		classField.setItems(classOptions);
		
		new AutoCompleteComboBoxListener(typeField);
		String[] types = handler.getCellsMeetingCriteria(new int[] {Columns.HOMEWORK_TYPE}, new String[] {"Type of Homework"}, "Not", new int[] {Columns.HOMEWORK_TYPE}, false, handler.csvDir, handler.csvName).get(0);
		ObservableList<String> typeOptions = FXCollections.observableArrayList(types);
		javafx.collections.FXCollections.reverse(typeOptions);
		typeField.setItems(typeOptions);
		
		new AutoCompleteComboBoxListener(unitField);
		String[] units = handler.getCellsMeetingCriteria(new int[] {Columns.UNIT}, new String[] {"Unit"}, "Not", new int[] {Columns.UNIT}, false, handler.csvDir, handler.csvName).get(0);
		ObservableList<String> unitOptions = FXCollections.observableArrayList(units);
		javafx.collections.FXCollections.reverse(unitOptions);
		unitField.setItems(unitOptions);
	}

	@FXML
	public void newRow() {
		try {
			this.handler.insertNewRow(-2, 16, handler.csvDir, handler.csvName);
			pastManager.resetCurrentLine();
		} catch (IOException e) {
			handler.showErrorDialogue(e);
			e.printStackTrace();
		}
	}
	
	@FXML
	private void lastEntry() {
		pastManager.lastEntry();
	}
	
	@FXML
	private void nextEntry() {
		pastManager.nextEntry();
	}
	
	private void checkForTimePrediction() {
		try {
			TextField[] neededInputs = {classField.getEditor(), typeField.getEditor(), unitField.getEditor(), numUnitRadial.getEditor()};
			if (checkIfAllFilled(neededInputs)) {
				String averageTimeSpent = handler.averageTimeSpent(classField.getEditor().getText(), typeField.getEditor().getText(),
						unitField.getEditor().getText());
				String predictedTimeSpent = handler.multiplyTime(averageTimeSpent, Integer.parseInt(numUnitRadial.getEditor().getText()));
				predictedField.setText(predictedTimeSpent);
				Tooltip averageTime = new Tooltip("The average time spent on a unit is: " + averageTimeSpent);
				predictedField.setTooltip(averageTime);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			handler.showErrorDialogue(e);
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
			TextField[] neededInputs = {numUnitRadial.getEditor(), startedField, endedField, wastedField};
			if (checkIfAllFilled(neededInputs)) {
				String timeTaken = handler.subtractTime(startedField.getText(), endedField.getText());
				String timeMinusWasted = handler.subtractTime(wastedField.getText(), timeTaken);
				double numUnit = Math.round(numUnitRadial.getValue());
				String division = handler.divideTime(timeMinusWasted, "HH:MM", numUnit);
				timeUnitField.setText(handler.convertTime(division, "HH:MM:SS", "MM:SS", false));
			}
		} catch (NumberFormatException e) {
			System.err.println("There was an error converting the text in \"Time Per Unit\" to an integer. Try again.");
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
		boolean didSave = saveData();
		
		if (didSave) {
			System.out.println("Saved");

			FadeTransition transition = new FadeTransition(Duration.millis(80), saveRowButton);
			transition.setFromValue(1.0);
			transition.setToValue(0.0);
			transition.setCycleCount(2);
			transition.setAutoReverse(true);
			transition.play();
		} else {
			printCurrentData();
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Save Failure!");
			alert.setHeaderText("Your data was not saved!");
			alert.setContentText("You should ensure that the data is not lost somehow, and try again.\n"
					+ "If it fails again, save data in some other way, reopen the program again, and try again.\n"
					+ "The current data has been written to the console and log.");

			alert.showAndWait();
		}			
	}
	
	public boolean saveData() {
		try {
			ArrayList<String[]> dataSheet = handler.readFile(handler.csvDir, handler.csvName, true, -1);// Get the current data sheet
			
			int line = dataSheet.size() - 1;
			if (pastManager.IsInPast()){
				line = (dataSheet.size() - handler.readFile(handler.csvDir, handler.csvName, true, handler.mostRecentYear).size()) + pastManager.currentLine;
			}
			
			String[] currentRow = dataSheet.get(line); // Get the last row of the sheet
			boolean shouldSave = true;

			boolean isEmpty = true;
			for (String item : currentRow) {
				if (!item.equals("")) {
					isEmpty = false;
					break;
				}
			}

			if (!isEmpty) {
				Alert confirmationAlert = new Alert(AlertType.CONFIRMATION); // Creates a confirmation alert, to ensure that you want to overwrite the data
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
						currentRow[i] = getText(inputFields[i]);
					}
				}

				dataSheet.set(line, currentRow); // Set the data sheet line to the modified line
				handler.writeStringArray(dataSheet, handler.csvDir, handler.csvName); // Write the modified file (Array) to the file on disk

				// Save confirmation stuff

				boolean didSave = true;
				ArrayList<String[]> data = handler.readFile(handler.csvDir, handler.csvName, false, -1);
				for (int i = 0; i < inputFields.length; i++) {
					String inputText = getText(inputFields[i]);

					if (!data.get(line)[i].equals(inputText)) {
						didSave = false;
						break;
					}
				}

				return didSave;
			}
			
			return false; // TODO: When this returns like this, it'll show the error dialogue as if it failed critically

		} catch (IOException e) {
			handler.showErrorDialogue(e);
			e.printStackTrace();
			System.out.println("The data did not save! Shit... Outputting data to console(hopefully)...");
			printCurrentData();
		}
		return false;
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
		System.out.println("Setting the end time to: \"" + settingTo + "\". (It used to be \"" + endedField.getText() + "\")");
		endedField.setText(settingTo);
	}

	@FXML
	private void autoFillWasted() {
		wastedField.setText("0:00");
	}
	
	@FXML
	private void showPreferences() {
		try {
			Stage prefStage = new Stage();
			FXMLLoader loader = new FXMLLoader(GUI.class.getResource("/view/Preferences.fxml"));
			VBox page = (VBox)loader.load();
			page.requestFocus(); // Just so it doesn't focus some textField by default
			Scene scene = new Scene(page);
			prefStage.setScene(scene);
			prefStage.show();
			PreferencesHandler preferencesHandler = (PreferencesHandler)loader.getController();
			preferencesHandler.setPrefArrays(this.handler.prefKeys, this.handler.prefDefs); // Just so it knows what the pref keys and defs are, coming from DataHandler
			preferencesHandler.setDefaultPrefValues();
			preferencesHandler.handler = this.handler;
			preferencesHandler.controller = this;
			
			NumberSpinner yearSpinner = new NumberSpinner(new BigDecimal(handler.mostRecentYear), BigDecimal.ONE, "((\\-+\\d*)|(\\d*))");
			preferencesHandler.yearSpinner = yearSpinner;
			preferencesHandler.initYearSpinner();
			
		} catch (IOException e) {
			System.out.println("Something went wrong with loading the preferences window in showPreferences()");
			e.printStackTrace();
		}
	}
	
	@FXML 
	void quit() {
		if (isSaved(pastManager.currentLine)) {
			System.exit(0);
		} else {
			int warningResult = showSaveWarning("Quit");
			
			if (warningResult == 0) {
				newRow();
				boolean didSave = saveData();
				if (didSave) { // This is basically the same as the saveRow() method above
					System.out.println("Saved");
					System.exit(0);
				} else {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Save Failure!");
					alert.setHeaderText("Your data was not saved!");
					alert.setContentText("You should ensure that the data is not lost somehow, and try again.\nIf it fails again, save data in some other way, reopen the program again, and try again.");

					alert.showAndWait();
				}	
			} else if (warningResult == 1) {
				System.exit(0);
			}
		}
	}
	
	@FXML
	void newSemester() {
		addOtherInfo("NEW SEMESTER");
	}
	
	@FXML
	void newYear() {
		addOtherInfo("NEW SCHOOL YEAR");
		handler.mostRecentYear++;
	}
	
	/**
	 * Shows a warning that the user is about to quit without the data being saved
	 * @param verb - The verb to use in the buttons: "Save and [verb]","[verb] without saving"
	 * @return An int representing the index of the button clicked:
	 * <li>0 == Save and [verb]
	 * <li>1 == [verb] without Saving
	 * <li>2 == Cancel
	 */
	public int showSaveWarning(String verb) {
		Alert quitWarning = new Alert(AlertType.WARNING);
		
		quitWarning.setTitle("Data Not Saved!");
		quitWarning.setHeaderText("If you continue, you will lose this data.");
		quitWarning.setContentText("The data currently contained in the input boxes does not match the last line of \"" + handler.csvName + "\". You may want to save this data.");
		
		ButtonType saveQuitButton = new ButtonType("New Row, Save, and " + verb);
		ButtonType quitButton = new ButtonType(verb + " Without Saving");
		ButtonType cancelButton = new ButtonType("Cancel");
		
		quitWarning.getButtonTypes().setAll(saveQuitButton, quitButton, cancelButton);
		//quitWarning.getButtonTypes().setAll(buttons);
		
		Optional<ButtonType> result = quitWarning.showAndWait();
		
		if (result.get().equals(saveQuitButton)) {
			return 0;
		} else if (result.get().equals(quitButton)) {
			return 1;
		} else {
			return 2;
		}
		
	}
	
	/**
	 * Determines whether or now the current data is saved in the given row.
	 * @param rowIndex - The row to check the current data against. Pass -1 if the most recent row is desired.
	 * @return True if the data is saved, false if it's not.
	 */
	public boolean isSaved(int rowIndex) {
		boolean wasSaved = true;
		if (hasPotentiallyBeenEdited) {
			ArrayList<String[]> data = handler.readFile(handler.csvDir, handler.csvName, false, handler.mostRecentYear);
			
			System.out.print("The last row of the file was: ");
			handler.printLine(rowIndex);
			System.out.print("The current data is: ");
			printCurrentData();
			
			HashMap<Integer, String[]> ignores = new HashMap<Integer, String[]>(5);
			ignores.put(0, new String[] {";.;"});
			ignores.put(4, new String[] {"0","1"});
			ignores.put(7, new String[] {"0:00"});
			ignores.put(10, new String[] {"0.0"});
			ignores.put(15, new String[] {"0.0"});
			
			// It's debatable whether the following two should be ignored - not ignoring can cause troubles when viewing old data
			ignores.put(5, new String[] {";.;"});
			ignores.put(9, new String[] {";.;"});
			
			outer:
			for (int i = 0; i < inputFields.length; i++) { // Ignore date, it's always automatic
				String inputText = getText(inputFields[i]);
				
				if (ignores.containsKey(i)) {
					for (String ignore : ignores.get(i)) {
						if (ignore == ";.;" || inputText.equals(ignore)) {
							System.out.println("Skipped index " + i + ", with value \"" + inputText + "\".");
							
							continue outer;
						}
					}
				}

				// By virtue of making it here, there weren't any indexes to ignore
				int index = rowIndex;
				if (index == -1)
					index = data.size() - 1;
				if (!inputText.equals(data.get(index)[i])) {
					System.out.println("Stopped on index " + i + ", with value \"" + inputText + "\". It was found to be inequal with \"" + data.get(index)[i] + "\".");

					wasSaved = false;
					break;
				}
			}
		}
		
		return wasSaved;
	}
	
	private String getText(Control input) {
		String toReturn = "";
		if (input instanceof TextField) {
			toReturn = ((TextField)input).getText();
		} else if (input instanceof ComboBox) {
			toReturn = ((ComboBox)input).getEditor().getText();
		} else if (input instanceof RadialSpinner) {
			toReturn = ((RadialSpinner)input).getEditor().getText();
		} else if (input instanceof DatePicker) {
			toReturn = ((DatePicker)input).getEditor().getText();
		} else {
			System.out.println("There's an unknown control, and I can therefore not interact with the file properly.");
			toReturn = "ERROR";
		}
		return toReturn;
	}
	
	private void setText(Control input, String value) {
		if (input instanceof TextField) {
			((TextField)input).setText(value);;
		} else if (input instanceof ComboBox) {
			((ComboBox)input).getEditor().setText(value);
		} else if (input instanceof RadialSpinner) {
			((RadialSpinner)input).getEditor().setText(value);
		} else if (input instanceof DatePicker) {
			((DatePicker)input).getEditor().setText(value);
		} else {
			String message = "There's an unknown control, and I can therefore not interact with it properly.";
			System.out.println(message);
		}
	}
	
	public void printCurrentData() {
		System.out.print("[");
		for (int i = 0; i < inputFields.length; i++) {
			System.out.print(getText(inputFields[i]) + (i != inputFields.length - 1 ? ", " : ""));
		}
		System.out.print("] ([");
		for (int i = 0; i < inputFields.length; i++) {
			System.out.print(inputFields[i].getId() + ":");
			System.out.print("\"" + getText(inputFields[i]) + "\"" + (i != inputFields.length - 1 ? ", " : ""));
		}
		System.out.println(")]");
	}
	
	private void addOtherInfo(String infoText) {
		this.newRow();
		ArrayList<String[]> dataSheet =  handler.readFile(handler.csvDir, handler.csvName, true, -1);
		int numLines = handler.getNumberOfLines(handler.csvDir, handler.csvName, true) - 1;
		String[] currentRow = dataSheet.get(numLines);
		
		currentRow[0] = "OTHER INFO";
		currentRow[1] = infoText;
		try {
			handler.writeStringArray(dataSheet, handler.csvDir, handler.csvName);
		} catch (IOException e) {
			handler.showErrorDialogue(e);
		}
	}
	
	protected void loadLine(int rowIndex) { // rowIndex == -1 means most recent line
		ArrayList<String[]> dataSheet = handler.readFile(handler.csvDir, handler.csvName, false, handler.mostRecentYear);
		int index = rowIndex;
		if (index == -1) {
			index = dataSheet.size() - 1;
		}
		
		for (int i = 0; i < inputFields.length; i++) {
			setText(inputFields[i], dataSheet.get(index)[i]);
		}
	}
	
	protected void resetInputs() {
		dateField.setValue(LocalDate.now());
		dateField.getEditor().setText(dateField.getConverter().toString(LocalDate.now()));
		
		numUnitRadial.setValue(1);
		for (Control input : inputFields) {
			if (input != dateField && input != numUnitRadial) {
				setText(input, "");
			}
		}
		hasPotentiallyBeenEdited = false;
	}
	
	public class PastManager {
		private int currentLine; // For use only when going to the last entry or next entry. Should be -1 when those aren't currently happening.
		
		public boolean IsInPast() { return currentLine != -1 ; }

		public PastManager() {
			resetCurrentLine();
		}
		
		public void resetCurrentLine() {
			currentLine = -1;
		}
		
		public int getCurrentLine() {
			return currentLine;
		}
		
		protected void lastEntry() {
			if (!isSaved(currentLine)) {				
				int action = showSaveWarning("Continue");
				if (action == 0) { // Save and continue
					newRow();
					saveData(); // Save the data, and by the function being allowed to proceed, it continues
				} else if (action == 2) { // Cancel
					return; // Don't allow the function to proceed
				}
			}
			decrementCurrentLine();
			loadLine(currentLine);
			hasPotentiallyBeenEdited = false;
		}
		
		protected void nextEntry() {
			if (!isSaved(currentLine)) {
				int action = showSaveWarning("Continue");
				if (action == 0) {
					newRow();
					saveData();
				} else if (action == 2) {
					return;
				}
			}
			incrementCurrentLine();
			if (currentLine == -1) {
				resetInputs();
			} else {
				loadLine(currentLine);
				hasPotentiallyBeenEdited = false;
			}
		}
		
		private void decrementCurrentLine() {
			ArrayList<String[]> dataSheet = handler.readFile(handler.csvDir, handler.csvName, false, handler.mostRecentYear);
			if (currentLine == -1 || currentLine == 1) {
				currentLine = dataSheet.size() - 1; // -1 denotes the most recent line, which is size() - 1, therefore the one before that is size() - 2
			} else {
				currentLine--;
			}
			System.out.println(currentLine);
		}
		
		private void incrementCurrentLine() {
			if (currentLine == -1)
				return;
			ArrayList<String[]> dataSheet = handler.readFile(handler.csvDir, handler.csvName, false, handler.mostRecentYear);
			if (currentLine == dataSheet.size() - 1) { // We're at the most recent data already written
				resetInputs(); // So clear that data to allow inputting the new data
				currentLine = -1; // Signal that we're on the most recent line (not yet saved)
			} else { 
				currentLine++;
			}
			System.out.println(currentLine);
		}
	}
}