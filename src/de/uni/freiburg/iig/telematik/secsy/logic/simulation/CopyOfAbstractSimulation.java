package de.uni.freiburg.iig.telematik.secsy.logic.simulation;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;

import de.uni.freiburg.iig.telematik.secsy.logic.generator.LogEntryGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.LogGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.CaseTimeGenerator;


public abstract class CopyOfAbstractSimulation {

	protected Calendar calendar = new GregorianCalendar(Calendar.getInstance().getTimeZone());
	protected CaseTimeGenerator timeGenerator = null;
	protected LogEntryGenerator entryGenerator = null;
	protected LogGenerator logGenerator = null;
	
	
	public CopyOfAbstractSimulation() throws Exception{
		getLogGenerator().setCaseTimeGenerator(getCaseTimeGenerator());
		getLogGenerator().setLogEntryGenerator(getLogEntryGenerator());
		getLogGenerator().addSimulationRuns(getSimulationRuns());
	}
	
	public void executeSimulation() throws Exception{
		long startTime = System.currentTimeMillis();
		logGenerator.generateLog();
		long endTime = System.currentTimeMillis();
		System.out.println("Simulation time: " + (endTime - startTime) / 1000.0 + " s");
	}
	
	protected LogGenerator getLogGenerator() throws Exception{
		if(logGenerator == null){
			logGenerator = createLogGenerator();
		}
		return logGenerator;
	}
	protected abstract LogGenerator createLogGenerator() throws Exception;
	
	protected LogEntryGenerator getLogEntryGenerator() throws Exception{
		if(entryGenerator == null){
			entryGenerator = createLogEntryGenerator();
		}
		return entryGenerator;
	}
	protected abstract LogEntryGenerator createLogEntryGenerator() throws Exception;
	
	protected CaseTimeGenerator getCaseTimeGenerator() throws Exception{
		if(timeGenerator == null){
			timeGenerator = createCaseTimeGenerator();
		}
		return timeGenerator;
	}
	protected abstract CaseTimeGenerator createCaseTimeGenerator();
	
	//Can be empty, but Exception if null
	protected abstract Collection<SimulationRun> getSimulationRuns() throws Exception;
	
	public boolean isValid(){
		if(timeGenerator == null)
			return false;
		if(entryGenerator == null)
			return false;
		if(logGenerator == null)
			return false;
		return true;
	}
	
}
