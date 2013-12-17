package de.uni.freiburg.iig.telematik.secsy.gui.dialog;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
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
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import de.invation.code.toval.graphic.dialog.AbstractDialog;
import de.invation.code.toval.graphic.dialog.ValueChooserDialog;
import de.invation.code.toval.graphic.renderer.AlternatingRowColorListCellRenderer;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.secsy.gui.GUIProperties;
import de.uni.freiburg.iig.telematik.secsy.gui.Hints;
import de.uni.freiburg.iig.telematik.secsy.gui.SimulationComponents;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.TransformerDialog;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.SimulationRun;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.SimulationRunProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerManager;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.AbstractTraceTransformer;
import de.uni.freiburg.iig.telematik.sepia.graphic.AbstractGraphicalPN;
import de.uni.freiburg.iig.telematik.sepia.parser.graphic.ParserDialog;
import de.uni.freiburg.iig.telematik.sepia.petrinet.AbstractPetriNet;
import de.uni.freiburg.iig.telematik.sepia.petrinet.pt.PTNet;
import de.uni.freiburg.iig.telematik.sepia.petrinet.pt.RandomPTTraverser;
import de.uni.freiburg.iig.telematik.sepia.util.PNUtils;



public class SimulationRunDialog extends AbstractDialog {

	private static final long serialVersionUID = 5957764226864259142L;
	
	public static final Dimension PREFERRED_SIZE = new Dimension(420, 460);
	
	private JButton btnAddTransformer;
	private JButton btnNewTransformer;
	private JButton btnEditTransformer;
	private JButton btnTransformerUp;
	private JButton btnTransformerDown;
	private JButton btnImportNet;
	private JTextField txtName;
	
	private JComboBox comboNet;
	
	private JSpinner spinPasses;
	
	private JList listTransformers;
	private DefaultListModel listTransformersModel;
	
	//---------------------------------------------------
	
	private List<AbstractTraceTransformer> transformers;
	
	public SimulationRunDialog(Window owner) throws Exception {
		super(owner);
	}
	
	public SimulationRunDialog(Window owner, SimulationRun simulationRun) throws Exception {
		super(owner, true, new Object[]{simulationRun});
	}
	
	@Override
	protected void initialize(Object... parameters) {
		listTransformersModel = new DefaultListModel();
		transformers = new ArrayList<AbstractTraceTransformer>();
		if(editMode){
			setDialogObject((SimulationRun) parameters[0]);
		}
	}
	
	@Override
	public Dimension getPreferredSize() {
		return PREFERRED_SIZE;
	}
	
	@Override
	protected void setTitle() {
		if(!editMode){
			setTitle("New Simulation Run");
		} else {
			setTitle("Edit Simulation Run");
		}
	}

	@Override
	protected void prepareEditing() {
		txtName.setText(getDialogObject().getName());
		comboNet.setSelectedItem(getDialogObject().getPetriNet().getName());
		spinPasses.setValue(getDialogObject().getPasses());
		for(AbstractTraceTransformer transformer: getDialogObject().getTraceTransformerManager().getTraceTransformers()){
			transformers.add(transformer);
		}
		updateListTransformers();
	}

	@Override
	protected void addComponents() {
		mainPanel().setBorder(GUIProperties.DEFAULT_DIALOG_BORDER);
		BoxLayout layout = new BoxLayout(mainPanel(), BoxLayout.PAGE_AXIS);
		mainPanel().setLayout(layout);
		mainPanel().add(getParameterPanel());
		mainPanel().add(Box.createVerticalStrut(5));
		mainPanel().add(new JSeparator(SwingConstants.HORIZONTAL));
		mainPanel().add(Box.createVerticalStrut(5));
		mainPanel().add(getTransformerPanel());
		setResizable(true);
	}
	
