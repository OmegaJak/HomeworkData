import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Main {

	private static Charset charset = Charset.forName("US-ASCII");
	public static String csvDir = "";
	public static String csvName = "";

	public static void main(String[] args) {
		try {
			//csvDir = "/home/jak/Programming/HomeworkData/HomeworkData/bin";
			csvDir = "C:\\Users\\JAK\\Programming\\Other Random Java\\HomeworkData";
			csvName = "HomeworkData.csv";
			
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
			
			timePerUnit(readFile(csvDir, csvName, false), 4);
			
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
			System.out.println("writeCell()");
			System.out.println(rows.size());
			System.out.println("Writing \"" + fill + "\" to row " + row + ", column " + column + " of " + file);
		}

		for (int i = 0; i < rows.size(); i++) {
			for (int k = 0; k < rows.get(i).length; k++) {
				if (i == row && k == column && !isMakingACopy) { //A specific cell, row 4 column 3
					pw.write(fill + ",");
				} else {
					if (k == rows.get(i).length - 1) { //If this is the last cell in the row
						pw.write(rows.get(i)[k] + "\n");
					} else {
						pw.write(rows.get(i)[k] + ",");
					}
				}
			}
		}

		pw.close();
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
			System.out.println(e.getLocalizedMessage());
		}
		return new ArrayList<String[]>();
	}

	//-------------------------------------------------------------------------------------//
	//--------------------------------Data analysis methods--------------------------------//
	//-------------------------------------------------------------------------------------//

	private static ArrayList<String[]> timePerUnit(ArrayList<String[]> dataSheet, int row) throws IOException{
		double calculatedResult = convertTimeToDouble(dataSheet.get(row)[7]) / Double.parseDouble(dataSheet.get(row)[4]);
		writeCell(row, 5, String.valueOf(calculatedResult), csvDir, csvName, false);
		return null;
	}
	
	//------------------------------------------------------------------------------------//
	//--------------------------------Other Helper Methods--------------------------------//
	//------------------------------------------------------------------------------------//
	
	/**
	 * 
	 * @param time Needs to be in the format of H:MM
	 * @return The total minutes in the time provided
	 */
	public static double convertTimeToDouble(String time) {
		String minutes = time.substring(time.length() - 2, time.length());
		String hours = time.substring(0, 1);
		
		double total = 0;
		
		try {
			total = (Double.parseDouble(hours) * 60) + Double.parseDouble(minutes);
		} catch (NumberFormatException e) {
			System.out.println("Them numbers ain't right...");
			e.printStackTrace();
		}
		
		return total;
	}
}
