package de.uni.freiburg.iig.telematik.secsy.gui;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import de.invation.code.toval.constraint.AbstractConstraint;
import de.invation.code.toval.file.FileUtils;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.ParameterException.ErrorCode;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.format.AbstractLogFormat;
import de.uni.freiburg.iig.telematik.jawl.format.LogFormatFactory;
import de.uni.freiburg.iig.telematik.jawl.writer.PerspectiveException;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.MessageDialog;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.AbstractTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.TransformerType;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel.BoDPropertyTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel.DayDelayTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel.IncompleteLoggingTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel.ObfuscationTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel.SkipActivitiesTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel.SoDPropertyTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel.UnauthorizedExecutionTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.gui.properties.GeneralProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.AttributeValueGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.CaseDataContainer;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.Context;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.DetailedLogEntryGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.LogEntryGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.TraceLogGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.properties.CaseDataContainerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.properties.ContextProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.CaseTimeGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.properties.TimeGeneratorFactory;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.properties.TimeProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.ConfigurationException;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.Simulation;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.SimulationRun;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.EntryGenerationType;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.SimulationProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.SimulationProperty;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.SimulationRunProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.SimulationRunProperty;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerManager;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.PropertyAwareTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.TransformerFactory;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.exception.MissingRequirementException;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.BoDPropertyTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.DayDelayTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.IncompleteLoggingTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.ObfuscationTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.SkipActivitiesTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.SoDPropertyTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.UnauthorizedExecutionTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.AbstractTraceTransformer;
import de.uni.freiburg.iig.telematik.sepia.parser.pnml.PNMLParser;
import de.uni.freiburg.iig.telematik.sepia.petrinet.AbstractPetriNet;
import de.uni.freiburg.iig.telematik.sepia.petrinet.pt.PTNet;
import de.uni.freiburg.iig.telematik.sepia.petrinet.pt.RandomPTTraverser;
import de.uni.freiburg.iig.telematik.sepia.serialize.PNSerialization;
import de.uni.freiburg.iig.telematik.sepia.serialize.SerializationException;
import de.uni.freiburg.iig.telematik.sepia.serialize.formats.PNSerializationFormat;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.ACModel;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.acl.ACLModel;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.properties.ACLModelProperties;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.properties.ACMValidationException;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.properties.ACModelProperties;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.properties.ACModelType;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.properties.RBACModelProperties;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.rbac.RBACModel;


public class SimulationComponents {
	
	public static final String CUSTOM_TRANSFORMER_PANEL_PACKAGE = "de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel.custom.";
	public static final String CUSTOM_TRANSFORMER_PACKAGE = "de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.custom.";
	
	private static SimulationComponents instance = null;
	
	private Map<String, ACModel> acModels = new HashMap<String, ACModel>();
	private Map<String, PTNet> petriNets = new HashMap<String, PTNet>();
	private Map<String, Context> contexts = new HashMap<String, Context>();
	private Map<String, CaseDataContainer> caseDataContainers = new HashMap<String, CaseDataContainer>();
	private Map<String, AbstractTraceTransformer> transformers = new HashMap<String, AbstractTraceTransformer>();
	private Map<String, CaseTimeGenerator> caseTimeGenerators = new HashMap<String, CaseTimeGenerator>();
	private Map<String, Simulation> simulations = new HashMap<String, Simulation>();
	private static Set<TransformerType> transformerTypes = new HashSet<TransformerType>();
	static {
		transformerTypes.add(new TransformerType(DayDelayTransformer.class, DayDelayTransformerPanel.class));
		transformerTypes.add(new TransformerType(IncompleteLoggingTransformer.class, IncompleteLoggingTransformerPanel.class));
		transformerTypes.add(new TransformerType(SkipActivitiesTransformer.class, SkipActivitiesTransformerPanel.class));
		transformerTypes.add(new TransformerType(ObfuscationTransformer.class, ObfuscationTransformerPanel.class));
		transformerTypes.add(new TransformerType(UnauthorizedExecutionTransformer.class, UnauthorizedExecutionTransformerPanel.class));
		transformerTypes.add(new TransformerType(SoDPropertyTransformer.class, SoDPropertyTransformerPanel.class));
		transformerTypes.add(new TransformerType(BoDPropertyTransformer.class, BoDPropertyTransformerPanel.class));
	}
	private Set<TransformerType> customTransformerTypes = new HashSet<TransformerType>();
	
	
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
		MessageDialog.getInstance().addMessage("Accessing Simulation Directory:");
		MessageDialog.getInstance().addMessage(GeneralProperties.getInstance().getSimulationDirectory());
		MessageDialog.getInstance().newLine();
		MessageDialog.getInstance().addMessage("Loading simulation components.");
		int loadingStep = 1;
		
