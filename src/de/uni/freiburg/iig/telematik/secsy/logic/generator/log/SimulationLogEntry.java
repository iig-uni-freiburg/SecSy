package de.uni.freiburg.iig.telematik.secsy.logic.generator.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import de.invation.code.toval.types.DataUsage;
import de.invation.code.toval.types.HashList;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.log.DULogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.DataAttribute;
import de.uni.freiburg.iig.telematik.sewol.log.EntryField;
import de.uni.freiburg.iig.telematik.sewol.log.LockingException;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.ModificationException;
import java.util.Objects;

public class SimulationLogEntry extends DULogEntry{

	/**
	 * The list of originator candidates.<br>
	 * The originator of the log entry can only be chosen out of this candidate list.
	 */
	private HashList<String> originatorCandidates = new HashList<>();
	
	
	// ------- Constructors ----------------------------------------------------------------------
	
	/**
	 * Generates a new log entry.
	 */
	public SimulationLogEntry(){
		super();
	}
	
	/**
	 * Generates a new log entry using the given activity.
	 * @param activity Activity of the log entry.
	 * @ if the given activity is <code>null</code>.
	 */
	public SimulationLogEntry(String activity)  {
		super(activity);
	}
	
	/**
	 * Generates a new log entry using the given activity and the set of originator candidates.
	 * @param activity Activity of the log entry.
	 * @param originatorCandidates List of originator candidates.
	 */
	public SimulationLogEntry(String activity, List<String> originatorCandidates) {
		this(activity);
		try {
			this.setOriginatorCandidates(originatorCandidates);
		} catch (LockingException e) {
			// Cannot happen since no field is locked by default.
		}
	}
	
	
	// ------- Originator ------------------------------------------------------------------------
	
	/**
	 * Sets the originator of the log entry ({@link #originator}).<br>
	 * Generally only originators out of {@link #originatorCandidates} may be set as originators.<br>
	 * Depending on the actual state of the log entry, this operation may be prohibited due to locking.
	 * @param originator Originator to set.
	 * @throws NullPointerException if the given value is <code>null</code>.
	 * @throws LockingException if the originator field is locked <br>and the given value differs from the actual value of {@link #originator}.
	 * @return <code>true</code> if {@link #originator} was modified;<br>
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean setOriginator(String originator) throws LockingException {
		Validate.notNull(originator);

		if(isFieldLocked(EntryField.ORIGINATOR)){
			if(!this.originator.equals(originator))
				throw new LockingException(EntryField.ORIGINATOR);
			return false;
		} else {
			if(!originatorCandidates.contains(originator))
				throw new ParameterException("Originator not contained in candidate list.");
			this.originator = originator;
			return true;
		}
	}
	

	/**
	 * Chooses the originator with the given index from {@link #originatorCandidates} as new value for {@link #originator}.
	 * @param index Index of the originator candidate.
	 * @ if the extracted originator candidate is <code>null</code> or cannot be extracted.
	 * @throws LockingException if the originator field is locked <br>and the given value differs from the actual value of {@link #originator}.
	 * @return <code>true</code> if {@link #originator} was modified;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean setOriginator(Integer index) throws LockingException{
		Validate.notNull(index);
		Validate.notNegative(index);
		
		String origin = null;
		try {
			origin = originatorCandidates.get(index);
		} catch(Exception e){
			throw new ParameterException("Cannot extract candidate with index " + index, e);
		}
		return setOriginator(origin);
	}
	
	
	// ------- Originator Candidates -------------------------------------------------------------
	
	/**
	 * Returns the list of originator candidates ({@link #originatorCandidates}).
	 * @return all originator candidates
	 */
	public List<String> getOriginatorCandidates(){
		return new ArrayList<>(originatorCandidates);
	}
	
