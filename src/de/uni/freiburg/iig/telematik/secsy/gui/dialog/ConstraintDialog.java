package de.uni.freiburg.iig.telematik.secsy.gui.dialog;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import de.invation.code.toval.constraint.AbstractConstraint;
import de.invation.code.toval.constraint.NumberConstraint;
import de.invation.code.toval.constraint.NumberOperator;
import de.invation.code.toval.constraint.Operator;
import de.invation.code.toval.constraint.OperatorFormats;
import de.invation.code.toval.constraint.StringConstraint;
import de.invation.code.toval.constraint.StringOperator;
import de.invation.code.toval.graphic.dialog.ValueChooserDialog;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.Context;


public class ConstraintDialog extends JDialog {

	private static final long serialVersionUID = -4145097630293671723L;

	private final JPanel contentPanel = new JPanel();
	
	private JPanel argumentPanel = null;
	
	private AbstractConstraint<?> constraint = null;
	
	private JComboBox operators = null;
	private Operator<?> operator = null;
	private ConstraintType view = ConstraintType.NUMBER;
	
	private Map<String, JTextField> argumentFields = new HashMap<String, JTextField>();
	
	private String attribute = null;
	private JTextField txtAttributeName;
	
	private JButton btnChoose = null;
	private Context context = null;
	
	private boolean attributeEditable = true;
	private boolean attributeChoosable = false;
	
	/**
	 * @wbp.parser.constructor
	 */
	public ConstraintDialog(Window owner, AbstractConstraint<?> constraint, Context context) {
		super(owner);
		this.context = context;
		this.constraint = constraint;
		this.attribute = constraint.getElement();
		attributeEditable = false;
		attributeChoosable = true;
		initialize(owner);
	}
	
	public ConstraintDialog(Window owner, AbstractConstraint<?> constraint, boolean attributeChangeable) {
		super(owner);
		this.constraint = constraint;
		this.attribute = constraint.getElement();
		attributeEditable = attributeChangeable;
		initialize(owner);
	}
	
	public ConstraintDialog(Window owner, AbstractConstraint<?> constraint) {
		this(owner, constraint, true);
	}
	
	public ConstraintDialog(Window owner, Context context) {
		super(owner);
		this.context = context;
		attributeEditable = false;
		attributeChoosable = true;
		initialize(owner);
	}

	public ConstraintDialog(Window owner, String attribute, boolean attributeChangeable) {
		super(owner);
		this.attribute = attribute;
		attributeEditable = attributeChangeable;
		initialize(owner);
	}
	
	public ConstraintDialog(Window owner, String attribute) {
		this(owner, attribute, true);
	}
	
