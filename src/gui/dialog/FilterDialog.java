package gui.dialog;

import gui.Hints;
import gui.SimulationComponents;
import gui.misc.CustomListRenderer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.xml.stream.XMLStreamException;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;

import log.EntryField;
import logic.filtering.filter.FilterType;
import logic.filtering.filter.trace.AbstractTraceFilter;
import logic.filtering.filter.trace.BoDPropertyFilter;
import logic.filtering.filter.trace.SoDBoDPropertyFilter;
import logic.filtering.filter.trace.SoDPropertyFilter;
import logic.filtering.filter.trace.multiple.AbstractMultipleTraceFilter;
import logic.filtering.filter.trace.multiple.DayDelayFilter;
import logic.filtering.filter.trace.multiple.IncompleteLoggingFilter;
import logic.filtering.filter.trace.multiple.ObfuscationFilter;
import logic.filtering.filter.trace.multiple.SkipActivitiesFilter;
import logic.filtering.filter.trace.multiple.UnauthorizedExecutionFilter;

public class FilterDialog extends AbstractSimulationDialog {
	
	private static final long serialVersionUID = -6315050477292478094L;
	
	private JComboBox comboFilterType = null;
	private JTextField txtFilterName = null;
	private JTextField txtFilterActivation = null;
	private JTextField txtMaxAppliances = null;
	private JLabel lblMaxAppliances = null;
	private JLabel lblMax = null;
	private JLabel lblFilterActivation = null;
	private JSeparator separator = null;
	private JLabel lblHint = null;
	
	private JPanel panelGeneral = null;
	private JPanel panelConfiguration = null;
	
	private JPanel panelDayDelay = null;
	private JTextField txtMinDays = null;
	private JTextField txtMaxDays = null;
	
	private JPanel panelSkipActivities = null;
	private JList listSkipActivities = null;
	private DefaultListModel listSkipActivitiesModel = null;
	private JButton btnAddSkipActivity = null;
	private Set<String> skipActivities = null;
	
	private JPanel panelObfuscation = null;
	private JList listObfuscationFields = null;
	private DefaultListModel listObfuscationFieldsModel = null;
	private JButton btnAddObfuscationField = null;
	private Set<EntryField> excludedFields = null;
	
	private JPanel panelBindings = null;
	private JList listBindingActivities = null;
	private DefaultListModel listBindingActivitiesModel = null;
	private JButton btnAddBindingActivity = null;
	private Set<String> bindingActivities = null;
	
	private Set<String> activities = null;
	
	private Double activationProbability = null;
	private Integer maxAppliances = null;
	private Integer minDays = null;
	private Integer maxDays = null;
	private String filterName = null;
	
	/**
	 * @wbp.parser.constructor
	 */
	public FilterDialog(Window owner, Set<String> activities) {
		super(owner, new Object[]{activities});
	}
	
	public FilterDialog(Window owner, Set<String> activities, AbstractTraceFilter filter) {
		super(owner, true, new Object[]{activities, filter});
	}

	@Override
	protected void addComponents() {
		mainPanel().setLayout(new BorderLayout());
		
		mainPanel().add(getGeneralPanel(), BorderLayout.NORTH);
		
		panelConfiguration = new JPanel(new BorderLayout());
		updateConfigurationPanel((FilterType) comboFilterType.getSelectedItem());
		mainPanel().add(panelConfiguration, BorderLayout.CENTER);
		
		pack();
	}
	
