package CustomControls;

import java.util.regex.Pattern;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.DoubleStringConverter;

public class FilteredTextField extends TextField {
	public FilteredTextField(String regex) {
		super();
		
		Pattern validText = Pattern.compile(regex);
		
		TextFormatter textFormatter = new TextFormatter<>(change -> {
			String newText = change.getControlNewText();
			if (validText.matcher(newText).matches()) {
				return change;
			} else
				return null;
		});
		
		this.setTextFormatter(textFormatter);
	}
}
