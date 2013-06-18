package logic.filtering.filter.trace;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import log.EntryField;
import log.EntryUtils;
import log.LogEntry;
import logic.filtering.AbstractFilterResult;
import logic.filtering.filter.FilterType;
import logic.filtering.filter.properties.AbstractFilterProperties;
import logic.filtering.filter.properties.BoDFilterProperties;
import misc.SetUtils;
import properties.PropertyException;
import validate.ParameterException;

public class BoDPropertyFilter extends SoDBoDPropertyFilter {
	
	protected final String ERRORF_NO_SHARED_ORIGINATORS = "no shared originator candidates for group %s.";
	protected final String ERRORF_FIXED_DIFFERENT_ORIGINATORS = "originators are different and not alterable for group %s.";
	
	public BoDPropertyFilter(BoDFilterProperties properties) throws ParameterException, PropertyException{
		super(properties);
	}
	
	public BoDPropertyFilter(Set<String>... bindings) throws ParameterException {
		this(0.0, bindings);
	}
	
	public BoDPropertyFilter(double violationProbability, Set<String>... bindings) throws ParameterException {
		super(FilterType.BOD_FILTER, violationProbability, bindings);
	}

	@Override
	protected EnforcementResult ensureProperty(Set<String> activityGroup, List<LogEntry> entries, AbstractFilterResult filterResult) throws ParameterException {
		EnforcementResult trivialResult = super.ensureProperty(activityGroup, entries, filterResult);
		if(trivialResult.equals(EnforcementResult.SUCCESSFUL) || trivialResult.equals(EnforcementResult.NOTNECESSARY))
			return trivialResult;
		
		//The sets of originators executing the corresponding activities do not contain the same originators
		
		//Check if the locking settings for the field ORIGINATOR violate the property.
		List<LogEntry> allEntriesWithNoAlternativeOriginator = EntryUtils.getEntriesWithNoAlternativeOriginator(entries);
		if(!allEntriesWithNoAlternativeOriginator.isEmpty()){
			Set<String> allDistinctFixedOriginators = EntryUtils.getDistinctValuesForField(allEntriesWithNoAlternativeOriginator, EntryField.ORIGINATOR);
		
			List<LogEntry> activityEntriesWithNoAlternativeOriginator;
			Set<String> distinctFixedOriginatorsForActivity;
			Set<String> missingOriginators = new HashSet<String>();
			//If one of the entry sets has not enough entries for which the originator can be altered
			//the property is not enforceable
			for(String activity: activityGroup){
				//Determine all fixed originators for this activity
				activityEntriesWithNoAlternativeOriginator = EntryUtils.getEntriesWithNoAlternativeOriginator(entriesForActivity.get(activity));
				distinctFixedOriginatorsForActivity = EntryUtils.getDistinctValuesForField(activityEntriesWithNoAlternativeOriginator, EntryField.ORIGINATOR);
				//Determine the originators that have to be assigned, but are not assigned so far.
				missingOriginators.clear();
				missingOriginators.addAll(allDistinctFixedOriginators);
				missingOriginators.removeAll(distinctFixedOriginatorsForActivity);
				//Check if there are enough entries left that can potentially be assigned adequately to ensure the property.
				if(distinctFixedOriginatorsForActivity.size() + missingOriginators.size() > allDistinctFixedOriginators.size()){
					//There are not enough entries with alternative originators that potentially could ensure the property
					//-> Property cannot be ensured
					addMessageToResult(getErrorMessage(String.format(ERRORF_LOCKED_ORIGINATORS, activityGroup)), filterResult);
					return EnforcementResult.UNSUCCESSFUL;
				}
			}
			//The entries with fixed originator fields do not violate the property
			//-> Property enforcement depends on entries without locked originator field
		}
				
		//Try to choose the same originators for all other entries (without fixed originator field)
		List<LogEntry> entriesWithAlternativeOriginator = EntryUtils.getEntriesWithAlternativeOriginator(entries);	
		//Determine all originator candidates for entries with alternative originators
		Map<LogEntry, List<String>> candidateList = new HashMap<LogEntry, List<String>>();
		for(String activity: activityGroup){
			if(entriesForActivity.containsKey(activity)){
				//Otherwise there is no entry that relates to this activity
				for(LogEntry entry: entriesForActivity.get(activity)){
					//Add all originator candidates of the log entry to the list of candidates
					List<String> originatorCandidates = entry.getOriginatorCandidates();
					if(entriesWithAlternativeOriginator.contains(entry)){
						Collections.shuffle(originatorCandidates);
						candidateList.put(entry, originatorCandidates);
					}
				}
			}
		}
		
		//Try to find a valid combination of originators
		return findValidOriginatorCombination(activityGroup, entries, candidateList, filterResult, FilterAction.ENSURE);
	}
	
