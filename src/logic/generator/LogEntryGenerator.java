package logic.generator;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.EntryField;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import logic.filtering.EntryFilterManager;
import logic.simulation.ConfigurationException;
import petrinet.AbstractTransition;

/**
 * This class is used to generate Log entries from fired transitions.<br>
 * Additionally, it can be generated with a reference to an entry filter manager,
 * whose filters are applied on generated log traces.
 * 
 * @see EntryFilterManager
 * 
 * @author Thomas Stocker
 */
public class LogEntryGenerator implements TraceCompletionListener{
	/**
	 * The filter manager, that manages the set of entry filters that
	 * are applied on generated log entries.
	 */
	protected EntryFilterManager entryFilterManager = null;
	
	/**
	 * Creates a new log entry generator.<br>
	 * In this case, no filter manager is used and no filters are applied
	 * on generated log entries.
	 */
	public LogEntryGenerator(){}
	
	/**
	 * Creates a new log entry generator with the given entry filter manager,
	 * that contains all entry filters that should be applied to generated log entries.
	 * This log entry generator may be incompatible to the given entry filter manager,
	 * if it does dot provide enough information for filters to be applied.
	 * 
	 * @param entryFilterManager Entry filter manager.
	 * @throws ParameterException 
	 * @throws Exception If this generator is incompatible to the filter manager.
	 */
	public LogEntryGenerator(EntryFilterManager entryFilterManager) throws ParameterException{
		setEntryfilterManager(entryFilterManager);
	}
	
	public void checkValidity() throws ConfigurationException{}
	
	/**
	 * Sets the entry filter manager of the log entry generator.<br>
	 * This log entry generator may be incompatible to the given entry filter manager,
	 * if it does dot provide enough information for filters to be applied.
	 * 
	 * @param entryFilterManager
	 * @throws ParameterException 
	 * @throws IllegalArgumentException If the log entry generator is incompatible with the filter manager.
	 */
	public void setEntryfilterManager(EntryFilterManager entryFilterManager) throws ParameterException{
		Validate.notNull(entryFilterManager);
		this.entryFilterManager = entryFilterManager;
		this.entryFilterManager.setSource(this);
	}
	
	/**
	 * Generates a log entry containing information related to the fired transition and the case number.
	 * A case is defined as a process execution (path through the Petri net). 
	 * After preparing a context specific log entry with the help of {@link #prepareLogEntry(String)}, 
	 * this method applies all managed distortion filters on the entry.
	 * 
	 * @param transition The fired transition.
	 * @param caseNumber The number of the corresponding case.
	 * @return A log entry related to the fired transition.
	 * @throws ParameterException 
	 * @throws Exception 
	 */
	public LogEntry getLogEntryFor(AbstractTransition<?,?> transition, int caseNumber) throws ParameterException {
		LogEntry entry = prepareLogEntry(transition, caseNumber);
		if(entryFilterManager != null)
			entryFilterManager.applyFilters(entry, caseNumber);
		return entry;
	}
	
	/**
	 * Prepares a context specific log entry based on the given activity name.<br>
	 * This method only uses the name of the fired transition.
	 * Subclasses can override it to add specific fields like originators
	 * or data in/outputs.
	 * @param activity Name of the executed activity.
	 * @return A context specific log entry.
	 * @throws ParameterException 
	 * @throws Exception 
	 */
	protected LogEntry prepareLogEntry(AbstractTransition<?,?> transition, int caseNumber) throws ParameterException {
		Validate.notNull(transition);
		Validate.bigger(caseNumber, 0);
		LogEntry entry = new LogEntry(transition.getLabel());
		return entry;
	}
	
	/**
	 * Checks if this entry generator provides a specific type of log information,<br>
	 * i.e. if it generates log entries containing this information.<br>
	 * This implementation only provides activity information.<br>
	 * Note: Although this entry generator does not add timing information,
	 * it returns true for the entry field TIME. Time information is assumed to be added
	 * by the corresponding log generator (e.g. TraceLogGenerator) to every generated log entry.
	 * However, it is important to return <code>true</code> to ensure proper
	 * compatibility checks with entry filters.
	 * 
	 * @param field The type of log information requested.
	 * @return <code>true</code> if this entry generator provides the information;<br>
	 * <code>false</code> otherwise.
	 * @throws ParameterException 
	 */
	public boolean providesLogInformation(EntryField field) throws ParameterException{
		Validate.notNull(field);
		switch(field){
		case ACTIVITY: 
			return true;
		case TIME:
			return true;
		default: 
			return false;
		}
	}

	/**
	 * Implemented interface method for trace completion notification.
	 * Standard implementation is: Do nothing.
	 */
	@Override
	public void traceCompleted(int caseNumber) {}

}
