package logic.generator;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.EntryField;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.sepia.petrinet.AbstractTransition;
import logic.simulation.ConfigurationException;
import logic.transformation.EntryTransformerManager;

/**
 * This class is used to generate Log entries from fired transitions.<br>
 * Additionally, it can be generated with a reference to an entry transformer manager,
 * whose transformers are applied on generated log traces.
 * 
 * @see EntryTransformerManager
 * 
 * @author Thomas Stocker
 */
public class LogEntryGenerator implements TraceCompletionListener{
	/**
	 * The transformer manager, that manages the set of entry transformers that
	 * are applied on generated log entries.
	 */
	protected EntryTransformerManager entryTransformerManager = null;
	
	/**
	 * Creates a new log entry generator.<br>
	 * In this case, no transformer manager is used and no transformers are applied
	 * on generated log entries.
	 */
	public LogEntryGenerator(){}
	
	/**
	 * Creates a new log entry generator with the given entry transformer manager,
	 * that contains all entry transformers that should be applied to generated log entries.
	 * This log entry generator may be incompatible to the given entry transformer manager,
	 * if it does dot provide enough information for transformers to be applied.
	 * 
	 * @param entryTransformerManager Entry transformer manager.
	 * @throws ParameterException 
	 * @throws Exception If this generator is incompatible to the transformer manager.
	 */
	public LogEntryGenerator(EntryTransformerManager entryTransformerManager) throws ParameterException{
		setEntryTransformerManager(entryTransformerManager);
	}
	
	public void checkValidity() throws ConfigurationException{}
	
	/**
	 * Sets the entry transformer manager of the log entry generator.<br>
	 * This log entry generator may be incompatible to the given entry transformer manager,
	 * if it does dot provide enough information for transformers to be applied.
	 * 
	 * @param entryTransformerManager
	 * @throws ParameterException 
	 * @throws IllegalArgumentException If the log entry generator is incompatible with the transformer manager.
	 */
	public void setEntryTransformerManager(EntryTransformerManager entryTransformerManager) throws ParameterException{
		Validate.notNull(entryTransformerManager);
		this.entryTransformerManager = entryTransformerManager;
		this.entryTransformerManager.setSource(this);
	}
	
	/**
	 * Generates a log entry containing information related to the fired transition and the case number.
	 * A case is defined as a process execution (path through the Petri net). 
	 * After preparing a context specific log entry with the help of {@link #prepareLogEntry(String)}, 
	 * this method applies all managed distortion transformers on the entry.
	 * 
	 * @param transition The fired transition.
	 * @param caseNumber The number of the corresponding case.
	 * @return A log entry related to the fired transition.
	 * @throws ParameterException 
	 * @throws Exception 
	 */
	public LogEntry getLogEntryFor(AbstractTransition<?,?> transition, int caseNumber) throws ParameterException {
		LogEntry entry = prepareLogEntry(transition, caseNumber);
		if(entryTransformerManager != null)
			entryTransformerManager.applyTransformers(entry, caseNumber);
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
	 * compatibility checks with entry transformers.
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
