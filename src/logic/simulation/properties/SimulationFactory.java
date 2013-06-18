package logic.simulation.properties;

import java.io.IOException;

import logic.simulation.Simulation;
import properties.PropertyException;
import validate.ParameterException;

public class SimulationFactory {
	
	public static Simulation createSimulation(SimulationProperties properties) throws ParameterException, PropertyException{
		Simulation simulation = new Simulation();

		//TODO
		
//		
//		LogGenerator logGenerator = null;
//		try {
//			logGenerator = new TraceLogGenerator(getLogFormatType(), txtLogName.getText(), txtLogPath.getText());
//		} catch (ParameterException e3) {
//			JOptionPane.showMessageDialog(SimulationDialog.this, "Parameter exception on creating new trace log generator:\n" + e3.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
//			return;
//		} catch (PerspectiveException e3) {
//			JOptionPane.showMessageDialog(SimulationDialog.this, "Chosen log format is not compatible with the log generator:\n" + e3.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
//			return;
//		} catch (IOException e3) {
//			JOptionPane.showMessageDialog(SimulationDialog.this, "Exception during target file creation, please check permissions:\n" + e3.getMessage(), "I/O Error", JOptionPane.ERROR_MESSAGE);
//			return;
//		}
//		try {
//			simulation.setLogGenerator(logGenerator);
//		} catch (ConfigurationException e4) {
//			JOptionPane.showMessageDialog(SimulationDialog.this, "Exception on assigning log generator to simulation:\n" + e4.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
//			return;
//		} catch (ParameterException e4) {
//			JOptionPane.showMessageDialog(SimulationDialog.this, "Exception on assigning log generator to simulation:\n" + e4.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
//			return;
//		}
//		
//		if(comboEntryGenerator.getSelectedIndex() == 0){
//			if(!addNewSimpleLogEntryGenerator())
//				return;
//		} else if(comboEntryGenerator.getSelectedIndex() == 1){
//			if(!addNewDetailedLogEntryGenerator())
//				return;
//		}
//		if(comboTimeGenerator.getSelectedItem() == null){
//			JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot create simulation without case time generator!", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
//			return;
//		}
//		
//		try {
//			simulation.setCaseTimeGenerator(SimulationComponents.getInstance().getCaseTimeGeneratorProperties(comboTimeGenerator.getSelectedItem().toString()));
//		} catch (Exception e1) {
//			JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception on assigning case time generator to simulation:\n" + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
//			return;
//		}
		
		return simulation;
	}
	
	public static SimulationProperties parseProperties(String propertyFile) throws IOException, ParameterException, PropertyException{
		return new SimulationProperties(propertyFile);
	}
	
	public static Simulation parseSimulation(String propertyFile) throws IOException, ParameterException, PropertyException{
		return createSimulation(parseProperties(propertyFile));
	}
	
}
