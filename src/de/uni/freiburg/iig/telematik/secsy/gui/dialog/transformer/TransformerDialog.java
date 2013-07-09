package de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer;


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
import de.uni.freiburg.iig.telematik.jawl.log.EntryField;
import de.uni.freiburg.iig.telematik.secsy.gui.Hints;
import de.uni.freiburg.iig.telematik.secsy.gui.SimulationComponents;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.AbstractSimulationDialog;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.ValueChooserDialog;
import de.uni.freiburg.iig.telematik.secsy.gui.misc.CustomListRenderer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.TransformerType;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.AbstractTraceTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.BoDPropertyTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.SoDBoDPropertyTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.SoDPropertyTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.multiple.AbstractMultipleTraceTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.multiple.DayDelayTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.multiple.IncompleteLoggingTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.multiple.ObfuscationTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.multiple.SkipActivitiesTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.multiple.UnauthorizedExecutionTransformer;


public class TransformerDialog extends AbstractSimulationDialog {
	
	private static final long serialVersionUID = -6315050477292478094L;
	
	private JComboBox comboTransformerType = null;
	private JTextField txtTransformerName = null;
	private JTextField txtTransformerActivation = null;
	private JTextField txtMaxAppliances = null;
	private JLabel lblMaxAppliances = null;
	private JLabel lblMax = null;
	private JLabel lblTransformerActivation = null;
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
	private String TransformerName = null;
	
	/**
	 * @wbp.parser.constructor
	 */
	public TransformerDialog(Window owner, Set<String> activities) {
		super(owner, new Object[]{activities});
	}
	
	public TransformerDialog(Window owner, Set<String> activities, AbstractTraceTransformer transformer) {
		super(owner, true, new Object[]{activities, transformer});
	}

	@Override
	protected void addComponents() {
		mainPanel().setLayout(new BorderLayout());
		
		mainPanel().add(getGeneralPanel(), BorderLayout.NORTH);
		
		panelConfiguration = new JPanel(new BorderLayout());
		updateConfigurationPanel((TransformerType) comboTransformerType.getSelectedItem());
		mainPanel().add(panelConfiguration, BorderLayout.CENTER);
		
		pack();
	}
	
