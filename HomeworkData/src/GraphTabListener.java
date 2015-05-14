import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.util.Duration;
import CustomCharts.CustomPieChart;
import CustomCharts.PieChart;
import CustomCharts.PieChart.LabelLayoutInfo;


public class GraphTabListener implements ChangeListener<Number> {
	
	private AnchorPane graphDisplay;
	private DataHandler handler;
	private HBox graphTabOptions;
	private int startingNumGraphTabOptionsChildren;
	String[] graphNames;
	private ChoiceBox graphPicker;
	
	public GraphTabListener(AnchorPane graphDisplay, ChoiceBox graphPicker, HBox graphTabOptions, DataHandler handler) {
		this.graphDisplay = graphDisplay;
		this.handler = handler;
		this.graphTabOptions = graphTabOptions;
		
		startingNumGraphTabOptionsChildren = graphTabOptions.getChildren().size();
		
		graphNames = new String[] {"Total Spent Time Pie Chart", "Spent Time Line Chart"};
		ObservableList<String> graphOptions = FXCollections.observableArrayList(graphNames);
		graphPicker.setItems(graphOptions);
		
		graphPicker.getSelectionModel().selectedIndexProperty().addListener(GraphTabListener.this);
		this.graphPicker = graphPicker;
	}

	@Override
	public void changed(ObservableValue<? extends Number> obsValue, Number oldValue, Number newValue) {
		graphDisplay.getChildren().clear();
		clearGraphTabOptions();
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
					NumberSpinner groupingRangeSpinner = new NumberSpinner();
					CheckBox showBlanks = new CheckBox("Show Empty Days");
					
					final CategoryAxis xAxis = new CategoryAxis();
					final NumberAxis yAxis = new NumberAxis();
					xAxis.setLabel("Date");
					final LineChart<String, Number> lineChart = new LineChart<String, Number>(xAxis, yAxis);
					lineChart.setTitle("Spent Time Line Chart");
					
					yAxis.setTickLabelFormatter(new NumberTimeStringConverter(handler));
					
					groupingRangeSpinner.setNumber(new BigDecimal(1));
					
					XYChart.Series series = getLineChartData(showBlanks.selectedProperty().getValue(), groupingRangeSpinner.getNumber().toBigInteger().intValue());
					
					showBlanks.selectedProperty().addListener(new ChangeListener<Boolean>() {
						public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue) {
							updateChartData(lineChart, showBlanks.selectedProperty().getValue(), groupingRangeSpinner.getNumber().toBigInteger().intValue());
						}
					});
					