	private JPanel getTransformerPanel() {
		JPanel transformerPanel = new JPanel(new BorderLayout());
		
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel lblTransformer = new JLabel("Trace Transformer:");
		topPanel.add(lblTransformer);
		transformerPanel.add(topPanel, BorderLayout.PAGE_START);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(getListTransformers());
		transformerPanel.add(scrollPane, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		BoxLayout layout = new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS);
		buttonPanel.setLayout(layout);
		buttonPanel.add(getButtonAddTransformer());
		buttonPanel.add(getButtonNewTransformer());
		buttonPanel.add(getButtonEditTransformer());
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(getButtonTransformerUp());
		buttonPanel.add(getButtonTransformerDown());
		transformerPanel.add(buttonPanel, BorderLayout.PAGE_END);
		
		return transformerPanel;
	}

	private JPanel getParameterPanel() {
		JPanel parameterPanel = new JPanel();
		BoxLayout layout = new BoxLayout(parameterPanel, BoxLayout.PAGE_AXIS);
		parameterPanel.setLayout(layout);
		
		int labelWidth = 120;
		JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel lblName = new JLabel("Name:");
		lblName.setPreferredSize(new Dimension(labelWidth, GUIProperties.DEFAULT_LABEL_HEIGHT));
		lblName.setHorizontalAlignment(SwingConstants.TRAILING);
		namePanel.add(lblName);
		txtName = new JTextField();
		txtName.setText(SimulationRunProperties.defaultName);
		namePanel.add(txtName);
		parameterPanel.add(namePanel);
		
		JPanel netPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel lblPetriNet = new JLabel("Petri net:");
		lblPetriNet.setPreferredSize(new Dimension(labelWidth, GUIProperties.DEFAULT_LABEL_HEIGHT));
		lblPetriNet.setHorizontalAlignment(SwingConstants.TRAILING);
		netPanel.add(lblPetriNet);
		netPanel.add(getComboNet());
		netPanel.add(getButtonImportNet());
		parameterPanel.add(netPanel);
		
		JPanel traverserPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel lblTraverser = new JLabel("Traverser:");
		lblTraverser.setPreferredSize(new Dimension(labelWidth, GUIProperties.DEFAULT_LABEL_HEIGHT));
		lblTraverser.setHorizontalAlignment(SwingConstants.TRAILING);
		traverserPanel.add(lblTraverser);
		JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"RANDOM TRAVERSAL"}));
		comboBox.setToolTipText(Hints.hintRandomTraversal);
		traverserPanel.add(comboBox);
		parameterPanel.add(traverserPanel);
		
		JPanel passesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel lblPasses = new JLabel("Passes:");
		lblPasses.setPreferredSize(new Dimension(labelWidth, GUIProperties.DEFAULT_LABEL_HEIGHT));
		lblPasses.setHorizontalAlignment(SwingConstants.TRAILING);
		passesPanel.add(lblPasses);
		spinPasses = new JSpinner();
		SpinnerModel model = new SpinnerNumberModel(1000, 1, 1000000, 10); 
		spinPasses.setModel(model);
		passesPanel.add(spinPasses);
		parameterPanel.add(passesPanel);
		
		return parameterPanel;
	}

	private JComboBox getComboNet(){
		if(comboNet == null){
			comboNet = new JComboBox();
			updateComboNet();
		}
		return comboNet;
	}
	
	private void updateComboNet(){
		List<String> netNames = new ArrayList<String>();
		try {
			for(PTNet net: SimulationComponents.getInstance().getPetriNets()){
				netNames.add(net.getName());
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(SimulationRunDialog.this, "Internal exception: Cannot extract Petri nets from simulation components:\n" + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
		}
		comboNet.setModel(new DefaultComboBoxModel(netNames.toArray()));
	}
	
	private JList getListTransformers(){
		if(listTransformers == null){
			listTransformers = new JList(listTransformersModel);
			listTransformers.setToolTipText(Hints.hintTransformerList);
			listTransformers.setCellRenderer(new AlternatingRowColorListCellRenderer());
			listTransformers.setFixedCellHeight(20);
			listTransformers.setVisibleRowCount(10);
			listTransformers.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			listTransformers.setBorder(null);
			
			listTransformers.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
						removeSelectedTransformers();
						updateListTransformers();
					}
				}
				
				@Override
				public void keyPressed(KeyEvent e) {}
			});
		}
		updateListTransformers();
		return listTransformers;
	}
	
	private void updateListTransformers(){
		listTransformersModel.clear();
		for(AbstractTraceTransformer transformer: transformers){
			listTransformersModel.addElement(transformer);
		}
	}
	
	private void removeSelectedTransformers(){
		if(listTransformers.getSelectedValues() == null || listTransformers.getSelectedValues().length == 0){
			return;
		}
		for(Object selectedTransformer: listTransformers.getSelectedValues()){
			transformers.remove((AbstractTraceTransformer) selectedTransformer);
		}
	}
	
