package de.uni.freiburg.iig.telematik.secsy.logic.simulation;

import de.invation.code.toval.graphic.dialog.DialogObject;
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
import java.util.HashSet;
import java.util.Set;

/**
 * Simple class for simulation runs.
 * 
 * @author Thomas Stocker
 */
public class SimulationRun implements TraceCompletionListener,DialogObject<SimulationRun>{
	
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

        public SimulationRun(){}
        
	public SimulationRun(SimulationRunGenerator generator){
            Validate.notNull(generator.getPetriNet());
            Validate.notNull(generator.getPasses());
            if(generator.getPasses() != null)
		setPasses(generator.getPasses());
            if(generator.getEntryTransformerManager() != null)
                setEntryTransformerManager(generator.getEntryTransformerManager());
            if(generator.getTraceTransformerManager() != null)
                setTraceTransformerManager(generator.getTraceTransformerManager());
            if(generator.getPetriNet() != null)
                setPetriNet(generator.getPetriNet());
            if(generator.getPnTraverser() != null){
                setPNTraverser(generator.getPnTraverser());
            } else {
                setPNTraverser(new RandomPNTraverser(getPetriNet()));
            }
	}
	
	//------- Getters and Setters ----------------------------------------------------------------------
	
	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}
	
	public final AbstractPetriNet getPetriNet() {
		return petriNet;
	}
	
	public final void setPetriNet(AbstractPetriNet petriNet){
		Validate.notNull(petriNet);
		this.petriNet = petriNet;
	}

	public final TraceTransformerManager getTraceTransformerManager() {
		return traceTransformerManager;
	}
	
	public final void setTraceTransformerManager(TraceTransformerManager traceTransformerManager){
		Validate.notNull(traceTransformerManager);
		this.traceTransformerManager = traceTransformerManager;
	}
	
	public final EntryTransformerManager getEntryTransformerManager() {
		return entryTransformerManager;
	}
	
	public final void setEntryTransformerManager(EntryTransformerManager entryTransformerManager){
		Validate.notNull(entryTransformerManager);
		this.entryTransformerManager = entryTransformerManager;
	}
	
	public final void setPNTraverser(PNTraverser pnTraverser){
		Validate.notNull(pnTraverser);
		this.pnTraverser = pnTraverser;
	}
	
	public final PNTraverser getPNTraverser(){
		return pnTraverser;
	}
	
	public final void setPasses(int passes){
		Validate.notNegative(passes, "Negative number of passes.");
		this.passes = passes;
	}
	
	public final boolean isDone(){
		return generatedTraces >= passes;
	}
	
	public final Integer getPasses(){
		return passes;
	}
	
	public final int getGeneratedTraces(){
		return generatedTraces;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final Set<String> getActivities(){
		Set<String> activities = new HashSet<>();
		AbstractPetriNet ptNet = getPetriNet();
		if(ptNet == null)
			throw new ParameterException("Cannot extract activities: Petri net not set.");
		else{
			activities.addAll(PNUtils.getLabelSetFromTransitions(ptNet.getTransitions(), false));
			return activities;
		}
	}

	@Override
	public final void traceCompleted(int caseNumber) {
		generatedTraces++;
	}

        @Override
	public final void takeoverValues(SimulationRun other) throws Exception{
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
    public SimulationRun clone() {
        SimulationRun result = null;

        try {
            EntryTransformerManager newEntryTransformerManager = new EntryTransformerManager();
            for (AbstractEntryTransformer entryTransformer : getEntryTransformerManager().getEntryTransformers()) {
                newEntryTransformerManager.addTransformer(entryTransformer);
            }

            TraceTransformerManager newTraceTransformerManager = new TraceTransformerManager();
            for (AbstractTraceTransformer traceTransformer : getTraceTransformerManager().getTraceTransformers()) {
                newTraceTransformerManager.addTransformer(traceTransformer);
            }
            SimulationRunGenerator generator = new SimulationRunGenerator();
            generator.setEntryTransformerManager(newEntryTransformerManager);
            generator.setTraceTransformerManager(newTraceTransformerManager);
            generator.setPasses(getPasses());
            generator.setPetriNet(getPetriNet());
            generator.setPnTraverser(getPNTraverser());
            result = new SimulationRun(generator);
            result.setName(getName());
        } catch (Exception e) {
            return null;
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