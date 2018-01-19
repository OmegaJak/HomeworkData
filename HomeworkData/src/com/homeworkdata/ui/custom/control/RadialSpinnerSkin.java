package com.homeworkdata.ui.custom.control;

import java.awt.Point;

import com.homeworkdata.helper.MathLib;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class RadialSpinnerSkin extends BehaviorSkinBase<RadialSpinner, RadialSpinnerBehavior> {

	public static final double RADIUS = 25.0;
	
	double orgSceneX, orgSceneY;
	double orgTranslateX, orgTranslateY;
	double oldTheta, lastDelta;
	
	private StackPane stackPane;
	
	private Circle circle;
	private StackPane thumb;
	
	private NumberTextField numField;
	
	protected RadialSpinnerSkin(RadialSpinner control) {
		super(control, new RadialSpinnerBehavior(control));
		
		circle = new Circle(RADIUS);
		circle.getStyleClass().setAll("radial-track");
		circle.setFill(Color.TRANSPARENT);
		
		thumb = new StackPane();
		thumb.getStyleClass().setAll("radial-thumb");
		thumb.setMaxHeight(20);
		thumb.setMaxWidth(20);
		thumb.setTranslateY(thumb.getTranslateY() + RADIUS); // Shift it down to 0 on the circle
		thumb.setFocusTraversable(false);
		
		thumb.setOnMousePressed(getBehavior().mousePressed);
		
		thumb.setOnKeyPressed(getBehavior().keyPressed);
		
		oldTheta = (control.getMin() / control.getMax()) * 360.0; // Need to initialize it with the starting position
		
		thumb.setOnMouseDragged(getBehavior().mouseDragged);
		
		numField = control.getNumberTextField();
		numField.setMaxWidth(40);
		numField.setAlignment(Pos.CENTER);
		numField.textProperty().addListener(getBehavior().numFieldChanged);
		
		stackPane = new StackPane();
		
		updateLayout();
		
		getChildren().add(stackPane);
	}

	private void updateLayout() {
		stackPane.getChildren().add(circle);
		stackPane.getChildren().add(numField);
		stackPane.getChildren().add(thumb);
	}
	
	public NumberTextField getNumberTextField() {
		return this.numField;
	}
	
	public StackPane getThumb() {
		return thumb;
	}

	public void setThumbPos(double newThumbX, double newThumbY) {
		thumb.setTranslateX(newThumbX);
		thumb.setTranslateY(newThumbY);
	}
}
