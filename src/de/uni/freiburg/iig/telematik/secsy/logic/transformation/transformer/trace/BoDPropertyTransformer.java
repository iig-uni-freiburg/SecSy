package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.invation.code.toval.misc.SetUtils;
import de.invation.code.toval.properties.PropertyException;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.log.SimulationLogEntry;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.log.SimulationLogEntryUtils;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.AbstractTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.BoDTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.SoDBoDPropertyTransformer;
import de.uni.freiburg.iig.telematik.sewol.log.EntryField;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntryUtils;

public class BoDPropertyTransformer extends SoDBoDPropertyTransformer<BoDTransformerProperties, BoDPropertyTransformer> {

    private static final long serialVersionUID = -6002102952666641628L;

    protected final String ERRORF_NO_SHARED_ORIGINATORS = "no shared originator candidates for group %s.";
    protected final String ERRORF_FIXED_DIFFERENT_ORIGINATORS = "originators are different and not alterable for group %s.";

    public static final String hint = "<p>This transformer enforces or violates binding of duties constraints"
            + "on a process trace.A binding of duties constraint is specified"
            + "for a set of activities. Originators are not required to execute"
            + "all activities in the set as soon as they execute one of them.</p>"
            + "<p>Instead of an activation probability, the transformer is"
            + "parameterized with a violation probability."
            + "A probability of 0 lets the transformer enforce the property.</p>"
            + "<p>The transformer fails, if originators cannot be chosen adequately.</p>";

    public BoDPropertyTransformer(BoDTransformerProperties properties) throws PropertyException {
        super(properties);
    }

    public BoDPropertyTransformer(Double violationProbability) {
        super(violationProbability);
    }

    public BoDPropertyTransformer() {
        super();
    }

    @Override
    protected EnforcementResult ensureProperty(Set<String> activityGroup, List<SimulationLogEntry> entries, AbstractTransformerResult transformerResult) {
        EnforcementResult trivialResult = super.ensureProperty(activityGroup, entries, transformerResult);
        if (trivialResult.equals(EnforcementResult.SUCCESSFUL) || trivialResult.equals(EnforcementResult.NOTNECESSARY)) {
            return trivialResult;
        }

		//The sets of originators executing the corresponding activities do not contain the same originators
        //Check if the locking settings for the field ORIGINATOR violate the property.
        List<SimulationLogEntry> allEntriesWithNoAlternativeOriginator = SimulationLogEntryUtils.getEntriesWithNoAlternativeOriginator(entries);
        if (!allEntriesWithNoAlternativeOriginator.isEmpty()) {
            Set<String> allDistinctFixedOriginators = LogEntryUtils.getDistinctValuesForField(allEntriesWithNoAlternativeOriginator, EntryField.ORIGINATOR);

            List<SimulationLogEntry> activityEntriesWithNoAlternativeOriginator;
            Set<String> distinctFixedOriginatorsForActivity;
            Set<String> missingOriginators = new HashSet<String>();
			//If one of the entry sets has not enough entries for which the originator can be altered
            //the property is not enforceable
            for (String activity : activityGroup) {
                //Determine all fixed originators for this activity
                activityEntriesWithNoAlternativeOriginator = SimulationLogEntryUtils.getEntriesWithNoAlternativeOriginator(entriesForActivity.get(activity));
                distinctFixedOriginatorsForActivity = LogEntryUtils.getDistinctValuesForField(activityEntriesWithNoAlternativeOriginator, EntryField.ORIGINATOR);
                //Determine the originators that have to be assigned, but are not assigned so far.
                missingOriginators.clear();
                missingOriginators.addAll(allDistinctFixedOriginators);
                missingOriginators.removeAll(distinctFixedOriginatorsForActivity);
                //Check if there are enough entries left that can potentially be assigned adequately to ensure the property.
                if (distinctFixedOriginatorsForActivity.size() + missingOriginators.size() > allDistinctFixedOriginators.size()) {
					//There are not enough entries with alternative originators that potentially could ensure the property
                    //-> Property cannot be ensured
                    addMessageToResult(getErrorMessage(String.format(ERRORF_LOCKED_ORIGINATORS, activityGroup)), transformerResult);
                    return EnforcementResult.UNSUCCESSFUL;
                }
            }
			//The entries with fixed originator fields do not violate the property
            //-> Property enforcement depends on entries without locked originator field
        }

        //Try to choose the same originators for all other entries (without fixed originator field)
        List<SimulationLogEntry> entriesWithAlternativeOriginator = SimulationLogEntryUtils.getEntriesWithAlternativeOriginator(entries);
        //Determine all originator candidates for entries with alternative originators
        Map<SimulationLogEntry, List<String>> candidateList = new HashMap<SimulationLogEntry, List<String>>();
        for (String activity : activityGroup) {
            if (entriesForActivity.containsKey(activity)) {
                //Otherwise there is no entry that relates to this activity
                for (SimulationLogEntry entry : entriesForActivity.get(activity)) {
                    //Add all originator candidates of the log entry to the list of candidates
                    List<String> originatorCandidates = entry.getOriginatorCandidates();
                    if (entriesWithAlternativeOriginator.contains(entry)) {
                        Collections.shuffle(originatorCandidates);
                        candidateList.put(entry, originatorCandidates);
                    }
                }
            }
        }

        //Try to find a valid combination of originators
        return findValidOriginatorCombination(activityGroup, entries, candidateList, transformerResult, TransformerAction.ENSURE);
    }

    @Override
    protected boolean transformerEnforcedOnOriginatorSets(Map<String, Set<String>> originatorSets, TransformerAction transformerAction) {
        //Check if any two sets intersect
        switch (transformerAction) {
            case ENSURE:
                return SetUtils.containSameElements(originatorSets.values());
            case VIOLATE:
                return !SetUtils.containSameElements(originatorSets.values());
            default:
                return false;
        }
    }

