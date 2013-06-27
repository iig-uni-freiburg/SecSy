package gui.dialog;

import gui.SimulationComponents;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.time.Weekday;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;

import logic.generator.time.properties.TimeProperties;
import logic.generator.time.properties.TimeProperties.CaseStartPrecision;

public class TimeGeneratorDialog extends JDialog {

	private static final long serialVersionUID = 8543128527621285140L;
	
	private final JPanel contentPanel = new JPanel();
	private JTextField txtCasesPerDay;
	private JTextField txtOfficeHoursStart;
	private JTextField txtOfficeHoursEnd;
	private JTextField txtDeviationPercentage;
	
	private TimeProperties timeProperties = null;
	
	private JButton btnSetActivityDurations;
	private JButton btnSetActivityDelays;
	private JTextField txtName;
	
	private JCheckBox chckbxSkipWeekend;
	private JTextField txtStartDate;
	private JComboBox comboDateFormat;
	private JButton btnOK;
	private JButton btnCancel;
	private JComboBox comboPrecision;
	
	private Date oldStartDate = null;
	private SimpleDateFormat dateFormat = null;
	private JList listOfficeDays;
	private DefaultListModel officeDayListModel = null;
	
	private boolean editMode = false;
	
	private Collection<String> activities = null;
	
	/**
	 * @throws ParameterException 
	 * @wbp.parser.constructor
	 */
	public TimeGeneratorDialog(Window owner, Collection<String> activities) throws ParameterException {
		this(owner, activities, new TimeProperties(), false);
	}
	
	public TimeGeneratorDialog(Window owner, Collection<String> activities, TimeProperties timeProperties) throws ParameterException {
		this(owner, activities, timeProperties, true);
	}

	private TimeGeneratorDialog(Window owner, Collection<String> activities, TimeProperties timeProperties, boolean editMode) throws ParameterException {
		super(owner);
		Validate.notNull(timeProperties);
		Validate.notNull(activities);
		Validate.noNullElements(activities);
		
		this.activities = activities;
		
		this.editMode = editMode;
		if(editMode){
			this.timeProperties = timeProperties.clone();
		} else {
			this.timeProperties = timeProperties;
		}
		
		setUpGUI(owner);
	}
	
