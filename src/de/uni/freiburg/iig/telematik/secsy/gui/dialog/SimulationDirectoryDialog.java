package de.uni.freiburg.iig.telematik.secsy.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;

import de.invation.code.toval.graphic.dialog.AbstractDialog;
import de.invation.code.toval.graphic.renderer.AlternatingRowColorListCellRenderer;
import de.uni.freiburg.iig.telematik.secsy.gui.GUIProperties;
import de.uni.freiburg.iig.telematik.secsy.gui.action.AbstractSimulationDirectoryAction;
import de.uni.freiburg.iig.telematik.secsy.gui.action.NewSimulationDirectoryAction;
import de.uni.freiburg.iig.telematik.secsy.gui.action.OpenSimulationDirectoryAction;
import de.uni.freiburg.iig.telematik.secsy.gui.properties.GeneralProperties;


public class SimulationDirectoryDialog extends AbstractDialog implements PropertyChangeListener {
	
	private static final long serialVersionUID = 2306027725394345926L;
	
	public static final Dimension PREFERRED_SIZE = new Dimension(500, 300);
	
	private JList stringList;

	private DefaultListModel stringListModel;
	
	private String simulationDirectory;
	private NewSimulationDirectoryAction newDirectoryAction;
	private OpenSimulationDirectoryAction openDirectoryAction;
	
	public SimulationDirectoryDialog(Window owner) throws Exception {
		super(owner);
	}
	
	@Override
	protected void addComponents() {
		mainPanel().setBorder(GUIProperties.DEFAULT_DIALOG_BORDER);
		mainPanel().setLayout(new BorderLayout());
		
		JLabel label1 = new JLabel("Please choose the simulation directory to work with:");
		label1.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
		mainPanel().add(label1, BorderLayout.PAGE_START);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setViewportView(getValueList());
		mainPanel().add(scrollPane, BorderLayout.CENTER);
		
		mainPanel().add(new JLabel("Simulation directories are used to store simulation-related content."), BorderLayout.PAGE_END);
	}
	
	@Override
	protected void initialize(Object... parameters) {
		newDirectoryAction = new NewSimulationDirectoryAction(this);
		newDirectoryAction.addPropertyChangeListener(this);
		openDirectoryAction = new OpenSimulationDirectoryAction(this);
		openDirectoryAction.addPropertyChangeListener(this);
		stringListModel = new DefaultListModel();
	}

	@Override
	protected List<JButton> getLefthandButtons() {
		List<JButton> result = super.getLefthandButtons();
		result.add(new JButton(newDirectoryAction));
		result.add(new JButton(openDirectoryAction));
		return result;
	}

	@Override
	protected void setTitle() {
		setTitle("Choose Simulation Directory");
	}

	@Override
	protected void okProcedure() {
		if(!stringListModel.isEmpty()){
			if(stringList.getSelectedValue() == null){
				JOptionPane.showMessageDialog(SimulationDirectoryDialog.this, "Please choose a simulation directory.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
				return;
			}
			simulationDirectory = stringList.getSelectedValue().toString();
			dispose();
		} else {
			JOptionPane.showMessageDialog(SimulationDirectoryDialog.this, "No known entries, please create new simulation directory.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	private JList getValueList(){
		if(stringList == null){
			stringList = new JList(stringListModel);
			stringList.setCellRenderer(new AlternatingRowColorListCellRenderer());
			stringList.setFixedCellHeight(20);
			stringList.setVisibleRowCount(10);
			stringList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			stringList.setBorder(null);
			updateValueList();
		}
		return stringList;
	}
	
	private void updateValueList(){
		try {
			for(String knownDirectory: GeneralProperties.getInstance().getKnownSimulationDirectories()){
				stringListModel.addElement(knownDirectory);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(SimulationDirectoryDialog.this, "Cannot extract known simulation directories.\nReason: "+e.getMessage(), "Internal Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public String getSimulationDirectory(){
		return simulationDirectory;
	}
	
	@Override
	public Dimension getPreferredSize() {
		return PREFERRED_SIZE;
	}
	
	public static String showDialog(Window owner) throws Exception{
		SimulationDirectoryDialog activityDialog = new SimulationDirectoryDialog(owner);
		return activityDialog.getSimulationDirectory();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getSource() instanceof NewSimulationDirectoryAction || evt.getSource() instanceof OpenSimulationDirectoryAction){
			if(evt.getPropertyName().equals(AbstractSimulationDirectoryAction.PROPERTY_NAME_SIMULATION_DIRECTORY)){
				this.simulationDirectory = evt.getNewValue().toString();
			} else if(evt.getPropertyName().equals(AbstractSimulationDirectoryAction.PROPERTY_NAME_SUCCESS)){
				dispose();
			}
		}
	}

	@Override
	protected Border getBorder() {
		return GUIProperties.DEFAULT_DIALOG_BORDER;
	}
	
}
