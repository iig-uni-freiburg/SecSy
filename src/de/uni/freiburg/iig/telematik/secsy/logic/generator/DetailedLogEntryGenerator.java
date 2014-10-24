package de.uni.freiburg.iig.telematik.secsy.logic.generator;

import de.invation.code.toval.misc.valuegeneration.ValueGenerationException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.DataAttribute;
import de.uni.freiburg.iig.telematik.jawl.log.EntryField;
import de.uni.freiburg.iig.telematik.jawl.log.EventType;
import de.uni.freiburg.iig.telematik.jawl.log.LockingException;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.log.SimulationLogEntry;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.ConfigurationException;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.ConfigurationException.ErrorCode;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.EntryTransformerManager;
import de.uni.freiburg.iig.telematik.sepia.petrinet.AbstractTransition;

/**
 * This class is used to generate Log entries from fired transitions.<br>
 * It inherits from {@link LogEntryGenerator} to include more information
 * in generated log entries. This additional information has to be provided
 * by a log context ({@link SynthesisContext}) and a case data container ({@link CaseDataContainer}).<br>
 * <br>
 * <ul>
 * <li>Context: Authorized subjects -> Entry originator candidates</li>
 * <li>CaseDataContainer: Data attributes used on activity execution -> Entry input and output data</li>
 * </ul>
 * 
 * @see SynthesisContext
 * @see CaseDataContainer
 * @author Thomas Stocker
 */
public class DetailedLogEntryGenerator extends LogEntryGenerator {
	/**
	 * The case data container that holds required information about the data usage of log activities.
	 */
	protected CaseDataContainer caseDataContainer = null;
	/**
	 * The log context that holds required information about authorized subjects for activity execution.
	 */
	protected SynthesisContext context = null;

	/**
	 * Creates a new detailed log entry generator using the given context and case data generator.<br>
	 * The log context holds required information about authorized subjects for activity execution
	 * and the case data container holds required information about the data usage of log activities.<br>
	 * In this case, no transformer manager is used and no transformers are applied
	 * on generated log entries.<br>
	 * The given log context has to be equal to the context of the case data container,
	 * otherwise an exception is raised.
	 * 
	 * @param context Log context.
	 * @param caseDataContainer Case data container.
	 * @throws ConfigurationException 
	 * @throws ParameterException 
	 * @see SynthesisContext
	 * @see CaseDataContainer
	 */
	public DetailedLogEntryGenerator(SynthesisContext context, CaseDataContainer caseDataContainer) 
			throws ConfigurationException{
		super();
		Validate.notNull(context);
		if(!context.isValid())
			throw new ParameterException(ParameterException.ErrorCode.INCOMPATIBILITY, "Context is not valid");
		Validate.notNull(caseDataContainer);
//		if(context != caseDataContainer.getContext())
//			throw new ConfigurationException(ErrorCode.CONTEXT_INCONSISTENCY);
		//Context is now adjusted right before the simulation starts
		this.context = context;
		this.caseDataContainer = caseDataContainer;
	}
	
	/**
	 * Creates a new detailed log entry generator using the given context, case data generator
	 * and entry transformer manager.<br>
	 * The log context holds required information about authorized subjects for activity execution,
	 * the case data container holds required information about the data usage of log activities
	 * and the log entry generator contains all entry transformers that should be applied to generated log entries.
	 * This log entry generator may be incompatible to the given entry transformer manager,
	 * if it does dot provide enough information for transformers to be applied.
	 * 
	 * @param context Log context.
	 * @param caseDataContainer Case data container.
	 * @param entryTransformerManager Entry transformer manager.
	 * @throws ParameterException 
	 * @throws ConfigurationException 
	 * @throws Exception If this generator is incompatible to the transformer manager.
	 */
	public DetailedLogEntryGenerator(SynthesisContext context, CaseDataContainer caseDataContainer, EntryTransformerManager entryTransformerManager) 
			throws ConfigurationException {
		super(entryTransformerManager);
		Validate.notNull(context);
		if(!context.isValid())
			throw new ParameterException(ParameterException.ErrorCode.INCOMPATIBILITY, "Context is not valid");
		Validate.notNull(caseDataContainer);
		if(!context.equals(caseDataContainer.getContext()))
			throw new ConfigurationException(ErrorCode.CONTEXT_INCONSISTENCY);
		this.context = context;
		this.caseDataContainer = caseDataContainer;
	}