	private void setUpGUI(Window owner){
		setBounds(100, 100, 408, 498);
		setModal(true);
		setLocationRelativeTo(owner);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		setTitle("New Case Time Generator");
		
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
				if(!TimeGeneratorDialog.this.editMode){
					TimeGeneratorDialog.this.timeProperties = null;
				}
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
		
		txtCasesPerDay = new JTextField();
		txtCasesPerDay.setText("10");
		txtCasesPerDay.setColumns(10);
		txtCasesPerDay.setBounds(121, 151, 69, 28);
		contentPanel.add(txtCasesPerDay);
		
		comboPrecision = new JComboBox();
		comboPrecision.setModel(new DefaultComboBoxModel(new String[] {"HOUR", "MINUTE", "SECOND", "MILLISECOND"}));
		comboPrecision.setBounds(162, 115, 146, 27);
		contentPanel.add(comboPrecision);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(180, 240, 143, 91);
		contentPanel.add(scrollPane);
		
		listOfficeDays = new JList();
		officeDayListModel = new DefaultListModel();
		officeDayListModel.addElement("Monday");
		officeDayListModel.addElement("Tuesday");
		officeDayListModel.addElement("Wednesday");
		officeDayListModel.addElement("Thursday");
		officeDayListModel.addElement("Friday");
		listOfficeDays.setModel(officeDayListModel);
		listOfficeDays.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_DELETE){
					for(Object selectedValue: listOfficeDays.getSelectedValues())
						officeDayListModel.removeElement(selectedValue);
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {}
		});
		scrollPane.setColumnHeaderView(listOfficeDays);
		
		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<String> weekdays = Weekday.stringValues();
				for(Enumeration<?> elements = officeDayListModel.elements(); elements.hasMoreElements();)
					weekdays.remove(elements.nextElement().toString());
				if(!weekdays.isEmpty()){
					List<String> newWeekdays = ValueChooserDialog.showDialog(TimeGeneratorDialog.this, "Add weekdays", weekdays, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					if(newWeekdays != null && !newWeekdays.isEmpty()){
						for(String newWeekday: newWeekdays){
							if(chckbxSkipWeekend.isSelected() && (newWeekday.equals("Saturday") || newWeekday.equals("Sunday")))
								continue;
							officeDayListModel.addElement(newWeekday);
						}
					}
				}
			}
		});
		btnAdd.setBounds(325, 237, 69, 29);
		contentPanel.add(btnAdd);
		
		chckbxSkipWeekend = new JCheckBox("Skip weekend");
		chckbxSkipWeekend.setSelected(true);
		chckbxSkipWeekend.setBounds(180, 331, 128, 23);
		chckbxSkipWeekend.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if(chckbxSkipWeekend.isSelected()){
					officeDayListModel.removeElement("Saturday");
					officeDayListModel.removeElement("Sunday");
				} else {
					if(!officeDayListModel.contains("Saturday"))
						officeDayListModel.addElement("Saturday");
					if(!officeDayListModel.contains("Sunday"))
						officeDayListModel.addElement("Sunday");
				}
			}
		});
		contentPanel.add(chckbxSkipWeekend);
		
		txtOfficeHoursStart = new JTextField();
		txtOfficeHoursStart.setText("8");
		txtOfficeHoursStart.setColumns(10);
		txtOfficeHoursStart.setBounds(105, 240, 48, 28);
		contentPanel.add(txtOfficeHoursStart);
		
		txtOfficeHoursEnd = new JTextField();
		txtOfficeHoursEnd.setText("18");
		txtOfficeHoursEnd.setColumns(10);
		txtOfficeHoursEnd.setBounds(105, 268, 48, 28);
		contentPanel.add(txtOfficeHoursEnd);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(18, 191, 376, 12);
		contentPanel.add(separator);
		
		txtDeviationPercentage = new JTextField();
		txtDeviationPercentage.setText("0.0");
		txtDeviationPercentage.setColumns(10);
		txtDeviationPercentage.setBounds(262, 151, 48, 28);
		contentPanel.add(txtDeviationPercentage);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(18, 368, 376, 12);
		contentPanel.add(separator_1);
		
		contentPanel.add(getSetActivityDurationsButton());
		
		contentPanel.add(getSetActivityDelaysButton());
		
		txtName = new JTextField();
		txtName.setText("NewCaseTimeGenerator");
		txtName.setColumns(10);
		txtName.setBounds(94, 18, 178, 28);
		contentPanel.add(txtName);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setBounds(18, 52, 376, 12);
		contentPanel.add(separator_2);
		
		comboDateFormat = new JComboBox();
		comboDateFormat.setModel(new DefaultComboBoxModel(new String[] {"yyyy.MM.dd", "dd.MM.yyyy", "MM.dd.yyyy", "yyyy-MM-dd", "dd-MM-yyyy", "MM-dd-yyyy"}));
		comboDateFormat.setSelectedIndex(1);
		applyDateFormat();
		comboDateFormat.setBounds(224, 77, 138, 27);
		comboDateFormat.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if(txtStartDate.getText().isEmpty())
					return;
				if(arg0.getStateChange() == ItemEvent.DESELECTED){
					try {
						oldStartDate = getStartDate();
					} catch (ParseException e) {
						oldStartDate = null;
					}
				} else if(arg0.getStateChange() == ItemEvent.SELECTED){
					applyDateFormat();
					if(oldStartDate == null)
						return;
					txtStartDate.setText(dateFormat.format(oldStartDate));
				}
			}
		});
		contentPanel.add(comboDateFormat);
		
		txtStartDate = new JTextField();
		txtStartDate.setColumns(10);
		txtStartDate.setBounds(105, 75, 110, 28);
		setStartDate();
		contentPanel.add(txtStartDate);
			
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		buttonPane.add(getOKButton());
		buttonPane.add(getCancelButton());
		
		addLabels();
		
		initializeFields();
		
		setVisible(true);
	}
	
	private void addLabels(){
		JLabel lblStartDate = new JLabel("Start date:");
		lblStartDate.setHorizontalAlignment(SwingConstants.TRAILING);
		lblStartDate.setBounds(25, 81, 69, 16);
		contentPanel.add(lblStartDate);
		
		JLabel lblCasesPerDay = new JLabel("Cases per day:");
		lblCasesPerDay.setBounds(25, 157, 98, 16);
		contentPanel.add(lblCasesPerDay);
		
		JLabel lblCaseStartPrecision = new JLabel("Case start precision:");
		lblCaseStartPrecision.setBounds(25, 119, 132, 16);
		contentPanel.add(lblCaseStartPrecision);
		
		JLabel lblSkipDays = new JLabel("Office days:");
		lblSkipDays.setBounds(180, 215, 77, 16);
		contentPanel.add(lblSkipDays);
		
		JLabel lblOfficeHours = new JLabel("Office hours:");
		lblOfficeHours.setBounds(25, 212, 98, 16);
		contentPanel.add(lblOfficeHours);
		
		JLabel lblStartTime = new JLabel("Start time:");
		lblStartTime.setHorizontalAlignment(SwingConstants.TRAILING);
		lblStartTime.setBounds(35, 246, 69, 16);
		contentPanel.add(lblStartTime);
		
		JLabel lblEndTime = new JLabel("End time:");
		lblEndTime.setHorizontalAlignment(SwingConstants.TRAILING);
		lblEndTime.setBounds(35, 274, 69, 16);
		contentPanel.add(lblEndTime);
		
		JLabel lblDeviation = new JLabel("Deviation:");
		lblDeviation.setBounds(195, 157, 69, 16);
		contentPanel.add(lblDeviation);
		
		JLabel label = new JLabel("%");
		label.setBounds(315, 157, 15, 16);
		contentPanel.add(label);
		
		JLabel lblName = new JLabel("Name:");
		lblName.setHorizontalAlignment(SwingConstants.TRAILING);
		lblName.setBounds(24, 24, 69, 16);
		contentPanel.add(lblName);
	}
	
	private void applyDateFormat(){
		if(dateFormat == null){
			dateFormat = new SimpleDateFormat(comboDateFormat.getSelectedItem().toString());
			dateFormat.setLenient(false);
		} else {
			dateFormat.applyPattern(comboDateFormat.getSelectedItem().toString());
		}
	}
	
	private JButton getOKButton(){
		if(btnOK == null){
			btnOK = new JButton("OK");
			btnOK.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					//Set generator name
					String generatorName = txtName.getText();
					if(generatorName == null || generatorName.isEmpty()){
						JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Affected field: Generator name.\nReason: Null or empty value.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					Set<String> generatorNames = new HashSet<String>(SimulationComponents.getInstance().getCaseTimeGeneratorNames());
					if(editMode){
						try {
							generatorNames.remove(timeProperties.getName());
						} catch (PropertyException e1) {
							JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Cannot extract old generator name.\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
					if(generatorNames.contains(generatorName)){
						JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "There is already a time generator with name \""+generatorName+"\"", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					try {
						TimeGeneratorDialog.this.timeProperties.setName(generatorName);
					} catch (ParameterException e3) {
						JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Affected field: Generator name.\nReason: "+e3.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					
					//Set start time 
					try {
						Date startDate = getStartDate();
						TimeGeneratorDialog.this.timeProperties.setStartTime(startDate.getTime());
					} catch (ParseException e1){
						JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Affected value: Start date\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					} catch (ParameterException e2) {
						JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Affected value: Start date\nReason: " + e2.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					//Set case start time precision
					try {
						TimeGeneratorDialog.this.timeProperties.setCaseStarttimePrecision(CaseStartPrecision.valueOf(comboPrecision.getSelectedItem().toString()));
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Error on setting value for: Case start time\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					//Set cases per day
					Integer casesPerDay = null;
					try {
						casesPerDay = Validate.positiveInteger(txtCasesPerDay.getText());
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Affected value: Cases per day\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					try {
						TimeGeneratorDialog.this.timeProperties.setCasesPerDay(casesPerDay);
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Error on setting value for: Cases per day\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					//Set deviation for cases per day
					Double dayCasesDeviation = null;
					try {
						dayCasesDeviation = Validate.percentage(txtDeviationPercentage.getText());
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Affected value: Deviation for cases per day\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					try {
						TimeGeneratorDialog.this.timeProperties.setDayCasesDeviation(dayCasesDeviation / 100.0);
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Error on setting value for: Deviation for cases per day\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					//Set start and end time for office hours
					Integer startTime = null;
					try {
						startTime = Validate.notNegativeInteger(txtOfficeHoursStart.getText());
						Validate.inclusiveBetween(0, 23, startTime);
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Affected value: Office hours (start time)\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					Integer endTime = null;
					try {
						endTime = Validate.notNegativeInteger(txtOfficeHoursEnd.getText());
						Validate.inclusiveBetween(0, 23, endTime);
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Affected value: Office hours (end time)\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					try {
						Validate.minMax(startTime, endTime);
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Affected values: Office hours\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					try {
						TimeGeneratorDialog.this.timeProperties.setWorkingHours(startTime, endTime);
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Error on setting value for: Office hours\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					//Set weekdays
					if(officeDayListModel.isEmpty()){
						JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Affected values: Office days\nReason: No office days given", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					List<String> allWeekdays = Weekday.stringValues();
					for(Enumeration<?> officeDays = officeDayListModel.elements(); officeDays.hasMoreElements();)
						allWeekdays.remove(officeDays.nextElement());
					List<Weekday> skipDays = new ArrayList<Weekday>();
					for(String remainingWeekday: allWeekdays)
						skipDays.add(Weekday.valueOf(remainingWeekday.toUpperCase()));
					try {
						TimeGeneratorDialog.this.timeProperties.setSkipDays(skipDays);
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Error on setting value for: Days to skip\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					System.out.println("no error");
					
					dispose();
				}
			});
			btnOK.setActionCommand("OK");
			getRootPane().setDefaultButton(btnOK);
		}
		return btnOK;
	}
	
	private JButton getCancelButton(){
		if(btnCancel == null){
			btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(!TimeGeneratorDialog.this.editMode){
						TimeGeneratorDialog.this.timeProperties = null;
					}
					dispose();
				}
			});
			btnCancel.setActionCommand("Cancel");
		}
		return btnCancel;
	}
	
	private JButton getSetActivityDurationsButton(){
		if(btnSetActivityDurations == null){
			btnSetActivityDurations = new JButton("Activity Durations");
			btnSetActivityDurations.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						ActivityDDAllDialog.showDialog(TimeGeneratorDialog.this, DialogType.Duration, TimeGeneratorDialog.this.activities, TimeGeneratorDialog.this.timeProperties);
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Internal exception, cannot launch activity duration dialog:\n" + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			});
			btnSetActivityDurations.setToolTipText("Set and edit the duration of processs activities.");
			btnSetActivityDurations.setBounds(25, 391, 172, 29);
			btnSetActivityDurations.setEnabled(!activities.isEmpty());
		}
		return btnSetActivityDurations;
	}
	
	private JButton getSetActivityDelaysButton(){
		if(btnSetActivityDelays == null){
			btnSetActivityDelays = new JButton("Activity Delays");
			btnSetActivityDelays.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						ActivityDDAllDialog.showDialog(TimeGeneratorDialog.this, DialogType.Delay, TimeGeneratorDialog.this.activities, TimeGeneratorDialog.this.timeProperties);
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Internal exception, cannot launch activity delay dialog:\n" + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			});
			btnSetActivityDelays.setToolTipText("Set and edit delays after execution of process activities.");
			btnSetActivityDelays.setBounds(209, 391, 172, 29);
			btnSetActivityDelays.setEnabled(!activities.isEmpty());
		}
		return btnSetActivityDelays;
	}
	
	private Date getStartDate() throws ParseException{
        return dateFormat.parse(txtStartDate.getText());
	}
	
	public TimeProperties getTimeProperties(){
		return timeProperties;
	}
	
	private void setStartDate(){
		SimpleDateFormat formatter = new SimpleDateFormat(comboDateFormat.getSelectedItem().toString());
		txtStartDate.setText(formatter.format(new Date(System.currentTimeMillis())));
	}
	
	private void initializeFields(){
		try {
			//Set name
			txtName.setText(timeProperties.getName());
			
			//Set start date
			try{
				SimpleDateFormat dateFormat = new SimpleDateFormat(comboDateFormat.getSelectedItem().toString());
				txtStartDate.setText(dateFormat.format(timeProperties.getStartTime()));
			} catch (Exception e) {
				//Do nothing, i.e. keep the standard value of the field which is today.
			}
			
			//Set case start precision
			comboPrecision.setSelectedItem(timeProperties.getCaseStarttimePrecision().toString());
			
			//Set cases per day
			txtCasesPerDay.setText(timeProperties.getCasesPerDay().toString());
			
			//Set cases per day deviation
			try{
				txtDeviationPercentage.setText(String.valueOf(timeProperties.getCasesPerDayDeviation()*100));
			} catch (Exception e) {
				//Do nothing, i.e. keep the standard value of the field which is 0.0.
			}
			
			//Set working hours
			txtOfficeHoursStart.setText(timeProperties.getOfficeHoursStart().toString());
			txtOfficeHoursEnd.setText(timeProperties.getOfficeHoursEnd().toString());
			
			//Set office days
			List<Weekday> skipDays = timeProperties.getSkipDays();
			List<Weekday> workingDays = new ArrayList<Weekday>(Arrays.asList(Weekday.values()));
			workingDays.removeAll(skipDays);
			chckbxSkipWeekend.setSelected(!workingDays.contains(Weekday.SATURDAY) && !workingDays.contains(Weekday.SUNDAY));
			officeDayListModel.clear();
			for(Weekday workingDay: workingDays)
				officeDayListModel.addElement(workingDay.toString());
			
		} catch (PropertyException e) {
			JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Error on initializing fields \nReason: " + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
			return;
		} catch (ParameterException e) {
			JOptionPane.showMessageDialog(TimeGeneratorDialog.this, "Error on initializing fields \nReason: " + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
	
	public static TimeProperties showTimeGeneratorDialog(Window owner, Collection<String> activities) throws ParameterException {
		TimeGeneratorDialog dialog;
		dialog = new TimeGeneratorDialog(owner, activities);
		return dialog.getTimeProperties();
	}
	
	public static TimeProperties showTimeGeneratorDialog(Window owner, Collection<String> activities, TimeProperties timeProperties) throws ParameterException {
		TimeGeneratorDialog dialog;
		dialog = new TimeGeneratorDialog(owner, activities, timeProperties);
		return dialog.getTimeProperties();
	}
	
	public static void main(String[] args) {
		try {
			List<String> activities = new ArrayList<String>();
			activities.add("act1");
			activities.add("act2");
			activities.add("act3");
			new TimeGeneratorDialog(null, activities);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
