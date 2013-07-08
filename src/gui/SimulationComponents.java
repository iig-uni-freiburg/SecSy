package gui;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import logic.generator.AttributeValueGenerator;
import logic.generator.CaseDataContainer;
import logic.generator.Context;
import logic.generator.DetailedLogEntryGenerator;
import logic.generator.LogEntryGenerator;
import logic.generator.LogGenerator;
import logic.generator.TraceLogGenerator;
import logic.generator.properties.CaseDataContainerProperties;
import logic.generator.properties.ContextProperties;
import logic.generator.time.CaseTimeGenerator;
import logic.generator.time.properties.TimeGeneratorFactory;
import logic.generator.time.properties.TimeProperties;
import logic.simulation.ConfigurationException;
import logic.simulation.Simulation;
import logic.simulation.SimulationRun;
import logic.simulation.properties.EntryGenerationType;
import logic.simulation.properties.SimulationProperties;
import logic.simulation.properties.SimulationProperty;
import logic.simulation.properties.SimulationRunProperties;
import logic.simulation.properties.SimulationRunProperty;
import logic.transformation.TraceTransformerManager;
import logic.transformation.transformer.AbstractTransformer;
import logic.transformation.transformer.TransformerFactory;
import logic.transformation.transformer.exception.MissingRequirementException;
import logic.transformation.transformer.trace.AbstractTraceTransformer;
import parser.PNMLParser;
import petrinet.pt.PTNet;
import petrinet.pt.RandomPTTraverser;
import de.invation.code.toval.constraint.AbstractConstraint;
import de.invation.code.toval.file.FileUtils;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.invation.code.toval.validate.ParameterException.ErrorCode;
import de.uni.freiburg.iig.telematik.jawl.logformat.LogFormat;
import de.uni.freiburg.iig.telematik.jawl.logformat.LogFormatFactory;
import de.uni.freiburg.iig.telematik.jawl.writer.PerspectiveException;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.ACLModel;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.ACModel;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.RBACModel;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.properties.ACLModelProperties;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.properties.ACModelProperties;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.properties.ACModelType;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.properties.RBACModelProperties;
import gui.properties.GeneralProperties;


public class SimulationComponents {
	
	private static SimulationComponents instance = null;
	
	private Map<String, ACModel> acModels = new HashMap<String, ACModel>();
	private Map<String, PTNet> petriNets = new HashMap<String, PTNet>();
	private Map<String, Context> contexts = new HashMap<String, Context>();
	private Map<String, CaseDataContainer> caseDataContainers = new HashMap<String, CaseDataContainer>();
	private Map<String, AbstractTraceTransformer> transformers = new HashMap<String, AbstractTraceTransformer>();
	private Map<String, CaseTimeGenerator> caseTimeGenerators = new HashMap<String, CaseTimeGenerator>();
	private Map<String, Simulation> simulations = new HashMap<String, Simulation>();
	
//	private Map<String, TimeProperties> caseTimeGeneratorProperties = new HashMap<String, TimeProperties>();
//	private Map<String, SimulationProperties> simulationProperties = new HashMap<String, SimulationProperties>();
	
	private SimulationComponents(){
		try {
			loadSimulationComponents();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Cannot access simulation directory:\n" + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
			
		}
	}
	
	public static SimulationComponents getInstance(){
		if(instance == null){
			instance = new SimulationComponents();
		}
		return instance;
	}
	
	public void reset() throws IOException, ParameterException, PropertyException {
		acModels.clear();
		petriNets.clear();
		contexts.clear();
		caseDataContainers.clear();
		transformers.clear();
		caseTimeGenerators.clear();
		simulations.clear();
		loadSimulationComponents();
	}
	
	//------- Load simulation components --------------------------------------------------------------------------------------
	
