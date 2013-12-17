package de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel;

import java.awt.Dimension;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.invation.code.toval.graphic.component.BoxLayoutPanel;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.gui.GUIProperties;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.AbstractTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.TransformerDialog;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.DayDelayTransformer;

public class DayDelayTransformerPanel extends AbstractTransformerPanel<DayDelayTransformer> {

	private static final long serialVersionUID = -4070946570299997646L;
	
	private JTextField txtMinDays;
	private JTextField txtMaxDays;
	
	public DayDelayTransformerPanel(Set<String> activities) throws Exception {
		super(activities);
	}
	
	@Override
	protected void initialize() {}
	
	@Override
	protected void addComponents(){
		BoxLayout layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
		setLayout(layout);
		
		JPanel minDayPanel = new BoxLayoutPanel();
		JLabel lblMinDays = new JLabel("Min days:");
		lblMinDays.setHorizontalAlignment(JLabel.TRAILING);
		lblMinDays.setPreferredSize(new Dimension(TransformerDialog.LABEL_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
		lblMinDays.setMaximumSize(new Dimension(TransformerDialog.LABEL_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
		minDayPanel.add(lblMinDays);
		txtMinDays = new JTextField("1");
		txtMinDays.setPreferredSize(new Dimension(TransformerDialog.FIELD_WIDTH, GUIProperties.DEFAULT_TEXTFIELD_HEIGHT));
		txtMinDays.setMaximumSize(new Dimension(TransformerDialog.FIELD_WIDTH, GUIProperties.DEFAULT_TEXTFIELD_HEIGHT));
		minDayPanel.add(txtMinDays);
		minDayPanel.add(Box.createHorizontalGlue());
		add(minDayPanel);
		
		add(Box.createVerticalStrut(5));
		
		JPanel maxDayPanel = new BoxLayoutPanel();
		JLabel lblMaxDays = new JLabel("Max days:");
		lblMaxDays.setHorizontalAlignment(JLabel.TRAILING);
		lblMaxDays.setPreferredSize(new Dimension(TransformerDialog.LABEL_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
		lblMaxDays.setMaximumSize(new Dimension(TransformerDialog.LABEL_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
		maxDayPanel.add(lblMaxDays);
		txtMaxDays = new JTextField("1");
		txtMaxDays.setPreferredSize(new Dimension(TransformerDialog.FIELD_WIDTH, GUIProperties.DEFAULT_TEXTFIELD_HEIGHT));
		txtMaxDays.setMaximumSize(new Dimension(TransformerDialog.FIELD_WIDTH, GUIProperties.DEFAULT_TEXTFIELD_HEIGHT));
		maxDayPanel.add(txtMaxDays);
		maxDayPanel.add(Box.createHorizontalGlue());
		add(maxDayPanel);
		
		add(Box.createVerticalGlue());
	}

	@Override
	public void initializeFields(DayDelayTransformer transformer) throws Exception{
		Validate.notNull(transformer);
		txtMinDays.setText(transformer.getMinDays().toString());
		txtMaxDays.setText(transformer.getMaxDays().toString());
	}

	@Override
	public void validateFieldValues() throws ParameterException{
		Validate.positiveInteger(txtMinDays.getText());
		Validate.positiveInteger(txtMaxDays.getText());
	}

	@Override
	public Object[] getParameters() throws Exception{
		return new Object[]{Integer.parseInt(txtMinDays.getText()), Integer.parseInt(txtMaxDays.getText())};
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

}
