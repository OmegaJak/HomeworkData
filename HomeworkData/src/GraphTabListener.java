import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;


public class GraphTabListener implements ChangeListener<Number> {
	
	private AnchorPane graphDisplay;
	private ChoiceBox graphPicker;
	private DataHandler handler;
	String[] graphNames;
	
	public GraphTabListener(AnchorPane graphDisplay, ChoiceBox graphPicker, DataHandler handler) {
		this.graphDisplay = graphDisplay;
		this.graphPicker = graphPicker;
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
					
					graphDisplay.getChildren().add(chart);
				} catch (NumberFormatException e) {
					System.out.println("There was an error parsing some numbers when generating the \"Spent Time Pie Chart\"");
					handler.showErrorDialogue(e);
				}
				break;
		}
		
	}
}
