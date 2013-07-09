package de.uni.freiburg.iig.telematik.secsy.gui.dialog;

import gui.properties.GeneralProperties;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import de.invation.code.toval.misc.ArrayUtils;
import de.invation.code.toval.misc.StringUtils;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.jawl.logformat.LogFormat;
import de.uni.freiburg.iig.telematik.jawl.logformat.LogFormatFactory;
import de.uni.freiburg.iig.telematik.jawl.logformat.LogFormatType;
import de.uni.freiburg.iig.telematik.secsy.gui.Hints;
import de.uni.freiburg.iig.telematik.secsy.gui.SimulationComponents;
import de.uni.freiburg.iig.telematik.sepia.util.PNUtils;

import logic.generator.CaseDataContainer;
import logic.generator.Context;
import logic.generator.DetailedLogEntryGenerator;
import logic.generator.LogEntryGenerator;
import logic.generator.LogGenerator;
import logic.generator.TraceLogGenerator;
import logic.generator.time.CaseTimeGenerator;
import logic.generator.time.properties.TimeGeneratorFactory;
import logic.generator.time.properties.TimeProperties;
import logic.simulation.ConfigurationException;
import logic.simulation.Simulation;
import logic.simulation.SimulationRun;
import logic.simulation.properties.EntryGenerationType;

public class SimulationDialog extends JDialog {

	private static final long serialVersionUID = 1962737975495538666L;
	
	private final JPanel contentPanel = new JPanel();
	
	private JComboBox comboEntryGenerator;
	private JComboBox comboContext;
	private JComboBox comboDataContainer;
	private JComboBox comboLogFormat;
	private JComboBox comboTimeGenerator;
	
	private JButton btnAddTimeGenerator;
	private JButton btnAddContext;
	private JButton btnAddDataContainer;
	private JButton btnAddSimulationRun;
	private JButton btnOK;
	private JButton btnCancel;
	
	private JButton btnEditTimeGenerator;
	private JButton btnEditContext;
	private JButton btnEditDataContainer;
	private JButton btnEditSimulationRun;
	
	private JList listSimulationRuns;
	private DefaultListModel listSimulationRunsModel = new DefaultListModel();
	private JTextField txtLogName;
	
	private Simulation simulation = null;
	
	private Map<String, SimulationRun> simulationRuns = new HashMap<String, SimulationRun>();
	
//	private SimulationProperties simulationProperties = null;
	private JTextField txtName;
	
	private boolean editMode = false;
	private JButton btnShowActivities;
	
	/**
	 * @throws ParameterException 
	 * @wbp.parser.constructor
	 */
	public SimulationDialog(Window owner, Simulation simulation) throws ParameterException {
		super(owner);
		this.simulation = simulation;
		this.editMode = true;
		setUpGUI(owner);
	}
	
	public SimulationDialog(Window owner) throws ParameterException {
		super(owner);
		setUpGUI(owner);
	}
	