    @Override
    protected boolean checkTrivialCase(List<SimulationLogEntry> entries, TransformerAction action) {
        if (super.checkTrivialCase(entries, action)) {
            return true;
        }
        //SoD is enforced if all pairwise intersections of the sets of executors of activities is empty.
        return transformerEnforcedOnOriginatorSets(LogEntryUtils.clusterOriginatorsAccordingToActivity(entries), action);
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
//				addMessageToResult(getErrorMessage(String.format(ERRORF_FIXED_DIFFERENT_ORIGINATORS, activityGroup)), transformerResult);
//				return EnforcementResult.UNSUCCESSFUL;
//			}
//			//Check the values of the locked originators
//			List<String> lockedOriginators = getFieldValues(entriesWithLockedOriginator, EntryField.ORIGINATOR);
//			if(lockedOriginators.size()>1){
//				//The values of the locked originator fields differ
//				//-> No enforcement possible
//				addMessageToResult(getErrorMessage(String.format(ERRORF_LOCKING, activityGroup)), transformerResult);
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
//						addMessageToResult(getErrorMessage(String.format(ERRORF_LOCKING, activityGroup)), transformerResult);
//						return EnforcementResult.UNSUCCESSFUL;
//					}
//				}
//				//The fixed originator is an originator candidate for all rest-entries
//				//-> adjust the value of the originator field for all rest-entries
//				setOriginatorForEntries(fixedOriginator, entriesWithoutLockedOriginator);
//				addMessageToResult(getErrorMessage(String.format(CUSTOM_SUCCESSFUL_ENFORCEMENT_FORMAT, fixedOriginator, activityGroup)), transformerResult);
//				lockFieldForEntries(EntryField.ORIGINATOR, "Transformer Enforcement: BoD", entries);
//				return EnforcementResult.SUCCESSFUL;
//			}
//		} else { //There are no entries with locked originator fields
//			
//			//Determine the set of common originator candidates
//			HashList<String> commonOriginatorCandidates = getSharedOriginatorCandidates(entries);
//			if(commonOriginatorCandidates.isEmpty()){
//				//The entries do not have a single originator candidate in common
//				//-> Property cannot be enforced
//				addMessageToResult(getErrorMessage(String.format(ERRORF_NO_SHARED_ORIGINATORS, activityGroup)), transformerResult);
//				return EnforcementResult.UNSUCCESSFUL;
//			}
//			
//			//There is at least one common originator candidate
//			//-> Randomly choose one of them and adjust all entries accordingly
//			String commonOriginatorCandidate = commonOriginatorCandidates.get(rand.nextInt(commonOriginatorCandidates.size()));
//			setOriginatorForEntries(commonOriginatorCandidate, entries);
//			addMessageToResult(getSuccessMessage(String.format(CUSTOM_SUCCESSFUL_ENFORCEMENT_FORMAT, commonOriginatorCandidate, activityGroup)), transformerResult);
//			lockFieldForEntries(EntryField.ORIGINATOR, "Transformer Enforcement: BoD", entries);
//			return EnforcementResult.SUCCESSFUL;
//		}
//	}
    @Override
    protected EnforcementResult violateProperty(Set<String> activityGroup, List<SimulationLogEntry> entries, AbstractTransformerResult transformerResult) {
        EnforcementResult trivialResult = super.violateProperty(activityGroup, entries, transformerResult);
        if (trivialResult.equals(EnforcementResult.SUCCESSFUL) || trivialResult.equals(EnforcementResult.NOTNECESSARY)) {
            return trivialResult;
        }
		//All originator sets for the different activities contain the same elements
        //-> To violate the property an arbitrary number of originators has to be altered.

        //Check the locking settings for the field ORIGINATOR of all entries.
        List<SimulationLogEntry> entriesWithNoAlternativeOriginator = SimulationLogEntryUtils.getEntriesWithNoAlternativeOriginator(entries);
		//If all entries of all entry sets have locked originator fields, the property is not enforceable

        boolean allEntriesInAllSetsLocked = true;
        for (String activity : activityGroup) {
            if (!entriesWithNoAlternativeOriginator.containsAll(entriesForActivity.get(activity))) {
                allEntriesInAllSetsLocked = false;
                break;
            }
        }
        if (allEntriesInAllSetsLocked) {
            addMessageToResult(getErrorMessage(String.format(ERRORF_LOCKED_ORIGINATORS, activityGroup)), transformerResult);
            return EnforcementResult.UNSUCCESSFUL;
        }

		//Try to violate the property by choosing an alternative originator 
        //for any of the entries that differs from the set of originators already chosen.
        Set<String> distinctOriginators = LogEntryUtils.getDistinctValuesForField(entries, EntryField.ORIGINATOR);
        for (String activity : activityGroup) {
            for (SimulationLogEntry groupEntry : SimulationLogEntryUtils.getEntriesWithAlternativeOriginator(entriesForActivity.get(activity))) {
                for (String originatorCandidate : groupEntry.getOriginatorCandidates()) {
                    if (!distinctOriginators.contains(originatorCandidate)) {
                        //Violation possible with this candidate
                        addMessageToResult(getNoticeMessage(String.format(CUSTOM_SINGLE_SUCCESSFUL_ENFORCEMENT_FORMAT, originatorCandidate, groupEntry)), transformerResult);
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
        addMessageToResult(getErrorMessage(String.format(ERRORF_ORIGINATOR_COMBINATION, activityGroup)), transformerResult);
        return EnforcementResult.UNSUCCESSFUL;
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
        return false;
    }

    @Override
    protected BoDTransformerProperties newProperties() {
        return new BoDTransformerProperties();
    }

}
