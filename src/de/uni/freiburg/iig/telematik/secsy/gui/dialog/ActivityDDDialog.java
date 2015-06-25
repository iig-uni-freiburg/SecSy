package de.uni.freiburg.iig.telematik.secsy.gui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import de.invation.code.toval.time.TimeScale;
import de.invation.code.toval.time.TimeValue;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.properties.TimeProperties;


public class ActivityDDDialog extends JDialog {

	private static final long serialVersionUID = -5001885740444672961L;

	private final JPanel contentPanel = new JPanel();
	
	private JTextField txtValue;
	private JTextField txtDeviation;

	private JComboBox comboValueScale;
	private JComboBox comboActivity;
	
	private JButton btnOK;
	
	private DefaultComboBoxModel comboActivityModel = null;
	
	private TimeProperties timeProperties;
	
	private DialogType dialogType = null;
	
	public ActivityDDDialog(Window owner, DialogType type, Collection<String> activities, TimeProperties timeProperties) throws ParameterException {
		super(owner);
		
		Validate.notNull(type);
		Validate.notNull(activities);
		Validate.notEmpty(activities);
		Validate.noNullElements(activities);
		Validate.notNull(timeProperties);
		
		this.dialogType = type;
		this.timeProperties = timeProperties;
		setTitle("Set Activity " + type.toString());
		setBounds(100, 100, 329, 189);
		setModal(true);
		setLocationRelativeTo(owner);
		setResizable(false);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JLabel lblValue = new JLabel(type.toString() + ':');
		lblValue.setHorizontalAlignment(SwingConstants.TRAILING);
		lblValue.setBounds(6, 59, 84, 16);
		contentPanel.add(lblValue);
		
		txtValue = new JTextField();
		txtValue.setText("30");
		txtValue.setColumns(10);
		txtValue.setBounds(90, 53, 73, 28);
		contentPanel.add(txtValue);
		
		comboValueScale = new JComboBox();
		comboValueScale.setModel(new DefaultComboBoxModel(new String[] {"MILLISECONDS", "SECONDS", "MINUTES", "HOURS", "DAYS", "WEEKS", "MONTHS", "YEARS"}));
		comboValueScale.setBounds(168, 53, 149, 27);
		comboValueScale.setSelectedIndex(2);
		contentPanel.add(comboValueScale);
		
		txtDeviation = new JTextField();
		txtDeviation.setText("0.0");
		txtDeviation.setColumns(10);
		txtDeviation.setBounds(90, 86, 73, 28);
		contentPanel.add(txtDeviation);
		
		JLabel label = new JLabel("%");
		label.setBounds(168, 92, 20, 16);
		contentPanel.add(label);
		
		JLabel lblDeviation = new JLabel("Deviation:");
		lblDeviation.setHorizontalAlignment(SwingConstants.TRAILING);
		lblDeviation.setBounds(6, 92, 84, 16);
		contentPanel.add(lblDeviation);
		
		comboActivity = new JComboBox();
		comboActivity.setBounds(90, 16, 227, 27);
		comboActivityModel = new DefaultComboBoxModel(activities.toArray());
		comboActivity.setModel(comboActivityModel);
		contentPanel.add(comboActivity);
		
		JLabel lblActivity = new JLabel("Activity:");
		lblActivity.setHorizontalAlignment(SwingConstants.TRAILING);
		lblActivity.setBounds(6, 20, 84, 16);
		contentPanel.add(lblActivity);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(0, 120, 329, 12);
		contentPanel.add(separator);
		
		//Add button panel (bottom)
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		buttonPane.add(getOKButton());
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
		
		checkSelectedActivity();
		setVisible(true);
	}
	
