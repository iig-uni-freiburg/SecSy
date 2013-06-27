package gui.dialog.acl;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;

import logic.generator.Context;
import accesscontrol.ACLModel;
import accesscontrol.ACModel;
import accesscontrol.RBACModel;


public class ACLDialog extends JDialog {
	
	private static final long serialVersionUID = -5216821409053567193L;

	private final JPanel contentPanel = new JPanel();
	
	private AdvancedACLTable aclTable = null;
	private ACLModel aclModel = null;
	
	private Set<String> subjects = null;
	private Context context = null;
	
	JCheckBox chckbxDeriveAttributePermissions = null;
	JComboBox viewComboBox = null;
	
	public ACLDialog(Window owner, String title, ACModel acModel, Context context) throws ParameterException {
		super(owner);
		Validate.notNull(title);
		Validate.notNull(context);
		Validate.notNull(acModel);
		
		setTitle(title);
		this.context = context;
		if(acModel instanceof ACLModel){
			this.aclModel = (ACLModel) acModel;
		} else if(acModel instanceof RBACModel){
			this.aclModel = ((RBACModel) acModel).getRolePermissions();
		}
		initialize();
		setLocationRelativeTo(owner);
	}
	
//	/**
//	 * @wbp.parser.constructor
//	 */
//	public ACLDialog(Window owner, String title, List<String> subjects, Context context) throws ParameterException {
//		super(owner);
//		
//		Validate.notNull(subjects);
//		Validate.noNullElements(subjects);
//		this.subjects = subjects;
//		initialize(title, context);
//		setLocationRelativeTo(owner);
//	}
	
	private void initialize() throws ParameterException{

		setBounds(100, 100, 400, 423);
		setModal(true);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		
		{
			JPanel panel = new JPanel();
			panel.setPreferredSize(new Dimension(200,70));
			contentPanel.add(panel, BorderLayout.NORTH);
			panel.setLayout(null);
			
			{
				viewComboBox = new JComboBox();
				viewComboBox.setBounds(6, 6, 222, 27);
				panel.add(viewComboBox);
				viewComboBox.addItem("Activity Permissions");
				if(context.hasAttributes()){
					viewComboBox.addItem("Attribute Permissions");
				}
				viewComboBox.setSelectedIndex(0);
				
				chckbxDeriveAttributePermissions = new JCheckBox("Derive Attribute Permissions");
				chckbxDeriveAttributePermissions.setBounds(6, 32, 222, 23);
				panel.add(chckbxDeriveAttributePermissions);
				chckbxDeriveAttributePermissions.setEnabled(context.hasAttributes());
				chckbxDeriveAttributePermissions.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						aclTable.setDeriveAttributePermissions(chckbxDeriveAttributePermissions.isSelected());
					}
				});
				viewComboBox.addItemListener(new ItemListener(){

					@Override
					public void itemStateChanged(ItemEvent e) {
						try {
							if(viewComboBox.getSelectedIndex() == 0){
								aclTable.setView(AdvancedACLTable.VIEW.TRANSACTION);
							} else {
								aclTable.setView(AdvancedACLTable.VIEW.OBJECT);
							}
						} catch (ParameterException e1) {
							e1.printStackTrace();
						}
					}
					
				});
			}
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, BorderLayout.CENTER);
			scrollPane.setViewportView(getACLTable());
		}
		
		aclModel = aclTable.getACLModel();
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						ACLDialog.this.dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						aclModel = null;
						ACLDialog.this.dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		setVisible(true);
		
	}
	
	private AdvancedACLTable getACLTable() throws ParameterException{
		if(aclTable == null){
			if(aclModel != null){
				aclTable = new AdvancedACLTable(aclModel, context);
			} else {
				aclTable = new AdvancedACLTable(subjects, context);
				aclModel = aclTable.getACLModel();
			}
	        aclTable.setFillsViewportHeight(true);
		}
		return aclTable;
	}

	public ACLModel getACLModel(){
		return aclModel;
	}
	
	
//	public static ACLModel showDialog(Window owner, String title, List<String> subjects, Context context) throws ParameterException{
//		ACLDialog activityDialog = new ACLDialog(owner, title, subjects, context);
//		return activityDialog.getACLModel();
//	}
	
	public static ACLModel showDialog(Window owner, String title, ACLModel aclModel, Context context) throws ParameterException{
		ACLDialog activityDialog = new ACLDialog(owner, title, aclModel, context);
		return activityDialog.getACLModel();
	}
}
