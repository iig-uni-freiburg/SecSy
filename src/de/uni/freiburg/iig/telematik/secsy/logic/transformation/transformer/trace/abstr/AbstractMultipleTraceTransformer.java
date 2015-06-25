package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.log.SimulationLogEntry;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.AbstractTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerEvent;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractMultipleTraceTransformerProperties;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;

/**
 *
 *
 * @author Thomas Stocker
 * @param <P>
 * @param <T>
 */
public abstract class AbstractMultipleTraceTransformer<P extends AbstractMultipleTraceTransformerProperties, T extends AbstractMultipleTraceTransformer<P,T>> extends AbstractTraceTransformer<P,T> {

    private static final long serialVersionUID = 5882420755943878025L;

    protected final String TARGET_APPLIANCES_FORMAT = "target appliances: %s";
    protected final String UNSUCCESSFUL_APPLIANCES_FORMAT = "unsuccessful appliances: %s";
    protected final String SUCCESSFUL_APPLIANCES_FORMAT = "successful appliances: %s";

    /**
     * General maximum number of appliances on a given trace.
     */
    protected int maxAppliances = AbstractMultipleTraceTransformerProperties.defaultMaxAppliances;

    /**
     * target transformer appliances for the actual trace.
     */
    protected int targetAppliances;

    protected Set<LogEntry> transformedEntries = new HashSet<LogEntry>();

    public AbstractMultipleTraceTransformer(P properties) throws PropertyException {
        super(properties);
    }
    
    @Override
    protected void takeoverValuesFromProperties(P properties) throws PropertyException {
        super.takeoverValuesFromProperties(properties);
        maxAppliances = properties.getMaxAppliances();
    }

    public AbstractMultipleTraceTransformer(Double activationProbability, Integer maxAppliances) {
        super(activationProbability);
        setMaxAppliances(maxAppliances);
    }

    public AbstractMultipleTraceTransformer() {
        super();
    }

    public Integer getMaxAppliances() {
        return maxAppliances;
    }

    public final void setMaxAppliances(Integer maxAppliances) {
        Validate.bigger(maxAppliances, 0);
        this.maxAppliances = maxAppliances;
    }

    @Override
    protected TraceTransformerResult applyTransformation(TraceTransformerEvent event) {
        transformedEntries.clear();
        TraceTransformerResult result = new TraceTransformerResult(event.logTrace, true);

        //Decide on how many log entries, the filter is applied.
        determineAppliances(event.logTrace.size(), result);

        int successfulAppliances = 0;
        List<SimulationLogEntry> entries = new ArrayList<SimulationLogEntry>(event.logTrace.getEntries());
        Collections.shuffle(entries);
        Iterator<SimulationLogEntry> iter = entries.iterator();
        SimulationLogEntry nextEntry;
        while (successfulAppliances < targetAppliances && iter.hasNext()) {
            nextEntry = iter.next();
            if (applyEntryTransformation(event.logTrace, nextEntry, result)) {
                successfulAppliances++;
                transformedEntries.add(nextEntry);
            }
        }
        if (successfulAppliances == 0) {
            result.setTransformerSuccess(false);
            addMessageToResult(getErrorMessage(""), result);
        } else {
            result.setTransformerSuccess(true);
            addMessageToResult(getSuccessMessage(String.format(SUCCESSFUL_APPLIANCES_FORMAT, successfulAppliances)), result);
            if (successfulAppliances < targetAppliances) {
                addMessageToResult(getNoticeMessage(String.format(UNSUCCESSFUL_APPLIANCES_FORMAT, targetAppliances - successfulAppliances)), result);
            }
        }
        return result;
    }

    protected void determineAppliances(int logEntries, AbstractTransformerResult result) {
        Validate.notNull(logEntries);
        while ((targetAppliances = rand.nextInt(maxAppliances) + 1) > logEntries) {
        }
        addMessageToResult(getNoticeMessage(String.format(TARGET_APPLIANCES_FORMAT, targetAppliances)), result);
    }

    protected abstract boolean applyEntryTransformation(LogTrace<SimulationLogEntry> trace, SimulationLogEntry entry, TraceTransformerResult transformerResult);

    @Override
    protected void fillProperties(P properties) throws PropertyException {
        super.fillProperties(properties);
        properties.setMaxAppliances(maxAppliances);
    }

}
