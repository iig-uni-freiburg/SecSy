package de.uni.freiburg.iig.telematik.secsy.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import de.invation.code.toval.misc.valuegeneration.StochasticValueGenerator;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;


public class ValueProbabilityDialog extends JDialog {
	
	private static final long serialVersionUID = -4615306286690321678L;
	
	private final JPanel contentPanel = new JPanel();
	
	private JTextField txtValue;
	private JTextField txtProbability;
	
	@SuppressWarnings("rawtypes")
	private StochasticValueGenerator valueGenerator = null;
	@SuppressWarnings("rawtypes")
	private Class valueType = null;
	

	@SuppressWarnings("rawtypes")
	public ValueProbabilityDialog(Window owner, StochasticValueGenerator valueGenerator, Class valueType) throws ParameterException {
		super(owner);
		Validate.notNull(valueGenerator);
		this.valueGenerator = valueGenerator;
		Validate.notNull(valueType);
		this.valueType = valueType;
		setUpGUI(owner);
	}
	
	@SuppressWarnings("rawtypes")
	public ValueProbabilityDialog(Window owner, Class valueType) throws ParameterException {
		super(owner);
		Validate.notNull(valueType);
		this.valueType = valueType;
		
		if(valueType.equals(String.class)){
			valueGenerator = new StochasticValueGenerator<String>();
		}
		if(valueType.equals(Integer.class)){
			valueGenerator = new StochasticValueGenerator<Integer>();
		}
		if(valueType.equals(Double.class)){
			valueGenerator = new StochasticValueGenerator<Double>();
		}
		setUpGUI(owner);
	}
		
		
	
	private void setUpGUI(Window owner) throws ParameterException{
		
		setResizable(false);
		setBounds(100, 100, 241, 165);
		setModal(true);
		setLocationRelativeTo(owner);
		setTitle("New Value Probability");
		
		addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {}
			
			@Override
			public void windowIconified(WindowEvent e) {}
			
			@Override
			public void windowDeiconified(WindowEvent e) {}
			
			@Override
			public void windowDeactivated(WindowEvent e) {}
			
			@Override
			public void windowClosing(WindowEvent e) {
//				valueGenerator = null;
			}
			
			@Override
			public void windowClosed(WindowEvent e) {}
			
			@Override
			public void windowActivated(WindowEvent e) {}
		});
		
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		{
			JButton btnDone = new JButton("OK");
			getRootPane().setDefaultButton(btnDone);
			btnDone.addActionListener(new ActionListener() {
				@SuppressWarnings("unchecked")
				public void actionPerformed(ActionEvent e) {
					if(txtValue.getText() == null || txtValue.getText().isEmpty()){
						JOptionPane.showMessageDialog(ValueProbabilityDialog.this, "Value field is empty.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					Object valueObject = null;
					try{
						valueObject = valueType.getDeclaredConstructor(String.class).newInstance(txtValue.getText());
					} catch(Exception e1){
						JOptionPane.showMessageDialog(ValueProbabilityDialog.this, "Value field content is of wrong type.\nExpected type: " + valueType.getSimpleName(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					try{
						Validate.percentage(txtProbability.getText());
					} catch(Exception e1){
						JOptionPane.showMessageDialog(ValueProbabilityDialog.this, "Probability field content is invalid.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					Double probability = null;
					try{
						probability = Double.parseDouble(txtProbability.getText());
						probability = probability / 100.0;
					}catch(Exception e1){
						JOptionPane.showMessageDialog(ValueProbabilityDialog.this, "Cannot extract value of probability field.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(probability == 0){
						JOptionPane.showMessageDialog(ValueProbabilityDialog.this, "Probability must not be 0.0", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					try {
						valueGenerator.addProbability(valueObject, probability);
					} catch(Exception e1){
						JOptionPane.showMessageDialog(ValueProbabilityDialog.this, "Cannot add value probability to value generator.\nReason: "+e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					dispose();
				}
			});
			btnDone.setBounds(85, 108, 80, 29);
			contentPanel.add(btnDone);
		}
		
		JLabel lblName = new JLabel("Value:");
		lblName.setHorizontalAlignment(SwingConstants.TRAILING);
		lblName.setBounds(17, 22, 86, 16);
		contentPanel.add(lblName);
		
		txtValue = new JTextField();
		txtValue.setBounds(114, 16, 109, 28);
		contentPanel.add(txtValue);
		txtValue.setColumns(10);
		
		txtProbability = new JTextField();
		txtProbability.setText("0.0");
		txtProbability.setColumns(10);
		txtProbability.setBounds(114, 50, 66, 28);
		contentPanel.add(txtProbability);
		
		JLabel lblProbability = new JLabel("Probability:");
		lblProbability.setHorizontalAlignment(SwingConstants.TRAILING);
		lblProbability.setBounds(17, 56, 86, 16);
		contentPanel.add(lblProbability);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(-13, 90, 277, 12);
		contentPanel.add(separator);
		
		JLabel label = new JLabel("%");
		label.setBounds(184, 56, 36, 16);
		contentPanel.add(label);
		setVisible(true);
	}
	
	public StochasticValueGenerator<?> getValueGenerator(){
		return valueGenerator;
	}
	
	@SuppressWarnings("rawtypes")
	public static void showDialog(Window owner, StochasticValueGenerator valueGenerator, Class valueType) throws ParameterException{
		new ValueProbabilityDialog(owner, valueGenerator, valueType);
	}
	
	@SuppressWarnings("rawtypes")
	public static StochasticValueGenerator<?> showDialog(Window owner, Class valueType) throws ParameterException{
		ValueProbabilityDialog dialog = new ValueProbabilityDialog(owner, valueType);
		return dialog.getValueGenerator();
	}
	
}
