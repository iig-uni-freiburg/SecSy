package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.types.IndexCounter;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.EntryField;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntryUtils;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.log.SimulationLogEntry;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.AbstractTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AGPropertyEnforcementTransformerProperties;


@SuppressWarnings("serial")
public abstract class SoDBoDPropertyTransformer extends ActivityGroupPropertyEnforcementTransformer {
	
	protected final String NOTICEF_NO_ORIGINATOR = "trace does not contain originator information %s";
	protected final String ERRORF_FIXED_EQUAL_ORIGINATORS = "originators are equal and not alterable for group %s.";
	protected final String ERRORF_LOCKED_ORIGINATORS = "locked fields interfere with the property enforcement for group %s.";
	protected final String ERRORF_ORIGINATOR_COMBINATION = "no possible originator combination to enforce property for group %s.";
	protected final String CUSTOM_SINGLE_SUCCESSFUL_ENFORCEMENT_FORMAT = "Set originator to %s for entry %s";
	protected final String NONEEDF_TRIVIAL = "No enforcement necessary - trace only contains one activity of group %s";
	protected final String NOTICE_COMBINATIONS = "Trying originator combinations ";
	
	protected Map<String, List<SimulationLogEntry>> entriesForActivity;
	
	public SoDBoDPropertyTransformer(AGPropertyEnforcementTransformerProperties properties) throws ParameterException, PropertyException {
		super(properties);
	}
	
	public SoDBoDPropertyTransformer(Double violationProbability) throws ParameterException {
		super(violationProbability);
	}
	
	public SoDBoDPropertyTransformer() throws ParameterException {
		super();
	}

	@Override
	public List<EntryField> requiredEntryFields() {
		return Arrays.asList(EntryField.ACTIVITY, EntryField.ORIGINATOR);
	}

	/**
	 * Removes all entries from the list of affected entries that contain <code>null</null> values for the originator field.
	 * @throws ParameterException 
	 */
	@Override
	protected List<SimulationLogEntry> removeIrrelevantEntries(List<SimulationLogEntry> entries, TraceTransformerResult result) throws ParameterException {
		super.removeIrrelevantEntries(entries, result);
		List<SimulationLogEntry> relevantEntries = new ArrayList<SimulationLogEntry>(entries);
		for (SimulationLogEntry entry : relevantEntries)
			if (entry.getOriginatorCandidates() == null) {
				relevantEntries.remove(entry);
				try {
					addMessageToResult(getNoticeMessage(String.format(NOTICEF_NO_ORIGINATOR, entry.getActivity())), result);
				} catch (ParameterException e) {
					// Cannot happen, since String format is not null.
					e.printStackTrace();
				}
			}
		return relevantEntries;
	}
	
	/**
	 * Checks if the property is ensured/violated trivially by the original entries.<br>
	 * @param entries Entries before applying routines for property enforcement/violation.
	 * @param action Desired enforcement goal: enforcement or violation.
	 * @return <code>true</code> if property is ensured/violated trivially;<br>
	 * <code>false</code> otherwise.
	 * @throws ParameterException 
	 */
	protected boolean checkTrivialCase(List<SimulationLogEntry> entries, TransformerAction action) throws ParameterException{
		entriesForActivity = LogEntryUtils.clusterEntriesAccordingToActivities(entries);
		if(entriesForActivity.keySet().size()<2)
			return true;
		return transformerEnforcedOnOriginatorSets(LogEntryUtils.clusterOriginatorsAccordingToActivity(entries), action);
	}
	
	/**
	 * Tries to choose originators for entries in a way that the property is satisfied.<br>
	 * That means that the set of originators for any two different activities must not have common originators.
	 * @param originatorList The originators that can be chosen for the different entries
	 * @param indexes The current index combination for the originators
	 * @return A valid index combination in case a valid one could be found;<br> <code>null</code> otherwise.
	 * @throws ParameterException 
	 */
	protected Map<SimulationLogEntry, Integer> findValidIndexCombination(Map<SimulationLogEntry, List<String>> candidateList, TransformerAction transformerAction) throws ParameterException{
		Validate.notNull(candidateList);
		Validate.notNull(candidateList.keySet());
		Validate.noNullElements(candidateList.keySet());
		Validate.noNullElements(candidateList.values());
		Validate.notNull(transformerAction);
		IndexCounter<SimulationLogEntry> counter = new IndexCounter<SimulationLogEntry>();
		for(SimulationLogEntry entry: candidateList.keySet()){
			counter.addNewIndex(entry, candidateList.get(entry).size() - 1);
		}
		
		//Check if there is an originator configuration that fulfills the property
		Map<SimulationLogEntry, Integer> indexConfig = null;
		Map<String, Set<String>> originatorSets = new HashMap<String, Set<String>>();
		while(counter.hasNext()){
			//Next configuration
			indexConfig = counter.next();
			
			//Cluster the originators according to the activity
			originatorSets.clear();
			for(LogEntry entry: candidateList.keySet()){
				if(!originatorSets.containsKey(entry.getActivity())){
					originatorSets.put(entry.getActivity(), new HashSet<String>());
				}
				originatorSets.get(entry.getActivity()).add(candidateList.get(entry).get(indexConfig.get(entry)));
			}

			if(transformerEnforcedOnOriginatorSets(originatorSets, transformerAction)){
//				if(includeMessages)
//					System.out.println();
				return indexConfig;
			}
			
		}
//		if(includeMessages)
//			System.out.println();
		return null;
	}
	
