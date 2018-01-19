package com.homeworkdata.data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.homeworkdata.helper.enums.Columns;
import com.homeworkdata.ui.core.graph.DataPoint;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class DataHandler {

	private static Charset charset = Charset.forName("US-ASCII");
	public String csvDir = "";
	public String csvName = "";
	public Preferences prefs;
	public String[] prefKeys = {"csvDir", "csvName"}; // An array of the keys for all the preferences
	public String[] prefDefs = {"C:/Users/JAK/Documents/Google Drive/", "HomeworkDataSem2"}; // An array of the defaults for all the preferences, corresponding indexes to the prefKeys
	public int mostRecentYear = 0;
	private static long lastErrorTime;
	
	public DataHandler() {
		//csvDir = "/home/jak/Programming/HomeworkData/HomeworkData";
		//csvDir = "C:\\Users\\JAK\\Programming\\Other Random Java\\HomeworkData\\HomeworkData";
		//csvDir = "C:/Users/JAK/Programming/Other Random Java/HomeworkData/HomeworkData/";
		//csvDir = "C:/Users/JAK/Documents/Google Drive/";
		//csvDir = "C:\\Users\\JAK\\Documents\\Google Drive";
		//csvName = "HomeworkDataSem2.csv";
		
		initPreferences();
		refreshPreferences();
		main();
	}

	public void main() { //not the real and proper main method
		try {
			mostRecentYear = getMostRecentYear(csvDir, csvName);
			
			ArrayList<String[]> rows = readFile(csvDir, csvName, false, -1);

			boolean switchBoolean = false;
			if (switchBoolean) {
				for (int i = 0; i < rows.size(); i++) { //Loop through theArrayList 
					for (int k = 0; k < rows.get(i).length; k++) {//through the inner array, i.e., looping through the columns of the current row 
						System.out.print(rows.get(i)[k] + ",  \t\t");
					}
					System.out.println("");
				}
			}

			System.out.println(rows.size());
			
			ArrayList<String[]> dataSheet = readFile(csvDir, csvName, false, mostRecentYear);
			for (int i = 0; i < dataSheet.size(); i++) {
				if (dataSheet.get(i)[7].equals("0")) {
					writeCell(i, 7, "0:00", csvDir, csvName);
				}
			}
			
			dataSheet = readFile(csvDir, csvName, false, mostRecentYear);
			
			for (int i = 1; i < dataSheet.size(); i++) {
				if (dataSheet.get(i).equals("")) {
					timePerUnit(dataSheet, i);
				}
			}
			
			//getCellsMeetingCriteria(new int[] {1, 2, 3}, new String[] {"Physics C", "Webassign", "Points"}, "And", new int[] {1, 3});
			//getCellsMeetingCriteria(new int[] {1, 2, 3}, new String[] {"Physics C", "Webassign", "Points"}, "Or", new int[] {1, 3}, true, csvDir, csvName);
			//getCellsMeetingCriteria(new int[] {1, 2, 3}, new String[] {"Physics C", "Webassign", "Points"}, "Not", new int[] {1, 3});
			
			String[] timesToAdd = {};
			DateFormat dateFormat = new SimpleDateFormat("d-MMM-yy");
			String date = dateFormat.format(new Date());
			ArrayList<String[]> matchingCells = getCellsMeetingCriteria(new int[] {Columns.DATE}, new String[] {date}, "And", new int[] {Columns.TIME_STARTED, Columns.TIME_ENDED}, true, csvDir, csvName);
			for (int i = 0; i < matchingCells.size(); i++) {
				if (matchingCells.get(i)[0].contains(":") && matchingCells.get(i)[1].contains(":")) {
					String[] timesToAdd2 = new String[timesToAdd.length + 1];
					System.arraycopy(timesToAdd, 0, timesToAdd2, 0, timesToAdd.length);
					timesToAdd2[timesToAdd.length] = subtractTime(matchingCells.get(i)[0], matchingCells.get(i)[1]);
					timesToAdd = timesToAdd2;
				} else {
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Format issue");
					alert.setHeaderText("Something is wrong with line " + i + " of \"" + csvName + "\".");
					alert.setContentText("When checking it, I found no colon in the cells for time started and time ended.\nWould you like me to remove this line for you, or should I add blank times to it?");

					ButtonType buttonTypeOne = new ButtonType("Remove Line");
					ButtonType buttonTypeTwo = new ButtonType("Add Blank Data");
					ButtonType buttonTypeCancel = new ButtonType("I'll take care of it", ButtonData.CANCEL_CLOSE);

					alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);

					showColonError(alert, i, buttonTypeOne, buttonTypeTwo, buttonTypeCancel);
				}
			}
			System.out.println(addTimes(timesToAdd));
			
			//getClassTotalTimes(csvDir, csvName);
			
			//multiplyTime(averageTimeSpent(readFile(csvDir, csvName, false), "Euro", "Textbook Reading", "Pages"), 4);

			/*for (int i = 0; i < rows.size(); i++) {
				System.out.println(rows.get(i)[2]);
			}*/

			/*writeCell(6, 4, "Test", csvDir, "test.csv");
			
			convertTime("02:23:54", "HH:MM:SS", "MM:SS");
			
			//timePerUnit(readFile(csvDir, csvName, false), 4);
			
			insertNewRow(-1, 7, csvDir, "test.csv");
			
		} catch (IOException e) {
			System.out.println("There was an IOException somewhere. Stahp.");
			e.printStackTrace();*/
		} catch (Exception e) {
			System.out.println("Something went wrong, who the hell knows...");
			e.printStackTrace();
		}
	}

	//----------------------------------------------------------------------------------------//
	//--------------------------------File interaction methods--------------------------------//
	//----------------------------------------------------------------------------------------//

	/**
	 * Write to a cell in the csv file
	 * 
	 * @param row - The row you want to edit, starting at 0
	 * @param column - The column you want to edit, starting at 0
	 * @param fill - The string to fill the cell with
	 * @param dir - The directory of the file to be edited
	 * @param file - The name of the file to be edited
	 */
	public void writeCell(int row, int column, String fill, String dir, String file) throws IOException {
		makeBackup(dir, file);

		ArrayList<String[]> rows = readFile(dir, file, true, -1);

		PrintWriter pw = new PrintWriter(new File(dir + file));

		System.out.println("Writing \"" + fill + "\" to row " + row + ", column " + column + " of " + file + ", which used to say \"" + rows.get(row)[column] + "\"");

		for (int i = 0; i < rows.size(); i++) {
			for (int k = 0; k < rows.get(i).length; k++) {
				if (i == row && k == column) { //A specific cell, like row 4 column 3
					if (k == rows.get(i).length - 1) {// If this is the last cell in the row
						pw.write(fill);
					} else {
						pw.write(fill + ",");
					}
				} else {
					if (k == rows.get(i).length - 1) { //If this is the last cell in the row
						pw.write(rows.get(i)[k] + "\n");
					} else {
						pw.write(rows.get(i)[k] + ",");
					}
				}
			}
		}

		pw.close(); //Don't forget to close the PrintWriter
	}
	
	/**
	 * Write a given ArrayList<String[]> to a file
	 * 
	 * @param fileArray - an ArrayList<String[]> representation of the .csv file, with outer indexes being the rows and the inner indexes being the columns
	 * @param dir - The directory of the file to be edited
	 * @param file - The name of the file to be edited
	 * @param isMakingACopy - If this is true, then it will just copy the whole thing to another file, and not make any changes
	 */
	public void writeStringArray(ArrayList<String[]> fileArray, String dir, String file) throws IOException {
		makeBackup(dir, file);

		PrintWriter pw = new PrintWriter(new File(dir + file));

		for (int i = 0; i < fileArray.size(); i++) {
			for (int k = 0; k < fileArray.get(i).length; k++) {
				if (k == fileArray.get(i).length - 1) { //If this is the last cell in the row
					pw.write(fileArray.get(i)[k] + "\n");
				} else {
					pw.write(fileArray.get(i)[k] + ",");
				}
			}
		}

		pw.close(); //Don't forget to close the PrintWriter
	}
	
	/**
	 * This just makes a backup copy of the specified .csv file
	 *  
	 * @param dir - The directory of the file to be edited
	 * @param file - The name of the file to be edited
	 */
	public void makeBackup(String dir, String file) throws IOException {

		ArrayList<String[]> rows = readFile(dir, file, true, -1);

		PrintWriter pw = new PrintWriter(new FileWriter("Backup-" + file));

		for (int i = 0; i < rows.size(); i++) {
			for (int k = 0; k < rows.get(i).length; k++) {
				if (k == rows.get(i).length - 1) { //If this is the last cell in the row
					pw.write(rows.get(i)[k] + "\n");
				} else {
					pw.write(rows.get(i)[k] + ",");
				}
			}
		}

		pw.close(); //Don't forget to close the PrintWriter
	}
	
	/**
	 * Inserts a new row, basically just adds some commas where you tell it to
	 * 
	 * @param precedingRow - The row preceding where you will insert the new row 
	 * @param precedingRow = -2 - Add a new row to the end of the document
	 * @param precedingRow > -1 - Add a new row to the beginning of the document
	 * @param precedingRow >= 0 - Exactly as it sounds
	 * @param columns - The number of columns to create in between and after the commas
	 * @param dir - The directory of the file to be edited
	 * @param file - The name of the file to be edited
	 * @throws IOException - If something goes wrong reading the file
	 */
	public void insertNewRow(int precedingRow, int columns, String dir, String file) throws IOException {
		makeBackup(dir, file);
		
		ArrayList<String[]> rows = readFile(dir, file, true, -1);

		try {
			PrintWriter pw = new PrintWriter(new File(dir + file));

			System.out.println("Creating a new line after row " + precedingRow + " with " + columns + " columns in " + file + " (directory of " + dir + ")");

			if (precedingRow == -1) { //Special case, in order to add a line before everything else
				for (int comma = 1; comma <= columns - 1; comma++) {
					if (comma == columns - 1) { //If this is the last comma to add
						pw.write("," + "\n");
					} else {
						pw.write(",");
					}
				}
			}

			for (int i = 0; i < rows.size(); i++) {
				for (int k = 0; k < rows.get(i).length; k++) {
					if (i == precedingRow) { //If this is just after the preceding row
						for (int comma = 1; comma <= columns - 1; comma++) {
							if (comma == columns - 1) { //If this is the last comma to add
								pw.write("," + "\n");
							} else {
								pw.write(",");
							}
						}
						precedingRow = precedingRow - 1;
						i = i - 1;
						break;
					} else {
						if (k == rows.get(i).length - 1) { //If this is the last cell in the row
							pw.write(rows.get(i)[k] + "\n");
						} else {
							pw.write(rows.get(i)[k] + ",");
						}
					}
				}
			}

			if (precedingRow == -2) { //Special case, in order to add a line to the end of the document
				for (int comma = 1; comma <= columns - 1; comma++) {
					if (comma == columns - 1) { //If this is the last comma to add
						pw.write("," + "\n");
					} else {
						pw.write(",");
					}
				}
			}

			pw.close(); //Don't forget to close the PrintWriter
		} catch (FileNotFoundException e){
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("PrintWriter Error");
			alert.setHeaderText("File inaccessible");
			alert.setContentText("HomeworkData does not have access to the \"" + csvName + "\" file. This is most likely because it is open in another program, such as Excel.\n\n"
					+ 			"Please close other programs that may have this file open.");
			alert.showAndWait();
		}
	}

	/**
	 * @param dir - The directory of the file to be read
	 * @param file- The name of the file to be read
	 * @param allowEmptyLines - If this is false, it will only add lines that have something in the first cell
	 * @param year - The school year that the data should be taken from (0 = 2014-2015, 1 = 2015-2016). -1 means ignore years, return all data
	 * @return An Arraylist of String arrays, with each String array being a row of the csv file, each index of each array a cell
	 */
	public ArrayList<String[]> readFile(String dir, String file, boolean allowEmptyLines, int year) {
		
		try {
			Path path = FileSystems.getDefault().getPath(dir, file); //The path to the file, needed for newBufferedReader()

			BufferedReader reader = Files.newBufferedReader(path, charset);
			String line = null;

			ArrayList<String[]> rows = new ArrayList<String[]>();
			String curLine[] = {};
			int curYear = 0;
			while ((line = reader.readLine()) != null) {//Go through each line sequentially until there are no more
				curLine = line.split(",", -1); //Creates an array of each element between the commas
				boolean isEmpty = true;
				for (String item : curLine) {
					if (!item.equals(""))
						isEmpty = false;
				}
				if (allowEmptyLines || !isEmpty) {//Ignore empty rows
					if (curLine[0].equals("OTHER INFO") && curLine[1].equals("NEW SCHOOL YEAR")) // Marker of a new school year
						curYear++;
					
					if (curLine[0].equals("Date") || year == -1 || curYear == year) // If it's either the first line, or if we're in the year we want to get data for. Override if year == -1
						rows.add(curLine);
				}
			}

			reader.close();
			return rows;
		} catch (IOException e) {
			System.err.println("There was an error while reading the file!");
			e.printStackTrace();
		}
		return new ArrayList<String[]>();
	}
	
	public int getNumberOfLines(String dir, String file, boolean allowEmptyLines) {
		ArrayList<String[]> temp = readFile(dir, file, allowEmptyLines, -1);
		return temp.size();
	}
	
	
	/**
	 * Simply reads the data file, then passes along to other overload.
	 * For paramater descriptions, see other overload. 
	 */
	public ArrayList<String[]> getCellsMeetingCriteria(int[] columnsToLookIn, String[] cellValuesToMatch, String operator, int[] desiredColumns, boolean allowDuplicates, String dir, String file) {
		ArrayList<String[]> dataSheet = readFile(dir, file, false, mostRecentYear);

		return getCellsMeetingCriteria(columnsToLookIn, cellValuesToMatch, operator, desiredColumns, allowDuplicates, dataSheet);
	}
	
	/**
	 * @param columnsToLookIn - Which columns the cellValuesToMatch will be looked for in. Ex: new int[] {2, 5}
	 * @param cellValuesToMatch - Tests whether the currently examined cell matches this value, in the specified column, with corresponding indexes, in
	 *            columnsToLookIn. Ex: new String[] {"Euro", "Pages"}
	 * @param operator - An option for whether they're all required ("And"), if just one is needed ("Or"), or if it should grab all that aren't it ("Not")
	 * @param desiredColumns - The column of the result cells. Ex: new int[] {2, 6, 8}.   
	 * 							Note: If this is only one column, then ArrayList will just contain one String[], which will contain that column.
	 * @param allowDuplicates - Whether or not duplicate values are allowed to be returned. Ex: true: {5, 5, 2, 3, 5}, false: {5, 2, 3}
	 * @param dataSheet - The data to process
	 * @return All of the cells that meet the specified criteria in an Arraylist of String arrays from the desired column
	 */
	// TODO: Add option to ignore first line. Or just make it default. Probably that.
	public ArrayList<String[]> getCellsMeetingCriteria(int[] columnsToLookIn, String[] cellValuesToMatch, String operator, int[] desiredColumns, boolean allowDuplicates, ArrayList<String[]> dataSheet) {
		ArrayList<String[]> toReturn = new ArrayList<String[]>();
		boolean success;
		if (operator.equals("Or"))
			success = false;
		else
			success = true;
		String[] arrayListIndex = {};
		for (int i = 0; i < dataSheet.size(); i++) { // Loops 'top to bottom' through the whole data sheet
			if (!dataSheet.get(i)[0].equals("OTHER INFO")) {
				for (int k = 0; k < columnsToLookIn.length; k++) { // Loops 'left to right' through the current row
					if (operator.equals("And")) {
						if (k >= cellValuesToMatch.length) {
							System.out.println("Trying to compare too many values to columns inside getCellsMeetingCriteria with the \"And\" operator.");
							break;
						}
						
						if (!dataSheet.get(i)[columnsToLookIn[k]].equals(cellValuesToMatch[k])) {
							success = false;
						}
					} else if (operator.equals("Or") || operator.equals("Not")) {
						for (String cellValue : cellValuesToMatch) {
							boolean equality = dataSheet.get(i)[columnsToLookIn[k]].equals(cellValue);

							switch (operator) {
								case "Or":
									if (equality)
										success = true;
									break;
								case "Not":
									if (equality)
										success = false;
									break;
							}
						}
					}
				}
				if (success) {
					if (allowDuplicates || (desiredColumns.length > 1 ? !alreadyAdded(toReturn, arrayListIndex) : !alreadyAdded(arrayListIndex, dataSheet.get(i)[desiredColumns[0]]))) {
						for (int b : desiredColumns) {
							String[] arrayListIndex2 = new String[arrayListIndex.length + 1];
							System.arraycopy(arrayListIndex, 0, arrayListIndex2, 0, arrayListIndex.length);
							arrayListIndex2[arrayListIndex.length] = dataSheet.get(i)[b];
							arrayListIndex = arrayListIndex2;
						}
						if (desiredColumns.length > 1) {
							toReturn.add(arrayListIndex);
							arrayListIndex = new String[] {};
						}
					}

				}
				if (operator.equals("Or"))
					success = false;
				else
					success = true;
			}
		}

		if (desiredColumns.length == 1 && (allowDuplicates || !alreadyAdded(toReturn, arrayListIndex))) {
			toReturn.add(arrayListIndex);
		}

		return toReturn;
	}
	
	public void printLastLine() {
		printLine(-1);
	}
	
	public void printLine(int lineIndex) { // Pass in -1 if you want to print the last line
		ArrayList<String[]> dataSheet = readFile(csvDir, csvName, true, mostRecentYear);
		int index = lineIndex;
		if (lineIndex == -1)
			index = dataSheet.size() - 1;
		
		System.out.println(Arrays.toString(dataSheet.get(index)));
	}

	//-------------------------------------------------------------------------------------//
	//--------------------------------Data analysis methods--------------------------------//
	//-------------------------------------------------------------------------------------//

	public ArrayList<String[]> timePerUnit(ArrayList<String[]> dataSheet, int row) throws IOException { // This is a different methodology than checkForTimePerUnit in EventHandlerController, but it produces the same result. Might as well leave this in.
		try {
			double calculatedResult = Double.parseDouble(convertTime(subtractTime(dataSheet.get(row)[6], dataSheet.get(row)[8]), "H:MM", "SS", true)) / Double.parseDouble(dataSheet.get(row)[4]);
			System.out.println("The calculated result was " + calculatedResult);
			String formattedResult = convertTime(addZeroes("" + (int)(calculatedResult + 0.5), 2), "SS", "M:SS", true); // The "+ 0.5" is for rounding to the nearest integer, rather than just rounding down
			writeCell(row, 5, formattedResult, csvDir, csvName);
		} catch (NumberFormatException e) {
			System.out.println("What are numbers!?");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Determines the average time that was spent
	 * @param className - Name of the class for the homework you want to know about
	 * @param type - Type of the homework
	 * @param unit - The unit of homework
	 * @return Average time in the format of "HH:MM:SS"
	 */
	public String averageTimeSpent(String className, String type, String unit) {
		ArrayList<String[]> timePerUnits = getCellsMeetingCriteria(new int[] {Columns.CLASS,  Columns.HOMEWORK_TYPE,  Columns.UNIT}, new String[] {className, type, unit}, "And", new int[] {Columns.NUM_UNIT, Columns.TIME_STARTED, Columns.TIME_WASTED, Columns.TIME_ENDED}, true, csvDir, csvName); // Get numUnits, startTime, wasted, endTime for each
		
		String totalTime = "00:00:00", timeSpent, timeMinusWasted;
		int totalUnits = 0;
		for (String[] arr : timePerUnits) { // Go through each time that homework was done
			totalUnits += Integer.parseInt(arr[0]);
			
			timeSpent = subtractTime(arr[1], arr[3]);
			timeMinusWasted = subtractTime(arr[2], timeSpent);
			totalTime = addTimes(new String[] {convertTime(totalTime, "HH:MM:SS", "MM:SS", false), convertTime(timeMinusWasted, "HH:MM", "MM:SS", false)});
		}
		
		String result = divideTime(totalTime, "HH:MM:SS", totalUnits);
		
		return result;
	}
	
	/**\
	 * Get the total amount of time spent on each class
	 * @param csvDir - The directory of the properly formatted csv to analyze
	 * @param csvName - The name of the properly formatted csv to analyze
	 * @return An ArrayList of String arrays for each class, in the format of String{"Class", "Total Time (format of HH:MM)"}
	 */
	public ArrayList<String[]> getClassTotalTimes(String csvDir, String csvName) {
		String[] classNames = getCellsMeetingCriteria(new int[] {Columns.CLASS}, new String[] {"Class"}, "Not", new int[] {Columns.CLASS}, false, csvDir, csvName).get(0);
		
		ArrayList<ArrayList<String[]>> superList = new ArrayList<ArrayList<String[]>>();
		//Looks something like this:
		//ArrayList{ArrayList{String{"Euro"}, String{"1:23", "2:34"}}, ArrayList{String{"Lit"}, String{"5:34", 6:21"}}}
		
		ArrayList<String[]> relevantColumns = new ArrayList<String[]>();
		for (String className : classNames) {
			relevantColumns = getCellsMeetingCriteria(new int[] {Columns.CLASS}, new String[] {className}, "Or", new int[] {Columns.TIME_STARTED, Columns.TIME_ENDED}, true, csvDir, csvName);
			relevantColumns.add(0, new String[] {className});
			superList.add(relevantColumns);
		}
		
		ArrayList<String[]> toReturn = new ArrayList<String[]>();
		for (int i = 0; i < superList.size(); i++) { // Which class we're dealing with
			String[] subtractedClass = new String[superList.get(i).size() - 1]; // Will end up being something like: {"Euro", "2:45", "1:23"}
			for (int k = 1; k < superList.get(i).size(); k++) { // The String array in the class that we're dealing with
				subtractedClass[k - 1] = subtractTime(superList.get(i).get(k)[0], superList.get(i).get(k)[1]);
			}
			toReturn.add(subtractedClass);
		}
		
		for (int i = 0; i < toReturn.size(); i++) {
			String tempTotal = addTimes(toReturn.get(i));
			toReturn.remove(i);
			toReturn.add(i, new String[] {classNames[i], convertTime(tempTotal, "HH:MM:SS", "MM:SS", false)});
		}

		return toReturn;
	}
	
	public ArrayList<String[]> getFilteredTotals(int filterColumn, String filterValue, int itemColumn, String csvDir, String csvName) {
		String[] classNames = getCellsMeetingCriteria(new int[] {filterColumn}, new String[] {filterValue}, "Or", new int[] {itemColumn}, false, csvDir, csvName).get(0);
		
		ArrayList<ArrayList<String[]>> superList = new ArrayList<ArrayList<String[]>>();
		//Looks something like this:
		//ArrayList{ArrayList{String{"Euro"}, String{"1:23", "2:34"}}, ArrayList{String{"Lit"}, String{"5:34", 6:21"}}}
		
		ArrayList<String[]> relevantColumns = new ArrayList<String[]>();
		for (String className : classNames) {
			relevantColumns = getCellsMeetingCriteria(new int[] {filterColumn, itemColumn}, new String[] {filterValue, className}, "Or", new int[] {Columns.TIME_STARTED, Columns.TIME_ENDED}, true, csvDir, csvName);
			relevantColumns.add(0, new String[] {className});
			superList.add(relevantColumns);
		}
		
		ArrayList<String[]> toReturn = new ArrayList<String[]>();
		for (int i = 0; i < superList.size(); i++) { // Which class we're dealing with
			String[] subtractedClass = new String[superList.get(i).size() - 1]; // Will end up being something like: {"Euro", "2:45", "1:23"}
			for (int k = 1; k < superList.get(i).size(); k++) { // The String array in the class that we're dealing with
				subtractedClass[k - 1] = subtractTime(superList.get(i).get(k)[0], superList.get(i).get(k)[1]);
			}
			toReturn.add(subtractedClass);
		}
		
		for (int i = 0; i < toReturn.size(); i++) {
			String tempTotal = addTimes(toReturn.get(i));
			toReturn.remove(i);
			toReturn.add(i, new String[] {classNames[i], tempTotal.substring(2)});
		}

		return toReturn;
	}
	
	/**
	 * Gets the properly formatted and relevant data for the "Spent Time Line Chart"
	 * @param timeUnit - Either day, week, or month. Controls how much time is lumped together in the data points.
	 * @param shouldShowBlanks - Whether or not to include the days where no homework was done.
	 * @param classFilters 
	 * @return An ArrayList consisting of the relevant DataPoints, each with the String of the date and an int of the seconds spent.
	 */
	@SuppressWarnings("static-access")
	public ArrayList<DataPoint> getLineChartData(boolean shouldShowBlanks, int groupingRange, ObservableList<String> classFilters) {
		ArrayList<DataPoint> toReturn = new ArrayList<DataPoint>();
		ArrayList<String> dates = new ArrayList<String>();
		ArrayList<String[]> startStopTimes = new ArrayList<String[]>();
		if (classFilters.size() > 0) {
			dates = new ArrayList<String>(Arrays.asList(getCellsMeetingCriteria(new int[] {Columns.CLASS}, classFilters.toArray(new String[0]), "Or", new int[] {Columns.DATE}, true,
					this.csvDir, this.csvName).get(0)));
			startStopTimes = getCellsMeetingCriteria(new int[] {Columns.CLASS}, classFilters.toArray(new String[0]), "Or", new int[] {Columns.TIME_STARTED, Columns.TIME_ENDED, Columns.TIME_WASTED}, true, this.csvDir, this.csvName);
		} else {
			dates = new ArrayList<String>(Arrays.asList(getCellsMeetingCriteria(new int[] {Columns.CLASS, Columns.TIME_STARTED, Columns.TIME_WASTED}, new String[] {"Date", "Time Started", "Time Ended"}, "Not", new int[] {Columns.DATE}, true,
					this.csvDir, this.csvName).get(0)));
			startStopTimes = getCellsMeetingCriteria(new int[] {Columns.DATE, Columns.TIME_STARTED, Columns.TIME_ENDED, Columns.TIME_WASTED}, new String[] {"Date", "Time Started", "Time Ended", "Time Wasted"}, "Not", new int[] {Columns.TIME_STARTED, Columns.TIME_ENDED, Columns.TIME_WASTED}, true, this.csvDir, this.csvName);
		}
		// This gets the first column, which is in the form of ArrayList[String{"asdf", "wqerqwer"}], and converts it to ArrayList["asdf", "wqerqwer"]

		ArrayList<Integer> secondsSpents = convertTimesToSeconds(convertToSpentTime(startStopTimes), "HH:MM");

		if (dates.size() != secondsSpents.size()) { // Just as a double check. Most likely not at all worth the trouble.
			System.out.println("The sizes didn't match up when getting the LineChartData. This will cause issues.");

			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("LineChartData Error");
			alert.setHeaderText("Size Mismatch");
			alert.setContentText("The sizes didn't match up when getting the LineChartData. This will cause issues.");
			alert.showAndWait();
		}

		try {
			DateFormat dateFormat = new SimpleDateFormat("d-MMM-yy");
			Date lastDate = dateFormat.parse(dates.get(0));
			Date currentDate;
			Calendar lastCalendar = Calendar.getInstance();
			lastCalendar.setTime(dateFormat.parse(dates.get(1)));
			Calendar currentCalendar = Calendar.getInstance();
			for (int i = 1; i < dates.size(); i++) { // Combining things that should be combined.
				currentDate = dateFormat.parse(dates.get(i));
				currentCalendar.setTime(currentDate);
				
				if (lastCalendar.get(Calendar.DAY_OF_YEAR) + groupingRange > currentCalendar.get(Calendar.DAY_OF_YEAR)) {
					int tempSeconds = secondsSpents.get(i) + secondsSpents.get(i - 1);
					dates.remove(i);
					secondsSpents.set(i - 1, tempSeconds);
					secondsSpents.remove(i);
					i--;
				}
				
				lastDate = dateFormat.parse(dates.get(i));
				lastCalendar.setTime(lastDate);
			}

			dateFormat = new SimpleDateFormat("d-MMM-yy");
			dateFormat.parse(dates.get(0));
			lastCalendar = Calendar.getInstance();
			lastCalendar.setTime(dateFormat.parse(dates.get(1)));
			currentCalendar = Calendar.getInstance();
			DataPoint currentPoint;
			for (int i = 0; i < dates.size(); i++) { // Convert the separate dates and secondsSpent values into DataPoints for the graph
				currentDate = dateFormat.parse(dates.get(i));
				currentCalendar.setTime(currentDate);

				if (shouldShowBlanks && currentCalendar.get(Calendar.DAY_OF_YEAR) != lastCalendar.get(Calendar.DAY_OF_YEAR) + groupingRange) { // If we skipped a day
					Calendar workingCalendar = Calendar.getInstance();
					workingCalendar.setTime(currentDate);
					for (int k = lastCalendar.get(lastCalendar.DAY_OF_YEAR) + 1; k < currentCalendar.get(Calendar.DAY_OF_YEAR); k++) { // Fill in all missed days
						workingCalendar.set(Calendar.DAY_OF_YEAR, k);
						currentPoint = new DataPoint(dateFormat.format(workingCalendar.getTime()), 0);
						toReturn.add(currentPoint);
					}
					currentPoint = new DataPoint(dates.get(i), secondsSpents.get(i));
					toReturn.add(currentPoint);
				} else {
					Calendar throughCalendar = currentCalendar;
					if (groupingRange > 1) {
						throughCalendar.add(Calendar.DATE, groupingRange);
						currentPoint = new DataPoint(dates.get(i) + " ~ " + dateFormat.format(throughCalendar.getTime()), secondsSpents.get(i));
					} else {
						currentPoint = new DataPoint(dates.get(i), secondsSpents.get(i));
					}
					toReturn.add(currentPoint);
				}

				lastDate = dateFormat.parse(dates.get(i));
				lastCalendar.setTime(lastDate);
			}
			
			dateFormat = new SimpleDateFormat("d-MMM-yy");
			dateFormat.parse(dates.get(0));
			lastCalendar = Calendar.getInstance();
			lastCalendar.setTime(dateFormat.parse(dates.get(1)));
			currentCalendar = Calendar.getInstance();
			for (int i = 0; i < toReturn.size(); i++) {
				currentDate = dateFormat.parse(toReturn.get(i).getDate());
				currentCalendar.setTime(currentDate);
				
				if (toReturn.get(i).getSecondsSpent() == 0) { // If it's an empty/filler DataPoint
					//TODO: Improve this methodology. It kinda works, but not as intended. Just observe.
					if (currentCalendar.get(Calendar.DAY_OF_YEAR) < lastCalendar.get(Calendar.DAY_OF_YEAR) + groupingRange) {// If it's within the grouping range of the last real DataPoint
						toReturn.remove(i);
						i--;
					}
				}
				
				lastDate = dateFormat.parse(toReturn.get(i).getDate());
				lastCalendar.setTime(lastDate);
			}
		} catch (ParseException e) {
			System.out.println("There was an issue parsing the date while getting the line chart data");
			showErrorDialogue(e);
		} catch (IndexOutOfBoundsException e) {
			// The weird call in this gives the current line number. This may have to be updated with future Java versions
			System.err.println("There was an error getting line chart data. Most likely because there isn't any. (DataHandler.java:" + Thread.currentThread().getStackTrace()[1].getLineNumber() + ")");
		}
		return toReturn;
	}
	
	public int getSecondsSpentOnDay(String date, String[] classFilter) {
		ArrayList<String[]> startStopTimesWithClass = getCellsMeetingCriteria(new int[] {Columns.DATE}, new String[] {date}, "And", new int[] {Columns.CLASS, Columns.TIME_STARTED, Columns.TIME_ENDED, Columns.TIME_WASTED}, true, this.csvDir, this.csvName);
		
		ArrayList<String[]> startStopTimes;
		if (classFilter.length > 0)
			startStopTimes = getCellsMeetingCriteria(new int[] {Columns.DATE}, classFilter, "Or", new int[] {1, 2, 3}, true, startStopTimesWithClass);
		else
			startStopTimes = getCellsMeetingCriteria(new int[] {}, new String[] {}, "Not", new int[] {1, 2, 3}, true, startStopTimesWithClass);
				
		ArrayList<Integer> secondsSpents = convertTimesToSeconds(convertToSpentTime(startStopTimes), "HH:MM");
		
		int totalSeconds = 0;
		for (Integer integer : secondsSpents) {
			totalSeconds += integer.intValue();
		}
		
		return totalSeconds;
	}
	
	public String getAverageHomeworkPerWeek() {
		//ArrayList<String[]> data = readFile(this.csvDir, this.csvName, false, this.mostRecentYear);
		ArrayList<String[]> data = getCellsMeetingCriteria(new int[] {0}, new String[] {"Date"}, "Not", new int[] {0, 6, 7, 8}, true, this.csvDir, this.csvName);
		//String[] classNames = getCellsMeetingCriteria(new int[] {1}, new String[] {"Class"}, "Not", new int[] {1}, false, csvDir, csvName).get(0);
		
		return "";
	}
	
	public int getAverageDailySeconds(boolean includeZeroDays) { // TODO: Add option for including zero days, currently does not include them
		ArrayList<String[]> startStopTimes = getCellsMeetingCriteria(new int[] {0}, new String[] {"Date"}, "Not", new int[] {Columns.TIME_STARTED, Columns.TIME_ENDED, Columns.TIME_WASTED}, true, this.csvDir, this.csvName);
		
		ArrayList<Integer> secondsSpents = convertTimesToSeconds(convertToSpentTime(startStopTimes), "HH:MM");
		
		int totalSeconds = 0;
		for (Integer integer : secondsSpents) {
			totalSeconds += integer.intValue();
		}
		
		int numDays = getNumDays(includeZeroDays);
		
		double dailySeconds = ((double)totalSeconds) / numDays;
		
		return (int)Math.round(dailySeconds); // Safe cast as the double is just a division of two ints and therefore fits in int
	}
	
	public int getNumDays(boolean includeZeroDays) {
		ArrayList<String[]> days = getCellsMeetingCriteria(new int[] {0}, new String[] {"Date"}, "Not", new int[] {Columns.DATE}, true, this.csvDir, this.csvName);
		
		int numDays = 0;
		String lastDay = "";
		for (String day : days.get(0)) {
			if (!day.equals(lastDay)) {
				if (includeZeroDays) {
					if (!lastDay.equals("")) {
						LocalDate lastDate = LocalDate.parse(lastDay, DateTimeFormatter.ofPattern("d-MMM-yy"));
						LocalDate currentDate = LocalDate.parse(day, DateTimeFormatter.ofPattern("d-MMM-yy"));
						numDays += currentDate.getLong(ChronoField.EPOCH_DAY) - lastDate.getLong(ChronoField.EPOCH_DAY);
					} else {
						numDays++;
					}
				} else {
					numDays++;
				}
				
				lastDay = day;
			}
		}
		
		return numDays;
	}
	
	//------------------------------------------------------------------------------------//
	//--------------------------------Other Helper Methods--------------------------------//
	//------------------------------------------------------------------------------------//
	
	public int getMostRecentYear(String csvDir, String csvName) {
		ArrayList<String[]> data = readFile(csvDir, csvName, false, -1);
		int yearNum = 0;
		
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i)[0].equals("OTHER INFO") && data.get(i)[1].equals("NEW SCHOOL YEAR"))
				yearNum++;
		}
		
		
		return yearNum;
	}
	
	/**
	 * Goes through given array and checks if the given string is already in the given array
	 * @param arr - The array to check
	 * @param toCheck - The string to check
	 * @return Whether or not it's already been added
	 */
	public boolean alreadyAdded(String[] arr, String toCheck) {
		if (arr.length == 0) {
			return false;
		}
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equals(toCheck)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Goes through given ArrayList and checks if the exact given String array is already in the given array 
	 * @param arr - The array to check
	 * @param toCheck - The string to check
	 * @return Whether or not it's already been added
	 */
	public boolean alreadyAdded(ArrayList<String[]> arr, String[] toCheck) {
		if (arr.size() == 0) {
			return false;
		}
		for (int i = 0; i < arr.size(); i++) {
			if (arr.get(i).equals(toCheck)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param input Needs to be in the format provided
	 * @param inputFormat Something like HH:MM:SS
	 * @param outputFormat Same as above, like HH:MM:SS
	 * @param verbose Whether or not to output the conversion to the console
	 * @return The total minutes in the time provided
	 */
	public String convertTime(String input, String inputFormat, String outputFormat, boolean verbose) {

		String[] inputFormatInBetweens = findInBetween(inputFormat, ':'); //This is an array something like... {"HH","MM","SS"}
		String[] inputInBetweens = findInBetween(input, ':'); //This is an array something like... {"05","32","50"}
		int[] numberInputsInBetween = convertStringsToInts(inputInBetweens); // Just converting inputInBetweens to integers, so now it's {5,32,50}
		String[] outputFormatInBetweens = findInBetween(outputFormat, ':'); //Same as inputFormatInBetweens, but for the output

		String outputString = "";
		if (!outputFormat.contains("H") && !outputFormat.contains("M")) { // Check whether this helps at some point.... it might...
			outputString = "" + convertTimeToSeconds(inputFormatInBetweens, numberInputsInBetween);
		} else {
			int totalSeconds = convertTimeToSeconds(inputFormatInBetweens, numberInputsInBetween);
			outputString = convertSecondsToFormattedString(outputFormatInBetweens, totalSeconds);
		}

		if (verbose)
			System.out.println("Converting \"" + input + "\" with format \"" + inputFormat + "\", outputting with format \"" + outputFormat + "\". The result is: " + outputString);

		return outputString;
	}
	
	/**
	 * Adds together the times provided
	 * @param times - An array of times, in the format of MM:SS
	 * @return The total sum of all times given, format of H:MM:SS
	 */
	public String addTimes(String[] times) {
		ArrayList<int[]> timeParts = new ArrayList<int[]>();
		for (int i = 0; i < times.length; i++) {
			timeParts.add(convertStringsToInts(findInBetween(times[i], ':')));
		}
		
		int hours = 0;
		
		int minutesSum = 0;
		for (int i = 0; i < timeParts.size(); i++) {
			minutesSum += timeParts.get(i)[0];
		}
		while (minutesSum > 59) {
			hours++;
			minutesSum -= 60;
		}
		
		int secondsSum = 0;
		for (int i = 0; i < timeParts.size(); i++) {
			secondsSum += timeParts.get(i)[1];
		}
		while (secondsSum > 59) {
			minutesSum++;
			secondsSum -= 60;
		}

		String hoursReturn = "" + hours;
		String minutesReturn = addZeroes("" + minutesSum, 2);
		String secondsReturn = addZeroes("" + secondsSum, 2);

		return hoursReturn + ":" + minutesReturn + ":" + secondsReturn;
	}
	
	/**
	 * The times must be in the format of HH:MM, military time
	 * 
	 * @param earlierTime - The time that comes before
	 * @param laterTime - The time that comes after
	 * @return - The amount of time between laterTime and earlierTime
	 */
	// TODO Add seconds capability
	public String subtractTime(String earlierTime, String laterTime) {
		try {
			int[] earlierParts = convertStringsToInts(findInBetween(earlierTime, ':'));
			int[] laterParts = convertStringsToInts(findInBetween(laterTime, ':'));
			
			int hoursDiff = laterParts[0] - earlierParts[0];
			if (hoursDiff < 0) {
				laterParts[0] += 24;
				hoursDiff = laterParts[0] - earlierParts[0];
			}

			int minutesDiff = laterParts[1] - earlierParts[1];
			if (minutesDiff < 0) {
				minutesDiff = 60 - earlierParts[1] + laterParts[1];
				if (minutesDiff > 60) {
					hoursDiff++;
					minutesDiff -= 60;
				} else {
					hoursDiff--;
				}
			}
			
			String hoursReturn = "" + hoursDiff;
			String minutesReturn = addZeroes("" + minutesDiff, 2); // Just for formatting
			
			return hoursReturn + ":" + minutesReturn;
		} catch (ArrayIndexOutOfBoundsException e) {
			showErrorDialogue(e);
		}
		return "Error";
	}
	
	/**
	 * Multiplies the time by a given number... who would've guessed that...
	 * 
	 * @param time - The time to multiply, in the format of H:MM:SS
	 * @param num - The number to multiply it by
	 * @return - The multiplied result in format of H:MM:SS
	 */
	public String multiplyTime(String time, int num) {
		int[] parts = convertStringsToInts(findInBetween(time, ':'));
		
		int hours = parts[0] * num;
		
		int minutesProduct = 0;
		
		int secondsProduct = parts[2] * num;
		while (secondsProduct > 59) {
			minutesProduct++;
			secondsProduct -= 60;
		}
		
		minutesProduct += parts[1] * num;
		while (minutesProduct > 59) {
			hours++;
			minutesProduct -= 60;
		}
		
		String hoursReturn = "" + hours;
		String minutesReturn = addZeroes("" + minutesProduct, 2);
		String secondsReturn = addZeroes("" + secondsProduct, 2);

		return hoursReturn + ":" + minutesReturn + ":" + secondsReturn;
	}
	
	/**
	 * Divides given time by a number
	 * @param time - The amount of time
	 * @param inputFormat - The format of that time
	 * @param num - The number to divide the time by
	 * @return The resultant time, rounded to the nearest second, in the format of HH:MM:SS
	 */
	public String divideTime(String time, String inputFormat, double num) {
		String[] formatInBetweens = findInBetween(inputFormat, ':');
		int totalSeconds = convertTimeToSeconds(formatInBetweens, convertStringsToInts(findInBetween(time, ':')));
		double dividedSeconds = totalSeconds / (double)num;
		
		return convertSecondsToFormattedString(new String[] {"HH", "MM", "SS"}, (int)Math.round(dividedSeconds));
	}
	
	/**
	 * Rounds a number
	 * 
	 * @param num - The double to round
	 * @param decimalPlaces - The number of decimal places to round the number to
	 * @return
	 */
	public double round(double num, int decimalPlaces) {
		return ((int)(num * (int)Math.pow(10, decimalPlaces)) / (int)Math.pow(10, decimalPlaces));
	}
	
	/**
	 * Puts zero(es) in front of a string
	 * 
	 * @param input - The input string
	 * @param preferredLength - How long you want the string to be in the end
	 * @return
	 */
	public String addZeroes(String input, int preferredLength) {
		while (input.length() < preferredLength) {
			input = "0" + input;
		}
		
		return input;
	}
	
	public int[] convertStringsToInts(String[] toConvert) throws NumberFormatException {
		try {
			int[] convertedTo = new int[toConvert.length];
			for (int i = 0; i < toConvert.length; i++) {
				convertedTo[i] = Integer.parseInt(toConvert[i]);
			}
			return convertedTo;
		} catch (NumberFormatException e) {
			System.err.println("There was an error while converting a string to ints");
			showErrorDialogue(e);
			
			throw e;
		}
	}
	
	/**
	 * Converts the given ArrayList of times to a corresponding ArrayList of those times in seconds
	 * @param spentTimes - ArrayList of the times spent, in the format of ArrayList["timeString", "anotherTimeString"]
	 * @param timeFormat - The format of the times in spentTimes
	 * @return An ArrayList of the spentTimes converted to seconds. In the format of ArrayList[seconds, anotherSeconds]
	 */
	public ArrayList<Integer> convertTimesToSeconds(ArrayList<String> spentTimes, String timeFormat) {
		ArrayList<Integer> secondsSpent = new ArrayList<Integer>();
		for (String spentTime : spentTimes) {
			secondsSpent.add(convertTimeToSeconds(findInBetween(timeFormat, ':'), convertStringsToInts(findInBetween(spentTime, ':'))));
		}
		return secondsSpent;
	}
	
	/**
	 * Converts an ArrayList of the starting, stopping, and wasted times to a combined ArrayList of the individual time spent strings
	 * @param startStopTimes - ArrayList representing the starting and stopping times. In the format of ArrayList[String{"HH:MM", "HH:MM", "HH:MM"}, String{"HH:MM", "HH:MM", "HH:MM"}].
	 * @return Arraylist in the form of ArrayList["HH:MM", "HH:MM"], with each string being the time spent
	 */
	public ArrayList<String> convertToSpentTime(ArrayList<String[]> startStopTimes) {
		ArrayList<String> spentTimes = new ArrayList<String>();
		String totalTime;
		for (String[] startStopTime : startStopTimes) { // Creates an arrayList of the time spent based off of the start and stop times.
			totalTime = subtractTime(startStopTime[0], startStopTime[1]); // The time difference between start and stop
			String[] totalTimeInBetweens = findInBetween(totalTime, ':'); // Gets the numbers for hours and minutes seperately in an array (still strings)
			String[] wastedInBetweens = findInBetween(startStopTime[2], ':');
			System.out.println(Arrays.toString(wastedInBetweens));
			int totalTimeSeconds = convertTimeToSeconds(new String[] {"HH", "MM"}, new int[] {Integer.parseInt(totalTimeInBetweens[0]), Integer.parseInt(totalTimeInBetweens[1])});
			int wastedTimeSeconds = convertTimeToSeconds(new String[] {"HH", "MM"}, new int[] {Integer.parseInt(wastedInBetweens[0]), Integer.parseInt(wastedInBetweens[1])});
			spentTimes.add(convertSecondsToFormattedString(new String[] {"HH", "MM"}, totalTimeSeconds - wastedTimeSeconds));
		}
		return spentTimes;
	}
	
	// TODO Convert this to return a double
	public static int convertTimeToSeconds(String[] inputFormatInBetweens, int[] inputInBetweens) {
		int totalSeconds = 0;
		for (int i = 0; i < inputFormatInBetweens.length; i++) {
			if (inputFormatInBetweens[i].contains("H")) {
				totalSeconds += inputInBetweens[i] * 3600;
			} else if (inputFormatInBetweens[i].contains("M")) {
				totalSeconds += inputInBetweens[i] * 60;
			} else if (inputFormatInBetweens[i].contains("S")) {
				totalSeconds += inputInBetweens[i];
			}
		}
		
		return totalSeconds;
	}
	
	public String convertSecondsToFormattedString(String[] outputFormatInBetweens, int totalSeconds) {
		String[] output = new String[outputFormatInBetweens.length];
		int hours = -1, minutes = -1, seconds = -1, indexes = 0;
		for (int i = 0; i < outputFormatInBetweens.length; i++) {
			if (outputFormatInBetweens[i].toUpperCase().contains("H")) {
				hours = totalSeconds / (60*60);
				totalSeconds = totalSeconds % (60*60);
				indexes = outputFormatInBetweens[i].length();
				output[i] = addZeroes("" + hours, indexes);
			} else if (outputFormatInBetweens[i].toUpperCase().contains("M")) {
				minutes = totalSeconds / 60;
				totalSeconds = totalSeconds % 60;
				indexes = outputFormatInBetweens[i].length();
				output[i] = addZeroes("" + minutes, indexes);
			} else if (outputFormatInBetweens[i].toUpperCase().contains("S")) {
				seconds = totalSeconds;
				indexes = outputFormatInBetweens[i].length();
				output[i] = addZeroes("" + seconds, indexes);
			}
		}
		
		String toReturn = convertArrayToSeperatedString(output, ':');
		return toReturn;
	}
	
	public static String convertArrayToSeperatedString(String[] inBetweens, char divider) {
		String outputString = "";
		for (int i = 0; i < inBetweens.length; i++) {
			if (i == inBetweens.length - 1) {
				outputString = outputString + inBetweens[i];
			} else {
				outputString = outputString + inBetweens[i] + divider;
			}
		}

		return outputString;
	}
	

	/**
	 * This finds the substrings in between the divider given
	 * @param input - The string to loop through and find the substrings of
	 * @param divider - The character to look for when dividing up the input
	 * @return A String[] of the substrings in between
	 */
	public static String[] findInBetween(String input, char divider) {
		int[] dividerIndexes = {};
		for (int i = 0; i < input.length(); i++) { //This loop finds all of the dividers in input and adds them to colons, with each index holding the index
			if (input.charAt(i) == divider) {	   //of each colon within input
				int[] dividerIndexes2 = new int[dividerIndexes.length + 1];
				System.arraycopy(dividerIndexes, 0, dividerIndexes2, 0, dividerIndexes.length);
				dividerIndexes2[dividerIndexes.length] = i;
				dividerIndexes = dividerIndexes2;
			}
		}
		
		String[] inBetween = {};
		try {
			for (int i = 0; i <= dividerIndexes.length; i++) {
				if (dividerIndexes.length == 0) {
					String[] inBetween2 = new String[inBetween.length + 1];
					System.arraycopy(inBetween, 0, inBetween2, 0, inBetween.length);
					inBetween2[inBetween.length] = input;
					inBetween = inBetween2;
				} else if (i == 0) { //If this is the first colon 
					String[] inBetween2 = new String[inBetween.length + 1];
					System.arraycopy(inBetween, 0, inBetween2, 0, inBetween.length);
					inBetween2[inBetween.length] = input.substring(0, dividerIndexes[i]);
					inBetween = inBetween2;
				} else if (i == dividerIndexes.length) { //If this is the last colon
					String[] inBetween2 = new String[inBetween.length + 1];
					System.arraycopy(inBetween, 0, inBetween2, 0, inBetween.length);
					inBetween2[inBetween.length] = input.substring(dividerIndexes[i - 1] + 1, input.length());
					inBetween = inBetween2;
				} else {
					String[] inBetween2 = new String[inBetween.length + 1];
					System.arraycopy(inBetween, 0, inBetween2, 0, inBetween.length);
					inBetween2[inBetween.length] = input.substring(dividerIndexes[i - 1] + 1, dividerIndexes[i]);
					inBetween = inBetween2;
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Something went wrong in findInBetween. If I had to guess, I'd say that your format's wrong.");
			e.printStackTrace();
		}
		
		return inBetween;
	}
	
	public void showErrorDialogue(Exception e) { 
		if (System.currentTimeMillis() - this.lastErrorTime < 1000) {
			errorDialogue(e);
		} else {
			System.err.println(e.toString());
		}
		
		lastErrorTime = System.currentTimeMillis();
	}
	
	public static void errorDialogue(Throwable e) { // Credit for this goes to this blog: http://code.makery.ch/blog/javafx-dialogs-official/
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Exception");
		alert.setHeaderText("There was an exception of type " + e.getClass());
		alert.setContentText("Shit...");

		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String exceptionText = sw.toString();

		Label label = new Label("The exception stacktrace was:");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);
		
		ButtonType printType = new ButtonType("Print to Console");
		alert.getButtonTypes().add(printType);
		
		final Button printButton = (Button)alert.getDialogPane().lookupButton(printType);
		printButton.addEventFilter(ActionEvent.ACTION, event -> {
			e.printStackTrace();
			event.consume();
		});

		alert.showAndWait();
	}
	
	public void showColonError(Alert alert, int i, ButtonType... elements) {
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == elements[0]) { // elements[0] should be buttonType1
		    Alert doubleCheck = new Alert(AlertType.CONFIRMATION);
		    doubleCheck.setTitle("Are you sure?");
		    doubleCheck.setHeaderText("Think about it");
		    doubleCheck.setContentText("This will permanently erase this line from the data sheet! (there is always a backup somewhere). Are you sure you want to do this?");
		    
		    ButtonType yes = new ButtonType("Yes");
			ButtonType cancel = new ButtonType("Cancel");

			doubleCheck.getButtonTypes().setAll(yes, cancel);

			Optional<ButtonType> doubleCheckResult = doubleCheck.showAndWait();
			if (doubleCheckResult.get() == yes) {
				ArrayList<String[]> data = readFile(csvDir, csvName, true,-1);
				System.out.println("Removing line " + i + " from \"" + csvName + "\", it used to say: " + Arrays.toString(data.get(i)));
				data.remove(i);
				try {
					writeStringArray(data, csvDir, csvName);
				} catch (IOException e) {
					showErrorDialogue(e);
					System.err.println("You know things are bad when there's an error dialog showing an error. (1)");
					showColonError(alert, i, elements);
				}
		    } else {
		    	showColonError(alert, i, elements); // go back to the original error dialog
		    }
		} else if (result.get() == elements[1]) { // elements[1] should be buttonType2
			try {
				writeCell(i, 0, "0:00", csvDir, csvName);
				writeCell(i, 1, "0:00", csvDir, csvName);
			} catch (IOException e) {
				showErrorDialogue(e);
				System.err.println("You know things are bad when there's an error dialog showing an error. (2)");
				showColonError(alert, i, elements);
			}
		} else {
		    // ... user chose CANCEL or closed the dialog
		}
	}
	

	//------------------------------------------------------------------------------------//
	//------------------------------------Preferences-------------------------------------//
	//------------------------------------------------------------------------------------//

	public void initPreferences() {
		prefs = Preferences.userNodeForPackage(DataHandler.class);
		
		for (int i = 0; i < prefKeys.length; i++) {
			if (prefs.get(prefKeys[i], prefDefs[i]).equals(prefDefs[i])) { // If it's the default value currently, this works because .get() returns the default value given when no value is found
				prefs.put(prefKeys[i], prefDefs[i]); // Make sure the key is there with the default value, otherwise
			}
		}

		String csvDir = prefs.get("csvDir", "C:/Users/JAK/Documents/Google Drive/asdf");
		System.out.println(csvDir);

		try {
			prefs.sync(); // Not entirely sure that I need this or what exactly it does, but it seems like a good idea 
		} catch (BackingStoreException e) {
			System.out.println("The syncing operation in initPreferences() failed");
			e.printStackTrace();
		}
	}
	
	public void resetPreferencesToDefaults() {
		for (int i = 0; i < prefKeys.length; i++) {
			prefs.put(prefKeys[i], prefDefs[i]);
		}
	}
	
	public void refreshPreferences() {
		csvDir = prefs.get(prefKeys[0], prefDefs[0]); // Set the csvDir to the stored preference value
		csvName = prefs.get(prefKeys[1], prefDefs[1]) + ".csv"; // The extra ".csv" is necessary because the user isn't allowed to change that in the preferences, so it isn't a part of the stored value
		
		mostRecentYear = getMostRecentYear(csvDir, csvName);
	}
}
