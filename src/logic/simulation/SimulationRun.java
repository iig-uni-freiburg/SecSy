package logic.simulation;

import logic.filtering.EntryFilterManager;
import logic.filtering.TraceFilterManager;
import logic.filtering.filter.entry.AbstractEntryFilter;
import logic.filtering.filter.trace.AbstractTraceFilter;
import logic.generator.TraceCompletionListener;
import logic.simulation.properties.SimulationRunProperties;
import petrinet.AbstractPetriNet;
import petrinet.AbstractTransition;
import traversal.PNTraverser;
import traversal.RandomPNTraverser;
import validate.ParameterException;
import validate.Validate;

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
	protected TraceFilterManager traceFilterManager = new TraceFilterManager();
	protected EntryFilterManager entryFilterManager = new EntryFilterManager();
	
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
														   TraceFilterManager traceFilterManager) 
														   throws ParameterException {
		this(petriNet, passes, traverser);
		setTraceFilterManager(traceFilterManager);
	}
	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
			   												int passes,
			   												TraceFilterManager traceFilterManager) 
			   											    throws ParameterException {
		this(petriNet, passes, new RandomPNTraverser<T>(petriNet), traceFilterManager);
	}

	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
			   												int passes,
			   												PNTraverser traverser,
			   												EntryFilterManager entryFilterManager) 
			   											    throws ParameterException {
		this(petriNet, passes, traverser);
		setEntryFilterManager(entryFilterManager);
	}

	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
														   int passes,
														   EntryFilterManager entryFilterManager) 
														   throws ParameterException {
		this(petriNet, passes, new RandomPNTraverser<T>(petriNet), entryFilterManager);
	}

	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
														   int passes,
														   TraceFilterManager traceFilterManager,
														   EntryFilterManager entryFilterManager) 
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
														   TraceFilterManager traceFilterManager) 
														   throws ParameterException {
		this(petriNet, passes, traceFilterManager);
		setPNTraverser(pnTraverser);
	}
	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?,T,?,?,?> petriNet, 
														   PNTraverser pnTraverser, 
														   int passes, 
														   EntryFilterManager entryFilterManager) 
														   throws ParameterException {
		this(petriNet, passes, entryFilterManager);
		setPNTraverser(pnTraverser);
	}
	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?,T,?,?,?> petriNet, 
														   PNTraverser pnTraverser, 
														   int passes, 
														   TraceFilterManager traceFilterManager, 
														   EntryFilterManager entryFilterManager) 
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

	public TraceFilterManager getTraceFilterManager() {
		return traceFilterManager;
	}
	
	public void setTraceFilterManager(TraceFilterManager traceFilterManager) throws ParameterException {
		Validate.notNull(traceFilterManager);
		this.traceFilterManager = traceFilterManager;
	}
	
	public EntryFilterManager getEntryFilterManager() {
		return entryFilterManager;
	}
	
	public void setEntryFilterManager(EntryFilterManager entryFilterManager) throws ParameterException {
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
		for(AbstractEntryFilter entryFilter: other.getEntryFilterManager().getEntryFilters()){
			entryFilterManager.addFilter(entryFilter);
		}
		traceFilterManager.clear();
		for(AbstractTraceFilter traceFilter: other.getTraceFilterManager().getTraceFilters()){
			traceFilterManager.addFilter(traceFilter);
		}
	}
	
	@Override
	public SimulationRun clone(){
		SimulationRun result = null;
		
		try {
			EntryFilterManager entryFilterManager = new EntryFilterManager();
			for(AbstractEntryFilter entryFilter: getEntryFilterManager().getEntryFilters()){
				entryFilterManager.addFilter(entryFilter);
			}
			
			TraceFilterManager traceFilterManager = new TraceFilterManager();
			for(AbstractTraceFilter traceFilter: getTraceFilterManager().getTraceFilters()){
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
		for(AbstractTraceFilter filter: getTraceFilterManager().getTraceFilters()){
			builder.append(String.format(filterformat, filter.toString()));
		}
		return builder.toString();
	}
	
}