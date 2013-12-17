package de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel.custom;

import java.awt.Dimension;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import de.invation.code.toval.graphic.util.SpringUtilities;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.gui.GUIProperties;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.AbstractTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.TransformerDialog;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.custom.DesignatorTransformer;

public class DesignatorTransformerPanel extends AbstractTransformerPanel<DesignatorTransformer> {

	private static final long serialVersionUID = -7904502865560626800L;
	
	private JTextField txtMinSize;
	private JTextField txtDesignator;
	
	public DesignatorTransformerPanel(Set<String> activities) throws Exception {
		super(activities);
	}

	@Override
	protected void initialize() throws Exception {}

	@Override
	protected void addComponents() throws Exception {
		setLayout(new SpringLayout());
		JLabel lblMinSize = new JLabel("Minimum Size:");
		lblMinSize.setPreferredSize(new Dimension(TransformerDialog.LABEL_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
		lblMinSize.setHorizontalAlignment(JLabel.TRAILING);
		add(lblMinSize);
		txtMinSize = new JTextField("1");
		txtMinSize.setPreferredSize(new Dimension(TransformerDialog.FIELD_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
		add(txtMinSize);
		JLabel lblDesignator = new JLabel("Designator:");
		lblDesignator.setPreferredSize(new Dimension(TransformerDialog.LABEL_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
		lblDesignator.setHorizontalAlignment(JLabel.TRAILING);
		add(lblDesignator);
		txtDesignator = new JTextField("longTrace");
		txtDesignator.setPreferredSize(new Dimension(TransformerDialog.FIELD_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
		add(txtDesignator);
		SpringUtilities.makeCompactGrid(this, 2, 2, 0, 0, 5, 5);
	}

	@Override
	public void initializeFields(DesignatorTransformer transformer) throws Exception {
		txtMinSize.setText(transformer.getMinSize().toString());
		txtDesignator.setText(transformer.getDesignator());
	}

	@Override
	public void validateFieldValues() throws ParameterException {
		Validate.positiveInteger(txtMinSize.getText());
		Validate.notEmpty(txtDesignator.getText());
	}

	@Override
	public Object[] getParameters() throws Exception {
		return new Object[]{Integer.parseInt(txtMinSize.getText()), txtDesignator.getText()};
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

}