//	private Set<String> getTransformerNames(){
//		Set<String> transformerNames = new HashSet<String>();
//		for(AbstractTraceTransformer transformer: transformers){
//			transformerNames.add(transformer.getName());
//		}
//		return transformerNames;
//	}
//	
//	private AbstractTraceTransformer getTransformer(String transformerName){
//		for(AbstractTraceTransformer transformer: transformers){
//			if(transformer.getName().equals(transformerName)){
//				return transformer;
//			}
//		}
//		return null;
//	}
	
	private JButton getButtonAddTransformer(){
		if(btnAddTransformer == null){
			btnAddTransformer = new JButton("Add");
			btnAddTransformer.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(SimulationComponents.getInstance().containsTransformers()){
						List<String> transformerNames = new ArrayList<String>(SimulationComponents.getInstance().getTransformerNames());
						Collections.sort(transformerNames);
						List<String> chosenTransformers = null;
						try {
							chosenTransformers = ValueChooserDialog.showDialog(SimulationRunDialog.this, "Choose Existing Transformer", transformerNames, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						} catch (Exception e2) {
							JOptionPane.showMessageDialog(SimulationRunDialog.this, "<html>Cannot launch value chooser dialog dialog.<br>Reason: " + e2.getMessage() + "</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
						}
						if(chosenTransformers != null){
							for(String transformerName: chosenTransformers){
								try {
									transformers.add(SimulationComponents.getInstance().getTransformer(transformerName));
								} catch (ParameterException e1) {
									JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot add transformer \""+transformerName+"\"\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
					        		return;
								}
							}
							updateListTransformers();
						}
					} else {
						JOptionPane.showMessageDialog(SimulationRunDialog.this, "Simulation components do not contain any transformers yet.", "Simulation Components", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
				}
			});
		}
		return btnAddTransformer;
	}
	
	private JButton getButtonNewTransformer(){
		if(btnNewTransformer == null){
			btnNewTransformer = new JButton("New");
			btnNewTransformer.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(comboNet.getSelectedItem() == null){
						JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot add new transformer without Petri net.", "Missing Requirement", JOptionPane.ERROR_MESSAGE);
		        		return;
					}
					
					AbstractTraceTransformer newTransformer = null;
					try {
						newTransformer = TransformerDialog.showDialog(SimulationRunDialog.this, getActivities());
					} catch (Exception e2) {
						JOptionPane.showMessageDialog(SimulationRunDialog.this, "<html>Cannot launch transformer dialog.<br>Reason: "+e2.getMessage()+"</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
					}
					if(newTransformer == null){
						//User cancelled transformer dialog.
						return;
					}
					
					try {
						SimulationComponents.getInstance().addTransformer(newTransformer);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(SimulationRunDialog.this, "<html>Cannot add new transformer \""+newTransformer.getName()+"\" to simulation components.<br>Reason: "+ e1.getMessage() + "</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
		        		return;
					}
					
					transformers.add(newTransformer);
					updateListTransformers();
				}
			});
		}
		return btnNewTransformer;
	}
	
	private JButton getButtonEditTransformer(){
		if(btnEditTransformer == null){
			btnEditTransformer = new JButton("Edit");
			btnEditTransformer.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(listTransformers.getSelectedValue() == null){
						return;
					}
					AbstractTraceTransformer selectedTransformer = (AbstractTraceTransformer) listTransformers.getSelectedValue();
					
					String oldTransformerName = selectedTransformer.getName();
					AbstractTraceTransformer adjustedTransformer = null;
					try {
						adjustedTransformer = TransformerDialog.showDialog(SimulationRunDialog.this, getActivities(), selectedTransformer);
					} catch (Exception e2) {
						JOptionPane.showMessageDialog(SimulationRunDialog.this, "<html>Cannot launch transformer dialog.<br>Reason: "+e2.getMessage()+"</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
					}
					if(adjustedTransformer == null){
						//User cancelled the transformer dialog
						return;
					}
					if(!oldTransformerName.equals(adjustedTransformer.getName())){
						try {
							SimulationComponents.getInstance().removeTransformer(oldTransformerName);
							SimulationComponents.getInstance().addTransformer(adjustedTransformer);
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot add transformer\""+adjustedTransformer.getName()+"\" under new name to simulation components.\nReason: "+e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
				    		return;
						}
					}
					updateListTransformers();
				}
			});
		}
		return btnEditTransformer;
	}
	
	private JButton getButtonTransformerUp(){
		if(btnTransformerUp == null){
			btnTransformerUp = new JButton("Up");
			btnTransformerUp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(listTransformers.getSelectedValue() == null)
						return;
					int selectedIndex = listTransformers.getSelectedIndex();
					if(listTransformersModel.size() > 1 && selectedIndex > 0){
						Collections.swap(transformers, selectedIndex, selectedIndex-1);
						updateListTransformers();
						listTransformers.setSelectedIndex(selectedIndex-1);
					}
				}
			});
		}
		return btnTransformerUp;
	}
	
	private JButton getButtonTransformerDown(){
		if(btnTransformerDown == null){
			btnTransformerDown = new JButton("Down");
			btnTransformerDown.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(listTransformers.getSelectedValue() == null)
						return;
					int selectedIndex = listTransformers.getSelectedIndex();
					if(listTransformersModel.size() > 1 && selectedIndex < listTransformersModel.size()-1){
						Collections.swap(transformers, selectedIndex, selectedIndex+1);
						updateListTransformers();
						listTransformers.setSelectedIndex(selectedIndex+1);
					}
				}
			});
		}
		return btnTransformerDown;
	}
	
	private JButton getButtonImportNet(){
		if(btnImportNet == null){
			btnImportNet = new JButton("Import...");
			btnImportNet.addActionListener(new ActionListener() {
				
				@SuppressWarnings("rawtypes")
				@Override
				public void actionPerformed(ActionEvent e) {
					
					AbstractGraphicalPN importedNet = ParserDialog.showPetriNetDialog(SimulationRunDialog.this);
					if(importedNet != null){
						
			            AbstractPetriNet loadedNet = importedNet.getPetriNet();
						if(!(loadedNet instanceof PTNet)){
							JOptionPane.showMessageDialog(SimulationRunDialog.this,"Loaded Petri net is not a P/T Net, cannot proceed","Unexpected Petri net type", JOptionPane.ERROR_MESSAGE);
							return;
						}
						PTNet petriNet = (PTNet) loadedNet;
						
			            String netName = petriNet.getName();
			            try {
			            	while(netName == null || SimulationComponents.getInstance().getPetriNet(netName) != null){
			            		netName = JOptionPane.showInputDialog(SimulationRunDialog.this, "Name for the Petri net:", "");
			            	}
			            } catch (ParameterException e1) {
			            	JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot check if net name is already in use.\nReason: " + e1.getMessage(), "Internal Exeption", JOptionPane.ERROR_MESSAGE);
			        		return;
						}
			            try {
			            	if(!petriNet.getName().equals(netName))
			            		petriNet.setName(netName);
						} catch (ParameterException e2) {
							JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot change Petri net name to\""+netName+"\".\nReason: " + e2.getMessage(), "Internal Exeption", JOptionPane.ERROR_MESSAGE);
			        		return;
						}
			            
			            try {
							SimulationComponents.getInstance().addPetriNet(petriNet);
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot add imported net to simulation components.\nReason: " + e1.getMessage(), "Internal Exeption", JOptionPane.ERROR_MESSAGE);
			        		return;
						}
			            updateComboNet();
			        } else {
			            //User aborted the dialog.
			        }
				}
			});
		}
		return btnImportNet;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Set<String> getActivities(){
		AbstractPetriNet ptNet = null;
		try {
			ptNet = getPetriNet();
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Cannot extract Petri net from simulation run.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
    		return null;
		}
		if(ptNet == null){
			JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Petri net reference in simulation run is null.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
    		return null;
		}
		
		Set<String> activities = null;
		try {
			activities = PNUtils.getLabelSetFromTransitions(ptNet.getTransitions(), false);
		} catch (ParameterException e1) {
			JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Cannot extract activity names from Petri net \""+ptNet.getName()+"\".", "Internal Exception", JOptionPane.ERROR_MESSAGE);
    		return null;
		}
		return activities;
	}
	
	@SuppressWarnings("rawtypes")
	protected AbstractPetriNet getPetriNet(){
		//Get Petri net
		if (comboNet.getSelectedItem() == null) {
			JOptionPane.showMessageDialog(SimulationRunDialog.this, "No Petri net chosen.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		PTNet petriNet = null;
		try {
			petriNet = SimulationComponents.getInstance().getPetriNet(comboNet.getSelectedItem().toString());
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot extract Petri net from simulation components.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		if (petriNet == null) {
			JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot extract Petri net from simulation components.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return petriNet;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void okProcedure() {
		
		//Validate transformer name
		String runName = txtName.getText();
		
		//Get number of passes
		if(spinPasses.getValue() == null){
			JOptionPane.showMessageDialog(SimulationRunDialog.this, "Invalid value in field for number of passes.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
    		return;
		}
		Integer passes = null;
		try{
			passes = Integer.valueOf(spinPasses.getValue().toString());
		}catch(Exception ex){
			JOptionPane.showMessageDialog(SimulationRunDialog.this, "Invalid value in field for number of passes.\nValues does not seem to be an integer.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
    		return;
		}
		
		// Get transformers
		TraceTransformerManager transformerManager = new TraceTransformerManager();
		//Add transformers to transformer manager.
		for(AbstractTraceTransformer transformer: transformers){
			try{
				transformerManager.addTransformer(transformer);
			} catch(Exception ex){
				JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot add transformer \""+transformer.getName()+"\"", "Internal Exception", JOptionPane.ERROR_MESSAGE);
	    		return;
			}
		}
		
		AbstractPetriNet petriNet = getPetriNet();
		try {
			if(editMode){
				try {
					getDialogObject().setName(runName);
					getDialogObject().setPetriNet(petriNet);
					getDialogObject().setPasses(passes);
					getDialogObject().setTraceTransformerManager(transformerManager);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot change simulation run properties.\nReason: "+e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
		    		return;
				}
			} else {
				setDialogObject(new SimulationRun(petriNet, new RandomPTTraverser(petriNet), passes, transformerManager));
			}
		} catch (ParameterException e) {
			JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot create simulation run.\nReason: "+e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
    		return;
		}
		
		super.okProcedure();
	}

	@Override
	protected SimulationRun getDialogObject() {
		return (SimulationRun) super.getDialogObject();
	}
	
	public static SimulationRun showDialog(Window owner) throws Exception{
		SimulationRunDialog dialog = new SimulationRunDialog(owner);
		return dialog.getDialogObject();
	}
	
	public static SimulationRun showDialog(Window owner, SimulationRun simulationRun) throws Exception{
		SimulationRunDialog dialog = new SimulationRunDialog(owner, simulationRun);
		return dialog.getDialogObject();
	}

	@Override
	protected Border getBorder() {
		return GUIProperties.DEFAULT_DIALOG_BORDER;
	}

	
}
