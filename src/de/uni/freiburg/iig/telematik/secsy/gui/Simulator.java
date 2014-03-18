package de.uni.freiburg.iig.telematik.secsy.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.secsy.gui.action.CloseAction;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.ExecutionDialog;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.MessageDialog;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.SimulationDialog;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.SimulationDirectoryDialog;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.TimeFrameDialog;
import de.uni.freiburg.iig.telematik.secsy.gui.properties.GeneralProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.ConfigurationException;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.Simulation;
import de.uni.freiburg.iig.telematik.sepia.graphic.AbstractGraphicalPN;
import de.uni.freiburg.iig.telematik.sepia.parser.graphic.PNParserDialog;
import de.uni.freiburg.iig.telematik.sepia.petrinet.AbstractPetriNet;
import de.uni.freiburg.iig.telematik.sepia.petrinet.pt.PTNet;

public class Simulator extends JFrame {

	private static final long serialVersionUID = -8445477529677450528L;
	
	private static final Dimension PREFERRED_SIZE = new Dimension(500,500);
	
	private final CloseAction closeAction = new CloseAction();
	
	private JButton btnRun;
	private JButton btnTimeFrame;
	private JButton btnNewSimulation;
	private JButton btnEditSimulation;
	private JComboBox comboSimulation;
	private JTextArea areaSimulation;
	private JPanel contentPanel = new JPanel(new BorderLayout());

	public Simulator() {
		//Check if there is a path to a simulation directory.
		if(!checkSimulationDirectory()){
			//There is no path and it is either not possible to set a path or the user aborted the corresponding dialog.
			System.exit(0);
		}
		//Trigger the loading of simulation components
		SimulationComponents.getInstance();
		
		setPreferredSize(PREFERRED_SIZE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setUpGUI();
		pack();
		
		setLocationRelativeTo(null);
	}
	
	private boolean checkSimulationDirectory(){
		try {
			GeneralProperties.getInstance().getSimulationDirectory();
			return true;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Internal exception: Cannot load/create general property file:\n" + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (PropertyException e) {
			// There is no recent simulation directory
			// -> Let the user choose a path for the simulation directory
			return chooseSimulationDirectory();
		} catch (ParameterException e) {
			// Value for simulation directory is invalid, possibly due to moved directories
			// -> Remove entry for actual simulation directory
			try {
				GeneralProperties.getInstance().removeSimulationDirectory();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, "Internal exception: Cannot fix corrupt property entries:\n" + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			// -> Let the user choose a path for the simulation directory
			return chooseSimulationDirectory();
		}
	}
	
	private boolean chooseSimulationDirectory(){
		String simulationDirectory = null;
		try {
			simulationDirectory = SimulationDirectoryDialog.showDialog(null);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "<html>Cannot start simulation directory dialog.<br>Reason: "+e.getMessage()+"</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
		}
		if(simulationDirectory == null)
			return false;
		try {
			GeneralProperties.getInstance().setSimulationDirectory(simulationDirectory);
			return true;
		} catch (ParameterException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (PropertyException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage(), "Property Exception", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	private void setUpGUI() {
		setJMenuBar(getMenu());
		setContentPane(contentPanel);
		
		JPanel topPanel = new JPanel();
		BoxLayout topPanelLayout = new BoxLayout(topPanel, BoxLayout.LINE_AXIS);
		topPanel.setLayout(topPanelLayout);
		topPanel.add(new JLabel("Simulation:"));
		topPanel.add(getComboSimulation());
		topPanel.add(getButtonNewSimulation());
		topPanel.add(getButtonEditSimulation());
		topPanel.add(Box.createHorizontalGlue());
		topPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
		contentPanel.add(topPanel, BorderLayout.PAGE_START);
		
		JPanel midPanel = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane();
		areaSimulation = new JTextArea();
		areaSimulation.setFont(new Font("Monospaced", Font.PLAIN, 12));
		scrollPane.setViewportView(areaSimulation);
		midPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		midPanel.add(scrollPane, BorderLayout.CENTER);
		contentPanel.add(midPanel, BorderLayout.CENTER);
		updateSimulationArea();
		
		JPanel bottomPanel = new JPanel();
		BoxLayout bottomPanelLayout = new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS);
		bottomPanel.setLayout(bottomPanelLayout);
		bottomPanel.add(Box.createHorizontalGlue());
		bottomPanel.add(getButtonTimeFrame());
		bottomPanel.add(getButtonRun());
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		contentPanel.add(bottomPanel, BorderLayout.PAGE_END);
		
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
				try {
					SimulationComponents.getInstance().updateFiles();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Internal exception while storing simulation components to disk.\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					GeneralProperties.getInstance().store();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Cannot store general properties to disk.\nReason: " + e1.getMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			
			@Override
			public void windowClosed(WindowEvent e) {}
			
			@Override
			public void windowActivated(WindowEvent e) {}
		});
		
		setVisible(true);
	}
	
	private JMenuBar getMenu(){
		JMenuBar menuBar = new JMenuBar();
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmSwitch = new JMenuItem("Switch Simulation Directory");
		mntmSwitch.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String simulationDirectory = null;
				try {
					simulationDirectory = SimulationDirectoryDialog.showDialog(Simulator.this);
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(Simulator.this, "<html>Cannot start simulation directory dialog.<br>Reason: "+e2.getMessage()+"</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
				}
				if(simulationDirectory == null){
					return;
				}
				try {
					if(!GeneralProperties.getInstance().getSimulationDirectory().equals(simulationDirectory)){
						GeneralProperties.getInstance().setSimulationDirectory(simulationDirectory);
					}
				} catch (Exception e1) {
					return;
				}
				
				updateSimulationBox();
				updateSimulationArea();
			}
		});
		mntmSwitch.setName("Switch Simulation Directory");
		mntmSwitch.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0));
		mnFile.add(mntmSwitch);
		
