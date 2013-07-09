package de.uni.freiburg.iig.telematik.secsy.gui.dialog;


import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.secsy.gui.Hints;
import de.uni.freiburg.iig.telematik.secsy.gui.SimulationComponents;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.TransformerDialog;
import de.uni.freiburg.iig.telematik.secsy.gui.misc.CustomListRenderer;
import de.uni.freiburg.iig.telematik.sepia.parser.PNMLFilter;
import de.uni.freiburg.iig.telematik.sepia.parser.PNMLParser;
import de.uni.freiburg.iig.telematik.sepia.petrinet.pt.PTNet;
import de.uni.freiburg.iig.telematik.sepia.petrinet.pt.RandomPTTraverser;
import de.uni.freiburg.iig.telematik.sepia.util.PNUtils;

import logic.simulation.SimulationRun;
import logic.simulation.properties.SimulationRunProperties;
import logic.transformation.TraceTransformerManager;
import logic.transformation.transformer.trace.AbstractTraceTransformer;


public class SimulationRunDialog extends AbstractSimulationDialog {

	private static final long serialVersionUID = 5957764226864259142L;
	
	private JButton btnAddTransformer = null;
	private JButton btnNewTransformer = null;
	private JButton btnEditTransformer = null;
	private JButton btnTransformerUp = null;
	private JButton btnTransformerDown = null;
	private JButton btnImportNet = null;
	private JTextField txtName = null;
	
	private JComboBox comboNet = null;
	
	private JSpinner spinPasses = null;
	
	private JList listTransformers = null;
	private DefaultListModel listTransformersModel = null;
	
	//---------------------------------------------------
	
	private List<AbstractTraceTransformer> transformers = null;
	
	/**
	 * @wbp.parser.constructor
	 */
	public SimulationRunDialog(Window owner) {
		super(owner);
	}
	
