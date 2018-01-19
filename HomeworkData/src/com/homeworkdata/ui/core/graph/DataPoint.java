package com.homeworkdata.ui.core.graph;
public class DataPoint {
	public String date;
	public int secondsSpent;
	
	DataPoint() {
		this.date = "";
		this.secondsSpent = 0;
	}
	
	public DataPoint(String date, int secondsSpent) {
		this.date = date;
		this.secondsSpent = secondsSpent;
	}
	
	public String getDate() {
		return this.date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public int getSecondsSpent() {
		return this.secondsSpent;
	}
	
	public void setSecondsSpent(int secondsSpent) {
		this.secondsSpent = secondsSpent;
	}
}