					groupingRangeSpinner.getIncremementButton().setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							groupingRangeSpinner.increment();
							event.consume();
							updateChartData(lineChart, showBlanks.selectedProperty().getValue(), groupingRangeSpinner.getNumber().toBigInteger().intValue());
						}
					});

					groupingRangeSpinner.getDecrementButton().setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							groupingRangeSpinner.decrement();
							event.consume();
							updateChartData(lineChart, showBlanks.selectedProperty().getValue(), groupingRangeSpinner.getNumber().toBigInteger().intValue());
						}
					});
					
			        lineChart.getData().add(series);
			        series = addSeriesListeners(series);
			        
			        graphDisplay.getChildren().add(lineChart);
			        graphTabOptions.getChildren().add(groupingRangeSpinner);
			        graphTabOptions.getChildren().add(showBlanks);
					
					break;
			}
		}
	}
	
	private void updateChartData(LineChart lineChart, boolean shouldShowBlanks, int groupingRange) {
		lineChart.getData().clear(); // Get rid of the old data
		XYChart.Series series = getLineChartData(shouldShowBlanks, groupingRange); // Get the new data
		lineChart.getData().add(series); // Add that new data
		addSeriesListeners(series); // Set animation & click listeners for the new chart data
	}
	
	private XYChart.Series addSeriesListeners(XYChart.Series originalSeries) {
		XYChart.Series series = originalSeries;
		
		for (int i = 0; i < series.getData().size(); i++) {
			Data<String, Number> currentData = (Data<String, Number>)series.getData().get(i);
			if (currentData.getNode() != null) {
				currentData.getNode().setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						System.out.println("My X value is: " + currentData.getXValue() + ", and my Y value is: " + currentData.getYValue() + ".");
						ObservableList<String[]> relevantData = FXCollections.observableArrayList(handler.getCellsMeetingCriteria(new int[] {0}, new String[] {currentData.getXValue()},
								"And", new int[] {1, 2, 6, 8}, true, handler.csvDir, handler.csvName)); // Get the rows on this date, and convert it to an ObservableList

						ObservableList<Homework> data = FXCollections.observableArrayList();
						for (String[] row : relevantData) {
							data.add(new Homework(row));
						}
						
						Dialog<ButtonType> dialog = new Dialog<>();
						dialog.setTitle("Point Info");
						dialog.setHeaderText(currentData.getXValue() + "  |  " + currentData.getYValue() + " seconds  |  " + handler.convertSecondsToFormattedString(handler.findInBetween("HH:MM", ':'), currentData.getYValue().intValue()));

						dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
						
						AnchorPane anchorPane = new AnchorPane();

						TableColumn classCol = new TableColumn("Class");
						classCol.setCellValueFactory(new PropertyValueFactory("classProp"));
						TableColumn homeworkTypeCol = new TableColumn("Homework Type");
						homeworkTypeCol.setCellValueFactory(new PropertyValueFactory("homeworkType"));
						TableColumn timeStartedCol = new TableColumn("Time Started");
						timeStartedCol.setCellValueFactory(new PropertyValueFactory("timeStarted"));
						TableColumn timeEndedCol = new TableColumn("Time Ended");
						timeEndedCol.setCellValueFactory(new PropertyValueFactory("timeEnded"));

						if (currentData.getXValue().contains("~")) {
							try {
							String[] startEndDays = handler.findInBetween(currentData.getXValue().replaceAll("\\s", ""), '~');
							System.out.println(Arrays.toString(startEndDays));
							
							//----Date Crap----//
							DateFormat dateFormat = new SimpleDateFormat("d-MMM-yy");
							Date startingDate = dateFormat.parse(startEndDays[0]);
							Calendar startingCalendar = Calendar.getInstance();
							startingCalendar.setTime(startingDate);
							Date endingDate = dateFormat.parse(startEndDays[1]);
							Calendar endingCalendar = Calendar.getInstance();
							endingCalendar.setTime(endingDate);
							int daysInBetween = endingCalendar.get(Calendar.DAY_OF_YEAR) - startingCalendar.get(Calendar.DAY_OF_YEAR);
							
							
							} catch (ParseException e) {
								handler.showErrorDialogue(e);
								e.printStackTrace();
							}
						} else { // This DataPoint is only for one day
							TableView tableView = new TableView();
							tableView.setItems(data);
							tableView.getColumns().addAll(classCol, homeworkTypeCol, timeStartedCol, timeEndedCol);
							//tableView.setPrefWidth(357);
							tableView.setFixedCellSize(25);
							tableView.prefHeightProperty().bind(Bindings.size(tableView.getItems()).multiply(tableView.getFixedCellSize()).add(26));
							tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
							anchorPane.getChildren().add(tableView);
						}
						
						dialog.getDialogPane().setContent(anchorPane);
						
						dialog.showAndWait();
					}
				});
				
				//Thanks to this blog http://www.jensd.de/wordpress/?p=54 for reminding me of this animation stuff
				currentData.getNode().setOnMouseEntered(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						ScaleTransition transition = new ScaleTransition(Duration.millis(500), currentData.getNode());
						transition.setFromX(currentData.getNode().getScaleX());
						transition.setFromX(currentData.getNode().getScaleY());
						transition.setToX(1.5);
						transition.setToY(1.5);
						transition.setInterpolator(Interpolator.EASE_BOTH);
						
						transition.play();
					}
				});
				currentData.getNode().setOnMouseExited(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						ScaleTransition transition = new ScaleTransition(Duration.millis(500), currentData.getNode());
						transition.setFromX(currentData.getNode().getScaleX());
						transition.setFromX(currentData.getNode().getScaleY());
						transition.setToX(1);
						transition.setToY(1);
						transition.setInterpolator(Interpolator.EASE_BOTH);
						
						transition.play();
					}
				});
			}
		}
		
		return series;
	}
	
	private XYChart.Series getLineChartData(boolean shouldShowBlanks, int groupingRange) {
		XYChart.Series toReturn = new XYChart.Series();
		toReturn.setName("Time Spent");

		ArrayList<DataPoint> dataPoints = handler.getLineChartData(shouldShowBlanks, groupingRange);
		for (DataPoint dataPoint : dataPoints) {
			toReturn.getData().add(new XYChart.Data(dataPoint.getDate(), dataPoint.getSecondsSpent()));
		}

		return toReturn;
	}
	
	public void unload() {
		graphDisplay.getChildren().clear();
		graphPicker.getSelectionModel().clearSelection();
		graphPicker.getSelectionModel().selectedIndexProperty().removeListener(GraphTabListener.this);
		clearGraphTabOptions();
	}
	
	public void clearGraphTabOptions() {
		int currentNumGraphTabOptionsChildren = graphTabOptions.getChildren().size();
		for (int i = startingNumGraphTabOptionsChildren; i < currentNumGraphTabOptionsChildren; i++) { // Clear out any options that were added
			graphTabOptions.getChildren().remove(startingNumGraphTabOptionsChildren);
		}
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
			double[] startingX = {0,0,0};
			double[] startingY = {0,0,0};
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
						startingX[0] = n.getTranslateX();
						startingY[0] = n.getTranslateY();
					} else if (i == 1) { // Animating the line
						Path relevantPath = paths.get(sliceIndex);
						translateTransition = new TranslateTransition(animationDuration, relevantPath);
						startingX[1] = relevantPath.getTranslateX();
						startingY[1] = relevantPath.getTranslateY();
					} else if (i == 2) { // Animating the label
						translateTransition = new TranslateTransition(animationDuration, fullPieLabels.get(sliceIndex).text);
						startingX[2] = fullPieLabels.get(sliceIndex).text.getTranslateX();
						startingY[2] = fullPieLabels.get(sliceIndex).text.getTranslateY();
					}
					
					translateTransition.setFromX(n.getTranslateX());
					translateTransition.setFromY(n.getTranslateY());
					translateTransition.setToX(n.getTranslateX() + xTranslate);
					translateTransition.setToY(n.getTranslateY() + yTranslate);
					translateTransition.setInterpolator(Interpolator.EASE_BOTH);
					translateTransition.play();
					
				}

			} else if (event.getEventType().equals(MouseEvent.MOUSE_EXITED)) {
				
				for (PieChart.Data d : chart.getData()) {
					minX = Math.min(minX, d.getNode().getBoundsInParent().getMinX());
					maxX = Math.max(maxX, d.getNode().getBoundsInParent().getMaxX());
				}

				//double startTime = transitions.get(0).get(sliceIndex).getCurrentTime().toMillis();
				for (int i = 0; i < 3; i++) {
					TranslateTransition translateTransition = new TranslateTransition();

					if (i == 0) { // Animating the slice (Region)
						translateTransition = new TranslateTransition(animationDuration, n);
					} else if (i == 1) { // Animating the line
						Path relevantPath = paths.get(sliceIndex);
						translateTransition = new TranslateTransition(animationDuration, relevantPath);
					} else if (i == 2) { // Animating the label
						translateTransition = new TranslateTransition(animationDuration, fullPieLabels.get(sliceIndex).text);
					}

					translateTransition.setFromX(n.getTranslateX());
					translateTransition.setFromY(n.getTranslateY());
					translateTransition.setToX(startingX[i]);
					translateTransition.setToY(startingY[i]);
					translateTransition.setInterpolator(Interpolator.EASE_BOTH);
					translateTransition.play();
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
