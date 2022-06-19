package org.ent.gui;

import org.ent.dev.hyper.FloatHyperparameter;
import org.ent.dev.hyper.Hyperparameter;
import org.ent.dev.hyper.IntegerHyperparameter;
import org.ent.dev.hyper.RangedHyperparameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import java.awt.Frame;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;
import static javax.swing.LayoutStyle.ComponentPlacement.UNRELATED;

public class ParametersDialog extends JDialog {

	private static final Logger log = LoggerFactory.getLogger(ParametersDialog.class);

	@Serial
	private static final long serialVersionUID = 1L;

	private static class ParameterControl<T> {

		private final RangedHyperparameter<T> hyper;

		private final JLabel label;

		private final JTextField textfield;

		private final JSlider slider;

		public ParameterControl(RangedHyperparameter<T> hyper) {
			this.hyper = hyper;

			label = new JLabel();
			label.setText(hyper.getDescription());

			textfield = new JTextField();
			textfield.setColumns(6);
			textfield.setText(String.valueOf(hyper.getDefaultValue()));
			textfield.addActionListener(a -> {
				log.trace("pressed enter {}", textfield.getText());
				updateFromTextfield();
			});
			textfield.addFocusListener(new FocusAdapter() {
			    @Override
				public void focusLost(FocusEvent e) {
			        log.trace("User entered {}", textfield.getText());
					updateFromTextfield();
			    }
			});

			slider = new JSlider();
			slider.setMinimum(toSliderValue(hyper.getMinimumValue()));
			slider.setMaximum(toSliderValue(hyper.getMaximumValue()));
			slider.setMajorTickSpacing(10);
			slider.setMinorTickSpacing(1);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			slider.setValue(toSliderValue(hyper.getDefaultValue()));
			slider.addChangeListener(e -> {
				log.trace("slider value changed {}", slider.getValue());
				updateFromSlider();
			});
		}

		private void updateFromTextfield() {
			String text = textfield.getText();
			T value = toHyperValue(text);
			slider.setValue(toSliderValue(value));
			updateHyper(value);
		}

		private void updateFromSlider() {
			int value = slider.getValue();
			T x = fromSliderValue(value);
			textfield.setText(String.valueOf(x));
			updateHyper(x);
		}

		private void updateHyper(T value) {
			try {
				hyper.setValue(value);
			} catch (IllegalArgumentException e) {
				log.error("illegal parameter value", e);
			}
		}

		public JLabel getLabel() {
			return label;
		}

		public JTextField getTextfield() {
			return textfield;
		}

		public JSlider getSlider() {
			return slider;
		}

		private int toSliderValue(T x) {
			if (hyper instanceof IntegerHyperparameter) {
				return (Integer) x;
			} else if (hyper instanceof FloatHyperparameter) {
				return Math.round((Float) x * 100);
			} else {
				throw new AssertionError();
			}
		}

		private T toHyperValue(String text) {
			if (hyper instanceof IntegerHyperparameter) {
				@SuppressWarnings("unchecked")
				T result = (T) Integer.valueOf(Integer.parseInt(text));
				return result;
			} else if (hyper instanceof FloatHyperparameter) {
				@SuppressWarnings("unchecked")
				T result = (T) Float.valueOf(Float.parseFloat(text));
				return result;
			} else {
				throw new AssertionError();
			}
		}

		private T fromSliderValue(int value) {
			if (hyper instanceof IntegerHyperparameter) {
				@SuppressWarnings("unchecked")
				T result = (T) Integer.valueOf(value);
				return result;
			} else if (hyper instanceof FloatHyperparameter) {
				@SuppressWarnings("unchecked")
				T result = (T) Float.valueOf((float) value / 100);
				return result;
			} else {
				throw new AssertionError();
			}
		}
	}

	public ParametersDialog(Frame parent) {
		super(parent);
		setTitle("Parameters");
		build();
		pack();
	}

	private void build() {
		JCheckBox cbGroup = new JCheckBox("From registry");
		cbGroup.setSelected(true);

		JPanel pnlGroup = new JPanel();
		pnlGroup.setBorder(BorderFactory.createEtchedBorder());

		List<Hyperparameter<?>> parameters = Main.getHyperRegistry().getParameters();
		List<ParameterControl<?>> elements = new ArrayList<>();
		for (Hyperparameter<?> hyper : parameters) {
			if (hyper instanceof RangedHyperparameter rangedHyperparameter) {
				elements.add(new ParameterControl(rangedHyperparameter));
			} else {
				throw new AssertionError();
			}
		}

		GroupLayout pnlGroupLayout = new GroupLayout(pnlGroup);
		pnlGroup.setLayout(pnlGroupLayout);

		ParallelGroup labelGroupHorizontal = pnlGroupLayout.createParallelGroup(TRAILING);
		elements.forEach(element -> labelGroupHorizontal.addComponent(element.getLabel()));

		ParallelGroup textfieldGroupHorizontal = pnlGroupLayout.createParallelGroup();
		elements.forEach(element ->
				textfieldGroupHorizontal.addComponent(element.getTextfield(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE));

		ParallelGroup sliderGroupHorizontal = pnlGroupLayout.createParallelGroup();
		elements.forEach(element ->
				sliderGroupHorizontal.addComponent(element.getSlider(), DEFAULT_SIZE, 400, Short.MAX_VALUE));

		pnlGroupLayout.setHorizontalGroup(pnlGroupLayout.createSequentialGroup()
			.addContainerGap()
			.addGroup(labelGroupHorizontal)
			.addPreferredGap(RELATED)
			.addGroup(textfieldGroupHorizontal)
			.addPreferredGap(RELATED)
			.addGroup(sliderGroupHorizontal)
			.addContainerGap()
		);

		SequentialGroup linesGroup = pnlGroupLayout.createSequentialGroup()
				.addContainerGap();
		for (int i = 0; i < elements.size(); i++) {
			ParameterControl<?> element = elements.get(i);
			ParallelGroup lineGroup = pnlGroupLayout.createParallelGroup(LEADING)
				.addGroup(
					pnlGroupLayout.createParallelGroup(BASELINE)
					.addComponent(element.getLabel())
					.addComponent(element.getTextfield(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(element.getSlider());
			linesGroup.addGroup(lineGroup);
			if (i < elements.size() - 1) {
				linesGroup.addPreferredGap(UNRELATED);
			} else {
				linesGroup.addContainerGap();
			}
		}

		pnlGroupLayout.setVerticalGroup(linesGroup);

		GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
        		.addContainerGap()
        		.addGroup(layout.createParallelGroup()
        				.addComponent(cbGroup)
        				.addComponent(pnlGroup)
        				)
        		.addContainerGap()
        		);

        layout.setVerticalGroup(layout.createSequentialGroup()
        		.addContainerGap()
				.addPreferredGap(UNRELATED)
				.addComponent(cbGroup)
				.addComponent(pnlGroup)
        		.addContainerGap()
        		);
	}
}
