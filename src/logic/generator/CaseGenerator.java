package logic.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import misc.valuegeneration.ValueGenerationException;
import petrinet.AbstractPetriNet;
import petrinet.AbstractTransition;
import traversal.PNTraverser;
import validate.InconsistencyException;
import validate.ParameterException;
import validate.ParameterException.ErrorCode;
import validate.Validate;
import constraint.AbstractConstraint;

public class CaseGenerator {
	
	protected AbstractPetriNet<?,?,?,?,?> net = null;
	protected PNTraverser<?> traverser = null;
	protected Context context = null;
	protected CaseDataContainer caseDataContainer = null;
	protected AbstractTransition<?,?> nextTransition = null;
	
	protected int caseNumber = 0;
	

	public void setPetriNet(AbstractPetriNet<?,?,?,?,?> petriNet, PNTraverser<?> traverser) throws ParameterException{
		Validate.notNull(petriNet);
		Validate.notNull(traverser);
		if(petriNet != traverser.getPetriNet())
			throw new ParameterException(ErrorCode.INCOMPATIBILITY, "Petri net of traverser must equal the given Petri net.");
		
		this.net = petriNet;
		this.net.reset();
		this.traverser = traverser;
	}
	
	public void setContext(Context context) throws ParameterException{
		Validate.notNull(context);
		this.context = context;
	}
	
	public void setCaseDataContainer(CaseDataContainer caseDataContainer) throws ParameterException{
		Validate.notNull(caseDataContainer);
		this.caseDataContainer = caseDataContainer;
	}
	
	public AbstractTransition<?,?> getNextTransition() throws InconsistencyException, ParameterException, ValueGenerationException{
		checkValidity();
		if(isCaseCompleted())
			throw new InconsistencyException("Case is completed, please call reset() to start new case.");
		AbstractTransition<?,?> result = nextTransition;
		nextTransition = null;
		return result;
	}
	
	private void findNextTransition() throws ParameterException, ValueGenerationException{
		checkValidity();
		if(context == null){
			nextTransition = traverser.chooseNextTransition((List<AbstractTransition<?,?>>) net.getEnabledTransitions());
		} else {
			List<AbstractTransition<?,?>> enabledTransitionsWithSatisfiedConstraints = new ArrayList<AbstractTransition<?,?>>();
			for(AbstractTransition<?,?> enabledTransition: net.getEnabledTransitions()){
				if(enabledTransition.isSilent()){
					enabledTransitionsWithSatisfiedConstraints.add(enabledTransition);
					continue;
				}
				String activity = enabledTransition.getLabel();
				if(context.hasRoutingConstraints(activity)){
					boolean constraintsSatisfied = true;
					Map<String, Object> caseAttributeValues = caseDataContainer.getAttributeValuesForActivity(activity, caseNumber);
					for(AbstractConstraint constraint: context.getRoutingConstraints(activity)){
						if(!constraint.validate(caseAttributeValues.get(constraint.getElement()))){
							constraintsSatisfied = false;
							break;
						}
					}
					if(constraintsSatisfied){
						enabledTransitionsWithSatisfiedConstraints.add(enabledTransition);
					}
				} else {
					enabledTransitionsWithSatisfiedConstraints.add(enabledTransition);
				}
				
			}
			nextTransition = traverser.chooseNextTransition(enabledTransitionsWithSatisfiedConstraints);
		}
	}
	
	public boolean isCaseCompleted() throws ParameterException, ValueGenerationException{
		if(nextTransition == null)
			findNextTransition();
		return nextTransition == null;
	}
	
	public void newCase(int caseNumber) throws InconsistencyException{
		checkValidity();
		net.reset();
		this.caseNumber = caseNumber;
		if(caseDataContainer != null){
			try {
				caseDataContainer.setActualGuardCase(caseNumber);
			} catch (ParameterException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void checkValidity() throws InconsistencyException{
		if(net == null)
			throw new InconsistencyException("No Petri net assigned.");
		if(traverser == null)
			throw new InconsistencyException("No Petri net traverser assigned.");
		if(context != null && caseDataContainer == null)
			throw new InconsistencyException("Context without case data container.");
		if(context == null && caseDataContainer != null)
			throw new InconsistencyException("Case data container without context.");
	}
	
	public boolean isValid(){
		try {
			checkValidity();
		} catch (InconsistencyException e) {
			return false;
		}
		return true;
	}
//	
//	public static <T extends AbstractTransition<?>> CaseGenerator<T> createCaseGenerator(AbstractPetriNet<?,T,?,?,?> petriNet, PNTraverser<T> traverser) 
//			throws ParameterException{
//		CaseGenerator<T> generator = new CaseGenerator<T>();
//		generator.setPetriNet(petriNet, traverser);
//		return generator;
//	}

}
