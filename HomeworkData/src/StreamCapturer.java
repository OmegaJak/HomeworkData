import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javafx.scene.control.TextArea;


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
