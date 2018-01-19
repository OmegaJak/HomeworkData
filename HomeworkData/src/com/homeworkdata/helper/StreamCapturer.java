package com.homeworkdata.helper;
/**
 * This was originally taken from the post from MadProgrammer in this StackOverflow thread: 
 * http://stackoverflow.com/questions/12945537/how-to-set-output-stream-to-textarea
 * Many thanks to him/her
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import com.homeworkdata.data.DataHandler;

import javafx.scene.control.TextArea;


public class StreamCapturer extends OutputStream {

	private StringBuilder buffer;
	private String prefix;
	private TextArea area;
	private PrintStream old;
	private DataHandler handler;

	public StreamCapturer(String prefix, TextArea area, PrintStream old, DataHandler handler) {
		this.prefix = prefix;
		buffer = new StringBuilder(128);
		buffer.append("[").append(prefix).append("] ");
		this.old = old;
		this.area = area;
		this.handler = handler;
		
		try {
			new PrintWriter(handler.csvDir + "console.log").close(); // Clear the log file
		} catch (FileNotFoundException e) {
			handler.showErrorDialogue(e);
			e.printStackTrace();
		}
	}

	@Override
	public void write(int b) throws IOException {
		char c = (char)b;
		String value = Character.toString(c);
		buffer.append(value);
		if (value.equals("\n")) {
			area.appendText(buffer.toString());
			
			// Write the log to a file
			File file = new File(handler.csvDir + "console.log");
			FileWriter writer = new FileWriter(file, true);
			BufferedWriter bufferWriter = new BufferedWriter(writer);	
			bufferWriter.append(buffer.toString());
			bufferWriter.close(); // Always close...
			
			buffer.delete(0, buffer.length());
			buffer.append("[").append(prefix).append("] ");
		}
		old.print(c);
	}

}
