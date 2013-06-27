package gui.dialog.datausage;

import gui.dialog.ValueChooserDialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.ParameterException;

import logic.generator.Context;

public class DataUsageDialog extends JDialog {

	private static final long serialVersionUID = 9017322681121907900L;
	
	private final JPanel contentPanel = new JPanel();
	private JList activityList = null;
	private DataUsageTable dataUsageTable = null;
	private JButton btnAddDataUsage = null;
	private Context context = null;


	public DataUsageDialog(Window owner, Context context) {
		super(owner);
		this.context = context;
		setResizable(false);
		setTitle("Activity Data Usage");
		setBounds(100, 100, 445, 320);
		setModal(true);
		setLocationRelativeTo(owner);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setBounds(20, 40, 150, 240);
			contentPanel.add(scrollPane);
			{
				scrollPane.setViewportView(getActivityList());
			}
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setBounds(185, 40, 240, 205);
			contentPanel.add(scrollPane);
			{
				scrollPane.setViewportView(getDataUsageTable());
				updateDataUsageList(getActivityList().getSelectedValue().toString());
			}
		}
		{
			JLabel lblActivities = new JLabel("Activities:");
			lblActivities.setBounds(20, 20, 86, 16);
			contentPanel.add(lblActivities);
		}
		{
			btnAddDataUsage = new JButton("Add");
			btnAddDataUsage.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					List<String> attributes = ValueChooserDialog.showDialog(DataUsageDialog.this, "Add new attribute to activity", DataUsageDialog.this.context.getAttributes());
					if(attributes != null && !attributes.isEmpty()){
						dataUsageTable.getModel().addElement(attributes.get(0));
					}
					dataUsageTable.repaint();
				}
			});
			btnAddDataUsage.setBounds(185, 250, 80, 29);
			contentPanel.add(btnAddDataUsage);
		}
		{
			JLabel lblDataUsage = new JLabel("Attributes and usage modes:");
			lblDataUsage.setBounds(185, 20, 181, 16);
			contentPanel.add(lblDataUsage);
		}
		{
			JButton btnRemove = new JButton("Remove");
			btnRemove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(dataUsageTable.getSelectedRow() >= 0){
						String attribute = dataUsageTable.getModel().getValueAt(dataUsageTable.getSelectedRow(), 0).toString();
						dataUsageTable.getModel().removeElement(attribute);
						try {
							DataUsageDialog.this.context.removeDataUsageFor(activityList.getSelectedValue().toString(), attribute);
						} catch (CompatibilityException e1) {
							e1.printStackTrace();
						} catch (ParameterException e1) {
							e1.printStackTrace();
						}
					}
				}
			});
			btnRemove.setBounds(265, 250, 80, 29);
			contentPanel.add(btnRemove);
		}
		{
			JButton btnDone = new JButton("Done");
			btnDone.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					DataUsageDialog.this.dispose();
				}
			});
			btnDone.setBounds(345, 251, 80, 29);
			contentPanel.add(btnDone);
		}
		setVisible(true);
	}
	
	private JList getActivityList(){
		if(activityList == null){
			activityList = new JList();
			activityList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			activityList.setCellRenderer(new DataUsageListRenderer());
			activityList.setFixedCellHeight(20);
			activityList.setVisibleRowCount(10);
			activityList.setBorder(null);
			
			DefaultListModel listModel = new DefaultListModel();
			for(String activity: context.getActivities())
				listModel.addElement(activity);
			activityList.setModel(listModel);
			
			activityList.setSelectedIndex(0);
			
			activityList.addListSelectionListener(
	        		new ListSelectionListener(){
	        			public void valueChanged(ListSelectionEvent e) {
	        			    if ((e.getValueIsAdjusting() == false) && (activityList.getSelectedValue() != null)) {
	        			    	updateDataUsageList(activityList.getSelectedValue().toString());
	        			    }
	        			}
	        		}
	        );
		}
		return activityList;
	}
	
	private JTable getDataUsageTable(){
		if(dataUsageTable == null){
			dataUsageTable = new DataUsageTable(context);
			dataUsageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			dataUsageTable.setBorder(null);
			
//			dataUsageList.addListSelectionListener(
//	        		new ListSelectionListener(){
//	        			public void valueChanged(ListSelectionEvent e) {
//	        			    if ((e.getValueIsAdjusting() == false) && (dataUsageList.getSelectedValue() != null)) {
//	        			    }
//	        			}
//	        		}
//	        );
		}
		return dataUsageTable;
	}
	
	private void updateDataUsageList(String selectedActivity){
		try {
			dataUsageTable.update(selectedActivity);
		} catch (CompatibilityException e) {
			e.printStackTrace();
		} catch (ParameterException e) {
			e.printStackTrace();
		}
	}
	
	public class DataUsageListRenderer extends JLabel implements ListCellRenderer {
		public static final long serialVersionUID = 1L;
		
		public DataUsageListRenderer() {
			setOpaque(true);
			setHorizontalAlignment(LEFT);
			setVerticalAlignment(CENTER);
			this.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			
			boolean activityHasDataUsage = false;
			try {
				activityHasDataUsage = !context.getDataUsageFor(value.toString()).isEmpty();
			} catch (Exception e) {}

			if(activityHasDataUsage){
				setText("<html><b>"+(String) value+"");
			} else {
				setText((String) value);
			}
			setToolTipText((String) value);
			
			if (isSelected) {
				setBackground(new Color(10,100,200));
				setForeground(new Color(0,0,0));
			} else {
				if((index%2)==0){
					setBackground(list.getBackground());
					setForeground(list.getForeground());
				} else {
					setBackground(new Color(230,230,230));
					setForeground(list.getForeground());
				}
				if(activityHasDataUsage){
					setBackground(new Color(201,233,255));
				}
			}
					
			return this;

		}

	}
	
	public static void showDialog(Window owner, Context context) throws ParameterException{
		new DataUsageDialog(owner, context);
	}

}
