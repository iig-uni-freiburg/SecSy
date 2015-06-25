/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.secsy.logic.simulation;

import de.uni.freiburg.iig.telematik.secsy.logic.transformation.EntryTransformerManager;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerManager;
import de.uni.freiburg.iig.telematik.sepia.petrinet.abstr.AbstractPetriNet;
import de.uni.freiburg.iig.telematik.sepia.traversal.PNTraverser;

/**
 *
 * @author stocker
 */
public class SimulationRunGenerator {
    
    private AbstractPetriNet petriNet = null;
    private PNTraverser pnTraverser = null;
    private TraceTransformerManager traceTransformerManager = null;
    private EntryTransformerManager entryTransformerManager = null;	
    private Integer passes = null;

    /**
     * @return the petriNet
     */
    public AbstractPetriNet getPetriNet() {
        return petriNet;
    }

    /**
     * @param petriNet the petriNet to set
     */
    public void setPetriNet(AbstractPetriNet petriNet) {
        this.petriNet = petriNet;
    }

    /**
     * @return the pnTraverser
     */
    public PNTraverser getPnTraverser() {
        return pnTraverser;
    }

    /**
     * @param pnTraverser the pnTraverser to set
     */
    public void setPnTraverser(PNTraverser pnTraverser) {
        this.pnTraverser = pnTraverser;
    }

    /**
     * @return the traceTransformerManager
     */
    public TraceTransformerManager getTraceTransformerManager() {
        return traceTransformerManager;
    }

    /**
     * @param traceTransformerManager the traceTransformerManager to set
     */
    public void setTraceTransformerManager(TraceTransformerManager traceTransformerManager) {
        this.traceTransformerManager = traceTransformerManager;
    }

    /**
     * @return the entryTransformerManager
     */
    public EntryTransformerManager getEntryTransformerManager() {
        return entryTransformerManager;
    }

    /**
     * @param entryTransformerManager the entryTransformerManager to set
     */
    public void setEntryTransformerManager(EntryTransformerManager entryTransformerManager) {
        this.entryTransformerManager = entryTransformerManager;
    }

    /**
     * @return the passes
     */
    public Integer getPasses() {
        return passes;
    }

    /**
     * @param passes the passes to set
     */
    public void setPasses(Integer passes) {
        this.passes = passes;
    }
    
    
}