		// Load access control models
		//   -> Contexts require access control models, thus they have to be loaded first.
		MessageDialog.getInstance().addMessage(loadingStep++ + ". Searching for access control models:");
		List<String> acFiles = null;
		try {
			acFiles = FileUtils.getFileNamesInDirectory(GeneralProperties.getInstance().getPathForACModels(), true);
		} catch (IOException e1) {
			throw new IOException("Cannot access access control model directory.");
		}
		for(String acFile: acFiles){
			MessageDialog.getInstance().addMessage("Loading access control model: " + acFile.substring(acFile.lastIndexOf('/') + 1) + "...   ");
			try{
				addACModel(loadACModel(acFile), false);
				MessageDialog.getInstance().addMessage("Done.");
			} catch(Exception e){
				MessageDialog.getInstance().addMessage("Error: "+e.getMessage());
			}
		}
		MessageDialog.getInstance().newLine();

		// Load contexts
		MessageDialog.getInstance().addMessage(loadingStep++ + ". Searching for contexts:");
		List<String> contextFiles = null;
		try {
			contextFiles = FileUtils.getFileNamesInDirectory(GeneralProperties.getInstance().getPathForContexts(), true);
		} catch (IOException e1) {
			throw new IOException("Cannot access context directory.");
		}
		for(String contextFile: contextFiles){
			MessageDialog.getInstance().addMessage("Loading context: " + contextFile.substring(contextFile.lastIndexOf('/') + 1) + "...   ");
			try{
				addContext(loadContext(contextFile), false);
				MessageDialog.getInstance().addMessage("Done.");
			} catch(Exception e){
				MessageDialog.getInstance().addMessage("Error: "+e.getMessage());
			}
		}
		MessageDialog.getInstance().newLine();
		
		// Load Petri nets
		MessageDialog.getInstance().addMessage(loadingStep++ + ". Searching for Petri nets:");
		List<String> netFiles = null;
		try {
			netFiles = FileUtils.getFileNamesInDirectory(GeneralProperties.getInstance().getPathForPetriNets(), true);
		} catch (IOException e1) {
			throw new IOException("Cannot access Petri net directory.");
		}
		for(String netFile: netFiles){
			MessageDialog.getInstance().addMessage("Loading Petri net: " + netFile.substring(netFile.lastIndexOf('/') + 1) + "...   ");
			try{
				AbstractPetriNet<?, ?, ?, ?, ?> loadedNet = null;
				loadedNet = new PNMLParser().parse(netFile, false, false).getPetriNet();
				
				if(!(loadedNet instanceof PTNet))
					throw new ParameterException(ErrorCode.INCOMPATIBILITY, "Loaded Petri net is not a P/T Net");
				addPetriNet((PTNet) loadedNet, false);
				MessageDialog.getInstance().addMessage("Done.");
			} catch(Exception e){
				MessageDialog.getInstance().addMessage("Error: "+e.getMessage());
				e.printStackTrace();
			}
		}
		MessageDialog.getInstance().newLine();
		
