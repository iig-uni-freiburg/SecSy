package de.uni.freiburg.iig.telematik.secsy.gui.dialog;


import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.invation.code.toval.constraint.AbstractConstraint;
import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.secsy.gui.misc.CustomListRenderer;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.Context;

public class RoutingConstraintsDialog extends JDialog {

	private static final long serialVersionUID = 9017322681121907900L;
	
	private final JPanel contentPanel = new JPanel();
	
	private JList activityList = null;
	private JList constraintList = null;
	
	private DefaultListModel activityListModel = new DefaultListModel();
	private DefaultListModel constraintListModel = new DefaultListModel();
	
	private JButton btnAddConstraint = null;
	
	private Context context = null;
	private List<AbstractConstraint<?>> constraints = new ArrayList<AbstractConstraint<?>>();


	public RoutingConstraintsDialog(Window owner, Context context) {
		super(owner);
		this.context = context;
		setResizable(false);
		setTitle("Activity Data Usage");
		setBounds(100, 100, 445, 362);
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
				scrollPane.setViewportView(getConstraintList());
				updateConstraintList();
			}
		}
		{
			JLabel lblActivities = new JLabel("Activities:");
			lblActivities.setBounds(20, 20, 86, 16);
			contentPanel.add(lblActivities);
		}
		{
			btnAddConstraint = new JButton("Add");
			btnAddConstraint.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String activity = activityList.getSelectedValue().toString();
					Set<String> activityAttributes = null;
					try{
						activityAttributes = RoutingConstraintsDialog.this.context.getDataUsageFor(activity).keySet();
						if(activityAttributes.isEmpty()){
							JOptionPane.showMessageDialog(RoutingConstraintsDialog.this, "Cannot add constraints to activities without data usage.", "Missing data usage", JOptionPane.ERROR_MESSAGE);
							return;
						}
					} catch(ParameterException ex){
						JOptionPane.showMessageDialog(RoutingConstraintsDialog.this, "Cannot extract data usage information for activity \"" + activity + "\".", "Internal Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					List<String> chosenAttributes = ValueChooserDialog.showDialog(RoutingConstraintsDialog.this, "Choose attribute for new constraint", activityAttributes);
					if(chosenAttributes != null && !chosenAttributes.isEmpty()){
						AbstractConstraint<?> newConstraint = ConstraintDialog.showDialog(RoutingConstraintsDialog.this, chosenAttributes.get(0), false);
						if(newConstraint != null){
							try {
								RoutingConstraintsDialog.this.context.addRoutingConstraint(activity, newConstraint);
								updateConstraintList();
							} catch (CompatibilityException e1) {
								JOptionPane.showMessageDialog(RoutingConstraintsDialog.this, e1.getMessage(), "Incompatible routing constraint", JOptionPane.ERROR_MESSAGE);
								return;
							} catch (ParameterException e1) {
								JOptionPane.showMessageDialog(RoutingConstraintsDialog.this, e1.getMessage(), "Invalid Argument", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}
					
				}
			});
			btnAddConstraint.setBounds(185, 250, 80, 29);
			contentPanel.add(btnAddConstraint);
		}
		{
			JLabel lblDataUsage = new JLabel("Constraints:");
			lblDataUsage.setBounds(185, 20, 181, 16);
			contentPanel.add(lblDataUsage);
		}
		{
			JButton btnRemove = new JButton("Remove");
			btnRemove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String activity = activityList.getSelectedValue().toString();
					if(constraintList.getSelectedValue() != null){
						try {
							RoutingConstraintsDialog.this.context.removeRoutingConstraint(activity, constraints.get(constraintList.getSelectedIndex()));
						} catch (ParameterException e1) {
							JOptionPane.showMessageDialog(RoutingConstraintsDialog.this, "Cannot remove constraint from context.", "Internal Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						updateConstraintList();
					}
				}
			});
			btnRemove.setBounds(345, 250, 80, 29);
			contentPanel.add(btnRemove);
		}
		{
			JButton btnDone = new JButton("Done");
			btnDone.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					RoutingConstraintsDialog.this.dispose();
				}
			});
			btnDone.setBounds(185, 305, 80, 29);
			contentPanel.add(btnDone);
		}
		{
			JButton btnEdit = new JButton("Edit");
			btnEdit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(constraintList.getSelectedValue() != null){
						ConstraintDialog.showDialog(RoutingConstraintsDialog.this, constraints.get(constraintList.getSelectedIndex()), false);
						updateConstraintList();
					}
				}
			});
			btnEdit.setBounds(265, 250, 80, 29);
			contentPanel.add(btnEdit);
		}
		{
			JSeparator separator = new JSeparator();
			separator.setBounds(0, 290, 450, 12);
			contentPanel.add(separator);
		}
		setVisible(true);
	}
	
	private JList getActivityList(){
		if(activityList == null){
			activityList = new JList(activityListModel);
			activityList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			activityList.setCellRenderer(new CustomListRenderer());
			activityList.setFixedCellHeight(20);
			activityList.setVisibleRowCount(10);
			activityList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			activityList.setBorder(null);
			
			for(String activity: context.getActivities()){
				activityListModel.addElement(activity);
			}
			
			activityList.setSelectedIndex(0);
			
			activityList.addListSelectionListener(
	        		new ListSelectionListener(){
	        			public void valueChanged(ListSelectionEvent e) {
	        			    if ((e.getValueIsAdjusting() == false) && (activityList.getSelectedValue() != null)) {
	        			    	updateConstraintList();
	        			    }
	        			}
	        		}
	        );
		}
		return activityList;
	}
	
	private JList getConstraintList(){
		if(constraintList == null){
			constraintList = new JList(constraintListModel);
			constraintList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			constraintList.setCellRenderer(new CustomListRenderer());
			constraintList.setFixedCellHeight(20);
			constraintList.setVisibleRowCount(10);
			constraintList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			constraintList.setBorder(null);
		}
		return constraintList;
	}
	
	private void updateConstraintList(){
		constraints.clear();
		constraintListModel.clear();
		String activity = activityList.getSelectedValue().toString();
		try{
			if(activity != null){
				if(context.hasRoutingConstraints(activity)){
					constraints.addAll(context.getRoutingConstraints(activity));
				}
			}
		} catch(ParameterException e){
			e.printStackTrace();
		}
		for(AbstractConstraint<?> constraint: constraints){
			constraintListModel.addElement(constraint.toString());
		}
		if(!constraintListModel.isEmpty())
			constraintList.setSelectedIndex(0);
	}
	
	public static void showDialog(Window owner, Context context) throws ParameterException{
		new RoutingConstraintsDialog(owner, context);
	}

}
