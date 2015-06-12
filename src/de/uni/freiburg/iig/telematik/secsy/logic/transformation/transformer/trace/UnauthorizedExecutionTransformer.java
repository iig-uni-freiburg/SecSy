package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.invation.code.toval.properties.PropertyException;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.log.SimulationLogEntry;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.PropertyAwareTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.UnauthorizedExecutionTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.AbstractMultipleTraceTransformer;
import de.uni.freiburg.iig.telematik.sewol.log.EntryField;
import de.uni.freiburg.iig.telematik.sewol.log.LockingException;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;


/**
 * Manipulates a given log trace by inserting an access control violation.
 * 
 * @author Thomas Stocker
 */
public class UnauthorizedExecutionTransformer extends AbstractMultipleTraceTransformer implements PropertyAwareTransformer{
	
	private static final long serialVersionUID = 4507754400951475987L;

	private final String CUSTOM_SUCCESS_FORMAT = "entry %s: %s -> %s";
	
	public static final String hint = "<html><p>An unauthorized execution transformer changes single events of" +
									  "a process trace in a way, that the assigned originator of the" +
									  "corresponding process activity has no permission to execute" +
									  "the activity according to the contexts' access control model." +
									  "The number of appliances per trace is randomly chosen with" +
									  "an adjustable upper bound.</p>" +
									  "<p>The transformer fails, if all originators are authorized" +
									  "to execute all activities.</p></html>";
	
	public UnauthorizedExecutionTransformer(UnauthorizedExecutionTransformerProperties properties) throws PropertyException{
		super(properties);
	}
	
	public UnauthorizedExecutionTransformer(Double activationProbability, Integer maxAppliance){
		super(activationProbability, maxAppliance);
	}
	
	public UnauthorizedExecutionTransformer() {
		super();
	}
	
	public boolean isValid(){
		return getContext() != null;
	}

	/**
	 * This transformer does not accept any properties.<br>
	 */
	@Override
	public void setProperties(Object[] properties) throws Exception {}

	@Override
	protected boolean applyEntryTransformation(LogTrace<SimulationLogEntry> trace, SimulationLogEntry entry, TraceTransformerResult transformerResult){
		if(!isValid()){
			addMessageToResult(getErrorMessage("Cannot apply transformer in invalid state: No context reference."), transformerResult);
			return false;
		}
		
		List<String> unauthorizedOriginators = new ArrayList<String>(getContext().getSubjects());
		List<String> authorizedOriginators;
		try {
			authorizedOriginators = getContext().getACModel().getAuthorizedSubjectsForTransaction(entry.getActivity());
			unauthorizedOriginators.removeAll(authorizedOriginators);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		List<String> originatorCandidates = entry.getOriginatorCandidates();
		if(!unauthorizedOriginators.isEmpty()) {
			// Check if the actual originator of the log entry already is unauthorized
			if(unauthorizedOriginators.contains(entry.getOriginator())){
				// Lock the originator field and exit
				entry.lockField(EntryField.ORIGINATOR, "Transformer-Enforcement: UnauthorizedExecution");
				return true;
			} else {
				//If the field ORIGINATOR is locked, there is no chance to set an unauthorized originator.
				if(entry.isFieldLocked(EntryField.ORIGINATOR))
					return false;
			}
			// Try to directly set the unauthorized originators as originator candidates of the log entry
			try {
				entry.setOriginatorCandidates(unauthorizedOriginators);
				entry.lockField(EntryField.ORIGINATOR_CANDIDATES, "Transformer-Enforcement: UnauthorizedExecution");
				addMessageToResult(getSuccessMessage(entry.getActivity(), originatorCandidates.toString(), entry.getOriginatorCandidates().toString()), transformerResult);
				return true;
			} catch (NullPointerException e) {
				// Cannot happen since unauthorizedOriginators contains elements
			} catch (LockingException e) {
				// The field ORIGINATOR_CANDIDATES is locked which prevents directly setting originator candidates.
				// -> Try to find an unauthorized originators among the actual originator candidates of the log entry
				//    Note: Since the field ORIGINATOR is not locked (see above) this could be possible
				List<String> unauthorizeCandidates = new ArrayList<String>();
				for(String candidate: entry.getOriginatorCandidates())
					if(unauthorizedOriginators.contains(candidate))
						unauthorizeCandidates.add(candidate);
				if(!unauthorizeCandidates.isEmpty()){
					// There is at least one originator candidate within the log entry that is unauthorized
					// -> Randomly choose one of them as originator.
					try {
						entry.setOriginator(unauthorizeCandidates.get(rand.nextInt(unauthorizeCandidates.size())));
						entry.lockField(EntryField.ORIGINATOR, "Transformer-Enforcement: UnauthorizedExecution");
						addMessageToResult(getSuccessMessage(entry.getActivity(), originatorCandidates.toString(), entry.getOriginatorCandidates().toString()), transformerResult);
						return true;
					} catch (NullPointerException e1) {
						// Cannot happen since unauthorizedCandidates contains elements
						e1.printStackTrace();
					} catch (LockingException e1) {
						// Cannot happen since the field ORIGINATOR is not locked
						e1.printStackTrace();
					}
				}
				return false;
			}
		}
		return false;
	}
	
	private String getSuccessMessage(String activity, String lastCandidates, String newCandidates){
		return getNoticeMessage(String.format(CUSTOM_SUCCESS_FORMAT, activity, lastCandidates, newCandidates));
	}
	
	@Override
	public List<EntryField> requiredEntryFields() {
		return Arrays.asList(EntryField.ACTIVITY, EntryField.ORIGINATOR);
	}

	@Override
	public AbstractTransformerProperties getProperties() throws PropertyException {
		UnauthorizedExecutionTransformerProperties properties = new UnauthorizedExecutionTransformerProperties();
		fillProperties(properties);
		return properties;
	}

	@Override
	public String getHint() {
		return hint;
	}

	@Override
	public boolean requiresTimeGenerator() {
		return false;
	}

	@Override
	public boolean requiresContext() {
		return true;
	}
	
}
