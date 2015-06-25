/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.secsy.logic.simulation;

import de.invation.code.toval.debug.SimpleDebugger;
import de.invation.code.toval.misc.wd.AbstractComponentContainer;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.gui.properties.SecSyProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.CaseDataContainer;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.CaseDataContainerContainer;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.DetailedLogEntryGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.LogEntryGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.TraceLogGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.context.SynthesisContext;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.context.SynthesisContextContainer;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.CaseTimeGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.CaseTimeGeneratorContainer;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.EntryGenerationType;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.SimulationProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.SimulationProperty;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.SimulationRunProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.SimulationRunProperty;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerManager;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TransformerComponents;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.AbstractTraceTransformer;
import de.uni.freiburg.iig.telematik.sepia.graphic.container.AbstractGraphicalPNContainer;
import de.uni.freiburg.iig.telematik.sepia.petrinet.abstr.AbstractPetriNet;
import de.uni.freiburg.iig.telematik.sepia.petrinet.pt.traverse.RandomPTTraverser;
import de.uni.freiburg.iig.telematik.sewol.format.AbstractLogFormat;
import de.uni.freiburg.iig.telematik.sewol.format.LogFormatFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author stocker
 */
public class SimulationContainer extends AbstractComponentContainer<Simulation> {

    public static final String SIMULATION_DESCRIPTOR = "Simulation";

    private SecSyProperties secsyProperties = null;
    private SynthesisContextContainer availableContexts = null;
    private CaseDataContainerContainer availableDataContainers = null;
    private CaseTimeGeneratorContainer availableTimeGenerators = null;
    private AbstractGraphicalPNContainer availablePetriNets = null;
    
    public SimulationContainer(String serializationPath,
            SecSyProperties secsyProperties,
            SynthesisContextContainer availableContexts,
            CaseDataContainerContainer availableDataContainers,
            CaseTimeGeneratorContainer availableTimeGenerators,
            AbstractGraphicalPNContainer availablePetriNets) {
        this(serializationPath, secsyProperties, availableContexts, availableDataContainers, availableTimeGenerators, availablePetriNets, null);
    }

    public SimulationContainer(String serializationPath,
            SecSyProperties secsyProperties,
            SynthesisContextContainer availableContexts,
            CaseDataContainerContainer availableDataContainers,
            CaseTimeGeneratorContainer availableTimeGenerators,
            AbstractGraphicalPNContainer availablePetriNets,
            SimpleDebugger debugger) {
        super(serializationPath, debugger);
        Validate.notNull(secsyProperties);
        this.secsyProperties = secsyProperties;
        Validate.notNull(availableContexts);
        this.availableContexts = availableContexts;
        Validate.notNull(availableDataContainers);
        this.availableDataContainers = availableDataContainers;
        Validate.notNull(availableTimeGenerators);
        this.availableTimeGenerators = availableTimeGenerators;
        Validate.notNull(availablePetriNets);
        this.availablePetriNets = availablePetriNets;
    }

    @Override
    public String getComponentDescriptor() {
        return SIMULATION_DESCRIPTOR;
    }

    @Override
    public Set<String> getAcceptedFileEndings() {
        return new HashSet<>(Arrays.asList(""));
    }

