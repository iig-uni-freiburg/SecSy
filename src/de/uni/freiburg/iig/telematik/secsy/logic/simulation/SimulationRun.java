package de.uni.freiburg.iig.telematik.secsy.logic.simulation;

import java.util.HashSet;
import java.util.Set;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.TraceCompletionListener;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.SimulationRunProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.EntryTransformerManager;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerManager;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.entry.AbstractEntryTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.AbstractTraceTransformer;
import de.uni.freiburg.iig.telematik.sepia.petrinet.abstr.AbstractPetriNet;
import de.uni.freiburg.iig.telematik.sepia.petrinet.abstr.AbstractTransition;
import de.uni.freiburg.iig.telematik.sepia.traversal.PNTraverser;
import de.uni.freiburg.iig.telematik.sepia.traversal.RandomPNTraverser;
import de.uni.freiburg.iig.telematik.sepia.util.PNUtils;

/**
 * Simple class for simulation runs.
 * 
 * @author Thomas Stocker
 */
public class SimulationRun implements TraceCompletionListener{
	
	private static final String toStringFormat = "%s: %s, %s passes%s";
	private static final String transformerformat = "--- %s\n";
	
	protected AbstractPetriNet petriNet = null;
	protected PNTraverser pnTraverser = null;
	protected TraceTransformerManager traceTransformerManager = new TraceTransformerManager();
	protected EntryTransformerManager entryTransformerManager = new EntryTransformerManager();
	
	protected Integer passes = null;
	protected int generatedTraces = 0;
	
	private String name = SimulationRunProperties.defaultName;
	
	//------- Constructors ----------------------------------------------------------------------------------

	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
			   								   int passes,
			   								   PNTraverser<T> traverser){
		setPasses(passes);
		setPetriNet(petriNet);
		setPNTraverser(traverser);
	}
	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
														   int passes) {
		this(petriNet, passes, new RandomPNTraverser<T>(petriNet));
	}
	

	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
														   int passes,
														   PNTraverser<T> traverser,
														   TraceTransformerManager traceTransformerManager){
		this(petriNet, passes, traverser);
		setTraceTransformerManager(traceTransformerManager);
	}
	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
			   												int passes,
			   												TraceTransformerManager traceTransformerManager){
		this(petriNet, passes, new RandomPNTraverser<T>(petriNet), traceTransformerManager);
	}

	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
			   												int passes,
			   												PNTraverser<T> traverser,
			   												EntryTransformerManager entryTransformerManager){
		this(petriNet, passes, traverser);
		setEntryTransformerManager(entryTransformerManager);
	}

	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
														   int passes,
														   EntryTransformerManager entryTransformerManager){
		this(petriNet, passes, new RandomPNTraverser<T>(petriNet), entryTransformerManager);
	}

	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?, T, ?, ?, ?> petriNet,
														   int passes,
														   TraceTransformerManager traceTransformerManager,
														   EntryTransformerManager entryTransformerManager){
		this(petriNet, passes);
		setEntryTransformerManager(entryTransformerManager);
		setTraceTransformerManager(traceTransformerManager);
	}

	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?,T,?,?,?> petriNet, 
														   PNTraverser<T> pnTraverser, 
														   int passes){
		this(petriNet, passes);
		setPNTraverser(pnTraverser);
	}
	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?,T,?,?,?> petriNet, 
														   PNTraverser<T> pnTraverser, 
														   int passes, 
														   TraceTransformerManager traceTransformerManager){
		this(petriNet, passes, traceTransformerManager);
		setPNTraverser(pnTraverser);
	}
	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?,T,?,?,?> petriNet, 
														   PNTraverser<T> pnTraverser, 
														   int passes, 
														   EntryTransformerManager entryTransformerManager){
		this(petriNet, passes, entryTransformerManager);
		setPNTraverser(pnTraverser);
	}
	
	public <T extends AbstractTransition<?,?>> SimulationRun(AbstractPetriNet<?,T,?,?,?> petriNet, 
														   PNTraverser<T> pnTraverser, 
														   int passes, 
														   TraceTransformerManager traceTransformerManager, 
														   EntryTransformerManager entryTransformerManager){
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
	
	public AbstractPetriNet getPetriNet() {
		return petriNet;
	}
	
	public void setPetriNet(AbstractPetriNet petriNet){
		Validate.notNull(petriNet);
		this.petriNet = petriNet;
	}

	public TraceTransformerManager getTraceTransformerManager() {
		return traceTransformerManager;
	}
	
	public void setTraceTransformerManager(TraceTransformerManager traceTransformerManager){
		Validate.notNull(traceTransformerManager);
		this.traceTransformerManager = traceTransformerManager;
	}
	
	public EntryTransformerManager getEntryTransformerManager() {
		return entryTransformerManager;
	}
	
	public void setEntryTransformerManager(EntryTransformerManager entryTransformerManager){
		Validate.notNull(entryTransformerManager);
		this.entryTransformerManager = entryTransformerManager;
	}
	
	public void setPNTraverser(PNTraverser pnTraverser){
		Validate.notNull(pnTraverser);
		this.pnTraverser = pnTraverser;
	}
	
	public PNTraverser getPNTraverser(){
		return pnTraverser;
	}
	
	public void setPasses(int passes){
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Set<String> getActivities(){
		Set<String> activities = new HashSet<String>();
		AbstractPetriNet ptNet = getPetriNet();
		if(ptNet == null)
			throw new ParameterException("Cannot extract activities: Petri net not set.");
		else{
			activities.addAll(PNUtils.getLabelSetFromTransitions(ptNet.getTransitions(), false));
			return activities;
		}
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
	
	@SuppressWarnings("unchecked")
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