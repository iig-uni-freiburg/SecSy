package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.custom;

import java.util.ArrayList;
import java.util.List;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerEvent;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.AbstractTraceTransformer;
import de.uni.freiburg.iig.telematik.sewol.log.DataAttribute;
import de.uni.freiburg.iig.telematik.sewol.log.EntryField;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;

public class DesignatorTransformer extends AbstractTraceTransformer{

	private static final long serialVersionUID = -6609100379391629297L;
	
	private Integer minSize = null;
	private String designator = null;

	public DesignatorTransformer() {
		super();
	}

	public DesignatorTransformer(Double activationProbability) {
		super(activationProbability);
	}

	/**
	 * Sets the transformer properties.<br>
	 * This method expects exactly 2 parameters.<br>
	 * The first parameter is used for the transformer-property "minSize" and must be a positive integer.<br>
	 * The second parameter is used for the transformer-property "designator" and must be a string.
	 */
	@Override
	public void setProperties(Object[] properties) throws Exception {
		// Validate the given parameters
		Validate.notNull(properties);
		Validate.notEmpty(properties);
		if(properties.length != 2)
			throw new ParameterException("Wrong number of parameters. Expected 2, but got " + properties.length);
		Validate.noNullElements(properties);
		Validate.type(properties[0], Integer.class);
		Validate.type(properties[1], String.class);
		
		setMinSize((Integer) properties[0]);
		setDesignator((String) properties[1]);
	}
	
	public Integer getMinSize() {
		return minSize;
	}

	public void setMinSize(Integer minSize) {
		Validate.notNull(minSize);
		Validate.positive(minSize);
		this.minSize = minSize;
	}

	public String getDesignator() {
		return designator;
	}

	public void setDesignator(String designator) {
		Validate.notNull(designator);
		Validate.notEmpty(designator);
		this.designator = designator;
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
	public boolean isMandatory() {
		return true;
	}

	@Override
	public List<EntryField> requiredEntryFields() {
		List<EntryField> result = new ArrayList<EntryField>();
		result.add(EntryField.ACTIVITY);
		return result;
	}

	@Override
	public String getHint() {
		return "<p>This is a dummy custom-transformer. Based on the parameter \"minSize\", " +
				"it adds a case attribute to each trace with length >= minSize with " +
				"the value of the parameter \"designator\".</p>";
	}

	@Override
	protected TraceTransformerResult applyTransformation(TraceTransformerEvent event){
		TraceTransformerResult result = new TraceTransformerResult(event.logTrace, true);
		if(event.logTrace.size() >= minSize){
			for(LogEntry traceEntry: event.logTrace.getEntries()){
				traceEntry.addMetaAttribute(new DataAttribute("designator", designator));
			}
		}
		return result;
	}

}