    @Override
    protected Simulation loadComponentFromFile(String file) throws Exception {
        SimulationProperties properties;
        try {
            properties = new SimulationProperties(file);
        } catch (IOException e) {
            throw new IOException("Cannot load properties file: " + file + ".");
        }

        AbstractLogFormat logFormat = LogFormatFactory.getFormat(properties.getLogFormatType());
        String fileName = properties.getFileName();

        //Log Generator
        TraceLogGenerator logGenerator = new TraceLogGenerator(logFormat, secsyProperties.getWorkingDirectory(), fileName);
        logGenerator.setEventHandling(properties.getEventHandling());

        EntryGenerationType generationType = properties.getEntryGenerationType();
        LogEntryGenerator entryGenerator = null;
        if (generationType.equals(EntryGenerationType.DETAILED)) {

            String contextName = null;
            try {
                contextName = properties.getContextName();
                Validate.notNull(contextName);
            } catch (Exception e) {
                throw new PropertyException(SimulationProperty.CONTEXT_NAME, null, "Cannot extract context name from properties", e);
            }
            if (!availableContexts.containsComponent(contextName)) {
                throw new PropertyException(SimulationProperty.CONTEXT_NAME, contextName, "No context with adequate name available.");
            }
            SynthesisContext context = availableContexts.getComponent(contextName);

            String dataContainerName = null;
            try {
                dataContainerName = properties.getDataContainerName();
                Validate.notNull(dataContainerName);
            } catch (Exception e) {
                throw new PropertyException(SimulationProperty.DATA_CONTAINER_NAME, null, "Cannot extract data container name from properties", e);
            }
            if (!availableDataContainers.containsComponent(contextName)) {
                throw new PropertyException(SimulationProperty.DATA_CONTAINER_NAME, dataContainerName, "No data container with adequate name available.");
            }
            CaseDataContainer dataContainer = availableDataContainers.getComponent(dataContainerName);

            entryGenerator = new DetailedLogEntryGenerator(context, dataContainer);
        } else {
            entryGenerator = new LogEntryGenerator();
        }

        String timeGeneratorName = null;
        try {
            timeGeneratorName = properties.getTimeGeneratorName();
            Validate.notNull(timeGeneratorName);
        } catch (Exception e) {
            throw new PropertyException(SimulationProperty.TIME_GENERATOR_NAME, null, "Cannot extract time generator name from properties", e);
        }
        if (!availableTimeGenerators.containsComponent(timeGeneratorName)) {
            throw new PropertyException(SimulationProperty.TIME_GENERATOR_NAME, timeGeneratorName, "No time generator with adequate name available.");
        }
        CaseTimeGenerator timeGenerator = availableTimeGenerators.getComponent(timeGeneratorName);

        Simulation result = new Simulation(logGenerator, entryGenerator, timeGenerator);

        result.addSimulationRuns(loadSimulationRuns(properties));
        result.setName(properties.getName());
        return result;
    }

    private Set<SimulationRun> loadSimulationRuns(SimulationProperties properties) throws Exception {
        Set<SimulationRun> simulationRuns = new HashSet<SimulationRun>();
        Set<SimulationRunProperties> runPropertiesSet = null;
        try {
            runPropertiesSet = properties.getSimulationRuns();
            Validate.notNull(runPropertiesSet);
        } catch (Exception e) {
            throw new PropertyException(SimulationProperty.SIMULATION_RUNS, null, "Cannot extract simulation run properties from properties", e);
        }

        for (SimulationRunProperties runProperties : runPropertiesSet) {
            String runName = null;
            try {
                runName = runProperties.getName();
                Validate.notNull(runName);
            } catch (Exception e) {
                throw new PropertyException(SimulationRunProperty.SIMULATION_RUN_NAME, null, "Cannot extract simulation run name from run properties", e);
            }

            String netName = null;
            try {
                netName = runProperties.getNetName();
                Validate.notNull(netName);
            } catch (Exception e) {
                throw new PropertyException(SimulationRunProperty.NET_NAME, null, "Cannot extract Petri net name from run properties", e);
            }
            if (!availablePetriNets.containsComponent(netName)) {
                throw new PropertyException(SimulationRunProperty.NET_NAME, netName, "No Petri net with adequate name available.");
            }
            AbstractPetriNet net = (AbstractPetriNet) availablePetriNets.getComponent(netName);

            TraceTransformerManager transformerManager = new TraceTransformerManager();
            Set<String> transformerNames = null;
            try {
                transformerNames = runProperties.getTransformerNames();
                Validate.notNull(transformerNames);
            } catch (Exception e) {
                throw new PropertyException(SimulationRunProperty.TRANSFORMERS, null, "Cannot extract transformer names from run properties", e);
            }
            for (String transformerName : transformerNames) {
                if (!TransformerComponents.getInstance().containsTransformer(transformerName)) {
                    throw new PropertyException(SimulationRunProperty.TRANSFORMERS, netName, "No transformer with adequate name available.");
                }
                AbstractTraceTransformer transformer = TransformerComponents.getInstance().getTransformer(transformerName);
                transformerManager.addTransformer(transformer);
            }

            Integer passes = null;
            try {
                passes = runProperties.getPasses();
                Validate.notNull(passes);
            } catch (Exception e) {
                throw new PropertyException(SimulationRunProperty.PASSES, null, "Cannot extract passes from run properties", e);
            }

            SimulationRunGenerator generator = new SimulationRunGenerator();
            generator.setPetriNet(net);
            generator.setPasses(runProperties.getPasses());
            generator.setPnTraverser(new RandomPTTraverser(net));
            generator.setTraceTransformerManager(transformerManager);
            SimulationRun newSimulationRun = new SimulationRun(generator);
            newSimulationRun.setName(runName);
            simulationRuns.add(newSimulationRun);
        }
        return simulationRuns;
    }

    @Override
    protected void serializeComponent(Simulation component, String serializationPath, String fileName) throws Exception {
        component.getProperties().store(serializationPath.concat(fileName));
    }

}
