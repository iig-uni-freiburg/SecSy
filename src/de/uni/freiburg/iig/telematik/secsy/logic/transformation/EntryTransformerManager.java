package de.uni.freiburg.iig.telematik.secsy.logic.transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.DetailedLogEntryGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.LogEntryGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.entry.AbstractEntryTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.exception.MissingRequirementException;
import de.uni.freiburg.iig.telematik.sewol.log.EntryField;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;

/**
 * This class manages log transformers which operate on log entries.<br>
 * Entry transformer managers are used in log entry generators to apply
 * transformer to log entries after their creation.<br>
 * The compatibility of transformers with the corresponding log entry generator
 * is checked when the transformer manager source (= log entry generator) is set
 * and when transformers are added. Transformers are compatible with a log entry
 * generator when the generator provides all information required by the
 * transformer.
 * <br>
 * Transformers are applied in the order they are added to the transformer
 * manager.
 *
 * @author Thomas Stocker
 */
public class EntryTransformerManager {

    /**
     * The list of managed log entry transformers.
     */
    protected List<AbstractEntryTransformer> entryTransformers = new ArrayList<>();
    /**
     * The transformer manager source (= log entry generator).
     */
    protected LogEntryGenerator entryGenerator;

    /**
     * Sets the source of the transformer manager (= entry generator). When the
     * source is set, the compatibility of all managed transformers is checked.
     * Transformers are incompatible when they require information which is not
     * provided by the log entry generator.
     *
     * @param source The log entry generator to be used as transformer manager
     * source.
     * @throws IllegalArgumentException If the given log entry generator is
     * incompatible to one of the managed transformers.
     */
    public void setSource(LogEntryGenerator source) {
        Validate.notNull(source);
        for (AbstractEntryTransformer transformer : entryTransformers) {
            for (Object requiredEntryFieldObject : transformer.requiredEntryFields()) {
                EntryField requiredEntryField = (EntryField) requiredEntryFieldObject;
                if (!source.providesLogInformation(requiredEntryField)) {
                    throw new IllegalArgumentException("Transformer requirement (" + requiredEntryFieldObject + ") cannot be provided by source.");
                }
            }
        }
        this.entryGenerator = source;
    }

    public List<AbstractEntryTransformer> getEntryTransformers() {
        return Collections.unmodifiableList(entryTransformers);
    }

    /**
     * Adds the given transformers to the set of managed transformers.<br>
     * When transformers are added, their compatibility with the transformer
     * manager source (= entry generator) is checked. Transformers are
     * incompatible when they require information which is not provided by the
     * log entry generator.
     *
     * @param entryTransformer Set of transformers.
     * @throws MissingRequirementException If the given entry transformer is incompatible with the
     * transformer manager source.
     */
    public void addTransformer(AbstractEntryTransformer entryTransformer) throws MissingRequirementException {
        Validate.notNull(entryTransformer);
        if (entryGenerator != null) {
            for (Object requiredEntryFieldObject : entryTransformer.requiredEntryFields()) {
                EntryField requiredEntryField = (EntryField) requiredEntryFieldObject;
                if (!entryGenerator.providesLogInformation(requiredEntryField)) {
                    throw new MissingRequirementException(requiredEntryField);
                }
            }
        }
        this.entryTransformers.add(entryTransformer);
    }

    /**
     * Applies all managed transformers to the given log entry.<br>
     * As some transformers may need the corresponding case number, this
     * information is also required.<br>
     * Note: This method assumed that the given log entry is compatible with the
     * managed transformers, i.e. it provides sufficient information.
     * Compatibility is checked when transformer managers are added to log entry
     * generators.
     *
     * @see LogEntryGenerator
     * @see DetailedLogEntryGenerator
     * @param logEntry
     * @param caseNumber
     */
    public void applyTransformers(LogEntry logEntry, int caseNumber) {
        EntryTransformerEvent event = new EntryTransformerEvent(logEntry, caseNumber, entryGenerator);
        for (AbstractEntryTransformer tl : entryTransformers) {
            AbstractTransformerResult transformerResult = tl.transformLogEntry(event);
            if (transformerResult.containsMessages()) {
//				System.out.println(transformerResult.getTransformerMessages());
//				System.out.println();
            }
        }
    }

    public void clear() {
        entryTransformers.clear();
    }

}