	private void loadSimulationComponents() throws ParameterException, IOException, PropertyException {
		MessageDialog.getInstance().addMessage("Loading simulation components.");
		
		//1. Load access control models
		//   -> Contexts require access control models, thus they have to be loaded first.
		MessageDialog.getInstance().addMessage("1. Searching for access control models:");
		List<String> acFiles = null;
		try {
			acFiles = FileUtils.getFileNamesInDirectory(GeneralProperties.getInstance().getPathForACModels(), true);
		} catch (IOException e1) {
			throw new IOException("Cannot access access control model directory.");
		}
		for(String acFile: acFiles){
			MessageDialog.getInstance().addMessage("Loading access control model: " + acFile + "...   ");
			try{
				addACModel(loadACModel(acFile), false);
				MessageDialog.getInstance().addMessage("Done.");
			} catch(Exception e){
				MessageDialog.getInstance().addMessage("Error: "+e.getMessage());
			}
		}
		MessageDialog.getInstance().newLine();
		
		//2. Load contexts
		MessageDialog.getInstance().addMessage("2. Searching for contexts:");
		List<String> contextFiles = null;
		try {
			contextFiles = FileUtils.getFileNamesInDirectory(GeneralProperties.getInstance().getPathForContexts(), true);
		} catch (IOException e1) {
			throw new IOException("Cannot access context directory.");
		}
		for(String contextFile: contextFiles){
			MessageDialog.getInstance().addMessage("Loading context: " + contextFile + "...   ");
			try{
				addContext(loadContext(contextFile), false);
				MessageDialog.getInstance().addMessage("Done.");
			} catch(Exception e){
				MessageDialog.getInstance().addMessage("Error: "+e.getMessage());
			}
		}
		MessageDialog.getInstance().newLine();
		
		//3. Load Petri nets
		MessageDialog.getInstance().addMessage("3. Searching for Petri nets:");
		List<String> netFiles = null;
		try {
			netFiles = FileUtils.getFileNamesInDirectory(GeneralProperties.getInstance().getPathForPetriNets(), true);
		} catch (IOException e1) {
			throw new IOException("Cannot access Petri net directory.");
		}
		for(String netFile: netFiles){
			MessageDialog.getInstance().addMessage("Loading Petri net: " + netFile + "...   ");
			try{
				addPetriNet(PNMLParser.parsePNML(netFile, true), false);
				MessageDialog.getInstance().addMessage("Done.");
			} catch(Exception e){
				MessageDialog.getInstance().addMessage("Error: "+e.getMessage());
			}
		}
		MessageDialog.getInstance().newLine();
		
		//4. Load time generator properties
		MessageDialog.getInstance().addMessage("4. Searching for time generators:");
		List<String> timePropertiesFiles = null;
		try {
			timePropertiesFiles = FileUtils.getFileNamesInDirectory(GeneralProperties.getInstance().getPathForTimeGenerators(), true);
		} catch (IOException e1) {
			throw new IOException("Cannot access time generator directory.");
		}
		for(String propertiesFile: timePropertiesFiles){
			MessageDialog.getInstance().addMessage("Loading time generator: " + propertiesFile + "...   ");
			try{
//				TimeProperties newTimeProperties = new TimeProperties();
//				newTimeProperties.load(propertiesFile);
//				addCaseTimeGeneratorProperties(newTimeProperties, false);
				addCaseTimeGenerator(TimeGeneratorFactory.createCaseTimeGenerator(new TimeProperties(propertiesFile)));
				MessageDialog.getInstance().addMessage("Done.");
			} catch(Exception e){
				MessageDialog.getInstance().addMessage("Error: "+e.getMessage());
			}
		}
		MessageDialog.getInstance().newLine();
		
		//5. Load data containers
		MessageDialog.getInstance().addMessage("5. Searching for data containers:");
		List<String> containerFiles = null;
		try {
			containerFiles = FileUtils.getFileNamesInDirectory(GeneralProperties.getInstance().getPathForDataContainers(), true);
		} catch (IOException e1) {
			throw new IOException("Cannot access data container directory.");
		}
		for(String containerFile: containerFiles){
			MessageDialog.getInstance().addMessage("Loading data container: " + containerFile + "...   ");
			try{
				addCaseDataContainer(loadDataContainer(containerFile), false);
				MessageDialog.getInstance().addMessage("Done.");
			} catch(Exception e){
				MessageDialog.getInstance().addMessage("Error: "+e.getMessage());
			}
		}
		MessageDialog.getInstance().newLine();
		
		//6. Load transformers
		MessageDialog.getInstance().addMessage("6. Searching for transformers:");
		List<String> transformerFiles = null;
		try {
			transformerFiles = FileUtils.getFileNamesInDirectory(GeneralProperties.getInstance().getPathForTransformers(), true);
		} catch (IOException e1) {
			throw new IOException("Cannot access transformer directory.");
		}
		for(String transformerFile: transformerFiles){
			MessageDialog.getInstance().addMessage("Loading transformer: " + transformerFile + "...   ");
			try{
				addTransformer((AbstractTraceTransformer) TransformerFactory.loadTransformer(transformerFile), false);
				MessageDialog.getInstance().addMessage("Done.");
			} catch(Exception e){
				MessageDialog.getInstance().addMessage("Error: "+e.getMessage());
			}
		}
		MessageDialog.getInstance().newLine();
		
		//7. Load Simulations
		MessageDialog.getInstance().addMessage("7. Searching for simulations:");
		List<String> simulationFiles= null;
		try {
			simulationFiles = FileUtils.getFileNamesInDirectory(GeneralProperties.getInstance().getPathForSimulations(), true);
		} catch (IOException e1) {
			throw new IOException("Cannot access simulation directory.");
		}
		for(String simulationFile: simulationFiles){
			MessageDialog.getInstance().addMessage("Loading simulation: " + simulationFile + "...   ");
			try{
//				SimulationProperties newSimulationProperties = new SimulationProperties();
//				newSimulationProperties.load(simulationFile);
//				addSimulationProperties(newSimulationProperties, false);
				addSimulation(loadSimulation(simulationFile), false);
				MessageDialog.getInstance().addMessage("Done.");
			} catch(Exception e){
				MessageDialog.getInstance().addMessage("Error: "+e.getMessage());
			}
		}
		MessageDialog.getInstance().newLine();
		
	}
	
	private ACModel loadACModel(String acFile) throws PropertyException, ParameterException, IOException {
		ACModelProperties testProperties = new ACModelProperties();
		try {
			testProperties.load(acFile);

			// Check ACModel type
			if (testProperties.getType().equals(ACModelType.ACL)) {
				ACLModelProperties aclProperties = new ACLModelProperties();
				aclProperties.load(acFile);
				return new ACLModel(aclProperties);
			} else {
				RBACModelProperties rbacProperties = new RBACModelProperties();
				rbacProperties.load(acFile);
				return new RBACModel(rbacProperties);
			}
		} catch(IOException e){
			throw new IOException("Cannot load properties file: " + acFile + ".");
		}
	}
	
	private Context loadContext(String contextFile) throws ParameterException, PropertyException, IOException{
		// Load context properties.
		ContextProperties properties = new ContextProperties();
		try {
			properties.load(contextFile);
		} catch(IOException e){
			throw new IOException("Cannot load properties file: " + contextFile + ".");
		}
		Context result = new Context(properties.getName(), properties.getActivities());
		Set<String> subjects = properties.getSubjects();
		if(subjects != null && !subjects.isEmpty())
			result.addSubjects(subjects);
		Set<String> attributes = properties.getAttributes();
		if(attributes != null && !attributes.isEmpty())
			result.addAttributes(attributes);
		ACModel acModel = getACModel(properties.getACModelName());
		if(acModel == null){
			//The access control model which is referenced by the context could not be loaded
			//-> Abort loading the context.
			throw new ParameterException(ErrorCode.INCONSISTENCY, "Referenced access control model could not be loaded.\nmodel name: "+properties.getACModelName());
		}
		result.setACModel(acModel);
		for(String activity: properties.getActivitiesWithDataUsage()){
			result.setDataUsageFor(activity, properties.getDataUsageFor(activity));
		}
		for(String activity: properties.getActivitiesWithConstraints()){
			for(AbstractConstraint<?> constraint: properties.getRoutingConstraints(activity)){
				result.addRoutingConstraint(activity, constraint);
			}
		}
		return result;
	}
	
	
	private CaseDataContainer loadDataContainer(String containerFile) throws ParameterException, PropertyException, IOException{
		//Load container properties
		CaseDataContainerProperties properties = new CaseDataContainerProperties();
		try {
			properties.load(containerFile);
		} catch(IOException e){
			throw new IOException("Cannot load properties file: " + containerFile + ".");
		}
		
//		String contextName = properties.getContextName();
//		Context referencedContext = getContext(contextName);
//		if(referencedContext == null)
//			throw new PropertyException(CaseDataContainerProperty.CONTEXT_NAME, referencedContext, "Unknown context.");

		AttributeValueGenerator valueGenerator = properties.getAttributeValueGenerator();
//		CaseDataContainer result = new CaseDataContainer(referencedContext, valueGenerator);
		CaseDataContainer result = new CaseDataContainer(valueGenerator);
		result.setName(properties.getName());
		
		return result;
	}
	
