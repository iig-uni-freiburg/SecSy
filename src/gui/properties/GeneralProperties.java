package gui.properties;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import de.invation.code.toval.misc.ArrayUtils;
import de.invation.code.toval.misc.StringUtils;
import de.invation.code.toval.properties.AbstractProperties;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.invation.code.toval.validate.ParameterException.ErrorCode;


public class GeneralProperties extends AbstractProperties{
	
	protected static final String defaultSimulationDirectory = ".";
	public static final String defaultSimulationDirectoryName = "SimulationDirectory";
	
	protected static final String pathSimulations = "simulations/";
	protected static final String pathTimeGenerators = "time_generators/";
	protected static final String pathContexts = "contexts/";
	protected static final String pathDataContainers = "data_containers/";
	protected static final String pathACModels = "ac_models/";
	protected static final String pathPetriNets = "nets/";
	protected static final String pathFilters = "filters/";
	
	protected static final String propertyFileName = "GeneralProperties/";
	
	private static GeneralProperties instance = null;
	
	public GeneralProperties() throws IOException {
		try {
			load(propertyFileName);
		} catch (IOException e) {
			// Create new property file.
			loadDefaultProperties();
			store();
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException if there is no property file and a new one cannot be created.
	 */
	public static GeneralProperties getInstance() throws IOException {
		if(instance == null){
			instance = new GeneralProperties();
		}
		return instance;
	}
	
	//------- Property setting -------------------------------------------------------------
	
	private void setProperty(GeneralProperty property, Object value){
		props.setProperty(property.toString(), value.toString());
	}
	
	private String getProperty(GeneralProperty property){
		return props.getProperty(property.toString());
	}
	
	@SuppressWarnings("unused")
	private void removeProperty(GeneralProperty property){
		props.remove(property.toString());
	}
	
	//-- Simulation Directory
	
	public void setSimulationDirectory(String directory) throws ParameterException{
		validateSimulationDirectory(directory, false);
		setProperty(GeneralProperty.SIMULATION_DIRECTORY, directory);
		//Check, if the simulation directory is empty
		File dir = new File(directory + pathSimulations);
		if(!dir.exists()){
			dir.mkdir();
		}
	}
	
	public String getSimulationDirectory() throws PropertyException, ParameterException {
		String propertyValue = getProperty(GeneralProperty.SIMULATION_DIRECTORY);
		if(propertyValue == null)
			throw new PropertyException(GeneralProperty.SIMULATION_DIRECTORY, propertyValue);
		validatePath(propertyValue);
		return propertyValue;
	}
	
	public void removeSimulationDirectory(){
		removeProperty(GeneralProperty.SIMULATION_DIRECTORY);
	}
	
	//-- Known SimulationDirectory
	
//	public void addNewKnownSimulationDirectory(String simulationDirectory) throws ParameterException{
//		addKnownSimulationDirectory(simulationDirectory, true);
//	}
//	
//	public void addExistingKnownSimulationDirectory(String simulationDirectory) throws ParameterException{
//		addKnownSimulationDirectory(simulationDirectory, false);
//	}
	
	public void addKnownSimulationDirectory(String simulationDirectory, boolean createSubdirectories) throws ParameterException{
		validateSimulationDirectory(simulationDirectory, createSubdirectories);
		Set<String> currentDirectories = getKnownSimulationDirectories();
		currentDirectories.add(simulationDirectory);
		setProperty(GeneralProperty.KNOWN_SIMULATION_DIRECTORIES, ArrayUtils.toString(prepareSimulationDirectories(currentDirectories)));
	}
	
	public void removeKnownSimulationDirectory(String simulationDirectory) throws ParameterException{
		validateStringValue(simulationDirectory);
		Set<String> currentDirectories = getKnownSimulationDirectories();
		currentDirectories.remove(simulationDirectory);
		setProperty(GeneralProperty.KNOWN_SIMULATION_DIRECTORIES, ArrayUtils.toString(prepareSimulationDirectories(currentDirectories)));
	}
	
	private String[] prepareSimulationDirectories(Set<String> directories){
		String[] result = new String[directories.size()];
		int count = 0;
		for(String directory: directories)
			result[count++] = "'"+directory+"'";
		return result;
	}
	
	public Set<String> getKnownSimulationDirectories(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(GeneralProperty.KNOWN_SIMULATION_DIRECTORIES);
		if(propertyValue == null)
			return result;
		StringTokenizer directoryTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(directoryTokens.hasMoreTokens()){
			String nextToken = directoryTokens.nextToken();
			result.add(nextToken.substring(1, nextToken.length()-1));
		}
		return result;
	}
	
	//-- Simulation component paths (not stored in property file)
	
	public String getPathForSimulations() throws PropertyException, ParameterException{
		return getSimulationDirectory().concat(pathSimulations);
	}
	
	public String getPathForTimeGenerators() throws PropertyException, ParameterException{
		return getSimulationDirectory().concat(pathTimeGenerators);
	}
	
	public String getPathForContexts() throws PropertyException, ParameterException {
		return getSimulationDirectory().concat(pathContexts);
	}
	
	public String getPathForDataContainers() throws PropertyException, ParameterException{
		return getSimulationDirectory().concat(pathDataContainers);
	}
	
	public String getPathForACModels() throws PropertyException, ParameterException{
		return getSimulationDirectory().concat(pathACModels);
	}
	
	public String getPathForPetriNets() throws PropertyException, ParameterException{
		return getSimulationDirectory().concat(pathPetriNets);
	}
	
	public String getPathForFilters() throws PropertyException, ParameterException{
		return getSimulationDirectory().concat(pathFilters);
	}
	
	//------- Validation -------------------------------------------------------------------
	
	public static void validateStringValue(String value) throws ParameterException{
		Validate.notNull(value);
		Validate.notEmpty(value);
	}
	
	public static void validatePath(String logPath) throws ParameterException {
		Validate.notNull(logPath);
		File cPath = new File(logPath);
		if(!cPath.isDirectory())
			throw new ParameterException(ErrorCode.INCOMPATIBILITY, logPath + " is not a valid path!");
	}
	
	public static void validateSimulationDirectory(String directory, boolean createSubdirectories) throws ParameterException{
		validatePath(directory);
		checkSubDirectory(directory, pathSimulations, createSubdirectories);
		checkSubDirectory(directory, pathTimeGenerators, createSubdirectories);
		checkSubDirectory(directory, pathContexts, createSubdirectories);
		checkSubDirectory(directory, pathDataContainers, createSubdirectories);
		checkSubDirectory(directory, pathACModels, createSubdirectories);
		checkSubDirectory(directory, pathPetriNets, createSubdirectories);
		checkSubDirectory(directory, pathFilters, createSubdirectories);
	}
	
	private static void checkSubDirectory(String simulationDirectory, String subDirectoryName, boolean ensureSubdirectory) throws ParameterException{
		File dir = new File(simulationDirectory + subDirectoryName);
		if(!dir.exists()){
			if(!ensureSubdirectory)
				throw new ParameterException(ErrorCode.INCOMPATIBILITY, "Corrupt structure of simulation directory:\n"+dir.getAbsolutePath());
			dir.mkdir();
		}
	}
	
	//------- Default Properties -----------------------------------------------------------
	
	@Override
	protected Properties getDefaultProperties(){
		Properties defaultProperties = new Properties();
		
//		defaultProperties.setProperty(GeneralProperty.SIMULATION_DIRECTORY.toString(), defaultSimulationDirectory);
		
		return defaultProperties;
	}
	
	//--------------------------------------------------------------------------------------
	
	public void store() throws IOException{
		store(propertyFileName);
	}

}
