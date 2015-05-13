import java.util.ArrayList;

import javafx.beans.property.SimpleStringProperty;

public class Homework {
	private final SimpleStringProperty classProp;
	private final SimpleStringProperty homeworkType;
	private final SimpleStringProperty timeStarted;
	private final SimpleStringProperty timeEnded;
	
	public Homework(String[] rowData) {
		this.classProp = new SimpleStringProperty(rowData[0]);
		this.homeworkType = new SimpleStringProperty(rowData[1]);
		this.timeStarted = new SimpleStringProperty(rowData[2]);
		this.timeEnded = new SimpleStringProperty(rowData[3]);
	}
	
	public SimpleStringProperty classPropProperty() {
		return classProp;
	}
	
	public SimpleStringProperty homeworkTypeProperty() {
		return homeworkType;
	}
	
	public SimpleStringProperty timeStartedProperty() {
		return timeStarted;
	}
	
	public SimpleStringProperty timeEndedProperty() {
		return timeEnded;
	}
}
