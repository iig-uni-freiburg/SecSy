package logic.filtering.filter.trace.multiple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import log.EntryField;
import log.LockingException;
import log.LogEntry;
import log.LogTrace;
import log.ModificationException;
import logic.filtering.TraceFilterResult;
import logic.filtering.filter.FilterType;
import logic.filtering.filter.properties.AbstractFilterProperties;
import logic.filtering.filter.properties.UnauthorizedExecutionFilterProperties;
import logic.generator.Context;
import properties.PropertyException;
import validate.ParameterException;
import validate.Validate;

/**
 * Manipulates a given log trace by inserting an access control violation.
 * 
 * @author Thomas Stocker
 */
public class UnauthorizedExecutionFilter extends AbstractMultipleTraceFilter {
	
	private final String CUSTOM_SUCCESS_FORMAT = "entry %s: %s -> %s";
	private Context context = null;
	
	public UnauthorizedExecutionFilter(UnauthorizedExecutionFilterProperties properties) throws ParameterException, PropertyException{
		super(properties);
	}
	
	public UnauthorizedExecutionFilter(Context context, UnauthorizedExecutionFilterProperties properties) throws ParameterException, PropertyException {
		super(properties);
		setContext(context);
	}

	public UnauthorizedExecutionFilter(double activationProbability, Context context, int maxAppliance) throws ParameterException {
		this(activationProbability, maxAppliance);
		setContext(context);
	}
	
	public UnauthorizedExecutionFilter(double activationProbability, int maxAppliance) throws ParameterException {
		super(FilterType.UNAUTHORIZED_EXECUTION_FILTER, activationProbability, maxAppliance);
	}
	
	public void setContext(Context context) throws ParameterException{
		Validate.notNull(context);
		this.context = context;
	}
	
	public boolean isValid(){
		return context != null;
	}

	@Override
	protected boolean applyEntryTransformation(LogEntry entry, TraceFilterResult filterResult) throws ParameterException {
		super.applyEntryTransformation(entry, filterResult);
		
		if(!isValid()){
			addMessageToResult(getErrorMessage("Cannot apply filter in invalid state: No context reference."), filterResult);
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
				entry.lockField(EntryField.ORIGINATOR, "Filter-Enforcement: UnauthorizedExecution");
				return true;
			} else {
				//If the field ORIGINATOR is locked, there is no chance to set an unauthorized originator.
				if(entry.isFieldLocked(EntryField.ORIGINATOR))
					return false;
			}
			// Try to directly set the unauthorized originators as originator candidates of the log entry
			try {
				entry.setOriginatorCandidates(unauthorizedOriginators);
				entry.lockField(EntryField.ORIGINATOR_CANDIDATES, "Filter-Enforcement: UnauthorizedExecution");
				addMessageToResult(getSuccessMessage(entry.getActivity(), originatorCandidates.toString(), entry.getOriginatorCandidates().toString()), filterResult);
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
						entry.lockField(EntryField.ORIGINATOR, "Filter-Enforcement: UnauthorizedExecution");
						addMessageToResult(getSuccessMessage(entry.getActivity(), originatorCandidates.toString(), entry.getOriginatorCandidates().toString()), filterResult);
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
	protected void traceFeedback(LogTrace logTrace, LogEntry logEntry, boolean entryFilterSuccess) throws ParameterException {
		// TODO Auto-generated method stub
	}

	@Override
	public AbstractFilterProperties getProperties() throws ParameterException, PropertyException {
		UnauthorizedExecutionFilterProperties properties = new UnauthorizedExecutionFilterProperties();
		fillProperties(properties);
		return properties;
	}
	
	
}
