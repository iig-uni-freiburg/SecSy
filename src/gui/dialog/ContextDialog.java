package gui.dialog;
import gui.SimulationComponents;
import gui.dialog.acl.ACLDialog;
import gui.dialog.datausage.DataUsageDialog;
import gui.misc.CustomListRenderer;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import de.invation.code.toval.misc.ArrayUtils;
import de.invation.code.toval.validate.ParameterException;

import logic.generator.Context;
import petrinet.pt.PTNet;
import util.PNUtils;
import accesscontrol.ACLModel;
import accesscontrol.ACModel;


public class ContextDialog extends JDialog {

	private static final long serialVersionUID = -3232421653315374886L;
	private final String INCONSISTENCY_ON_ADDING_MESSAGE_FORMAT = "The resulting set of context %s causes an inconsistency with respect to the assigned Access Control model.\n" +
																  "Access control models must contain all context %s.\n\n" +
																  "Proceed and add new %s also to the assigned Access control model?";
	
	private final String INCONSISTENCY_ON_ADDING_ACTIVITIES_MESSAGE = String.format(INCONSISTENCY_ON_ADDING_MESSAGE_FORMAT, "activities","activities", "activities");
	private final String INCONSISTENCY_ON_ADDING_SUBJECTS_MESSAGE = String.format(INCONSISTENCY_ON_ADDING_MESSAGE_FORMAT, "subjects", "subjects", "subjects");
	private final String INCONSISTENCY_ON_ADDING_ATTRIBUTES_MESSAGE = String.format(INCONSISTENCY_ON_ADDING_MESSAGE_FORMAT, "attributes", "attributes", "attributes");

	private final String DEFAULT_CONTEXT_NAME = "NewContext";

	private JPanel contentPane;
	
	private JList activityList = null;
	private JList subjectList = null;
	private JList attributeList = null;
	
	private DefaultListModel activityListModel = new DefaultListModel();
	private DefaultListModel subjectListModel = new DefaultListModel();
	private DefaultListModel attributeListModel = new DefaultListModel();
	
	private JTextField txtContextName;
	private JTextField txtACModelName;
	
	private JButton btnAddActivities = null;
	private JButton btnAddSubjects = null;
	private JButton btnAddAttributes = null;
	private JButton btnSetACModel = null;
	private JButton btnSetDataUsage = null;
	private JButton btnImportActivities = null;
	private JButton btnSetConstraints = null;
	private JButton btnEditPermissions = null;
	private JButton btnShowContext = null;
	private JButton btnOK = null;
	private JButton btnCancel = null;
	
	private AbstractAction addActivitiesAction = new AddActivitiesAction();
	private AbstractAction addSubjectsAction = new AddSubjectsAction();
	private AbstractAction addAttributesAction = new AddAttributesAction();
	
	private boolean activitiesAssigned = false;
	private boolean subjectsAssigned = false;
	private boolean attributesAssigned = false;
	private boolean acModelAssigned = false;
	
	private Context context = null;
	private Context originalContext = null;
	
	private boolean editMode = false;
	