	private Simulation loadSimulation(String simulationFile) throws IOException, ParameterException, PropertyException, PerspectiveException, ConfigurationException, MissingRequirementException{
		//Load simulation properties
		SimulationProperties properties;
		try {
			properties = new SimulationProperties(simulationFile);
		} catch(IOException e){
			throw new IOException("Cannot load properties file: " + simulationFile + ".");
		}
		
		LogFormat logFormat= LogFormatFactory.getFormat(properties.getLogFormatType());
//		String fileName = new File(properties.getFileName()).getName();
//		fileName = fileName.substring(0, fileName.lastIndexOf('.'));
		String fileName = properties.getFileName();
		LogGenerator logGenerator = new TraceLogGenerator(logFormat, GeneralProperties.getInstance().getSimulationDirectory(), fileName);
		
		EntryGenerationType generationType = properties.getEntryGenerationType();
		LogEntryGenerator entryGenerator = null;
		if(generationType.equals(EntryGenerationType.DETAILED)){
			String contextName = properties.getContextName();
			Context context = getContext(contextName);
			if(context == null)
				throw new PropertyException(SimulationProperty.CONTEXT_NAME, contextName, "Unknown context.");
			
			String dataContainerName = properties.getDataContainerName();
			CaseDataContainer dataContainer = getCaseDataContainer(dataContainerName);
			if(dataContainer == null)
				throw new PropertyException(SimulationProperty.DATA_CONTAINER_NAME, dataContainerName, "Unknown data container.");
			entryGenerator = new DetailedLogEntryGenerator(context, dataContainer);
		} else {
			entryGenerator = new LogEntryGenerator();
		}
		
		CaseTimeGenerator timeGenerator = getCaseTimeGenerator(properties.getTimeGeneratorName());
		
		Simulation result = new Simulation(logGenerator, entryGenerator, timeGenerator);
		
		Set<SimulationRun> simulationRuns = new HashSet<SimulationRun>();
		for(SimulationRunProperties runProperties: properties.getSimulationRuns()){
			String runName = runProperties.getName();
			String ptNetName = runProperties.getNetName();
			PTNet ptNet = getPetriNet(ptNetName);
			if(ptNet == null)
				throw new PropertyException(SimulationRunProperty.NET_NAME, ptNetName, "Unknown Petri net.");
			TraceTransformerManager transformerManager = new TraceTransformerManager();
			for(String transformerName: runProperties.getTransformerNames()){
				AbstractTraceTransformer transformer = getTransformer(transformerName);
				if(transformer == null)
					throw new PropertyException(SimulationRunProperty.TRANSFORMERS, transformerName, "Unknown transformer.");
				transformerManager.addTransformer(transformer);
			}
		
			SimulationRun newSimulationRun = new SimulationRun(ptNet, runProperties.getPasses(), new RandomPTTraverser(ptNet), transformerManager);
			newSimulationRun.setName(runName);
			simulationRuns.add(newSimulationRun);
		}
		result.addSimulationRuns(simulationRuns);
		
		result.setName(properties.getName());
		
		return result;
	}
	
	public void updateFiles() throws ParameterException, IOException, PropertyException{
		for(ACModel acModel: acModels.values()){
//			System.out.println(acModel.getName());
			storeACModel(acModel);
		}
		for(Context context: contexts.values()){
//			System.out.println(context.getName());
			storeContext(context);
		}
		for(CaseDataContainer dataContainer: caseDataContainers.values()){
//			System.out.println(dataContainer.getName());
			storeCaseDataContainer(dataContainer);
		}
		for(AbstractTransformer transformer: transformers.values()){
//			System.out.println(transformer.getName());
			storeTransformer(transformer);
		}
		for(CaseTimeGenerator timeGenerator: caseTimeGenerators.values()){
//			System.out.println(timeGenerator.getName());
			storeCaseTimeGenerator(timeGenerator);
		}
		for(Simulation simulation: simulations.values()){
//			System.out.println(simulation.getName());
			storeSimulation(simulation);
		}
	}

	
	//------- Adding and removing contexts -------------------------------------------------------

	/**
	 * Adds a new context.<br>
	 * The context is also stores as property-file in the simulation directory.
	 * @param context The context to add.
	 * @throws ParameterException if the given context is <code>null</code>.
	 * @throws PropertyException if the procedure of property extraction fails.
	 * @throws IOException if the property-representation of the new context cannot be stored.
	 */
	public void addContext(Context context) throws ParameterException, IOException, PropertyException{
		addContext(context, true);
	}
	
	/**
	 * Adds a new context.<br>
	 * Depending on the value of the store-parameter, the context is also stores as property-file in the simulation directory.
	 * @param context The new context to add.
	 * @param storeToFile Indicates if the context should be stored on disk.
	 * @throws ParameterException if any parameter is invalid.
	 * @throws PropertyException if the context cannot be stored due to an error during property extraction.
	 * @throws IOException if the context cannot be stored due to an I/O Error.
	 */
	public void addContext(Context context, boolean storeToFile) throws ParameterException, IOException, PropertyException{
		Validate.notNull(context);
		Validate.notNull(storeToFile);
		contexts.put(context.getName(), context);
		if(storeToFile){
			storeContext(context);
		}
	}
	
	/**
	 * Stores the given context in form of a property-file in the simulation directory.<br>
	 * The context name will be used as file name.
	 * @param context The context to store.
	 * @throws ParameterException if the given context is <code>null</code> or invalid.
	 * @throws IOException if the context cannot be stored due to an I/O Error.
	 * @throws PropertyException if the context cannot be stored due to an error during property extraction.
	 */
	public void storeContext(Context context) throws ParameterException, IOException, PropertyException{
		Validate.notNull(context);
		context.getProperties().store(GeneralProperties.getInstance().getPathForContexts()+context.getName());
	}
	
	/**
	 * Checks, if there are context-components.
	 * @return <code>true</code> if there is at least one context;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean containsContexts(){
		return !contexts.isEmpty();
	}
	
	/**
	 * Checks, if there is a context with the given name.
	 * @return <code>true</code> if there is such a context;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean containsContext(String name){
		return contexts.get(name) != null;
	}
	
	/**
	 * Checks if there are contexts whose access control model equals the given model.
	 * @param acModelName The name of the access control model.
	 * @return <code>true</code> if there is at least one such context;<br>
	 * <code>fasle</code> otherwise.
	 */
	public boolean containsContextsWithACModel(ACModel acModel){
		for(Context context: contexts.values()){
			if(context.getACModel().equals(acModel))
				return true;
		}
		return false;
	}
	
	public Set<String> getContextsWithACModel(ACModel acModel){
		Set<String> result = new HashSet<String>();
		for(Context context: contexts.values()){
			if(context.getACModel().equals(acModel))
				result.add(context.getName());
		}
		return result;
	}
	
	/**
	 * Returns all contexts, i.e. contexts stored in the simulation directory.
	 * @return A set containing all contexts.
	 */
	public Collection<Context> getContexts(){
		return Collections.unmodifiableCollection(contexts.values());
	}
	
