import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class DataHandler {

	private static Charset charset = Charset.forName("US-ASCII");
	public static String csvDir = "";
	public static String csvName = "";
	
	public DataHandler() {
		//csvDir = "/home/jak/Programming/HomeworkData/HomeworkData";
		csvDir = "C:\\Users\\JAK\\Programming\\Other Random Java\\HomeworkData\\HomeworkData";
		//csvDir = "C:\\Users\\JAK\\Documents\\Google Drive";
		csvName = "HomeworkDataSem2.csv";
		main();
	}

	public void main() { //not the real and proper main method
		try {
			ArrayList<String[]> rows = readFile(csvDir, csvName, false);

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
			
			ArrayList<String[]> dataSheet = readFile(csvDir, csvName, false);
			for (int i = 0; i < dataSheet.size(); i++) {
				if (dataSheet.get(i)[7].equals("0")) {
					writeCell(i, 7, "0:00", csvDir, csvName);
				}
			}
			
			dataSheet = readFile(csvDir, csvName, false);
			for (int i = 1; i < dataSheet.size(); i++) {
				if (dataSheet.get(i).equals("")) {
					timePerUnit(dataSheet, i);
				}
			}
			
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

		ArrayList<String[]> rows = readFile(dir, file, true);

		PrintWriter pw = new PrintWriter(new FileWriter(file));

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

		PrintWriter pw = new PrintWriter(new FileWriter(file));

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

		ArrayList<String[]> rows = readFile(dir, file, true);

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
		
		ArrayList<String[]> rows = readFile(dir, file, true);

		PrintWriter pw = new PrintWriter(new FileWriter(file));

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
	}

	/**
	 * @param dir - The directory of the file to be read
	 * @param file- The name of the file to be read
	 * @param allowEmptyLines - If this is false, it will only add lines that have something in the first cell
	 * @return An Arraylist of String arrays, with each String array being a row of the csv file, each index of each array a cell
	 */
	public ArrayList<String[]> readFile(String dir, String file, boolean allowEmptyLines) {
		try {
			Path path = FileSystems.getDefault().getPath(dir, file); //The path to the file, needed for newBufferedReader()

			BufferedReader reader = Files.newBufferedReader(path, charset);
			String line = null;

			ArrayList<String[]> rows = new ArrayList<String[]>();
			String curLine[] = {};
			while ((line = reader.readLine()) != null) {//Go through each line sequentially until there are no more
				curLine = line.split(",", -1); //Creates an array of each element between the commas
				if (allowEmptyLines || line.charAt(0) != ',') {//Ignore empty rows
					rows.add(curLine);
				}
			}

			reader.close();
			return rows;
		} catch (IOException e) {
			System.out.println("There was an error while reading the file!");
			e.printStackTrace();
		}
		return new ArrayList<String[]>();
	}
	
	public int getNumberOfLines(String dir, String file, boolean allowEmptyLines) {
		ArrayList<String[]> temp = readFile(dir, file, allowEmptyLines);
		return temp.size();
	}
	
	/**
	 * Generates an array representing the cells in a given column
	 * @param column - The number of the column to generate from, starting at 0
	 * @param allowDuplicates - Whether or not there can be more than one of an item in the generated array
	 * @param dir - The directory of the file to read from
	 * @param file - The name of the file to read from
	 * @param allowEmptyLines - Whether or not empty lines are allowed when reading the file
	 * @return A String[] representing the individual cells in a given column
	 */
	public String[] getColumnArray(int column, boolean allowDuplicates, String dir, String file, boolean allowEmptyLines, int conditionalColumn, String ifMatches) {
		ArrayList<String[]> rows = readFile(dir, file, allowEmptyLines);
		String[] columnCells = {};
		
		for (int i = 1; i < rows.size(); i++) {
			if (ifMatches.length() > 0 ? rows.get(i)[conditionalColumn].equals(ifMatches) : true) {
				if (!allowDuplicates && !alreadyAdded(columnCells, rows.get(i)[column])) {
					String[] columnCells2 = new String[columnCells.length + 1];
					System.arraycopy(columnCells, 0, columnCells2, 0, columnCells.length);
					columnCells2[columnCells.length] = rows.get(i)[column];
					columnCells = columnCells2;
				} else if (allowDuplicates) {
					String[] columnCells2 = new String[columnCells.length + 1];
					System.arraycopy(columnCells, 0, columnCells2, 0, columnCells.length);
					columnCells2[columnCells.length] = rows.get(i)[column];
					columnCells = columnCells2;
				}
			}
		}
		
		return columnCells;
	}

	//-------------------------------------------------------------------------------------//
	//--------------------------------Data analysis methods--------------------------------//
	//-------------------------------------------------------------------------------------//

	public ArrayList<String[]> timePerUnit(ArrayList<String[]> dataSheet, int row) throws IOException { // This is a different methodology than checkForTimePerUnit in EventHandlerController, but it produces the same result. Might as well leave this in.
		try {
			double calculatedResult = Double.parseDouble(convertTime(subtractTime(dataSheet.get(row)[6], dataSheet.get(row)[8]), "H:MM", "SS")) / Double.parseDouble(dataSheet.get(row)[4]);
			System.out.println("The calculated result was " + calculatedResult);
			String formattedResult = convertTime(addZeroes("" + (int)(calculatedResult + 0.5), 2), "SS", "M:SS"); // The "+ 0.5" is for rounding to the nearest integer, rather than just rounding down
			writeCell(row, 5, formattedResult, csvDir, csvName);
		} catch (NumberFormatException e) {
			System.out.println("What are numbers!?");
			e.printStackTrace();
		}
		return null;
	}
	
	public String averageTimeSpent(ArrayList<String[]> datasheet, String homeworkClass, String homeworkType, String homeworkUnit) {
		String[] timePerUnits = {};
		String[] currentRow;
		
		for (int i = 0; i < datasheet.size(); i++) {
			currentRow = datasheet.get(i);
			if (currentRow[1].equals(homeworkClass) && currentRow[2].equals(homeworkType) && currentRow[3].equals(homeworkUnit)) {
				String[] timePerUnits2 = new String[timePerUnits.length + 1];
				System.arraycopy(timePerUnits, 0, timePerUnits2, 0, timePerUnits.length);
				timePerUnits2[timePerUnits.length] = currentRow[5];
				timePerUnits = timePerUnits2;
			}
		}
		
		String result = divideTime(addTimes(timePerUnits), timePerUnits.length);
		System.out.println("I found the average time spent on \"" + homeworkType + "\" and unit \"" + homeworkUnit + " in the class \"" + homeworkClass + "\" to be " + result + ".");
		return result;
	}
	
	//------------------------------------------------------------------------------------//
	//--------------------------------Other Helper Methods--------------------------------//
	//------------------------------------------------------------------------------------//
	
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
	 * 
	 * @param input Needs to be in the format provided
	 * @param inputFormat Something like HH:MM:SS
	 * @param outputFormat Same as above, like HH:MM:SS
	 * @return The total minutes in the time provided
	 */
	public String convertTime(String input, String inputFormat, String outputFormat) {
		
		String[] inputFormatInBetweens = findInBetween(inputFormat, ':'); //This is an array something like... {"HH","MM","SS"}
		String[] inputInBetweens = findInBetween(input, ':'); //This is an array something like... {"05","32","50"}
		int[] numberInputsInBetween = convertStringsToInts(inputInBetweens); // Just converting inputInBetweens to integers
		String[] outputFormatInBetweens = findInBetween(outputFormat, ':');
		
		String outputString = "";
		if (!outputFormat.contains("H") && !outputFormat.contains("M")) { // Check whether this helps at some point.... it might...
			outputString = "" + convertTimeToSeconds(inputFormatInBetweens, numberInputsInBetween);
		} else {
			int totalSeconds = convertTimeToSeconds(inputFormatInBetweens, numberInputsInBetween);
			outputString = convertSecondsToFormattedString(outputFormatInBetweens, totalSeconds);
		}

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
		minutesProduct = parts[1] * num;
		while (minutesProduct > 59) {
			hours++;
			minutesProduct -= 60;
		}
		
		int secondsProduct = parts[2] * num;
		while (secondsProduct > 59) {
			minutesProduct++;
			secondsProduct -= 60;
		}
		
		String hoursReturn = "" + hours;
		String minutesReturn = addZeroes("" + minutesProduct, 2);
		String secondsReturn = addZeroes("" + secondsProduct, 2);

		return hoursReturn + ":" + minutesReturn + ":" + secondsReturn;
	}
	
	/**
	 * Divides an amount of time by a number
	 * 
	 * @param time - The amount of time to divide, in the format of HH:MM:SS
	 * @param num - The number to divide the time by
	 * @return - The rounded, divided time
	 */
	public String divideTime(String time, int num) {
		int[] parts = convertStringsToInts(findInBetween(time, ':'));
		double returnHours = 0, returnMinutes = 0, returnSeconds = 0;
		
		double dividedHours = parts[0] / (double)num;
		double decimalHours = dividedHours - round(dividedHours, 2);
		returnHours = dividedHours - decimalHours;
		
		returnMinutes += round(60 * decimalHours, 2);

		if (parts.length > 1) {
			double dividedMinutes = parts[1] / (double)num;
			double decimalMinutes = dividedMinutes - round(dividedMinutes, 2);
			returnMinutes = returnMinutes + dividedMinutes - decimalMinutes;
			if (returnMinutes > 60) {
				// TODO Do some stuff here that will probably never be run
			}

			returnSeconds += round(60 * decimalMinutes, 2);

			if (parts.length > 2) {
				double dividedSeconds = parts[2] / (double)num;
				double decimalSeconds = dividedSeconds - round(dividedSeconds, 2);
				returnSeconds = returnSeconds + dividedSeconds - decimalSeconds;
				if (returnSeconds > 60) {
					// TODO Do some stuff here
				}
			}
		}

		return (int)returnHours + ":" + addZeroes("" + (int)returnMinutes, 2) + ":" + addZeroes("" + (int)returnSeconds, 2);
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
	
	public static int[] convertStringsToInts(String[] toConvert) {
		try {
			int[] convertedTo = new int[toConvert.length];
			for (int i = 0; i < toConvert.length; i++) {
				convertedTo[i] = Integer.parseInt(toConvert[i]);
			}
			return convertedTo;
		} catch (NumberFormatException e) {
			System.out.println("There was an error while converting a string to ints");
		}
		int[] blank = {};
		return blank;
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
}
