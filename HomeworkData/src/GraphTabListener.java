import java.util.ArrayList;

import javafx.animation.PathTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;


public class GraphTabListener implements ChangeListener<Number> {
	
	private AnchorPane graphDisplay;
	//private ChoiceBox graphPicker;
	private DataHandler handler;
	String[] graphNames;
	
	public GraphTabListener(AnchorPane graphDisplay, ChoiceBox graphPicker, DataHandler handler) {
		this.graphDisplay = graphDisplay;
		//this.graphPicker = graphPicker;
		this.handler = handler;
		
		graphNames = new String[] {"Spent Time Pie Chart"};
		ObservableList<String> graphOptions = FXCollections.observableArrayList(graphNames);
		graphPicker.setItems(graphOptions);
		
		graphPicker.getSelectionModel().selectedIndexProperty().addListener(GraphTabListener.this);
	}

	@Override
	public void changed(ObservableValue<? extends Number> obsValue, Number oldValue, Number newValue) {
		switch (graphNames[newValue.intValue()]) {
			case "Spent Time Pie Chart":
				try {
					System.out.println("Displaying \"Spent Time Pie Chart\"");
					ArrayList<String[]> totalTimes = handler.getClassTotalTimes(handler.csvDir, handler.csvName);

					for (int i = 0; i < totalTimes.size(); i++) {
						String className = totalTimes.get(i)[0];
						String totalSeconds = handler.convertTime(totalTimes.get(i)[1], "HH:MM", "SS");
						totalTimes.remove(i);
						totalTimes.add(i, new String[] {className, totalSeconds});
					}

					ObservableList<PieChart.Data> obsArr = FXCollections.observableArrayList();
					for (int i = 0; i < totalTimes.size(); i++) {
						obsArr.add(new PieChart.Data(totalTimes.get(i)[0], Integer.parseInt(totalTimes.get(i)[1])));
					}
					ObservableList<PieChart.Data> pieChartData = obsArr;
					
					final PieChart chart = new PieChart(pieChartData);
					chart.setTitle(graphNames[newValue.intValue()]);
					
					for (PieChart.Data data : pieChartData) {
						MouseHoverAnimation hoverAnim = new MouseHoverAnimation(data, chart);
						data.getNode().setOnMouseEntered(hoverAnim);
						data.getNode().setOnMouseExited(hoverAnim);
					}
					
					/*graphDisplay.setOnMouseMoved(new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event) {
							System.out.println("(X: " + event.getSceneX() + ", Y: " + event.getSceneY() + ")");
						}
					});*/
					
					graphDisplay.getChildren().add(chart);
				} catch (NumberFormatException e) {
					System.out.println("There was an error parsing some numbers when generating the \"Spent Time Pie Chart\"");
					handler.showErrorDialogue(e);
				}
				break;
		}
		
	}
	
	/**
	 * 
	 * @author Tom Schindl Took the origins of this from here: http://tomsondev.bestsolution.at/2012/11/21/animating-the-javafx-piechart-a-bit/
	 */
	static class MouseHoverAnimation implements EventHandler<MouseEvent> {
		static final Duration ANIMATION_DURATION = new Duration(500);
		static final double ANIMATION_DISTANCE = 0.1;
		private double cos;
		private double sin;
		private PieChart chart;

		public MouseHoverAnimation(PieChart.Data d, PieChart chart) {
			this.chart = chart;
			double start = 0;
			double angle = calcAngle(d); // Figures out how many degrees wide the slice is
			for (PieChart.Data tmp : chart.getData()) { // This compensates for the starting angle of the pie slice, goes till it gets to the one it wants
				if (tmp == d) {
					break;
				}
				start += calcAngle(tmp);
			}

			cos = Math.cos(Math.toRadians(0 - start - angle / 2)); // The horizontal leg of the triangle
			sin = Math.sin(Math.toRadians(0 + start + angle / 2)); // The vertical leg of the triangle
		}

		@Override
		public void handle(MouseEvent event) {
			if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED)) {
				Node n = (Node)event.getSource();

				double minX = Double.MAX_VALUE; // Just temporarily
				double maxX = Double.MAX_VALUE * -1;

				for (PieChart.Data d : chart.getData()) { // Loops through, finding the max of the max x's (rightmost edge) and the min of the min x's (left edge)
					minX = Math.min(minX, d.getNode().getBoundsInParent().getMinX());
					maxX = Math.max(maxX, d.getNode().getBoundsInParent().getMaxX());
				}

				double diameter = maxX - minX; // Just the difference between the right edge and the left edge
				Path path = new Path();
				double xCenter = 0;
				double yCenter = 0;
				double xTranslate = (diameter * ANIMATION_DISTANCE) * cos;
				double yTranslate = (diameter * ANIMATION_DISTANCE) * sin;
				path.getElements().add(new MoveTo(xCenter + n.getTranslateX(), yCenter + n.getTranslateY())); // Where it's starting
				path.getElements().add(new LineTo(xCenter + xTranslate, yCenter + yTranslate)); // Where it'll animate to

				PathTransition pathTransition = new PathTransition();
				pathTransition.setDuration(ANIMATION_DURATION);
				pathTransition.setNode(n);
				pathTransition.setPath(path);
				pathTransition.setCycleCount(1);
				pathTransition.setAutoReverse(false);

				pathTransition.play();
			} else if (event.getEventType().equals(MouseEvent.MOUSE_EXITED)) {
				Node n = (Node)event.getSource();

				double minX = Double.MAX_VALUE;
				double maxX = Double.MAX_VALUE * -1;

				for (PieChart.Data d : chart.getData()) {
					minX = Math.min(minX, d.getNode().getBoundsInParent().getMinX());
					maxX = Math.max(maxX, d.getNode().getBoundsInParent().getMaxX());
				}

				double diameter = maxX - minX;
				Path path = new Path();
				double xCenter = 0;
				double yCenter = 0;
				double xTranslate = (diameter * ANIMATION_DISTANCE) * cos;
				double yTranslate = (diameter * ANIMATION_DISTANCE) * sin;
				path.getElements().add(new MoveTo(xCenter + n.getTranslateX(), yCenter + n.getTranslateY()));
				path.getElements().add(new LineTo(xCenter, yCenter));

				PathTransition pathTransition = new PathTransition();
				pathTransition.setDuration(ANIMATION_DURATION);
				pathTransition.setNode(n);
				pathTransition.setPath(path);
				pathTransition.setCycleCount(1);
				pathTransition.setAutoReverse(false);
				
				pathTransition.play();
			}
		}

		private static double calcAngle(PieChart.Data d) {
			double total = 0;
			for (PieChart.Data tmp : d.getChart().getData()) {
				total += tmp.getPieValue();
			}

			return 360.0 * (d.getPieValue() / total);
		}
	}
}
