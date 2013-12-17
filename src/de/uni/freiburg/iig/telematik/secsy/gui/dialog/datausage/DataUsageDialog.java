package de.uni.freiburg.iig.telematik.secsy.gui.dialog.datausage;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.invation.code.toval.graphic.dialog.ValueChooserDialog;
import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.Context;


public class DataUsageDialog extends JDialog {

	private static final long serialVersionUID = 9017322681121907900L;
	private static final Dimension PREFERRED_SIZE_LEFT_PANEL = new Dimension(180,350);

	private JList activityList = null;
	private DataUsageTable dataUsageTable = null;
	private Context context = null;
	private JPanel contentPanel = new JPanel(new BorderLayout(5,0));
	private JButton addDUButton = null;
	private JButton removeDUButton = null;
	private JButton doneButton = null;

	public DataUsageDialog(Window owner, Context context) {
		super(owner);
		this.context = context;
		setTitle("Activity Data Usage");
		setModal(true);
		setUpGUI();
		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}
	
	private void setUpGUI(){
		setContentPane(contentPanel);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		JPanel leftPanel = new JPanel(new BorderLayout(0,5));
		leftPanel.setPreferredSize(PREFERRED_SIZE_LEFT_PANEL);
		leftPanel.setMinimumSize(PREFERRED_SIZE_LEFT_PANEL);
		JLabel lblActivities = new JLabel("Activities:");
		leftPanel.add(lblActivities, BorderLayout.PAGE_START);
		JScrollPane scrollPaneActivities = new JScrollPane(getActivityList());
		scrollPaneActivities.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		leftPanel.add(scrollPaneActivities, BorderLayout.CENTER);
		contentPanel.add(leftPanel, BorderLayout.LINE_START);
		
		JPanel rightPanel = new JPanel(new BorderLayout(0,5));
		JLabel lblDataUsage = new JLabel("Attributes and usage modes:");
		rightPanel.add(lblDataUsage, BorderLayout.PAGE_START);
		JScrollPane scrollPaneDataUsage = new JScrollPane(getDataUsageTable());
		scrollPaneDataUsage.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPaneDataUsage.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		rightPanel.add(scrollPaneDataUsage);
		updateDataUsageList(getActivityList().getSelectedValue().toString());
		JPanel buttonPanel = new JPanel();
		BoxLayout layout = new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS);
		buttonPanel.setLayout(layout);
		buttonPanel.add(getAddDUButton());
		buttonPanel.add(getRemoveDUButton());
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(getDoneButton());
		rightPanel.add(buttonPanel, BorderLayout.PAGE_END);
//		Dimension preferredSize = new Dimension(getActivityList().getPreferredSize().width + 10, getActivityList().getPreferredSize().height);
//		rightPanel.setPreferredSize(preferredSize);
//		rightPanel.setMinimumSize(preferredSize);
		contentPanel.add(rightPanel, BorderLayout.CENTER);
	}

	private JButton getAddDUButton(){
		if(addDUButton == null){
			addDUButton = new JButton("Add");
			addDUButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					List<String> attributes = null;
					try {
						attributes = ValueChooserDialog.showDialog(DataUsageDialog.this, "Add new attribute to activity", DataUsageDialog.this.context.getAttributes());
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(DataUsageDialog.this, "<html>Cannot launch value chooser dialog.<br>Reason: " + e1.getMessage() + "</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
					}
					if(attributes != null && !attributes.isEmpty()){
						dataUsageTable.getModel().addElement(attributes.get(0));
					}
					dataUsageTable.repaint();
				}
			});
		}
		return addDUButton;
	}
	
	private JButton getRemoveDUButton(){
		if(removeDUButton == null){
			removeDUButton = new JButton("Remove");
			removeDUButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeSelectedDataUsages();
				}
			});
		}
		return removeDUButton;
	}
	
	private JButton getDoneButton(){
		if(doneButton == null){
			doneButton = new JButton("Done");
			doneButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					DataUsageDialog.this.dispose();
				}
			});
		}
		return doneButton;
	}
	
	private void removeSelectedDataUsages(){
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
	        			    	getDataUsageTable().requestFocusInWindow();
	        			    }
	        			}
	        		}
	        );
			activityList.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
						removeSelectedDataUsages();
					}
				}
				
				@Override
				public void keyPressed(KeyEvent e) {}
			});
		}
		return activityList;
	}
	
	private DataUsageTable getDataUsageTable(){
		if(dataUsageTable == null){
			dataUsageTable = new DataUsageTable(context);
			dataUsageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			dataUsageTable.setBorder(null);
		}
		return dataUsageTable;
	}
	
	private void updateDataUsageList(String selectedActivity){
		try {
			getDataUsageTable().update(selectedActivity);
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

//			if(activityHasDataUsage){
//				setText("<html><b>"+(String) value+"");
//			} else {
//				setText((String) value);
//			}
			setText((String) value);
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