	private JButton getOKButton(){
		if(btnOK == null){
			btnOK = new JButton("OK");
			btnOK.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					Integer value = null;
					try {
						value = Validate.positiveInteger(txtValue.getText());
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(ActivityDDDialog.this, "Affected value: Activity "+dialogType.toString()+"\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					TimeScale scale = TimeScale.valueOf(comboValueScale.getSelectedItem().toString());
					Double deviationPercentage = null;
					try {
						deviationPercentage = Validate.percentage(txtDeviation.getText());
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(ActivityDDDialog.this, "Affected value: Deviation\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					Double deviation = deviationPercentage / 100.0;
					try {
						if(dialogType.equals(DialogType.DURATION_DIALOG)){
							ActivityDDDialog.this.timeProperties.removeIndividualActivityDuration(comboActivity.getSelectedItem().toString());
							if(deviation == 0.0){
								ActivityDDDialog.this.timeProperties.setActivityDuration(comboActivity.getSelectedItem().toString(), value, scale);
							} else {
								ActivityDDDialog.this.timeProperties.setActivityDuration(comboActivity.getSelectedItem().toString(), value, scale, deviation);
							}
						} else {
							ActivityDDDialog.this.timeProperties.removeIndividualActivityDelay(comboActivity.getSelectedItem().toString());
							if(deviation == 0.0){
								ActivityDDDialog.this.timeProperties.setActivityDelay(comboActivity.getSelectedItem().toString(), value, scale);
							} else {
								ActivityDDDialog.this.timeProperties.setActivityDelay(comboActivity.getSelectedItem().toString(), value, scale, deviation);
							}
						}
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(ActivityDDDialog.this, "Error on setting value for: Activity "+dialogType.toString()+"\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					dispose();
				}
			});
			btnOK.setActionCommand("OK");
			getRootPane().setDefaultButton(btnOK);
		}
		return btnOK;
	}
	
	private void checkSelectedActivity(){
		String selectedActivity = comboActivity.getSelectedItem().toString();
		if(selectedActivity == null)
			return;
		try {
			if(dialogType.equals(DialogType.DURATION_DIALOG)){
				if(timeProperties.hasIndivivualDuration(selectedActivity)){
					TimeValue duration = timeProperties.getIndividualActivityDuration(selectedActivity);
					txtValue.setText(duration.getValue().toString());
					comboValueScale.setSelectedItem(duration.getScale());
				}
				if(timeProperties.hasIndividualDurationDeviation(selectedActivity)){
					Double deviation = timeProperties.getIndividualActivityDurationDeviation(selectedActivity);
					DecimalFormat f = new DecimalFormat("#0.00"); 
					txtDeviation.setText(f.format(deviation * 100.0));
				}
			} else {
				if(timeProperties.hasIndividualDelay(selectedActivity)){
					TimeValue delay = timeProperties.getIndividualActivityDelay(selectedActivity);
					txtValue.setText(delay.getValue().toString());
					comboValueScale.setSelectedItem(delay.getScale());
				}
				if(timeProperties.hasIndividualDelayDeviation(selectedActivity)){
					Double deviation = timeProperties.getIndividualActivityDelayDeviation(selectedActivity);
					DecimalFormat f = new DecimalFormat("#0.00"); 
					txtDeviation.setText(f.format(deviation * 100.0));
				}
			}
		} catch (Exception e) {
			return;
		}
	}
	
	
	//------- STARTUP --------------------------------------------------------------------------------
	

	public static void showActivityDDDialog(Window owner, DialogType dialogType, Collection<String> activities, TimeProperties timeProperties) throws ParameterException{
		new ActivityDDDialog(owner, dialogType, activities, timeProperties);
	}
	
	public static void showActivityDDDialog(Window owner, DialogType dialogType, String activity, TimeProperties timeProperties) throws ParameterException{
		new ActivityDDDialog(owner, dialogType, Arrays.asList(activity), timeProperties);
	}
	
	public static void main(String[] args) {
		try {
			List<String> activities = new ArrayList<String>();
			activities.add("act1");
			activities.add("act2");
			activities.add("act3");
			ActivityDDDialog.showActivityDDDialog(null, DialogType.DURATION_DIALOG, activities, new TimeProperties());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
