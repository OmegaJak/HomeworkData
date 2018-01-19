package com.homeworkdata.ui.custom.control;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;

public class RadialSpinner extends Control {

	public static final int DEFAULT_MAX = 100;
	public static final int DEFAULT_MIN = 0;
	
	private NumberTextField numTextField;
	
	public RadialSpinner() {
		getStyleClass().add("radial-spinner");
		numTextField = new NumberTextField(new BigDecimal(0), new DecimalFormat("#"));
	}
	
	@Override
	protected Skin<?> createDefaultSkin() {
		RadialSpinnerSkin toReturn = new RadialSpinnerSkin(this);
		toReturn.getBehavior().skin = toReturn;
		numTextField.setText("" + (int)getMin());
		numTextField.parseAndFormatInput();
		return toReturn;
	}
	
	@Override
	public String getUserAgentStylesheet() {
		return RadialSpinner.class.getResource("css/radialSpinner.css").toExternalForm();
	}
	
	// All this max stuff is VERY closely based on Slider.class's implementation of it
	private DoubleProperty max;
    public final void setMax(double value) {
        maxProperty().set(value);
    }

    public final double getMax() {
        return max == null ? DEFAULT_MAX : max.get();
    }

    public final DoubleProperty maxProperty() {
        if (max == null) {
            max = new DoublePropertyBase(DEFAULT_MAX) {
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
        return min == null ? DEFAULT_MIN : min.get();
    }

    public final DoubleProperty minProperty() {
        if (min == null) {
            min = new DoublePropertyBase(DEFAULT_MIN) {
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
        if (!valueProperty().isBound()) {
        	valueProperty().set(value);
        	if (value > getMax()) {
        		setMax(value);
        	} else if (value < getMin()) {
        		setMin(value);
        	}
        }
        numTextField.setNumber(getValue());
    }

    public final double getValue() {
        return value == null ? DEFAULT_MIN : value.get();
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
    
    protected void setTextField(NumberTextField field) {
    	this.numTextField = field;
    }
    
    public TextField getEditor() {
    	return (TextField)this.numTextField;
    }
    
    public NumberTextField getNumberTextField() {
    	return this.numTextField;
    }
}
