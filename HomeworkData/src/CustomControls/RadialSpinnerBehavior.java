package CustomControls;

import java.util.ArrayList;
import java.util.List;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;

import javafx.scene.input.MouseEvent;

public class RadialSpinnerBehavior extends BehaviorBase<RadialSpinnerControl> {
	
	protected static final List<KeyBinding> RADIAL_SPINNER_BINDINGS = new ArrayList<KeyBinding>();
	
	public RadialSpinnerBehavior(RadialSpinnerControl control) {
		super(control, RADIAL_SPINNER_BINDINGS);
	}

	public void trackPress(MouseEvent me, double position) {
		final RadialSpinnerControl radial = getControl();
		
		if (!radial.isFocused()) radial.requestFocus();
		
	}

}