	public ContextDialog(Window owner, Context context) {
		super(owner);
		this.originalContext = context;
		this.context = context.clone();
		this.editMode = true;
		setUpGUI(owner);
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public ContextDialog(Window owner) {
		super(owner);
		editMode = false;
		setUpGUI(owner);
	}
	

	public void setUpGUI(Window owner) {
		setResizable(false);
		setBounds(100, 100, 499, 505);
		setModal(true);
		setLocationRelativeTo(owner);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle(editMode ? "Edit Context" : "New Context");
		
		addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {}
			
			@Override
			public void windowIconified(WindowEvent e) {}
			
			@Override
			public void windowDeiconified(WindowEvent e) {}
			
			@Override
			public void windowDeactivated(WindowEvent e) {}
			
			@Override
			public void windowClosing(WindowEvent e) {
				cancelProcedure();
			}
			
			@Override
			public void windowClosed(WindowEvent e) {}
			
			@Override
			public void windowActivated(WindowEvent e) {}
		});
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);
		
		contentPane.add(getAddActivitiesButton());
		
		contentPane.add(getCancelButton());
		
		JLabel lblActivities = new JLabel("Activities:");
		lblActivities.setBounds(19, 57, 81, 16);
		contentPane.add(lblActivities);
		
		// Activity list
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(19, 77, 160, 150);
		contentPane.add(scrollPane);
		scrollPane.setViewportView(getActivityList());
		
		// Subject list
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane_1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane_1.setBounds(190, 77, 140, 150);
		scrollPane_1.setViewportView(getSubjectList());
		contentPane.add(scrollPane_1);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane_2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane_2.setBounds(342, 77, 140, 150);
		contentPane.add(scrollPane_2);
		scrollPane_2.setViewportView(getAttributeList());
		
		contentPane.add(getContextNameField());
		
		JLabel lblContextName = new JLabel("Context name:");
		lblContextName.setBounds(19, 15, 106, 16);
		contentPane.add(lblContextName);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(19, 43, 463, 12);
		contentPane.add(separator);
		
		contentPane.add(getSetDataUsageButton());
		
		JLabel lblSubjects = new JLabel("Subjects:");
		lblSubjects.setBounds(190, 57, 81, 16);
		contentPane.add(lblSubjects);
		
		JLabel lblAttributes = new JLabel("Attributes:");
		lblAttributes.setBounds(342, 57, 81, 16);
		contentPane.add(lblAttributes);
		
		contentPane.add(getAddSubjectsButton());
		
		contentPane.add(getAddAttributesButton());
		
		contentPane.add(getImportActivitiesButton());
		
		contentPane.add(getSetACModelButton());
		
		contentPane.add(getSetConstraintsButton());
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(19, 349, 463, 12);
		contentPane.add(separator_1);
		
		JLabel lblAccessControlModel = new JLabel("Access Control Model:");
		lblAccessControlModel.setHorizontalAlignment(SwingConstants.TRAILING);
		lblAccessControlModel.setBounds(19, 375, 160, 16);
		contentPane.add(lblAccessControlModel);
		
		txtACModelName = new JTextField();
		txtACModelName.setEditable(false);
		txtACModelName.setBounds(190, 370, 140, 28);
		contentPane.add(txtACModelName);
		txtACModelName.setColumns(10);
		
		contentPane.add(getEditPermissionsButton());
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setBounds(0, 403, 500, 12);
		contentPane.add(separator_2);
		
		contentPane.add(getOKButton());
		
		contentPane.add(getShowContextButton());
		
		if(editMode){
			initializeFields();
		}
		this.getRootPane().setDefaultButton(btnOK);
		
		updateVisibility();
		
		setVisible(true);
	}
	
	private void initializeFields(){
		txtContextName.setText(context.getName());
		for(String activity: context.getActivities()){
			activityListModel.addElement(activity);
			activitiesAssigned = true;
		}
		for(String subject: context.getSubjects()){
			subjectListModel.addElement(subject);
			subjectsAssigned = true;
		}
		for(String attribute: context.getAttributes()){
			attributeListModel.addElement(attribute);
			attributesAssigned = true;
		}
		
		ACModel acModel = context.getACModel();
		if(acModel != null){
			txtACModelName.setText(acModel.getName());
			acModelAssigned = true;
		}
		//TODO
	}
	
	
	//------- BUTTONS --------------------------------------------------------------------------------------------
	
	private JButton getOKButton(){
		if(btnOK == null){
			btnOK = new JButton("OK");
			btnOK.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(context == null || !context.hasActivities()){
						JOptionPane.showMessageDialog(ContextDialog.this, "Cannot add empty context.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(!context.isValid()){
						JOptionPane.showMessageDialog(ContextDialog.this, "Cannot add invalid context.\nReason: All activities must be executable.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					if(context.getName().isEmpty()){
						JOptionPane.showMessageDialog(ContextDialog.this, "Context name cannot be empty.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					String contextName = (txtContextName.getText());
					Set<String> contextNames = new HashSet<String>(SimulationComponents.getInstance().getContextNames());
					if(originalContext != null){
						contextNames.remove(originalContext.getName());
					}
					if(contextNames.contains(contextName)){
						JOptionPane.showMessageDialog(ContextDialog.this, "There is already a context with name \""+contextName+"\"", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					try {
						context.setName(contextName);
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(ContextDialog.this, "Cannot set context name.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
	
					dispose();
				}
			});
			btnOK.setBounds(241, 422, 117, 29);
		}
		return btnOK;
	}
	
	private JButton getCancelButton(){
		if(btnCancel == null){
			btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cancelProcedure();
					dispose();
				}
			});
			btnCancel.setBounds(365, 422, 117, 29);
		}
		return btnCancel;
	}
	
	private void cancelProcedure(){	
		if(editMode){
			//Check if access control model is still compatible.
			ACModel acModel = originalContext.getACModel();
			if(acModel != null){
				try {
					originalContext.validateACModel(acModel);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(ContextDialog.this, "Inconsistency caused by modifications in the referenced access control model.\nReason: "+e.getMessage()+"\n\nPlease adjust the access control model or choose another one.", "Inconsistency Exception", JOptionPane.ERROR_MESSAGE);
				}	
			}
		}
		context = null;
	}
	
	private JButton getShowContextButton(){
		if(btnShowContext == null){
			btnShowContext = new JButton("Show Context");
			btnShowContext.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(context == null){
						new StringDialog(ContextDialog.this, "");
					} else {
						new StringDialog(ContextDialog.this, context.toString());
					}
				}
			});
			btnShowContext.setEnabled(false);
			btnShowContext.setBounds(342, 314, 140, 29);
		}
		return btnShowContext;
	}
	
	private JButton getAddActivitiesButton(){
		if(btnAddActivities == null){
			btnAddActivities = new JButton();
			btnAddActivities.setAction(addActivitiesAction); 
			btnAddActivities.setBounds(19, 230, 160, 29);
		}
		return btnAddActivities;
	}
	
	private JButton getImportActivitiesButton(){
		if(btnImportActivities == null){
			btnImportActivities = new JButton("Import activities");
			btnImportActivities.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(context != null)
						JOptionPane.showMessageDialog(ContextDialog.this, "Importing activities will reset all context properties.", "Warning", JOptionPane.WARNING_MESSAGE);
					PTNet ptNet = PetriNetDialog.showPetriNetDialog(ContextDialog.this);
					if(ptNet != null){
						if(ptNet.getTransitions().isEmpty())
							JOptionPane.showMessageDialog(ContextDialog.this, "Cannot import activities: Petri net contains no transitions.", "Invalid Argument", JOptionPane.ERROR_MESSAGE);
						try{
							newContext(PNUtils.getLabelSetFromTransitions(ptNet.getTransitions()));
						}catch(ParameterException ex){
							JOptionPane.showMessageDialog(ContextDialog.this, "Cannot extract activity names from Petri net transitions.", "Internal Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});
			btnImportActivities.setBounds(19, 258, 160, 29);
		}
		return btnImportActivities;
	}
	
	private JButton getAddSubjectsButton(){
		if(btnAddSubjects == null){
			btnAddSubjects = new JButton();
			btnAddSubjects.setEnabled(false);
			btnAddSubjects.setAction(addSubjectsAction);
			btnAddSubjects.setBounds(190, 230, 140, 29);
		}
		return btnAddSubjects;
	}
	
	private JButton getAddAttributesButton(){
		if(btnAddAttributes == null){
			btnAddAttributes = new JButton();
			btnAddAttributes.setEnabled(false);
			btnAddAttributes.setAction(addAttributesAction);
			btnAddAttributes.setBounds(342, 230, 140, 29);
		}
		return btnAddAttributes;
	}
	
	private JButton getSetDataUsageButton(){
		if(btnSetDataUsage == null){
			btnSetDataUsage = new JButton("Set data usage");
			btnSetDataUsage.setEnabled(false);
			btnSetDataUsage.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						DataUsageDialog.showDialog(ContextDialog.this, context);
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(ContextDialog.this, "Cannot trigger data usage mode.", "Internal Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			btnSetDataUsage.setBounds(19, 286, 160, 29);
		}
		return btnSetDataUsage;
	}
	
	private JButton getSetConstraintsButton(){
		if(btnSetConstraints == null){
			btnSetConstraints = new JButton("Set constraints");
			btnSetConstraints.setEnabled(false);
			btnSetConstraints.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						RoutingConstraintsDialog.showDialog(ContextDialog.this, context);
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(ContextDialog.this, "Cannot trigger constraint mode.", "Internal Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			btnSetConstraints.setBounds(19, 314, 160, 29);
		}
		return btnSetConstraints;
	}
	
	private JButton getSetACModelButton(){
		if(btnSetACModel == null){
			btnSetACModel = new JButton("Set/Edit");
			btnSetACModel.setEnabled(false);
			btnSetACModel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						ACModel acModel = ACModelDialog.showDialog(ContextDialog.this, context);
						if(acModel != null){
							context.setACModel(acModel);
							acModelAssigned = true;
							txtACModelName.setText(acModel.getName());
							updateVisibility();
						}
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(ContextDialog.this, e1.getMessage(), "Cannot set access control model.", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			btnSetACModel.setBounds(342, 371, 98, 29);
		}
		return btnSetACModel;
	}
	
	private JButton getEditPermissionsButton(){
		if(btnEditPermissions == null){
			btnEditPermissions = new JButton("Edit permissions");
			btnEditPermissions.setEnabled(false);
			btnEditPermissions.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					try {
						if(context.getACModel() instanceof ACLModel){
							ACLDialog.showDialog(ContextDialog.this, "Edit subject permissions", (ACLModel) context.getACModel(), context);
						}
						}catch(ParameterException ex){
							ex.printStackTrace();
					}
				}
			});
			btnEditPermissions.setBounds(190, 258, 140, 29);
		}
		return btnEditPermissions;
	}
	
	
	//------- OTHER GUI COMPONENTS ----------------------------------------------------------------------------------------
	
	private JTextField getContextNameField(){
		if(txtContextName == null){
			txtContextName = new JTextField();
			txtContextName.setText(DEFAULT_CONTEXT_NAME);
			txtContextName.setBounds(118, 10, 121, 28);
			txtContextName.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					if(context != null){
						try {
							context.setName(txtContextName.getText());
						} catch (ParameterException e1) {
							e1.printStackTrace();
						}
					}
				}
				
				@Override
				public void keyPressed(KeyEvent e) {}
			});
			
			txtContextName.setColumns(10);
		}
		return txtContextName;
	}
	
	private JList getActivityList(){
		if(activityList == null){
			activityList = new JList(activityListModel);
			activityList.setCellRenderer(new CustomListRenderer());
			activityList.setFixedCellHeight(20);
			activityList.setVisibleRowCount(10);
			activityList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			activityList.setBorder(null);
			
			activityList.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_DELETE){
						if(activityList.getSelectedValues() != null){
							boolean propagateDeletion = false;
							if(acModelAssigned){
								int option = JOptionPane.showConfirmDialog(ContextDialog.this, "Also remove activities from the assigned Access Control model?" +
																							   "\n(This operation cannot be undone and in case of cancelling the dialog," +
																							   "\nthe assigned access control model may become incompatible)", "Propagation of removal operation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
								if(option == JOptionPane.CANCEL_OPTION)
									return;
								propagateDeletion = option == JOptionPane.YES_OPTION;
							}
							Set<String> contextsWithReferenceToSameACModel = SimulationComponents.getInstance().getContextsWithACModel(context.getACModel());
							contextsWithReferenceToSameACModel.remove(context.getName());
							
							if(propagateDeletion && !contextsWithReferenceToSameACModel.isEmpty())
								JOptionPane.showMessageDialog(ContextDialog.this, "Cannot remove activities from assigned Access Control model:\n Other contexts refer to the same model.", "Consistency Exception", JOptionPane.ERROR_MESSAGE);
							try {
								context.removeActivities(ArrayUtils.toStringList(activityList.getSelectedValues()), propagateDeletion);
								if((activityListModel.size() - activityList.getSelectedIndices().length == 0)){
									activitiesAssigned = false;
								}
							} catch (ParameterException e1) {
								JOptionPane.showMessageDialog(ContextDialog.this, "Cannot remove activities.", "Internal Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
							updateVisibility();
							updateActivityList(true);
						}
					}
				}
				
				@Override
				public void keyPressed(KeyEvent e) {}
			});
		}
		return activityList;
	}
	
	private JList getSubjectList(){
		if(subjectList == null){
			subjectList = new JList(subjectListModel);
			subjectList.setCellRenderer(new CustomListRenderer());
			subjectList.setFixedCellHeight(20);
			subjectList.setVisibleRowCount(10);
			subjectList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			subjectList.setBorder(null);
			
			subjectList.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_DELETE){
						if(subjectList.getSelectedValues() != null){
							boolean propagateDeletion = false;
							if(acModelAssigned){
								int option = JOptionPane.showConfirmDialog(ContextDialog.this, "Also remove subjects from the assigned Access Control model?" +
																							   "\n(This operation cannot be undone and in case of cancelling the dialog," +
																							   "\nthe assigned access control model may become incompatible)", "Propagation of removal operation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
								if(option == JOptionPane.CANCEL_OPTION)
									return;
								propagateDeletion = option == JOptionPane.YES_OPTION;
							}
							Set<String> contextsWithReferenceToSameACModel = SimulationComponents.getInstance().getContextsWithACModel(context.getACModel());
							contextsWithReferenceToSameACModel.remove(context.getName());
							
							if(propagateDeletion && !contextsWithReferenceToSameACModel.isEmpty())
								JOptionPane.showMessageDialog(ContextDialog.this, "Cannot remove subjects from assigned Access Control model:\n Other contexts refer to the same model.", "Consistency Exception", JOptionPane.ERROR_MESSAGE);
							try {
								context.removeSubjects(ArrayUtils.toStringList(subjectList.getSelectedValues()), propagateDeletion);
								if((subjectListModel.size() - subjectList.getSelectedIndices().length == 0)){
									subjectsAssigned = false;
								}
							} catch (ParameterException e1) {
								JOptionPane.showMessageDialog(ContextDialog.this, "Cannot remove subjects.", "Internal Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
							updateVisibility();
							updateSubjectList(true);
						}
					}
				}
				
				@Override
				public void keyPressed(KeyEvent e) {}
			});
		}
		return subjectList;
	}
	
	private JList getAttributeList(){
		if(attributeList == null){
			attributeList = new JList(attributeListModel);
			attributeList.setCellRenderer(new CustomListRenderer());
			attributeList.setFixedCellHeight(20);
			attributeList.setVisibleRowCount(10);
			attributeList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			attributeList.setBorder(null);
			
			attributeList.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_DELETE){
						if(attributeList.getSelectedValues() != null){
							boolean propagateDeletion = false;
							if(acModelAssigned){
								int option = JOptionPane.showConfirmDialog(ContextDialog.this, "Also remove attributes from the assigned Access Control model?"+
																							   "\n(This operation cannot be undone and in case of cancelling the dialog," +
																							   "\nthe assigned access control model may become incompatible)", "Propagation of removal operation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
								if(option == JOptionPane.CANCEL_OPTION)
									return;
								propagateDeletion = option == JOptionPane.YES_OPTION;
							}
							Set<String> contextsWithReferenceToSameACModel = SimulationComponents.getInstance().getContextsWithACModel(context.getACModel());
							contextsWithReferenceToSameACModel.remove(context.getName());
							
							if(propagateDeletion && !contextsWithReferenceToSameACModel.isEmpty())
								JOptionPane.showMessageDialog(ContextDialog.this, "Cannot remove attributes from assigned Access Control model:\n Other contexts refer to the same model.", "Consistency Exception", JOptionPane.ERROR_MESSAGE);
							try {
								context.removeAttributes(ArrayUtils.toStringList(attributeList.getSelectedValues()), propagateDeletion);
								if((attributeListModel.size() - attributeList.getSelectedIndices().length == 0)){
									attributesAssigned = false;
								}
							} catch (ParameterException e1) {
								JOptionPane.showMessageDialog(ContextDialog.this, "Cannot remove attributes: \n" + e1.getMessage(), "Internal Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
							updateVisibility();
							updateAttributeList(true);
						}
					}
				}
				
				@Override
				public void keyPressed(KeyEvent e) {}
			});
		}
		return attributeList;
	}
	
	
	//------- FUNCTIONALITY ----------------------------------------------------------------------------------------------
	
	public Context getContext(){
		return context;
	}
	
	private void newContext(Set<String> activities){
		resetVisibility();
		try {
			context = new Context(txtContextName.getText(), activities);
			activitiesAssigned = true;
			updateActivityList(false);
			updateSubjectList(false);
			updateAttributeList(false);
			updateVisibility();
		} catch (ParameterException e1) {
			JOptionPane.showMessageDialog(ContextDialog.this, "Cannot generate new context with the given set of activities.", "Internal Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void updateActivityList(boolean setSelection){
		activityListModel.clear();
		if(context != null){
			for(String activity: context.getActivities()){
				activityListModel.addElement(activity);
			}
		}
		if(!activityListModel.isEmpty() && setSelection)
			activityList.setSelectedIndex(0);
	}
	
	private void updateSubjectList(boolean setSelection){
		subjectListModel.clear();
		if(context != null){
			for(String subject: context.getSubjects()){
				subjectListModel.addElement(subject);
			}
		}
		if(!subjectListModel.isEmpty() && setSelection)
			subjectList.setSelectedIndex(0);
	}
	
	private void updateAttributeList(boolean setSelection){
		attributeListModel.clear();
		if(context != null){
			for(String attribute: context.getAttributes()){
				attributeListModel.addElement(attribute);
			}
		}
		if(!attributeListModel.isEmpty() && setSelection)
			attributeList.setSelectedIndex(0);
	}
	
	private void updateVisibility(){
		addSubjectsAction.setEnabled(activitiesAssigned || attributesAssigned || subjectsAssigned);
		addAttributesAction.setEnabled(activitiesAssigned || attributesAssigned || subjectsAssigned);
		btnShowContext.setEnabled(activitiesAssigned);
//		btnImportActivities.setEnabled(SimulationComponents.getInstance().hasPetriNets());
		btnSetDataUsage.setEnabled(activitiesAssigned && attributesAssigned);
		btnSetConstraints.setEnabled(activitiesAssigned && attributesAssigned);
		btnEditPermissions.setEnabled(acModelAssigned && subjectsAssigned && context != null && context.getACModel() instanceof ACLModel);
		btnSetACModel.setEnabled((activitiesAssigned && subjectsAssigned) || acModelAssigned);
	}
	
	private void resetVisibility(){
		activitiesAssigned = false;
		subjectsAssigned = false;
		attributesAssigned = false;
		acModelAssigned = false;
	}
	
	
	//------- ACTIONS -----------------------------------------------------------------------------------------------------------
	
	private class AddAttributesAction extends AbstractAction {

		private static final long serialVersionUID = -5213738768531109730L;
		
		public AddAttributesAction(){
			super("Add Attributes");
			this.setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(context.hasAttributes()){
				int decision = JOptionPane.showConfirmDialog(ContextDialog.this, "Adding attributes may affect the consistency of this context. \n Do you want to proceed?", "Consistency Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if(decision == JOptionPane.NO_OPTION)
				return;
			}
			
			List<String> attributes = DefineGenerateDialog.showDialog(ContextDialog.this, "Attributes");
			if(attributes != null){
				// Add attributes to the existing context.
				boolean addToACModel = false;
				if(context.getACModel() != null){
					if(!context.getACModel().getObjects().containsAll(attributes)){
						int option = JOptionPane.showConfirmDialog(ContextDialog.this, INCONSISTENCY_ON_ADDING_ATTRIBUTES_MESSAGE, "Consistency Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						switch(option){
							case JOptionPane.CANCEL_OPTION: return;
							case JOptionPane.NO_OPTION: return;
							case JOptionPane.YES_OPTION: addToACModel = true;
						}
					}
				}
				try {
					context.addAttributes(attributes, addToACModel);
				} catch (ParameterException e1) {
					JOptionPane.showMessageDialog(ContextDialog.this, "Cannot add objects to context.", "Inconsistency Exception", JOptionPane.ERROR_MESSAGE);
					return;
				}
				attributesAssigned = true;
				updateVisibility();
				updateAttributeList(false);
			}
		}
		
	}
	
	private class AddSubjectsAction extends AbstractAction {
		
		private static final long serialVersionUID = -4148251659616210607L;
		
		public AddSubjectsAction(){
			super("Add Subjects");
			this.setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(context.hasSubjects()){
				int decision = JOptionPane.showConfirmDialog(ContextDialog.this, "Adding subjects may affect the consistency of this context. \n Do you want to proceed?", "Consistency Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if(decision == JOptionPane.NO_OPTION)
					return;
			}
			
			List<String> subjects = DefineGenerateDialog.showDialog(ContextDialog.this, "Subjects");
			if(subjects != null){
				// Add subjects to the existing context.
				boolean addToACModel = false;
				if(context.getACModel() != null){
					if(!context.getACModel().getSubjects().containsAll(subjects)){
						int option = JOptionPane.showConfirmDialog(ContextDialog.this, INCONSISTENCY_ON_ADDING_SUBJECTS_MESSAGE, "Consistency Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						switch(option){
							case JOptionPane.CANCEL_OPTION: return;
							case JOptionPane.NO_OPTION: return;
							case JOptionPane.YES_OPTION: addToACModel = true;
						}
					}
				}
				try {
					context.addSubjects(subjects, addToACModel);
				} catch (ParameterException e1) {
					JOptionPane.showMessageDialog(ContextDialog.this, "Cannot add subjects to context.", "Inconsistency Exception", JOptionPane.ERROR_MESSAGE);
					return;
				}
				subjectsAssigned = true;
				updateVisibility();
				updateSubjectList(false);
			}
		}
		
	}
	
	private class AddActivitiesAction extends AbstractAction {
		
		private static final long serialVersionUID = 1979108778175746934L;
		
		public AddActivitiesAction(){
			super("Add Activities");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(context != null && context.hasActivities()){
				int decision = JOptionPane.showConfirmDialog(ContextDialog.this, "Adding activities may affect the consistency of this context. \n Do you want to proceed?", "Consistency Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if(decision == JOptionPane.NO_OPTION)
					return;
			}
			List<String> activities = DefineGenerateDialog.showDialog(ContextDialog.this, "Activities");
			if(activities != null){
				if(context == null){
					// Create new context with the generated activities.
					newContext(new HashSet<String>(activities));
				} else {
					// Add activities to the existing context.
					boolean addToACModel = false;
					if(context.getACModel() != null){
						if(!context.getACModel().getTransactions().containsAll(activities)){
							int option = JOptionPane.showConfirmDialog(ContextDialog.this, INCONSISTENCY_ON_ADDING_ACTIVITIES_MESSAGE, "Consistency Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
							switch(option){
								case JOptionPane.CANCEL_OPTION: return;
								case JOptionPane.NO_OPTION: return;
								case JOptionPane.YES_OPTION: addToACModel = true;
							}
						}
					}
					try {
						context.addActivities(activities, addToACModel);
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(ContextDialog.this, "Cannot add activities to context: \n" + e1.getMessage(), "Inconsistency Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					activitiesAssigned = true;
					updateVisibility();
					updateActivityList(false);
				}
			}
		}
		
	}
	
	//------- STARTUP ---------------------------------------------------------------------------------------------------------------
	
	public static Context showContextDialog(Window parentWindow){
	        ContextDialog contextDialog = new ContextDialog(parentWindow);
	        return contextDialog.getContext();
	}
	
	public static Context showContextDialog(Window parentWindow, Context context){
        ContextDialog contextDialog = new ContextDialog(parentWindow, context);
        return contextDialog.getContext();
}
	
	public static void main(String[] args) {
		ContextDialog.showContextDialog(null);
	}
}
