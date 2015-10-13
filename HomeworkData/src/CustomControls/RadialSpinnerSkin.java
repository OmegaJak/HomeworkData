package CustomControls;

import javafx.scene.control.SkinBase;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

public class RadialSpinnerSkin extends SkinBase<RadialSpinnerControl> {

	private Slider slider;
	
	private Region plusRegion;

	private Region minusRegion;
	private Rectangle rect;

	private BorderPane borderPane;
	
	protected RadialSpinnerSkin(RadialSpinnerControl control) {
		super(control);
		
		slider = new Slider(-1, 1, 0);
		
		/*slider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				getSkinnable().getProperties().put("plusminusslidervalue", newValue.doubleValue());
			}
		});*/
		
		plusRegion = new Region();
		
		minusRegion = new Region();
		
		rect = new Rectangle(10, 10);
		
		borderPane = new BorderPane();
		
		updateLayout();
		
		getChildren().add(borderPane);
	}

	private void updateLayout() {
		borderPane.setLeft(rect);
		borderPane.setCenter(slider);
		borderPane.setRight(plusRegion);
	}
}