	private void initialize(Window owner){
		setResizable(false);
		setBounds(100, 100, 293, 306);
		setModal(true);
		setLocationRelativeTo(owner);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel operatorPanel = new JPanel();
		operatorPanel.setPreferredSize(new Dimension(280, 120));
		contentPanel.add(operatorPanel, BorderLayout.NORTH);
		operatorPanel.setLayout(null);
		
		final JComboBox comboBox = new JComboBox();
		comboBox.setBounds(15, 15, 255, 27);
		operatorPanel.add(comboBox);
		comboBox.setEnabled(constraint == null);
		comboBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(comboBox.getSelectedIndex() == 0){
					setView(ConstraintType.NUMBER);
				} else {
					setView(ConstraintType.STRING);
				}
			}
		});
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"Number Constraint", "String Constraint"}));
		
		JLabel lblOperator = new JLabel("Operator:");
		lblOperator.setBounds(20, 91, 61, 16);
		operatorPanel.add(lblOperator);
		lblOperator.setHorizontalAlignment(SwingConstants.TRAILING);
		
		operators = new JComboBox();
		operators.setBounds(86, 87, 100, 27);
		operatorPanel.add(operators);
		
		txtAttributeName = new JTextField();
		txtAttributeName.setEditable(attributeEditable);
		txtAttributeName.setBounds(86, 49, 100, 28);
		operatorPanel.add(txtAttributeName);
		txtAttributeName.setColumns(10);
		txtAttributeName.setText(attribute);
		txtAttributeName.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {
				ConstraintDialog.this.attribute = txtAttributeName.getText();
			}
			
			@Override
			public void keyPressed(KeyEvent e) {}
		});
		
		btnChoose = new JButton("Choose");
		btnChoose.setBounds(190, 50, 80, 29);
		operatorPanel.add(btnChoose);
		btnChoose.setEnabled(attributeChoosable);
		btnChoose.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				List<String> chosenAttribute = null;
				try {
					chosenAttribute = ValueChooserDialog.showDialog(ConstraintDialog.this, "Constraint attribute", context.getAttributes());
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(ConstraintDialog.this), "<html>Cannot launch value chooser dialog dialog.<br>Reason: " + e1.getMessage() + "</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
				}
				if(chosenAttribute != null && !chosenAttribute.isEmpty()){
					ConstraintDialog.this.attribute = chosenAttribute.get(0);
					txtAttributeName.setText(chosenAttribute.get(0));
				}
			}
		});
		
		JLabel lblAttribute = new JLabel("Attribute:");
		lblAttribute.setHorizontalAlignment(SwingConstants.TRAILING);
		lblAttribute.setBounds(20, 54, 61, 16);
		operatorPanel.add(lblAttribute);
		
		argumentPanel = new JPanel();
		argumentPanel.setLayout(null);
		contentPanel.add(argumentPanel, BorderLayout.CENTER);
		operators.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(view == ConstraintType.NUMBER){
					operator = NumberOperator.values()[operators.getSelectedIndex()];
				} else {
					operator = StringOperator.values()[operators.getSelectedIndex()];
				}
				operators.setToolTipText(OperatorFormats.getDescriptor(operator));
				updateArguments();
			}
		});

		
		
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
						if(ConstraintDialog.this.attribute == null || ConstraintDialog.this.attribute.equals("")){
							JOptionPane.showMessageDialog(ConstraintDialog.this, "Missing attribute name.", "Invalid Argument", JOptionPane.ERROR_MESSAGE);
							return;
						}
						
						List<String> arguments = new ArrayList<String>(operator.getRequiredArguments());
						for(String argument: operator.getArgumentNames()){
							if(argumentFields.containsKey(argument)){
								if(argumentFields.get(argument).getText().equals("")){
									JOptionPane.showMessageDialog(ConstraintDialog.this, "Missing value for argument \"" + argument + "\"", "Invalid Argument", JOptionPane.ERROR_MESSAGE);
									return;
								}
								arguments.add(argumentFields.get(argument).getText());
							}
						}
						
						if(view == ConstraintType.NUMBER){
							Number[] numberArguments = new Number[arguments.size()];
							for(int i=0; i<arguments.size(); i++){
								try{
									numberArguments[i] = Integer.parseInt(arguments.get(i));
								} catch(Exception integerException){
									try{
										numberArguments[i] = Double.parseDouble(arguments.get(i));
									} catch(Exception doubleException){
										JOptionPane.showMessageDialog(ConstraintDialog.this, "Argument \"" + arguments.get(i) + "\" does not seem to be a number.", "Invalid Argument", JOptionPane.ERROR_MESSAGE);
										return;
									}
								}
							}
							try {
								if(constraint == null){
									constraint = new NumberConstraint(ConstraintDialog.this.attribute, (NumberOperator) operator, numberArguments);
								} else {
									((NumberConstraint) constraint).setElement(ConstraintDialog.this.attribute);
									((NumberConstraint) constraint).setOperator((NumberOperator) operator);
									((NumberConstraint) constraint).setParameters(numberArguments);
								}
							} catch (ParameterException e1) {
								JOptionPane.showMessageDialog(ConstraintDialog.this, "Cannot create constraint, please check argument values.", "Invalid Argument", JOptionPane.ERROR_MESSAGE);
								return;
							} 
						} else {
							String[] stringArguments = new String[arguments.size()];
							for(int i=0; i<arguments.size(); i++){
								stringArguments[i] = arguments.get(i);
							}
							try {
								if(constraint == null){
									constraint = new StringConstraint(ConstraintDialog.this.attribute, (StringOperator) operator, stringArguments);
								} else {
									((StringConstraint) constraint).setElement(ConstraintDialog.this.attribute);
									((StringConstraint) constraint).setOperator((StringOperator) operator);
									((StringConstraint) constraint).setParameters(stringArguments);
								}
							} catch (ParameterException e1) {
								JOptionPane.showMessageDialog(ConstraintDialog.this, "Cannot create constraint, please check argument values.", "Invalid Argument", JOptionPane.ERROR_MESSAGE);
								return;
							} 
						}
						ConstraintDialog.this.dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						ConstraintDialog.this.dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		if(constraint != null){
			if(constraint instanceof NumberConstraint){
				setView(ConstraintType.NUMBER);
				comboBox.setSelectedIndex(0);
			} else {
				setView(ConstraintType.STRING);
				comboBox.setSelectedIndex(1);
			}
		} else {
			setView(ConstraintType.NUMBER);
			comboBox.setSelectedIndex(0);
		}
		setVisible(true);
	}
	
	private void updateArguments(){
		argumentPanel.removeAll();
		argumentFields.clear();
		Object[] parameters = null;
		if(constraint != null){
			if(constraint instanceof NumberConstraint){
				parameters = ((NumberConstraint) constraint).getParameters();
			} else if(constraint instanceof StringConstraint){
				parameters = ((StringConstraint) constraint).getParameters();
			}
		}
		for(int i=1; i<operator.getRequiredArguments(); i++){
			JTextField argumentField = new JTextField();
			argumentField.setPreferredSize(new Dimension(100, 30));
			argumentField.setBounds(115, (i-1) * (30 + 5), 100, 30);
			if(parameters != null && parameters.length == operator.getRequiredArguments()-1){
				argumentField.setText(parameters[i-1].toString());
			}
			argumentPanel.add(argumentField);
			argumentFields.put(operator.getArgumentNames()[i], argumentField);
			JLabel argumentLabel = new JLabel(operator.getArgumentNames()[i] + ": ");
			argumentLabel.setHorizontalAlignment(JLabel.TRAILING);
			argumentLabel.setBounds(15, (i-1) * (30 + 5), 100, 30);
			argumentPanel.add(argumentLabel);
		}
		argumentPanel.setPreferredSize(new Dimension(230, (operator.getRequiredArguments()-1) * (30 + 5)));
		argumentPanel.repaint();
		ConstraintDialog.this.pack();
	}
	
	private void setView(ConstraintType constraintType){
		this.view = constraintType;
		update();
	}
	
	private void update(){
		if(view == ConstraintType.NUMBER){
			operators.setModel(new DefaultComboBoxModel(NumberOperator.values()));
			operator = NumberOperator.values()[0];
		} else if(view == ConstraintType.STRING){
			operators.setModel(new DefaultComboBoxModel(StringOperator.values()));
			operator = StringOperator.values()[0];
		}
		if(constraint != null){
			operator = constraint.getOperator();
		}
		operators.setSelectedItem(operator);
		operators.setToolTipText(OperatorFormats.getDescriptor(operator));
		updateArguments();
	}
	
	public AbstractConstraint<?> getConstraint(){
		return constraint;
	}
	
	
	public static AbstractConstraint<?> showDialog(Window owner, String attribute, boolean attributeChangeable){
		ConstraintDialog constraintDialog = new ConstraintDialog(owner, attribute, attributeChangeable);
		return constraintDialog.getConstraint();
	}
	
	public static AbstractConstraint<?> showDialog(Window owner, AbstractConstraint<?> constraint, boolean attributeChangeable){
		ConstraintDialog constraintDialog = new ConstraintDialog(owner, constraint, attributeChangeable);
		return constraintDialog.getConstraint();
	}
	
	private enum ConstraintType {NUMBER, STRING}
	
	public class ConstraintListRenderer extends JLabel implements ListCellRenderer {
		public static final long serialVersionUID = 1L;
		
		public ConstraintListRenderer() {
			setOpaque(true);
			setHorizontalAlignment(LEFT);
			setVerticalAlignment(CENTER);
			this.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			
			setText(value.toString());
			if(value instanceof NumberOperator || value instanceof StringOperator){
//				setToolTipText(String.format(((NumberOperator) value).getStringFormat(), value.toString()));
				setToolTipText(((NumberOperator) value).getStringFormat());
			}
					
			return this;

		}

	}

}
