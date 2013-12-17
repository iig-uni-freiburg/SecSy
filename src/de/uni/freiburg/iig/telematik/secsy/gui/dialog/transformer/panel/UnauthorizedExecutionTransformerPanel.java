package de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel;

import java.util.Set;

import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.AbstractTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.UnauthorizedExecutionTransformer;

public class UnauthorizedExecutionTransformerPanel extends AbstractTransformerPanel<UnauthorizedExecutionTransformer>{

	private static final long serialVersionUID = 5917405485312716685L;
	
	public UnauthorizedExecutionTransformerPanel(Set<String> activities) throws Exception {
		super(activities);
	}

	@Override
	protected void initialize() {}

	@Override
	protected void addComponents() {}

	@Override
	public void initializeFields(UnauthorizedExecutionTransformer transformer) throws Exception {}

	@Override
	public void validateFieldValues() throws ParameterException {}

	@Override
	public Object[] getParameters() throws Exception {
		return null;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

}
