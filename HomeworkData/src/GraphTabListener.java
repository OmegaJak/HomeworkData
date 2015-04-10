import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;


public class GraphTabListener implements ChangeListener<Number> {
	
	private AnchorPane graphDisplay;
	private ChoiceBox graphPicker;
	private DataHandler handler;
	String[] graphNames;
	
	public GraphTabListener(AnchorPane graphDisplay, ChoiceBox graphPicker, DataHandler handler) {
		this.graphDisplay = graphDisplay;
		this.graphPicker = graphPicker;
		this.handler = handler;
		
		graphNames = new String[] {"Spent Time Pie Chart"};
		ObservableList<String> graphOptions = FXCollections.observableArrayList(graphNames);
		graphPicker.setItems(graphOptions);
		
		graphPicker.getSelectionModel().selectedIndexProperty().addListener(GraphTabListener.this);
	}

	@Override
	public void changed(ObservableValue<? extends Number> obsValue, Number oldValue, Number newValue) {
		switch (graphNames[newValue.intValue()]) {
			case "Spent Time Pie Chart":
				System.out.println("Displaying \"Spent Time Pie Chart\"");
				
				break;
		}
	}
}