	/**
	 * Sets the given originator as the only originator candidate and thus as the value of {@link #originator}.
	 * @param originator Originator candidate
	 * @ if the given originator is <code>null</code>.
	 * @throws LockingException if the field ORIGINATOR_CANDIDATES is locked <br>and the given candidate is not already the only candidate.
	 * @return <code>true</code> if {@link #originatorCandidates} was modified;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean setOriginatorCandidate(String originator) throws LockingException {
		Validate.notNull(originator);
		return setOriginatorCandidates(Collections.singletonList(originator));
	}
	
	/**
	 * Sets the elements of the given list as originator candidates and chooses a random entry as new value of {@link #originator}.
	 * @param originators List of originator candidates
	 * @ if the list is <code>null</code>.
	 * @throws LockingException if the field ORIGINATOR_CANDIDATES is locked <br>and the given candidates are not the same than the current ones.
	 * @return <code>true</code> if {@link #originatorCandidates} was modified;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean setOriginatorCandidates(List<String> originators) throws LockingException {
		Validate.notNull(originators);
		Validate.notEmpty(originators);
		Validate.noNullElements(originators);

		if(isFieldLocked(EntryField.ORIGINATOR_CANDIDATES)){
			if(!(originatorCandidates.containsAll(originators) && originators.containsAll(originatorCandidates)))
				throw new LockingException(EntryField.ORIGINATOR_CANDIDATES);
			return false;
		} else {
			this.originatorCandidates.clear();
			for(String candidate: originators){
				addOriginatorCandidate(candidate);
			}
			chooseOriginator();
			return true;
		}
	}
	
	/**
	 * Adds the given originator to the list of originator candidates {@link #originatorCandidates}.
	 * @param originator Originator candidate to add
	 * @ if the given originator candidate is <code>null</code> or empty.
	 * @throws LockingException if The field ORIGINATOR_CANDIDATES is locked <br>and {@link #originatorCandidates} does not already contain the given candidate.
	 * @return <code>true</code> if {@link #originatorCandidates} was modified;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean addOriginatorCandidate(String originator) throws LockingException {
		Validate.notNull(originator);
		Validate.notEmpty(originator);
		if(isFieldLocked(EntryField.ORIGINATOR_CANDIDATES)){
			if(!originatorCandidates.contains(originator))
				throw new LockingException(EntryField.ORIGINATOR_CANDIDATES);
			return false;
		}
		originatorCandidates.add(originator);
		chooseOriginator();
		return true;
	}
	
	/**
	 * Adds the given originators to the list of originator candidates ({@link #originatorCandidates}).
	 * @param originators Originators to add
	 * @ if the given originator candidate list is <code>null</code>, empty or contains invalid values.
	 * @throws LockingException if the originator candidate field is locked <br>and {@link #originatorCandidates} does not already contain all given candidates.
	 * @return <code>true</code> if {@link #originatorCandidates} was modified;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean addOriginatorCandidates(List<String> originators) throws LockingException {
		Validate.notNull(originators);
		Validate.notEmpty(originators);
		if(isFieldLocked(EntryField.ORIGINATOR_CANDIDATES)){
			if(!originatorCandidates.containsAll(originators))
				throw new LockingException(EntryField.ORIGINATOR_CANDIDATES);
			return false;
		}
		for(String originator: originators){
			Validate.notNull(originator);
			Validate.notEmpty(originator);
			originatorCandidates.add(originator);
		}
		chooseOriginator();
		return true;
	}
	
	/**
	 * Removes the given originator from the list of originator candidates ({@link #originatorCandidates}).
	 * @param originator Originator to remove.
	 * @return <code>true</code> if {@link #originatorCandidates} was modified;<br>
	 * <code>false</code> otherwise.
	 * @ if the given originator is <code>null</code>.
	 * @throws LockingException if the field ORIGINATOR_CANDIDATES is locked <br>and the given originator is not already contained in {@link #originatorCandidates}.
	 */
	public boolean removeOriginatorCandidate(String originator) throws LockingException{
		if(originator==null)
			throw new NullPointerException();
		return removeOriginatorCandidates(Collections.singletonList(originator));
	}
	
	/**
	 * Removes the given originators from the list of originator candidates ({@link #originatorCandidates}).
	 * @param originators Originators to remove
	 * @return <code>true</code> if {@link #originatorCandidates} was modified;<br>
	 * <code>false</code> otherwise.
	 * @ if the given originator list is <code>null</code>.
	 * @throws LockingException if the field ORIGINATOR_CANDIDATES is locked <br>and the given originators are not already contained in {@link #originatorCandidates}.
	 */
	public boolean removeOriginatorCandidates(Collection<String> originators) throws LockingException{
		Validate.notNull(originators);
		if(originators.isEmpty())
			return false;
		if(isFieldLocked(EntryField.ORIGINATOR_CANDIDATES)){
			HashList<String> check = originatorCandidates.clone();
			check.removeAll(originators);
			if(check.size()<originatorCandidates.size())
				throw new LockingException(EntryField.ORIGINATOR_CANDIDATES);
			return false;
		} else {
			boolean change = this.originatorCandidates.removeAll(originators);
			chooseOriginator();
			return change;
		}
	}
	
	/**
	 * Removes all originator candidates.<br>
	 * Note that this operation sets {@link #originator} to <code>null</code>.
	 * @return <code>true</code> if {@link #originatorCandidates} was modified;<br>
	 * <code>false</code> otherwise.
	 * @throws LockingException if the field ORIGINATOR_CANDIDATE is locked and {@link #originatorCandidates} is not empty.
	 */
	public boolean removeAllOriginatorCandidates() throws LockingException{
		if(originatorCandidates.isEmpty())
			return false;
		if(isFieldLocked(EntryField.ORIGINATOR)){
			if(!originatorCandidates.isEmpty())
				throw new LockingException(EntryField.ORIGINATOR_CANDIDATES);
		}
		originatorCandidates.clear();
		chooseOriginator();
		return true;
	}
	
	/**
	 * Randomly chooses an originator candidate for {@link #originator}.<br>
	 * If there are no candidates, the value of {@link #originator} is set to <code>null</code>.
	 */
	protected void chooseOriginator(){
		if(!originatorCandidates.isEmpty()){
			originator = originatorCandidates.get(rand.nextInt(originatorCandidates.size()));
		} else {
			originator = null;
		}
	}
	
	
	//------- Overridden methods ---------------------------------------------------------------------

	@Override
	protected LogEntry newInstance() {
		return new SimulationLogEntry();
	}

	@Override
	public SimulationLogEntry clone() {
		return (SimulationLogEntry) super.clone();
	}

	@Override
	protected void copyFieldValues(LogEntry clone) throws LockingException {
		super.copyFieldValues(clone);
		for(DataAttribute att: dataUsage.keySet()){
			((SimulationLogEntry) clone).setDataUsageFor(att, new HashSet<>(dataUsage.get(att)));
		}
		((SimulationLogEntry) clone).setOriginatorCandidates(originatorCandidates);
	}

	@Override
	public Object getFieldValue(EntryField field) {
		Object superValue = super.getFieldValue(field);
		if(superValue != null)
			return superValue;
		if(field == EntryField.ORIGINATOR_CANDIDATES){
			if(dataUsage != null){
				return Collections.unmodifiableSet(originatorCandidates);
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((originatorCandidates == null) ? 0 : originatorCandidates.hashCode());
		return result;
	}

        @Override
        public boolean equals(Object obj) {
                if (obj == null) {
                        return false;
                }
                if (getClass() != obj.getClass()) {
                        return false;
                }
                final SimulationLogEntry other = (SimulationLogEntry) obj;
                return Objects.equals(this.originatorCandidates, other.originatorCandidates);
        }
}
