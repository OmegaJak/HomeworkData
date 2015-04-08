import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;


public class PreferencesHandler {

	@FXML private TextField csvDirTextField;
	@FXML private TextField csvNameTextField;
	@FXML private Button refreshButton;
	
	private String[] prefKeys;
	private String[] prefDefs;
	private Preferences prefs;
	public DataHandler handler;
	
	@FXML
	private void initialize() {
		prefs = Preferences.userRoot().node(DataHandler.class.getName());
		
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
					prefs.put(prefKeys[0], csvDirTextField.getText());
				}
			}
		});
		
		csvNameTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
				if (!newPropertyValue) {
					prefs.put(prefKeys[1], csvNameTextField.getText());
				}
			}
		});
		
		refreshButton.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
				if (!newPropertyValue) {
					//handler.refreshPreferences(); This probably actually isn't a good idea yet
				}
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
