package de.uni.freiburg.iig.telematik.secsy.gui.permission;

import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;

import de.invation.code.toval.types.DataUsage;



public class ObjectPermissionItemEvent extends ItemEvent {
	
	private static final long serialVersionUID = 6937079222927014550L;
	
	private DataUsage dataUsageMode = null;
	private String attribute = null;
	
	public ObjectPermissionItemEvent(ItemEvent itemEvent, DataUsage dataUsageMode){
		super((ItemSelectable) itemEvent.getSource(), itemEvent.getID(), itemEvent.getItem(), itemEvent.getStateChange());
		this.dataUsageMode = dataUsageMode;
	}

	public DataUsage getDataUsageMode() {
		return dataUsageMode;
	}
	
	public void setAttribute(String attribute){
		this.attribute = attribute;
	}
	
	public String getAttribute(){
		return attribute;
	}

	
}
