package com.homeworkdata.ui.custom.control;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;

/**
 * Textfield implementation that accepts formatted number and stores them in a BigDecimal property The user input is formatted when the focus is lost or the
 * user hits RETURN.
 *
 * @author Thomas Bolz
 * Taken from this post: http://java.dzone.com/articles/javafx-numbertextfield-and
 */
public class NumberTextField extends FilteredTextField {

	private NumberFormat nf;
	private ObjectProperty<BigDecimal> number = new SimpleObjectProperty<>();

	public final BigDecimal getNumber() {
		return number.get();
	}

	public final void setNumber(BigDecimal value) {
		number.set(value);
	}
	
	public final void setNumber(double value) {
		number.set(new BigDecimal(value));
	}

	public ObjectProperty<BigDecimal> numberProperty() {
		return number;
	}

	public NumberTextField() {
		this(BigDecimal.ZERO);
	}

	public NumberTextField(BigDecimal value) {
		this(value, NumberFormat.getInstance());
		initHandlers();
	}

	public NumberTextField(BigDecimal value, NumberFormat nf) {
		this(value, nf, "((\\d*)|(\\d+\\.\\d*))");
	}
	
	public NumberTextField(BigDecimal value, NumberFormat nf, String regex) {
		super(regex);
		this.nf = nf;
		initHandlers();
		setNumber(value);
	}

	private void initHandlers() {

		// try to parse when focus is lost or RETURN is hit
		setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				parseAndFormatInput();
			}
		});

		focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (!newValue.booleanValue()) {
					parseAndFormatInput();
				}
			}
		});

		// Set text in field if BigDecimal property is changed from outside.
		numberProperty().addListener(new ChangeListener<BigDecimal>() {

			@Override
			public void changed(ObservableValue<? extends BigDecimal> obserable, BigDecimal oldValue, BigDecimal newValue) {
				setText(nf.format(newValue));
			}
		});
	}
	
	public void setFormat(NumberFormat format) {
		this.nf = format;
	}

	/**
	 * Tries to parse the user input to a number according to the provided NumberFormat
	 */
	public void parseAndFormatInput() {
		try {
			String input = getText();
			if (input == null || input.length() == 0) {
				return;
			}
			Number parsedNumber = nf.parse(input);
			BigDecimal newValue = new BigDecimal(parsedNumber.toString());
			setNumber(newValue);
			selectAll();
		} catch (ParseException ex) {
			// If parsing fails keep old number
			setText(nf.format(number.get()));
		}
	}
}