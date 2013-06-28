package gui;
import gui.dialog.ExecutionDialog;
import gui.dialog.SimulationDialog;
import gui.dialog.SimulationDirectoryDialog;
import gui.dialog.TimeFrameDialog;
import gui.properties.GeneralProperties;

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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;

import logic.simulation.Simulation;


public class Simulator extends JFrame {

	private static final long serialVersionUID = -8445477529677450528L;
	
	private final CloseAction closeAction = new CloseAction();
	
	private JButton btnRun;
	private JButton btnTimeFrame;
	private JButton btnNewSimulation;
	private JButton btnEditSimulation;
	private JComboBox comboSimulation;
	private JTextArea areaSimulation;
	
//	private FileWriter fileWriter = null;

	/**
	 * Create the application.
	 */
	public Simulator() {
		setResizable(false);
		//Check if there is a path to a simulation directory.
		if(!checkSimulationDirectory()){
			//There is no path and it is either not possible to set a path or the user aborted the corresponding dialog.
			System.exit(0);
		}
		//Trigger the loading of simulation components
		SimulationComponents.getInstance();
		
		setUpGUI();
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
		String simulationDirectory = SimulationDirectoryDialog.showDialog(Simulator.this);
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
		
		setBounds(100, 100, 540, 545);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setJMenuBar(getMenu());
		
		getContentPane().add(getButtonNewSimulation());
		
		getContentPane().add(getComboSimulation());
		
		JLabel lblNewLabel = new JLabel("Simulation:");
		lblNewLabel.setBounds(20, 20, 76, 16);
		getContentPane().add(lblNewLabel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(20, 55, 500, 400);
		getContentPane().add(scrollPane);
		
		areaSimulation = new JTextArea();
		areaSimulation.setFont(new Font("Monospaced", Font.PLAIN, 12));
		scrollPane.setViewportView(areaSimulation);
		updateSimulationArea();
		
		getContentPane().add(getButtonRun());
		
		getContentPane().add(getButtonTimeFrame());

		getContentPane().add(getButtonEditSimulation());
		
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
		mntmSwitch.setAction(new AbstractAction("Switch Simulation Directory") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String simulationDirectory = SimulationDirectoryDialog.showDialog(Simulator.this);
				if(simulationDirectory == null){
					return;
				}
				try {
					if(GeneralProperties.getInstance().getSimulationDirectory().equals(simulationDirectory)){
						return;
					}
				} catch (Exception e1) {
					return;
				}
				
				try {
					GeneralProperties.getInstance().setSimulationDirectory(simulationDirectory);
				} catch (ParameterException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (PropertyException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
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
					} catch (ParameterException e2) {
						JOptionPane.showMessageDialog(Simulator.this, "Cannot launch simulation dialog.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
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
			btnEditSimulation.setBounds(452, 16, 68, 29);
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
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(null, "Internal exception: Cannot launch simulation dialog.\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
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
			btnNewSimulation.setBounds(382, 16, 70, 29);
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
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				}
			});
			btnRun.setBounds(405, 460, 115, 29);
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
			btnTimeFrame.setBounds(290, 460, 115, 29);
			btnTimeFrame.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Simulation simulation = getSimulation();
					if(simulation == null)
						return;
					new TimeFrameDialog(Simulator.this, simulation);
				}
			});
		}
		return btnTimeFrame;
	}
	
	private JComboBox getComboSimulation(){
		if(comboSimulation == null){
			comboSimulation = new JComboBox();
			comboSimulation.setBounds(100, 16, 279, 27);
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
	
	public static void main(String[] args) {
		new Simulator();
	}
	
	
	
	
//	private class SimulationThread implements Runnable,TraceStartListener {
//		
//		private Simulation simulation = null;
//
//		public SimulationThread(Simulation simulation) {
//			this.simulation = simulation;
//		}
//
//		public void run() {
////			try {
////				simulation.getLogGenerator().registerTraceStartListener(this);
////			} catch (ParameterException e2) {
////				JOptionPane.showMessageDialog(Simulator.this, "Cannot register simulator as trace start listener at log generator.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
////				return;
////			}
//			try {
//				if(fileWriter != null){
//					fileWriter.closeFile();
//				}
//				fileWriter = new FileWriter(simulation.getLogGenerator().getLogPath(), simulation.getLogGenerator().getFileNameShort());
//				fileWriter.setFileExtension("log");
//			} catch (ParameterException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			simulation.addSimulationListener(Simulator.this);
//			
//			try {
//				MessageDialog.getInstance().addMessage("Starting simulation: " + simulation.getName() + "...");
//				long startTime = System.currentTimeMillis();
//				simulation.executeSimulation();
//				long endTime = System.currentTimeMillis();
//				MessageDialog.getInstance().addMessage("                     Completed.");
//				MessageDialog.getInstance().addMessage("                     Simulation time: " + (endTime - startTime) / 1000.0 + " s");
//				MessageDialog.getInstance().addMessage("                     Log file size: " + simulation.getLogGenerator().getLogFileSize());
//			} catch (ConfigurationException e1) {
//				JOptionPane.showMessageDialog(Simulator.this,"Simulation components are not connected properly.\nReason: " + e1.getMessage(), "Configuration Exception",JOptionPane.ERROR_MESSAGE);
//				return;
//			} catch (SimulationException e1) {
//				JOptionPane.showMessageDialog(Simulator.this,"Exception during process simulation.\nReason: " + e1.getMessage(), "Simulation Exception",JOptionPane.ERROR_MESSAGE);
//				return;
//			} catch (IOException e1) {
//				JOptionPane.showMessageDialog(Simulator.this,"I/O Exception during process simulation.\nReason: " + e1.getMessage(), "Simulation Exception", JOptionPane.ERROR_MESSAGE);
//				return;
//			}
//			
//			try {
//				fileWriter.closeFile();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//		@Override
//		public void traceStarted(int caseNumber) throws ParameterException {
//			if (caseNumber == 1) {
//				MessageDialog.getInstance().addMessage("                     Starting trace " + caseNumber);
//			} else {
//				MessageDialog.getInstance().addMessageOverride("                     Starting trace " + caseNumber);
//			}
//		}
//	}

//	@Override
//	public void simulationMessage(String message) {
//		try {
//			fileWriter.writeLine(message);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
