package gui.dialog;

import gui.Hints;
import gui.misc.CustomListRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

import logic.generator.time.properties.TimeProperties;
import properties.PropertyException;
import time.TimeScale;
import time.TimeValue;
import validate.ParameterException;
import validate.Validate;

public class ActivityDDAllDialog extends JDialog {
	
	private static final long serialVersionUID = 6589034118115595410L;
	
	private final String valueFormatWithDeviation = "%s: %s with %s deviation";
	private final String valueFormatWithoutDeviation = "%s: %s with no deviation";

	private JPanel mainPanel = null;
	private JPanel panelDefaultFixed;
	private JPanel panelDefaultBounded;
	
	private JTextField txtDefaultFixedValue;
	private JTextField txtDefaultFixedDeviation;
	private JTextField txtDefaultBoundedMinDelay;
	private JTextField txtDefaultBoundedMaxDelay;
	
	private JComboBox comboDefaultValueScale;
	private JComboBox comboMinDelayScale;
	private JComboBox comboMaxDelayScale;
	
	private JButton btnAdd;
	private JButton btnEdit;
	private JButton btnOK;
	private JButton btnApplyFixed;
	private JButton btnApplyBounded;
	
	private ButtonGroup buttonGroup = new ButtonGroup();
	
	private JRadioButton rdbtnDefaultDelayFixed;
	private JRadioButton rdbtnDefaultDelayBounded;
	
	private JLabel lblDefaultFixedDeviation;
	private JLabel lblDefaultFixedValue;
	private JLabel lblPercent;
	private JLabel lblMinimum;
	private JLabel lblMaximum;
	
	private JList valueList;
	private DefaultListModel valueListModel = new DefaultListModel();

	private TimeProperties timeProperties = null;
	private List<String> activityCandidates = new ArrayList<String>();
	private DialogType dialogType = null;
	private HashMap<String,String> activityListEntryMap = new HashMap<String,String>();
	
	
	public ActivityDDAllDialog(Window owner, DialogType dialogType, Collection<String> activities, TimeProperties timeProperties) throws ParameterException {
		super(owner);
		setBounds(100, 100, 467, 592);
		setModal(true);
		setLocationRelativeTo(owner);
		setResizable(false);
//		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		this.dialogType = dialogType;
		Validate.notNull(activities);
		Validate.notEmpty(activities);
		Validate.noNullElements(activities);
		activityCandidates.addAll(activities);
		Validate.notNull(timeProperties);
		this.timeProperties = timeProperties;
				
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(getMainPanel(), BorderLayout.CENTER);
		
		initializeFields();
		
		setVisible(true);
	}
	
