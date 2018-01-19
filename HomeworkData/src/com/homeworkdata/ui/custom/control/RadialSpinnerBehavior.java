package com.homeworkdata.ui.custom.control;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.homeworkdata.helper.MathLib;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class RadialSpinnerBehavior extends BehaviorBase<RadialSpinner> {
	
	protected static final List<KeyBinding> RADIAL_SPINNER_BINDINGS = new ArrayList<KeyBinding>();
	
	public RadialSpinnerSkin skin;
	
	double orgSceneX, orgSceneY;
	double orgTranslateX, orgTranslateY;
	double oldTheta, lastDelta;
	
	public EventHandler<MouseEvent> mouseDragged = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent me) {
			//getBehavior().trackPress(me, (me.getX()));
			double offsetX = me.getSceneX() - orgSceneX; // How much the mouse has moved
			double offsetY = me.getSceneY() - orgSceneY;
			double newTranslateX = orgTranslateX + offsetX; // The new relative position of the mouse
			double newTranslateY = orgTranslateY + offsetY;

			double theta = MathLib.calculateTheta(newTranslateX, newTranslateY, skin.RADIUS);
			double max = getControl().getMax(), min = getControl().getMin();
			double minTheta = (min / max) * 360.0;
			double halfwayTheta = ((360.0 - minTheta) / 2.0) + minTheta;
			if (oldTheta > halfwayTheta && theta < halfwayTheta && lastDelta >= 0) // Doesn't let it go past max
				theta = 360.0;
			else if ((oldTheta >= minTheta && theta < minTheta && lastDelta <= 0)
					|| (oldTheta < halfwayTheta && theta > halfwayTheta && lastDelta <= 0))
				theta = (min / max) * 360.0;

			lastDelta = theta - oldTheta;

			// I think these need the "+ Math.PI / 2.0" because the 0 degrees is at the 'bottom', not the 'right'
			double newThumbX = RadialSpinnerSkin.RADIUS * Math.cos(MathLib.toRadians(theta) + Math.PI / 2.0);
			double newThumbY = RadialSpinnerSkin.RADIUS * Math.sin(MathLib.toRadians(theta) + Math.PI / 2.0);

			oldTheta = theta;

			skin.setThumbPos(newThumbX, newThumbY);

			updateValue(theta);
			//numField.setNumber(control.getValue());
		}
	};
	
	public EventHandler<MouseEvent> mousePressed = new EventHandler<MouseEvent>() {
		@Override 
		public void handle(MouseEvent me) {
			if (!skin.getThumb().isFocused())
				skin.getThumb().requestFocus();
			orgSceneX = me.getSceneX();
			orgSceneY = me.getSceneY();
			orgTranslateX = skin.getThumb().getTranslateX();
			orgTranslateY = skin.getThumb().getTranslateY();
		}
	};
	
	public EventHandler<KeyEvent> keyPressed = new EventHandler<KeyEvent>() {
		@Override
		public void handle(KeyEvent keyEvent) {
			KeyCode code = keyEvent.getCode();
			switch (code) {
				case UP:
				case RIGHT:
					getControl().setValue(getControl().getValue() + 1);
					updatePosition();
					break;
				case DOWN:
				case LEFT:
					double newVal = getControl().getValue();
					if (newVal - 1 >= 0)
						newVal = newVal - 1;
					getControl().setValue(newVal);
					updatePosition();
					break;
			}
		}
	};
	
	public ChangeListener<String> numFieldChanged = new ChangeListener<String>() {
		@Override
		public void changed(ObservableValue observable, String oldValue, String newValue) {
			if (!getControl().getNumberTextField().getText().equals("")) {
				getControl().getNumberTextField().parseAndFormatInput();
				getControl().setValue(getControl().getNumberTextField().getNumber().doubleValue());
				updatePosition();
			}
		}
	};
	
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
	
	public void updatePosition() {
		Point newPos = getPosition(MathLib.calculateThetaFromPercent(getControl().getValue() / getControl().getMax()));
		
		skin.setThumbPos(newPos.getX(), newPos.getY());
	}
	
	private Point getPosition(double theta) {
		return new Point((int)Math.round(skin.RADIUS * Math.cos(theta)), (int)Math.round(skin.RADIUS * Math.sin(theta)));
	}
}