	protected EnforcementResult findValidOriginatorCombination(Set<String> activityGroup,
															   List<SimulationLogEntry> allEntries,
			 												   Map<SimulationLogEntry, List<String>> candidateList, 
			 												   AbstractTransformerResult transformerResult, 
			 												   TransformerAction transformerAction) throws ParameterException{
		Map<SimulationLogEntry, Integer> validIndexCombination = findValidIndexCombination(candidateList, transformerAction);
		if(validIndexCombination == null){
			//No valid index combination could be found
			//-> Violation is not possible
			addMessageToResult(getErrorMessage(String.format(ERRORF_ORIGINATOR_COMBINATION, activityGroup)), transformerResult);
			return EnforcementResult.UNSUCCESSFUL;
		}
		//A valid index combination could be found
		//Assign originators to entries according to the valid index combination
		//and lock the originator field for those entries
		for(SimulationLogEntry entry: candidateList.keySet()){
			addMessageToResult(getNoticeMessage(String.format(CUSTOM_SINGLE_SUCCESSFUL_ENFORCEMENT_FORMAT, candidateList.get(entry).get(validIndexCombination.get(entry)), entry)), transformerResult);
			try {
				entry.setOriginator(candidateList.get(entry).get(validIndexCombination .get(entry)));
			} catch (Exception e) {
				// Should not happen since we assign a candidate of the entry itself.
				e.printStackTrace();
			} 
		}
		//Lock the originator field for all entries where it is not already locked.
		LogEntryUtils.lockFieldForEntries(EntryField.ORIGINATOR, "Transformer Enforcement: "+this.getClass().getName(), allEntries);

		return EnforcementResult.SUCCESSFUL;
}
	
	protected abstract boolean transformerEnforcedOnOriginatorSets(Map<String, Set<String>> originatorSets, TransformerAction transformerAction);

	@Override
	protected EnforcementResult ensureProperty(Set<String> activityGroup, List<SimulationLogEntry> entries, AbstractTransformerResult transformerResult) throws ParameterException{
		if(checkTrivialCase(entries, TransformerAction.ENSURE)){
			//property already enforced by the current setting of task executors
			//-> no need for enforcement 
			try{
				if(entriesForActivity.keySet().size()<2){
					addMessageToResult(getNoticeMessage(String.format(NONEEDF_TRIVIAL, activityGroup)), transformerResult);
				} else {
					addMessageToResult(getNoticeMessage(String.format(NONEEDF_ENFORCEMENT, activityGroup)), transformerResult);
				}
			}catch(ParameterException e){
				// Cannot happen, since getNotiveMessage() is not called with null-values.
				e.printStackTrace();
			}
			LogEntryUtils.lockFieldForEntries(EntryField.ORIGINATOR, "Transformer Enforcement: "+this.getClass().getName(), entries);
			return EnforcementResult.NOTNECESSARY;
		}
		return EnforcementResult.UNSUCCESSFUL;
	}

	@Override
	protected EnforcementResult violateProperty(Set<String> activityGroup, List<SimulationLogEntry> entries, AbstractTransformerResult transformerResult) throws ParameterException{
		super.violateProperty(activityGroup, entries, transformerResult);
		if(checkTrivialCase(entries, TransformerAction.VIOLATE)){
			//property already enforced by the current setting of task executors
			//-> no need for enforcement 
			try{
				if(entriesForActivity.keySet().size()<2){
					addMessageToResult(getNoticeMessage(String.format(NONEEDF_TRIVIAL, activityGroup)), transformerResult);
				} else {
					addMessageToResult(getNoticeMessage(String.format(NONEEDF_VIOLATION, activityGroup)), transformerResult);
				}
			}catch(ParameterException e){
				// Cannot happen, since getNotiveMessage() is not called with null-values.
				e.printStackTrace();
			}
			LogEntryUtils.lockFieldForEntries(EntryField.ORIGINATOR, "Transformer Enforcement: "+this.getClass().getName(), entries);
			return EnforcementResult.NOTNECESSARY;
		}
		return EnforcementResult.UNSUCCESSFUL;
	}

}
