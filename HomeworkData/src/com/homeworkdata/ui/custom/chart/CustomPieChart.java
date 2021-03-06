package com.homeworkdata.ui.custom.chart;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Text;


public class CustomPieChart extends PieChart{
	
	public CustomPieChart(ObservableList<PieChart.Data> data) {
		super(data);
	}
	
	public Path getLabelLinePath() {
		return super.labelLinePath;
	}
	
	/**
	 * Categorizes the children of the chart into nice groupings
	 * @return An ArrayList formatted as such: ArrayList{ArrayList{Region, Text, ArrayList<PathElement>}, ...}
	 */
	public ArrayList<ArrayList<Object>> parseChildren() {
		ArrayList<ArrayList<Object>> toReturn = new ArrayList<ArrayList<Object>>();
		ObservableList<Node> children = this.getChartChildren();
		
		ArrayList<ArrayList<PathElement>> categorizedElements = new ArrayList<ArrayList<PathElement>>();
		for (int i = 0; i < children.size(); i++) {
			Node child = children.get(i);
			
			if (child instanceof Text) { // This is just to make sure there are as many ArrayLists as there are slices of the pie
				toReturn.add(new ArrayList<Object>());
			}

			if (child instanceof Path) {
				categorizedElements = getCategorizedPathElements(((Path)child).getElements());
			}
		}
		
		int regionCounter = 0;
		int textCounter = 0;
		for (int i = 0; i < children.size(); i++) {
			Node child = children.get(i);
			
			if (child instanceof Region) {
				toReturn.get(regionCounter).add(child);
				regionCounter++;
			} else if (child instanceof Text) {
				toReturn.get(textCounter).add(child);
				textCounter++;
			}
		}
		
		int pathCounter = 0;
		for (ArrayList<PathElement> elementArray : categorizedElements) {
			toReturn.get(pathCounter).add(elementArray);
			pathCounter++;
		}
		
		return toReturn;
	}
	
	/**
	 * Each sub-ArrayList represents a Path
	 * @param pathElements
	 * @return
	 */
	public ArrayList<ArrayList<PathElement>> getCategorizedPathElements(ObservableList<PathElement> pathElements) {
		ArrayList<ArrayList<PathElement>> categorizedElements = new ArrayList<ArrayList<PathElement>>();
		
		categorizedElements.add(new ArrayList<PathElement>());
		int categoryCounter = 0;
		for (int k = 0; k < pathElements.size(); k++) {
			PathElement element = pathElements.get(k);
			categorizedElements.get(categoryCounter).add(element);
			
			if (element instanceof ClosePath && k != pathElements.size() - 1) {
				categorizedElements.add(new ArrayList<PathElement>());
				categoryCounter++;
			}
		}
		
		return categorizedElements;
	}
	
	@Override protected void layoutChartChildren(double top, double left, double contentWidth, double contentHeight) {
		super.layoutChartChildren(top, left, contentWidth, contentHeight);
		
		MouseEvent event1 = new MouseEvent(MouseEvent.MOUSE_ENTERED, 0, 0, 0, 0, MouseButton.NONE, 0, false, false, false, false, false, false, false, false, false, false, null);
		MouseEvent event2 = new MouseEvent(MouseEvent.MOUSE_EXITED, 0, 0, 0, 0, MouseButton.NONE, 0, false, false, false, false, false, false, false, false, false, false, null);
	}

	public ArrayList<LabelLayoutInfo> getFullPieLabels() {
		return super.fullPieLabels;
	}
	
	public ArrayList<Region> getFullPieRegions() {
		return super.fullPieRegions;
	}
	
	public ArrayList<ArrayList<Double>> getFullPiePathEndPoints() {
		return super.fullPiePathEndPoints;
	}
}
