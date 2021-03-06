package de.uni.freiburg.iig.telematik.secsy.gui;


import java.io.IOException;
import java.util.List;

import javax.swing.SwingWorker;

import de.invation.code.toval.time.TimeScale;
import de.invation.code.toval.time.TimeValue;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.ExecutionDialog;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.SimulationException;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.TraceStartListener;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.ConfigurationException;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.Simulation;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.SimulationListener;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.SimulationRun;


public class ExecutionTask extends SwingWorker<Boolean, String> implements TraceStartListener, SimulationListener {
	
	private Simulation simulation = null;
	private ExecutionDialog executionDialog = null;
	private int targetCases = 0;
	private TimeValue executionTime = null;
		
	public ExecutionTask(Simulation simulation, ExecutionDialog executionDialog) { 
		this.simulation = simulation;
		this.executionDialog = executionDialog;
	}

	@Override
	public Boolean doInBackground() throws SimulationException{
		
		long startTime = System.currentTimeMillis();
		
		try {
			simulation.addSimulationListener(ExecutionTask.this);
			simulation.getLogGenerator().registerTraceStartListener(ExecutionTask.this);
		} catch (ParameterException e2) {
			throw new SimulationException("Cannot register execution thread as log generator listener.\nReason: " + e2.getMessage());
		}
		
		try {
			simulation.executeSimulation();
		} catch (ConfigurationException e1) {
			throw new SimulationException("Simulation components are not connected properly.\nReason: " + e1.getMessage());
		} catch (SimulationException e1) {
			throw new SimulationException("Exception during process simulation.\nReason: " + e1.getMessage());
		} catch (IOException e1) {
			throw new SimulationException("I/O Exception during process simulation.\nReason: " + e1.getMessage());
		}
		
		long endTime = System.currentTimeMillis();
		try {
			executionTime = new TimeValue(endTime - startTime, TimeScale.MILLISECONDS);
		} catch (ParameterException e) {
			throw new SimulationException("Cannot set execution time.\nReason: " + e.getMessage());
		}
		
		return true;
	}
	
	@Override
	protected void done() {
		try {
            get();
            executionDialog.taskCompleted();
        } catch (Exception e) {
        	executionDialog.taskCancelled(e);
        }
	}

	public TimeValue getExecutionTime(){
		return executionTime;
	}
	
	@Override
	protected void process(List<String> chunks) {
		for (String message: chunks) {
			executionDialog.getTextArea().append(message + "\n");
         }
	}

	@Override
	public void traceStarted(int caseNumber) throws ParameterException {
		setProgress(100 * caseNumber / targetCases);
	}

	@Override
	public void simulationMessage(String message) {}

	@Override
	public void simulationRunStarted(SimulationRun simulationRun) {
		targetCases = simulationRun.getPasses();
	}

	@Override
	public void simulationRunCompleted(SimulationRun simulationRun) {}
}