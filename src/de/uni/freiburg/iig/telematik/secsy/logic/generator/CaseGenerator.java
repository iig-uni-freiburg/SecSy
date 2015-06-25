package de.uni.freiburg.iig.telematik.secsy.logic.generator;

import de.uni.freiburg.iig.telematik.secsy.logic.generator.context.SynthesisContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.invation.code.toval.constraint.AbstractConstraint;
import de.invation.code.toval.misc.valuegeneration.ValueGenerationException;
import de.invation.code.toval.validate.InconsistencyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.ParameterException.ErrorCode;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sepia.petrinet.abstr.AbstractPetriNet;
import de.uni.freiburg.iig.telematik.sepia.petrinet.abstr.AbstractTransition;
import de.uni.freiburg.iig.telematik.sepia.traversal.PNTraverser;

public class CaseGenerator {
	
	protected AbstractPetriNet net = null;
	protected PNTraverser<?> traverser = null;
	protected SynthesisContext context = null;
	protected CaseDataContainer caseDataContainer = null;
	protected AbstractTransition<?,?> nextTransition = null;
	
	protected int caseNumber = 0;
	

	public <T extends AbstractTransition<?,?>> void setPetriNet(AbstractPetriNet petriNet, PNTraverser traverser){
		Validate.notNull(petriNet);
		Validate.notNull(traverser);
		if(petriNet != traverser.getPetriNet())
			throw new ParameterException(ErrorCode.INCOMPATIBILITY, "Petri net of traverser must equal the given Petri net.");
		
		this.net = petriNet;
		this.net.reset();
		this.traverser = traverser;
	}
	
	public void setContext(SynthesisContext context){
		Validate.notNull(context);
		this.context = context;
	}
	
	public void setCaseDataContainer(CaseDataContainer caseDataContainer){
		Validate.notNull(caseDataContainer);
		this.caseDataContainer = caseDataContainer;
	}
	
	public AbstractTransition getNextTransition() throws InconsistencyException, ValueGenerationException{
		checkValidity();
		if(isCaseCompleted())
			throw new InconsistencyException("Case is completed, please call reset() to start new case.");
		AbstractTransition result = nextTransition;
		nextTransition = null;
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void findNextTransition() throws ValueGenerationException{
		checkValidity();
		if(context == null){
			nextTransition = traverser.chooseNextTransition(net.getEnabledTransitions());
		} else {
			List enabledTransitionsWithSatisfiedConstraints = new ArrayList<AbstractTransition>();
			for(Object object: net.getEnabledTransitions()){
				AbstractTransition enabledTransition = (AbstractTransition) object;
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
	
	public boolean isCaseCompleted() throws ValueGenerationException{
		if(nextTransition == null)
			findNextTransition();
		return nextTransition == null;
	}
	
	public void newCase(int caseNumber) throws InconsistencyException{
		checkValidity();
		net.reset();
		this.caseNumber = caseNumber;
		if(caseDataContainer != null){
			caseDataContainer.setActualGuardCase(caseNumber);
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
