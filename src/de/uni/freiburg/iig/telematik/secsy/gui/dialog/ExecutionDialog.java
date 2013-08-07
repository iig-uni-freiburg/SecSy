package de.uni.freiburg.iig.telematik.secsy.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import de.invation.code.toval.file.FileWriter;
import de.invation.code.toval.time.TimeValue;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.gui.ExecutionTask;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.Simulation;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.SimulationListener;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.SimulationRun;


public class ExecutionDialog extends JDialog implements SimulationListener {

	private static final long serialVersionUID = 4501959307493776929L;
	
	private JTextArea area = null;
	private FileWriter fileWriter = null;
	private JProgressBar progressBar = null;
	
	private ExecutionTask task = null;
	
	private Simulation simulation = null;

	public ExecutionDialog(Window parent, Simulation simulation) throws ParameterException{
		super(parent);
		setResizable(false);
		setTitle("Execution Progress");
		setBounds(100, 100, 400, 431);
		setModal(true);
		setLocationRelativeTo(parent);
		
		Validate.notNull(simulation);
		this.simulation = simulation;
		//Add this dialog as simulation listener to get simulation messages
		try {
			simulation.addSimulationListener(this);
		} catch (ParameterException e) {}
		
		getContentPane().setLayout(new BorderLayout());
		
		getContentPane().add(getMainPanel(), BorderLayout.CENTER);
		
		getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);
		
		setVisible(true);
	}
	
	private JPanel getMainPanel(){
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainPanel.setLayout(new BorderLayout(0, 0));
		
		area = new JTextArea();
		area.setEditable(false);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(area);
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		
		progressBar = new JProgressBar(0, 100);
		mainPanel.add(progressBar, BorderLayout.SOUTH);
		
		return mainPanel;
	}
	
	private JPanel getButtonPanel(){
		JPanel buttonPane = new JPanel();
		buttonPane.setPreferredSize(new Dimension(450, 40));
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		
		Component horizontalGlue = Box.createHorizontalGlue();
		buttonPane.add(horizontalGlue);
		
		JButton okButton = new JButton("Close");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		JButton btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startSimulation();
			}
		});
		buttonPane.add(btnRun);
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		
		Component horizontalGlue2 = Box.createHorizontalGlue();
		buttonPane.add(horizontalGlue2);
		
		return buttonPane;
	}
	
	public void startSimulation(){
		
		//Prepare file writer
		try {
			if(fileWriter != null){
				fileWriter.closeFile();
			}
			fileWriter = new FileWriter(simulation.getLogGenerator().getLogPath(), simulation.getLogGenerator().getFileNameShort());
			fileWriter.setFileExtension("log");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(ExecutionDialog.this,"Cannot set up log writer.\nReason: " + e.getMessage(), "Configuration Exception",JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//Prepare the simulation task
		task = new ExecutionTask(simulation, this);
		task.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("progress".equals(evt.getPropertyName())) {
	                 progressBar.setValue((Integer) evt.getNewValue());
	             }
			}
		});
		
		addMessage("Starting simulation: " + simulation.getName() + "...");
		
		//Start the task
		task.execute();

	}
	
	public JTextArea getTextArea(){
		return area;
	}
	
	public void taskCancelled(Exception exception){
		addMessage("Simulation Cancelled:");
		addMessage(exception.getMessage());
		JOptionPane.showMessageDialog(ExecutionDialog.this,"Simulation procedure was cancelled.\nReason: " + exception.getMessage(), "Simulation Exception",JOptionPane.ERROR_MESSAGE);
		exception.printStackTrace();
		return;
	}
	
	public void taskCompleted(){
		addMessage("Simulation Completed.");
		TimeValue simulationTime = task.getExecutionTime();
		simulationTime.adjustScale();
		addMessage("Simulation time: " + simulationTime);
		
		//Complete and close log file.
		try {
			fileWriter.closeFile();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(ExecutionDialog.this,"Cannot complete synthesized log file.\nReason: " + e.getMessage(), "I/O Exception",JOptionPane.ERROR_MESSAGE);
			return;
		}
		addMessage("Log file size: " + simulation.getLogGenerator().getLogFileSize());
	}
	
	
	private void addMessage(String message){
		area.append(message + "\n");
	}

	@Override
	public void simulationMessage(String message) {
		try {
			fileWriter.writeLine(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void simulationRunStarted(SimulationRun simulationRun) {
		addMessage("Simulation run started: \""+simulationRun.getName()+"\"");
	}

	@Override
	public void simulationRunCompleted(SimulationRun simulationRun) {
		addMessage("Simulation run completed: \""+simulationRun.getName()+"\"");
	}
	
	public static void showDialog(Window parent, Simulation simulation) throws ParameterException{
		ExecutionDialog dialog = new ExecutionDialog(parent, simulation);
		dialog.startSimulation();
	}

	public static void main(String[] args) throws ParameterException {
		new ExecutionDialog(null, null);
	}

}
