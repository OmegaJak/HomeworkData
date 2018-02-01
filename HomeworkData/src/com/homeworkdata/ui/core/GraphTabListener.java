package com.homeworkdata.ui.core;
import java.awt.Color;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.controlsfx.control.CheckComboBox;

import com.homeworkdata.data.DataHandler;
import com.homeworkdata.data.Homework;
import com.homeworkdata.helper.enums.Columns;
import com.homeworkdata.helper.enums.Direction;
import com.homeworkdata.ui.core.graph.DataPoint;
import com.homeworkdata.ui.core.graph.NumberTimeStringConverter;
import com.homeworkdata.ui.custom.chart.CustomPieChart;
import com.homeworkdata.ui.custom.chart.PieChart;
import com.homeworkdata.ui.custom.chart.PieChart.LabelLayoutInfo;
import com.homeworkdata.ui.custom.control.ArrowButton;
import com.homeworkdata.ui.custom.control.NumberSpinner;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Font;
import javafx.util.Duration;


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
		
		graphNames = new String[] {"Total Spent Time Pie Chart", "Spent Time Line Chart", "Random Stats"};
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
						
						ObservableList<PieChart.Data> pieChartData = getPieChartData(totalTimes);
						
						final CustomPieChart chart = new CustomPieChart(pieChartData);
						chart.setTitle(graphNames[newValue.intValue()]);
						chart.scaleShapeProperty().set(true);

						addChartAnimations(chart, pieChartData);
						
						graphDisplay.getChildren().add(chart);
						
					} catch (NumberFormatException e) {
						System.err.println("There was an error parsing some numbers when generating the \"Total Spent Time Pie Chart\"");
						handler.showErrorDialogue(e);
					}
					break;
				case "Spent Time Line Chart":
					NumberSpinner groupingRangeSpinner = new NumberSpinner();
					CheckBox showBlanks = new CheckBox("Show Empty Days");
					CheckComboBox<String> classFilter = new CheckComboBox<String>();
					
					final CategoryAxis xAxis = new CategoryAxis();
					final NumberAxis yAxis = new NumberAxis();
					xAxis.setLabel("Date");
					final LineChart<String, Number> lineChart = new LineChart<String, Number>(xAxis, yAxis);
					lineChart.setTitle("Spent Time Line Chart");
					
					yAxis.setTickLabelFormatter(new NumberTimeStringConverter(handler));
					
					groupingRangeSpinner.setNumber(new BigDecimal(1));
					
					ObservableList<String> classOptions = FXCollections.observableArrayList(handler.getCellsMeetingCriteria(new int[] {Columns.CLASS}, new String[] {"Class"}, "Not", new int[] {Columns.CLASS}, false, handler.csvDir, handler.csvName).get(0));
					classFilter.getItems().addAll(classOptions);					
					String[] classFilterArr = classFilter.getCheckModel().getCheckedItems().toArray(new String[] {});
					
					XYChart.Series series = getLineChartData(showBlanks.selectedProperty().getValue(), groupingRangeSpinner.getNumber().toBigInteger().intValue(), classFilter.getCheckModel().getCheckedItems());
					
					showBlanks.selectedProperty().addListener(new ChangeListener<Boolean>() {
						public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue) {
							updateChartData(lineChart, showBlanks.selectedProperty().getValue(), groupingRangeSpinner.getNumber().toBigInteger().intValue(), classFilter.getCheckModel().getCheckedItems());
						}
					});
					
					groupingRangeSpinner.numberProperty().addListener(new ChangeListener<BigDecimal>() {
						@Override
						public void changed(ObservableValue<? extends BigDecimal> observable, BigDecimal oldValue, BigDecimal newValue) {
							updateChartData(lineChart, showBlanks.selectedProperty().getValue(), groupingRangeSpinner.getNumber().toBigInteger().intValue(), classFilter.getCheckModel().getCheckedItems());
						}
					});
					
					classFilter.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
						@Override
						public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> c) {
							updateChartData(lineChart, showBlanks.selectedProperty().getValue(), groupingRangeSpinner.getNumber().toBigInteger().intValue(), classFilter.getCheckModel().getCheckedItems());
						}
					});
					
			        lineChart.getData().add(series);
			        series = addSeriesListeners(series, classFilterArr);
			        
			        graphDisplay.getChildren().add(lineChart);
			        graphTabOptions.getChildren().add(groupingRangeSpinner);
			        graphTabOptions.getChildren().add(showBlanks);
					graphTabOptions.getChildren().add(classFilter);
			        
					break;
				case "Random Stats":
					ListView<String> statsList = new ListView<String>();
					ObservableList<String> items =FXCollections.observableArrayList();
					
					//handler.getAverageHomeworkPerWeek();
					items.add("Average Daily Homework  (ignoring zero days): " + 
							handler.convertSecondsToFormattedString(new String[] {"HH", "MM", "SS"}, handler.getAverageDailySeconds(false)));
					items.add("Average Daily Homework (including zero days): " + 
							handler.convertSecondsToFormattedString(new String[] {"HH", "MM", "SS"}, handler.getAverageDailySeconds(true)));
					items.add("Number of days no homework was done on: " + (handler.getNumDays(true) - handler.getNumDays(false))); // TODO: Make this more efficient
					items.add("Total homework done: " + handler.getTotalTime());
					statsList.setItems(items);
					
					statsList.minWidthProperty().bind(graphDisplay.widthProperty().subtract(10));
					statsList.minHeightProperty().bind(graphDisplay.heightProperty().subtract(10));
					
					graphDisplay.getChildren().add(statsList);
					break;
			}
		}
	}
	
	/*
	 * Report Generator
	 */
	
	
	
	/*
	 * 
	 */
	
	/*
	 * Spent Time Line Chart
	 */
	
	

	private void updateChartData(LineChart lineChart, boolean shouldShowBlanks, int groupingRange, ObservableList<String> classFilters) {
		lineChart.getData().clear(); // Get rid of the old data
		XYChart.Series series = getLineChartData(shouldShowBlanks, groupingRange, classFilters); // Get the new data
		lineChart.getData().add(series); // Add that new data
		addSeriesListeners(series, classFilters.toArray(new String[] {})); // Set animation & click listeners for the new chart data
	}
	
	private XYChart.Series addSeriesListeners(XYChart.Series originalSeries, String[] classFilter) {
		XYChart.Series series = originalSeries;
		
		for (int i = 0; i < series.getData().size(); i++) {
			Data<String, Number> currentData = (Data<String, Number>)series.getData().get(i);
			if (currentData.getNode() != null) {
				currentData.getNode().setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						System.out.println("My X value is: " + currentData.getXValue() + ", and my Y value is: " + currentData.getYValue() + ".");
												
						Dialog<ButtonType> dialog = new Dialog<>();
						dialog.setTitle("Point Info");
						dialog.setResizable(true);
						//dialog.setHeaderText(currentData.getXValue() + "  |  " + currentData.getYValue() + " seconds  |  " + handler.convertSecondsToFormattedString(handler.findInBetween("HH:MM", ':'), currentData.getYValue().intValue()));
						
						String[] startingEndingDays = handler.findInBetween(currentData.getXValue().replaceAll("\\s", ""), '~');
						String startingDay = startingEndingDays[0];
						String endingDay;
						if (startingEndingDays.length > 1) 
							endingDay = startingEndingDays[1]; 
						else
							endingDay = startingEndingDays[0];
						
						//----Header Contents----//
						final Font labelFont = new Font(16);
						
						HBox headerBox = new HBox();
						headerBox.setAlignment(Pos.CENTER);
						headerBox.setSpacing(13);
						
						Label date = new Label(startingDay);
						date.setFont(labelFont);
						
						Separator separatorOne = new Separator();
						separatorOne.setOrientation(Orientation.VERTICAL);
						
						int secondsSpentOnDay = handler.getSecondsSpentOnDay(startingDay, classFilter);
						Label secondsSpent = new Label(secondsSpentOnDay + " seconds");
						secondsSpent.fontProperty().bind(date.fontProperty());
						
						Separator separatorTwo = new Separator();
						separatorTwo.setOrientation(Orientation.VERTICAL);
						
						Label timeSpent = new Label(handler.convertSecondsToFormattedString(handler.findInBetween("HH:MM", ':'), secondsSpentOnDay));
						timeSpent.fontProperty().bind(date.fontProperty());

						headerBox.getChildren().addAll(date, separatorOne, secondsSpent, separatorTwo, timeSpent);
						//~~//

						AnchorPane headerAnchor = new AnchorPane();

						ArrowButton leftArrow = new ArrowButton(12);
						leftArrow.setArrowDirection(Direction.LEFT);

						ArrowButton rightArrow = new ArrowButton(12);
						rightArrow.setArrowDirection(Direction.RIGHT);

						headerAnchor.getChildren().add(leftArrow.getStackPane());
						headerAnchor.setLeftAnchor(leftArrow.getStackPane(), 10.0);

						headerAnchor.getChildren().add(rightArrow.getStackPane());
						headerAnchor.setRightAnchor(rightArrow.getStackPane(), 10.0);

						headerAnchor.getChildren().add(headerBox);
						headerAnchor.setBottomAnchor(headerBox, 0.0);
						headerAnchor.setTopAnchor(headerBox, 0.0);
						headerAnchor.setLeftAnchor(headerBox, 20.0);
						headerAnchor.setRightAnchor(headerBox, 20.0);

						dialog.getDialogPane().setHeader(headerAnchor);
						//--------//
						
						dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
						
						AnchorPane anchorPane = new AnchorPane();

						TableColumn classCol = new TableColumn("Class");
						classCol.setCellValueFactory(new PropertyValueFactory("classProp"));
						TableColumn homeworkTypeCol = new TableColumn("Homework Type");
						homeworkTypeCol.setCellValueFactory(new PropertyValueFactory("homeworkType"));
						TableColumn timeStartedCol = new TableColumn("Time Started");
						timeStartedCol.setCellValueFactory(new PropertyValueFactory("timeStarted"));
						TableColumn timeEndedCol = new TableColumn("Time Ended");
						timeEndedCol.setCellValueFactory(new PropertyValueFactory("timeEnded"));
						
						ArrayList<String[]> dataArr = handler.getCellsMeetingCriteria(new int[] {Columns.DATE}, new String[] {startingDay}, "And",
								new int[] {Columns.CLASS, Columns.HOMEWORK_TYPE, Columns.TIME_STARTED, Columns.TIME_ENDED}, true, handler.csvDir, handler.csvName);
						if (classFilter.length > 0)
							dataArr = handler.getCellsMeetingCriteria(new int[] {0}, classFilter, "Or", new int[] {0, 1, 2, 3}, true, dataArr); // Gotta keep in mind now we're working with an already-limited set
						ObservableList<String[]> relevantData = FXCollections.observableArrayList(dataArr); // Get the rows on this date, and convert it to an ObservableList
						
						ObservableList<Homework> data = FXCollections.observableArrayList();
						for (String[] row : relevantData) {
							data.add(new Homework(row));
						}

						TableView tableView = new TableView();
						tableView.setItems(data);
						tableView.getColumns().addAll(classCol, homeworkTypeCol, timeStartedCol, timeEndedCol);
						tableView.setFixedCellSize(25);
						tableView.prefHeightProperty().bind(Bindings.size(tableView.getItems()).multiply(tableView.getFixedCellSize()).add(26));
						tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
						anchorPane.getChildren().add(tableView);
						
						anchorPane.setLeftAnchor(tableView, 0.0);
						anchorPane.setRightAnchor(tableView, 0.0);
						
						leftArrow.setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								handlePopupButtons("left", date, secondsSpent, timeSpent, tableView, startingDay, endingDay);
							}
						});
						
						rightArrow.setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								handlePopupButtons("right", date, secondsSpent, timeSpent, tableView, startingDay, endingDay);
							}
						});

						dialog.getDialogPane().setContent(anchorPane);

						dialog.showAndWait();
					}
				});
				
				//Thanks to this blog http://www.jensd.de/wordpress/?p=54 for reminding me of this animation stuff
				currentData.getNode().setOnMouseEntered(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						ScaleTransition transition = new ScaleTransition(Duration.millis(50), currentData.getNode());
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
						ScaleTransition transition = new ScaleTransition(Duration.millis(50), currentData.getNode());
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
	
	private void handlePopupButtons(String type, Label date, Label secondsSpent, Label timeSpent, TableView tableView, String startingDay, String endingDay) {
		try {
			//----Updating the header----//
			DateFormat dateFormat = new SimpleDateFormat("d-MMM-yy");
			Date currentDate = dateFormat.parse(date.getText());
			Calendar currentCalendar = Calendar.getInstance();
			currentCalendar.setTime(currentDate);
			Date startingDate = dateFormat.parse(startingDay);
			Calendar startingCalendar = Calendar.getInstance();
			startingCalendar.setTime(startingDate);
			Date endingDate = dateFormat.parse(endingDay);
			Calendar endingCalendar = Calendar.getInstance();
			endingCalendar.setTime(endingDate);
			
			if (type.toLowerCase().equals("right")) {
				if (currentCalendar.get(Calendar.DAY_OF_YEAR) >= endingCalendar.get(Calendar.DAY_OF_YEAR)) return; // Keeps it from going above the range
				currentCalendar.add(Calendar.DAY_OF_YEAR, 1);// Increment by a day
			} else if (type.toLowerCase().equals("left")) {
				if (currentCalendar.get(Calendar.DAY_OF_YEAR) <= startingCalendar.get(Calendar.DAY_OF_YEAR)) return; // Keeps it from going below the range
				currentCalendar.add(Calendar.DAY_OF_YEAR, -1);// Decrement by a day
			}

			date.setText(dateFormat.format(currentCalendar.getTime()));
			int secondsSpentOnDay = handler.getSecondsSpentOnDay(date.getText(), new String[] {});
			secondsSpent.setText(secondsSpentOnDay + " seconds");
			timeSpent.setText(handler.convertSecondsToFormattedString(handler.findInBetween("HH:MM", ':'), secondsSpentOnDay));
			//--------//

			//----Updating the Data Table----//
			ObservableList<String[]> relevantData = FXCollections.observableArrayList(handler.getCellsMeetingCriteria(new int[] {Columns.DATE}, new String[] {date.getText()}, "And",
					new int[] {Columns.CLASS, Columns.HOMEWORK_TYPE, Columns.TIME_STARTED, Columns.TIME_ENDED},
					true, handler.csvDir, handler.csvName)); // Get the rows on this date, and convert it to an ObservableList

			ObservableList<Homework> data = FXCollections.observableArrayList();
			for (String[] row : relevantData) {
				data.add(new Homework(row));
			}

			tableView.setItems(data);
			tableView.prefHeightProperty().bind(Bindings.size(tableView.getItems()).multiply(tableView.getFixedCellSize()).add(26));
			tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
			//--------//
		} catch (ParseException e) {
			System.err.println("There was an error parsing the string after the left arrow was clicked.");
			handler.showErrorDialogue(e);
			e.printStackTrace();
		}
	}
	
	private XYChart.Series getLineChartData(boolean shouldShowBlanks, int groupingRange, ObservableList<String> classFilters) {
		XYChart.Series toReturn = new XYChart.Series();
		toReturn.setName("Time Spent");

		ArrayList<DataPoint> dataPoints = handler.getLineChartData(shouldShowBlanks, groupingRange, classFilters);
		for (DataPoint dataPoint : dataPoints) {
			toReturn.getData().add(new XYChart.Data(dataPoint.getDate(), dataPoint.getSecondsSpent()));
		}

		return toReturn;
	}
	
	/*
	 * 
	 */
	
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
	
	/*
	 * Pie Chart Craziness
	 */
	
	private void addChartAnimations(CustomPieChart chart, ObservableList<com.homeworkdata.ui.custom.chart.PieChart.Data> pieChartData) {
		for (PieChart.Data data : pieChartData) {
			PieChartMouseHoverAnimation hoverAnim = new PieChartMouseHoverAnimation(data, chart, pieChartData.size());
			data.getNode().setOnMouseEntered(hoverAnim);
			data.getNode().setOnMouseExited(hoverAnim);
			data.getNode().setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					System.out.println(data.getName());
					String name = data.getName().substring(0, data.getName().length() - 6);
					ArrayList<String[]> newTotalTimes = handler.getFilteredTotals(Columns.CLASS, name, Columns.HOMEWORK_TYPE, handler.csvDir, handler.csvName);
					ObservableList<PieChart.Data> newData = getPieChartData(newTotalTimes);

					chart.getChartChildren().removeIf(node -> node instanceof Path);
					
					chart.setData(newData);
					addChartAnimations(chart, newData);
				}
			});
		}
	}
	
	private ObservableList<PieChart.Data> getPieChartData(ArrayList<String[]> totalTimes) {
		int totalTime = 0;
		
		for (int i = 0; i < totalTimes.size(); i++) {
			String className = totalTimes.get(i)[0];
			// TODO: Investigate whether this is necessary
			String totalSeconds = handler.convertTime(totalTimes.get(i)[1], "HH:MM", "SS", false);
			totalTime += Integer.parseInt(totalSeconds);
			totalTimes.remove(i);
			totalTimes.add(i, new String[] {className, totalSeconds});
			System.out.println(className + ": " + totalSeconds + " seconds");
		}
		
		System.out.println("Total Time: " + totalTime + " seconds.");
		
		ObservableList<PieChart.Data> obsArr = FXCollections.observableArrayList();
		for (int i = 0; i < totalTimes.size(); i++) { 
			obsArr.add(new PieChart.Data(totalTimes.get(i)[0] + " (" + Math.round(((Double.parseDouble(totalTimes.get(i)[1])/totalTime))*100) + "%)",
					Integer.parseInt(totalTimes.get(i)[1])));
		}
		
		return obsArr;
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
		}

		@Override
		public void handle(MouseEvent event) {
			Node n = (Node)event.getSource();
			CustomPieChart pieChart = (CustomPieChart)n.getParent().getParent();
			
			double minX = Double.MAX_VALUE; // Just temporarily
			double maxX = Double.MAX_VALUE * -1;

			for (PieChart.Data d : chart.getData()) { // Loops through, finding the max of the max x's (rightmost edge) and the min of the min x's (left edge)
				minX = Math.min(minX, d.getNode().getBoundsInParent().getMinX());
				maxX = Math.max(maxX, d.getNode().getBoundsInParent().getMaxX());
			}
			
			
			ArrayList<LabelLayoutInfo> fullPieLabels = pieChart.getFullPieLabels();
			ArrayList<Region> fullPieRegions  = pieChart.getFullPieRegions();
			
			int sliceIndex = 0;
			for (int i = 0; i < fullPieRegions.size(); i++) {
				if (fullPieRegions.get(i).equals(n)) {
					sliceIndex = i;
				}
			}
			
			// Process the raw array of line components into a number of ArrayLists, each of which represents one of the label lines
			ArrayList<ArrayList<PathElement>> drawnPathElements = pieChart.getCategorizedPathElements(pieChart.getLabelLinePath().getElements());

			pieChart.getLabelLinePath().setOpacity(0.0); // Hide the old lines from view
			chart.getChartChildren().removeIf(node -> node instanceof Path);
			
			ObservableList<Node> chartChildrenList = pieChart.getChartChildren();
			if (!(chartChildrenList.get(chartChildrenList.size() - 1) instanceof Path)) { // Ensure that we don't just endlessly add Paths to the chart
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
			
			chartChildrenList = pieChart.getChartChildren();
			
			ArrayList<Path> paths = new ArrayList<Path>();
			for (int i = 1; i < chartChildrenList.size(); i++) {
				if (chartChildrenList.get(i) instanceof Path) {						
					paths.add((Path)chartChildrenList.get(i));
				}
			}
			
			int[] invisibleText = {};
			for (int i = 0; i < fullPieLabels.size(); i++) {
				if (!fullPieLabels.get(i).text.isVisible()) {
					int[] invisibleText2 = new int[invisibleText.length + 1];
					System.arraycopy(invisibleText, 0, invisibleText2, 0, invisibleText.length);
					invisibleText2[invisibleText.length] = i;
					invisibleText = invisibleText2;
				}
			}
			
			//---Actually animating now
			double diameter = maxX - minX; // Just the difference between the right edge and the left edge of the pie
			double xTranslate = (diameter * ANIMATION_DISTANCE) * cos;
			double yTranslate = (diameter * ANIMATION_DISTANCE) * sin;
			double[] startingX = {0,0,0};
			double[] startingY = {0,0,0};
			if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED)) {
				for (int i = 0; i < 3; i++) {
					TranslateTransition translateTransition = new TranslateTransition();
					
					if (i == 0) { // Animating the slice (Region)
						translateTransition = new TranslateTransition(animationDuration, n);
						startingX[0] = n.getTranslateX();
						startingY[0] = n.getTranslateY();
					} else if (i == 1) { // Animating the line
						int numMissing = 0;
						for (int k = 0; k < invisibleText.length; k++) {
							if (sliceIndex > invisibleText[k]) {
								numMissing++;
							}
						}
						
						if (fullPieLabels.get(sliceIndex).text.isVisible()) {
							Path relevantPath = paths.get(sliceIndex - numMissing);
							translateTransition = new TranslateTransition(animationDuration, relevantPath);
							startingX[1] = relevantPath.getTranslateX();
							startingY[1] = relevantPath.getTranslateY();
						}
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
						int numMissing = 0;
						for (int k = 0; k < invisibleText.length; k++) {
							if (sliceIndex > invisibleText[k]) {
								numMissing++;
							}
						}
						
						if (fullPieLabels.get(sliceIndex).text.isVisible()) {
							Path relevantPath = paths.get(sliceIndex - numMissing);
							translateTransition = new TranslateTransition(animationDuration, relevantPath);
						}
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
				
				
				//chart.getChartChildren().removeIf(node -> node instanceof Path);
			}
		}

		private static double calcAngle(PieChart.Data d) {
			double total = 0;
			for (PieChart.Data tmp : d.getChart().getData()) {
				total += tmp.getPieValue();
			}

			return 360.0 * (d.getPieValue() / total);
		}
		
		/*
		 * 
		 */
	}
}
