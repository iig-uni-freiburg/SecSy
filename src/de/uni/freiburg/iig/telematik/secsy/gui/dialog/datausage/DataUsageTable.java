package de.uni.freiburg.iig.telematik.secsy.gui.dialog.datausage;


import java.awt.Component;
import java.awt.Dimension;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import de.invation.code.toval.types.DataUsage;
import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.SynthesisContext;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.acl.graphic.permission.ObjectPermissionItemEvent;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.acl.graphic.permission.ObjectPermissionItemListener;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.acl.graphic.permission.ObjectPermissionPanel;


public class DataUsageTable extends JTable implements ObjectPermissionItemListener{

	private static final long serialVersionUID = -1935993027476998583L;
	
	private final int HEADER_HEIGHT = 40;

	private SynthesisContext context = null;
	private String activity = null;
	
	public DataUsageTable(SynthesisContext context){
		super(new DataUsageTableModel(context));
		this.context = context;
		getModel().addPermissionItemListener(this);
		initialize();
	}
	
	protected void initialize(){
		setRowHeight(getModel().preferredCellSize().height);
		
		getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer());
		int minWidthAttributeColumn = getModel().getMinHeaderWidth(0);
		getColumnModel().getColumn(0).setWidth(minWidthAttributeColumn);
		getColumnModel().getColumn(0).setMinWidth(minWidthAttributeColumn);
		
		getColumnModel().getColumn(1).setCellEditor(new UsagTableCellEditor(new JCheckBox()));
		getColumnModel().getColumn(1).setCellRenderer(new UsageTableCellRenderer());
		int minWidthDUcolumn = Math.max(getModel().preferredCellSize().width, getModel().getMinHeaderWidth(1));
		getColumnModel().getColumn(1).setWidth(minWidthDUcolumn);
		getColumnModel().getColumn(1).setMinWidth(minWidthDUcolumn);
		
		int minHeight = HEADER_HEIGHT + getModel().getRowCount()*getRowHeight();
		setPreferredSize(new Dimension(minWidthAttributeColumn + minWidthDUcolumn, minHeight));
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setShowHorizontalLines(true);
	}
	
	
	
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}
	
	@Override
	public Dimension getMinimumSize(){
		return getPreferredSize();
	}

	@Override
	public DataUsageTableModel getModel(){
		return (DataUsageTableModel) super.getModel();
	}
	
	public void update(String activity) throws CompatibilityException, ParameterException{
		setModel(new DataUsageTableModel(context));
		getModel().addPermissionItemListener(this);
		initialize();
		
		this.activity = activity;
		Map<String, Set<DataUsage>> activityDataUsage = context.getDataUsageFor(activity);
		if(!activityDataUsage.isEmpty()){
			for(String attribute: activityDataUsage.keySet()){
				getModel().addElement(attribute, activityDataUsage.get(attribute));
			}
		}
		if(getRowCount() > 0){
			getSelectionModel().setSelectionInterval(0, 0);
			((ObjectPermissionPanel) getModel().getValueAt(0, 1)).requestFocus();
		}
	}

	@Override
	public void permissionChanged(ObjectPermissionItemEvent e) {
		try {
			if(e.getAttribute() != null){
				boolean usageAdded = ((JCheckBox) e.getSource()).isSelected();
				if(usageAdded){
					context.addDataUsageFor(activity, e.getAttribute(), e.getDataUsageMode());
				} else {
					context.removeDataUsageFor(activity, e.getAttribute(), e.getDataUsageMode());
				}
//				System.out.println(context.getDataUsageFor(activity, e.getAttribute()));
			}
		} catch (CompatibilityException e1) {
			e1.printStackTrace();
		} catch (ParameterException e1) {
			e1.printStackTrace();
		}
		
	}
	
	

	@Override
	public void tableChanged(TableModelEvent e) {
		int lastSelectedRow = getSelectedRow();
		super.tableChanged(e);
		if(e.getType() == TableModelEvent.INSERT){
			getSelectionModel().setSelectionInterval(e.getFirstRow(), e.getLastRow());
		}
		if(e.getType() == TableModelEvent.DELETE){
			getSelectionModel().setSelectionInterval(getRowCount()-1, getRowCount()-1);
		}
		if(e.getType() == TableModelEvent.UPDATE){
			getSelectionModel().setSelectionInterval(lastSelectedRow, lastSelectedRow);
		}
	}



	public class UsageTableCellRenderer implements TableCellRenderer{

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component comp = (Component) value;
			if (isSelected) {
				comp.setForeground(table.getSelectionForeground());
				comp.setBackground(table.getSelectionBackground());
			} else {
				comp.setForeground(table.getForeground());
				comp.setBackground(table.getBackground());
			}
			return comp;
		}
		
	}
	
	public class UsagTableCellEditor extends DefaultCellEditor {

		private static final long serialVersionUID = -5452183629220317768L;

		public UsagTableCellEditor(JCheckBox checkBox) {
			super(checkBox);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			return (Component) value;
		}
		
	}
	
}
