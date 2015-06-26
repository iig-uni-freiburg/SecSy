package de.uni.freiburg.iig.telematik.secsy.gui.properties;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import de.invation.code.toval.misc.wd.AbstractProjectComponents;
import de.invation.code.toval.misc.wd.AbstractWorkingDirectoryProperties;
import de.invation.code.toval.properties.PropertyException;
import de.uni.freiburg.iig.telematik.secsy.gui.SimulationComponents;

public class SecSyProperties extends AbstractWorkingDirectoryProperties<SecSyProperty> {

    public static final String SIMULATION_DIRECTORY_DESCRIPTOR = "Simulation Directory";
    public static final String DEFAULT_SIMULATION_DIRECTORY_NAME = "SimulationDirectory";
    public static final String SECSY_PROPERTY_FILE_NAME = "SecSyProperties";

    protected static final String pathSimulations = "simulations/";
    protected static final String pathTimeGenerators = "time_generators/";
    protected static final String pathContexts = "contexts/";
    protected static final String pathDataContainers = "data_containers/";
    protected static final String pathACModels = "ac_models/";
    protected static final String pathPetriNets = "nets/";
    protected static final String pathTransformers = "transformers/";
    protected static final String pathCustomTransformers = pathTransformers + "customTransformers/";
    protected static final String pathCustomTransformerTypes = pathCustomTransformers + "types/";

    protected static final Set<String> validationSubDirectories = new HashSet<>();
    static {
        validationSubDirectories.add(pathSimulations);
        validationSubDirectories.add(pathTimeGenerators);
        validationSubDirectories.add(pathContexts);
        validationSubDirectories.add(pathDataContainers);
        validationSubDirectories.add(pathACModels);
        validationSubDirectories.add(pathPetriNets);
        validationSubDirectories.add(pathTransformers);
    }
    
    private static SecSyProperties instance = null;

    public SecSyProperties() throws IOException {
        super();
    }

    @Override
    public String getDefaultWorkingDirectoryName() {
        return DEFAULT_SIMULATION_DIRECTORY_NAME;
    }

    @Override
    public String getPropertyFileName() {
        return SECSY_PROPERTY_FILE_NAME;
    }

    @Override
    public String getWorkingDirectoryDescriptor() {
        return SIMULATION_DIRECTORY_DESCRIPTOR;
    }
    
    
    /**
     *
     * @return @throws IOException if there is no property file and a new one
     * cannot be created.
     */
    public static SecSyProperties getInstance() throws IOException {
        if (instance == null) {
            instance = new SecSyProperties();
        }
        return instance;
    }
    
    //-- Simulation component paths (not stored in property file)
    
    public String getPathForSimulations() throws PropertyException {
        return getWorkingDirectory().concat(pathSimulations);
    }

    public String getPathForTimeGenerators() throws PropertyException {
        return getWorkingDirectory().concat(pathTimeGenerators);
    }

    public String getPathForContexts() throws PropertyException {
        return getWorkingDirectory().concat(pathContexts);
    }

    public String getPathForDataContainers() throws PropertyException {
        return getWorkingDirectory().concat(pathDataContainers);
    }

    public String getPathForACModels() throws PropertyException {
        return getWorkingDirectory().concat(pathACModels);
    }

    public String getPathForPetriNets() throws PropertyException {
        return getWorkingDirectory().concat(pathPetriNets);
    }

    public String getPathForTransformers() throws PropertyException {
        return getWorkingDirectory().concat(pathTransformers);
    }

    public String getPathForCustomTransformers() throws PropertyException {
        return getWorkingDirectory().concat(pathCustomTransformers);
    }

    public String getPathForCustomTransformerTypes() throws PropertyException {
        return getWorkingDirectory().concat(pathCustomTransformerTypes);
    }

    //------- Default Properties -----------------------------------------------------------
    @Override
    protected Properties getDefaultProperties() {
        Properties defaultProperties = new Properties();

//		defaultProperties.setProperty(SecSyProperty.SIMULATION_DIRECTORY.toString(), defaultSimulationDirectory);
        return defaultProperties;
    }

    @Override
    protected AbstractProjectComponents getProjectComponents() throws Exception{
        return SimulationComponents.getInstance();
    }

    @Override
    protected Set<String> getSubDirectoriesForValidation() {
        return validationSubDirectories;
    }

}
