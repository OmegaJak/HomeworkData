import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;


public class GraphPickerListener implements ChangeListener<Number> {

	private AnchorPane graphDisplay;
	
	public GraphPickerListener(AnchorPane graphDisplay) {
		this.graphDisplay = graphDisplay;
		
		Label label = new Label("Test");
		graphDisplay.getChildren().add(label);
	}

	@Override
	public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
		
	}

}
