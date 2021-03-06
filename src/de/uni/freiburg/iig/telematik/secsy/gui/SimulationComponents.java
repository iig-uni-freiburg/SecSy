package de.uni.freiburg.iig.telematik.secsy.gui;

import de.invation.code.toval.graphic.dialog.MessageDialog;
import de.invation.code.toval.misc.wd.AbstractProjectComponents;
import de.invation.code.toval.misc.wd.ProjectComponentException;
import de.uni.freiburg.iig.telematik.secsy.gui.properties.SecSyProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.CaseDataContainerContainer;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.context.SynthesisContextContainer;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.CaseTimeGeneratorContainer;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.SimulationContainer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TransformerComponents;
import de.uni.freiburg.iig.telematik.sepia.graphic.container.GraphicalPTNetContainer;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.ACModelContainer;

public class SimulationComponents extends AbstractProjectComponents {

    private static SimulationComponents instance = null;
    
    private ACModelContainer containerACModels;
    private SynthesisContextContainer containerSynthesisContexts;
    private GraphicalPTNetContainer containerPTNets;
    private CaseDataContainerContainer containerCaseDataContainers;
    private CaseTimeGeneratorContainer containerTimeGenerators;
    private SimulationContainer containerSimulations;

    public SimulationComponents() throws ProjectComponentException {
        super(MessageDialog.getInstance());
    }

    public static SimulationComponents getInstance() throws ProjectComponentException {
        if (instance == null) {
            instance = new SimulationComponents();
        }
        return instance;
    }
    
    public SynthesisContextContainer getContainerSynthesisContexts(){
        return containerSynthesisContexts;
    }
    
    public ACModelContainer getContainerACModels(){
        return containerACModels;
    }
    
    public GraphicalPTNetContainer getContainerPTNets(){
        return containerPTNets;
    }
    
    public CaseDataContainerContainer getContainerCaseDataContainers(){
        return containerCaseDataContainers;
    }
    
    public CaseTimeGeneratorContainer getContainerTimeGenerators(){
        return containerTimeGenerators;
    }
    
    public SimulationContainer getContainerSimulations(){
        return containerSimulations;
    }

    @Override
    protected void addComponentContainers() throws ProjectComponentException {
        try{
            containerCaseDataContainers = new CaseDataContainerContainer(SecSyProperties.getInstance().getPathForDataContainers(), MessageDialog.getInstance());
            addComponentContainer(containerCaseDataContainers);
            containerSynthesisContexts = new SynthesisContextContainer(SecSyProperties.getInstance().getPathForContexts(), MessageDialog.getInstance());
            containerSynthesisContexts.setIgnoreIncompatibleFiles(true);
            addComponentContainer(containerSynthesisContexts);
            containerACModels = new ACModelContainer(SecSyProperties.getInstance().getPathForACModels(), getContainerSynthesisContexts(), MessageDialog.getInstance());
            addComponentContainer(containerACModels);
            containerSynthesisContexts.linkACModels(getContainerACModels(), true);
            containerPTNets = new GraphicalPTNetContainer(SecSyProperties.getInstance().getPathForPetriNets(), MessageDialog.getInstance());
            containerPTNets.setIgnoreIncompatibleFiles(true);
            addComponentContainer(containerPTNets);
            containerTimeGenerators = new CaseTimeGeneratorContainer(SecSyProperties.getInstance().getPathForTimeGenerators(), MessageDialog.getInstance());
            addComponentContainer(containerTimeGenerators);
            TransformerComponents.getInstance().loadComponents();
            containerSimulations = new SimulationContainer(SecSyProperties.getInstance().getPathForSimulations(), SecSyProperties.getInstance(), getContainerSynthesisContexts(), getContainerCaseDataContainers(), getContainerTimeGenerators(), getContainerPTNets(), MessageDialog.getInstance());
            addComponentContainer(containerSimulations);
        } catch(Exception e){
            throw new ProjectComponentException("Exception while creating component containers", e);
        }
    }
    
    public static void main(String[] args) throws Exception{
        SimulationComponents.getInstance();
    }
}