	/**
	 * Returns the context with the given name, if there is one.
	 * @param name The name of the desired context.
	 * @return The context with the given name, or <code>null</code> if there is no such context.
	 * @throws ParameterException if the given name is <code>null</code>.
	 */
	public Context getContext(String name) throws ParameterException{
		Validate.notNull(name);
		return contexts.get(name);
	}
	
	/**
	 * Returns the names of all contexts.
	 * @return
	 */
	public Set<String> getContextNames(){
		return Collections.unmodifiableSet(contexts.keySet());
	}
	
	/**
	 * Removes the context with the given name from the simulation components<br>
	 * and also deletes the corresponding property-file in the simulation directory.
	 * @param name The name of the context to remove.
	 * @throws PropertyException if the path for the simulation directory cannot be extracted from the general properties file.
	 * @throws ParameterException if there is an internal parameter misconfiguration.
	 * @throws IOException if the corresponding property file for the context cannot be deleted.
	 */
	public void removeContext(String name) throws PropertyException, IOException, ParameterException{
		if(contexts.remove(name) != null){
			FileUtils.deleteFile(GeneralProperties.getInstance().getPathForContexts()+name);
		}
	}
	
	
	//------- Adding and removing access control models ----------------------------------------------------
	
	/**
	 * Adds a new access control model.<br>
	 * The context is also stores as property-file in the simulation directory.
	 * @param acModel The model to add.
	 * @throws ParameterException if the given model is <code>null</code>.
	 * @throws PropertyException if the procedure of property extraction fails.
	 * @throws IOException if the property-representation of the new model cannot be stored.
	 */
	public void addACModel(ACModel acModel) throws ParameterException, IOException, PropertyException{
		addACModel(acModel, true);
	}
	
	/**
	 * Adds a new access control model.<br>
	 * Depending on the value of the store-parameter, the model is also stores as property-file in the simulation directory.
	 * @param acModel The new model to add.
	 * @param storeToFile Indicates if the model should be stored on disk.
	 * @throws ParameterException if any parameter is invalid.
	 * @throws PropertyException if the model cannot be stored due to an error during property extraction.
	 * @throws IOException if the model cannot be stored due to an I/O Error.
	 */
	public void addACModel(ACModel acModel, boolean storeToFile) throws ParameterException, IOException, PropertyException{
		Validate.notNull(acModel);
		Validate.notNull(storeToFile);
		acModels.put(acModel.getName(), acModel);
		if(storeToFile){
			storeACModel(acModel);
		}
	}
	
	/**
	 * Stores the given access control model in form of a property-file in the simulation directory.<br>
	 * The context name will be used as file name.
	 * @param acModel The model to store.
	 * @throws ParameterException if the given model is <code>null</code> or invalid.
	 * @throws IOException if the model cannot be stored due to an I/O Error.
	 * @throws PropertyException if the model cannot be stored due to an error during property extraction.
	 */
	public void storeACModel(ACModel acModel) throws ParameterException, IOException, PropertyException{
		Validate.notNull(acModel);
		acModel.getProperties().store(GeneralProperties.getInstance().getPathForACModels()+acModel.getName());
	}
	
	/**
	 * Checks, if there are access control model-components.
	 * @return <code>true</code> if there is at least one access control model;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean containsACModels(){
		return !acModels.isEmpty();
	}
	
	/**
	 * Checks, if there is an access control model with the given name.
	 * @return <code>true</code> if there is such a model;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean containsACModel(String name){
		return acModels.get(name) != null;
	}
	
	/**
	 * Returns all access control models, i.e. models stored in the simulation directory.
	 * @return A set containing all contexts.
	 */
	public Collection<ACModel> getACModels(){
		return Collections.unmodifiableCollection(acModels.values());
	}
	
	/**
	 * Returns the access control model with the given name, if there is one.
	 * @param name The name of the desired access control model.
	 * @return The model with the given name, or <code>null</code> if there is no such model.
	 * @throws ParameterException if the given name is <code>null</code>.
	 */
	public ACModel getACModel(String name) throws ParameterException{
		Validate.notNull(name);
		return acModels.get(name);
	}
	
	/**
	 * Returns the names of all access control models.
	 * @return
	 */
	public Set<String> getACModelNames(){
		return Collections.unmodifiableSet(acModels.keySet());
	}
	
	/**
	 * Removes the access control model with the given name from the simulation components<br>
	 * and also deletes the corresponding property-file in the simulation directory.
	 * @param name The name of the model to remove.
	 * @throws PropertyException if the path for the simulation directory cannot be extracted from the general properties file.
	 * @throws ParameterException if there is an internal parameter misconfiguration.
	 * @throws IOException if the corresponding property file for the model cannot be deleted.
	 */
	public void removeACModel(String name) throws PropertyException, IOException, ParameterException{
		if(acModels.remove(name) != null){
			FileUtils.deleteFile(GeneralProperties.getInstance().getPathForACModels()+name);
		}
	}
	
	
	//------- Adding and removing Petri nets ------------------------------------------------------------------------
	
	/**
	 * Adds a new Petri net.<br>
	 * The net is also stores as property-file in the simulation directory.
	 * @param context The Petri net to add.
	 * @throws ParameterException if the given net is <code>null</code>.
	 * @throws PropertyException if the procedure of property extraction fails.
	 * @throws IOException if the property-representation of the new net cannot be stored.
	 */
	public void addPetriNet(PTNet petriNet) throws ParameterException, IOException, PropertyException{
		addPetriNet(petriNet, true);
	}
	
	/**
	 * Adds a new Petri net.<br>
	 * Depending on the value of the store-parameter, the net is also stores as property-file in the simulation directory.
	 * @param context The new Petri net to add.
	 * @param storeToFile Indicates if the net should be stored on disk.
	 * @throws ParameterException if any parameter is invalid.
	 * @throws PropertyException if the net cannot be stored due to a property error on extracting path from general properties.
	 * @throws IOException if the net cannot be stored due to an I/O Error.
	 */
	public void addPetriNet(PTNet petriNet, boolean storeToFile) throws ParameterException, IOException, PropertyException{
		Validate.notNull(petriNet);
		Validate.notNull(storeToFile);
		petriNets.put(petriNet.getName(), petriNet);
		if(storeToFile){
			FileWriter writer = new FileWriter(GeneralProperties.getInstance().getPathForPetriNets()+petriNet.getName());
			writer.write(petriNet.toPNML());
			writer.close();
		}
	}
	
	/**
	 * Checks, if there are petri nets.
	 * @return <code>true</code> if there is at least one net;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean hasPetriNets(){
		return !petriNets.isEmpty();
	}
	
	/**
	 * Returns all Petri nets, i.e. nets stored in the simulation directory.
	 * @return A set containing all Petri nets.
	 */
	public Collection<PTNet> getPetriNets(){
		return Collections.unmodifiableCollection(petriNets.values());
	}
	
