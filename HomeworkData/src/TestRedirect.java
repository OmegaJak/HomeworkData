/**
 * This was originally taken from the post from MadProgrammer in this StackOverflow thread: 
 * http://stackoverflow.com/questions/12945537/how-to-set-output-stream-to-textarea
 * Many thanks to him
 */

import java.awt.EventQueue;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class TestRedirect {

	@FXML private TextArea output;
	
	public static void main(String[] args) {
		new TestRedirect();
	}

	public TestRedirect() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {

				PrintStream ps = System.out;
				System.setOut(new PrintStream(new StreamCapturer("STDOUT", output, ps)));

				System.out.println("Hello, this is a test");
				System.out.println("Wave if you can see me");
			}
		});
	}

	public class CapturePane extends TextArea implements Consumer {

		@Override
		public void appendText(final String text) {
			output.appendText(text);
		}
	}

	public interface Consumer {
		public void appendText(String text);
	}

	public class StreamCapturer extends OutputStream {

		private StringBuilder buffer;
		private String prefix;
		private TextArea area;
		private PrintStream old;

		public StreamCapturer(String prefix, TextArea area, PrintStream old) {
			this.prefix = prefix;
			buffer = new StringBuilder(128);
			buffer.append("[").append(prefix).append("] ");
			this.old = old;
			this.area = area;
		}

		@Override
		public void write(int b) throws IOException {
			char c = (char)b;
			String value = Character.toString(c);
			buffer.append(value);
			if (value.equals("\n")) {
				area.appendText(buffer.toString());
				buffer.delete(0, buffer.length());
				buffer.append("[").append(prefix).append("] ");
			}
			old.print(c);
		}
	}
}
