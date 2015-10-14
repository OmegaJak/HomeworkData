package CustomControls;

import java.util.ArrayList;
import java.util.List;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;

import javafx.scene.input.MouseEvent;

public class RadialSpinnerBehavior extends BehaviorBase<RadialSpinner> {
	
	protected static final List<KeyBinding> RADIAL_SPINNER_BINDINGS = new ArrayList<KeyBinding>();
	
	public RadialSpinnerBehavior(RadialSpinner control) {
		super(control, RADIAL_SPINNER_BINDINGS);
	}

	/*public void trackPress(MouseEvent me, double position) {
		final RadialSpinner radial = getControl();
		
		if (!radial.isFocused()) radial.requestFocus();
	}*/
	
	public void updateValue(double theta) {
		final RadialSpinner spinner = getControl();
		spinner.setValue(getValueFromTheta(theta));
	}
	
	private double getValueFromTheta(double theta) {
		return (theta / 360.0) * getControl().getMax();
	}
}
