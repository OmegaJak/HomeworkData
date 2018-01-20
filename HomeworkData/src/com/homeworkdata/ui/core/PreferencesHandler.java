package com.homeworkdata.ui.core;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.homeworkdata.data.DataHandler;
import com.homeworkdata.ui.custom.control.NumberSpinner;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;


public class PreferencesHandler {

	@FXML private TextField csvDirTextField;
	@FXML private TextField csvNameTextField;
	@FXML private Button refreshButton;
	@FXML private HBox yearBox;
	
	private String[] prefKeys;
	private String[] prefDefs;
	private Preferences prefs;
	public DataHandler handler;
	public EventHandlerController controller;
	public NumberSpinner yearSpinner;
	
	@FXML
	private void initialize() {
		prefs = Preferences.userNodeForPackage(DataHandler.class);
		
		try {
			prefs.sync(); // Not entirely sure that I need this or what exactly it does, but it seems like a good idea
		} catch (BackingStoreException e) {
			System.out.println("The syncing operation in csvDirTextField.focusedProperty failed");
			e.printStackTrace();
		}
		
		
		
		csvDirTextField.focusedProperty().addListener(new ChangeListener<Boolean>() { // Add a listener for when the startedField comes out of focus
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
				if (!newPropertyValue) {
					String newDir = csvDirTextField.getText();
					newDir = newDir.replace('\\', '/');
					if (newDir.charAt(newDir.length() - 1) != '/') {
						newDir = newDir + '/';
					}
					csvDirTextField.textProperty().set(newDir);
					
					if (verifyPreferences()) {
						prefs.put(prefKeys[0], newDir);
						System.out.println("Preference for \'" + prefKeys[0] + "\' saved as " + newDir);
					}
				}
			}
		});
		
		csvNameTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
				if (!newPropertyValue) {
					String newName = csvNameTextField.getText();
					if (newName.indexOf('.') != -1) {
						newName = newName.substring(0, newName.indexOf('.')); // Take everything before the dot
					}
					csvNameTextField.textProperty().set(newName);
					
					prefs.put(prefKeys[1], newName);
					System.out.println("Preference for \'" + prefKeys[1] + "\' saved as " + newName);
				}
			}
		});
		refreshButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				handler.refreshPreferences();
				yearSpinner.numberProperty().setValue(new BigDecimal(handler.mostRecentYear));
				controller.refreshInterface();
			}
		});
	}
	
	protected boolean verifyPreferences() {
		boolean validDir = Files.isDirectory(Paths.get(csvDirTextField.textProperty().get()));
		
		if (!validDir) {
			Alert invalidWarning = new Alert(AlertType.WARNING);
			
			invalidWarning.setTitle("Invalid Directory");
			invalidWarning.setHeaderText("The directory specified is invalid!");
			invalidWarning.setContentText("The directory may not exist, or may contain invalid characters. "
					+ "Please fix the mistake and try again.");
			
			invalidWarning.showAndWait();
		}
		
		return validDir;
	}

	public void initYearSpinner() {
		yearBox.getChildren().add(yearSpinner);
		yearBox.setMargin(yearSpinner, new Insets(30.0, 11, 0.0, 12.5));
		yearSpinner.numberProperty().addListener(new ChangeListener<BigDecimal>() {
			@Override
			public void changed(ObservableValue<? extends BigDecimal> observable, BigDecimal oldValue, BigDecimal newValue) {
				EventHandlerController.PastManager pastManager = controller.pastManager;
				if (pastManager.IsInPast()) {
					if (!controller.isSaved(pastManager.getCurrentLine())) {
						int saved = controller.showSaveWarning("Continue");
						if (saved == 0) {
							controller.newRow();
							controller.saveData();
						} else if (saved == 2) {
							yearSpinner.numberProperty().setValue(oldValue);
							return;
						}
					}
					pastManager.resetCurrentLine();
				}
				handler.mostRecentYear = newValue.intValue();
				controller.refreshInterface(); // Gotta refresh these, since they depend on which year it currently is
			}
		});
	}
	
	public void setPrefArrays(String[] prefKeys, String[] prefDefs) {
		this.prefKeys = prefKeys;
		this.prefDefs = prefDefs;
	}

	public void setDefaultPrefValues() { // Called after initialized
		csvDirTextField.setText(prefs.get(prefKeys[0], prefDefs[0]));
		csvNameTextField.setText(prefs.get(prefKeys[1], prefDefs[1]));
	}
}
