package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer;

import de.invation.code.toval.graphic.dialog.DialogObject;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Random;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.context.SynthesisContext;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.CaseTimeGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.AbstractTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;
import de.uni.freiburg.iig.telematik.sewol.log.EntryField;

@SuppressWarnings("serial")
public abstract class AbstractTransformer<P extends AbstractTransformerProperties, T extends AbstractTransformer<P,T>> implements Serializable, Comparable<T>, Cloneable, DialogObject<T> {

    private final String ERROR_FORMAT = "[TRANSFORMER ERROR] %s %%s";
    private final String SUCCESS_FORMAT = "[TRANSFORMER SUCCESS] %s %%s";
    private final String NOTICE_FORMAT = "[TRANSFORMER NOTICE] %s %%s";
    private String name = AbstractTransformerProperties.defaultName;
    private boolean includeMessages = AbstractTransformerProperties.defaultIncludeStatusMessages;
    private double activationProbability = AbstractTransformerProperties.defaultActivationProbability;

    private final String toStringFormat = "[%s] %s (%s%%)";

    private CaseTimeGenerator timeGenerator = null;
    private SynthesisContext context = null;

    protected Random rand = new Random();

    public AbstractTransformer(P properties) throws PropertyException {
        takeoverValuesFromProperties(properties);
    }
    
    protected void takeoverValuesFromProperties(P properties) throws PropertyException {
        activationProbability = properties.getActivationProbability();
        name = properties.getName();
        includeMessages = properties.getIncludeMessages();
    }

    public AbstractTransformer(Double activationProbability) {
        setActivationProbability(activationProbability);
    }

    public AbstractTransformer() {
    }

    /**
     * Sets all transformer-specific properties.
     *
     * @param parameters
     * @throws Exception
     */
    public abstract void setProperties(Object[] parameters) throws Exception;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Validate.notNull(name);
        this.name = name;
    }

    public abstract boolean requiresTimeGenerator();

    protected CaseTimeGenerator getTimeGenerator() {
        return timeGenerator;
    }

    public void setTimeGenerator(CaseTimeGenerator timeGenerator) {
        Validate.notNull(timeGenerator);
        this.timeGenerator = timeGenerator;
    }

    public abstract boolean requiresContext();

    protected SynthesisContext getContext() {
        return context;
    }

    public void setContext(SynthesisContext context) {
        Validate.notNull(context);
        this.context = context;
    }

    public final void setActivationProbability(Double activationProbability) {
        Validate.notNull(activationProbability);
        Validate.probability(activationProbability);
        this.activationProbability = activationProbability;
    }

    protected String getSuccessMessage(String message) {
        if (message == null) {
            message = "";
        }
        if (!message.equals("")) {
            message = ": " + message;
        }
        return String.format(String.format(getMessageFormat(MessageType.SUCCESS), this.getClass().getName()), message);
    }

    protected String getNoticeMessage(String message) {
        if (message == null) {
            message = "";
        }
        if (!message.equals("")) {
            message = ": " + message;
        }
        return String.format(String.format(getMessageFormat(MessageType.NOTICE), this.getClass().getName()), message);
    }

    protected String getErrorMessage(String message) {
        if (message == null) {
            message = "";
        }
        if (!message.equals("")) {
            message = ": " + message;
        }
        return String.format(String.format(getMessageFormat(MessageType.ERROR), this.getClass().getName()), message);
    }

    /**
     * Sets the message inclusion property of the transformer.<br>
     * If <code>true</code>, messages generated during transformer appliance can
     * be added by
     * {@link #addMessageToResult(String, AbstractTransformerResult)}.
     *
     * @param includeMessages
     */
    public void setIncludeMessages(boolean includeMessages) {
        this.includeMessages = includeMessages;
    }

    public boolean getIncludeMessages() {
        return includeMessages;
    }

    public Double getActivationProbability() {
        return activationProbability;
    }

    protected String getMessageFormat(MessageType messageType) {
        Validate.notNull(messageType);
        switch (messageType) {
            case ERROR:
                return ERROR_FORMAT;
            case SUCCESS:
                return SUCCESS_FORMAT;
            case NOTICE:
                return NOTICE_FORMAT;
            default:
                return null;
        }
    }

    /**
     * Adds the given message to the transformer result if the include messages
     * property is true and the given message is not <code>null</code>.
     *
     * @param message
     * @param result
     * @throws ParameterException If the given transformer result is
     * <code>null</code>.
     */
    protected void addMessageToResult(String message, AbstractTransformerResult result) {
        Validate.notNull(result);
        if (includeMessages && message != null) {
            result.addTransformerMessage(message);
        }
    }

    /**
     * Returns the log entry fields, that are required for applying the
     * transformer.<br>
     * This method must not return <code>null</code>
     *
     * @return 
     */
    public abstract List<EntryField> requiredEntryFields();

    @Override
    public String toString() {
        NumberFormat nf = new DecimalFormat("##0.####");
        return String.format(toStringFormat, this.getClass().getSimpleName().replace("Transformer", ""), name, nf.format(activationProbability * 100.0));
    }

    public P getProperties() throws PropertyException {
        P properties = newProperties();
        fillProperties(properties);
        return properties;
    }

    protected void fillProperties(P properties) throws PropertyException {
        Validate.notNull(properties);
        properties.setName(getName());
        properties.setActivationProbability(getActivationProbability());
        properties.setType(this.getClass().getName());
        properties.setIncludeMessages(getIncludeMessages());
    }

    public abstract String getHint();

    @Override
    public int compareTo(T o) {
        return getName().compareTo(o.getName());
    }

   @Override
    public T clone() {
        try{
            T clone = newInstance();
            clone.takeoverValues((T) this);
            return clone;
        } catch(Exception e){
            return null;
        }
    }

    protected abstract P newProperties();

    protected T newInstance(P properties) throws Exception{
        try {
            return (T) this.getClass().getConstructor(properties.getClass()).newInstance(properties);
        } catch (Exception ex) {
            throw new Exception("Cannot create new instance of class " + this.getClass().getSimpleName(), ex);
        }
    }
    
    protected T newInstance() throws Exception{
        try {
            return (T) this.getClass().newInstance();
        } catch (Exception ex) {
            throw new Exception("Cannot create new instance of class " + this.getClass().getSimpleName(), ex);
        }
    }
    

    @Override
    public void takeoverValues(T other) throws Exception {
        takeoverValuesFromProperties(other.getProperties());
    }

    protected enum MessageType {
        ERROR, SUCCESS, NOTICE
    };

}