		JMenuItem mntmClose_1 = new JMenuItem("Close");
		mntmClose_1.setAction(closeAction);
		mntmClose_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0));
		mnFile.add(mntmClose_1);
		
		JMenu mnImport = new JMenu("Import");
		menuBar.add(mnImport);
		
		JMenuItem mntmPetriNetpnml = new JMenuItem("Petri net (PNML)...");
		mntmPetriNetpnml.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0));
		mntmPetriNetpnml.addActionListener(new ActionListener() {
			
			@SuppressWarnings({ "rawtypes"})
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractGraphicalPN importedNet = PNParserDialog.showPetriNetDialog(Simulator.this);
				if(importedNet != null){
					
		            AbstractPetriNet loadedNet = importedNet.getPetriNet();
					if(!(loadedNet instanceof PTNet)){
						JOptionPane.showMessageDialog(Simulator.this,"Loaded Petri net is not a P/T Net, cannot proceed","Unexpected Petri net type", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(!(loadedNet instanceof PTNet)){
						JOptionPane.showMessageDialog(Simulator.this,"Loaded Petri net is not a P/T Net, cannot proceed","Unexpected Petri net type", JOptionPane.ERROR_MESSAGE);
						return;
					}
					PTNet petriNet = (PTNet) loadedNet;
					
					String netName = petriNet.getName();
					try {
						while (netName == null || SimulationComponents.getInstance().getPetriNet(netName) != null) {
							netName = JOptionPane.showInputDialog(Simulator.this, "Name for the Petri net:", "");
						}
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(Simulator.this, "Cannot check if net name is already in use.\nReason: " + e1.getMessage(), "Internal Exeption", JOptionPane.ERROR_MESSAGE);
						return;
					}
					try {
						if (!petriNet.getName().equals(netName))
							petriNet.setName(netName);
					} catch (ParameterException e2) {
						JOptionPane.showMessageDialog(Simulator.this, "Cannot change Petri net name to\"" + netName + "\".\nReason: " + e2.getMessage(), "Internal Exeption", JOptionPane.ERROR_MESSAGE);
						return;
					}

					try {
						SimulationComponents.getInstance().addPetriNet(petriNet);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(Simulator.this, "Cannot add imported net to simulation components.\nReason: " + e1.getMessage(), "Internal Exeption", JOptionPane.ERROR_MESSAGE);
						return;
					}
				} else {
					// User aborted the dialog.
				}
			}
		});
		mnImport.add(mntmPetriNetpnml);
		getContentPane().setLayout(null);
		
		JMenu mnWindow = new JMenu("Window");
		menuBar.add(mnWindow);
		
		JMenuItem mntmShowMessageDialog = new JMenuItem("Show message dialog");
		mntmShowMessageDialog.setAction(new AbstractAction("Show message dialog") {

			private static final long serialVersionUID = 4233471579891611070L;

			@Override
			public void actionPerformed(ActionEvent e) {
				MessageDialog.getInstance().setVisible(true);
			}
		});
		mntmShowMessageDialog.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0));
		mnWindow.add(mntmShowMessageDialog);
		
		return menuBar;
	}
	
	private JButton getButtonEditSimulation(){
		if(btnEditSimulation == null){
			btnEditSimulation = new JButton("Edit");
			btnEditSimulation.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(comboSimulation.getSelectedItem() == null){
						JOptionPane.showMessageDialog(Simulator.this, "No simulation chosen.", "Invalid parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					String chosenSimulation = comboSimulation.getSelectedItem().toString();
					
					Simulation simulation = null;
					try {
						simulation = SimulationComponents.getInstance().getSimulation(chosenSimulation);
					} catch (ParameterException e2) {
						// Cannot happen, since "chosenSimulation" is not null.
						JOptionPane.showMessageDialog(Simulator.this, "Cannot extract simulation: " + chosenSimulation, "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(simulation == null){
						JOptionPane.showMessageDialog(Simulator.this, "Cannot extract simulation \""+chosenSimulation+"\" from simulation components", "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					String oldSimulationName = simulation.getName();
					Simulation editedSimulation = null;
					try {
						editedSimulation = SimulationDialog.showSimulationDialog(Simulator.this, simulation);
					} catch (Exception e2) {
						JOptionPane.showMessageDialog(Simulator.this, "<html>Cannot launch simulation dialog.<br>Reason: "+e2.getMessage()+"</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(editedSimulation != null){
						if(!editedSimulation.getName().equals(oldSimulationName)){
							//Simulation name changed
							//-> Remove old simulation from simulation components and add it again under the new name.
							try {
								SimulationComponents.getInstance().removeSimulation(oldSimulationName);
							} catch (Exception e1) {
								JOptionPane.showMessageDialog(Simulator.this, "Cannot change name of simulation.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
						
						try {
							SimulationComponents.getInstance().addSimulation(editedSimulation);
						} catch (ParameterException e1) {
							JOptionPane.showMessageDialog(Simulator.this, "Cannot take over adjusted simulation properties.\nReason: "+e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						} catch (IOException e1) {
							JOptionPane.showMessageDialog(Simulator.this, "Cannot store edited simulation to disk.\nReason: "+e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						} catch (PropertyException e1) {
							JOptionPane.showMessageDialog(Simulator.this, "Cannot set properties for edited simulation.\nReason: "+e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						}
						
						updateSimulationBox();
						updateSimulationArea();
						
						comboSimulation.setSelectedItem(editedSimulation.getName());
					} else {
						//User cancelled the edit-dialog
					}
				}
			});
		}
		return btnEditSimulation;
	}
	
	private JButton getButtonNewSimulation(){
		if(btnNewSimulation == null){
			btnNewSimulation = new JButton("New");
			btnNewSimulation.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					Simulation newSimulation = null;
					try {
						newSimulation = SimulationDialog.showSimulationDialog(Simulator.this);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "<html>Cannot launch simulation dialog.<br>Reason: " + e1.getMessage()+"</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(newSimulation != null){
						try {
							SimulationComponents.getInstance().addSimulation(newSimulation);
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(Simulator.this, "Internal exception on adding simulation to simulation components.\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						}
						updateSimulationBox();
						updateSimulationArea();
					}
					
				}
			});
		}
		return btnNewSimulation;
	}
	
	private JButton getButtonRun(){
		if(btnRun == null){
			btnRun = new JButton("Run");
			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Simulation simulation = getSimulation();
					if(simulation != null){
						if(!simulation.containsSimulationRuns()){
							JOptionPane.showMessageDialog(Simulator.this, "Simulation does not contain any simulation runs.", "Empty Simulation", JOptionPane.ERROR_MESSAGE);
							return;
						}
						String logPath = getLogPath();
						if(logPath != null){
							try {
								simulation.getLogGenerator().setLogPath(logPath);
							} catch (Exception e1) {
								JOptionPane.showMessageDialog(Simulator.this, "Cannot set log path.\nReason: "+e1.getMessage(), "Configuration Exception", JOptionPane.ERROR_MESSAGE);
								return;
							}
//							new Thread(new SimulationThread(simulation)).start();
							try {
								new ExecutionDialog(Simulator.this, simulation);
							} catch (ParameterException e1) {
								JOptionPane.showMessageDialog(Simulator.this, "<html>Cannot launch execution dialog dialog.<br>Reason: " + e1.getMessage() + "</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});
		}
		return btnRun;
	}
	
	private String getLogPath(){
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Choose output directory for synthesized log.");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fileChooser.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            return file.getAbsolutePath()+"/";
        } else {
        	return null;
        }
	}
	
	private JButton getButtonTimeFrame(){
		if(btnTimeFrame == null){
			btnTimeFrame = new JButton("Time Frame");
			btnTimeFrame.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Simulation simulation = getSimulation();
					if(simulation == null)
						return;
					try {
						new TimeFrameDialog(Simulator.this, simulation);
					} catch (ConfigurationException e1) {
						JOptionPane.showMessageDialog(Simulator.this, "<html>Cannot launch time frame dialog dialog.<br>Reason: " + e1.getMessage() + "</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
		}
		return btnTimeFrame;
	}
	
	private JComboBox getComboSimulation(){
		if(comboSimulation == null){
			comboSimulation = new JComboBox();
			comboSimulation.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED){
						updateSimulationArea();
					}
				}
			});
			updateSimulationBox();
		}
		return comboSimulation;
	}
	
	
	private void updateSimulationBox(){
		List<String> simulationNames = new ArrayList<String>();
		for(Simulation simulation: SimulationComponents.getInstance().getSimulations()){
			simulationNames.add(simulation.getName());
		}
		comboSimulation.setModel(new DefaultComboBoxModel(simulationNames.toArray()));
	}
	
	private void updateSimulationArea(){
		Simulation simulation = getSimulation();
		if(simulation == null){
			areaSimulation.setText(null);
		} else {
			areaSimulation.setText(simulation.toString());
		}
	}
	
	private Simulation getSimulation(){
		if(comboSimulation.getSelectedItem() == null){
			return null;
		}
		try {
			return SimulationComponents.getInstance().getSimulation(comboSimulation.getSelectedItem().toString());
		} catch (ParameterException e) {
			return null;
		}
	}

}