	@Override
	protected boolean filterEnforcedOnOriginatorSets(Map<String, Set<String>> originatorSets, FilterAction filterAction){
		//Check if any two sets intersect
		switch(filterAction){
			case ENSURE: return SetUtils.containSameElements(originatorSets.values());
			case VIOLATE:return !SetUtils.containSameElements(originatorSets.values());
			default: return false;
		}
	}
	
	@Override
	protected boolean checkTrivialCase(List<LogEntry> entries, FilterAction action) throws ParameterException{
		if(super.checkTrivialCase(entries, action))
			return true;
		//SoD is enforced if all pairwise intersections of the sets of executors of activities is empty.
		return filterEnforcedOnOriginatorSets(EntryUtils.clusterOriginatorsAccordingToActivity(entries), action);
	}
	
//				
//		
//		//Check the locking settings for the field ORIGINATOR of all entries.
//		List<LogEntry> entriesWithLockedOriginator = getEntriesWithLockedField(entries, EntryField.ORIGINATOR);
//		if(!entriesWithLockedOriginator.isEmpty()){ 
//			//There are entries with locked originator fields
//			
//			if(entriesWithLockedOriginator.size() == entries.size()){
//				//The field ORIGINATOR is locked in all entries 
//				//-> No enforcement possible since the property is not enforced trivially
//				addMessageToResult(getErrorMessage(String.format(ERRORF_FIXED_DIFFERENT_ORIGINATORS, activityGroup)), filterResult);
//				return EnforcementResult.UNSUCCESSFUL;
//			}
//			//Check the values of the locked originators
//			List<String> lockedOriginators = getFieldValues(entriesWithLockedOriginator, EntryField.ORIGINATOR);
//			if(lockedOriginators.size()>1){
//				//The values of the locked originator fields differ
//				//-> No enforcement possible
//				addMessageToResult(getErrorMessage(String.format(ERRORF_LOCKING, activityGroup)), filterResult);
//				return EnforcementResult.UNSUCCESSFUL;
//			}else{
//				//All fixed originator fields have the same value
//				//-> Try to adjust the originators of the other entries (rest-entries)
//				String fixedOriginator = lockedOriginators.get(0);
//				List<LogEntry> entriesWithoutLockedOriginator = new ArrayList<LogEntry>(entries);
//				entriesWithoutLockedOriginator.removeAll(entriesWithLockedOriginator);
//				//Check if the fixed originator is contained in all originator candidate lists
//				for(LogEntry entry: entriesWithoutLockedOriginator){
//					if(!entry.getOriginatorCandidates().contains(fixedOriginator)){
//						//The fixed originator is not an originator candidate for the current entry
//						//-> No enforcement is possible
//						addMessageToResult(getErrorMessage(String.format(ERRORF_LOCKING, activityGroup)), filterResult);
//						return EnforcementResult.UNSUCCESSFUL;
//					}
//				}
//				//The fixed originator is an originator candidate for all rest-entries
//				//-> adjust the value of the originator field for all rest-entries
//				setOriginatorForEntries(fixedOriginator, entriesWithoutLockedOriginator);
//				addMessageToResult(getErrorMessage(String.format(CUSTOM_SUCCESSFUL_ENFORCEMENT_FORMAT, fixedOriginator, activityGroup)), filterResult);
//				lockFieldForEntries(EntryField.ORIGINATOR, "Filter Enforcement: BoD", entries);
//				return EnforcementResult.SUCCESSFUL;
//			}
//		} else { //There are no entries with locked originator fields
//			
//			//Determine the set of common originator candidates
//			HashList<String> commonOriginatorCandidates = getSharedOriginatorCandidates(entries);
//			if(commonOriginatorCandidates.isEmpty()){
//				//The entries do not have a single originator candidate in common
//				//-> Property cannot be enforced
//				addMessageToResult(getErrorMessage(String.format(ERRORF_NO_SHARED_ORIGINATORS, activityGroup)), filterResult);
//				return EnforcementResult.UNSUCCESSFUL;
//			}
//			
//			//There is at least one common originator candidate
//			//-> Randomly choose one of them and adjust all entries accordingly
//			String commonOriginatorCandidate = commonOriginatorCandidates.get(rand.nextInt(commonOriginatorCandidates.size()));
//			setOriginatorForEntries(commonOriginatorCandidate, entries);
//			addMessageToResult(getSuccessMessage(String.format(CUSTOM_SUCCESSFUL_ENFORCEMENT_FORMAT, commonOriginatorCandidate, activityGroup)), filterResult);
//			lockFieldForEntries(EntryField.ORIGINATOR, "Filter Enforcement: BoD", entries);
//			return EnforcementResult.SUCCESSFUL;
//		}
//	}