	/**
	 * Returns the Petri net with the given name, if there is one.
	 * @param name The name of the desired net.
	 * @return The Petri net with the given name, or <code>null</code> if there is no such net.
	 * @throws ParameterException if the given name is <code>null</code>.
	 */
	public PTNet getPetriNet(String name) throws ParameterException{
		Validate.notNull(name);
		return petriNets.get(name);
	}
	
	/**
	 * Returns the names of all Petri nets.
	 * @return
	 */
	public Set<String> getPetriNetNames(){
		return Collections.unmodifiableSet(petriNets.keySet());
	}
	
	/**
	 * Removes the Petri net with the given name from the simulation components<br>
	 * and also deletes the corresponding property-file in the simulation directory.
	 * @param name The name of the net to remove.
	 * @throws PropertyException if the path for the simulation directory cannot be extracted from the general properties file.
	 * @throws ParameterException if there is an internal parameter misconfiguration.
	 * @throws IOException if the corresponding property file for the Petri net cannot be deleted.
	 */
	public void removePetriNet(String name) throws PropertyException, IOException, ParameterException{
		if(petriNets.remove(name) != null){
			FileUtils.deleteFile(GeneralProperties.getInstance().getPathForPetriNets()+name);
		}
	}
	
	
	//------- Adding and removing case data containers ---------------------------------------------------------------
	
	/**
	 * Adds a new case data container.<br>
	 * The container is also stores as property-file in the simulation directory.
	 * @param container The case data container to add.
	 * @throws ParameterException if the given container is <code>null</code>.
	 * @throws PropertyException if the procedure of property extraction fails.
	 * @throws IOException if the property-representation of the new container cannot be stored.
	 */
	public void addCaseDataContainer(CaseDataContainer container) throws ParameterException, IOException, PropertyException{
		addCaseDataContainer(container, true);
	}
	
	/**
	 * Adds a new case data container.<br>
	 * Depending on the value of the store-parameter, the container is also stored as property-file in the simulation directory.
	 * @param container The new case data container to add.
	 * @param storeToFile Indicates if the container should be stored on disk.
	 * @throws ParameterException if any parameter is invalid.
	 * @throws PropertyException if the container cannot be stored due to an error during property extraction.
	 * @throws IOException if the container cannot be stored due to an I/O Error.
	 */
	public void addCaseDataContainer(CaseDataContainer container, boolean storeToFile) throws ParameterException, IOException, PropertyException{
		Validate.notNull(container);
		Validate.notNull(storeToFile);
		caseDataContainers.put(container.getName(), container);
		if(storeToFile){
			storeCaseDataContainer(container);
		}
	}
	
	/**
	 * Stores the given case data container in form of a property-file in the simulation directory.<br>
	 * The container name will be used as file name.
	 * @param container The container to store.
	 * @throws ParameterException if the given container is <code>null</code> or invalid.
	 * @throws IOException if the container cannot be stored due to an I/O Error.
	 * @throws PropertyException if the container cannot be stored due to an error during property extraction.
	 */
	public void storeCaseDataContainer(CaseDataContainer container) throws ParameterException, IOException, PropertyException{
		Validate.notNull(container);
		container.getProperties().store(GeneralProperties.getInstance().getPathForDataContainers()+container.getName());
	}
	
	/**
	 * Checks, if there are case data container-components.
	 * @return <code>true</code> if there is at least one container;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean containsCaseDataContainers(){
		return !caseDataContainers.isEmpty();
	}
	
	/**
	 * Checks, if there is a case data container with the given name.
	 * @return <code>true</code> if there is such a container;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean containsCaseDataContainer(String name){
		return caseDataContainers.get(name) != null;
	}
	
	/**
	 * Returns all case data containers, i.e. containers stored in the simulation directory.
	 * @return A set containing all case data containers.
	 */
	public Collection<CaseDataContainer> getCaseDataContainers(){
		return Collections.unmodifiableCollection(caseDataContainers.values());
	}
	
	/**
	 * Returns the case data container with the given name, if there is one.
	 * @param name The name of the desired container.
	 * @return The case data container with the given name, or <code>null</code> if there is no such container.
	 * @throws ParameterException if the given name is <code>null</code>.
	 */
	public CaseDataContainer getCaseDataContainer(String name) throws ParameterException{
		Validate.notNull(name);
		return caseDataContainers.get(name);
	}
	
	/**
	 * Returns the names of all case data containers.
	 * @return
	 */
	public Set<String> getCaseDataContainerNames(){
		return Collections.unmodifiableSet(caseDataContainers.keySet());
	}
	
//	/**
//	 * Returns all case data containers which refer to the given context.
//	 * @param context The context for the desired containers.
//	 * @return A set of all case data containers which refer to the given context.
//	 * @throws ParameterException if the given context is <code>null</code>.
//	 */
//	public Set<CaseDataContainer> getCaseDataContainersWithContext(Context context) throws ParameterException{
//		Validate.notNull(context);
//		Set<CaseDataContainer> caseDataContainers = new HashSet<CaseDataContainer>();
//		for(CaseDataContainer dataContainer: getCaseDataContainers()){
//			if(dataContainer.getContext() == context){
//				caseDataContainers.add(dataContainer);
//			}
//		}
//		return caseDataContainers;
//	}
	
	/**
	 * Removes the case data container with the given name from the simulation components<br>
	 * and also deletes the corresponding property-file in the simulation directory.
	 * @param name The name of the container to remove.
	 * @throws PropertyException if the path for the simulation directory cannot be extracted from the general properties file.
	 * @throws ParameterException if there is an internal parameter misconfiguration.
	 * @throws IOException if the corresponding property file for the container cannot be deleted.
	 */
	public void removeCaseDataContainer(String name) throws PropertyException, IOException, ParameterException{
		if(caseDataContainers.remove(name) != null){
			FileUtils.deleteFile(GeneralProperties.getInstance().getPathForDataContainers()+name);
		}
	}
	

	//------- Adding and removing time generators ---------------------------------------------------------------------
	
	/**
	 * Adds a new case time generator.<br>
	 * The generator is also stored as property-file in the simulation directory.
	 * @param timeGenerator The time generator to add.
	 * @throws ParameterException if the given generator is <code>null</code>.
	 * @throws PropertyException if the procedure of property extraction fails.
	 * @throws IOException if the new time generator cannot be stored.
	 */
	public void addCaseTimeGenerator(CaseTimeGenerator timeGenerator) throws ParameterException, PropertyException, IOException{
		addCaseTimeGenerator(timeGenerator, true);
	}
	
