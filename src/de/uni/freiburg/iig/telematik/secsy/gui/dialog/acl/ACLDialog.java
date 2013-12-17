package de.uni.freiburg.iig.telematik.secsy.gui.dialog.acl;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import de.invation.code.toval.graphic.dialog.AbstractDialog;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.secsy.gui.GUIProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.Context;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.ACModel;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.acl.ACLModel;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.rbac.RBACModel;



public class ACLDialog extends AbstractDialog {
	
	private static final long serialVersionUID = -5216821409053567193L;
	public static final Dimension PREFERRED_SIZE = new Dimension(500, 540);
	
	private JCheckBox chckbxDeriveAttributePermissions;
	private JComboBox viewComboBox;
	private AdvancedACLTable aclTable;
	
	private ACLModel aclModel;
	private Context context;

	
	public ACLDialog(Window owner, String title, ACModel acModel, Context context) throws Exception {
		super(owner, true, new Object[]{title,acModel,context});
	}
	
	@Override
	public Dimension getPreferredSize() {
		return PREFERRED_SIZE;
	}
	
	@Override
	protected void initialize(Object... parameters) throws Exception{
		setTitle((String) parameters[0]);
		ACModel acModel = (ACModel) parameters[1];
		if(acModel instanceof ACLModel){
			this.aclModel = (ACLModel) acModel;
		} else if(acModel instanceof RBACModel){
			this.aclModel = ((RBACModel) acModel).getRolePermissions();
		}
		this.context = (Context) parameters[2];
	}
	

	@Override
	protected void addComponents() throws Exception {
		mainPanel().setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		BoxLayout topPanelLayout = new BoxLayout(topPanel, BoxLayout.LINE_AXIS);
		topPanel.setLayout(topPanelLayout);
		topPanel.add(getViewBox());
		topPanel.add(getDeriveBox());
		topPanel.add(Box.createHorizontalGlue());
		mainPanel().add(topPanel, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(getACLTable());
		mainPanel().add(scrollPane, BorderLayout.CENTER);
		
//		aclModel = aclTable.getACLModel();
	}
	
	protected JCheckBox getDeriveBox(){
		if(chckbxDeriveAttributePermissions == null){
			chckbxDeriveAttributePermissions = new JCheckBox("Derive Attribute Permissions");
			chckbxDeriveAttributePermissions.setEnabled(context.hasAttributes());
			chckbxDeriveAttributePermissions.setSelected(false);
			chckbxDeriveAttributePermissions.setToolTipText("<html>In activated state, object permissions are <br>" +
															"derived automatically on basis of activity permissions.<br><br>" +
															"<b>Note:</b> This setting is only effective within this dialog.<br>" +
															"In case data usage information of activities are changed<br>" +
															"in a related context, this will not affect the object<br>" +
															"permissions of the connected access control model.<br>" +
															"This behavior is intentional and guarantees the independence<br>" +
															"of access control models from contexts and their reuse.</html>");
			chckbxDeriveAttributePermissions.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					aclTable.setDeriveAttributePermissions(chckbxDeriveAttributePermissions.isSelected());
				}
			});
		}
		return chckbxDeriveAttributePermissions;
	}
	
	protected JComboBox getViewBox(){
		if(viewComboBox == null){
			viewComboBox = new JComboBox();
			viewComboBox.addItem("Activity Permissions");
			if(context.hasAttributes()){
				viewComboBox.addItem("Attribute Permissions");
			}
			viewComboBox.setSelectedIndex(0);
			viewComboBox.setPreferredSize(new Dimension(200,GUIProperties.DEFAULT_LABEL_HEIGHT));
			viewComboBox.setMaximumSize(new Dimension(200,GUIProperties.DEFAULT_LABEL_HEIGHT));
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
		return viewComboBox;
	}

	@Override
	protected void setTitle() {}

	private AdvancedACLTable getACLTable() throws ParameterException{
		if(aclTable == null){
//			if(aclModel != null){
				aclTable = new AdvancedACLTable(aclModel, context);
//			} else {
//				aclTable = new AdvancedACLTable(subjects, context);
//				aclModel = aclTable.getACLModel();
//			}
	        aclTable.setFillsViewportHeight(true);
		}
		return aclTable;
	}

	public ACLModel getACLModel(){
		return aclModel;
	}
	
	public static ACLModel showDialog(Window owner, String title, ACLModel aclModel, Context context) throws Exception{
		ACLDialog activityDialog = new ACLDialog(owner, title, aclModel, context);
		return activityDialog.getACLModel();
	}

	@Override
	protected Border getBorder() {
		return GUIProperties.DEFAULT_DIALOG_BORDER;
	}
}