	private JPanel getGeneralPanel(){
		if(panelGeneral == null){
			
			panelGeneral = new JPanel();
			panelGeneral.setLayout(null);
			panelGeneral.setPreferredSize(new Dimension(360, 250));
			
			JLabel lblFilterType = new JLabel("Filter Type:");
			lblFilterType.setHorizontalAlignment(JLabel.TRAILING);
			lblFilterType.setBounds(20, 18, 80, 27);
			panelGeneral.add(lblFilterType);
			
			lblHint = new JLabel();
			lblHint.setOpaque(true);
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setViewportView(lblHint);
			scrollPane.setBounds(20, 54, 320, 80);
			scrollPane.setBorder(null);
			panelGeneral.add(scrollPane);
			
			JLabel lblFilterName = new JLabel("Filter Name:");
			lblFilterName.setHorizontalAlignment(JLabel.TRAILING);
			lblFilterName.setBounds(20, 144, 80, 27);
			panelGeneral.add(lblFilterName);
			
			lblFilterActivation = new JLabel("Activation:");
			lblFilterActivation.setHorizontalAlignment(JLabel.TRAILING);
			lblFilterActivation.setBounds(20, 175, 80, 27);
			panelGeneral.add(lblFilterActivation);
			
			JLabel lblPercentage = new JLabel("%");
			lblPercentage.setBounds(210, 175, 80, 27);
			panelGeneral.add(lblPercentage);
			
			lblMaxAppliances = new JLabel("Appliances:");
			lblMaxAppliances.setHorizontalAlignment(JLabel.TRAILING);
			lblMaxAppliances.setBounds(20, 206, 80, 27);
			panelGeneral.add(lblMaxAppliances);
			
			lblMax = new JLabel("(max)");
			lblMax.setBounds(210, 206, 80, 27);
			panelGeneral.add(lblMax);
			
			comboFilterType = new JComboBox();
			comboFilterType.setModel(new DefaultComboBoxModel(FilterType.values()));
			comboFilterType.setBounds(105, 20, 240, 27);
			comboFilterType.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED){
						FilterType filterType = (FilterType) comboFilterType.getSelectedItem();
						boolean bodsodFilter = filterType == FilterType.SOD_FILTER || filterType == FilterType.BOD_FILTER;
						txtMaxAppliances.setEnabled(!bodsodFilter);
						lblMaxAppliances.setEnabled(!bodsodFilter);
						lblMax.setEnabled(!bodsodFilter);
						lblFilterActivation.setText(bodsodFilter ? "Violation:" : "Activation");
						separator.setVisible(filterType != FilterType.UNAUTHORIZED_EXECUTION_FILTER);
						comboFilterType.setToolTipText(getHint(filterType));
						lblHint.setText(getHint(filterType));
						updateConfigurationPanel(filterType);
					}
				}
			});
			String hint = getHint((FilterType) comboFilterType.getSelectedItem());
			comboFilterType.setToolTipText(hint);
			lblHint.setText(hint);
			panelGeneral.add(comboFilterType);
			
			txtFilterName = new JTextField();
			txtFilterName.setBounds(105, 144, 150, 27);
			panelGeneral.add(txtFilterName);
			txtFilterName.setText("NewFilter");
			
			txtFilterActivation = new JTextField();
			txtFilterActivation.setBounds(105, 175, 100, 27);
			panelGeneral.add(txtFilterActivation);
			txtFilterActivation.setText("100");
			
			txtMaxAppliances = new JTextField();
			txtMaxAppliances.setBounds(105, 206, 100, 27);
			panelGeneral.add(txtMaxAppliances);
			txtMaxAppliances.setText("1");
			
			separator = new JSeparator(SwingConstants.HORIZONTAL);
			separator.setBounds(20, 237, 280, 20);
			panelGeneral.add(separator);
		}
		return panelGeneral;
	}
	
	private String getHint(FilterType filterType){
		switch(filterType){
		case OBFUSCATION_FILTER: return Hints.hintObfuscationFilter;
		case DAY_DELAY_FILTER: return Hints.hintDayDelayFilter;
		case SKIP_ACTIVITIES_FILTER: return Hints.hintSkipActivitiesFilter;
		case INCOMPLETE_LOGGING_FILTER: return Hints.hintIncompleteLoggingFilter;
		case UNAUTHORIZED_EXECUTION_FILTER: return Hints.hintUnauthorizedExecutionFilter;
		case BOD_FILTER: return Hints.hintBoDFilter;
		case SOD_FILTER: return Hints.hintSoDFilter;
		}
		return "";
	}
	
	@Override
	protected void setBounds() {
		setBounds(100, 100, 320, 432);
	}
	
	private void updateConfigurationPanel(FilterType filterType){
		panelConfiguration.removeAll();
		JPanel filterPanel = getConfigPanel(filterType);
		if(filterPanel != null){
			panelConfiguration.add(filterPanel, BorderLayout.CENTER);
		}
		pack();
		repaint();
	}
	
	private JPanel getConfigPanel(FilterType filterType){
		switch (filterType) {
		case DAY_DELAY_FILTER:
			return getConfigDayDelay();
		case SOD_FILTER:
		case BOD_FILTER:
			return getConfigBoDSoD();
		case OBFUSCATION_FILTER:
			return getConfigObfuscation();
		case SKIP_ACTIVITIES_FILTER:
		case INCOMPLETE_LOGGING_FILTER:
			return getConfigSkipActivities();
		default: return null;
		}
	}
	
	private JPanel getConfigSkipActivities(){
		if(panelSkipActivities == null){
			panelSkipActivities = new JPanel();
			panelSkipActivities.setPreferredSize(new Dimension(320, 200));
			panelSkipActivities.setLayout(null);
			
			JLabel lblSkipActivities = new JLabel("Skipping allowed for activities:");
			lblSkipActivities.setBounds(20, 0, 200, 27);
			panelSkipActivities.add(lblSkipActivities);
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(20, 26, 280, 140);
			scrollPane.setViewportView(getListSkipActivities());
			panelSkipActivities.add(scrollPane);
			
			panelSkipActivities.add(getButtonAddSkipActivity());
			
//			panelSkipActivities.setBackground(Color.red);
			
		}
		return panelSkipActivities;
	}
	
	private JList getListSkipActivities(){
		if(listSkipActivities == null){
			listSkipActivities = new JList(listSkipActivitiesModel);
			listSkipActivities.setCellRenderer(new CustomListRenderer());
			listSkipActivities.setFixedCellHeight(20);
			listSkipActivities.setVisibleRowCount(10);
			listSkipActivities.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			listSkipActivities.setBorder(null);
			listSkipActivities.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					for(Object selectedObject: listSkipActivities.getSelectedValues()){
						skipActivities.remove(selectedObject);
					}
					updateSkipActivityList();
				}
				
				@Override
				public void keyPressed(KeyEvent e) {}
			});
			updateSkipActivityList();
		}
		return listSkipActivities;
	}
	
	private void updateSkipActivityList(){
		listSkipActivitiesModel.clear();
		for(String skipActivity: skipActivities){
			listSkipActivitiesModel.addElement(skipActivity);
		}
	}
	
	private JButton getButtonAddSkipActivity(){
		if(btnAddSkipActivity == null){
			btnAddSkipActivity = new JButton("Add activity");
			btnAddSkipActivity.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					
					Set<String> skipActivityCandidates = new HashSet<String>(activities);
					skipActivityCandidates.removeAll(skipActivities);
					
					List<String> newSkipActivities = ValueChooserDialog.showDialog(FilterDialog.this, "Choose skip activities", skipActivityCandidates, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					
					if(newSkipActivities != null && !newSkipActivities.isEmpty()){
						for(String newSkipActivity: newSkipActivities){
							skipActivities.add(newSkipActivity);
						}
						updateSkipActivityList();
					}
				}
			});
			btnAddSkipActivity.setBounds(20,170,120,28);
		}
		return btnAddSkipActivity;
	}
	
	private JPanel getConfigObfuscation(){
		if(panelObfuscation == null){
			panelObfuscation = new JPanel();
			panelObfuscation.setPreferredSize(new Dimension(320, 200));
			panelObfuscation.setLayout(null);
			
			JLabel lblObfuscation = new JLabel("Obfuscation forbidden for fields:");
			lblObfuscation.setBounds(20, 0, 240, 27);
			panelObfuscation.add(lblObfuscation);
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(20, 26, 280, 140);
			scrollPane.setViewportView(getListObfuscationFields());
			panelObfuscation.add(scrollPane);
			
			panelObfuscation.add(getButtonAddObfuscationField());
			
		}
		return panelObfuscation;
	}
	
	private JList getListObfuscationFields(){
		if(listObfuscationFields == null){
			listObfuscationFields = new JList(listObfuscationFieldsModel);
			listObfuscationFields.setCellRenderer(new CustomListRenderer());
			listObfuscationFields.setFixedCellHeight(20);
			listObfuscationFields.setVisibleRowCount(10);
			listObfuscationFields.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			listObfuscationFields.setBorder(null);
			listObfuscationFields.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					for(Object selectedObject: listObfuscationFields.getSelectedValues()){
						EntryField field = EntryField.valueOf(selectedObject.toString());
						if(field != null && field != EntryField.TIME)
							excludedFields.remove(field);
					}
					updateObfuscationFieldsList();
				}
				
				@Override
				public void keyPressed(KeyEvent e) {}
			});
			updateSkipActivityList();
		}
		return listObfuscationFields;
	}
	
	private void updateObfuscationFieldsList(){
		listObfuscationFieldsModel.clear();
		for(EntryField obfuscationField: excludedFields){
			listObfuscationFieldsModel.addElement(obfuscationField.toString());
		}
	}
	
	private JButton getButtonAddObfuscationField(){
		if(btnAddObfuscationField == null){
			btnAddObfuscationField = new JButton("Add field");
			btnAddObfuscationField.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					Set<EntryField> obfuscationFieldCandidates = new HashSet<EntryField>(Arrays.asList(EntryField.values()));
					
					obfuscationFieldCandidates.remove(EntryField.ORIGINATOR_CANDIDATES);
					obfuscationFieldCandidates.remove(EntryField.META);
					
					obfuscationFieldCandidates.removeAll(excludedFields);
					
					Set<String> remainingCandidates = new HashSet<String>();
					for(EntryField remainingField: obfuscationFieldCandidates){
						remainingCandidates.add(remainingField.toString());
					}
					
					List<String> newObfuscationFields = ValueChooserDialog.showDialog(FilterDialog.this, "Choose obfuscation fields", remainingCandidates, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					
					if(newObfuscationFields != null && !newObfuscationFields.isEmpty()){
						for(String newObfuscationField: newObfuscationFields){
							excludedFields.add(EntryField.valueOf(newObfuscationField));
						}
						updateObfuscationFieldsList();
					}
				}
			});
			btnAddObfuscationField.setBounds(20,170,120,28);
		}
		return btnAddObfuscationField;
	}
	
	private JPanel getConfigBoDSoD(){
		if(panelBindings == null){
			panelBindings = new JPanel();
			panelBindings.setPreferredSize(new Dimension(320, 200));
			panelBindings.setLayout(null);
			
			JLabel lblBinding = new JLabel("Scope of property:");
			lblBinding.setBounds(20, 0, 200, 27);
			panelBindings.add(lblBinding);
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(20, 26, 280, 140);
			scrollPane.setViewportView(getListBindingActivities());
			panelBindings.add(scrollPane);
			
			panelBindings.add(getButtonAddBindingActivity());
			
		}
		return panelBindings;
	}
	
	private JList getListBindingActivities(){
		if(listBindingActivities == null){
			listBindingActivities = new JList(listBindingActivitiesModel);
			listBindingActivities.setCellRenderer(new CustomListRenderer());
			listBindingActivities.setFixedCellHeight(20);
			listBindingActivities.setVisibleRowCount(10);
			listBindingActivities.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			listBindingActivities.setBorder(null);
			listBindingActivities.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					for(Object selectedObject: listBindingActivities.getSelectedValues()){
						bindingActivities.remove(selectedObject.toString());
					}
					updateObfuscationFieldsList();
				}
				
				@Override
				public void keyPressed(KeyEvent e) {}
			});
			updateBindingActivitiesList();
		}
		return listBindingActivities;
	}
	
	private void updateBindingActivitiesList(){
		listBindingActivitiesModel.clear();
		for(String bindingActivity: bindingActivities){
			listBindingActivitiesModel.addElement(bindingActivity);
		}
	}
	
	private JButton getButtonAddBindingActivity(){
		if(btnAddBindingActivity == null){
			btnAddBindingActivity = new JButton("Add activity");
			btnAddBindingActivity.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					Set<String> bindingCandidates = new HashSet<String>(activities);
					bindingCandidates.removeAll(bindingActivities);
					
					List<String> newBindingActivities = ValueChooserDialog.showDialog(FilterDialog.this, "Choose activities", bindingCandidates, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					
					if(newBindingActivities != null && !newBindingActivities.isEmpty()){
						for(String newSkipActivity: newBindingActivities){
							bindingActivities.add(newSkipActivity);
						}
						updateBindingActivitiesList();
					}
				}
			});
			btnAddBindingActivity.setBounds(20,170,120,28);
		}
		return btnAddBindingActivity;
	}
	
	private JPanel getConfigDayDelay(){
		if(panelDayDelay == null){
			panelDayDelay = new JPanel();
			panelDayDelay.setPreferredSize(new Dimension(80,60));
			panelDayDelay.setLayout(null);
			
			JLabel lblMinDays = new JLabel("Min days:");
			lblMinDays.setHorizontalAlignment(JLabel.TRAILING);
			lblMinDays.setBounds(20, 0, 80, 27);
			panelDayDelay.add(lblMinDays);
			
			JLabel lblMaxDays = new JLabel("Max days:");
			lblMaxDays.setHorizontalAlignment(JLabel.TRAILING);
			lblMaxDays.setBounds(20, 30, 80, 27);
			panelDayDelay.add(lblMaxDays);
			
			txtMinDays = new JTextField();
			txtMinDays.setBounds(105, 0, 80, 27);
			panelDayDelay.add(txtMinDays);
			txtMinDays.setText("1");
			
			txtMaxDays = new JTextField();
			txtMaxDays.setBounds(105, 30, 80, 27);
			txtMaxDays.setText("1");
			panelDayDelay.add(txtMaxDays);
			
//			panelDayDelay.setBackground(Color.green);
		}
		return panelDayDelay;
	}

	@Override
	protected void initialize(Object... parameters) {
		super.initialize(parameters);
		listSkipActivitiesModel = new DefaultListModel();
		listObfuscationFieldsModel = new DefaultListModel();
		listBindingActivitiesModel = new DefaultListModel();
		
		skipActivities = new HashSet<String>();
		excludedFields = new HashSet<EntryField>();
		excludedFields.add(EntryField.TIME);
		bindingActivities = new HashSet<String>();
		
		this.activities = (Set<String>) parameters[0];
		if(editMode){
			setDialogObject((AbstractTraceFilter) parameters[1]); 
		}
	}
	
	
	
	@Override
	protected void okProcedure() {
		
		if(!validateInputFields()){
			return;
		}
		
		if(editMode){
			try {
				getDialogObject().setName(filterName);
				
				if(getDialogObject() instanceof AbstractMultipleTraceFilter){
					getDialogObject().setActivationProbability(activationProbability);
					
					((AbstractMultipleTraceFilter) getDialogObject()).setMaxAppliances(maxAppliances);
					
					if(getDialogObject() instanceof DayDelayFilter){
						((DayDelayFilter) getDialogObject()).setDayBounds(minDays, maxDays);
					} else if(getDialogObject() instanceof SkipActivitiesFilter){
						((SkipActivitiesFilter) getDialogObject()).setSkipActivities(skipActivities);
					} else if(getDialogObject() instanceof IncompleteLoggingFilter){
						((IncompleteLoggingFilter) getDialogObject()).setSkipActivities(skipActivities);
					} else if(getDialogObject() instanceof ObfuscationFilter){
						((ObfuscationFilter) getDialogObject()).setExcludedFields(excludedFields);
					} else if(getDialogObject() instanceof UnauthorizedExecutionFilter){
						
					} 
				} else if (getDialogObject() instanceof SoDBoDPropertyFilter){
					getDialogObject().setActivationProbability(1.0);
					((SoDBoDPropertyFilter) getDialogObject()).setViolationProbability(activationProbability);
					
					if(getDialogObject() instanceof SoDPropertyFilter){
						((SoDPropertyFilter) getDialogObject()).setActivityGroups(bindingActivities);
					} else if(getDialogObject() instanceof BoDPropertyFilter){
						((BoDPropertyFilter) getDialogObject()).setActivityGroups(bindingActivities);
					}
				}
			} catch(ParameterException e){
				JOptionPane.showMessageDialog(FilterDialog.this, "Cannot change filter properties.\nReason: " + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
				return;
			}
		} else {
			AbstractTraceFilter filter = null;
			try {
				switch(getFilterType()){
				
					case UNAUTHORIZED_EXECUTION_FILTER:
						filter = new UnauthorizedExecutionFilter(activationProbability, maxAppliances);
						break;
						
					case DAY_DELAY_FILTER: 
						filter = new DayDelayFilter(activationProbability, maxAppliances, minDays, maxDays);
						break;
						
					case SKIP_ACTIVITIES_FILTER:
						filter = new SkipActivitiesFilter(activationProbability, maxAppliances, skipActivities);
						break;
						
					case INCOMPLETE_LOGGING_FILTER:
						filter = new IncompleteLoggingFilter(activationProbability, maxAppliances, skipActivities);
						break;
						
					case OBFUSCATION_FILTER:
						filter = new ObfuscationFilter(activationProbability, maxAppliances, excludedFields);
						break;
						
					case BOD_FILTER:
						filter = new BoDPropertyFilter(activationProbability, bindingActivities);
						break;
					case SOD_FILTER:
						filter = new SoDPropertyFilter(activationProbability, bindingActivities);
						break;
				}
				if(filter == null) {
					JOptionPane.showMessageDialog(FilterDialog.this, "Filter was not created. Missing constant in switch-statement?", "Internal Exception", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				filter.setName(filterName);
				setDialogObject(filter);
			} catch (ParameterException e) {
				JOptionPane.showMessageDialog(FilterDialog.this, "Cannot set filter properties.\nReason: " + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		super.okProcedure();
	}
	
	private FilterType getFilterType(){
		return FilterType.valueOfString(comboFilterType.getSelectedItem().toString());
	}
	
	private boolean validateInputFields(){
		
		//Validate filter name
		filterName = txtFilterName.getText();
		if(filterName == null || filterName.isEmpty()){
			JOptionPane.showMessageDialog(FilterDialog.this, "Affected field: Filter name.\nReason: Null or empty value.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		Set<String> filterNames = new HashSet<String>(SimulationComponents.getInstance().getFilterNames());
		if(editMode){
			filterNames.remove(getDialogObject().getName());
		}
		if(filterNames.contains(filterName)){
			JOptionPane.showMessageDialog(FilterDialog.this, "There is already a filter with name \""+filterName+"\"", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		filterName = filterName.replace(' ', '_');
				
		//Validate activation probability
		//Set deviation for cases per day
		activationProbability = null;
		try {
			activationProbability = Validate.percentage(txtFilterActivation.getText());
		} catch (ParameterException e1) {
			JOptionPane.showMessageDialog(FilterDialog.this, "Affected value: Activation/Violation probability\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		activationProbability = activationProbability / 100.0;
			
		FilterType filterType = getFilterType();
		//Check max appliances
		maxAppliances = null;
		if(filterType != FilterType.BOD_FILTER && filterType != FilterType.SOD_FILTER){
			try {
				maxAppliances = Validate.positiveInteger(txtMaxAppliances.getText());
			} catch (ParameterException e1) {
				JOptionPane.showMessageDialog(FilterDialog.this, "Affected value: Max appliances\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
				
		switch(filterType){
		
		case UNAUTHORIZED_EXECUTION_FILTER:
			return true;
			
		case DAY_DELAY_FILTER: 
			//Check min days
			minDays = null;
			try {
				minDays = Validate.positiveInteger(txtMinDays.getText());
			} catch (ParameterException e1) {
				JOptionPane.showMessageDialog(FilterDialog.this, "Affected value: Min days\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			//Check max days
			maxDays = null;
			try {
				maxDays = Validate.positiveInteger(txtMaxDays.getText());
			} catch (ParameterException e1) {
				JOptionPane.showMessageDialog(FilterDialog.this, "Affected value: Max days\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
			
		case SKIP_ACTIVITIES_FILTER:
			if(skipActivities.isEmpty()){
				JOptionPane.showMessageDialog(FilterDialog.this, "No activities for which skipping is permitted.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
			
		case INCOMPLETE_LOGGING_FILTER:
			if(skipActivities.isEmpty()){
				JOptionPane.showMessageDialog(FilterDialog.this, "No activities for which skipping is permitted.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
			
		case OBFUSCATION_FILTER:
			return true;
			
		case BOD_FILTER:
			if(bindingActivities.isEmpty()){
				JOptionPane.showMessageDialog(FilterDialog.this, "No binding activities chosen.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if(bindingActivities.size()<2){
				JOptionPane.showMessageDialog(FilterDialog.this, "Please choose at least two activities.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
			
		case SOD_FILTER:
			if(bindingActivities.isEmpty()){
				JOptionPane.showMessageDialog(FilterDialog.this, "No binding activities chosen.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if(bindingActivities.size()<2){
				JOptionPane.showMessageDialog(FilterDialog.this, "Please choose at least two activities.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	protected void prepareEditing() {
		comboFilterType.setSelectedItem(getDialogObject().getFilterType());
		comboFilterType.setEnabled(false);
		txtFilterName.setText(getDialogObject().getName());
		txtFilterActivation.setText(new Double((getDialogObject().getActivationProbability()*100.0)).toString());
		
		if(getDialogObject() instanceof SoDBoDPropertyFilter){
			txtFilterActivation.setText(new Double(((SoDBoDPropertyFilter) getDialogObject()).getViolationProbability()*100.0).toString());
			
			bindingActivities.addAll(((SoDBoDPropertyFilter) getDialogObject()).getActivityGroups().get(0));
			updateBindingActivitiesList();
			
		} else if(getDialogObject() instanceof AbstractMultipleTraceFilter){
			txtMaxAppliances.setText(((AbstractMultipleTraceFilter) getDialogObject()).getMaxAppliances().toString());
			
			if(getDialogObject() instanceof DayDelayFilter){
				txtMinDays.setText(((DayDelayFilter) getDialogObject()).getMinDays().toString());
				txtMaxDays.setText(((DayDelayFilter) getDialogObject()).getMaxDays().toString());
			} else if(getDialogObject() instanceof SkipActivitiesFilter){
				skipActivities.addAll(((SkipActivitiesFilter) getDialogObject()).getSkipActivities());
				updateSkipActivityList();
			} else if(getDialogObject() instanceof IncompleteLoggingFilter){
				skipActivities.addAll(((IncompleteLoggingFilter) getDialogObject()).getSkipActivities());
				updateSkipActivityList();
			} else if(getDialogObject() instanceof ObfuscationFilter){
				excludedFields.addAll(((ObfuscationFilter) getDialogObject()).getExcludedFields());
				updateObfuscationFieldsList();
			}
		}
	}

	@Override
	protected AbstractTraceFilter getDialogObject() {
		return (AbstractTraceFilter) super.getDialogObject();
	}
	
	public static AbstractTraceFilter showDialog(Window owner,  Set<String> activities){
		FilterDialog dialog = new FilterDialog(owner, activities);
		return dialog.getDialogObject();
	}
	
	public static AbstractTraceFilter showDialog(Window owner,  Set<String> activities, AbstractTraceFilter traceFilter){
		FilterDialog dialog = new FilterDialog(owner, activities, traceFilter);
		return dialog.getDialogObject();
	}

	public static void main(String[] args) throws ParameterException, XMLStreamException, PropertyException {
		Set<String> activities = new HashSet<String>(Arrays.asList("act 1", "act 2", "act 3", "act 4"));
		
		UnauthorizedExecutionFilter unFilter = new UnauthorizedExecutionFilter(0.5, 10);
		unFilter.setName("un Filter");
		
		DayDelayFilter dayFilter = new DayDelayFilter(0.2, 12, 2, 3);
		dayFilter.setName("day Filter");
		
		Set<String> skipActivities = new HashSet<String>(Arrays.asList("act 1", "act 2"));
		SkipActivitiesFilter skipFilter = new SkipActivitiesFilter(0.4, 3, skipActivities);
		skipFilter.setName("skip Filter");
		
		IncompleteLoggingFilter inFilter = new IncompleteLoggingFilter(0.4, 3, skipActivities);
		inFilter.setName("in Filter");
		
		ObfuscationFilter obFilter = new ObfuscationFilter(0.6, 5, EntryField.ACTIVITY);
		obFilter.setName("ob Filter");
		
		SoDPropertyFilter sodFilter = new SoDPropertyFilter(0.1, skipActivities);
		sodFilter.setName("sod");
		
		BoDPropertyFilter bodFilter = new BoDPropertyFilter(0.1, skipActivities);
		bodFilter.setName("bod");
		
		AbstractTraceFilter filter = FilterDialog.showDialog(null, activities, bodFilter);
	}

}
