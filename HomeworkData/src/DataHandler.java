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
		csvDir = "C:\\Users\\JAK\\Programming\\Other Random Java\\HomeworkData";
		csvName = "HomeworkData.csv";
		main();
	}

	public static void main() { //not the real and proper main method
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

			/*for (int i = 0; i < rows.size(); i++) {
				System.out.println(rows.get(i)[2]);
			}*/

			writeCell(6, 4, "Test", csvDir + "\\HomeworkData", "test.csv", false);
			
			convertTime("02:23:54", "HH:MM:SS", "MM:SS");
			
			timePerUnit(readFile(csvDir, csvName, false), 4);
			
			insertNewRow(-1, 7, csvDir + "\\HomeworkData", "test.csv", false);
			
		} catch (IOException e) {
			System.out.println("There was an IOException somewhere. Stahp.");
			e.printStackTrace();
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
	 * @param isMakingACopy - If this is true, then it will just copy the whole thing to another file, and not make any changes
	 */
	private static void writeCell(int row, int column, String fill, String dir, String file, boolean isMakingACopy) throws IOException {
		if (!isMakingACopy) {
			writeCell(row, column, fill, dir, file, true); //Making a backup
		}

		ArrayList<String[]> rows = readFile(dir, file, true);

		PrintWriter pw = new PrintWriter(new FileWriter(isMakingACopy ? "Backup-" + file : file));

		if (!isMakingACopy) {
			System.out.println("Writing \"" + fill + "\" to row " + row + ", column " + column + " of " + file);
		}

		for (int i = 0; i < rows.size(); i++) {
			for (int k = 0; k < rows.get(i).length; k++) {
				if (i == row && k == column && !isMakingACopy) { //A specific cell, like row 4 column 3
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
	 * Inserts a new row, basically just adds some commas where you tell it to
	 * 
	 * @param precedingRow - The row preceding where you will insert the new row 
	 * @param precedingRow = -2 - Add a new row to the end of the document
	 * @param precedingRow > -1 - Add a new row to the beginning of the document
	 * @param precedingRow >= 0 - Exactly as it sounds
	 * @param columns - The number of columns to create in between and after the commas
	 * @param dir - The directory of the file to be edited
	 * @param file - The name of the file to be edited
	 * @param isMakingACopy - If this is true, then it will just copy the whole thing to another file, and not make any changes
	 * @throws IOException - If something goes wrong reading the file
	 */
	private static void insertNewRow(int precedingRow, int columns, String dir, String file, boolean isMakingACopy) throws IOException {
		if (!isMakingACopy) {
			insertNewRow(precedingRow, columns, dir, file, true); //Making a backup
		}

		ArrayList<String[]> rows = readFile(dir, file, true);

		PrintWriter pw = new PrintWriter(new FileWriter(isMakingACopy ? "Backup-" + file : file));

		if (!isMakingACopy) {
			System.out.println("Creating a new line after row " + precedingRow + " with " + columns + " columns in " + file);
		}

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
				if (i == precedingRow && !isMakingACopy) { //If this is just after the preceding row
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
	public static ArrayList<String[]> readFile(String dir, String file, boolean allowEmptyLines) {
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

	//-------------------------------------------------------------------------------------//
	//--------------------------------Data analysis methods--------------------------------//
	//-------------------------------------------------------------------------------------//

	private static ArrayList<String[]> timePerUnit(ArrayList<String[]> dataSheet, int row) throws IOException{
		try {
			double calculatedResult = Double.parseDouble(convertTime(dataSheet.get(row)[7], "H:MM", "SS")) / Double.parseDouble(dataSheet.get(row)[4]);
			String formattedResult = convertTime("" + (int)calculatedResult, "SS", "MM:SS");
			writeCell(row, 5, formattedResult, csvDir, csvName, false);
		} catch (NumberFormatException e) {
			System.out.println("What are numbers!?");
			e.printStackTrace();
		}
		return null;
	}
	
	//------------------------------------------------------------------------------------//
	//--------------------------------Other Helper Methods--------------------------------//
	//------------------------------------------------------------------------------------//
	
	/**
	 * 
	 * @param input Needs to be in the format provided
	 * @param inputFormat Something like HH:MM:SS
	 * @param outputFormat Same as above, like HH:MM:SS
	 * @return The total minutes in the time provided
	 */
	public static String convertTime(String input, String inputFormat, String outputFormat) {
		
		System.out.println("Converting \"" + input + "\" with format \"" + inputFormat + "\", outputting with format \"" + outputFormat + "\"");
		
		String[] inputFormatInBetweens = findInBetween(inputFormat, ':'); //This is an array something like... {"HH","MM","SS"}
		String[] inputInBetweens = findInBetween(input, ':'); //This is an array something like... {"05","32","50"}
		int[] numberInputsInBetween = convertStringsToInts(inputInBetweens);
		String[] outputFormatInBetweens = findInBetween(outputFormat, ':');
		
		int totalSeconds = convertTimeToSeconds(inputFormatInBetweens, numberInputsInBetween);
		String outputString = convertSecondsToFormattedString(outputFormatInBetweens, totalSeconds);
		
		System.out.println("The converted output was " + outputString);
		
		return outputString;
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
	
	public static String convertSecondsToFormattedString(String[] outputFormatInBetweens, int totalSeconds) {
		String[] output = new String[outputFormatInBetweens.length];
		int hours = -1, minutes = -1, seconds = -1;
		for (int i = 0; i < outputFormatInBetweens.length; i++) {
			if (outputFormatInBetweens[i].toUpperCase().contains("H")) {
				hours = totalSeconds / (60*60);
				totalSeconds = totalSeconds % (60*60);
				output[i] = hours + "";
			} else if (outputFormatInBetweens[i].toUpperCase().contains("M")) {
				minutes = totalSeconds / 60;
				totalSeconds = totalSeconds % 60;
				output[i] = minutes + "";
			} else if (outputFormatInBetweens[i].toUpperCase().contains("S")) {
				seconds = totalSeconds;
				output[i] = seconds + "";
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
