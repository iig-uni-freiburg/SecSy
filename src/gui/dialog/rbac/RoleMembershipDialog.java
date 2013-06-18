package gui.dialog.rbac;

import gui.dialog.ValueChooserDialog;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import misc.ArrayUtils;
import validate.CompatibilityException;
import validate.ParameterException;
import accesscontrol.RBACModel;

public class RoleMembershipDialog extends JDialog {

	private static final long serialVersionUID = 9017322681121907900L;
	
	private final JPanel contentPanel = new JPanel();
	private JList subjectList = null;
	private JList roleList = null;
	private DefaultListModel roleListModel = new DefaultListModel();
	private JButton btnAddDataUsage = null;
	
	private RBACModel rbacModel = null;
	
	public RoleMembershipDialog(Window owner, RBACModel rbacModel) {
		super(owner);
		this.rbacModel = rbacModel;
		setResizable(false);
		setTitle("Role Membership");
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
				scrollPane.setViewportView(getSubjectList());
			}
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setBounds(185, 40, 240, 205);
			contentPanel.add(scrollPane);
			{
				scrollPane.setViewportView(getRoleList());
				updateRoleList();
			}
		}
		{
			JLabel lblActivities = new JLabel("Subjects:");
			lblActivities.setBounds(20, 20, 86, 16);
			contentPanel.add(lblActivities);
		}
		{
			btnAddDataUsage = new JButton("Add");
			btnAddDataUsage.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					List<String> roles = ValueChooserDialog.showDialog(RoleMembershipDialog.this, "Add new role membership", RoleMembershipDialog.this.rbacModel.getRoles(), ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					if(roles != null && !roles.isEmpty()){
						try {
							RoleMembershipDialog.this.rbacModel.addRoleMembership(subjectList.getSelectedValue().toString(), roles);
						} catch (ParameterException e1) {
							JOptionPane.showMessageDialog(RoleMembershipDialog.this, "Cannot add role membership:\n"+e1.getMessage(), "Internal Error", JOptionPane.ERROR_MESSAGE);
						}
						updateRoleList();
					}
				}
			});
			btnAddDataUsage.setBounds(185, 250, 80, 29);
			contentPanel.add(btnAddDataUsage);
		}
		{
			JLabel lblDataUsage = new JLabel("Assigned roles:");
			lblDataUsage.setBounds(185, 20, 181, 16);
			contentPanel.add(lblDataUsage);
		}
		{
			JButton btnRemove = new JButton("Remove");
			btnRemove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(roleList.getSelectedValues().length > 0){
						removeRoleMembership(ArrayUtils.toStringList(roleList.getSelectedValues()));
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
					RoleMembershipDialog.this.dispose();
				}
			});
			btnDone.setBounds(345, 251, 80, 29);
			contentPanel.add(btnDone);
		}
		setVisible(true);
	}
	
	private void removeRoleMembership(Collection<String> roles){
		try {
			rbacModel.removeRoleMembership(subjectList.getSelectedValue().toString(), roles);
		} catch (ParameterException e) {
			JOptionPane.showMessageDialog(RoleMembershipDialog.this, "Cannot remove role membership:\n"+e.getMessage(), "Internal Error", JOptionPane.ERROR_MESSAGE);
		}
		updateRoleList();
	}
	
	private JList getSubjectList(){
		if(subjectList == null){
			subjectList = new JList();
			subjectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			subjectList.setFixedCellHeight(20);
			subjectList.setVisibleRowCount(10);
			subjectList.setBorder(null);
			
			DefaultListModel listModel = new DefaultListModel();
			for(String activity: rbacModel.getSubjects())
				listModel.addElement(activity);
			subjectList.setModel(listModel);
			
			subjectList.setSelectedIndex(0);
			
			subjectList.addListSelectionListener(
	        		new ListSelectionListener(){
	        			public void valueChanged(ListSelectionEvent e) {
	        			    if ((e.getValueIsAdjusting() == false) && (subjectList.getSelectedValue() != null)) {
	        			    	updateRoleList();
	        			    }
	        			}
	        		}
	        );
		}
		return subjectList;
	}
	
	private JList getRoleList(){
		if(roleList == null){
			roleList = new JList(roleListModel);
			roleList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			roleList.setFixedCellHeight(20);
			roleList.setVisibleRowCount(10);
			roleList.setBorder(null);
			
			roleList.setSelectedIndex(0);
			
			roleList.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_DELETE){
						if(roleList.getSelectedValues().length > 0){
							removeRoleMembership(ArrayUtils.toStringList(roleList.getSelectedValues()));
						}
					}
				}
				
				@Override
				public void keyPressed(KeyEvent e) {}
			});
		}
		return roleList;
	}
	
	private void updateRoleList(){
		if(subjectList.getSelectedValue() != null){
			roleListModel.clear();
			try {
				for(String role: rbacModel.getRolesFor(subjectList.getSelectedValue().toString(), false)){
					roleListModel.addElement(role);
				}
			} catch (CompatibilityException e) {
				e.printStackTrace();
			} catch (ParameterException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void showDialog(Window owner, RBACModel rbacModel) throws ParameterException{
		new RoleMembershipDialog(owner, rbacModel);
	}

}
