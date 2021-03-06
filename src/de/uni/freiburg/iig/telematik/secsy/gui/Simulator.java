package de.uni.freiburg.iig.telematik.secsy.gui;

import de.invation.code.toval.graphic.dialog.ExceptionDialog;
import de.invation.code.toval.graphic.dialog.MessageDialog;
import de.invation.code.toval.validate.CompatibilityException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
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

import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.ExecutionDialog;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.SimulationDialog;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.SimulationDirectoryDialog;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.TimeFrameDialog;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.action.CloseAction;
import de.uni.freiburg.iig.telematik.secsy.gui.properties.SecSyProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.ConfigurationException;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.Simulation;
import de.uni.freiburg.iig.telematik.sepia.graphic.AbstractGraphicalPN;
import de.uni.freiburg.iig.telematik.sepia.graphic.GraphicalPTNet;
import de.uni.freiburg.iig.telematik.sepia.parser.graphic.PNParserDialog;
import de.uni.freiburg.iig.telematik.sepia.petrinet.abstr.AbstractPetriNet;
import de.uni.freiburg.iig.telematik.sepia.petrinet.pt.PTNet;
import de.uni.freiburg.iig.telematik.sewol.writer.PerspectiveException;

public class Simulator extends JFrame {

    private static final long serialVersionUID = -8445477529677450528L;

    public static final Dimension PREFERRED_SIZE = new Dimension(500, 500);

    private final CloseAction closeAction = new CloseAction();

    private JButton btnRun;
    private JButton btnTimeFrame;
    private JButton btnNewSimulation;
    private JButton btnEditSimulation;
    private JComboBox comboSimulation;
    private JTextArea areaSimulation;
    private final JPanel contentPanel = new JPanel(new BorderLayout());

