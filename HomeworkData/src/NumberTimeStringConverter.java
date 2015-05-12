import javafx.util.StringConverter;


public class NumberTimeStringConverter extends StringConverter<Number> {
	DataHandler handler;
	
	NumberTimeStringConverter(DataHandler handler) {
		this.handler = handler;
	}
	
	@Override
	public Number fromString(String time) {
		return handler.convertTimeToSeconds(handler.findInBetween("HH:MM", ':'), handler.convertStringsToInts(handler.findInBetween(time, ':')));
	}

	@Override
	public String toString(Number seconds) {
		return handler.convertSecondsToFormattedString(handler.findInBetween("HH:MM", ':'), seconds.intValue());
	}

}
