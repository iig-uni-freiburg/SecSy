package logic.simulation;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import logic.generator.TraceCompletionListener;
import logic.simulation.properties.SimulationRunProperties;
import logic.transformation.EntryTransformerManager;
import logic.transformation.TraceTransformerManager;
import logic.transformation.transformer.entry.AbstractEntryFilter;
import logic.transformation.transformer.trace.AbstractTraceFilter;
import petrinet.AbstractPetriNet;
import petrinet.AbstractTransition;
import traversal.PNTraverser;
import traversal.RandomPNTraverser;

/**
 * Simple class for simulation runs.
 * 
 * @author Thomas Stocker
 */
public class SimulationRun implements TraceCompletionListener{
	
	private static final String toStringFormat = "%s: %s, %s passes%s";
	private static final String filterformat = "--- %s\n";
	
	protected AbstractPetriNet<?,?,?,?,?> petriNet = null;
	protected PNTraverser<?> pnTraverser = null;
	protected TraceTransformerManager traceFilterManager = new TraceTransformerManager();
	protected EntryTransformerManager entryFilterManager = new EntryTransformerManager();
	
	protected Integer passes = null;
	protected int generatedTraces = 0;
	
	private String name = SimulationRunProperties.defaultName;
	
	//------- Constructors ----------------------------------------------------------------------------------

	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
			   								   int passes,
			   								   PNTraverser traverser) 
			   								   throws ParameterException {
		setPasses(passes);
		setPetriNet(petriNet);
		setPNTraverser(traverser);
	}
	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
														   int passes) 
														   throws ParameterException {
		this(petriNet, passes, new RandomPNTraverser<T>(petriNet));
	}
	

	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
														   int passes,
														   PNTraverser traverser,
														   TraceTransformerManager traceFilterManager) 
														   throws ParameterException {
		this(petriNet, passes, traverser);
		setTraceFilterManager(traceFilterManager);
	}
	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
			   												int passes,
			   												TraceTransformerManager traceFilterManager) 
			   											    throws ParameterException {
		this(petriNet, passes, new RandomPNTraverser<T>(petriNet), traceFilterManager);
	}

	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
			   												int passes,
			   												PNTraverser traverser,
			   												EntryTransformerManager entryFilterManager) 
			   											    throws ParameterException {
		this(petriNet, passes, traverser);
		setEntryFilterManager(entryFilterManager);
	}

	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
														   int passes,
														   EntryTransformerManager entryFilterManager) 
														   throws ParameterException {
		this(petriNet, passes, new RandomPNTraverser<T>(petriNet), entryFilterManager);
	}

	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
														   int passes,
														   TraceTransformerManager traceFilterManager,
														   EntryTransformerManager entryFilterManager) 
														   throws ParameterException {
		this(petriNet, passes);
		setEntryFilterManager(entryFilterManager);
		setTraceFilterManager(traceFilterManager);
	}

	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?,T,?,?,?> petriNet, 
														   PNTraverser pnTraverser, 
														   int passes) 
														   throws ParameterException{
		this(petriNet, passes);
		setPNTraverser(pnTraverser);
	}
	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?,T,?,?,?> petriNet, 
														   PNTraverser pnTraverser, 
														   int passes, 
														   TraceTransformerManager traceFilterManager) 
														   throws ParameterException {
		this(petriNet, passes, traceFilterManager);
		setPNTraverser(pnTraverser);
	}
	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?,T,?,?,?> petriNet, 
														   PNTraverser pnTraverser, 
														   int passes, 
														   EntryTransformerManager entryFilterManager) 
														   throws ParameterException {
		this(petriNet, passes, entryFilterManager);
		setPNTraverser(pnTraverser);
	}
	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?,T,?,?,?> petriNet, 
														   PNTraverser pnTraverser, 
														   int passes, 
														   TraceTransformerManager traceFilterManager, 
														   EntryTransformerManager entryFilterManager) 
														   throws ParameterException {
		this(petriNet, passes, traceFilterManager, entryFilterManager);
		setPNTraverser(pnTraverser);
	}
	
	
	//------- Getters and Setters ----------------------------------------------------------------------
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public AbstractPetriNet<?,?,?,?,?> getPetriNet() {
		return petriNet;
	}
	
	public void setPetriNet(AbstractPetriNet<?,?,?,?,?> petriNet) throws ParameterException{
		Validate.notNull(petriNet);
		this.petriNet = petriNet;
	}

	public TraceTransformerManager getTraceFilterManager() {
		return traceFilterManager;
	}
	
	public void setTraceFilterManager(TraceTransformerManager traceFilterManager) throws ParameterException {
		Validate.notNull(traceFilterManager);
		this.traceFilterManager = traceFilterManager;
	}
	
	public EntryTransformerManager getEntryFilterManager() {
		return entryFilterManager;
	}
	
	public void setEntryFilterManager(EntryTransformerManager entryFilterManager) throws ParameterException {
		Validate.notNull(entryFilterManager);
		this.entryFilterManager = entryFilterManager;
	}
	
	public void setPNTraverser(PNTraverser<?> pnTraverser) throws ParameterException{
		Validate.notNull(pnTraverser);
		this.pnTraverser = pnTraverser;
	}
	
	public PNTraverser<?> getPNTraverser(){
		return pnTraverser;
	}
	
	public void setPasses(int passes) throws ParameterException{
		Validate.notNegative(passes, "Negative number of passes.");
		this.passes = passes;
	}
	
	public boolean isDone(){
		return generatedTraces >= passes;
	}
	
	public Integer getPasses(){
		return passes;
	}
	
	public int getGeneratedTraces(){
		return generatedTraces;
	}

	@Override
	public void traceCompleted(int caseNumber) {
		generatedTraces++;
	}

	public void takeoverValues(SimulationRun other) throws Exception{
		setPasses(other.getPasses());
		setPetriNet(other.getPetriNet());
		setPNTraverser(other.getPNTraverser());
		entryFilterManager.clear();
		for(AbstractEntryFilter entryFilter: other.getEntryFilterManager().getEntryTransformers()){
			entryFilterManager.addTransformer(entryFilter);
		}
		traceFilterManager.clear();
		for(AbstractTraceFilter traceFilter: other.getTraceFilterManager().getTraceTransformers()){
			traceFilterManager.addFilter(traceFilter);
		}
	}
	
	@Override
	public SimulationRun clone(){
		SimulationRun result = null;
		
		try {
			EntryTransformerManager entryFilterManager = new EntryTransformerManager();
			for(AbstractEntryFilter entryFilter: getEntryFilterManager().getEntryTransformers()){
				entryFilterManager.addTransformer(entryFilter);
			}
			
			TraceTransformerManager traceFilterManager = new TraceTransformerManager();
			for(AbstractTraceFilter traceFilter: getTraceFilterManager().getTraceTransformers()){
				traceFilterManager.addFilter(traceFilter);
			}
			
			result = new SimulationRun(getPetriNet(), getPNTraverser(), getPasses(), traceFilterManager, entryFilterManager);
			result.setName(getName());
		} catch(Exception e){
			return result;
		}
		return result;
	}
	
	public void reset(){
		generatedTraces = 0;
		traceFilterManager.reset();
	}
	
	@Override
	public String toString(){
		return String.format(toStringFormat, name, petriNet.getName(), passes, getFiltersString());
	}
	
	private String getFiltersString(){
		if(getTraceFilterManager().isEmpty()){
			return "";
		}
		StringBuilder builder = new StringBuilder();
		builder.append('\n');
		for(AbstractTraceFilter filter: getTraceFilterManager().getTraceTransformers()){
			builder.append(String.format(filterformat, filter.toString()));
		}
		return builder.toString();
	}
	
}