    public Simulator() {

        setPreferredSize(PREFERRED_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setUpGUI();
        Dimension screenSize = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
        int wdwLeft = (int) ((screenSize.width / 2.0) - ((PREFERRED_SIZE.width + MessageDialog.PREFERRED_SIZE.width + 10) / 2.0));
        int wdwTop = screenSize.height / 2 - PREFERRED_SIZE.height / 2;
        pack();
        setLocation(wdwLeft, wdwTop);
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
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    SimulationComponents.getInstance().writeFilesToDisk();
                } catch (Exception e1) {
                    ExceptionDialog.showException(Simulator.this, "Internal Exception", new Exception("Exception while writing simulation components to disk.", e1), true, true);
                    return;
                }
                try {
                    SecSyProperties.getInstance().store();
                } catch (IOException e1) {
                    ExceptionDialog.showException(Simulator.this, "Internal Exception", new Exception("Cannot store simulation properties to disk.", e1), true, true);
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }
        });

        setVisible(true);
    }

    private JMenuBar getMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);

        JMenuItem mntmSwitch = new JMenuItem("Switch Simulation Directory");

        mntmSwitch.addActionListener((ActionEvent e) -> {
		String simulationDirectory = null;
		try {
			simulationDirectory = SimulationDirectoryDialog.showDialog(Simulator.this);
		} catch (Exception e2) {
			ExceptionDialog.showException(Simulator.this, "Internal Exception", new Exception("Cannot launch simulation directory dialog.", e2), true, true);
		}
		if (simulationDirectory == null) {
			return;
		}
		try {
			if (!SecSyProperties.getInstance().getWorkingDirectory().equals(simulationDirectory)) {
				SecSyProperties.getInstance().setWorkingDirectory(simulationDirectory, true);
			}
		} catch (Exception e1) {
			ExceptionDialog.showException(Simulator.this, "Internal Exception", new Exception("Exception while switching working directory.", e1), true, true);
			return;
		}
		
		updateSimulationBox();
		updateSimulationArea();
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
        mntmPetriNetpnml.addActionListener((ActionEvent e) -> {
		AbstractGraphicalPN importedNet = PNParserDialog.showPetriNetDialog(Simulator.this);
		if (importedNet != null) {
			
			AbstractPetriNet loadedNet = importedNet.getPetriNet();
			if (!(loadedNet instanceof PTNet)) {
				ExceptionDialog.showException(Simulator.this, "Unexpected Petri net type", new Exception("Loaded Petri net is not a P/T-Net."), true, true);
				return;
			}
			PTNet petriNet = (PTNet) loadedNet;
			
			String netName = petriNet.getName();
			try {
				while (netName == null || SimulationComponents.getInstance().getContainerPTNets().containsComponent(netName)) {
					netName = JOptionPane.showInputDialog(Simulator.this, "Name for the Petri net:", "");
				}
			} catch (Exception e1) {
				ExceptionDialog.showException(Simulator.this, "Internal Exception", new Exception("Exception while checking if net name is already in use.", e1), true, true);
				return;
			}
			try {
				if (!petriNet.getName().equals(netName)) {
					petriNet.setName(netName);
				}
			} catch (ParameterException e2) {
				ExceptionDialog.showException(Simulator.this, "Internal Exception", new Exception("Cannot change Petri net name to\"" + netName + "\"", e2), true, true);
				return;
			}
			
			try {
				SimulationComponents.getInstance().getContainerPTNets().addComponent(new GraphicalPTNet(petriNet, null), true);
			} catch (Exception e1) {
				ExceptionDialog.showException(Simulator.this, "Internal Exception", new Exception("Cannot add imported net to simulation components.", e1), true, true);
			}
		} else {
			// User aborted the dialog.
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

    private JButton getButtonEditSimulation() {
        if (btnEditSimulation == null) {
            btnEditSimulation = new JButton("Edit");
            btnEditSimulation.addActionListener(new ActionListener() {
		@Override
                public void actionPerformed(ActionEvent e) {
                    if (comboSimulation.getSelectedItem() == null) {
                        ExceptionDialog.showException(Simulator.this, "Internal Exception", new Exception("No simulation chosen."), true, true);
                        return;
                    }
                    String chosenSimulation = comboSimulation.getSelectedItem().toString();

                    Simulation simulation;
                    try {
                        simulation = SimulationComponents.getInstance().getContainerSimulations().getComponent(chosenSimulation);
                    } catch (Exception e2) {
                        // Cannot happen, since "chosenSimulation" is not null.
                        ExceptionDialog.showException(Simulator.this, "Internal Exception", new Exception("Cannot extract simulation: " + chosenSimulation, e2), true, true);
                        return;
                    }
                    if (simulation == null) {
                        ExceptionDialog.showException(Simulator.this, "Internal Exception", new Exception("Cannot extract simulation: " + chosenSimulation), true, true);
                        return;
                    }

                    String oldSimulationName = simulation.getName();
                    Simulation editedSimulation;
                    try {
                        editedSimulation = SimulationDialog.showDialog(Simulator.this, simulation);
                    } catch (Exception e2) {
                        ExceptionDialog.showException(Simulator.this, "Internal Exception", new Exception("Cannot launch simulation dialog", e2), true, true);
                        return;
                    }
                    if (editedSimulation != null) {
                        if (!editedSimulation.getName().equals(oldSimulationName)) {
                            try {
                                SimulationComponents.getInstance().getContainerSimulations().renameComponent(oldSimulationName, editedSimulation.getName());
                            } catch (Exception e1) {
                                ExceptionDialog.showException(Simulator.this, "Internal Exception", new Exception("Cannot change name of simulation", e1), true, true);
                                return;
                            }
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

    private JButton getButtonNewSimulation() {
        if (btnNewSimulation == null) {
            btnNewSimulation = new JButton("New");
            btnNewSimulation.addActionListener(new ActionListener() {
		@Override
                public void actionPerformed(ActionEvent e) {

                    Simulation newSimulation;
                    try {
                        newSimulation = SimulationDialog.showDialog(Simulator.this);
                    } catch (Exception e1) {
                        ExceptionDialog.showException(Simulator.this, "Internal Exception", new Exception("Cannot launch simulation dialog.", e1), true, true);
                        return;
                    }
                    if (newSimulation != null) {
                        try {
                            SimulationComponents.getInstance().getContainerSimulations().addComponent(newSimulation, true);
                        } catch (Exception e1) {
                            ExceptionDialog.showException(Simulator.this, "Internal Exception", new Exception("Cannot add simulation to simulation components.", e1), true, true);
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

    private JButton getButtonRun() {
        if (btnRun == null) {
            btnRun = new JButton("Run");
            btnRun.addActionListener(new ActionListener() {
		@Override
                public void actionPerformed(ActionEvent e) {
                    Simulation simulation = getSelectedSimulation();
                    if (simulation != null) {
                        if (!simulation.containsSimulationRuns()) {
                            ExceptionDialog.showException(Simulator.this, "Empty Simulation", new Exception("Simulation does not contain any simulation runs."), true, true);
                            return;
                        }
                        String logPath = getLogPath();
                        if (logPath != null) {
                            try {
                                simulation.getLogGenerator().setLogPath(logPath);
                            } catch (CompatibilityException | PerspectiveException | IOException e1) {
                                ExceptionDialog.showException(Simulator.this, "Configuration Exception", new Exception("Cannot set log path.", e1), true, true);
                                return;
                            }
                            try {
                                new ExecutionDialog(Simulator.this, simulation);
                            } catch (ParameterException e1) {
                                ExceptionDialog.showException(Simulator.this, "Internal Exception", new Exception("Cannot launch execution dialog.", e1), true, true);
                            }
                        }
                    }
                }
            });
        }
        return btnRun;
    }

    private String getLogPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose output directory for synthesized log.");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fileChooser.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            return file.getAbsolutePath() + "/";
        } else {
            return null;
        }
    }

    private JButton getButtonTimeFrame() {
        if (btnTimeFrame == null) {
            btnTimeFrame = new JButton("Time Frame");
            btnTimeFrame.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Simulation simulation = getSelectedSimulation();
                    if (simulation == null) {
                        return;
                    }
                    try {
                        new TimeFrameDialog(Simulator.this, simulation);
                    } catch (ConfigurationException e1) {
                        ExceptionDialog.showException(Simulator.this, "Internal Exception", new Exception("Cannot lainch time frame dialog.", e1), true, true);
                    }
                }
            });
        }
        return btnTimeFrame;
    }

    private JComboBox getComboSimulation() {
        if (comboSimulation == null) {
            comboSimulation = new JComboBox();
            comboSimulation.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        updateSimulationArea();
                    }
                }
            });
            updateSimulationBox();
        }
        return comboSimulation;
    }

    private void updateSimulationBox() {
        List<String> simulationNames = new ArrayList<>();
        try {
            for (Simulation simulation : SimulationComponents.getInstance().getContainerSimulations().getComponents()) {
                simulationNames.add(simulation.getName());
            }
        } catch (Exception e) {
            ExceptionDialog.showException(Simulator.this, "Internal Exception", new Exception("Cannot extract simulations from simulation components", e), true);
        }
        comboSimulation.setModel(new DefaultComboBoxModel(simulationNames.toArray()));
    }

    private void updateSimulationArea() {
        Simulation simulation = getSelectedSimulation();
        if (simulation == null) {
            areaSimulation.setText(null);
        } else {
            areaSimulation.setText(simulation.toString());
        }
    }

    private Simulation getSelectedSimulation() {
        if (comboSimulation.getSelectedItem() == null) {
            return null;
        }
        Simulation selectedSimulation = null;
        try{
            selectedSimulation = SimulationComponents.getInstance().getContainerSimulations().getComponent(comboSimulation.getSelectedItem().toString());
        } catch(Exception e){
            ExceptionDialog.showException(Simulator.this, "Internal Exception", new Exception("Cannot extract simulation from simulation components", e), true);
        }
        return selectedSimulation;
    }

}
