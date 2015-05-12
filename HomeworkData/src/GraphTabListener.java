import java.util.ArrayList;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.util.Duration;
import CustomCharts.CustomPieChart;
import CustomCharts.PieChart;
import CustomCharts.PieChart.LabelLayoutInfo;


public class GraphTabListener implements ChangeListener<Number> {
	
	private AnchorPane graphDisplay;
	private DataHandler handler;
	String[] graphNames;
	private ChoiceBox graphPicker;
	
	public GraphTabListener(AnchorPane graphDisplay, ChoiceBox graphPicker, DataHandler handler) {
		this.graphDisplay = graphDisplay;
		this.handler = handler;
		
		graphNames = new String[] {"Total Spent Time Pie Chart", "Spent Time Line Chart"};
		ObservableList<String> graphOptions = FXCollections.observableArrayList(graphNames);
		graphPicker.setItems(graphOptions);
		
		graphPicker.getSelectionModel().selectedIndexProperty().addListener(GraphTabListener.this);
		this.graphPicker = graphPicker;
	}

	@Override
	public void changed(ObservableValue<? extends Number> obsValue, Number oldValue, Number newValue) {
		graphDisplay.getChildren().clear();
		if (newValue.intValue() != -1) { // -1 is given when unloaded
			System.out.println("Displaying \"" + graphNames[newValue.intValue()] + "\"");
			switch (graphNames[newValue.intValue()]) {
				case "Total Spent Time Pie Chart":
					try {
						ArrayList<String[]> totalTimes = handler.getClassTotalTimes(handler.csvDir, handler.csvName);

						for (int i = 0; i < totalTimes.size(); i++) {
							String className = totalTimes.get(i)[0];
							String totalSeconds = handler.convertTime(totalTimes.get(i)[1], "HH:MM", "SS", false);
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
							PieChartMouseHoverAnimation hoverAnim = new PieChartMouseHoverAnimation(data, chart, pieChartData.size());
							data.getNode().setOnMouseEntered(hoverAnim);
							data.getNode().setOnMouseExited(hoverAnim);
						}

						graphDisplay.getChildren().add(chart);
						
					} catch (NumberFormatException e) {
						System.out.println("There was an error parsing some numbers when generating the \"Total Spent Time Pie Chart\"");
						handler.showErrorDialogue(e);
					}
					break;
				case "Spent Time Line Chart":
					CheckBox showBlanks = new CheckBox("Show Empty Days");
					
					final CategoryAxis xAxis = new CategoryAxis();
					final NumberAxis yAxis = new NumberAxis();
					xAxis.setLabel("Date");
					final LineChart<String, Number> lineChart = new LineChart<String, Number>(xAxis, yAxis);
					lineChart.setTitle("Spent Time Line Chart");
					
					yAxis.setTickLabelFormatter(new NumberTimeStringConverter(handler));
					
					XYChart.Series series = getLineChartData(showBlanks.selectedProperty().getValue());
					
					showBlanks.selectedProperty().addListener(new ChangeListener<Boolean>() {
						public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue) {
							lineChart.getData().clear(); // Get rid of the old data
							XYChart.Series series = getLineChartData(showBlanks.selectedProperty().getValue()); // Get the new data
							lineChart.getData().add(series); // Add that new data
						}
					});
			        
			        lineChart.getData().add(series);
			        
					graphDisplay.getChildren().add(lineChart);
					graphDisplay.getChildren().add(showBlanks);
					
					break;
			}
		}
	}
	
	private XYChart.Series getLineChartData(boolean shouldShowBlanks) {
		XYChart.Series toReturn = new XYChart.Series();
		toReturn.setName("Time Spent");

		ArrayList<DataPoint> dataPoints = handler.getLineChartData("day", shouldShowBlanks);
		for (DataPoint dataPoint : dataPoints) {
			toReturn.getData().add(new XYChart.Data(dataPoint.getDate(), dataPoint.getSecondsSpent()));
		}

		return toReturn;
	}
	
	public void unload() {
		graphDisplay.getChildren().clear();
		graphPicker.getSelectionModel().clearSelection();
		graphPicker.getSelectionModel().selectedIndexProperty().removeListener(GraphTabListener.this);
	}
	
	/**
	 * 
	 * @author Tom Schindl
	 * Took the origins of this from here: http://tomsondev.bestsolution.at/2012/11/21/animating-the-javafx-piechart-a-bit/
	 */
	static class PieChartMouseHoverAnimation implements EventHandler<MouseEvent> {
		Duration animationDuration = new Duration(500);
		static final double ANIMATION_DISTANCE = 0.1;
		private double cos;
		private double sin;
		private CustomPieChart chart;
		public ArrayList<ArrayList<TranslateTransition>> transitions; // The first arrayList is for Region transitions, then linePaths, then Labels
		double lineStartX = 500;
		double lineStartY = 300;
		
		public PieChartMouseHoverAnimation(PieChart.Data d, CustomPieChart chart, int numberOfData) {
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
			
			transitions = new ArrayList<ArrayList<TranslateTransition>>();
			transitions.add(new ArrayList<TranslateTransition>());
			transitions.add(new ArrayList<TranslateTransition>());
			transitions.add(new ArrayList<TranslateTransition>());
			for (ArrayList<TranslateTransition> innerList : transitions) {
				for (int i = 0; i < numberOfData; i++) {
					innerList.add(null); // I realize this is dangerous, but I just want the indexes to be there and accessible
				}
			}
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
			if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED)) {
				double startTime = 2000;
				if (transitions.get(0).get(sliceIndex) != null) {
					startTime =  transitions.get(0).get(sliceIndex).getCurrentTime().toMillis();
				}
				for (int i = 0; i < 3; i++) {
					boolean isFirstTime = transitions.get(i).get(sliceIndex) == null;
					TranslateTransition translateTransition = new TranslateTransition();
					
					if (i == 0) { // Animating the slice (Region)
						translateTransition = new TranslateTransition(animationDuration, n);
					} else if (i == 1) { // Animating the line
						Path relevantPath = paths.get(sliceIndex);
						translateTransition = new TranslateTransition(animationDuration, relevantPath);
					} else if (i == 2) { // Animating the label
						translateTransition = new TranslateTransition(animationDuration, fullPieLabels.get(sliceIndex).text);
					}
					
					if (isFirstTime) {
						translateTransition.setByX(xTranslate);
						translateTransition.setByY(yTranslate);
						translateTransition.setCycleCount(1);
						translateTransition.setAutoReverse(false);
						translateTransition.setInterpolator(Interpolator.LINEAR);
						translateTransition.play();
						transitions.get(i).set(sliceIndex, translateTransition);
						
						if (i == 1) {
							Line line = new Line();
							line.setStartX(lineStartX);
							line.setStartY(lineStartY);
							line.setEndX(lineStartX + translateTransition.getByX());
							line.setEndY(lineStartY + translateTransition.getByY());
							lineStartY += 40;
							((AnchorPane)n.getParent().getParent().getParent()).getChildren().add(line);
						}
					} else {
						TranslateTransition relevantTransition = transitions.get(i).get(sliceIndex);
						relevantTransition.pause();
						if (!(startTime / animationDuration.toMillis() >= 1) && i == 1) {
							System.out.println("Went in: " + (startTime / animationDuration.toMillis()) * relevantTransition.getByX());
						}
						if (relevantTransition.getByX() != xTranslate * -1) {
							relevantTransition.setByX(xTranslate - Math.abs(Math.abs(relevantTransition.getByX()) - Math.abs((startTime / animationDuration.toMillis()) * relevantTransition.getByX())));
							relevantTransition.setByY(yTranslate - Math.abs(Math.abs(relevantTransition.getByY()) - Math.abs((startTime / animationDuration.toMillis()) * relevantTransition.getByY())));
						} else {
							System.out.println("Else");
							relevantTransition.setByX(xTranslate - Math.abs(relevantTransition.getByX() - (startTime / animationDuration.toMillis()) * relevantTransition.getByX()));
							relevantTransition.setByY(yTranslate - Math.abs(relevantTransition.getByY() - (startTime / animationDuration.toMillis()) * relevantTransition.getByY()));
						}
						relevantTransition.setCycleCount(1);
						relevantTransition.setAutoReverse(false);
						relevantTransition.setInterpolator(Interpolator.LINEAR);
						relevantTransition.playFromStart();

						if (i == 1) {
							System.out.println("Going out: (" + relevantTransition.getByX() + ", " + relevantTransition.getByY() + ")");
							Line line = new Line();
							line.setStartX(lineStartX);
							line.setStartY(lineStartY);
							line.setEndX(lineStartX + relevantTransition.getByX());
							line.setEndY(lineStartY + relevantTransition.getByY());
							lineStartY += 40;
							((AnchorPane)n.getParent().getParent().getParent()).getChildren().add(line);
						}
						
						transitions.get(i).set(sliceIndex, relevantTransition);
					}
					
					
				}

			} else if (event.getEventType().equals(MouseEvent.MOUSE_EXITED)) {
				
				for (PieChart.Data d : chart.getData()) {
					minX = Math.min(minX, d.getNode().getBoundsInParent().getMinX());
					maxX = Math.max(maxX, d.getNode().getBoundsInParent().getMaxX());
				}

				double startTime = transitions.get(0).get(sliceIndex).getCurrentTime().toMillis();
				for (int i = 0; i < 3; i++) {
					TranslateTransition relevantTransition = transitions.get(i).get(sliceIndex);
					relevantTransition.pause();
					if (!(startTime / animationDuration.toMillis() >= 1) && i == 1) {
						System.out.println("Went out: " + (startTime / animationDuration.toMillis()) * relevantTransition.getByX());
					}
					relevantTransition.setByX((xTranslate - (relevantTransition.getByX() - (startTime / animationDuration.toMillis()) * relevantTransition.getByX())) * -1);
					relevantTransition.setByY((yTranslate - (relevantTransition.getByY() - (startTime / animationDuration.toMillis()) * relevantTransition.getByY())) * -1);
					if (i == 1)
						System.out.println("Going in: (" + relevantTransition.getByX() + ", " + relevantTransition.getByY() + ")");
					relevantTransition.setAutoReverse(false);
					relevantTransition.setCycleCount(1);
					relevantTransition.setInterpolator(Interpolator.LINEAR);
					relevantTransition.playFromStart();
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
