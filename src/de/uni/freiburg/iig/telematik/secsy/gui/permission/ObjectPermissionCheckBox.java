package de.uni.freiburg.iig.telematik.secsy.gui.permission;

import javax.swing.JCheckBox;

import de.invation.code.toval.types.DataUsage;



public class ObjectPermissionCheckBox extends JCheckBox {
	
	private static final long serialVersionUID = -3383505423721436005L;
	
	private DataUsage dataUsageMode;
	
	public ObjectPermissionCheckBox(DataUsage dataUsageMode){
		this.dataUsageMode = dataUsageMode;
		setSelected(false);
		setToolTipText(dataUsageMode.toString());
	}
	
	public DataUsage getDataUsageMode(){
		return dataUsageMode;
	}

}
