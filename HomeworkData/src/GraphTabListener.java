import java.util.ArrayList;

import javafx.animation.PathTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Text;
import javafx.util.Duration;
import CustomCharts.CustomPieChart;
import CustomCharts.PieChart;
import CustomCharts.PieChart.LabelLayoutInfo;


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
					
					final CustomPieChart chart = new CustomPieChart(pieChartData);
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
				
				
				ArrayList<LabelLayoutInfo> fullPieLabels = ((CustomPieChart)n.getParent().getParent()).getFullPieLabels();
				ArrayList<Region> fullPieRegions  = ((CustomPieChart)n.getParent().getParent()).getFullPieRegions();
				
				int sliceIndex = 0;
				for (int i = 0; i < fullPieRegions.size() / 2; i++) {
					if (fullPieRegions.get(i).equals(n)) {
						sliceIndex = i;
						System.out.println("i is " + i);
					}
				}
				//System.out.println(fullPieRegions.toString());
				
				
				
				
				/*for (Node chartNode : ((CustomPieChart)n.getParent().getParent()).getChartChildren()) {
					System.out.println(chartNode.toString());
				}
				System.out.println("-------------------------------");*/
				
				/*ArrayList<ArrayList<Object>> chartChildren = ((CustomPieChart)n.getParent().getParent()).parseChildren();
				for (int i = 0; i < chartChildren.size(); i++) {
					ArrayList<Object> list = chartChildren.get(i);
					
					if (list.get(0).equals(n)) {
						System.out.println(((Text)list.get(1)).getText());
					}
				}*/
				
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
				
				Path relevantPath = paths.get(sliceIndex);
				Path pathPath = new Path();
				xCenter = ((MoveTo)relevantPath.getElements().get(0)).getX();
				yCenter = ((MoveTo)relevantPath.getElements().get(0)).getY();
				double xDiff = (((MoveTo)relevantPath.getElements().get(2)).getX() - ((MoveTo)relevantPath.getElements().get(0)).getX()) / 2.0;
				double yDiff = (((MoveTo)relevantPath.getElements().get(2)).getY() - ((MoveTo)relevantPath.getElements().get(0)).getY()) / 2.0;
				xCenter += xDiff;
				yCenter += yDiff;
				pathPath.getElements().add(new MoveTo(xCenter, yCenter)); // Where it's starting
				pathPath.getElements().add(new LineTo(xCenter + xTranslate, yCenter + yTranslate)); // Where it'll animate to
				
				PathTransition pathTransition2 = new PathTransition();
				pathTransition2.setDuration(ANIMATION_DURATION);
				pathTransition2.setNode(relevantPath);
				pathTransition2.setPath(pathPath);
				pathTransition2.setCycleCount(1);
				pathTransition2.setAutoReverse(false);

				pathTransition2.play();

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
				path.getElements().add(new MoveTo(xCenter + (n.getTranslateX() == 0 ? 0.01 : n.getTranslateX()), yCenter + (n.getTranslateY() == 0 ? 0.01 : n.getTranslateY())));
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
