package gui.dialog.datausage;

import gui.permission.ObjectPermissionItemEvent;
import gui.permission.ObjectPermissionItemListener;
import gui.permission.ObjectPermissionListenerSupport;
import gui.permission.ObjectPermissionPanel;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import logic.generator.Context;
import accesscontrol.DataUsage;

public class DataUsageTableModel extends AbstractTableModel implements ObjectPermissionItemListener{
	
	private static final long serialVersionUID = -145830408957650293L;
	
	private List<ObjectPermissionPanel> dataUsagePanels = new ArrayList<ObjectPermissionPanel>();
	private List<String> attributes = new ArrayList<String>();
	private ObjectPermissionListenerSupport permissionListenerSupport = new ObjectPermissionListenerSupport();
	private Context context = null;
	
	public DataUsageTableModel(Context context){
		this.context = context;
	}
	
	public void addElements(Collection<String> attributes){
		for(String activity: attributes){
			addElement(activity);
		}
	}
	
	public Dimension preferredCellSize(){
		if(getRowCount() == 0){
			Dimension dim = new ObjectPermissionPanel("dummy", context.getValidUsageModes()).getPreferredSize();
			return dim;
		}
		return dataUsagePanels.get(0).getPreferredSize();
	}
	
	public void addElement(String attribute, Set<DataUsage> dataUsageModes){
		addElement(attribute);
		dataUsagePanels.get(dataUsagePanels.size()-1).setPermission(dataUsageModes);
	}
	
	public void addElement(String attribute){
		if(attributes.contains(attribute))
			return;
		ObjectPermissionPanel newPanel = new ObjectPermissionPanel(attribute, context.getValidUsageModes());
		newPanel.addPermissionItemListener(this);
		dataUsagePanels.add(newPanel);
		attributes.add(attribute);
		fireTableRowsInserted(attributes.size()-1, attributes.size()-1);
	}
	
	public void clear(){
		List<String> attributesToRemove = new ArrayList<String>();
		attributesToRemove.addAll(attributes);
		for(String attribute: attributesToRemove){
			removeElement(attribute);
		}
	}
	
	public void removeElement(String attribute){
		if(!attributes.contains(attribute))
			return;
		int index = attributes.indexOf(attribute);
		dataUsagePanels.get(index).removePermissionItemListener(this);
		dataUsagePanels.remove(index);
		attributes.remove(attribute);
		fireTableRowsDeleted(index, index);
	}
	
	public void addPermissionItemListener(ObjectPermissionItemListener listener){
		permissionListenerSupport.addPermissionItemListener(listener);
	}
	
	public void removePermissionItemListener(ObjectPermissionItemListener listener){
		permissionListenerSupport.removePermissionItemListener(listener);
	}

	@Override
	public void permissionChanged(ObjectPermissionItemEvent e) {
		permissionListenerSupport.firePermissionChangedEvent(e);
		fireTableDataChanged();
	}

	@Override
	public int getRowCount() {
		return attributes.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0){
			return attributes.get(rowIndex);
		}
		if(columnIndex == 1){
			return dataUsagePanels.get(rowIndex);
		}
		return null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
    	if(col == 1){
    		return true;
    	}
    	return false;
    }

}
