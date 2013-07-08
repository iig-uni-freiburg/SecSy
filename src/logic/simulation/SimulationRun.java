package logic.simulation;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import logic.generator.TraceCompletionListener;
import logic.simulation.properties.SimulationRunProperties;
import logic.transformation.EntryTransformerManager;
import logic.transformation.TraceTransformerManager;
import logic.transformation.transformer.entry.AbstractEntryTransformer;
import logic.transformation.transformer.trace.AbstractTraceTransformer;
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
	private static final String transformerformat = "--- %s\n";
	
	protected AbstractPetriNet<?,?,?,?,?> petriNet = null;
	protected PNTraverser<?> pnTraverser = null;
	protected TraceTransformerManager traceTransformerManager = new TraceTransformerManager();
	protected EntryTransformerManager entryTransformerManager = new EntryTransformerManager();
	
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
														   TraceTransformerManager traceTransformerManager) 
														   throws ParameterException {
		this(petriNet, passes, traverser);
		setTraceTransformerManager(traceTransformerManager);
	}
	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
			   												int passes,
			   												TraceTransformerManager traceTransformerManager) 
			   											    throws ParameterException {
		this(petriNet, passes, new RandomPNTraverser<T>(petriNet), traceTransformerManager);
	}

	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
			   												int passes,
			   												PNTraverser traverser,
			   												EntryTransformerManager entryTransformerManager) 
			   											    throws ParameterException {
		this(petriNet, passes, traverser);
		setEntryTransformerManager(entryTransformerManager);
	}

	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
														   int passes,
														   EntryTransformerManager entryTransformerManager) 
														   throws ParameterException {
		this(petriNet, passes, new RandomPNTraverser<T>(petriNet), entryTransformerManager);
	}

	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
														   int passes,
														   TraceTransformerManager traceTransformerManager,
														   EntryTransformerManager entryTransformerManager) 
														   throws ParameterException {
		this(petriNet, passes);
		setEntryTransformerManager(entryTransformerManager);
		setTraceTransformerManager(traceTransformerManager);
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
														   TraceTransformerManager traceTransformerManager) 
														   throws ParameterException {
		this(petriNet, passes, traceTransformerManager);
		setPNTraverser(pnTraverser);
	}
	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?,T,?,?,?> petriNet, 
														   PNTraverser pnTraverser, 
														   int passes, 
														   EntryTransformerManager entryTransformerManager) 
														   throws ParameterException {
		this(petriNet, passes, entryTransformerManager);
		setPNTraverser(pnTraverser);
	}
	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?,T,?,?,?> petriNet, 
														   PNTraverser pnTraverser, 
														   int passes, 
														   TraceTransformerManager traceTransformerManager, 
														   EntryTransformerManager entryTransformerManager) 
														   throws ParameterException {
		this(petriNet, passes, traceTransformerManager, entryTransformerManager);
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

	public TraceTransformerManager getTraceTransformerManager() {
		return traceTransformerManager;
	}
	
	public void setTraceTransformerManager(TraceTransformerManager traceTransformerManager) throws ParameterException {
		Validate.notNull(traceTransformerManager);
		this.traceTransformerManager = traceTransformerManager;
	}
	
	public EntryTransformerManager getEntryTransformerManager() {
		return entryTransformerManager;
	}
	
	public void setEntryTransformerManager(EntryTransformerManager entryTransformerManager) throws ParameterException {
		Validate.notNull(entryTransformerManager);
		this.entryTransformerManager = entryTransformerManager;
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
		entryTransformerManager.clear();
		for(AbstractEntryTransformer entryTransformer: other.getEntryTransformerManager().getEntryTransformers()){
			entryTransformerManager.addTransformer(entryTransformer);
		}
		traceTransformerManager.clear();
		for(AbstractTraceTransformer traceTransformer: other.getTraceTransformerManager().getTraceTransformers()){
			traceTransformerManager.addTransformer(traceTransformer);
		}
	}
	
	@Override
	public SimulationRun clone(){
		SimulationRun result = null;
		
		try {
			EntryTransformerManager entryTransformerManager = new EntryTransformerManager();
			for(AbstractEntryTransformer entryTransformer: getEntryTransformerManager().getEntryTransformers()){
				entryTransformerManager.addTransformer(entryTransformer);
			}
			
			TraceTransformerManager traceTransformerManager = new TraceTransformerManager();
			for(AbstractTraceTransformer traceTransformer: getTraceTransformerManager().getTraceTransformers()){
				traceTransformerManager.addTransformer(traceTransformer);
			}
			
			result = new SimulationRun(getPetriNet(), getPNTraverser(), getPasses(), traceTransformerManager, entryTransformerManager);
			result.setName(getName());
		} catch(Exception e){
			return result;
		}
		return result;
	}
	
	public void reset(){
		generatedTraces = 0;
		traceTransformerManager.reset();
	}
	
	@Override
	public String toString(){
		return String.format(toStringFormat, name, petriNet.getName(), passes, getTransformersString());
	}
	
	private String getTransformersString(){
		if(getTraceTransformerManager().isEmpty()){
			return "";
		}
		StringBuilder builder = new StringBuilder();
		builder.append('\n');
		for(AbstractTraceTransformer transformer: getTraceTransformerManager().getTraceTransformers()){
			builder.append(String.format(transformerformat, transformer.toString()));
		}
		return builder.toString();
	}
	
}