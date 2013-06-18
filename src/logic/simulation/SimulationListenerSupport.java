package logic.simulation;

import java.util.HashSet;
import java.util.Set;

import validate.ParameterException;
import validate.Validate;

public class SimulationListenerSupport {
	
	private Set<SimulationListener> listeners = new HashSet<SimulationListener>();
	
	public void addSimulationListener(SimulationListener listener) throws ParameterException{
		Validate.notNull(listener);
		this.listeners.add(listener);
	}
	
	public void removeSimulationListener(SimulationListener listener){
		this.listeners.remove(listener);
	}
	
	public void fireSimulationMessage(String message){
		for(SimulationListener listener: listeners){
			listener.simulationMessage(message);
		}
	}
	
	public void fireSimulationRunStarted(SimulationRun simulationRun){
		for(SimulationListener listener: listeners){
			listener.simulationRunStarted(simulationRun);
		}
	}

	public void fireSimulationRunCompleted(SimulationRun simulationRun){
		for(SimulationListener listener: listeners){
			listener.simulationRunCompleted(simulationRun);
		}
	}
	
}
