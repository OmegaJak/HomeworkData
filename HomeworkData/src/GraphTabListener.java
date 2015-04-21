import java.util.ArrayList;

import javafx.animation.PathTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.util.Duration;
import CustomCharts.CustomPieChart;
import CustomCharts.PieChart;
import CustomCharts.PieChart.LabelLayoutInfo;


public class GraphTabListener implements ChangeListener<Number> {
	
	private AnchorPane graphDisplay;
	//private ChoiceBox graphPicker;
	private DataHandler handler;
	String[] graphNames;
	private ChoiceBox graphPicker;
	
	public GraphTabListener(AnchorPane graphDisplay, ChoiceBox graphPicker, DataHandler handler) {
		this.graphDisplay = graphDisplay;
		//this.graphPicker = graphPicker;
		this.handler = handler;
		
		graphNames = new String[] {"Spent Time Pie Chart"};
		ObservableList<String> graphOptions = FXCollections.observableArrayList(graphNames);
		graphPicker.setItems(graphOptions);
		
		graphPicker.getSelectionModel().selectedIndexProperty().addListener(GraphTabListener.this);
		this.graphPicker = graphPicker;
	}

	@Override
	public void changed(ObservableValue<? extends Number> obsValue, Number oldValue, Number newValue) {
		if (newValue.intValue() != -1) { // -1 is given when unloaded
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

						final CustomPieChart chart = new CustomPieChart(pieChartData);
						chart.setTitle(graphNames[newValue.intValue()]);
						chart.scaleShapeProperty().set(true);

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
						graphDisplay.setTopAnchor(chart, 0.0);
						graphDisplay.setBottomAnchor(chart, 0.0);
						
					} catch (NumberFormatException e) {
						System.out.println("There was an error parsing some numbers when generating the \"Spent Time Pie Chart\"");
						handler.showErrorDialogue(e);
					}
					break;
			}
		}
		
	}
	
	public void unload() {
		graphDisplay.getChildren().clear();
		graphPicker.getSelectionModel().clearSelection();
		graphPicker.getSelectionModel().selectedIndexProperty().removeListener(GraphTabListener.this);
	}
	
	/**
	 * 
	 * @author Tom Schindl Took the origins of this from here: http://tomsondev.bestsolution.at/2012/11/21/animating-the-javafx-piechart-a-bit/
	 */
	static class MouseHoverAnimation implements EventHandler<MouseEvent> {
		static final Duration ANIMATION_DURATION = new Duration(300);
		static final double ANIMATION_DISTANCE = 0.1;
		private double cos;
		private double sin;
		private CustomPieChart chart;

		public MouseHoverAnimation(PieChart.Data d, CustomPieChart chart) {
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
			Node n = (Node)event.getSource();
			
			double minX = Double.MAX_VALUE; // Just temporarily
			double maxX = Double.MAX_VALUE * -1;

			for (PieChart.Data d : chart.getData()) { // Loops through, finding the max of the max x's (rightmost edge) and the min of the min x's (left edge)
				minX = Math.min(minX, d.getNode().getBoundsInParent().getMinX());
				maxX = Math.max(maxX, d.getNode().getBoundsInParent().getMaxX());
			}
			
			
			ArrayList<LabelLayoutInfo> fullPieLabels = ((CustomPieChart)n.getParent().getParent()).getFullPieLabels();
			ArrayList<Region> fullPieRegions  = ((CustomPieChart)n.getParent().getParent()).getFullPieRegions();
			
			int sliceIndex = 0;
			for (int i = 0; i < fullPieRegions.size(); i++) {
				if (fullPieRegions.get(i).equals(n)) {
					sliceIndex = i;
				}
			}
			
			ArrayList<ArrayList<PathElement>> drawnPathElements = ((CustomPieChart)n.getParent().getParent()).getCategorizedPathElements(((CustomPieChart)n.getParent().getParent()).getLabelLinePath().getElements());

			((CustomPieChart)n.getParent().getParent()).getLabelLinePath().setOpacity(0.0);
			
			ObservableList<Node> chartChildrenList = ((CustomPieChart)n.getParent().getParent()).getChartChildren();
			if (!(chartChildrenList.get(chartChildrenList.size() - 1) instanceof Path)) { // Ensure that we don;t just endlessly add Paths to the chart
				for (int i = 0; i < drawnPathElements.size(); i++) {
					Path newPath = new Path();
					for (PathElement element : drawnPathElements.get(i)) {
						if (element instanceof PathElement) { // Should always be true
							newPath.getElements().add((PathElement)element);
							newPath.getStyleClass().add("chart-pie-label-line");
						}
					}
					chart.getChartChildren().add(newPath);
				}
			}
			
			chartChildrenList = ((CustomPieChart)n.getParent().getParent()).getChartChildren();
			
			ArrayList<Path> paths = new ArrayList<Path>();
			for (int i = 1; i < chartChildrenList.size(); i++) {
				if (chartChildrenList.get(i) instanceof Path) {						
					paths.add((Path)chartChildrenList.get(i));
				}
			}
			
			//---Actually animating now
			double diameter = maxX - minX; // Just the difference between the right edge and the left edge of the pie
			double xTranslate = (diameter * ANIMATION_DISTANCE) * cos;
			double yTranslate = (diameter * ANIMATION_DISTANCE) * sin;
			double xCenter = 0;
			double yCenter = 0;
			if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED)) {

				for (int i = 0; i < 3; i++) {
					Path path = new Path();
					PathTransition pathTransition = new PathTransition();
					if (i == 0) { // Animating the slice (Region)
						xCenter = 0;
						yCenter = 0;
						path.getElements().add(new MoveTo(xCenter + n.getTranslateX(), yCenter + n.getTranslateY())); // Where it's starting
						path.getElements().add(new LineTo(xCenter + xTranslate, yCenter + yTranslate)); // Where it'll animate to
						pathTransition.setNode(n);
					} else if (i == 1) { // Animating the line
						Path relevantPath = paths.get(sliceIndex);
						xCenter = fullPieLabels.get(sliceIndex).startX + ((fullPieLabels.get(sliceIndex).endX - fullPieLabels.get(sliceIndex).startX) / 2);
						yCenter = fullPieLabels.get(sliceIndex).startY + ((fullPieLabels.get(sliceIndex).endY- fullPieLabels.get(sliceIndex).startY) / 2);
						path.getElements().add(new MoveTo(xCenter, yCenter)); // Where it's starting
						path.getElements().add(new LineTo(xCenter + xTranslate, yCenter + yTranslate)); // Where it'll animate to
						pathTransition.setNode(relevantPath);
					} else if (i == 2) { // Animating the label
						
					}
					pathTransition.setDuration(ANIMATION_DURATION);
					pathTransition.setPath(path);
					pathTransition.setCycleCount(1);
					pathTransition.setAutoReverse(false);

					pathTransition.play();
				}

			} else if (event.getEventType().equals(MouseEvent.MOUSE_EXITED)) {
				
				for (PieChart.Data d : chart.getData()) {
					minX = Math.min(minX, d.getNode().getBoundsInParent().getMinX());
					maxX = Math.max(maxX, d.getNode().getBoundsInParent().getMaxX());
				}
				
				for (int i = 0; i < 3; i++) {
					Path path = new Path();
					PathTransition pathTransition = new PathTransition();
					if (i == 0) { // Animating the slice (Region)
						xCenter = 0;
						yCenter = 0;
						pathTransition.setNode(n);
					} else if (i == 1) { // Animating the line
						Path relevantPath = paths.get(sliceIndex);
						xCenter = fullPieLabels.get(sliceIndex).startX + ((fullPieLabels.get(sliceIndex).endX - fullPieLabels.get(sliceIndex).startX) / 2);
						yCenter = fullPieLabels.get(sliceIndex).startY + ((fullPieLabels.get(sliceIndex).endY- fullPieLabels.get(sliceIndex).startY) / 2);
						pathTransition.setNode(relevantPath);
					} else if (i == 2) { // Animating the label
						
					}
					path.getElements().add(new MoveTo(xCenter + (n.getTranslateX() == 0 ? 0.01 : n.getTranslateX()), yCenter + (n.getTranslateY() == 0 ? 0.01 : n.getTranslateY()))); // Starting point
					path.getElements().add(new LineTo(xCenter, yCenter)); // Where it'll animate to
					pathTransition.setDuration(ANIMATION_DURATION);
					pathTransition.setPath(path);
					pathTransition.setCycleCount(1);
					pathTransition.setAutoReverse(false);

					pathTransition.play();
				}
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
