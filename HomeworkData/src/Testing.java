import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Testing {

	private static Charset charset = Charset.forName("US-ASCII");

	public static void main(String[] args) {
		try {
			Path path = FileSystems.getDefault().getPath(
					"/home/jak/Programming/HomeworkData/HomeworkData/bin",
					"HomeworkData.csv");
			BufferedReader reader = Files.newBufferedReader(path, charset);
			String line = null;

			ArrayList<String[]> rows = new ArrayList<String[]>();
			String curLine[] = {};
			while ((line = reader.readLine()) != null) {//Go through each line sequentially until there are no more
				curLine = line.split(","); //Creates an array of each element between the commas
				if (line.charAt(0) != ',') {//Ignore empty rows
					rows.add(curLine);
				}
			}

			/* for (int i = 0; i < rows.size(); i++) { //Loop through the
			 * ArrayList for (int k = 0; k < rows.get(i).length; k++) {//through
			 * the inner array, i.e., looping through the columns of the current
			 * row System.out.print(rows.get(i)[k] + ",  "); }
			 * System.out.println(""); } */

			System.out.println(rows.size());

			/* for (int i = 0; i < rows.size(); i++) {
			 * System.out.println(rows.get(i)[2]); } */

			reader.close();

			writeCell(3, 2, "Test");
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}
	}

	/** @param row
	 *            The row you want to edit - 1
	 * @param column
	 *            The column you want to edit - 1
	 * @param fill
	 *            The string to fill the cell with */
	private static void writeCell(int row, int column, String fill) {
		try {
			Path path = FileSystems.getDefault().getPath(
					"/home/jak/Programming/HomeworkData/HomeworkData",
					"test.csv");
			BufferedReader reader = Files.newBufferedReader(path, charset);
			String line = null;

			ArrayList<String[]> rows = new ArrayList<String[]>();
			String curLine[] = {};
			while ((line = reader.readLine()) != null) {//Go through each line sequentially until there are no more
				curLine = line.split(",", -10); //Creates an array of each element between the commas
				rows.add(curLine);
			}

			PrintWriter pw = new PrintWriter(new FileWriter("test.csv"));

			System.out.println("writeCell()");
			System.out.println(rows.size());

			for (int i = 0; i < rows.size(); i++) {
				//System.out.println("i is: " + i);
				//System.out.println("row length is: " + rows.get(i).length);
				for (int k = 0; k < rows.get(i).length; k++) {
					//pw.write(",,,,,,,,\n");
					//System.out.println("k is: " + k);
					if (i == row && k == column) { //A specific cell, row 4 column 3
						pw.write(fill + ",");
					} else {
						//System.out.println("Wassup");
						if (k == rows.get(i).length - 1) { //If this is the last cell in the row
							pw.write(rows.get(i)[k] + "\n");
						} else {
							pw.write(rows.get(i)[k] + ",");
						}
					}
				}
			}

			pw.close();
		} catch (IOException e) {
			System.out.println("Ruh roh...");
		}
	}

	public static void readFile(String dir, String file, boolean ignoreEmptyLines) {
		try {
			Path path = FileSystems.getDefault().getPath(dir, file);
			BufferedReader reader = Files.newBufferedReader(path, charset);
			String line = null;

			ArrayList<String[]> rows = new ArrayList<String[]>();
			String curLine[] = {};
			while ((line = reader.readLine()) != null) {//Go through each line sequentially until there are no more
				curLine = line.split(","); //Creates an array of each element between the commas
				if (line.charAt(0) != ',') {//Ignore empty rows
					rows.add(curLine);
				}
			}
		} catch (IOException ioex) {
			System.out.println("There was an IOExceptiojn. Stahp.");
		}
	}

}
