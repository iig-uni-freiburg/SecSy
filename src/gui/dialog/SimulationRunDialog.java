package gui.dialog;

import gui.Hints;
import gui.SimulationComponents;
import gui.misc.CustomListRenderer;

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

import logic.simulation.SimulationRun;
import logic.simulation.properties.SimulationRunProperties;
import logic.transformation.TraceTransformerManager;
import logic.transformation.transformer.trace.AbstractTraceFilter;
import parser.PNMLFilter;
import parser.PNMLParser;
import petrinet.pt.PTNet;
import petrinet.pt.RandomPTTraverser;
import util.PNUtils;


public class SimulationRunDialog extends AbstractSimulationDialog {

	private static final long serialVersionUID = 5957764226864259142L;
	
	private JButton btnAddFilter = null;
	private JButton btnNewFilter = null;
	private JButton btnEditFilter = null;
	private JButton btnFilterUp = null;
	private JButton btnFilterDown = null;
	private JButton btnImportNet = null;
	private JTextField txtName = null;
	
	private JComboBox comboNet = null;
	
	private JSpinner spinPasses = null;
	
	private JList listFilters = null;
	private DefaultListModel listFiltersModel = null;
	
	//---------------------------------------------------
	
	private List<AbstractTraceFilter> filters = null;
	
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
		listFiltersModel = new DefaultListModel();
		filters = new ArrayList<AbstractTraceFilter>();
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
		for(AbstractTraceFilter filter: getDialogObject().getTraceFilterManager().getTraceTransformers()){
			filters.add(filter);
		}
		updateListFilters();
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
		
		JLabel lblFilter = new JLabel("Trace Filter:");
		lblFilter.setBounds(19, 174, 74, 16);
		mainPanel().add(lblFilter);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(19, 194, 382, 155);
		mainPanel().add(scrollPane);
		scrollPane.setViewportView(getListFilters());
		
		mainPanel().add(getButtonAddFilter());
		mainPanel().add(getButtonNewFilter());
		mainPanel().add(getButtonEditFilter());
		mainPanel().add(getButtonFilterUp());
		mainPanel().add(getButtonFilterDown());
		
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
	
