import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Text;


public class CustomPieChart extends PieChart{
	
	CustomPieChart(ObservableList<PieChart.Data> data) {
		super(data);
	}
	
	public Path getLabelLinePath() {
		return super.labelLinePath;
	}
	
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
				ObservableList<PathElement> pathElements = ((Path)child).getElements();

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
		
		return toReturn;
	}
}