	private JPanel getGeneralPanel(){
		if(panelGeneral == null){
			
			panelGeneral = new JPanel();
			panelGeneral.setLayout(null);
			panelGeneral.setPreferredSize(new Dimension(360, 250));
			
			JLabel lblTransformerType = new JLabel("Transformer Type:");
			lblTransformerType.setHorizontalAlignment(JLabel.TRAILING);
			lblTransformerType.setBounds(20, 18, 80, 27);
			panelGeneral.add(lblTransformerType);
			
			lblHint = new JLabel();
			lblHint.setOpaque(true);
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setViewportView(lblHint);
			scrollPane.setBounds(20, 54, 320, 80);
			scrollPane.setBorder(null);
			panelGeneral.add(scrollPane);
			
			JLabel lblTransformerName = new JLabel("Transformer Name:");
			lblTransformerName.setHorizontalAlignment(JLabel.TRAILING);
			lblTransformerName.setBounds(20, 144, 80, 27);
			panelGeneral.add(lblTransformerName);
			
			lblTransformerActivation = new JLabel("Activation:");
			lblTransformerActivation.setHorizontalAlignment(JLabel.TRAILING);
			lblTransformerActivation.setBounds(20, 175, 80, 27);
			panelGeneral.add(lblTransformerActivation);
			
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
			
			comboTransformerType = new JComboBox();
			comboTransformerType.setModel(new DefaultComboBoxModel(TransformerType.values()));
			comboTransformerType.setBounds(105, 20, 240, 27);
			comboTransformerType.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED){
						TransformerType transformerType = (TransformerType) comboTransformerType.getSelectedItem();
						boolean bodsodTransformer = transformerType == TransformerType.SOD || transformerType == TransformerType.BOD;
						txtMaxAppliances.setEnabled(!bodsodTransformer);
						lblMaxAppliances.setEnabled(!bodsodTransformer);
						lblMax.setEnabled(!bodsodTransformer);
						lblTransformerActivation.setText(bodsodTransformer ? "Violation:" : "Activation");
						separator.setVisible(transformerType != TransformerType.UNAUTHORIZED_EXECUTION);
						comboTransformerType.setToolTipText(getHint(transformerType));
						lblHint.setText(getHint(transformerType));
						updateConfigurationPanel(transformerType);
					}
				}
			});
			String hint = getHint((TransformerType) comboTransformerType.getSelectedItem());
			comboTransformerType.setToolTipText(hint);
			lblHint.setText(hint);
			panelGeneral.add(comboTransformerType);
			
			txtTransformerName = new JTextField();
			txtTransformerName.setBounds(105, 144, 150, 27);
			panelGeneral.add(txtTransformerName);
			txtTransformerName.setText("New Transformer");
			
			txtTransformerActivation = new JTextField();
			txtTransformerActivation.setBounds(105, 175, 100, 27);
			panelGeneral.add(txtTransformerActivation);
			txtTransformerActivation.setText("100");
			
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
	
	private String getHint(TransformerType transformerType){
		switch(transformerType){
		case OBFUSCATION: return Hints.hintObfuscationTransformer;
		case DAY_DELAY: return Hints.hintDayDelayTransformer;
		case SKIP_ACTIVITIES: return Hints.hintSkipActivitiesTransformer;
		case INCOMPLETE_LOGGING: return Hints.hintIncompleteLoggingTransformer;
		case UNAUTHORIZED_EXECUTION: return Hints.hintUnauthorizedExecutionTransformer;
		case BOD: return Hints.hintBoDTransformer;
		case SOD: return Hints.hintSoDTransformer;
		}
		return "";
	}
	
	@Override
	protected void setBounds() {
		setBounds(100, 100, 320, 432);
	}
	
	private void updateConfigurationPanel(TransformerType transformerType){
		panelConfiguration.removeAll();
		JPanel transformerPanel = getConfigPanel(transformerType);
		if(transformerPanel != null){
			panelConfiguration.add(transformerPanel, BorderLayout.CENTER);
		}
		pack();
		repaint();
	}
	
	private JPanel getConfigPanel(TransformerType transformerType){
		switch (transformerType) {
		case DAY_DELAY:
			return getConfigDayDelay();
		case SOD:
		case BOD:
			return getConfigBoDSoD();
		case OBFUSCATION:
			return getConfigObfuscation();
		case SKIP_ACTIVITIES:
		case INCOMPLETE_LOGGING:
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
					
					List<String> newSkipActivities = ValueChooserDialog.showDialog(TransformerDialog.this, "Choose skip activities", skipActivityCandidates, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					
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
					
					List<String> newObfuscationFields = ValueChooserDialog.showDialog(TransformerDialog.this, "Choose obfuscation fields", remainingCandidates, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					
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
					
					List<String> newBindingActivities = ValueChooserDialog.showDialog(TransformerDialog.this, "Choose activities", bindingCandidates, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					
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
			setDialogObject((AbstractTraceTransformer) parameters[1]); 
		}
	}
	
	
	
	@Override
	protected void okProcedure() {
		
		if(!validateInputFields()){
			return;
		}
		
		if(editMode){
			try {
				getDialogObject().setName(TransformerName);
				
				if(getDialogObject() instanceof AbstractMultipleTraceTransformer){
					getDialogObject().setActivationProbability(activationProbability);
					
					((AbstractMultipleTraceTransformer) getDialogObject()).setMaxAppliances(maxAppliances);
					
					if(getDialogObject() instanceof DayDelayTransformer){
						((DayDelayTransformer) getDialogObject()).setDayBounds(minDays, maxDays);
					} else if(getDialogObject() instanceof SkipActivitiesTransformer){
						((SkipActivitiesTransformer) getDialogObject()).setSkipActivities(skipActivities);
					} else if(getDialogObject() instanceof IncompleteLoggingTransformer){
						((IncompleteLoggingTransformer) getDialogObject()).setSkipActivities(skipActivities);
					} else if(getDialogObject() instanceof ObfuscationTransformer){
						((ObfuscationTransformer) getDialogObject()).setExcludedFields(excludedFields);
					} else if(getDialogObject() instanceof UnauthorizedExecutionTransformer){
						
					} 
				} else if (getDialogObject() instanceof SoDBoDPropertyTransformer){
					getDialogObject().setActivationProbability(1.0);
					((SoDBoDPropertyTransformer) getDialogObject()).setViolationProbability(activationProbability);
					
					if(getDialogObject() instanceof SoDPropertyTransformer){
						((SoDPropertyTransformer) getDialogObject()).setActivityGroups(bindingActivities);
					} else if(getDialogObject() instanceof BoDPropertyTransformer){
						((BoDPropertyTransformer) getDialogObject()).setActivityGroups(bindingActivities);
					}
				}
			} catch(ParameterException e){
				JOptionPane.showMessageDialog(TransformerDialog.this, "Cannot change transformer properties.\nReason: " + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
				return;
			}
		} else {
			AbstractTraceTransformer transformer = null;
			try {
				switch(getTransformerType()){
				
					case UNAUTHORIZED_EXECUTION:
						transformer = new UnauthorizedExecutionTransformer(activationProbability, maxAppliances);
						break;
						
					case DAY_DELAY: 
						transformer = new DayDelayTransformer(activationProbability, maxAppliances, minDays, maxDays);
						break;
						
					case SKIP_ACTIVITIES:
						transformer = new SkipActivitiesTransformer(activationProbability, maxAppliances, skipActivities);
						break;
						
					case INCOMPLETE_LOGGING:
						transformer = new IncompleteLoggingTransformer(activationProbability, maxAppliances, skipActivities);
						break;
						
					case OBFUSCATION:
						transformer = new ObfuscationTransformer(activationProbability, maxAppliances, excludedFields);
						break;
						
					case BOD:
						transformer = new BoDPropertyTransformer(activationProbability, bindingActivities);
						break;
					case SOD:
						transformer = new SoDPropertyTransformer(activationProbability, bindingActivities);
						break;
				}
				if(transformer == null) {
					JOptionPane.showMessageDialog(TransformerDialog.this, "Transformer was not created. Missing constant in switch-statement?", "Internal Exception", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				transformer.setName(TransformerName);
				setDialogObject(transformer);
			} catch (ParameterException e) {
				JOptionPane.showMessageDialog(TransformerDialog.this, "Cannot set transformer properties.\nReason: " + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		super.okProcedure();
	}
	
	private TransformerType getTransformerType(){
		return TransformerType.valueOfString(comboTransformerType.getSelectedItem().toString());
	}
	
	private boolean validateInputFields(){
		
		//Validate transformer name
		TransformerName = txtTransformerName.getText();
		if(TransformerName == null || TransformerName.isEmpty()){
			JOptionPane.showMessageDialog(TransformerDialog.this, "Affected field: Transformer name.\nReason: Null or empty value.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		Set<String> transformerNames = new HashSet<String>(SimulationComponents.getInstance().getTransformerNames());
		if(editMode){
			transformerNames.remove(getDialogObject().getName());
		}
		if(transformerNames.contains(TransformerName)){
			JOptionPane.showMessageDialog(TransformerDialog.this, "There is already a transformer with name \""+TransformerName+"\"", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		TransformerName = TransformerName.replace(' ', '_');
				
		//Validate activation probability
		//Set deviation for cases per day
		activationProbability = null;
		try {
			activationProbability = Validate.percentage(txtTransformerActivation.getText());
		} catch (ParameterException e1) {
			JOptionPane.showMessageDialog(TransformerDialog.this, "Affected value: Activation/Violation probability\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		activationProbability = activationProbability / 100.0;
			
		TransformerType transformerType = getTransformerType();
		//Check max appliances
		maxAppliances = null;
		if(transformerType != TransformerType.BOD && transformerType != TransformerType.SOD){
			try {
				maxAppliances = Validate.positiveInteger(txtMaxAppliances.getText());
			} catch (ParameterException e1) {
				JOptionPane.showMessageDialog(TransformerDialog.this, "Affected value: Max appliances\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
				
		switch(transformerType){
		
		case UNAUTHORIZED_EXECUTION:
			return true;
			
		case DAY_DELAY: 
			//Check min days
			minDays = null;
			try {
				minDays = Validate.positiveInteger(txtMinDays.getText());
			} catch (ParameterException e1) {
				JOptionPane.showMessageDialog(TransformerDialog.this, "Affected value: Min days\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			//Check max days
			maxDays = null;
			try {
				maxDays = Validate.positiveInteger(txtMaxDays.getText());
			} catch (ParameterException e1) {
				JOptionPane.showMessageDialog(TransformerDialog.this, "Affected value: Max days\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
			
		case SKIP_ACTIVITIES:
			if(skipActivities.isEmpty()){
				JOptionPane.showMessageDialog(TransformerDialog.this, "No activities for which skipping is permitted.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
			
		case INCOMPLETE_LOGGING:
			if(skipActivities.isEmpty()){
				JOptionPane.showMessageDialog(TransformerDialog.this, "No activities for which skipping is permitted.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
			
		case OBFUSCATION:
			return true;
			
		case BOD:
			if(bindingActivities.isEmpty()){
				JOptionPane.showMessageDialog(TransformerDialog.this, "No binding activities chosen.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if(bindingActivities.size()<2){
				JOptionPane.showMessageDialog(TransformerDialog.this, "Please choose at least two activities.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
			
		case SOD:
			if(bindingActivities.isEmpty()){
				JOptionPane.showMessageDialog(TransformerDialog.this, "No binding activities chosen.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if(bindingActivities.size()<2){
				JOptionPane.showMessageDialog(TransformerDialog.this, "Please choose at least two activities.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	protected void prepareEditing() {
		comboTransformerType.setSelectedItem(getDialogObject().getType());
		comboTransformerType.setEnabled(false);
		txtTransformerName.setText(getDialogObject().getName());
		txtTransformerActivation.setText(new Double((getDialogObject().getActivationProbability()*100.0)).toString());
		
		if(getDialogObject() instanceof SoDBoDPropertyTransformer){
			txtTransformerActivation.setText(new Double(((SoDBoDPropertyTransformer) getDialogObject()).getViolationProbability()*100.0).toString());
			
			bindingActivities.addAll(((SoDBoDPropertyTransformer) getDialogObject()).getActivityGroups().get(0));
			updateBindingActivitiesList();
			
		} else if(getDialogObject() instanceof AbstractMultipleTraceTransformer){
			txtMaxAppliances.setText(((AbstractMultipleTraceTransformer) getDialogObject()).getMaxAppliances().toString());
			
			if(getDialogObject() instanceof DayDelayTransformer){
				txtMinDays.setText(((DayDelayTransformer) getDialogObject()).getMinDays().toString());
				txtMaxDays.setText(((DayDelayTransformer) getDialogObject()).getMaxDays().toString());
			} else if(getDialogObject() instanceof SkipActivitiesTransformer){
				skipActivities.addAll(((SkipActivitiesTransformer) getDialogObject()).getSkipActivities());
				updateSkipActivityList();
			} else if(getDialogObject() instanceof IncompleteLoggingTransformer){
				skipActivities.addAll(((IncompleteLoggingTransformer) getDialogObject()).getSkipActivities());
				updateSkipActivityList();
			} else if(getDialogObject() instanceof ObfuscationTransformer){
				excludedFields.addAll(((ObfuscationTransformer) getDialogObject()).getExcludedFields());
				updateObfuscationFieldsList();
			}
		}
	}

	@Override
	protected AbstractTraceTransformer getDialogObject() {
		return (AbstractTraceTransformer) super.getDialogObject();
	}
	
	public static AbstractTraceTransformer showDialog(Window owner,  Set<String> activities){
		TransformerDialog dialog = new TransformerDialog(owner, activities);
		return dialog.getDialogObject();
	}
	
	public static AbstractTraceTransformer showDialog(Window owner,  Set<String> activities, AbstractTraceTransformer traceTransformer){
		TransformerDialog dialog = new TransformerDialog(owner, activities, traceTransformer);
		return dialog.getDialogObject();
	}

	public static void main(String[] args) throws ParameterException, XMLStreamException, PropertyException {
		Set<String> activities = new HashSet<String>(Arrays.asList("act 1", "act 2", "act 3", "act 4"));
		
		UnauthorizedExecutionTransformer unTransformer = new UnauthorizedExecutionTransformer(0.5, 10);
		unTransformer.setName("un Transformer");
		
		DayDelayTransformer dayTransformer = new DayDelayTransformer(0.2, 12, 2, 3);
		dayTransformer.setName("day Transformer");
		
		Set<String> skipActivities = new HashSet<String>(Arrays.asList("act 1", "act 2"));
		SkipActivitiesTransformer skipTransformer = new SkipActivitiesTransformer(0.4, 3, skipActivities);
		skipTransformer.setName("skip Transformer");
		
		IncompleteLoggingTransformer inTransformer = new IncompleteLoggingTransformer(0.4, 3, skipActivities);
		inTransformer.setName("in Transformer");
		
		ObfuscationTransformer obTransformer = new ObfuscationTransformer(0.6, 5, EntryField.ACTIVITY);
		obTransformer.setName("ob Transformer");
		
		SoDPropertyTransformer sodTransformer = new SoDPropertyTransformer(0.1, skipActivities);
		sodTransformer.setName("sod");
		
		BoDPropertyTransformer bodTransformer = new BoDPropertyTransformer(0.1, skipActivities);
		bodTransformer.setName("bod");
		
		AbstractTraceTransformer transformer = TransformerDialog.showDialog(null, activities, bodTransformer);
	}

}
