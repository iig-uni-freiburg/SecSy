package gui.dialog;
import gui.misc.CustomListRenderer;
import gui.properties.GeneralProperties;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;


public class SimulationDirectoryDialog extends JDialog implements PropertyChangeListener {
	
	private static final long serialVersionUID = 2306027725394345926L;

	private final JPanel contentPanel = new JPanel();
	
	private JList stringList = null;
	private JButton btnOK = null;
	private JButton btnCancel = null;
	private DefaultListModel stringListModel = new DefaultListModel();
	
	private String simulationDirectory = null;
	private NewSimulationDirectoryAction newDirectoryAction = null;
	private OpenSimulationDirectoryAction openDirectoryAction = null;
	
	public SimulationDirectoryDialog(Window owner) {
		super(owner);
		setResizable(false);
		setBounds(100, 100, 411, 365);
		setModal(true);
		setLocationRelativeTo(owner);
		
		newDirectoryAction = new NewSimulationDirectoryAction(this);
		newDirectoryAction.addPropertyChangeListener(this);
		openDirectoryAction = new OpenSimulationDirectoryAction(this);
		openDirectoryAction.addPropertyChangeListener(this);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(20, 92, 370, 160);
		contentPanel.add(scrollPane);
		scrollPane.setViewportView(getValueList());
		
		JTextArea txtrThereAreNo = new JTextArea();
		txtrThereAreNo.setBackground(UIManager.getColor("Panel.background"));
		txtrThereAreNo.setText("Please choose the simulation directory to work with.\n\nSimulation directories are directories with a specific structure \nand are required to store simulation-related content.");
		txtrThereAreNo.setBounds(20, 16, 392, 64);
		contentPanel.add(txtrThereAreNo);
		
		JButton btnExistingDirectory = new JButton(openDirectoryAction);
		btnExistingDirectory.setBounds(88, 264, 157, 29);
		contentPanel.add(btnExistingDirectory);
		
		JButton btnNewButton = new JButton(newDirectoryAction);
		btnNewButton.setBounds(257, 264, 133, 29);
		contentPanel.add(btnNewButton);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		buttonPane.add(getButtonOK());
		buttonPane.add(getButtonCancel());
		
		
		setVisible(true);
	}
	
	private JButton getButtonOK(){
		if(btnOK == null){
			btnOK = new JButton("OK");
			btnOK.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
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
			});
			btnOK.setActionCommand("OK");
			getRootPane().setDefaultButton(btnOK);
		}
		return btnOK;
	}
	
	private JButton getButtonCancel(){
		if(btnCancel == null){
			btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					simulationDirectory = null;
					dispose();
				}
			});
			btnCancel.setActionCommand("Cancel");
		}
		return btnCancel;
	}
	
	private JList getValueList(){
		if(stringList == null){
			stringList = new JList(stringListModel);
			stringList.setCellRenderer(new CustomListRenderer());
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
	
	
	public static String showDialog(Window owner){
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
}
