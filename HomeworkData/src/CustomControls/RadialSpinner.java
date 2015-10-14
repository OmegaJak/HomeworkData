package CustomControls;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.Slider;

public class RadialSpinner extends Control {

	public RadialSpinner() {
		
	}
	
	@Override
	protected Skin<?> createDefaultSkin() {
		return new RadialSpinnerSkin(this);
	}
	
	// All this max stuff is VERY closely based on Slider.class's implementation of it
	private DoubleProperty max;
    public final void setMax(double value) {
        maxProperty().set(value);
    }

    public final double getMax() {
        return max == null ? 100 : max.get();
    }

    public final DoubleProperty maxProperty() {
        if (max == null) {
            max = new DoublePropertyBase(100) {
                @Override protected void invalidated() {
                    /*if (get() < getMin()) {
                        setMin(get());
                    }
                    adjustValues();*/
                    notifyAccessibleAttributeChanged(AccessibleAttribute.MAX_VALUE);
                }

                @Override
                public Object getBean() {
                    return RadialSpinner.this;
                }

                @Override
                public String getName() {
                    return "max";
                }
            };
        }
        return max;
    }
    
    // Again, right from Slider.class
    private DoubleProperty min;
    public final void setMin(double value) {
        minProperty().set(value);
    }

    public final double getMin() {
        return min == null ? 0 : min.get();
    }

    public final DoubleProperty minProperty() {
        if (min == null) {
            min = new DoublePropertyBase(0) {
                @Override protected void invalidated() {
                    if (get() > getMax()) {
                        setMax(get());
                    }
                    //adjustValues();
                    notifyAccessibleAttributeChanged(AccessibleAttribute.MIN_VALUE);
                }

                @Override
                public Object getBean() {
                    return RadialSpinner.this;
                }

                @Override
                public String getName() {
                    return "min";
                }
            };
        }
        return min;
    }
    
    // Same source as min and max
    private DoubleProperty value;
    public final void setValue(double value) {
        if (!valueProperty().isBound()) valueProperty().set(value);
    }

    public final double getValue() {
        return value == null ? 0 : value.get();
    }

    public final DoubleProperty valueProperty() {
        if (value == null) {
            value = new DoublePropertyBase(0) {
                @Override protected void invalidated() {
                    //adjustValues();
                    notifyAccessibleAttributeChanged(AccessibleAttribute.VALUE);
                }

                @Override
                public Object getBean() {
                    return RadialSpinner.this;
                }

                @Override
                public String getName() {
                    return "value";
                }
            };
        }
        return value;
    }
}
