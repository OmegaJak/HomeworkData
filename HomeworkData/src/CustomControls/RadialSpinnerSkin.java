package CustomControls;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class RadialSpinnerSkin extends BehaviorSkinBase<RadialSpinner, RadialSpinnerBehavior> {

	private static final double RADIUS = 25.0;
	
	double orgSceneX, orgSceneY;
	double orgTranslateX, orgTranslateY;
	double oldTheta, lastDelta;
	
	private StackPane stackPane;
	
	private Circle circle;
	private Circle thumb;
	
	private NumberTextField numField;
	
	protected RadialSpinnerSkin(RadialSpinner control) {
		super(control, new RadialSpinnerBehavior(control));
		
		circle = new Circle(RADIUS);
		circle.setFill(Color.TRANSPARENT);
		circle.setStroke(Color.RED);
		
		thumb = new Circle(7);
		thumb.setFill(Color.BLUE);
		thumb.setTranslateY(thumb.getTranslateY() + RADIUS); // Shift it down to 0 on the circle
		
		thumb.setOnMousePressed(me -> {
			orgSceneX = me.getSceneX();
			orgSceneY = me.getSceneY();
			orgTranslateX = thumb.getTranslateX();
			orgTranslateY = thumb.getTranslateY();
		});
		
		thumb.setOnMouseDragged(me -> {
			//getBehavior().trackPress(me, (me.getX()));
			double offsetX = me.getSceneX() - orgSceneX; // How much the mouse has moved
			double offsetY = me.getSceneY() - orgSceneY;
			double newTranslateX = orgTranslateX + offsetX; // The new relative position of the mouse
			double newTranslateY = orgTranslateY + offsetY;
			
			double theta = calculateTheta(newTranslateX, newTranslateY);
			if (oldTheta > 180.0 && theta < 180.0 && lastDelta >= 0) // Doesn't let it go past max
				theta = 360.0;
			else if (oldTheta < 180.0 && theta > 180.0 && lastDelta <= 0) // Doesn't let it go past min
				theta = 0.0;
			lastDelta = theta - oldTheta;
			
			// I think these need the "+ Math.PI / 2.0" because the 0 degrees is at the 'bottom', not the 'right'
			double newThumbX = RADIUS * Math.cos(toRadians(theta) + Math.PI / 2.0);
			double newThumbY = RADIUS * Math.sin(toRadians(theta) + Math.PI / 2.0);
			
			oldTheta = theta;
			
			thumb.setTranslateX(newThumbX);
			thumb.setTranslateY(newThumbY);
			
			getBehavior().updateValue(theta);
			numField.setNumber(control.getValue());
		});
		
		numField = new NumberTextField(new BigDecimal(0), new DecimalFormat("#"));
		numField.setMaxWidth(40);
		numField.setAlignment(Pos.CENTER);
		
		stackPane = new StackPane();
		
		updateLayout();
		
		getChildren().add(stackPane);
	}

	private void updateLayout() {
		stackPane.getChildren().add(numField);
		stackPane.getChildren().add(circle);
		stackPane.getChildren().add(thumb);
	}
	
	private double calculateTheta(double x, double y) {
		double[] startingVector = {0.0, RADIUS};
		double[] pointVector = {x, y};
		double dotProduct = dotProduct(startingVector, pointVector);
		double magnitudeProduct = magnitude(startingVector) * magnitude(pointVector);
		
		double result = toDegrees(Math.acos(dotProduct / magnitudeProduct));
	
		if (x > 0)
			result = (180 - result) + 180;
		
		return result;
	}
	
	private double toRadians(double theta) {
		return (theta / 360.0) * 2 * Math.PI;
	}
	
	private double toDegrees(double theta) {
		return (theta / (2 * Math.PI)) * 360.0;
	}
	
	// vector1 and vector2 must have the same number of components
	private double dotProduct(double[] vector1, double[] vector2) {
		double result = 0;
		for (int i = 0; i < vector1.length; i++) {
			result += vector1[i] * vector2[i];
		}
		return result;
	}
	
	private double magnitude(double[] vector) {
		double sqrtInside = 0;
		for (int i = 0; i < vector.length; i++) {
			sqrtInside += vector[i] * vector[i];
		}
		return Math.sqrt(sqrtInside);
	}
}