	/**
	 * Adds a new case time generator.<br>
	 * Depending on the value of the store-parameter, the generator is also stored as property-file in the simulation directory.
	 * @param timeGenerator The time generator to add.
	 * @param storeToFile Indicates if the generator should be stored on disk.
	 * @throws ParameterException if any parameter is invalid.
	 * @throws PropertyException if an error occurs during property extraction (e.g. properties name).
	 * @throws IOException if the generator cannot be stored due to an I/O Error.
	 */
	public void addCaseTimeGenerator(CaseTimeGenerator timeGenerator, boolean storeToFile) throws ParameterException, IOException, PropertyException{
		Validate.notNull(timeGenerator);
		Validate.notNull(storeToFile);
		caseTimeGenerators.put(timeGenerator.getName(), timeGenerator);
		if(storeToFile){
			storeCaseTimeGenerator(timeGenerator);
		}
	}
	
	/**
	 * Stores the given time generator in the simulation directory.<br>
	 * The time generator name will be used as file name.
	 * @param timeGenerator The time generator to store.
	 * @throws ParameterException if the given time generator is <code>null</code> or invalid.
	 * @throws IOException if the time generator cannot be stored due to an I/O Error.
	 * @throws PropertyException if the generator cannot be stored due to property extraction error (e.g. name field).
	 */
	public void storeCaseTimeGenerator(CaseTimeGenerator timeGenerator) throws ParameterException, IOException, PropertyException{
		Validate.notNull(timeGenerator);
		timeGenerator.getProperties().store(GeneralProperties.getInstance().getPathForTimeGenerators()+timeGenerator.getName());
	}
	
	/**
	 * Checks, if there are time generators.
	 * @return <code>true</code> if there is at least one time generator;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean containsCaseTimeGenerators(){
		return !caseTimeGenerators.isEmpty();
	}
	
	/**
	 * Checks, if there is a time generator with the given name.
	 * @return <code>true</code> if there is such a generator;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean containsCaseTimeGenerator(String name){
		return caseTimeGenerators.get(name) != null;
	}
	
	/**
	 * Returns all time generators, i.e. generators stored in the simulation directory.
	 * @return A set containing all time generators.
	 */
	public Collection<CaseTimeGenerator> getCaseTimeGenerators(){
		return Collections.unmodifiableCollection(caseTimeGenerators.values());
	}
	
	/**
	 * Returns the time generator with the given name, if there is one.
	 * @param name The name of the desired time generator.
	 * @return The time generator with the given name, or <code>null</code> if there is no such generator.
	 * @throws ParameterException if the given name is <code>null</code>.
	 */
	public CaseTimeGenerator getCaseTimeGenerator(String name) throws ParameterException{
		Validate.notNull(name);
		return caseTimeGenerators.get(name);
	}
	
	/**
	 * Returns the names of all time generators.
	 * @return
	 */
	public Set<String> getCaseTimeGeneratorNames(){
		return Collections.unmodifiableSet(caseTimeGenerators.keySet());
	}

	/**
	 * Removes the time generator with the given name from the simulation components<br>
	 * and also deletes the corresponding property-file in the simulation directory.
	 * @param name The name of the time generator to remove.
	 * @throws PropertyException if the path for the simulation directory cannot be extracted from the general properties file.
	 * @throws ParameterException if there is an internal parameter misconfiguration.
	 * @throws IOException if the corresponding property file for the time generator cannot be deleted.
	 */
	public void removeCaseTimeGenerator(String name) throws PropertyException, IOException, ParameterException{
		if(caseTimeGenerators.remove(name) != null){
			FileUtils.deleteFile(GeneralProperties.getInstance().getPathForTimeGenerators()+name);
		}
	}
	

//	/**
//	 * Adds new case time generator properties.<br>
//	 * The properties are also stored as property-file in the simulation directory.
//	 * @param timeProperties The time properties to add.
//	 * @throws ParameterException if the given properties are <code>null</code>.
//	 * @throws PropertyException if the procedure of property extraction fails.
//	 * @throws IOException if the new time properties cannot be stored.
//	 */
//	public void addCaseTimeGeneratorProperties(TimeProperties timeProperties) throws ParameterException, PropertyException, IOException{
//		addCaseTimeGeneratorProperties(timeProperties, true);
//	}
//	
//	/**
//	 * Adds new case time generator properties.<br>
//	 * Depending on the value of the store-parameter, the properties are also stored as property-file in the simulation directory.
//	 * @param timeProperties The time properties to add.
//	 * @param storeToFile Indicates if the properties should be stored on disk.
//	 * @throws ParameterException if any parameter is invalid.
//	 * @throws PropertyException if an error occurs during property extraction (e.g. properties name).
//	 * @throws IOException if the generator cannot be stored due to an I/O Error.
//	 */
//	public void addCaseTimeGeneratorProperties(TimeProperties timeProperties, boolean storeToFile) throws ParameterException, IOException, PropertyException{
//		Validate.notNull(timeProperties);
//		Validate.notNull(storeToFile);
//		caseTimeGeneratorProperties.put(timeProperties.getName(), timeProperties);
//		if(storeToFile){
//			storeCaseTimeGeneratorProperties(timeProperties);
//		}
//	}
//	
//	/**
//	 * Stores the given time properties in the simulation directory.<br>
//	 * The time properties name will be used as file name.
//	 * @param timeProperties The time properties to store.
//	 * @throws ParameterException if the given time properties are <code>null</code> or invalid.
//	 * @throws IOException if the time properties cannot be stored due to an I/O Error.
//	 * @throws PropertyException if the properties cannot be stored due to property extraction error (e.g. name field).
//	 */
//	public void storeCaseTimeGeneratorProperties(TimeProperties timeProperties) throws ParameterException, IOException, PropertyException{
//		Validate.notNull(timeProperties);
//		timeProperties.store(GeneralProperties.getInstance().getPathForTimeGenerators()+timeProperties.getName());
//	}
//	
//	/**
//	 * Checks, if there are time properties.
//	 * @return <code>true</code> if there is at least one time properties instance;<br>
//	 * <code>false</code> otherwise.
//	 */
//	public boolean containsCaseTimeGeneratorProperties(){
//		return !caseTimeGeneratorProperties.isEmpty();
//	}
//	
//	/**
//	 * Checks, if there is a time properties instance with the given name.
//	 * @return <code>true</code> if there is such an instance;<br>
//	 * <code>false</code> otherwise.
//	 */
//	public boolean containsCaseTimeGeneratorProperties(String name){
//		return caseTimeGeneratorProperties.get(name) != null;
//	}
//	
//	/**
//	 * Returns all time generator properties, i.e. properties stored in the simulation directory.
//	 * @return A set containing all time properties.
//	 */
//	public Collection<TimeProperties> getCaseTimeGeneratorProperties(){
//		return Collections.unmodifiableCollection(caseTimeGeneratorProperties.values());
//	}
//	
//	/**
//	 * Returns the time properties instance with the given name, if there is one.
//	 * @param name The name of the desired time properties.
//	 * @return The time properties instance with the given name, or <code>null</code> if there is no such instance.
//	 * @throws ParameterException if the given name is <code>null</code>.
//	 */
//	public TimeProperties getCaseTimeGeneratorProperties(String name){
//		return caseTimeGeneratorProperties.get(name);
//	}
//
//	/**
//	 * Removes the time properties instance with the given name from the simulation components<br>
//	 * and also deletes the corresponding property-file in the simulation directory.
//	 * @param name The name of the time properties to remove.
//	 * @throws PropertyException if the path for the simulation directory cannot be extracted from the general properties file.
//	 * @throws ParameterException if there is an internal parameter misconfiguration.
//	 * @throws IOException if the corresponding property file for the time properties cannot be deleted.
//	 */
//	public void removeCaseTimeGeneratorProperties(String name) throws PropertyException, IOException, ParameterException{
//		if(caseTimeGeneratorProperties.remove(name) != null){
//			FileUtils.deleteFile(GeneralProperties.getInstance().getPathForTimeGenerators()+name);
//		}
//	}
//	
	
