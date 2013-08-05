package de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties;

public enum EventHandling {
	START,  // Simulation creates only a start event for each log-entry.
	END,    // Simulation creates only an end event for each log-entry
	BOTH;   // Simulation creates both, a start and end-event for each log-entry.
}
