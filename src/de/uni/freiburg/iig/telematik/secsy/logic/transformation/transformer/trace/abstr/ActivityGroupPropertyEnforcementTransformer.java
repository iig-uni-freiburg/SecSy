package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.log.SimulationLogEntry;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.AbstractTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerEvent;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AGPropertyEnforcementTransformerProperties;

/**
 * This class defined abstract behavior for transformers that apply to groups of
 * activities within a trace, e.g. SoD or BoD constraints. The activation
 * probability is set to 1 to guarantee that on each trace, the property is
 * either ensured or violated.
 *
 * @author Thomas Stocker
 * @param <T>
 */
@SuppressWarnings("serial")
public abstract class ActivityGroupPropertyEnforcementTransformer<P extends AGPropertyEnforcementTransformerProperties, T extends ActivityGroupPropertyEnforcementTransformer<P,T>> extends AbstractTraceTransformer<P,T> {

    protected final String SUCCESSFULF_ENFORCEMENT = "Successful enforcements: %s";
    protected final String SUCCESSFULF_VIOLATION = "Successful violations: %s";
    protected final String UNSUCCESSFULF_ENFORCEMENT = "Unsuccessful enforcements: %s";
    protected final String UNSUCCESSFULF_VIOLATION = "Unsuccessful violations: %s";
    protected final String NONEEDF_ACTIVITIES_NOT_PRESENT = "No enforcement necessary - trace does not contain activity group %s";
    protected final String NONEEDF_ENFORCEMENT = "Property already ensured in group %s.";
    protected final String NONEEDF_VIOLATION = "Property already violated in group %s";
    protected final String ERRORF_CUSTOM = "[TRANSFORMER ERROR] %s Cannot enforce property on %%s";
    protected final String NOTICEF_ENFORCE = "Trying to %s property on group %s";
    protected final String NOTICEF_TRACE_RESULT = "Trace after transformation: %s";
    protected final String NOTICEF_TRACE = "Trace before transformation: %s";
    protected final String toStringFormat = "%s, violation: %s%%";

    protected double violationProbability = AGPropertyEnforcementTransformerProperties.defaultViolationProbability;
    protected List<Set<String>> activityGroups = new ArrayList<Set<String>>();

    public enum TransformerAction {

        ENSURE, VIOLATE
    };

    public ActivityGroupPropertyEnforcementTransformer(P properties) throws PropertyException {
        super(properties);
        violationProbability = properties.getViolationProbability();
        activityGroups = properties.getActivityGroups();
    }

    @Override
    protected void takeoverValuesFromProperties(P properties) throws PropertyException {
        super.takeoverValuesFromProperties(properties);
        violationProbability = properties.getViolationProbability();
        activityGroups = properties.getActivityGroups();
    }

    public ActivityGroupPropertyEnforcementTransformer() {
        super(1.0);
    }

    public ActivityGroupPropertyEnforcementTransformer(Double activationProbability) {
        super(activationProbability);
    }

    /**
     * Sets the transformer-specific properties and requires the following
     * values:<br>
     * <ul>
     * <li><code>Double</code>: Violation probability.<br>
     * Specifies the probability with which the underlying transformer-property
     * is violated on a given trace.<br>
     * Note: In case the transformer decides to violate the property if tries to
     * violate it for all activity groups.<br>
     * In all other cases the property will be enforced on the given trace.
     * <li><code>Set&lt;Set&lt;String&gt;&gt;</code>: Activity groups on which
     * the underlying transformer-property is enforced/violated.</li><br>
     * </li>
     * </ul>
     *
     * @see #setViolationProbability(Double)
     * @see #setActivityGroups(Set)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setProperties(Object[] properties) throws Exception {
        Validate.notNull(properties);
        Validate.notEmpty(properties);
        if (properties.length != 2) {
            throw new ParameterException("Wrong number of parameters. Expected 2, but got " + properties.length);
        }
        Validate.noNullElements(properties);

        Validate.type(properties[0], Double.class);
        setViolationProbability((Double) properties[0]);

        Validate.type(properties[1], Set.class);
        Set<Set<String>> activityGroups = null;
        try {
            activityGroups = (Set<Set<String>>) properties[1];
        } catch (Exception e) {
            throw new ParameterException("Wrong parameter type: " + e.getMessage());
        }
        setActivityGroups(activityGroups);
    }

    public void setViolationProbability(Double probability) {
        Validate.notNull(probability);
        Validate.probability(probability);
        this.violationProbability = probability;
    }

    public List<Set<String>> getActivityGroups() {
        return Collections.unmodifiableList(activityGroups);
    }

    public void setActivityGroups(Set<Set<String>> activityGroups) {
        Validate.notNull(activityGroups);
        Validate.noNullElements(activityGroups);
        this.activityGroups.clear();
        for (Set<String> activityGroup : activityGroups) {
            this.activityGroups.add(activityGroup);
        }
    }

    public Double getViolationProbability() {
        return violationProbability;
    }

    /**
     * Applies the transformation on the log trace.<br>
     * The property is ensured or violated for ALL activity groups.
     *
     * @throws ParameterException
     */
    @Override
    protected TraceTransformerResult applyTransformation(TraceTransformerEvent event) {
        Validate.notNull(event);
        TraceTransformerResult result = new TraceTransformerResult(event.logTrace, true);
        boolean ensureProperty = violationProbability == 0.0 || rand.nextDouble() > violationProbability;
        String successfulFormat = SUCCESSFULF_ENFORCEMENT;
        String notSuccessfulFormat = UNSUCCESSFULF_ENFORCEMENT;
        if (!ensureProperty) {
            successfulFormat = SUCCESSFULF_VIOLATION;
            notSuccessfulFormat = UNSUCCESSFULF_VIOLATION;
        }
        addMessageToResult(getNoticeMessage(String.format(NOTICEF_TRACE, event.logTrace)), result);
        //Ensure or violate the property for all activity groups.
        int counter = 0;
        EnforcementResult success;
        for (Set<String> activityGroup : activityGroups) {
            addMessageToResult(getNoticeMessage(String.format(NOTICEF_ENFORCE, (ensureProperty ? "ensure" : "violate"), activityGroup)), result);
            List<SimulationLogEntry> correspondingEntries = event.logTrace.getEntriesForActivities(activityGroup);
            if (!correspondingEntries.isEmpty()) {
                //Remove all traces that are not relevant for the transformation (e.g. do not contain enough information)
                removeIrrelevantEntries(correspondingEntries, result);
                if (!correspondingEntries.isEmpty()) {
                    //Ensure or violate the property in the entries that correspond to the actual activity group.
                    Collections.shuffle(correspondingEntries);
                    if (ensureProperty) {
                        success = ensureProperty(activityGroup, correspondingEntries, result);
                    } else {
                        success = violateProperty(activityGroup, correspondingEntries, result);
                    }
                    if (!success.equals(EnforcementResult.UNSUCCESSFUL)) {
                        counter++;
                    }
                } else {
                    //There are no traces left on which the transformation can be applied.
                    addMessageToResult(getNoticeMessage(String.format(NONEEDF_ACTIVITIES_NOT_PRESENT, activityGroup)), result);
                }
            } else {
                addMessageToResult(getNoticeMessage(String.format(NONEEDF_ACTIVITIES_NOT_PRESENT, activityGroup)), result);
                counter++;
            }
        }
        addMessageToResult(getNoticeMessage(String.format(NOTICEF_TRACE_RESULT, event.logTrace)), result);
        if (counter < activityGroups.size()) {
            //The property could not be ensured/violated for all activity groups.
            result.setTransformerSuccess(false);
            addMessageToResult(getErrorMessage(String.format(notSuccessfulFormat, activityGroups.size() - counter)), result);
        } else {
            //The property could be ensured/violated for all activity groups.
            result.setTransformerSuccess(true);
            addMessageToResult(getSuccessMessage(String.format(successfulFormat, counter)), result);
        }
        return result;
    }