		// Load time generator properties
		MessageDialog.getInstance().addMessage(loadingStep++ + ". Searching for time generators:");
		List<String> timePropertiesFiles = null;
		try {
			timePropertiesFiles = FileUtils.getFileNamesInDirectory(GeneralProperties.getInstance().getPathForTimeGenerators(), true);
		} catch (IOException e1) {
			throw new IOException("Cannot access time generator directory.");
		}
		for(String propertiesFile: timePropertiesFiles){
			MessageDialog.getInstance().addMessage("Loading time generator: " + propertiesFile.substring(propertiesFile.lastIndexOf('/') + 1) + "...   ");
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
		
		// Load data containers
		MessageDialog.getInstance().addMessage(loadingStep++ + ". Searching for data containers:");
		List<String> containerFiles = null;
		try {
			containerFiles = FileUtils.getFileNamesInDirectory(GeneralProperties.getInstance().getPathForDataContainers(), true);
		} catch (IOException e1) {
			throw new IOException("Cannot access data container directory.");
		}
		for(String containerFile: containerFiles){
			MessageDialog.getInstance().addMessage("Loading data container: " + containerFile.substring(containerFile.lastIndexOf('/') + 1) + "...   ");
			try{
				addCaseDataContainer(loadDataContainer(containerFile), false);
				MessageDialog.getInstance().addMessage("Done.");
			} catch(Exception e){
				MessageDialog.getInstance().addMessage("Error: "+e.getMessage());
			}
		}
		MessageDialog.getInstance().newLine();
		
		// Load custom transformer types
		MessageDialog.getInstance().addMessage(loadingStep++ + ". Searching for custom transformer types:");
		if(new File(GeneralProperties.getInstance().getPathForCustomTransformerTypes()).exists()){
			List<File> transformerTypeDirectories = null;
			try {
				transformerTypeDirectories = FileUtils.getSubdirectories(GeneralProperties.getInstance().getPathForCustomTransformerTypes());
			} catch (IOException e1) {
				throw new IOException("Cannot access custom transformer type directory.");
			}
			for(File transformerTypeDirectory: transformerTypeDirectories){
				MessageDialog.getInstance().addMessage("Loading transformer type: " + transformerTypeDirectory.getName());
				try{
					loadCustomTransformerType(transformerTypeDirectory);
					MessageDialog.getInstance().addMessage("Done.");
				} catch(Exception e){
					MessageDialog.getInstance().addMessage("Error: "+e.getMessage());
				}
			}
			MessageDialog.getInstance().newLine();
			
			
			// Load custom transformers
			MessageDialog.getInstance().addMessage(loadingStep++ + ". Searching for custom transformers:");
			if (new File(GeneralProperties.getInstance().getPathForCustomTransformers()).exists()) {
				List<String> customTransformerFiles = null;
				try {
					customTransformerFiles = FileUtils.getFileNamesInDirectory(GeneralProperties.getInstance().getPathForCustomTransformers(), true);
				} catch (IOException e1) {
					throw new IOException("Cannot access custom transformer directory.");
				}
				for (String customTransformerFile : customTransformerFiles) {
					MessageDialog.getInstance().addMessage("Loading custom transformer: " + customTransformerFile.substring(customTransformerFile.lastIndexOf('/') + 1) + "...   ");
					try {
						addTransformer((AbstractTraceTransformer) TransformerFactory.loadCustomTransformer(customTransformerFile), false);
						MessageDialog.getInstance().addMessage("Done.");
					} catch (Exception e) {
						MessageDialog.getInstance().addMessage("Error: " + e.getMessage());
					}
				}
				MessageDialog.getInstance().newLine();
			} else {
				MessageDialog.getInstance().addMessage("No custom transformers found.");
				MessageDialog.getInstance().newLine();
			}
		} else {
			MessageDialog.getInstance().addMessage("No custom transformer types found.");
			MessageDialog.getInstance().newLine();
		}
		
		// Load transformers
		MessageDialog.getInstance().addMessage(loadingStep++ + ". Searching for transformers:");
		List<String> transformerFiles = null;
		try {
			transformerFiles = FileUtils.getFileNamesInDirectory(GeneralProperties.getInstance().getPathForTransformers(), true);
		} catch (IOException e1) {
			throw new IOException("Cannot access transformer directory.");
		}
		for(String transformerFile: transformerFiles){
			MessageDialog.getInstance().addMessage("Loading transformer: " + transformerFile.substring(transformerFile.lastIndexOf('/') + 1) + "...   ");
			try{
				addTransformer((AbstractTraceTransformer) TransformerFactory.loadTransformer(transformerFile), false);
				MessageDialog.getInstance().addMessage("Done.");
			} catch(Exception e){
				MessageDialog.getInstance().addMessage("Error: "+e.getMessage());
			}
		}
		MessageDialog.getInstance().newLine();
		
		// Load Simulations
		MessageDialog.getInstance().addMessage(loadingStep++ + ". Searching for simulations:");
		List<String> simulationFiles= null;
		try {
			simulationFiles = FileUtils.getFileNamesInDirectory(GeneralProperties.getInstance().getPathForSimulations(), true);
		} catch (IOException e1) {
			throw new IOException("Cannot access simulation directory.");
		}
		for(String simulationFile: simulationFiles){
			MessageDialog.getInstance().addMessage("Loading simulation: " + simulationFile.substring(simulationFile.lastIndexOf('/') + 1) + "...   ");
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
			ACModel newModel = null;
			if (testProperties.getType().equals(ACModelType.ACL)) {
				ACLModelProperties aclProperties = new ACLModelProperties();
				aclProperties.load(acFile);
				newModel = new ACLModel(aclProperties);
			} else {
				RBACModelProperties rbacProperties = new RBACModelProperties();
				rbacProperties.load(acFile);
				newModel = new RBACModel(rbacProperties);
			}
			try {
				newModel.checkValidity();
			} catch (ACMValidationException e) {
				throw new ParameterException(e.getMessage());
			}
			return newModel;
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
	
	private void loadCustomTransformerType(File transformerTypeDirectory) throws ParameterException, IOException{
		
		String transformerName = transformerTypeDirectory.getName();
		// Check if all necessary files are there
		List<File> classFiles = null;
		classFiles = FileUtils.getFilesInDirectory(transformerTypeDirectory.getAbsolutePath(), "class");

		if(classFiles.size() != 2)
			throw new ParameterException("Required files missing for custom transformer type\""+transformerName+"\"");
		
		// Try to load the class files
		// 1. Check if there is a transformer class
		File transformerClassFile = null;
		File transformerPanelClassFile = null;
		if(FileUtils.getName(classFiles.get(0)).equals(transformerName)){
			transformerClassFile = classFiles.get(0);
			transformerPanelClassFile = classFiles.get(1);
		} else if(FileUtils.getName(classFiles.get(1)).equals(transformerName)){
			transformerClassFile = classFiles.get(1);
			transformerPanelClassFile = classFiles.get(0);
		}
		if(transformerClassFile == null)
			throw new ParameterException("Cannot find transformer class file.");
		
		
		// 2. Try to load the transformer class

		// Prepare class loader
		ClassLoader loader = null;
		try {
		    URL url = transformerTypeDirectory.toURI().toURL();
		    URL[] urls = new URL[]{url};
		    loader = new URLClassLoader(urls);
		} catch (MalformedURLException e) {
			throw new ParameterException("Error while preparing class loader: " + e.getMessage());
		}
		
		// Load transformer class
		Class<?> transformerClass = null;
		try {
			transformerClass = loader.loadClass(CUSTOM_TRANSFORMER_PACKAGE+FileUtils.getName(transformerClassFile));
		}catch(NoClassDefFoundError e) {
			throw new ParameterException("Class loader exception, cannot find class: " + e.getMessage());
		}catch (ClassNotFoundException e) {
			throw new ParameterException("Class loader exception, cannot load class: " + e.getMessage());
		}catch(Exception e){
			throw new ParameterException("Class loader exception: " + e.getMessage());
		}
		if(!AbstractTraceTransformer.class.isAssignableFrom(transformerClass))
			throw new ParameterException("Wrong type of loaded transformer class: " + transformerClass.getName());
		// Try if class can be instantiated
		try{
			transformerClass.newInstance();
		} catch(Exception e){
			throw new ParameterException("Transformer class cannot be instantiated: " + transformerClass.getName());
		}
		
		// Load transformer panel class
		Class<?> transformerPanelClass = null;
		try {
			transformerPanelClass = loader.loadClass(CUSTOM_TRANSFORMER_PANEL_PACKAGE+FileUtils.getName(transformerPanelClassFile));
		}catch(NoClassDefFoundError e) {
			throw new ParameterException("Class loader exception, cannot find class: " + e.getMessage());
		}catch (ClassNotFoundException e) {
			throw new ParameterException("Class loader exception, cannot load class: " + e.getMessage());
		}catch(Exception e){
			throw new ParameterException("Class loader exception: " + e.getMessage());
		}
		if (!AbstractTransformerPanel.class.isAssignableFrom(transformerPanelClass))
			throw new ParameterException("Wrong type of loaded transformer panel class: " + transformerPanelClass.getName());

		customTransformerTypes.add(new TransformerType(transformerClass, transformerPanelClass));

		// Dateiname = Kassenname
		// Statische Methode gibt die Bezeichnung des Transformers zurück
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
		
		AbstractLogFormat logFormat= LogFormatFactory.getFormat(properties.getLogFormatType());
		String fileName = properties.getFileName();
		
		//Log Generator
		TraceLogGenerator logGenerator = new TraceLogGenerator(logFormat, GeneralProperties.getInstance().getSimulationDirectory(), fileName);
		logGenerator.setEventHandling(properties.getEventHandling());
		
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
			storeACModel(acModel);
		}
		for(Context context: contexts.values()){
			storeContext(context);
		}
		for(CaseDataContainer dataContainer: caseDataContainers.values()){
			storeCaseDataContainer(dataContainer);
		}
		for(AbstractTraceTransformer transformer: transformers.values()){
			storeTransformer(transformer);
		}
		for(CaseTimeGenerator timeGenerator: caseTimeGenerators.values()){
			storeCaseTimeGenerator(timeGenerator);
		}
		for(Simulation simulation: simulations.values()){
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
	public void addPetriNet(PTNet petriNet) throws ParameterException, IOException, SerializationException, PropertyException{
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
	public void addPetriNet(PTNet petriNet, boolean storeToFile) throws ParameterException, IOException, SerializationException, PropertyException{
		Validate.notNull(petriNet);
		Validate.notNull(storeToFile);
		petriNets.put(petriNet.getName(), petriNet);
		if(storeToFile){
			PNSerialization.serialize(petriNet, PNSerializationFormat.PNML, GeneralProperties.getInstance().getPathForPetriNets(), petriNet.getName());
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
		String generatorName = GeneralProperties.getInstance().getPathForTimeGenerators()+timeGenerator.getName();
		timeGenerator.getProperties().store(generatorName);
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
	
	
	//------- Adding and removing transformers -------------------------------------------------------------------------
	
	public Set<TransformerType> getTransformerTypes(){
		return Collections.unmodifiableSet(transformerTypes);
	}
	
	public Set<TransformerType> getCustomTransformerTypes(){
		return Collections.unmodifiableSet(customTransformerTypes);
	}
	
	public List<TransformerType> getAllTransformerTypes(){
		List<TransformerType> result = new ArrayList<TransformerType>();
		result.addAll(getTransformerTypes());
		result.addAll(getCustomTransformerTypes());
		Collections.sort(result);
		return result;
	}
	
	public boolean isCustomTransformer(AbstractTraceTransformer transformer){
		for(TransformerType customTransformerType: customTransformerTypes){
			if(customTransformerType.getTransformerClass().isAssignableFrom(transformer.getClass())){
				return true;
			}
		}
		return false;
		//TODO: Correct?
	}
	
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
	 * Stores the given transformer.<br>
	 * Customized transformers are serialized into the directory for customized transformers.<br>
	 * Built-In transformers are stored in form of a property-file in the simulation directory.<br>
	 * The transformer name will be used as file name.
	 * @param transformer The transformer to store.
	 * @throws ParameterException if the given transformer is <code>null</code> or invalid.
	 * @throws IOException if the transformer cannot be stored due to an I/O Error.
	 * @throws PropertyException if the transformer cannot be stored due to an error during property extraction.
	 */
	public void storeTransformer(AbstractTraceTransformer transformer) throws ParameterException, IOException, PropertyException{
		Validate.notNull(transformer);
		if(isCustomTransformer(transformer)){
			String outFileString = GeneralProperties.getInstance().getPathForCustomTransformers()+transformer.getName();
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFileString));
			out.writeObject(transformer);
			out.close();
		} else {
			((PropertyAwareTransformer) transformer).getProperties().store(GeneralProperties.getInstance().getPathForTransformers()+transformer.getName());
		}
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
		AbstractTraceTransformer removedTransformer = getTransformer(name);
		if(transformers.remove(name) != null){
			if(isCustomTransformer(removedTransformer)){
				FileUtils.deleteFile(GeneralProperties.getInstance().getPathForCustomTransformers()+name);
			} else {
				FileUtils.deleteFile(GeneralProperties.getInstance().getPathForTransformers()+name);
			}
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
	
	public static void main(String[] args) {
		SimulationComponents.getInstance();
	}
}