	private JList getListFilters(){
		if(listFilters == null){
			listFilters = new JList(listFiltersModel);
			listFilters.setToolTipText(Hints.hintFilterList);
			listFilters.setCellRenderer(new CustomListRenderer());
			listFilters.setFixedCellHeight(20);
			listFilters.setVisibleRowCount(10);
			listFilters.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			listFilters.setBorder(null);
			
			listFilters.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					removeSelectedFilters();
					updateListFilters();
				}
				
				@Override
				public void keyPressed(KeyEvent e) {}
			});
		}
		updateListFilters();
		return listFilters;
	}
	
	private void updateListFilters(){
		listFiltersModel.clear();
		for(AbstractTraceFilter filter: filters){
			listFiltersModel.addElement(filter);
		}
	}
	
	private void removeSelectedFilters(){
		if(listFilters.getSelectedValues() == null || listFilters.getSelectedValues().length == 0){
			return;
		}
		for(Object selectedFilter: listFilters.getSelectedValues()){
			filters.remove((AbstractTraceFilter) selectedFilter);
		}
	}
	
	private Set<String> getFilterNames(){
		Set<String> filterNames = new HashSet<String>();
		for(AbstractTraceFilter filter: filters){
			filterNames.add(filter.getName());
		}
		return filterNames;
	}
	
	private AbstractTraceFilter getFilter(String filterName){
		for(AbstractTraceFilter filter: filters){
			if(filter.getName().equals(filterName)){
				return filter;
			}
		}
		return null;
	}
	
	private JButton getButtonAddFilter(){
		if(btnAddFilter == null){
			btnAddFilter = new JButton("Add");
			btnAddFilter.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(SimulationComponents.getInstance().containsFilters()){
						List<String> filterNames = new ArrayList<String>(SimulationComponents.getInstance().getFilterNames());
						Collections.sort(filterNames);
						List<String> chosenFilters = ValueChooserDialog.showDialog(SimulationRunDialog.this, "Choose Existing Filter", filterNames, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						if(chosenFilters != null){
							for(String filterName: chosenFilters){
								try {
									filters.add(SimulationComponents.getInstance().getFilter(filterName));
								} catch (ParameterException e1) {
									JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot add filter \""+filterName+"\"\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
					        		return;
								}
							}
							updateListFilters();
						}
					} else {
						JOptionPane.showMessageDialog(SimulationRunDialog.this, "Simulation components do not contain any filters yet.", "Simulation Components", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
				}
			});
			btnAddFilter.setBounds(20, 353, 80, 29);
		}
		return btnAddFilter;
	}
	
	private JButton getButtonNewFilter(){
		if(btnNewFilter == null){
			btnNewFilter = new JButton("New");
			btnNewFilter.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(comboNet.getSelectedItem() == null){
						JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot add new filter without Petri net.", "Missing Requirement", JOptionPane.ERROR_MESSAGE);
		        		return;
					}
					
					Set<String> activities = getActivities();
					if(activities == null){
						return;
					}
					
					AbstractTraceFilter newFilter = TransformerDialog.showDialog(SimulationRunDialog.this, activities);
					if(newFilter == null){
						//User cancelled filter dialog.
						return;
					}
					
					try {
						SimulationComponents.getInstance().addFilter(newFilter);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot add new filter \""+newFilter.getName()+"\" to simulation components.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
		        		return;
					}
					filters.add(newFilter);
					updateListFilters();
				}
			});
			btnNewFilter.setBounds(100, 353, 80, 29);
		}
		return btnNewFilter;
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
	
	private JButton getButtonEditFilter(){
		if(btnEditFilter == null){
			btnEditFilter = new JButton("Edit");
			btnEditFilter.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(listFilters.getSelectedValue() == null){
						return;
					}
					AbstractTraceFilter selectedFilter = (AbstractTraceFilter) listFilters.getSelectedValue();
					
					Set<String> activities = getActivities();
					if(activities == null){
						return;
					}
					
					String oldFilterName = selectedFilter.getName();
					AbstractTraceFilter adjustedFilter = TransformerDialog.showDialog(SimulationRunDialog.this, activities, selectedFilter);
					if(adjustedFilter == null){
						//User cancelled the filter dialog
						return;
					}
					if(!oldFilterName.equals(adjustedFilter.getName())){
						try {
							SimulationComponents.getInstance().removeFilter(oldFilterName);
							SimulationComponents.getInstance().addFilter(adjustedFilter);
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot add filter\""+adjustedFilter.getName()+"\" under new name to simulation components.\nReason: "+e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
				    		return;
						}
					}
					updateListFilters();
				}
			});
			btnEditFilter.setBounds(180, 353, 80, 29);
		}
		return btnEditFilter;
	}
	
	private JButton getButtonFilterUp(){
		if(btnFilterUp == null){
			btnFilterUp = new JButton("Up");
			btnFilterUp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(listFilters.getSelectedValue() == null)
						return;
					int selectedIndex = listFilters.getSelectedIndex();
					if(listFiltersModel.size() > 1 && selectedIndex > 0){
						Collections.swap(filters, selectedIndex, selectedIndex-1);
						updateListFilters();
						listFilters.setSelectedIndex(selectedIndex-1);
					}
				}
			});
			btnFilterUp.setBounds(280, 353, 60, 29);
		}
		return btnFilterUp;
	}
	
	private JButton getButtonFilterDown(){
		if(btnFilterDown == null){
			btnFilterDown = new JButton("Down");
			btnFilterDown.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(listFilters.getSelectedValue() == null)
						return;
					int selectedIndex = listFilters.getSelectedIndex();
					if(listFiltersModel.size() > 1 && selectedIndex < listFiltersModel.size()-1){
						Collections.swap(filters, selectedIndex, selectedIndex+1);
						updateListFilters();
						listFilters.setSelectedIndex(selectedIndex+1);
					}
				}
			});
			btnFilterDown.setBounds(340, 353, 60, 29);
		}
		return btnFilterDown;
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
		
		//Validate filter name
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
		
		// Get filters
		TraceTransformerManager filterManager = new TraceTransformerManager();
		//Add filters to filter manager.
		for(AbstractTraceFilter filter: filters){
			try{
				filterManager.addFilter(filter);
			} catch(Exception ex){
				JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot add filter \""+filter.getName()+"\"", "Internal Exception", JOptionPane.ERROR_MESSAGE);
	    		return;
			}
		}
		
		try {
			if(editMode){
				try {
					getDialogObject().setName(runName);
					getDialogObject().setPetriNet(petriNet);
					getDialogObject().setPasses(passes);
					getDialogObject().setTraceFilterManager(filterManager);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot change simulation run properties.\nReason: "+e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
		    		return;
				}
			} else {
				setDialogObject(new SimulationRun(petriNet, new RandomPTTraverser(petriNet), passes, filterManager));
			}
			System.out.println(getDialogObject().getTraceFilterManager().getTraceTransformers().size());
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