	private void setUpGUI(Window owner){
		setBounds(100, 100, 505, 586);
		setModal(true);
		setLocationRelativeTo(owner);
		setResizable(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		if(editMode){
			setTitle("Edit Simulation");
		} else {
			setTitle("New Simulation");
		}
		
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setLayout(null);
		
		contentPanel.add(getLogNameField());
		
		contentPanel.add(getLogFormatBox());
		
		contentPanel.add(getTimeGeneratorBox());
		contentPanel.add(getAddTimeGeneratorButton());
		contentPanel.add(getEditTimeGeneratorButton());
		
		contentPanel.add(getEntryGeneratorBox());
		
		contentPanel.add(getContextBox());
		contentPanel.add(getAddContextButton());
		contentPanel.add(getEditContextButton());
		
		contentPanel.add(getDataContainerBox());
		contentPanel.add(getAddDataContainerButton());
		contentPanel.add(getEditDataContainerButton());
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(23, 333, 456, 149);
		scrollPane.setViewportView(getSimulationRunList());
		contentPanel.add(scrollPane);
		contentPanel.add(getAddSimulationRunButton());
		contentPanel.add(getEditSimulationRunButton());
		
		// Add button panel (bottom).
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPane.add(getOKButton());
		buttonPane.add(getCancelButton());
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		addLabels();
		
		addSeparators();
		
		if(editMode){
			initializeFields();
		}
			
		setVisible(true);
	}
	
	private void addLabels(){
		
		JLabel lblFileName = new JLabel("File name:");
		lblFileName.setHorizontalAlignment(SwingConstants.TRAILING);
		lblFileName.setBounds(6, 52, 116, 16);
		contentPanel.add(lblFileName);
		
		JLabel lblTimeGenerator = new JLabel("Time generator:");
		lblTimeGenerator.setHorizontalAlignment(SwingConstants.TRAILING);
		lblTimeGenerator.setBounds(23, 253, 99, 16);
		contentPanel.add(lblTimeGenerator);
		
		JLabel lblEntryGeneration = new JLabel("Entry generation:");
		lblEntryGeneration.setHorizontalAlignment(SwingConstants.TRAILING);
		lblEntryGeneration.setBounds(6, 148, 116, 16);
		contentPanel.add(lblEntryGeneration);
		
		JLabel lblContext = new JLabel("Context:");
		lblContext.setHorizontalAlignment(SwingConstants.TRAILING);
		lblContext.setBounds(23, 187, 99, 16);
		contentPanel.add(lblContext);
		
		JLabel lblCaseDataContainer = new JLabel("Data Container:");
		lblCaseDataContainer.setHorizontalAlignment(SwingConstants.TRAILING);
		lblCaseDataContainer.setBounds(23, 219, 99, 16);
		contentPanel.add(lblCaseDataContainer);
		
		JLabel lblSimulationRuns = new JLabel("Simulation runs:");
		lblSimulationRuns.setBounds(23, 312, 116, 16);
		contentPanel.add(lblSimulationRuns);
		
		JLabel lblLogFormat = new JLabel("Log format:");
		lblLogFormat.setHorizontalAlignment(SwingConstants.TRAILING);
		lblLogFormat.setBounds(6, 84, 116, 16);
		contentPanel.add(lblLogFormat);
		
	}
	
	private void addSeparators(){
		
		JSeparator separator2 = new JSeparator();
		separator2.setBounds(6, 288, 482, 12);
		contentPanel.add(separator2);
		
		JSeparator separator3 = new JSeparator();
		separator3.setBounds(6, 119, 482, 12);
		contentPanel.add(separator3);
		
		txtName = new JTextField();
		txtName.setText("NewSimulation");
		txtName.setColumns(10);
		txtName.setBounds(134, 16, 230, 28);
		contentPanel.add(txtName);
		
		JLabel lblName = new JLabel("Name:");
		lblName.setHorizontalAlignment(SwingConstants.TRAILING);
		lblName.setBounds(6, 22, 116, 16);
		contentPanel.add(lblName);
		contentPanel.add(getButtonShowActivities());
	}
	
	
	//------- COMBO BOXES --------------------------------------------------------------------------------------------------------------------------------
	
	private JComboBox getContextBox(){
		if(comboContext == null){
			comboContext = new JComboBox();
			comboContext.setEnabled(false);
			comboContext.setBounds(134, 183, 230, 27);
			comboContext.setToolTipText(Hints.hintContext);
			comboContext.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					updateDataContainerBox();
				}
			});
			updateContextBox();
		}
		return comboContext;
	}
	
	private void updateContextBox(){
		List<String> contextNames = new ArrayList<String>();
		for(Context context: SimulationComponents.getInstance().getContexts()){
			contextNames.add(context.getName());
		}
		comboContext.setModel(new DefaultComboBoxModel(contextNames.toArray()));
	}
	
	private JComboBox getTimeGeneratorBox(){
		if(comboTimeGenerator == null){
			comboTimeGenerator = new JComboBox();
			comboTimeGenerator.setBounds(134, 249, 230, 27);
			comboTimeGenerator.setToolTipText(Hints.hintTimeGenerator);
			updateTimeGeneratorBox();
		}
		return comboTimeGenerator;
	}
	
	private void updateTimeGeneratorBox(){
		List<String> generatorNames = new ArrayList<String>();
		for(CaseTimeGenerator timeGenerator: SimulationComponents.getInstance().getCaseTimeGenerators()){
			generatorNames.add(timeGenerator.getName());
		}
		comboTimeGenerator.setModel(new DefaultComboBoxModel(generatorNames.toArray()));
	}
	
	private JComboBox getDataContainerBox(){
		if(comboDataContainer == null){
			comboDataContainer = new JComboBox();
			comboDataContainer.setEnabled(false);
			comboDataContainer.setBounds(134, 215, 230, 27);
			comboDataContainer.setToolTipText(Hints.hintDataContainer);
			updateDataContainerBox();
		}
		return comboDataContainer;
	}
	
	private void updateDataContainerBox(){
		List<String> containerNames = new ArrayList<String>();
		try {
			if(getContext() == null){
				return;
			}
			for(CaseDataContainer container: SimulationComponents.getInstance().getCaseDataContainers()){
				containerNames.add(container.getName());
			}
		} catch (ParameterException e) {
			JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception: Cannot extract contexts from simulation components:\n" + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
		}
		comboDataContainer.setModel(new DefaultComboBoxModel(containerNames.toArray()));
	}
	
	private JComboBox getEntryGeneratorBox(){
		if(comboEntryGenerator == null){
			comboEntryGenerator = new JComboBox();
			comboEntryGenerator.setModel(new DefaultComboBoxModel(new String[] {"SIMPLE (Activity, Time)", "DETAILED (More attributes)"}));
			comboEntryGenerator.setBounds(134, 144, 230, 27);
			comboEntryGenerator.setToolTipText(Hints.hintSIMPLEGeneration);
			comboEntryGenerator.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(comboEntryGenerator.getSelectedItem().equals("SIMPLE (Activity, Time)")){
						comboEntryGenerator.setToolTipText(Hints.hintSIMPLEGeneration);
					} else {
						comboEntryGenerator.setToolTipText(Hints.hintDETAILEDGeneration);
					}
					updateVisibility();
				}
			});
		}
		return comboEntryGenerator;
	}
	
	private JComboBox getLogFormatBox(){
		if(comboLogFormat == null){
			comboLogFormat = new JComboBox();
			comboLogFormat.setModel(new DefaultComboBoxModel(LogFormatType.values()));
			comboLogFormat.setBounds(134, 80, 230, 27);
			comboLogFormat.setToolTipText(Hints.hintMXMLFormat);
			comboLogFormat.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent arg0) {
					if(comboLogFormat.getSelectedItem().equals("MXML")){
						comboLogFormat.setToolTipText(Hints.hintMXMLFormat);
					} else {
						comboLogFormat.setToolTipText(Hints.hintPLAINFormat);
					}
				}
			});
		}
		return comboLogFormat;
	}
	 
	
	//------- BUTTONS -----------------------------------------------------------------------------------------------------------------------------------
	
	private JButton getAddContextButton(){
		if(btnAddContext == null){
			btnAddContext = new JButton("Add");
			btnAddContext.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Context newContext = ContextDialog.showContextDialog(SimulationDialog.this);
					if(newContext == null){
						// Happens only when context is null
						// In this case the user aborted the context dialog.
						return;
					}
					
					try {
						SimulationComponents.getInstance().addContext(newContext);
						updateContextBox();
						comboContext.setSelectedItem(newContext.getName());
					} catch (Exception e1) {
						// Error on generating context properties or storing the properties to disk.
						JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception, cannot store new context:\n" + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					updateVisibility();
				}
			});
			btnAddContext.setEnabled(false);
			btnAddContext.setActionCommand("OK");
			btnAddContext.setBounds(366, 181, 60, 29);
		}
		return btnAddContext; 
	}
	
	
	
	private JButton getEditContextButton(){
		if(btnEditContext == null){
			btnEditContext = new JButton("Edit");
			btnEditContext.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(comboContext.getSelectedItem() == null){
						JOptionPane.showMessageDialog(SimulationDialog.this, "No context chosen.", "Invalid parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					String chosenContext = comboContext.getSelectedItem().toString();
					
					Context context = null;
					try {
						context = SimulationComponents.getInstance().getContext(chosenContext);
					} catch (ParameterException e2) {
						// Cannot happen, since "chosenContext" is not null.
						JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract context: " + chosenContext, "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(context == null){
						JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract context \""+chosenContext+"\" from simulation components", "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					String oldContextName = context.getName();
					Context editedContext = ContextDialog.showContextDialog(SimulationDialog.this, context);
					if(editedContext != null){
						if(!editedContext.getName().equals(oldContextName)){
							//Context name changed
							//-> Remove old context from simulation components and add it again under the new name.
							//   Since the reference stays the same, this should not change anything with other components referring to this context.
							try {
								SimulationComponents.getInstance().removeContext(oldContextName);
							} catch (Exception e1) {
								JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot change name of context.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
						
						try {
							context.takeOverValues(editedContext);
							SimulationComponents.getInstance().addContext(editedContext);
						} catch (ParameterException e1) {
							JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot take over adjusted context properties.\nReason: "+e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						} catch (IOException e1) {
							JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot store edited context to disk.\nReason: "+e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						} catch (PropertyException e1) {
							JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot set properties for edited context.\nReason: "+e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						}
							
						
						
						updateContextBox();
						comboContext.setSelectedItem(editedContext.getName());
						
						updateVisibility();
					} else {
						//User cancelled the edit-dialog
					}
				}
			});
			btnEditContext.setEnabled(false);
			btnEditContext.setActionCommand("OK");
			btnEditContext.setBounds(428, 181, 60, 29);
		}
		return btnEditContext; 
	}
	
	private JButton getAddDataContainerButton(){
		if(btnAddDataContainer == null){
			btnAddDataContainer = new JButton("Add");
			btnAddDataContainer.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					//Even, when the chosen context contains no attributes,
					//the case data container dialog is launched.
					//-> Default value can be set anyway
					
					CaseDataContainer newContainer = null;
					try {
						newContainer = DataContainerDialog.showDialog(SimulationDialog.this, getContextAttributes());
					} catch (ParameterException e) {
						JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot launch case data container dialog.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					if(newContainer == null){
						//The user aborted the case data container dialog
						return;
					}
					
					try {
						SimulationComponents.getInstance().addCaseDataContainer(newContainer);
						updateDataContainerBox();
						comboDataContainer.setSelectedItem(newContainer.getName());
					} catch (ParameterException e1) {
						e1.printStackTrace();
						// Happens only when container is null
						// In this case the user aborted the container dialog.
						return;
					} catch (Exception e1) {
						// Error on generating container properties or storing the properties to disk.
						JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception, cannot store new case data container:\n" + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					updateVisibility();
				}
			});
			btnAddDataContainer.setEnabled(false);
			btnAddDataContainer.setActionCommand("OK");
			btnAddDataContainer.setBounds(366, 215, 60, 29);
		}
		return btnAddDataContainer; 
	}
	
	private Set<String> getContextAttributes(){
		Set<String> attributes = new HashSet<String>();
		//Check if context is set
		if(comboContext.getSelectedItem() == null){
			JOptionPane.showMessageDialog(SimulationDialog.this, "Please choose a context first.", "Missing Requirement", JOptionPane.ERROR_MESSAGE);
			return attributes;
		}
		String contextName = comboContext.getSelectedItem().toString();
		Context context;
		try {
			context = SimulationComponents.getInstance().getContext(contextName);
		} catch (ParameterException e1) {
			JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract context from simulation components to get attribute information.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
			return attributes;
		}
		return context.getAttributes();
	}
	
	private JButton getEditDataContainerButton(){
		if(btnEditDataContainer == null){
			btnEditDataContainer = new JButton("Edit");
			btnEditDataContainer.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(comboDataContainer.getSelectedItem() == null){
						JOptionPane.showMessageDialog(SimulationDialog.this, "No data container chosen.", "Invalid parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					String chosenContainer = comboDataContainer.getSelectedItem().toString();
					
					CaseDataContainer container = null;
					try {
						container = SimulationComponents.getInstance().getCaseDataContainer(chosenContainer);
					} catch (ParameterException e2) {
						// Cannot happen, since "chosenContainer" is not null.
						JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract case data container: " + chosenContainer, "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(container == null){
						JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract case data container \""+chosenContainer+"\" from simulation components", "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					String oldContainerName = container.getName();
					CaseDataContainer editedContainer;
					try {
						editedContainer = DataContainerDialog.showDialog(SimulationDialog.this, getContextAttributes(), container);
					} catch (ParameterException e2) {
						JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot launch case data container dialog.\nReason: " + e2.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(editedContainer != null){
						if(!editedContainer.getName().equals(oldContainerName)){
							//Container name changed
							//-> Remove old container from simulation components and add it again under the new name.
							//   Since the reference stays the same, this should not change anything with other components referring to this context.
							try {
								SimulationComponents.getInstance().removeCaseDataContainer(oldContainerName);
							} catch (Exception e1) {
								JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot change name of data container.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
						
						try {
							SimulationComponents.getInstance().addCaseDataContainer(editedContainer);
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot add edited data container to simulation components.\nReason: "+e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						}
						
						container.takeOverValues(editedContainer);
						
						updateDataContainerBox();
						comboDataContainer.setSelectedItem(editedContainer.getName());
						
						updateVisibility();
					}
				}
			});
			btnEditDataContainer.setEnabled(false);
			btnEditDataContainer.setActionCommand("OK");
			btnEditDataContainer.setBounds(428, 215, 60, 29);
		}
		return btnEditDataContainer; 
	}

	private JButton getAddSimulationRunButton(){
		if(btnAddSimulationRun == null){
			btnAddSimulationRun = new JButton("Add");
			btnAddSimulationRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					SimulationRun newSimulationRun = SimulationRunDialog.showDialog(SimulationDialog.this);
					if(newSimulationRun == null){
						// Happens only when context is null
						// In this case the user aborted the context dialog.
						return;
					}
					
					while(newSimulationRun.getName() != null && simulationRuns.containsKey(newSimulationRun.getName())){
						JOptionPane.showMessageDialog(SimulationDialog.this, "Simulation run name is already in use.\nYou will be propmted to change the name.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						SimulationRunDialog.showDialog(SimulationDialog.this, newSimulationRun);
					}
					SimulationDialog.this.simulationRuns.put(newSimulationRun.getName(), newSimulationRun);
					
					updateSimulationRunList();
					listSimulationRuns.setSelectedValue(newSimulationRun.getName(), true);
					updateVisibility();
				}
			});
			btnAddSimulationRun.setBounds(23, 486, 60, 29);
		}
		return btnAddSimulationRun; 
	}
	
	private JButton getEditSimulationRunButton(){
		if(btnEditSimulationRun == null){
			btnEditSimulationRun = new JButton("Edit");
			btnEditSimulationRun.setBounds(84, 486, 60, 29);
			btnEditSimulationRun.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					if(listSimulationRuns.getSelectedValue() == null){
						JOptionPane.showMessageDialog(SimulationDialog.this, "No simulation run chosen.", "Invalid parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					SimulationRun simulationRun = null;
					
					try{
						simulationRun = (SimulationRun) listSimulationRuns.getSelectedValue();
					}catch(Exception e1){
						JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract simulation run from list.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					String oldSimulationRunName = simulationRun.getName();
					SimulationRunDialog.showDialog(SimulationDialog.this, simulationRun);
					
					if(!simulationRun.getName().equals(oldSimulationRunName)){
						//Simulation run name changed
						//-> Remove old simulation run from simulation run map and add it again under the new name.
						try {
							simulationRuns.remove(oldSimulationRunName);
							simulationRuns.put(simulationRun.getName(), simulationRun);
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot change name of simulation run.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
					updateSimulationRunList();
					listSimulationRuns.setSelectedValue(simulationRun, true);
					
					updateVisibility();
				}
			});
		}
		return btnEditSimulationRun; 
	}
	
	private JButton getAddTimeGeneratorButton(){
		if(btnAddTimeGenerator == null){
			btnAddTimeGenerator = new JButton("Add");
			btnAddTimeGenerator.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					TimeProperties timeProperties = null;
					try {
						timeProperties = TimeGeneratorDialog.showTimeGeneratorDialog(SimulationDialog.this, getAllKnownProcessActivities());
					} catch (ParameterException e2) {
						JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception, cannot launch time generator dialog.\nReason: " + e2.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					if(timeProperties != null){
						CaseTimeGenerator newTimeGenerator = null;
						try {
							newTimeGenerator = TimeGeneratorFactory.createCaseTimeGenerator(timeProperties);
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception on creating case time generator.\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						}
						
						try {
							SimulationComponents.getInstance().addCaseTimeGenerator(newTimeGenerator);
						} catch (ParameterException e2) {
							JOptionPane.showMessageDialog(SimulationDialog.this, "Parameter Exception on creating new time generator.\nReason: " + e2.getMessage(), "Parameter Exception", JOptionPane.ERROR_MESSAGE);
							return;
						} catch (Exception e2) {
							JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception on adding case time generator to simulation components.\nReason: " + e2.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						}
						updateTimeGeneratorBox();
						try {
							comboTimeGenerator.setSelectedItem(timeProperties.getName());
						} catch (PropertyException e1) {
							// Should not happen, since the name value was successfully set in the time generator dialog.
							e1.printStackTrace();
						}
					}
					
				}
			});
			btnAddTimeGenerator.setBounds(366, 249, 60, 29);
		}
		return btnAddTimeGenerator;
	}
	
	private JButton getEditTimeGeneratorButton(){
		if(btnEditTimeGenerator == null){
			btnEditTimeGenerator = new JButton("Edit");
			btnEditTimeGenerator.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(comboTimeGenerator.getSelectedItem() == null){
						JOptionPane.showMessageDialog(SimulationDialog.this, "No time generator chosen.", "Invalid parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					String chosenTimeGeneratorName = comboTimeGenerator.getSelectedItem().toString();
					
					CaseTimeGenerator timeGenerator = null;
					try {
						timeGenerator = SimulationComponents.getInstance().getCaseTimeGenerator(chosenTimeGeneratorName);
					} catch (ParameterException e2) {
						// Cannot happen, since "chosenContext" is not null.
						JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract time generator: " + chosenTimeGeneratorName, "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(timeGenerator == null){
						JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract time generator \""+chosenTimeGeneratorName+"\" from simulation components", "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					String oldTimeGeneratorName = timeGenerator.getName();
					CaseTimeGenerator editedTimeGenerator = null;
					try {
						editedTimeGenerator = TimeGeneratorFactory.createCaseTimeGenerator(TimeGeneratorDialog.showTimeGeneratorDialog(SimulationDialog.this, getAllKnownProcessActivities(), timeGenerator.getProperties()));
					} catch (ParameterException e3) {
						JOptionPane.showMessageDialog(SimulationDialog.this, e3.getMessage(), "Parameter Exception", JOptionPane.ERROR_MESSAGE);
						return;
					} catch (PropertyException e3) {
						JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract time generator properties for editing.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}

					if(editedTimeGenerator != null){
						if(!editedTimeGenerator.getName().equals(oldTimeGeneratorName)){
							//Time generator name changed
							//-> Remove old time generator from simulation components and add it again under the new name.
							//   Since the reference stays the same, this should not change anything with other components referring to this time generator.
							try {
								SimulationComponents.getInstance().removeCaseTimeGenerator(oldTimeGeneratorName);
							} catch (Exception e1) {
								JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot change name of time generator.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
						
						//Add new case time generator
						try {
							SimulationComponents.getInstance().addCaseTimeGenerator(editedTimeGenerator);
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot add new time generator to simulation components", "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						}
						
						//Find all simulations with reference to edited time generator
						Set<Simulation> simulationsWithReferenceToEditedTimeGenerator = new HashSet<Simulation>();
						for(Simulation simulation: SimulationComponents.getInstance().getSimulations()){
							if(simulation.getCaseTimeGenerator() == timeGenerator){
								simulationsWithReferenceToEditedTimeGenerator.add(simulation);
							}
						}
						
						Set<Simulation> simulationsWithNewTimeGeneratorReference = new HashSet<Simulation>();
						try{
							for(Simulation simulation: simulationsWithReferenceToEditedTimeGenerator){
								simulation.setCaseTimeGenerator(editedTimeGenerator);
								simulationsWithReferenceToEditedTimeGenerator.add(simulation);
							}
						} catch(Exception e1){
							//Revert changes
							for(Simulation simulation: simulationsWithNewTimeGeneratorReference){
								try {
									simulation.setCaseTimeGenerator(timeGenerator);
								} catch (Exception e2) {
									//Should not happen, since this is only the old reference the simulation had before
									JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot revert update of time generator reference for \""+simulation+"\"", "Internal Exception", JOptionPane.ERROR_MESSAGE);
									return;
								}
							}
							JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot update the time generator reference for simulation \""+simulation+"\"", "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						}
						
						updateTimeGeneratorBox();
						comboTimeGenerator.setSelectedItem(editedTimeGenerator.getName());
						
						updateVisibility();
					}
				}
			});
			btnEditTimeGenerator.setBounds(428, 249, 60, 29);
		}
		return btnEditTimeGenerator;
	}
	
	private JButton getOKButton(){
		if(btnOK == null){
			btnOK = new JButton("OK");
			btnOK.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					// Add all values to a dummy simulation object.
					// In case of inconsistencies, the dialog can be aborted without changing the original object.
					// -> Especially required to avoid messed up simulations in editing mode.
					Simulation dummySimulation = new Simulation();
					
					//Set simulation name	
					String simulationName = txtName.getText();
					if(simulationName == null || simulationName.isEmpty()){
						JOptionPane.showMessageDialog(SimulationDialog.this, "Affected field: Simulation name.\nReason: Null or empty value.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					Set<String> simulationNames = new HashSet<String>(SimulationComponents.getInstance().getSimulationNames());
					if(editMode){
						simulationNames.remove(simulation.getName());
					}
					if(simulationNames.contains(simulationName)){
						JOptionPane.showMessageDialog(SimulationDialog.this, "There is already a simulation with name \""+simulationName+"\"", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					dummySimulation.setName(simulationName);
					
					String fileName = txtLogName.getText();
					if(fileName == null || fileName.isEmpty()){
						JOptionPane.showMessageDialog(SimulationDialog.this, "Affected value: File name.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
						
//					String logPath = txtLogPath.getText();
//					if(logPath == null || logPath.isEmpty()){
//						JOptionPane.showMessageDialog(SimulationDialog.this, "Affected value: Log path.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
//						return;
//					}
					
					LogFormat logFormat = LogFormatFactory.getFormat(getLogFormatType());
					
					// Create log generator
					LogGenerator logGenerator = null;
					try {
						logGenerator = new TraceLogGenerator(logFormat, GeneralProperties.getInstance().getSimulationDirectory(), fileName);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(SimulationDialog.this, "Affected values: Log format/log path/log name.\nReason: "+e1.getMessage(), "Invalid parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					try {
						dummySimulation.setLogGenerator(logGenerator);
					} catch (ConfigurationException e1) {
						JOptionPane.showMessageDialog(SimulationDialog.this, "Configuration exception while setting log generator.\nReason: "+e1.getMessage(), "Invalid parameter", JOptionPane.ERROR_MESSAGE);
						return;
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception while setting log generator.\nReason: "+e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					//Get entry generation type and create entry generator accordingly.
					EntryGenerationType generationType = (comboEntryGenerator.getSelectedIndex() == 0) ? EntryGenerationType.SIMPLE : EntryGenerationType.DETAILED;
					
					LogEntryGenerator entryGenerator = null;
					if(generationType.equals(EntryGenerationType.DETAILED)){
						
						// Extract context
						if(comboContext.getSelectedItem() == null){
							JOptionPane.showMessageDialog(SimulationDialog.this, "Affected value: Context\nReason: No context chosen", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
							return;
						}
						String contextName = comboContext.getSelectedItem().toString();
						Context context;
						try {
							context = SimulationComponents.getInstance().getContext(contextName);
						} catch (ParameterException e1) {
							JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception on extracting context \""+contextName+"\" from simulation components.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						}
						if(context == null){
							JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception on extracting context \""+contextName+"\" from simulation components.\nReason: Unknown context.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						}
						
						//check if there are incompatible simulation runs
						Set<SimulationRun> incompatibleSimulationRuns = getIncompatibleSimulationRuns();
						if(!incompatibleSimulationRuns.isEmpty()){
							//There is at least one incompatible simulation run
							JOptionPane.showMessageDialog(SimulationDialog.this, "Context is not compatible with at least one simulation run: Missing activities.\nAffected simulation runs: "+ArrayUtils.toString(incompatibleSimulationRuns.toArray()), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
							return;
							
//							//Let the user choose if he wants to delete it.
//							int dialogResult = JOptionPane.showOptionDialog(SimulationDialog.this, "Proceed with deletion of incompatible simulation run?","Incompatible Simulation Run",
//					                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
//							if(dialogResult == JOptionPane.YES_OPTION){
//								//Remove incompatible simulation run
//								simulationRuns.remove(incompatibleSimulationRun);
//							}
						}
						
						// Extract Case Data Container
						if(comboDataContainer.getSelectedItem() == null){
							JOptionPane.showMessageDialog(SimulationDialog.this, "Affected value: Case data container\nReason: No case data container chosen", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
							return;
						}
						String containerName = comboDataContainer.getSelectedItem().toString();
						CaseDataContainer container;
						try {
							container = SimulationComponents.getInstance().getCaseDataContainer(containerName);
						} catch (ParameterException e1) {
							JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception on extracting data container \""+containerName+"\" from simulation components.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						}
						if(container == null){
							JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception on extracting data container \""+containerName+"\" from simulation components.\nReason: Unknown data container.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						}
						
						// Create detailed case data container using the previously extracted context and data container
						try {
							entryGenerator = new DetailedLogEntryGenerator(context, container);
						} catch (ConfigurationException e1) {
							JOptionPane.showMessageDialog(SimulationDialog.this, "Configuration exception while creating log entry generator.\nReason: "+e1.getMessage(), "Invalid parameter", JOptionPane.ERROR_MESSAGE);
							return;
						} catch (ParameterException e1) {
							JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception  while creating log entry generator.\nReason: "+e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						}
					} else {
						entryGenerator = new LogEntryGenerator();
					}
					
					try {
						dummySimulation.setLogEntryGenerator(entryGenerator);
					} catch (ConfigurationException e1) {
						JOptionPane.showMessageDialog(SimulationDialog.this, "Configuration exception while setting log entry generator.\nReason: "+e1.getMessage(), "Invalid parameter", JOptionPane.ERROR_MESSAGE);
						return;
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception  while setting log entry generator.\nReason: "+e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					//Set time generator
					if(comboTimeGenerator.getSelectedItem() == null){
						JOptionPane.showMessageDialog(SimulationDialog.this, "Affected value: Case time generator\nReason: No case time generator chosen", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					String timeGeneratorName = comboTimeGenerator.getSelectedItem().toString();
					CaseTimeGenerator timeGenerator = null;
					try{
						timeGenerator = SimulationComponents.getInstance().getCaseTimeGenerator(timeGeneratorName);
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception on extracting timeGenerator \""+timeGeneratorName+"\" from simulation components.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(timeGeneratorName == null){
						JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception on extracting context \""+timeGeneratorName+"\" from simulation components.\nReason: Unknown time generator.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					try {
						dummySimulation.setCaseTimeGenerator(timeGenerator);
					} catch (ConfigurationException e1) {
						JOptionPane.showMessageDialog(SimulationDialog.this, "Configuration exception while setting time generator.\nReason: "+e1.getMessage(), "Invalid parameter", JOptionPane.ERROR_MESSAGE);
						return;
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception  while setting time generator.\nReason: "+e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					// Set simulation runs
					try {
						dummySimulation.setSimulationRuns(simulationRuns.values());
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception  while sadding simulation runs.\nReason: "+e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					// Check if simulation is valid
					try {
						dummySimulation.checkValidity();
					} catch (ConfigurationException e1) {
						JOptionPane.showMessageDialog(SimulationDialog.this, "Actual configuration causes invalid state of simulation. \nReason: "+e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					simulation = dummySimulation;
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
				public void actionPerformed(ActionEvent arg0) {
					if(!editMode){
						simulation = null;
					}
					dispose();
				}
			});
			btnCancel.setActionCommand("Cancel");
		}
		return btnCancel;
	}
	
	
	//------- OTHER GUI COMPONENTS ----------------------------------------------------------------------------------------------------------
	
	private JList getSimulationRunList(){
		if(listSimulationRuns == null){
			listSimulationRuns = new JList();
			listSimulationRuns.setCellRenderer(new SimulationRunListCellRenderer());
			listSimulationRuns.setModel(listSimulationRunsModel);
			listSimulationRuns.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					if(listSimulationRuns.getSelectedValues() != null){
						for(Object selectedObject: listSimulationRuns.getSelectedValues()){
							simulationRuns.remove(((SimulationRun) selectedObject).getName());
						}
						updateSimulationRunList();
					}
				}
				
				@Override
				public void keyPressed(KeyEvent e) {}
			});
			updateSimulationRunList();
		}
		return listSimulationRuns;
	}
	
	private void updateSimulationRunList(){
		listSimulationRunsModel.clear();
		
		for(SimulationRun simulationRun: simulationRuns.values()){
			listSimulationRunsModel.addElement(simulationRun);
		}
		if(!listSimulationRunsModel.isEmpty())
			listSimulationRuns.setSelectedIndex(0);
	}
	
	private JTextField getLogNameField(){
		if(txtLogName == null){
			txtLogName = new JTextField();
			txtLogName.setText("ProcessLog");
			txtLogName.setColumns(10);
			txtLogName.setBounds(134, 46, 230, 28);
		}
		return txtLogName;
	}
	
	
	//------- FUNCTIONALTY ---------------------------------------------------------------------------
	
	private void initializeFields(){
		txtName.setText(simulation.getName());
		String fileName = new File(simulation.getLogGenerator().getFileName()).getName();
		fileName = fileName.substring(0, fileName.lastIndexOf('.'));
		txtLogName.setText(fileName);
//		txtLogPath.setText(simulation.getLogGenerator().getLogPath());
		comboLogFormat.setSelectedItem(LogFormatFactory.getType(simulation.getLogGenerator().getLogFormat()));
		if(simulation.getLogEntryGenerator() instanceof DetailedLogEntryGenerator){
			comboEntryGenerator.setSelectedIndex(1);
			DetailedLogEntryGenerator entryGenerator = (DetailedLogEntryGenerator) simulation.getLogEntryGenerator();
			comboContext.setSelectedItem(entryGenerator.getContext().getName());
			comboDataContainer.setSelectedItem(entryGenerator.getCaseDataContainer().getName());
		}
		comboTimeGenerator.setSelectedItem(simulation.getCaseTimeGenerator().getName());
		try {
			for(SimulationRun simulationRun: simulation.getSimulationRuns()){
				simulationRuns.put(simulationRun.getName(), simulationRun.clone());
			}
		} catch (ConfigurationException e) {
			JOptionPane.showMessageDialog(SimulationDialog.this, "Configuration exception during extraction of simulation runs.\n" + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
		}
		updateSimulationRunList();
		updateVisibility();
	}
	
	private void updateVisibility(){
		comboContext.setEnabled(comboEntryGenerator.getSelectedIndex() == 1);
		btnAddContext.setEnabled(comboEntryGenerator.getSelectedIndex() == 1);
		btnEditContext.setEnabled(comboEntryGenerator.getSelectedIndex() == 1);
		comboDataContainer.setEnabled(comboEntryGenerator.getSelectedIndex() == 1 && comboContext.getModel().getSelectedItem() != null);
		btnAddDataContainer.setEnabled(comboEntryGenerator.getSelectedIndex() == 1 && comboContext.getModel().getSelectedItem() != null);
		btnEditDataContainer.setEnabled(comboEntryGenerator.getSelectedIndex() == 1 && comboContext.getModel().getSelectedItem() != null);
//		btnAddSimulationRun.setEnabled(comboContext.getModel().getSelectedItem() != null);
//		btnEditSimulationRun.setEnabled(comboContext.getModel().getSelectedItem() != null);
	}
	
	private Set<SimulationRun> getIncompatibleSimulationRuns(){
		String contextName = comboContext.getSelectedItem().toString();
		Context context = null;
		try {
			context = SimulationComponents.getInstance().getContext(contextName);
		} catch (ParameterException e1) {
			JOptionPane.showMessageDialog(SimulationDialog.this, "Exception during simulation consistency check: Cannot extract context.\n" + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		Set<SimulationRun> incompatibleSimulationRuns = new HashSet<SimulationRun>();
		Set<String> contextActivities = context.getActivities();
		for(SimulationRun simulationRun: simulationRuns.values()){
			Set<String> simulationRunActivities = null;
			try {
				simulationRunActivities = PNUtils.getLabelSetFromTransitions(simulationRun.getPetriNet().getTransitions());
			} catch (ParameterException e1) {
				JOptionPane.showMessageDialog(SimulationDialog.this, "Exception during simulation consistency check: Cannot extract activities from net in simulation run\""+simulationRun.getName()+"\"", "Internal exception", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			if(!contextActivities.containsAll(simulationRunActivities)){
				incompatibleSimulationRuns.add(simulationRun);
			}
		}
		return incompatibleSimulationRuns;
	}
	
	/**
	 * Returns all known process activities, i.e.<br>
	 * the union of all transitions of all Petri nets within all simulation runs.
	 * @return
	 */
	private Set<String> getAllKnownProcessActivities(){
		Set<String> allKnownProcessActivities = new HashSet<String>();
		for(SimulationRun simulationRun: simulationRuns.values()){
			try {
				allKnownProcessActivities.addAll(PNUtils.getLabelSetFromTransitions(simulationRun.getPetriNet().getTransitions()));
			} catch (ParameterException e1) {
				JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception on collecting known activities from simulation runs:\n" + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}
		return allKnownProcessActivities;
	}
	
	private Context getContext() throws ParameterException{
		if(comboContext.getSelectedItem() == null)
			return null;
		return SimulationComponents.getInstance().getContext(comboContext.getSelectedItem().toString());
	}

	
	private LogFormatType getLogFormatType(){
		return LogFormatType.valueOf(comboLogFormat.getSelectedItem().toString());
	}
	
	public Simulation getSimulation(){
		return simulation;
	}
	
	public static Simulation showSimulationDialog(Window owner) throws ParameterException {
		SimulationDialog dialog = new SimulationDialog(owner);
		return dialog.getSimulation();
	}
	
	public static Simulation showSimulationDialog(Window owner, Simulation simulation) throws ParameterException {
		SimulationDialog dialog = new SimulationDialog(owner, simulation);
		return dialog.getSimulation();
	}
	
	private class SimulationRunListCellRenderer extends DefaultListCellRenderer{

		private static final long serialVersionUID = 3946538578411097430L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			try {
				setText(StringUtils.convertToHTML(value.toString()));
			} catch (ParameterException e) {
				setText(value.toString());
			}
			return result;
		}
		
	}
	
	private JButton getButtonShowActivities() {
		if (btnShowActivities == null) {
			btnShowActivities = new JButton("Show activities");
			btnShowActivities.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(listSimulationRuns.getSelectedValue() == null){
						return;
					}
					SimulationRun run = (SimulationRun) listSimulationRuns.getSelectedValue();
					StringBuilder builder = new StringBuilder();
					try {
						List<String> activityList = new ArrayList<String>(PNUtils.getLabelSetFromTransitions(run.getPetriNet().getTransitions()));
						Collections.sort(activityList);
						for(String activity: activityList){
							builder.append(activity);
							builder.append('\n');
						}
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract transition labels.\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					new StringDialog(SimulationDialog.this, builder.toString());
				}
			});
			btnShowActivities.setBounds(147, 486, 138, 29);
		}
		return btnShowActivities;
	}
	
	
	public static void main(String[] args) throws ParameterException {
//		SimulationComponents.getInstance().addContext(new Context("Context 1", new HashSet<String>(Arrays.asList("gerd"))));
		try {
			new SimulationDialog(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
