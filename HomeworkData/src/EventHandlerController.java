/**
 * Credit for the basis of this goes to this blog:
 * http://code.makery.ch/blog/javafx-2-event-handlers-and-change-listeners/
 */

import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;

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
		
		numUnitField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when the startedField comes into focus
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
				if (!newPropertyValue) {
					checkForTimePerUnit();
					checkForTimePrediction();
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
					String[] units = handler.getColumnArray(3, false, handler.csvDir, handler.csvName, false, 2, typeField.getEditor().getText());
					ObservableList<String> unitOptions = FXCollections.observableArrayList(units);
					unitField.setItems(unitOptions);
					checkForTimePrediction();
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

	private void clearInputs() {
		for (int i = 0; i < inputFields.length; i++) {
			if (i == 0 || i > 3) {
				((TextField)inputFields[i]).setText("");
			} else if (i > 0 && i < 3) {
				((ComboBox)inputFields[i]).getEditor().setText("");
			}
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
			TextField[] neededInputs = { numUnitField, startedField, endedField };
			if (checkIfAllFilled(neededInputs)) {
				timeUnitField.setText(handler.convertTime(handler.divideTime(handler.subtractTime(startedField.getText(), endedField.getText()), Integer.parseInt(numUnitField.getText())), "H:MM:SS", "MM:SS"));
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
			
			System.out.println("Saved");
			
			long period = 1;
			TimerTask task = new TimerTask() {
				int timesToRun = 201;
				boolean isStillDecreasing = true;
				public void run() {
					if (timesToRun == 0) {
						cancel();
					} else {
						Platform.runLater(new Runnable() {
						    @Override 
						    public void run() {
						    	double opacity;
						    	opacity = saveRowButton.getOpacity();
						    	if (opacity > 0 && isStillDecreasing) {
						    		saveRowButton.setOpacity(opacity - .01);
						    	} else if (opacity <= 0.0) {
						    		isStillDecreasing = false;
						    		saveRowButton.setOpacity(opacity + .01);
						    	} else if (opacity == 1.0) {
						    		isStillDecreasing = true;
						    	} else {
						    		saveRowButton.setOpacity(opacity + .01);
						    	}
						    }
						});
						
						timesToRun -= 1;
					}
				}
			};
			
			Timer timer = new Timer();
			timer.schedule(task, 0, period);
			
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
}