    /**
     * Removes entries from the trace that are irrelevant for the property, i.e.
     * traces where specific fields are empty.
     *
     * @param entries Complete list of entries within the trace that.
     * @param result The TraceTransformerResult to be used for adding messages.
     * @return A list containing relevant entries only.
     * @throws ParameterException
     */
    protected List<SimulationLogEntry> removeIrrelevantEntries(List<SimulationLogEntry> entries, TraceTransformerResult result) {
        Validate.notNull(entries);
        Validate.noNullElements(entries);
        return entries;
    }

    /**
     * Enforces the property on a given set of log entries.
     *
     * @param activityGroup The group of activities for which the property must
     * hold.
     * @param entries The entries that relate to one of the activities in the
     * activity group.
     * @param transformerResult The TransformerResult to be used for adding
     * messages.
     * @return Outcome of the enforcement procedure.
     * @throws ParameterException
     * @see EnforcementResult
     */
    protected EnforcementResult ensureProperty(Set<String> activityGroup, List<SimulationLogEntry> entries, AbstractTransformerResult transformerResult) {
        Validate.notNull(activityGroup);
        Validate.notEmpty(activityGroup);
        Validate.noNullElements(activityGroup);
        Validate.notNull(entries);
        Validate.notEmpty(entries);
        Validate.noNullElements(entries);
        Validate.notNull(transformerResult);
        return EnforcementResult.SUCCESSFUL;
    }

    /**
     * Violates the property on a given set of log entries.
     *
     * @param activityGroup The group of activities for which the property must
     * not hold.
     * @param entries The entries that relate to one of the activities in the
     * activity group.
     * @param transformerResult The TransformerResult to be used for adding
     * messages.
     * @return Outcome of the violation procedure.
     * @throws ParameterException
     * @see EnforcementResult
     */
    protected EnforcementResult violateProperty(Set<String> activityGroup, List<SimulationLogEntry> entries, AbstractTransformerResult transformerResult) {
        Validate.notNull(activityGroup);
        Validate.notEmpty(activityGroup);
        Validate.noNullElements(activityGroup);
        Validate.notNull(entries);
        Validate.notEmpty(entries);
        Validate.noNullElements(entries);
        Validate.notNull(transformerResult);
        return EnforcementResult.SUCCESSFUL;
    }

    @Override
    protected void fillProperties(P properties) throws PropertyException {
        super.fillProperties(properties);
        properties.setViolationProbability(getViolationProbability());
        for (Set<String> activityGroup : activityGroups) {
            properties.addActivityGroup(activityGroup);
        }
    }

    /**
     * Enumeration for possible enforcement results.<br>
     * SUCCESSFUL: The property could be successfully enforced.<br>
     * UNSUCCESSFUL: The property could not be enforced.<br>
     * NOTNECESSARY: Either the (enforcement|violation) is not necessary,
     * because the property trivially (holds|does not hold) in the trace.<br>
     */
    protected enum EnforcementResult {

        SUCCESSFUL, UNSUCCESSFUL, NOTNECESSARY
    };

    @Override
    public boolean isMandatory() {
        return true;
    }

    @Override
    public String toString() {
        String superString = super.toString();
        NumberFormat nf = new DecimalFormat("##0.####");
        return String.format(toStringFormat, superString, nf.format(violationProbability * 100.0));
    }

}
