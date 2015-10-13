package CustomControls;

import javafx.geometry.Orientation;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class RadialSpinnerControl extends Control {

	public RadialSpinnerControl() {
		
	}
	
	@Override
	protected Skin<?> createDefaultSkin() {
		return new RadialSpinnerSkin(this);
	}
}
