package logic.transformation.transformer;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Random;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.EntryField;

import logic.transformation.AbstractTransformerResult;
import logic.transformation.transformer.properties.AbstractTransformerProperties;

public abstract class AbstractTransformer {
	
	protected final String ERROR_FORMAT = "[TRANSFORMER ERROR] %s %%s";
	protected final String SUCCESS_FORMAT = "[TRANSFORMER SUCCESS] %s %%s";
	protected final String NOTICE_FORMAT = "[TRANSFORMER NOTICE] %s %%s";
	protected TransformerType transformerType;
	protected String name = AbstractTransformerProperties.defaultName;
	protected boolean includeMessages = AbstractTransformerProperties.defaultIncludeStatusMessages;
	protected double activationProbability = AbstractTransformerProperties.defaultActivationProbability;
	
	private final String toStringFormat = "[%s] %s (%s%%)";
	
	
	
	protected Random rand = new Random();
	
	public AbstractTransformer(AbstractTransformerProperties properties) throws ParameterException, PropertyException{
		activationProbability = properties.getActivationProbability();
		name = properties.getName();
		transformerType = properties.getType();
		includeMessages = properties.getIncludeMessages();
	}
	
	public AbstractTransformer(TransformerType transformerType, double activationProbability) throws ParameterException{
		Validate.notNull(transformerType);
		this.transformerType = transformerType;
		setActivationProbability(activationProbability);
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name) throws ParameterException{
		Validate.notNull(name);
		this.name = name;
	}
	
	public void setActivationProbability(Double activationProbability) throws ParameterException{
		Validate.probability(activationProbability);
		this.activationProbability = activationProbability;
	}
	
	public TransformerType getType(){
		return transformerType;
	}
	
	protected String getSuccessMessage(String message){
		if(message == null)
			message = "";
		if(!message.equals(""))
			message = ": "+message;
		try {
			return String.format(String.format(getMessageFormat(MessageType.SUCCESS), transformerType), message);
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
			return String.format(String.format(getMessageFormat(MessageType.NOTICE), transformerType), message);
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
			return String.format(String.format(getMessageFormat(MessageType.ERROR), transformerType), message);
		} catch (ParameterException e) {
			// TODO Auto-generated catch block
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
	public abstract List<EntryField> requiredContextInformation();
	
	@Override
	public String toString(){
		NumberFormat nf = new DecimalFormat("##0.####");
		return String.format(toStringFormat, transformerType, name, nf.format(activationProbability*100.0));
	}
	
	public abstract AbstractTransformerProperties getProperties() throws ParameterException, PropertyException;
	
	protected void fillProperties(AbstractTransformerProperties properties) throws ParameterException, PropertyException{
		Validate.notNull(properties);
		properties.setName(getName());
		properties.setActivationProbability(getActivationProbability());
		properties.setType(getType());
		properties.setIncludeMessages(getIncludeMessages());
	}
	
	protected enum MessageType {ERROR, SUCCESS, NOTICE};

}
