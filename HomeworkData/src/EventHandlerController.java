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
	@FXML private ComboBox classField;
	@FXML private ComboBox typeField;
	@FXML private ComboBox unitField;
	@FXML private RadialSpinner numUnitRadial;
	@FXML private TextField timeUnitField;
	@FXML private TextField startedField;
	@FXML private TextField wastedField;
	@FXML private TextField endedField;
	@FXML private TextField predictedField;
	@FXML private RadialSpinner musicRadial;
	@FXML private TextField preAlertField;
	@FXML private TextField postAlertField;
	@FXML private TextField preMoodField;
	@FXML private TextField postMoodField;
	@FXML private RadialSpinner focusRadial;
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
	private Control[] inputFields = new Control[16];
	
	private boolean hasPotentiallyBeenEdited = false;

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
		Control[] inputFields = {dateField, classField, typeField, unitField, numUnitRadial, timeUnitField, startedField, wastedField, endedField, predictedField, musicRadial, preAlertField,
				postAlertField, preMoodField, postMoodField, focusRadial};//Ewwwwww
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
					String[] units = handler.getCellsMeetingCriteria(new int[] {Columns.HOMEWORK_TYPE}, new String[] {typeField.getEditor().getText()}, "And", new int[] {Columns.UNIT}, false, handler.csvDir, handler.csvName).get(0);
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
		
		musicRadial.getNumberTextField().setFormat(new DecimalFormat("0.0"));
		musicRadial.setMin(0.0);
		musicRadial.setMax(10.0);
		
		focusRadial.getNumberTextField().setFormat(new DecimalFormat("0.0"));
		focusRadial.setMin(0.0);
		focusRadial.setMax(10.0);
		
		initAutoCompletes();
		
		NumberSpinner yearSpinner = new NumberSpinner(new BigDecimal(handler.mostRecentYear), BigDecimal.ONE, "((\\-+\\d*)|(\\d*))");
		mainGrid.add(yearSpinner, 0, 4);
		mainGrid.setMargin(yearSpinner, new Insets(30.0, 11, 0.0, 12.5));
		yearSpinner.numberProperty().addListener(new ChangeListener<BigDecimal>() {
			@Override
			public void changed(ObservableValue<? extends BigDecimal> observable, BigDecimal oldValue, BigDecimal newValue) {
				handler.mostRecentYear = newValue.intValue();
				initAutoCompletes(); // Gotta refresh these, since they depend on which year it currently is
			}
		});
		
		PrintStream ps = System.out;
		System.setOut(new PrintStream(new StreamCapturer("STDOUT", consoleLog, ps, handler)));
		
		DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy @ KK:mm a");
		Date date = new Date();
		System.out.println("Hello World! The current date and time is: " + dateFormat.format(date) + ".");
	}
	
	private void initAutoCompletes() {
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
	private void newRow() {
		try {
			this.handler.insertNewRow(-2, 16, handler.csvDir, handler.csvName);
		} catch (IOException e) {
			handler.showErrorDialogue(e);
			e.printStackTrace();
		}
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
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Save Failure!");
			alert.setHeaderText("Your data was not saved!");
			alert.setContentText("You should ensure that the data is not lost somehow, and try again.\nIf it fails again, save data in some other way, reopen the program again, and try again.");

			alert.showAndWait();
		}			
	}
	
	private boolean saveData() {
		try {
			ArrayList<String[]> dataSheet = handler.readFile(handler.csvDir, handler.csvName, true, -1);// Get the current data sheet
			int numLines = handler.getNumberOfLines(handler.csvDir, handler.csvName, true) - 1; // Get the length of said sheet, subtract 1 because things start at 0
			String[] currentRow = dataSheet.get(numLines); // Get the last row of the sheet
			boolean shouldSave = true;

			boolean isEmpty = true;
			for (String item : currentRow) {
				if (!item.equals(""))
					isEmpty = false;
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

				dataSheet.set(numLines, currentRow); // Set the data sheet line to the modified line
				handler.writeStringArray(dataSheet, handler.csvDir, handler.csvName); // Write the modified file (Array) to the file on disk

				// Save confirmation stuff

				boolean didSave = true;
				ArrayList<String[]> data = handler.readFile(handler.csvDir, handler.csvName, false, handler.mostRecentYear);
				for (int i = 0; i < inputFields.length; i++) {
					String inputText = getText(inputFields[i]);

					if (!data.get(data.size() - 1)[i].equals(inputText)) {
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
			System.out.print("[");
			for (int i = 0; i < inputFields.length; i++) {
				System.out.print(inputFields[i].getId() + ":");
				System.out.print(getText(inputFields[i]) + (i != inputFields.length - 1 ? ", " : ""));
			}
			System.out.print("]");
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
			((PreferencesHandler)loader.getController()).setPrefArrays(this.handler.prefKeys, this.handler.prefDefs); // Just so it knows what the pref keys and defs are, coming from DataHandler
			((PreferencesHandler)loader.getController()).setDefaultPrefValues();
			((PreferencesHandler)loader.getController()).handler = this.handler;
		} catch (IOException e) {
			System.out.println("Something went wrong with loading the preferences window in showPreferences()");
			e.printStackTrace();
		}
	}
	
	@FXML 
	void quit() {
		boolean wasSaved = true;
		if (hasPotentiallyBeenEdited) {
			ArrayList<String[]> data = handler.readFile(handler.csvDir, handler.csvName, false, -1);
			
			int[] indexes = 	{0, 4, 4, 7, 10, 15};
			String[] ignores = 	{";.;", "1", "0", "0:00", "0.0", "0.0"}; // Vampire is best special character
			
			for (int i = 0; i < inputFields.length; i++) { // Ignore date, it's always automatic
				String inputText = getText(inputFields[i]);
				
				boolean exhaustedAllPossibilities = true;
				for (int k = 0; k < indexes.length; k++) {
					if (i == indexes[k]) {
						if (ignores[k] == ";.;" ? false : !(getText(inputFields[i]).equals(ignores[k]))) {
							System.out.println("Stopped on index " + i + " , with value \"" + inputText + "\"..");
							
							wasSaved = false;
							break;
						} else {
							//System.out.println("Ignored input " + indexes[k] + " because of ignore value \"" + ignores[k] + "\"");
							exhaustedAllPossibilities = false;
							break;
						}
					}
				}
				if (wasSaved == false)
					break;
				
				if (exhaustedAllPossibilities) { // Went through all potential ignores, this wasn't one, so do a normal check
					if (!inputText.equals(("")) && !data.get(data.size() - 1)[i].equals(inputText)) {
						System.out.println("Stopped on index " + i + " , with value \"" + inputText + "\".");

						wasSaved = false;
						break;
					}
				} else {
					exhaustedAllPossibilities = true; // Reset it
				}
			}
		}
		
		if (wasSaved) {
			System.exit(0);
		} else {
			Alert quitWarning = new Alert(AlertType.WARNING);
			
			quitWarning.setTitle("Data Not Saved!");
			quitWarning.setHeaderText("If you continue, you will lose this data.");
			quitWarning.setContentText("The data currently contained in the input boxes does not match the last line of \"" + handler.csvName + "\". You may want to save this data.");
			
			ButtonType saveQuitButton = new ButtonType("New Row, Save, and Quit");
			ButtonType quitButton = new ButtonType("Quit Without Saving");
			ButtonType cancelButton = new ButtonType("Cancel");
			
			quitWarning.getButtonTypes().setAll(saveQuitButton, quitButton, cancelButton);
			
			Optional<ButtonType> warningResult = quitWarning.showAndWait();
			if (warningResult.get().equals(saveQuitButton)) {
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
			} else if (warningResult.get().equals(quitButton)) {
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
}