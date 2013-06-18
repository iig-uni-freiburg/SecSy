package logic.simulation;


public interface SimulationListener {
	
public void simulationMessage(String message);
	
	public void simulationRunStarted(SimulationRun simulationRun);
	
	public void simulationRunCompleted(SimulationRun simulationRun);

}
