package gui.permission;

import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

import types.DataUsage;


public class ObjectPermissionListenerSupport {
	
	private List<ObjectPermissionItemListener> listeners = new ArrayList<ObjectPermissionItemListener>();
	
	public void addPermissionItemListener(ObjectPermissionItemListener listener){
		this.listeners.add(listener);
	}
	
	public void removePermissionItemListener(ObjectPermissionItemListener listener){
		this.listeners.remove(listener);
	}
	
	public void firePermissionChangedEvent(ItemEvent itemEvent, DataUsage dataUsageMode){
		firePermissionChangedEvent(itemEvent, dataUsageMode, null);
	}
	
	public void firePermissionChangedEvent(ItemEvent itemEvent, DataUsage dataUsageMode, String attribute){
		ObjectPermissionItemEvent event = new ObjectPermissionItemEvent(itemEvent, ((ObjectPermissionCheckBox) itemEvent.getSource()).getDataUsageMode());
		event.setAttribute(attribute);
		firePermissionChangedEvent(event);
	}
	
	public void firePermissionChangedEvent(ObjectPermissionItemEvent event){
		for(ObjectPermissionItemListener listener: listeners){
			listener.permissionChanged(event);
		}
	}

}
