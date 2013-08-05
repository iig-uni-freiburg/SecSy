package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.multiple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.EntryField;
import de.uni.freiburg.iig.telematik.jawl.log.LockingException;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.jawl.log.ModificationException;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.Context;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.TransformerType;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.UnauthorizedExecutionTransformerProperties;


/**
 * Manipulates a given log trace by inserting an access control violation.
 * 
 * @author Thomas Stocker
 */
public class UnauthorizedExecutionTransformer extends AbstractMultipleTraceTransformer {
	
	private final String CUSTOM_SUCCESS_FORMAT = "entry %s: %s -> %s";
	private Context context = null;
	
	public UnauthorizedExecutionTransformer(UnauthorizedExecutionTransformerProperties properties) throws ParameterException, PropertyException{
		super(properties);
	}
	
	public UnauthorizedExecutionTransformer(Context context, UnauthorizedExecutionTransformerProperties properties) throws ParameterException, PropertyException {
		super(properties);
		setContext(context);
	}

	public UnauthorizedExecutionTransformer(double activationProbability, Context context, int maxAppliance) throws ParameterException {
		this(activationProbability, maxAppliance);
		setContext(context);
	}
	
	public UnauthorizedExecutionTransformer(double activationProbability, int maxAppliance) throws ParameterException {
		super(TransformerType.UNAUTHORIZED_EXECUTION, activationProbability, maxAppliance);
	}
	
	public void setContext(Context context) throws ParameterException{
		Validate.notNull(context);
		this.context = context;
	}
	
	public boolean isValid(){
		return context != null;
	}
	

	@Override
	protected boolean applyEntryTransformation(LogTrace trace, LogEntry entry, TraceTransformerResult transformerResult) throws ParameterException {
		super.applyEntryTransformation(trace, entry, transformerResult);
		
		if(!isValid()){
			addMessageToResult(getErrorMessage("Cannot apply transformer in invalid state: No context reference."), transformerResult);
			return false;
		}
		
		List<String> unauthorizedOriginators = new ArrayList<String>(context.getSubjects());
		List<String> authorizedOriginators;
		try {
			authorizedOriginators = context.getACModel().getAuthorizedSubjectsForTransaction(entry.getActivity());
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
					} catch (ModificationException e1) {
						// Cannot happen since the argument is chosen out of the candidate set of the log entry
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
	public List<EntryField> requiredContextInformation() {
		return Arrays.asList(EntryField.ACTIVITY, EntryField.ORIGINATOR);
	}

	@Override
	public AbstractTransformerProperties getProperties() throws ParameterException, PropertyException {
		UnauthorizedExecutionTransformerProperties properties = new UnauthorizedExecutionTransformerProperties();
		fillProperties(properties);
		return properties;
	}
	
	
}