	private JPanel getMainPanel(){
		if(mainPanel == null){
			mainPanel = new JPanel();
			mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			mainPanel.setLayout(null);

			JLabel lblActivityDelay = new JLabel("Individual "+dialogType.toString().toLowerCase()+"s:");
			lblActivityDelay.setBounds(25, 16, 130, 16);
			mainPanel.add(lblActivityDelay);
			
			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPane_1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane_1.setBounds(25, 41, 418, 198);
			mainPanel.add(scrollPane_1);
			
			scrollPane_1.setViewportView(getValueList());
			
			mainPanel.add(getAddButton());
			
			mainPanel.add(getEditButton());
			
			rdbtnDefaultDelayFixed = new JRadioButton("Default "+dialogType.toString().toLowerCase()+" fixed");
			rdbtnDefaultDelayFixed.setBounds(35, 297, 180, 23);
			rdbtnDefaultDelayFixed.setSelected(true);
			rdbtnDefaultDelayFixed.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					updateDefaultValuePanelVisibility();
				}
			});
			buttonGroup.add(rdbtnDefaultDelayFixed);
			mainPanel.add(rdbtnDefaultDelayFixed);
			
			mainPanel.add(getDefaultFixedPanel());
			
			rdbtnDefaultDelayBounded = new JRadioButton("Default "+dialogType.toString().toLowerCase()+" bounded");
			rdbtnDefaultDelayBounded.setBounds(35, 412, 211, 23);
			rdbtnDefaultDelayBounded.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					updateDefaultValuePanelVisibility();
				}
			});
			mainPanel.add(rdbtnDefaultDelayBounded);
			buttonGroup.add(rdbtnDefaultDelayBounded);
			
			mainPanel.add(getDefaultBoundedPanel());
			
			//Add Button panel (bottom)
			JPanel buttonPane = new JPanel();
			buttonPane.setBorder(new LineBorder(new Color(0, 0, 0)));
			buttonPane.setBounds(-10, 530, 483, 42);
			buttonPane.setLayout(null);
			buttonPane.add(getOKButton());
			mainPanel.add(buttonPane);
			
			JButton btnRemove = new JButton("Remove");
			btnRemove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(valueList.getSelectedValues() != null){
						for(Object selectedValue: valueList.getSelectedValues()){
							valueListModel.removeElement(selectedValue);
							removeActivityValue(activityListEntryMap.get(selectedValue));
						}
					}
				}
			});
			btnRemove.setBounds(191, 248, 80, 28);
			mainPanel.add(btnRemove);
			
		}
		return mainPanel;
	}
	
	private JPanel getDefaultFixedPanel(){
		if(panelDefaultFixed == null){
			panelDefaultFixed = new JPanel();
			panelDefaultFixed.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panelDefaultFixed.setBounds(25, 288, 418, 114);
			panelDefaultFixed.setLayout(null);
			
			txtDefaultFixedValue = new JTextField();
			txtDefaultFixedValue.setText("0");
			txtDefaultFixedValue.setBounds(105, 44, 63, 28);
			panelDefaultFixed.add(txtDefaultFixedValue);
			txtDefaultFixedValue.setColumns(10);
			txtDefaultFixedValue.setToolTipText(Hints.hintDefaultDelay);
//			txtDefaultFixedDelay.setText(timeProperties.getDefaultActivityDelay().getValue().toString());
			
			comboDefaultValueScale = new JComboBox();
			comboDefaultValueScale.setBounds(180, 44, 149, 28);
			panelDefaultFixed.add(comboDefaultValueScale);
			comboDefaultValueScale.setModel(new DefaultComboBoxModel(new String[] {"MILLISECONDS", "SECONDS", "MINUTES", "HOURS", "DAYS", "WEEKS", "MONTHS", "YEARS"}));
//			comboDefaultDelayScale.setSelectedItem(timeProperties.getDefaultActivityDelay().getScale().toString());
			
			
			panelDefaultFixed.add(getApplyDefaultFixedButton());
			
			txtDefaultFixedDeviation = new JTextField();
			txtDefaultFixedDeviation.setText("0.0");
			txtDefaultFixedDeviation.setColumns(10);
			txtDefaultFixedDeviation.setBounds(105, 76, 63, 28);
			panelDefaultFixed.add(txtDefaultFixedDeviation);
			
			
			lblDefaultFixedValue = new JLabel("Delay:");
			lblDefaultFixedValue.setHorizontalAlignment(SwingConstants.TRAILING);
			lblDefaultFixedValue.setBounds(20, 50, 84, 16);
			panelDefaultFixed.add(lblDefaultFixedValue);
			
			lblPercent = new JLabel("%");
			lblPercent.setBounds(170, 83, 20, 16);
			panelDefaultFixed.add(lblPercent);
			
			lblDefaultFixedDeviation = new JLabel("Deviation:");
			lblDefaultFixedDeviation.setHorizontalAlignment(SwingConstants.TRAILING);
			lblDefaultFixedDeviation.setBounds(20, 82, 84, 16);
			panelDefaultFixed.add(lblDefaultFixedDeviation);
		}
		return panelDefaultFixed;
	}
	
	private JPanel getDefaultBoundedPanel(){
		if(panelDefaultBounded == null){
			panelDefaultBounded = new JPanel();
			panelDefaultBounded.setBounds(25, 404, 418, 114);
			panelDefaultBounded.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panelDefaultBounded.setLayout(null);
			
			txtDefaultBoundedMinDelay = new JTextField();
			txtDefaultBoundedMinDelay.setText("0");
			txtDefaultBoundedMinDelay.setEnabled(false);
			txtDefaultBoundedMinDelay.setBounds(105, 44, 63, 28);
			panelDefaultBounded.add(txtDefaultBoundedMinDelay);
			txtDefaultBoundedMinDelay.setColumns(10);
			
			comboMinDelayScale = new JComboBox();
			comboMinDelayScale.setEnabled(false);
			comboMinDelayScale.setBounds(180, 44, 149, 28);
			panelDefaultBounded.add(comboMinDelayScale);
			comboMinDelayScale.setModel(new DefaultComboBoxModel(new String[] {"MILLISECONDS", "SECONDS", "MINUTES", "HOURS", "DAYS", "WEEKS", "MONTHS", "YEARS"}));
			
			txtDefaultBoundedMaxDelay = new JTextField();
			txtDefaultBoundedMaxDelay.setText("0");
			txtDefaultBoundedMaxDelay.setEnabled(false);
			txtDefaultBoundedMaxDelay.setBounds(105, 78, 63, 28);
			panelDefaultBounded.add(txtDefaultBoundedMaxDelay);
			txtDefaultBoundedMaxDelay.setColumns(10);
			
			comboMaxDelayScale = new JComboBox();
			comboMaxDelayScale.setEnabled(false);
			comboMaxDelayScale.setBounds(180, 78, 149, 28);
			panelDefaultBounded.add(comboMaxDelayScale);
			comboMaxDelayScale.setModel(new DefaultComboBoxModel(new String[] {"MILLISECONDS", "SECONDS", "MINUTES", "HOURS", "DAYS", "WEEKS", "MONTHS", "YEARS"}));
			
			lblMinimum = new JLabel("Minimum:");
			lblMinimum.setEnabled(false);
			lblMinimum.setBounds(30, 50, 74, 16);
			panelDefaultBounded.add(lblMinimum);
			lblMinimum.setHorizontalAlignment(SwingConstants.TRAILING);
			
			lblMaximum = new JLabel("Maximum:");
			lblMaximum.setEnabled(false);
			lblMaximum.setBounds(30, 84, 73, 16);
			panelDefaultBounded.add(lblMaximum);
			lblMaximum.setHorizontalAlignment(SwingConstants.TRAILING);
			
			panelDefaultBounded.add(getApplyDefaultBoundedButton());
			panelDefaultBounded.setEnabled(false);
		}
		return panelDefaultBounded;
	}
	
	private JButton getAddButton(){
		if(btnAdd == null){
			btnAdd = new JButton("Add");
			btnAdd.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					List<String> activities = new ArrayList<String>(activityCandidates);
					try {
						if(dialogType.equals(DialogType.Duration)){
							activities.removeAll(ActivityDDAllDialog.this.timeProperties.getActivitiesWithIndividualDuration());
						} else {
							activities.removeAll(ActivityDDAllDialog.this.timeProperties.getActivitiesWithIndividualDelay());
						}
						if(activities.isEmpty())
							return;
						ActivityDDDialog.showActivityDDDialog(ActivityDDAllDialog.this, dialogType, activities, ActivityDDAllDialog.this.timeProperties);
						updateValueList();
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(ActivityDDAllDialog.this, "Error on launching activity "+dialogType.toString().toLowerCase()+" dialog\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			});
			btnAdd.setBounds(25, 248, 80, 28);
		}
		return btnAdd;
	}
	
	private JButton getEditButton(){
		if(btnEdit == null){
			btnEdit = new JButton("Edit");
			btnEdit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(valueList.getSelectedValues().length == 0 || valueList.getSelectedValues().length > 1){
						return;
					}
					
					try {
						ActivityDDDialog.showActivityDDDialog(ActivityDDAllDialog.this, dialogType, activityCandidates.get(valueList.getSelectedIndex()), ActivityDDAllDialog.this.timeProperties);
						updateValueList();
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(ActivityDDAllDialog.this, "Error on launching activity duration dialog\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			});
			btnEdit.setBounds(107, 248, 80, 28);
		}
		return btnEdit;
	}
	
	private JButton getOKButton(){
		if(btnOK == null){
			btnOK = new JButton("OK");
			btnOK.setBounds(199, 6, 75, 28);
			btnOK.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(!rdbtnDefaultDelayBounded.isSelected()){
						if(dialogType.equals(DialogType.Duration)){
							timeProperties.removeDefaultActivityDurationBounds();
						} else {
							timeProperties.removeDefaultActivityDelayBounds();
						}
					}
					dispose();
				}
			});
			btnOK.setActionCommand("OK");
			getRootPane().setDefaultButton(btnOK);
		}
		return btnOK;
	}
	
	private JButton getApplyDefaultFixedButton(){
		if(btnApplyFixed == null){
			btnApplyFixed = new JButton("Apply");
			btnApplyFixed.setBounds(332, 44, 73, 28);
			btnApplyFixed.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Integer defaultValue;
					try {
						defaultValue = Validate.positiveInteger(txtDefaultFixedValue.getText());
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(ActivityDDAllDialog.this, "Affected value: Default "+dialogType.toString().toLowerCase()+"\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					TimeScale defaultValueScale = TimeScale.valueOf(comboDefaultValueScale.getSelectedItem().toString());
					Double deviationPercentage = null;
					try {
						deviationPercentage = Validate.percentage(txtDefaultFixedDeviation.getText());
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(ActivityDDAllDialog.this, "Affected value: Deviation\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					Double deviation = deviationPercentage / 100.0;
					
					try {
						if(dialogType.equals(DialogType.Duration)){
							if(deviation == 0.0){
								ActivityDDAllDialog.this.timeProperties.setDefaultActivityDuration(defaultValue, defaultValueScale);
							} else {
								ActivityDDAllDialog.this.timeProperties.setDefaultActivityDuration(defaultValue, defaultValueScale, deviation);
							}
						} else {
							if(deviation == 0.0){
								ActivityDDAllDialog.this.timeProperties.setDefaultActivityDelay(defaultValue, defaultValueScale);
							} else {
								ActivityDDAllDialog.this.timeProperties.setDefaultActivityDelay(defaultValue, defaultValueScale, deviation);
							}
						}
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(ActivityDDAllDialog.this, "Error on setting value for: Default "+dialogType.toString().toLowerCase()+"\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
				}
			});
		}
		return btnApplyFixed;
	}
	
	private JButton getApplyDefaultBoundedButton(){
		if(btnApplyBounded == null){
			btnApplyBounded = new JButton("Apply");
			btnApplyBounded.setEnabled(false);
			btnApplyBounded.setBounds(332, 78, 73, 28);
			btnApplyBounded.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					Integer minValue = null;
					try {
						minValue = Validate.notNegativeInteger(txtDefaultBoundedMinDelay.getText());
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(ActivityDDAllDialog.this, "Affected value: Minimum default "+dialogType.toString().toLowerCase()+"\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					TimeScale minValueScale = TimeScale.valueOf(comboMinDelayScale.getSelectedItem().toString());
					
					Integer maxValue = null;
					try {
						maxValue = Validate.notNegativeInteger(txtDefaultBoundedMaxDelay.getText());
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(ActivityDDAllDialog.this, "Affected value: Maximum default "+dialogType.toString().toLowerCase()+"\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					TimeScale maxValueScale = TimeScale.valueOf(comboMaxDelayScale.getSelectedItem().toString());
					
					try {
						TimeValue minTimeValue = new TimeValue(minValue, minValueScale);
						TimeValue maxTimeValue = new TimeValue(maxValue, maxValueScale);

						if(!maxTimeValue.isBiggerThan(minTimeValue)){
							JOptionPane.showMessageDialog(ActivityDDAllDialog.this, "Affected value: Default "+dialogType.toString().toLowerCase()+" bound\nReason: Minimum is bigger (equal) maximum", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
							return;
						}
					} catch (ParameterException e2) {
						JOptionPane.showMessageDialog(ActivityDDAllDialog.this, "Error on setting value for: Default "+dialogType.toString().toLowerCase()+" bound\nReason: Cannot create time values for min/max comparison.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					try {
						if(dialogType.equals(DialogType.Duration)){
							ActivityDDAllDialog.this.timeProperties.setDefaultActivityDurationBounds(minValue, minValueScale, maxValue, maxValueScale);
						} else {
							ActivityDDAllDialog.this.timeProperties.setDefaultActivityDelayBounds(minValue, minValueScale, maxValue, maxValueScale);
						}
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(ActivityDDAllDialog.this, "Error on setting value for: Default "+dialogType.toString().toLowerCase()+" bound\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
				}
			});
		}
		return btnApplyBounded;
	}
	
	private String getValueString(String activity){
		if(dialogType.equals(DialogType.Duration)){
			try {
				if(timeProperties.hasIndivivualDuration(activity)){
					if(!timeProperties.hasIndividualDurationDeviation(activity)){
						return String.format(valueFormatWithoutDeviation, activity, timeProperties.getIndividualActivityDuration(activity));
					} else {
						DecimalFormat f = new DecimalFormat("#0.00"); 
						return String.format(valueFormatWithDeviation, activity, timeProperties.getIndividualActivityDuration(activity), f.format(timeProperties.getIndividualActivityDurationDeviation(activity)*100.0));
					}
				}
			} catch (ParameterException e) {
				JOptionPane.showMessageDialog(ActivityDDAllDialog.this, "Parameter exception on extracting duration for activity \""+activity+"\" :\n" + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
				return null;
			} catch (PropertyException e) {
				JOptionPane.showMessageDialog(ActivityDDAllDialog.this, "Cannot extract duration property for activity \""+activity+"\":\n" + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
				return null;
			}
		} else {
			try {
				if(timeProperties.hasIndividualDelay(activity)){
					if(!timeProperties.hasIndividualDelayDeviation(activity)){
						return String.format(valueFormatWithoutDeviation, activity, timeProperties.getIndividualActivityDelay(activity));
					} else {
						DecimalFormat f = new DecimalFormat("#0.00"); 
						return String.format(valueFormatWithDeviation, activity, timeProperties.getIndividualActivityDelay(activity), f.format(timeProperties.getIndividualActivityDelayDeviation(activity)*100.0));
					}
				}
			} catch (ParameterException e) {
				JOptionPane.showMessageDialog(ActivityDDAllDialog.this, "Parameter exception on extracting delay for activity \""+activity+"\" :\n" + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
				return null;
			} catch (PropertyException e) {
				JOptionPane.showMessageDialog(ActivityDDAllDialog.this, "Cannot extract delay property for activity \""+activity+"\":\n" + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}
		return null;
	}
	
	private JList getValueList(){
		if(valueList == null){
			valueList = new JList(valueListModel);
			valueList.setCellRenderer(new CustomListRenderer());
			valueList.setFixedCellHeight(20);
			valueList.setVisibleRowCount(10);
			valueList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			valueList.setBorder(null);
			
			valueList.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_DELETE){
						if(valueList.getSelectedValues() != null){
							for(Object selectedValue: valueList.getSelectedValues()){
								valueListModel.removeElement(selectedValue);
								removeActivityValue(activityListEntryMap.get(selectedValue));
							}
						}
					}
				}
				
				@Override
				public void keyPressed(KeyEvent e) {}
			});
		}
		return valueList;
	}
	
	private void removeActivityValue(String activity){
		try {
			if(dialogType.equals(DialogType.Duration)){
				ActivityDDAllDialog.this.timeProperties.removeIndividualActivityDuration(activity);
			} else {
				ActivityDDAllDialog.this.timeProperties.removeIndividualActivityDelay(activity);
			}
		} catch (ParameterException e1) {
			JOptionPane.showMessageDialog(ActivityDDAllDialog.this, "Error on removing "+dialogType.toString()+" for activity"+activity+"\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
	
	private void updateValueList(){
		activityListEntryMap.clear();
		valueListModel.clear();
		for(String activity: activityCandidates){
			String valueString = getValueString(activity);
			if(valueString != null){
				valueListModel.addElement(valueString);
				activityListEntryMap.put(valueString, activity);
			}
		}
		valueList.setSelectedIndex(0);
	}
	
	private void updateDefaultValuePanelVisibility(){
		boolean fixedVisibility = rdbtnDefaultDelayFixed.isSelected();
		lblDefaultFixedValue.setEnabled(fixedVisibility);
		lblDefaultFixedDeviation.setEnabled(fixedVisibility);
		txtDefaultFixedValue.setEnabled(fixedVisibility);
		txtDefaultFixedDeviation.setEnabled(fixedVisibility);
		lblPercent.setEnabled(fixedVisibility);
		comboDefaultValueScale.setEnabled(fixedVisibility);
		btnApplyFixed.setEnabled(fixedVisibility);
		boolean boundedVisibility = rdbtnDefaultDelayBounded.isSelected();
		lblMinimum.setEnabled(boundedVisibility);
		lblMaximum.setEnabled(boundedVisibility);
		txtDefaultBoundedMinDelay.setEnabled(boundedVisibility);
		txtDefaultBoundedMaxDelay.setEnabled(boundedVisibility);
		comboMinDelayScale.setEnabled(boundedVisibility);
		comboMaxDelayScale.setEnabled(boundedVisibility);
		btnApplyBounded.setEnabled(boundedVisibility);
	}
	
	private void initializeFields(){
		updateValueList();
		
		TimeValue defaultValue = null;
		TimeValue defaultMinValue = new TimeValue();
		TimeValue defaultMaxValue = new TimeValue();
		Double defaultDeviation = null;
		try {
			if(dialogType.equals(DialogType.Duration)){
				defaultValue = timeProperties.getDefaultActivityDuration();
				defaultDeviation = timeProperties.getDefaultActivityDurationDeviation();
				if(timeProperties.existDefaultActivityDurationBounds()){
					rdbtnDefaultDelayBounded.setSelected(true);
					defaultMinValue = timeProperties.getDefaultActivityMinDuration();
					defaultMaxValue = timeProperties.getDefaultActivityMaxDuration();
				}
			} else {
				defaultValue = timeProperties.getDefaultActivityDelay();
				defaultDeviation = timeProperties.getDefaultActivityDelayDeviation();
				if(timeProperties.existDefaultActivityDelayBounds()){
					rdbtnDefaultDelayBounded.setSelected(true);
					defaultMinValue = timeProperties.getDefaultActivityMinDelay();
					defaultMaxValue = timeProperties.getDefaultActivityMaxDelay();
				}
			}
		} catch (PropertyException e) {
			JOptionPane.showMessageDialog(ActivityDDAllDialog.this, "Error on initializing fields \nReason: " + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
			return;
		} catch (ParameterException e) {
			JOptionPane.showMessageDialog(ActivityDDAllDialog.this, "Error on initializing fields \nReason: " + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
			return;
		}
		txtDefaultFixedValue.setText(defaultValue.getValue().toString());
		comboDefaultValueScale.setSelectedItem(defaultValue.getScale().toString());
		txtDefaultFixedDeviation.setText(String.valueOf(defaultDeviation*100));
		txtDefaultBoundedMinDelay.setText(defaultMinValue.getValue().toString());
		comboMinDelayScale.setSelectedItem(defaultMinValue.getScale().toString().toUpperCase());
		txtDefaultBoundedMaxDelay.setText(defaultMaxValue.getValue().toString());
		comboMaxDelayScale.setSelectedItem(defaultMaxValue.getScale().toString().toUpperCase());
	}
	
	
	public static void showDialog(Window owner, DialogType dialogType, Collection<String> activities, TimeProperties timeProperties) throws ParameterException{
		new ActivityDDAllDialog(owner, dialogType, activities, timeProperties);
	}
	
}