	public SimulationRunDialog(Window owner, SimulationRun simulationRun) {
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
	protected void setBounds() {
		setBounds(100, 100, 420, 460);
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
		// TODO Auto-generated method stub
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
		
		mainPanel().add(getComboNet());
		
		JLabel lblName = new JLabel("Name:");
		lblName.setHorizontalAlignment(SwingConstants.TRAILING);
		lblName.setBounds(19, 20, 61, 27);
		mainPanel().add(lblName);
		
		JLabel lblPetriNet = new JLabel("Petri net:");
		lblPetriNet.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPetriNet.setBounds(19, 54, 61, 27);
		mainPanel().add(lblPetriNet);
		
		JLabel lblTraverser = new JLabel("Traverser:");
		lblTraverser.setHorizontalAlignment(SwingConstants.TRAILING);
		lblTraverser.setBounds(6, 85, 74, 27);
		mainPanel().add(lblTraverser);

		JLabel lblPasses = new JLabel("Passes:");
		lblPasses.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPasses.setBounds(6, 118, 74, 27);
		mainPanel().add(lblPasses);
		
		txtName = new JTextField();
		txtName.setText(SimulationRunProperties.defaultName);
		txtName.setBounds(92,20,218,27);
		mainPanel().add(txtName);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"RANDOM TRAVERSAL"}));
		comboBox.setBounds(92, 85, 218, 27);
		comboBox.setToolTipText(Hints.hintRandomTraversal);
		mainPanel().add(comboBox);
		
		spinPasses = new JSpinner();
		spinPasses.setBounds(92, 118, 118, 27);
		SpinnerModel model = new SpinnerNumberModel(1000, 1, 1000000, 10); 
		spinPasses.setModel(model);
		mainPanel().add(spinPasses);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(19, 155, 382, 12);
		mainPanel().add(separator);
		
		JLabel lblTransformer = new JLabel("Trace Transformer:");
		lblTransformer.setBounds(19, 174, 74, 16);
		mainPanel().add(lblTransformer);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(19, 194, 382, 155);
		mainPanel().add(scrollPane);
		scrollPane.setViewportView(getListTransformers());
		
		mainPanel().add(getButtonAddTransformer());
		mainPanel().add(getButtonNewTransformer());
		mainPanel().add(getButtonEditTransformer());
		mainPanel().add(getButtonTransformerUp());
		mainPanel().add(getButtonTransformerDown());
		
		mainPanel().add(getButtonImportNet());
	}
	
	private JComboBox getComboNet(){
		if(comboNet == null){
			comboNet = new JComboBox();
			comboNet.setBounds(92, 54, 218, 27);
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
			listTransformers.setCellRenderer(new CustomListRenderer());
			listTransformers.setFixedCellHeight(20);
			listTransformers.setVisibleRowCount(10);
			listTransformers.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			listTransformers.setBorder(null);
			
			listTransformers.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					removeSelectedTransformers();
					updateListTransformers();
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
	
	private Set<String> getTransformerNames(){
		Set<String> transformerNames = new HashSet<String>();
		for(AbstractTraceTransformer transformer: transformers){
			transformerNames.add(transformer.getName());
		}
		return transformerNames;
	}
	
	private AbstractTraceTransformer getTransformer(String transformerName){
		for(AbstractTraceTransformer transformer: transformers){
			if(transformer.getName().equals(transformerName)){
				return transformer;
			}
		}
		return null;
	}
	
	private JButton getButtonAddTransformer(){
		if(btnAddTransformer == null){
			btnAddTransformer = new JButton("Add");
			btnAddTransformer.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(SimulationComponents.getInstance().containsTransformers()){
						List<String> transformerNames = new ArrayList<String>(SimulationComponents.getInstance().getTransformerNames());
						Collections.sort(transformerNames);
						List<String> chosenTransformers = ValueChooserDialog.showDialog(SimulationRunDialog.this, "Choose Existing Transformer", transformerNames, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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
			btnAddTransformer.setBounds(20, 353, 80, 29);
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
					
					Set<String> activities = getActivities();
					if(activities == null){
						return;
					}
					
					AbstractTraceTransformer newTransformer = TransformerDialog.showDialog(SimulationRunDialog.this, activities);
					if(newTransformer == null){
						//User cancelled transformer dialog.
						return;
					}
					
					try {
						SimulationComponents.getInstance().addTransformer(newTransformer);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot add new transformer \""+newTransformer.getName()+"\" to simulation components.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
		        		return;
					}
					transformers.add(newTransformer);
					updateListTransformers();
				}
			});
			btnNewTransformer.setBounds(100, 353, 80, 29);
		}
		return btnNewTransformer;
	}
	
	private Set<String> getActivities(){
		PTNet ptNet = null;
		try {
			ptNet = SimulationComponents.getInstance().getPetriNet(comboNet.getSelectedItem().toString());
		} catch (ParameterException e1) {
			JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot extract Petri net \""+comboNet.getSelectedItem().toString()+"\" from simulation components.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
    		return null;
		}
		if(ptNet == null){
			JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot extract Petri net \""+comboNet.getSelectedItem().toString()+"\" from simulation components.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
    		return null;
		}
		
		Set<String> activities = null;
		try {
			activities = PNUtils.getLabelSetFromTransitions(ptNet.getTransitions());
		} catch (ParameterException e1) {
			JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot extract activity names from Petri net \""+comboNet.getSelectedItem().toString()+"\".", "Internal Exception", JOptionPane.ERROR_MESSAGE);
    		return null;
		}
		return activities;
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
					
					Set<String> activities = getActivities();
					if(activities == null){
						return;
					}
					
					String oldTransformerName = selectedTransformer.getName();
					AbstractTraceTransformer adjustedTransformer = TransformerDialog.showDialog(SimulationRunDialog.this, activities, selectedTransformer);
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
			btnEditTransformer.setBounds(180, 353, 80, 29);
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
			btnTransformerUp.setBounds(280, 353, 60, 29);
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
			btnTransformerDown.setBounds(340, 353, 60, 29);
		}
		return btnTransformerDown;
	}
	
	private JButton getButtonImportNet(){
		if(btnImportNet == null){
			btnImportNet = new JButton("Import...");
			btnImportNet.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					JOptionPane.showMessageDialog(SimulationRunDialog.this, "Transitions with names starting with \"_\" will be interpreted as silent transitions.\n" +
																			"(Their firing will not result in the generation of s log event.)\n\n" +
																			"Transition labels (not names/IDs!) will be interpreted as process activities.\n" +
																			"This way, it is possible to consider duplicated activities in processes.", "Petri net import", JOptionPane.INFORMATION_MESSAGE);
					
					JFileChooser fc = new JFileChooser();
					fc.setAcceptAllFileFilterUsed(false);
					fc.addChoosableFileFilter(new PNMLFilter());
					int returnValue = fc.showOpenDialog(SimulationRunDialog.this);
					if (returnValue == JFileChooser.APPROVE_OPTION) {
			            File file = fc.getSelectedFile();
			            
			            //Try to import the Petri net.
			            PTNet petriNet = null;
			            try{
			            	petriNet = PNMLParser.parsePNML(file.getAbsolutePath(), true);
			            } catch(Exception ex){
			            	JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot parse Petri net.\nReason: " + ex.getMessage(), "Parsing Exeption", JOptionPane.ERROR_MESSAGE);
			        		return;
			            }
			            String netName = petriNet.getName();
			            try {
			            	while(netName == null || SimulationComponents.getInstance().getPetriNet(netName) != null){
			            		netName = JOptionPane.showInputDialog(SimulationRunDialog.this, "Name for the Petri net:", file.getName().substring(0, file.getName().lastIndexOf(".")));
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
			btnImportNet.setBounds(311, 54, 90, 27);
		}
		return btnImportNet;
	}
	
	@Override
	protected void okProcedure() {
		
		//Get Petri net
		if(comboNet.getSelectedItem() == null){
			JOptionPane.showMessageDialog(SimulationRunDialog.this, "No Petri net chosen.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
    		return;
		}
		PTNet petriNet = null;
		try {
			petriNet = SimulationComponents.getInstance().getPetriNet(comboNet.getSelectedItem().toString());
		} catch(Exception ex){
			JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot extract Petri net from simulation components.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
    		return;
		}
		if(petriNet == null){
			JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot extract Petri net from simulation components.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
    		return;
		}
		
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
			System.out.println(getDialogObject().getTraceTransformerManager().getTraceTransformers().size());
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
	
	public static SimulationRun showDialog(Window owner){
		SimulationRunDialog dialog = new SimulationRunDialog(owner);
		return dialog.getDialogObject();
	}
	
	public static SimulationRun showDialog(Window owner, SimulationRun simulationRun){
		SimulationRunDialog dialog = new SimulationRunDialog(owner, simulationRun);
		return dialog.getDialogObject();
	}
	
	public static void main(String[] args) throws Exception{
		PTNet ptNet = new PTNet();
		ptNet.setName("net 1");
		SimulationRun run1 = new SimulationRun(ptNet, 100);
		System.out.println(run1.getPetriNet().getName());
		System.out.println(run1.getPasses());
		new SimulationRunDialog(null, run1);
		System.out.println(run1.getPetriNet().getName());
		System.out.println(run1.getPasses());
	}

	
}
