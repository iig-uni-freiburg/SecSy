package de.uni.freiburg.iig.telematik.secsy.gui;

public class Hints {
	
	public static final String hintMXMLFormat = 			"<html>The generated logfile will be formatted in the MXML style.<br>" +
			  												"MXML is an XML-based standard log format for process mining.<br>" +
			  												"or further information, please visit <a href=\"http://www.processmining.org/logs/mxml\">processmining.org</a></html>";
	
	public static final String hintPLAINFormat = 			"<html>The generated logfile will only contain plain text.<br>" +
												 			"In plain text logs, each line stands for a process trace<br>" +
												 			"and just contains TAB-separated activity names.</html>";
	
	public static final String hintSIMPLEGeneration = 		"<html>The simulation will only consider the control flow of a process.<br>" +
													  		"The generated log file will only contain timing information about cases and activities<br>" +
													  		"and the names of process activities.</html>";
	
	public static final String hintDETAILEDGeneration = 	"<html>Besides the control flow of a process, the simulation will also consider<br>" + 
				      										"resources and data usage. For this, the additional components are required:<br>" +
				      										"<ul><li>Context: Contains information about the organizational structure related to the process.<br>" +
				      										"This means persons, organizational structures like role lattices as well as<br>" +
				      										"data elements used in process activities and rights of persons to execute activities or<br>" +
				      										"access values of data elements.</li><br>" +
				      										"<li>Data Container: Generates values for data elements.<br>" +
				      										"To assure consistent simulation, values are generated/stored case-wise.</li></ul></html>";
	
	public static final String hintContext = 				"<html>A context contains information about the organizational structure related to the process.<br>" +
											 				"In detail, it covers the following aspects:<br>" +
											 				"<ul><li>Ressources: Process activities, executing sujects and dtaa elements involved in process execution.</li>" +
											 				"<li>Data Usage: Data elements, used by process activities.</li>" +
											 				"<li>Routing Constraints: Cosntraints on the value of data elements required for the execution of specific process activities</li>" +
											 				"<li>Authorization Constraints: Right of subjects to execute process activities and to obtain values of data elements.<br></li>" +
											 				"rights are specified with the help of an Access control List (ACL) or an RBAC-model (role Based Access Control).</ul></html>";
	
	public static final String hintDataContainer = 			"<html>A data container generates values for data elements.<br>" +
				 											"For each process case, the values for data elements used within the case are generated<br>" +
				 											"with the help of configurable value generators and stored case-wise.</html>";
	
	public static final String hintTimeGenerator = 			"<html>A time generator specifies all timing-related simulation properties.<br>" +
															"In details, it overs the following aspects:<br>" +
															"<ul><li>Start time of the simulation procedure.</li>" +
															"<li>Process cases to generate per day.</li>" +
															"<li>Weekdays and working hours.</li>" +
															"<li>Duration of process activities.</li>" +
															"<li>Delay between process activities.</li>" +
															"<li>Random deviations for the above properties.</li></ul><html>";
	
	public static final String hintDefaultDuration = 		"<html>The default duration applies to all activities for which no individual duration is specified.</html>";

	public static final String hintDefaultDelay = 			"<html>The default delay applies to all activities for which no individual delay is specified.</html>";
	
	public static final String hintRandomizedDelay = 		"<html>The randomized delay applies to all activities for which either no individual delay is specified<br>" +
															"or no deviation is specified. For individual delays without deviation, use a deviation value of zero.</html>";

	public static final String hintRandomTraversal =		"<html>In random traversal mode, each enbled transition is chosen with the same probability.</html>";
	
	public static final String hintTransformerList =				"<html>Transformers are applied in the order they appear in the list.<br>" +
															"Note, that transformers may lock fields to ensure consistency and this may block" +
															"the appliance of succeeding transformers.</html>";
																		
}
