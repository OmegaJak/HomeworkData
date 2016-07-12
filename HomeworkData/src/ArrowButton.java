import javafx.css.Styleable;
import javafx.event.EventTarget;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Skinnable;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;


public class ArrowButton extends Button implements Styleable, EventTarget, Skinnable {

	private double ARROW_SIZE;
	StackPane buttonStack;
	Polygon triangle;
	
	public ArrowButton() {
		this(6);
	}
	
	public ArrowButton(double size) {
		super();
		this.ARROW_SIZE = size;
		this.buttonStack = new StackPane();
		this.triangle = getArrow();
		triangle.setMouseTransparent(true);
		buttonStack.getChildren().addAll(this, this.triangle);
		buttonStack.setAlignment(Pos.CENTER);
	}

	public ArrowButton(String text, Node graphic) {
		super(text, graphic);
		// TODO Auto-generated constructor stub
	}

	private Polygon getArrow() {
		Polygon triangle = new Polygon();
		triangle.getPoints().addAll(new Double[]{0.0, 0.0, ARROW_SIZE, 0.0, 0.5*ARROW_SIZE, -0.5*ARROW_SIZE});
		
		return triangle;
	}
	
	public StackPane getStackPane() {
		return this.buttonStack;
	}
	
	public void setArrowDirection(ArrowDirection direction) {
		switch (direction) {
			case UP:
				this.triangle.rotateProperty().set(0.0);
				break;
			case DOWN:
				this.triangle.rotateProperty().set(180.0);
				break;
			case LEFT:
				this.triangle.rotateProperty().set(270.0);
				break;
			case RIGHT:
				this.triangle.rotateProperty().set(90.0);
				break;
		}
	}
	
	public void setArrowRotationAngle(double angle) {
		this.triangle.rotateProperty().set(angle);
	}
}
