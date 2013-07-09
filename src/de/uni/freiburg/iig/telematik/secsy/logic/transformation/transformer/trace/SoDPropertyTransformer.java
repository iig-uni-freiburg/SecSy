package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.invation.code.toval.misc.SetUtils;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.jawl.log.EntryUtils;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.AbstractTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.TransformerType;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.SoDTransformerProperties;


/**
 * This class defines a transformer that is able to ensure or violate an SoD-property on a set of activityGroups in log traces.
 * The SoD property is considered violated on a given set of activities if the corresponding set of originators intersect.<br>
 * A1: {o1, o2, o3}<br>
 * A2: {o4, o5, o6}<br>
 * A3: {o7, o2, o8}<br>
 * Common elements of A1 and A3: {o2} -> violation of SoD.<br>
 * Generally, the SoD property is violated if any two originator sets intersect.<br>
 * 
 * @author Thomas Stocker
 */
public class SoDPropertyTransformer extends SoDBoDPropertyTransformer {
	
	public SoDPropertyTransformer(SoDTransformerProperties properties) throws ParameterException, PropertyException {
		super(properties);
	}

	public SoDPropertyTransformer(Set<String>... bindings) throws ParameterException {
		this(0.0, bindings);
	}
	
	public SoDPropertyTransformer(double violationProbability, Set<String>... bindings) throws ParameterException {
		super(TransformerType.SOD, violationProbability, bindings);
	}

	@Override
	protected EnforcementResult ensureProperty(Set<String> activityGroup, List<LogEntry> entries, AbstractTransformerResult transformerResult) throws ParameterException {
		EnforcementResult trivialResult = super.ensureProperty(activityGroup, entries, transformerResult);
		if(trivialResult.equals(EnforcementResult.SUCCESSFUL) || trivialResult.equals(EnforcementResult.NOTNECESSARY))
			return trivialResult;
		
		//The sets of originators executing the corresponding activities
		
		//Check if the locking settings for the field ORIGINATOR violate the property.
		List<LogEntry> entriesWithNoAlternativeOriginator = EntryUtils.getEntriesWithNoAlternativeOriginator(entries);
		//Determine the set of fixed originators for each activity
		Map<String, Set<String>> fixedOriginatorSets = new HashMap<String, Set<String>>();
		if(!entriesWithNoAlternativeOriginator.isEmpty())
			EntryUtils.clusterOriginatorsAccordingToActivity(entriesWithNoAlternativeOriginator);
		if(!entriesWithNoAlternativeOriginator.isEmpty()){ 
			//There are entries with locked or unalterable originator fields
			//If any two sets of entries each belonging to a specific activity 
			//with locked or unalterable originator fields share an originator
			//the property is not enforceable
			
			//check if there are any intersections between the sets of fixed originators
			if(SetUtils.existPairwiseIntersections(fixedOriginatorSets.values())){
				//There are common originators amongst at least two sets
				//-> As originators are not alterable, the property is not enforceable
				addMessageToResult(getErrorMessage(String.format(ERRORF_FIXED_EQUAL_ORIGINATORS, activityGroup)), transformerResult);
				return EnforcementResult.UNSUCCESSFUL;
			}
		}
		//The entries with fixed originator fields do not violate the property
		//-> Property enforcement depends on entries without locked originator field
		
		//Try to choose different originators for all other entries (without fixed originator field)
		List<LogEntry> entriesWithAlternativeOriginator = EntryUtils.getEntriesWithAlternativeOriginator(entries);	
		//Determine all originator candidates for entries with alternative originators
		Map<LogEntry, List<String>> candidateList = new HashMap<LogEntry, List<String>>();
		for(String activity: activityGroup){
			if(entriesForActivity.containsKey(activity)){
				//Otherwise there is no entry that relates to this activity
				for(LogEntry entry: entriesForActivity.get(activity)){
					//Add all originator candidates of the log entry to the list of candidates
					List<String> originatorCandidates = entry.getOriginatorCandidates();
					//Remove all fixed originators of entries conducting concurrent activities from the list
					if(entriesWithAlternativeOriginator.contains(entry)){
						for(String otherActivity: activityGroup){
							if(!activity.equals(otherActivity) && fixedOriginatorSets.containsKey(otherActivity)){
								originatorCandidates.removeAll(fixedOriginatorSets.get(otherActivity));
							}
						}
						if(originatorCandidates.isEmpty()){
							//No valid combination possible because there is no originator left that can be chosen for this entry
							//-> Property not enforceable
							addMessageToResult(getErrorMessage(String.format(ERRORF_LOCKED_ORIGINATORS, activityGroup)), transformerResult);
							return EnforcementResult.UNSUCCESSFUL;
						}
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
	protected EnforcementResult violateProperty(Set<String> activityGroup, List<LogEntry> entries, AbstractTransformerResult transformerResult) throws ParameterException {
		EnforcementResult trivialResult = super.violateProperty(activityGroup, entries, transformerResult);
		if(trivialResult.equals(EnforcementResult.SUCCESSFUL) || trivialResult.equals(EnforcementResult.NOTNECESSARY))
			return trivialResult;
		
		//There are no overlaps in the sets of originators executing the corresponding activities
		
		//Try if there are entries for which alternative originators can be set
		List<LogEntry> entriesWithAlternativeOriginator = EntryUtils.getEntriesWithAlternativeOriginator(entries);	
		if(entriesWithAlternativeOriginator.isEmpty()){
			//No valid combination possible because for no entry the originator can be altered.
			//-> Violation not enforceable
			addMessageToResult(getErrorMessage(String.format(ERRORF_LOCKED_ORIGINATORS, activityGroup)), transformerResult);
			return EnforcementResult.UNSUCCESSFUL;
		}
		//There are log entries that have alternative originators.
		
		//Determine all originator candidates for entries with alternative originators
		Map<LogEntry, List<String>> candidateList = new HashMap<LogEntry, List<String>>();
		for(String activity: activityGroup){
			if(entriesForActivity.containsKey(activity)){
				//Otherwise there is no entry that relates to this activity
				for(LogEntry entry: entriesForActivity.get(activity)){
					if(entriesWithAlternativeOriginator.contains(entry)){
						//Add all originator candidates of the log entry to the list of candidates
						List<String> originatorCandidates = entry.getOriginatorCandidates();
						Collections.shuffle(originatorCandidates);
						candidateList.put(entry, originatorCandidates);
					}
				}
			}
		}
		
		//Try to find a valid combination of originators
		return findValidOriginatorCombination(activityGroup, entries, candidateList, transformerResult, TransformerAction.VIOLATE);
	}

	@Override
	protected boolean transformerEnforcedOnOriginatorSets(Map<String, Set<String>> originatorSets, TransformerAction transformerAction){
		//Check if any two sets intersect
		switch(transformerAction){
			case ENSURE: return !SetUtils.existPairwiseIntersections(originatorSets.values());
			case VIOLATE:return SetUtils.existPairwiseIntersections(originatorSets.values());
			default: return false;
		}
	}

	@Override
	public AbstractTransformerProperties getProperties() throws ParameterException, PropertyException {
		SoDTransformerProperties properties = new SoDTransformerProperties();
		fillProperties(properties);
		return properties;
	}

}
