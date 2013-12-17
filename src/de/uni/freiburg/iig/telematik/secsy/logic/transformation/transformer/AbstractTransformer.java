package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer;


import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Random;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.EntryField;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.Context;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.CaseTimeGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.AbstractTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;


@SuppressWarnings("serial")
public abstract class AbstractTransformer implements Serializable, Comparable<AbstractTransformer> {
	
	protected final String ERROR_FORMAT = "[TRANSFORMER ERROR] %s %%s";
	protected final String SUCCESS_FORMAT = "[TRANSFORMER SUCCESS] %s %%s";
	protected final String NOTICE_FORMAT = "[TRANSFORMER NOTICE] %s %%s";
	protected String name = AbstractTransformerProperties.defaultName;
	protected boolean includeMessages = AbstractTransformerProperties.defaultIncludeStatusMessages;
	protected double activationProbability = AbstractTransformerProperties.defaultActivationProbability;
	
	private final String toStringFormat = "[%s] %s (%s%%)";
	
	private CaseTimeGenerator timeGenerator = null;
	private Context context = null;
	
	protected Random rand = new Random();
	
	public AbstractTransformer(AbstractTransformerProperties properties) throws ParameterException, PropertyException{
		activationProbability = properties.getActivationProbability();
		name = properties.getName();
		includeMessages = properties.getIncludeMessages();
	}
	
	public AbstractTransformer(Double activationProbability) throws ParameterException{
		setActivationProbability(activationProbability);
	}
	
	public AbstractTransformer() {}
	
	/**
	 * Sets all transformer-specific properties.
	 * @param properties
	 * @throws Exception
	 */
	public abstract void setProperties(Object[] properties) throws Exception;
	
	public String getName(){
		return name;
	}
	
	public void setName(String name) throws ParameterException{
		Validate.notNull(name);
		this.name = name;
	}
	
	public abstract boolean requiresTimeGenerator();
	
	protected CaseTimeGenerator getTimeGenerator(){
		return timeGenerator;
	}
	
	public void setTimeGenerator(CaseTimeGenerator timeGenerator) throws ParameterException{
		Validate.notNull(timeGenerator);
		this.timeGenerator = timeGenerator;
	}
	
	public abstract boolean requiresContext();
	
	protected Context getContext(){
		return context;
	}
	
	public void setContext(Context context) throws ParameterException{
		Validate.notNull(context);
		this.context = context;
	}
	
	public void setActivationProbability(Double activationProbability) throws ParameterException{
		Validate.notNull(activationProbability);
		Validate.probability(activationProbability);
		this.activationProbability = activationProbability;
	}
	
	protected String getSuccessMessage(String message){
		if(message == null)
			message = "";
		if(!message.equals(""))
			message = ": "+message;
		try {
			return String.format(String.format(getMessageFormat(MessageType.SUCCESS), this.getClass().getName()), message);
		} catch (ParameterException e) {
			// Cannot happen, since the message type is not null.
			e.printStackTrace();
		}
		return null;
	}
	
	protected String getNoticeMessage(String message){
		if(message == null)
			message = "";
		if(!message.equals(""))
			message = ": "+message;
		try {
			return String.format(String.format(getMessageFormat(MessageType.NOTICE), this.getClass().getName()), message);
		} catch (ParameterException e) {
			// Cannot happen, since the message type is not null.
			e.printStackTrace();
		}
		return null;
	}
	
	protected String getErrorMessage(String message){
		if(message == null)
			message = "";
		if(!message.equals(""))
			message = ": "+message;
		try {
			return String.format(String.format(getMessageFormat(MessageType.ERROR), this.getClass().getName()), message);
		} catch (ParameterException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Sets the message inclusion property of the transformer.<br>
	 * If <code>true</code>, messages generated during transformer appliance can be added by
	 * {@link #addMessageToResult(String, AbstractTransformerResult)}.
	 * @param includeMessages
	 */
	public void setIncludeMessages(boolean includeMessages){
		this.includeMessages = includeMessages;
	}
	
	public boolean getIncludeMessages(){
		return includeMessages;
	}
	
	public Double getActivationProbability(){
		return activationProbability;
	}
	
	protected String getMessageFormat(MessageType messageType) throws ParameterException{
		Validate.notNull(messageType);
		switch(messageType){
		case ERROR: return ERROR_FORMAT;
		case SUCCESS: return SUCCESS_FORMAT;
		case NOTICE: return NOTICE_FORMAT;
		default: return null;
		}
	}

	/**
	 * Adds the given message to the transformer result if the include messages property is true
	 * and the given message is not <code>null</code>.
	 * @param message
	 * @param result
	 * @throws ParameterException If the given transformer result is <code>null</code>.
	 */
	protected void addMessageToResult(String message, AbstractTransformerResult result) throws ParameterException{
		Validate.notNull(result);
		if(includeMessages && message != null)
			result.addTransformerMessage(message);
	}
	
	/**
	 * Returns the log entry fields, that are required for applying the transformer.<br>
	 * This method must not return <code>null</code>
	 * @param message
	 * @param result
	 */
	public abstract List<EntryField> requiredEntryFields();
	
	@Override
	public String toString(){
		NumberFormat nf = new DecimalFormat("##0.####");
		return String.format(toStringFormat, this.getClass().getSimpleName().replace("Transformer", ""), name, nf.format(activationProbability*100.0));
	}
	
	protected void fillProperties(AbstractTransformerProperties properties) throws ParameterException, PropertyException{
		Validate.notNull(properties);
		properties.setName(getName());
		properties.setActivationProbability(getActivationProbability());
		properties.setType(this.getClass().getName());
		properties.setIncludeMessages(getIncludeMessages());
	}
	
	public abstract String getHint();
	
	@Override
	public int compareTo(AbstractTransformer o) {
		return getName().compareTo(o.getName());
	}

	protected enum MessageType {ERROR, SUCCESS, NOTICE};

}
