package gui.dialog;
import gui.misc.CustomListRenderer;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;


public class ValueEditingDialog extends AbstractSimulationDialog {
	
	private static final long serialVersionUID = 2306027725394345926L;
	
	private JList listValues = null;
	private DefaultListModel listValueModel = null;
	private JButton btnAdd = null;
	private JButton btnRemove = null;
	
	private Integer selectionMode = null;
	
	private String title = null;
	

	/**
	 * @wbp.parser.constructor
	 */
	public ValueEditingDialog(Window owner, String title) {
		super(owner, new Object[]{title});
	}
	
	public ValueEditingDialog(Window owner, String title, Collection<String> initialValues) {
		super(owner, new Object[]{title, initialValues});
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void initialize(Object... parameters) {
		listValueModel = new DefaultListModel();
		setDialogObject(new HashSet<String>());
		title = (String) parameters[0];
		if(parameters.length > 1){
			getDialogObject().addAll((Collection<String>) parameters[1]);
		}
		selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
	}
	
	@Override
	protected void setTitle() {
		setTitle(title);
	}

	@Override
	protected void setBounds() {
		setBounds(100, 100, 221, 282);
	}

	@Override
	protected void addComponents(){
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(20, 20, 180, 160);
		mainPanel().add(scrollPane);
		scrollPane.setViewportView(getValueList());
		
		mainPanel().add(getButtonAdd());
		mainPanel().add(getButtonRemove());
	}
	
	
	
	private JList getValueList(){
		if(listValues == null){
			listValues = new JList(listValueModel);
			listValues.setCellRenderer(new CustomListRenderer());
			listValues.setFixedCellHeight(20);
			listValues.setVisibleRowCount(10);
			listValues.getSelectionModel().setSelectionMode(selectionMode);
			listValues.setBorder(null);
			listValues.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_DELETE){
						removeSelectedItems();
					}
				}
				
				@Override
				public void keyPressed(KeyEvent e) {}
			});
			updateValueList();
		}
		return listValues;
	}
	
	private JButton getButtonAdd(){
		if(btnAdd == null){
			btnAdd = new JButton("Add...");
			btnAdd.setBounds(20, 185, 90, 29);
			btnAdd.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					List<String> newValues = DefineGenerateDialog.showDialog(ValueEditingDialog.this, "Add new values");
					if(newValues != null && !newValues.isEmpty()){
						getDialogObject().addAll(newValues);
						updateValueList();
					}
				}
			});
			btnAdd.setActionCommand("Add");
		}
		return btnAdd;
	}
	
	private JButton getButtonRemove(){
		if(btnRemove == null){
			btnRemove = new JButton("Remove");
			btnRemove.setBounds(110, 185, 90, 29);
			btnRemove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeSelectedItems();
				}
			});
			btnRemove.setActionCommand("Remove");
		}
		return btnRemove;
	}
	
	private void removeSelectedItems(){
		if(listValues.getSelectedValues() == null)
			return;
		for(Object selectedObject: listValues.getSelectedValues()){
			getDialogObject().remove(selectedObject.toString());
		}
		updateValueList();
	}
	
	private void updateValueList(){
		listValueModel.clear();
		for(String value: getDialogObject()){
			listValueModel.addElement(value);
		}
	}

	@Override
	protected void okProcedure() {
		super.okProcedure();
	}

	@Override
	protected void closingProcedure() {
		setDialogObject(null);
		super.closingProcedure();
	}

	public static Set<String> showDialog(Window owner, String title){
		ValueEditingDialog editingDialog = new ValueEditingDialog(owner, title);
		return editingDialog.getDialogObject();
	}
	
	public static Set<String> showDialog(Window owner, String title, Collection<String> values){
		ValueEditingDialog editingDialog = new ValueEditingDialog(owner, title, values);
		return editingDialog.getDialogObject();
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected Set<String> getDialogObject() {
		return (Set<String>) super.getDialogObject();
	}

	public static void main(String[] args) {
		System.out.println(ValueEditingDialog.showDialog(null, "dff"));
	}
}