	@Override
	protected EnforcementResult violateProperty(Set<String> activityGroup, List<LogEntry> entries, AbstractFilterResult filterResult) throws ParameterException {
		EnforcementResult trivialResult = super.violateProperty(activityGroup, entries, filterResult);
		if(trivialResult.equals(EnforcementResult.SUCCESSFUL) || trivialResult.equals(EnforcementResult.NOTNECESSARY))
			return trivialResult;
		//All originator sets for the different activities contain the same elements
		//-> To violate the property an arbitrary number of originators has to be altered.
		System.out.println("nontrivial");
		
		//Check the locking settings for the field ORIGINATOR of all entries.
		List<LogEntry> entriesWithNoAlternativeOriginator = EntryUtils.getEntriesWithNoAlternativeOriginator(entries);
		//If all entries of all entry sets have locked originator fields, the property is not enforceable
		
		boolean allEntriesInAllSetsLocked = true;
		for(String activity: activityGroup){
			if(!entriesWithNoAlternativeOriginator.containsAll(entriesForActivity.get(activity))){
				allEntriesInAllSetsLocked = false;
				break;
			}
		}
		if(allEntriesInAllSetsLocked){
			addMessageToResult(getErrorMessage(String.format(ERRORF_LOCKED_ORIGINATORS, activityGroup)), filterResult);
			return EnforcementResult.UNSUCCESSFUL;
		}
		
		//Try to violate the property by choosing an alternative originator 
		//for any of the entries that differs from the set of originators already chosen.
		Set<String> distinctOriginators = EntryUtils.getDistinctValuesForField(entries, EntryField.ORIGINATOR);
		for(String activity: activityGroup){
			for(LogEntry groupEntry: EntryUtils.getEntriesWithAlternativeOriginator(entriesForActivity.get(activity))){
				for(String originatorCandidate: groupEntry.getOriginatorCandidates()){
					if(!distinctOriginators.contains(originatorCandidate)){
						//Violation possible with this candidate
						addMessageToResult(getNoticeMessage(String.format(CUSTOM_SINGLE_SUCCESSFUL_ENFORCEMENT_FORMAT, originatorCandidate, groupEntry)), filterResult);
						try {
							groupEntry.setOriginator(originatorCandidate);
						} catch (Exception e) {
							// Should not happen since we iterate over the candidates of the entry itself.
							e.printStackTrace();
						} 
//						EntryUtils.setOriginatorForEntry(originatorCandidate, groupEntry);
						return EnforcementResult.SUCCESSFUL;
					}
				}
			}
		}
		
		//There is no possibility to alter the originator of any entry to violate the property
		addMessageToResult(getErrorMessage(String.format(ERRORF_ORIGINATOR_COMBINATION, activityGroup)), filterResult);
		return EnforcementResult.UNSUCCESSFUL;
	}

	@Override
	public AbstractFilterProperties getProperties() throws ParameterException, PropertyException {
		BoDFilterProperties properties = new BoDFilterProperties();
		fillProperties(properties);
		return properties;
	}

}