	@Override
	public void checkValidity() throws ConfigurationException{
		caseDataContainer.checkValidity();
	}

	/**
	 * Overrides the superclass method to include more information in generated log entries.<br>
	 * Sets the originator candidates of generated entries according to the authorized subjects
	 * within the log context and the input and output attributes of log entries according
	 * to the case data container.
	 * @throws ParameterException 
	 * @throws ValueGenerationException 
	 * @throws NullPointerException 
	 * @throws Exception If the value generator fails to generate new values.
	 * 
	 * @see LogEntry
	 * @see LogEntryGenerator#prepareLogEntry(AbstractTransition, int)
	 * @see CaseDataContainer
	 * @see SynthesisContext
	 */
	@Override
	protected SimulationLogEntry prepareLogEntry(AbstractTransition<?,?> transition, int caseNumber){
		Validate.notNull(transition);
		Validate.bigger(caseNumber, 0);
		SimulationLogEntry result = super.prepareLogEntry(transition, caseNumber);
		try {
			result.setOriginatorCandidates(context.getACModel().getAuthorizedSubjectsForTransaction(transition.getLabel()));
			for(DataAttribute attribute: caseDataContainer.getAttributesForActivity(transition.getLabel(), caseNumber)){
				result.setDataUsageFor(attribute, context.getDataUsageFor(transition.getLabel(), attribute.name));
			}
		} catch (LockingException e) {
			// Cannot happen, since the entry was just created and no fields were locked so far.
			e.printStackTrace();
		} catch (ValueGenerationException e) {
			// Cannot happen, since the generator only accepts valid value generators.
			// In valid state, valid generators do not throw ValueGenerationException.
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Implemented interface method for trace completion notification.
	 * Delegates notification to the case data container.
	 */
	@Override
	public void traceCompleted(int caseNumber) {
		caseDataContainer.traceCompleted(caseNumber);
	}
	
	public SynthesisContext getContext(){
		return context;
	}
	
	public CaseDataContainer getCaseDataContainer(){
		return caseDataContainer;
	}

	/**
	 * Checks if this entry generator provides a specific type of log information,<br>
	 * i.e. if it generates log entries containing this information.<br>
	 * In sum, generated log entries contain information about:<br>
	 * <ul>
	 * <li>Activity: Executed process activity.</li>
	 * <li>Originator: Actor responsible for the execution of the activity.</li>
	 * <li>Event type: Event type of the activity (e.g. completed, postponed, ...).</li>
	 * <li>Input data: Input data used by the activity.</li>
	 * <li>Output data: Generated output data of the activity.</li>
	 * </ul>
	 * Note: Although this entry generator does not add timing information,
	 * it returns true for the entry field TIME. Time information is assumed to be added
	 * by the corresponding log generator (e.g. TraceLogGenerator) to every generated log entry.
	 * However, it is important to return <code>true</code> to ensure proper
	 * compatibility checks with entry transformers.
	 * 
	 * @param field The type of log information requested.
	 * @return <code>true</code> if this entry generator provides the information;<br>
	 * <code>false</code> otherwise.
	 * @see EventType
	 */
	@Override
	public boolean providesLogInformation(EntryField contextType) {
		switch(contextType){
		case TIME: return true;
		case ACTIVITY: return true;
		case ORIGINATOR: return true;
		case EVENTTYPE: return false;
		case DATA: return true;
		default: return false;
		}
	}

}
