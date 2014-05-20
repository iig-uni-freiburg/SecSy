package de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer;

import java.util.Set;

import javax.swing.JPanel;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.AbstractTransformer;

@SuppressWarnings("serial")
public abstract class AbstractTransformerPanel<T extends AbstractTransformer> extends JPanel {
	
	protected Set<String> activities = null;

	public AbstractTransformerPanel(Set<String> activities) throws Exception{
		Validate.notNull(activities);
		Validate.noNullElements(activities);
		this.activities = activities;
		initialize();
		addComponents();
	}
	
	protected Set<String> getActivities(){
		return activities;
	}
	
	protected abstract void initialize() throws Exception;
	
	protected abstract void addComponents() throws Exception;
	
	public abstract void initializeFields(T transformer) throws Exception;
	
	public abstract void validateFieldValues();
	
	public abstract Object[] getParameters() throws Exception;
	
	public abstract boolean isEmpty();
}