	//------- Adding and removing transformers -------------------------------------------------------------------------
	
	/**
	 * Adds a new transformer.<br>
	 * The transformer is also stores as property-file in the simulation directory.
	 * @param transformer The transformer to add.
	 * @throws ParameterException if the given transformer is <code>null</code>.
	 * @throws PropertyException if the procedure of property extraction fails.
	 * @throws IOException if the property-representation of the new transformer cannot be stored.
	 */
	public void addTransformer(AbstractTraceTransformer transformer) throws ParameterException, IOException, PropertyException{
		addTransformer(transformer, true);
	}
	
	/**
	 * Adds a new transformer.<br>
	 * Depending on the value of the store-parameter, the transformer is also stores as property-file in the simulation directory.
	 * @param transformer The new transformer to add.
	 * @param storeToFile Indicates if the transformer should be stored on disk.
	 * @throws ParameterException if any parameter is invalid.
	 * @throws PropertyException if the transformer cannot be stored due to an error during property extraction.
	 * @throws IOException if the cannot be stored due to an I/O Error.
	 */
	public void addTransformer(AbstractTraceTransformer transformer, boolean storeToFile) throws ParameterException, IOException, PropertyException{
		Validate.notNull(transformer);
		Validate.notNull(storeToFile);
		transformers.put(transformer.getName(), transformer);
		if(storeToFile){
			storeTransformer(transformer);
		}
	}
	
	/**
	 * Stores the given transformer in form of a property-file in the simulation directory.<br>
	 * The transformer name will be used as file name.
	 * @param transformer The transformer to store.
	 * @throws ParameterException if the given transformer is <code>null</code> or invalid.
	 * @throws IOException if the transformer cannot be stored due to an I/O Error.
	 * @throws PropertyException if the transformer cannot be stored due to an error during property extraction.
	 */
	public void storeTransformer(AbstractTransformer transformer) throws ParameterException, IOException, PropertyException{
		Validate.notNull(transformer);
		transformer.getProperties().store(GeneralProperties.getInstance().getPathForTransformers()+transformer.getName());
	}
	
	/**
	 * Checks, if there are transformer-components.
	 * @return <code>true</code> if there is at least one transformer;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean containsTransformers(){
		return !transformers.isEmpty();
	}
	
	/**
	 * Checks, if there is a transformer with the given name.
	 * @return <code>true</code> if there is such a transformer;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean containsTransformer(String name){
		return transformers.get(name) != null;
	}
	
	/**
	 * Returns all transformers, i.e. transformers stored in the simulation directory.
	 * @return A set containing all transformers.
	 */
	public Collection<AbstractTraceTransformer> getTransformers(){
		return Collections.unmodifiableCollection(transformers.values());
	}
	
	/**
	 * Returns the names of all transformers, i.e. transformers stored in the simulation directory.
	 * @return A set containing all transformer names.
	 */
	public Set<String> getTransformerNames(){
		return Collections.unmodifiableSet(transformers.keySet());
	}
	
	/**
	 * Returns the transformer with the given name, if there is one.
	 * @param name The name of the desired transformer.
	 * @return The transformer with the given name, or <code>null</code> if there is no such transformer.
	 * @throws ParameterException if the given name is <code>null</code>.
	 */
	public AbstractTraceTransformer getTransformer(String name) throws ParameterException{
		Validate.notNull(name);
		return transformers.get(name);
	}
	
	/**
	 * Removes the transformer with the given name from the simulation components<br>
	 * and also deletes the corresponding property-file in the simulation directory.
	 * @param name The name of the transformer to remove.
	 * @throws PropertyException if the path for the simulation directory cannot be extracted from the general properties file.
	 * @throws ParameterException if there is an internal parameter misconfiguration.
	 * @throws IOException if the corresponding property file for the transformer cannot be deleted.
	 */
	public void removeTransformer(String name) throws PropertyException, IOException, ParameterException{
		if(transformers.remove(name) != null){
			FileUtils.deleteFile(GeneralProperties.getInstance().getPathForTransformers()+name);
		}
	}
	

	//------- Adding and removing simulations ----------------------------------------------------------
	
	/**
	 * Adds a new simulation.<br>
	 * The simulation is also stored as property-file in the simulation directory.
	 * @param simulation The simulation to add.
	 * @throws ParameterException if the given simulation is <code>null</code>.
	 * @throws PropertyException if the procedure of property extraction fails.
	 * @throws IOException if the property-representation of the new simulation cannot be stored.
	 */
	public void addSimulation(Simulation simulation) throws ParameterException, IOException, PropertyException{
		addSimulation(simulation, true);
	}
	
	/**
	 * Adds new a simulation.<br>
	 * Depending on the value of the store-parameter, the simulation is also stored as property-file in the simulation directory.
	 * @param simulation The new simulation to add.
	 * @param storeToFile Indicates if the simulation should be stored on disk.
	 * @throws ParameterException if any parameter is invalid.
	 * @throws PropertyException if the simulation cannot be stored due to an error during property extraction.
	 * @throws IOException if the simulation cannot be stored due to an I/O Error.
	 */
	public void addSimulation(Simulation simulation, boolean storeToFile) throws ParameterException, IOException, PropertyException{
		Validate.notNull(simulation);
		Validate.notNull(storeToFile);
		simulations.put(simulation.getName(), simulation);
		if(storeToFile){
			storeSimulation(simulation);
		}
	}
	
	/**
	 * Stores the given simulation in form of a property-file in the simulation directory.<br>
	 * The simulation name will be used as file name.
	 * @param simulation The simulation to store.
	 * @throws ParameterException if the given simulation is <code>null</code> or invalid.
	 * @throws IOException if the simulation cannot be stored due to an I/O Error.
	 * @throws PropertyException if the simulation cannot be stored due to an error during property extraction.
	 */
	public void storeSimulation(Simulation simulation) throws ParameterException, IOException, PropertyException{
		Validate.notNull(simulation);
		simulation.getProperties().store(GeneralProperties.getInstance().getPathForSimulations()+simulation.getName());
	}
	
	/**
	 * Checks, if there are simulations.
	 * @return <code>true</code> if there is at least one simulation;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean containsSimulations(){
		return !simulations.isEmpty();
	}
	
	/**
	 * Checks, if there is a simulation with the given name.
	 * @return <code>true</code> if there is such a simulation;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean containsSimulation(String name){
		return simulations.get(name) != null;
	}
	
	/**
	 * Returns all simulations, i.e. simulations stored in the simulation directory.
	 * @return A set containing all simulations.
	 */
	public Collection<Simulation> getSimulations(){
		return Collections.unmodifiableCollection(simulations.values());
	}
	
	/**
	 * Returns the simulation with the given name, if there is one.
	 * @param name The name of the desired simulation.
	 * @return The simulation with the given name, or <code>null</code> if there is no such simulation.
	 * @throws ParameterException if the given name is <code>null</code>.
	 */
	public Simulation getSimulation(String name) throws ParameterException{
		Validate.notNull(name);
		return simulations.get(name);
	}
	
	/**
	 * Returns the names of all simulations.
	 * @return
	 */
	public Set<String> getSimulationNames(){
		return Collections.unmodifiableSet(simulations.keySet());
	}
	
	/**
	 * Removes the simulation with the given name from the simulation components<br>
	 * and also deletes the corresponding property-file in the simulation directory.
	 * @param name The name of the simulation to remove.
	 * @throws PropertyException if the path for the simulation directory cannot be extracted from the general properties file.
	 * @throws ParameterException if there is an internal parameter misconfiguration.
	 * @throws IOException if the corresponding property file for the simulation cannot be deleted.
	 */
	public void removeSimulation(String name) throws PropertyException, IOException, ParameterException{
		if(simulations.remove(name) != null){
			FileUtils.deleteFile(GeneralProperties.getInstance().getPathForSimulations()+name);
		}
	}
	
//	/**
//	 * Adds new simulation properties.<br>
//	 * The simulation properties are also stored as property-file in the simulation directory.
//	 * @param properties The simulation properties to add.
//	 * @throws ParameterException if the given simulation properties are <code>null</code>.
//	 * @throws PropertyException if the procedure of property extraction fails.
//	 * @throws IOException if the property-representation of the new simulation properties cannot be stored.
//	 */
//	public void addSimulationProperties(SimulationProperties properties) throws ParameterException, IOException, PropertyException{
//		addSimulationProperties(properties, true);
//	}
//	
//	/**
//	 * Adds new simulation properties.<br>
//	 * Depending on the value of the store-parameter, the properties are also stored as property-file in the simulation directory.
//	 * @param properties The new simulation to add.
//	 * @param storeToFile Indicates if the simulation should be stored on disk.
//	 * @throws ParameterException if any parameter is invalid.
//	 * @throws PropertyException if the simulation properties cannot be stored due to an error during property extraction.
//	 * @throws IOException if the simulation properties cannot be stored due to an I/O Error.
//	 */
//	public void addSimulationProperties(SimulationProperties properties, boolean storeToFile) throws ParameterException, IOException, PropertyException{
//		Validate.notNull(properties);
//		Validate.notNull(storeToFile);
//		simulationProperties.put(properties.getName(), properties);
//		if(storeToFile){
//			storeSimulationProperties(properties);
//		}
//	}
//	
//	/**
//	 * Stores the given simulation in form of a property-file in the simulation directory.<br>
//	 * The simulation properties name will be used as file name.
//	 * @param properties The simulation properties to store.
//	 * @throws ParameterException if the given simulation properties are <code>null</code> or invalid.
//	 * @throws IOException if the simulation properties cannot be stored due to an I/O Error.
//	 * @throws PropertyException if the simulation properties cannot be stored due to an error during property extraction.
//	 */
//	public void storeSimulationProperties(SimulationProperties properties) throws ParameterException, IOException, PropertyException{
//		Validate.notNull(properties);
//		properties.store(GeneralProperties.getInstance().getPathForSimulations()+properties.getName());
//	}
//	
//	/**
//	 * Checks, if there are simulation properties.
//	 * @return <code>true</code> if there is at least one simulation properties instance;<br>
//	 * <code>false</code> otherwise.
//	 */
//	public boolean containsSimulationProperties(){
//		return !simulationProperties.isEmpty();
//	}
//	
//	/**
//	 * Checks, if there is a simulation properties instance with the given name.
//	 * @return <code>true</code> if there is such an instance;<br>
//	 * <code>false</code> otherwise.
//	 */
//	public boolean containsSimulationProperties(String name){
//		return simulationProperties.get(name) != null;
//	}
//	
//	/**
//	 * Returns all simulation properties, i.e. simulation properties stored in the simulation directory.
//	 * @return A set containing all simulation properties.
//	 */
//	public Collection<SimulationProperties> getSimulationProperties(){
//		return Collections.unmodifiableCollection(simulationProperties.values());
//	}
//	
//	/**
//	 * Returns the simulation property instance with the given name, if there is one.
//	 * @param name The name of the desired simulation properties instance.
//	 * @return The simulation properties instance with the given name, or <code>null</code> if there is no such instance.
//	 * @throws ParameterException if the given name is <code>null</code>.
//	 */
//	public SimulationProperties getSimulationProperties(String name) throws ParameterException{
//		Validate.notNull(name);
//		return simulationProperties.get(name);
//	}
//	
//	/**
//	 * Removes the simulation properties with the given name from the simulation components<br>
//	 * and also deletes the corresponding property-file in the simulation directory.
//	 * @param name The name of the simulation properties to remove.
//	 * @throws PropertyException if the path for the simulation directory cannot be extracted from the general properties file.
//	 * @throws ParameterException if there is an internal parameter misconfiguration.
//	 * @throws IOException if the corresponding property file for the simulation properties cannot be deleted.
//	 */
//	public void removeSimulationProperties(String name) throws PropertyException, IOException, ParameterException{
//		if(simulationProperties.remove(name) != null){
//			FileUtils.deleteFile(GeneralProperties.getInstance().getPathForSimulations()+name);
//		}
//	}
}
