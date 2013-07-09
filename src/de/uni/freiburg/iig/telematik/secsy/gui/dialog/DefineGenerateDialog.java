package de.uni.freiburg.iig.telematik.secsy.gui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uni.freiburg.iig.telematik.secsy.gui.misc.CustomListRenderer;
import de.uni.freiburg.iig.telematik.sepia.petrinet.pt.PTNet;
import de.uni.freiburg.iig.telematik.sepia.petrinet.pt.PTTransition;



public class DefineGenerateDialog extends JDialog {

	private static final long serialVersionUID = -1396837102031308301L;

	private final JPanel contentPanel = new JPanel();
	
	private JList stringList = null;
	private DefaultListModel stringListModel = new DefaultListModel();
	private List<String> strings = null;

	public DefineGenerateDialog(Window owner, String title) {
		super(owner);
		setTitle(title);
		setBounds(100, 100, 250, 280);
		setModal(true);
		setLocationRelativeTo(owner);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JButton btnDefine = new JButton("Define...");
			btnDefine.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					List<String> definedStrings = StringListDefinitionDialog.showDialog(DefineGenerateDialog.this, "Define " + getTitle());
					if(definedStrings != null){
						stringListModel.clear();
						for(String string: definedStrings){
							stringListModel.addElement(string);
						}
						
					}
				}
			});
			btnDefine.setBounds(20, 9, 103, 29);
			contentPanel.add(btnDefine);
		}
		{
			JButton btnGenerate = new JButton("Generate...");
			btnGenerate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					List<String> generatedStrings = StringListGeneratorDialog.showDialog(DefineGenerateDialog.this, "Generate " + getTitle());
					if(generatedStrings != null){
						stringListModel.clear();
						for(String string: generatedStrings){
							stringListModel.addElement(string);
						}
						
					}
				}
			});
			btnGenerate.setBounds(122, 9, 108, 29);
			contentPanel.add(btnGenerate);
		}
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(20, 45, 210, 160);
		contentPanel.add(scrollPane);
		scrollPane.setViewportView(getActivityList());
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(!stringListModel.isEmpty()){
							if(strings == null)
								strings = new ArrayList<String>();
							for(int i=0; i<stringListModel.size(); i++)
								strings.add((String) stringListModel.getElementAt(i));
							dispose();
						} else {
							JOptionPane.showMessageDialog(DefineGenerateDialog.this, "Activity list is empty.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						}
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
						DefineGenerateDialog.this.dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		setVisible(true);
	}
	
	private void fillActivityList(PTNet petriNet){
		Set<String> labels = new HashSet<String>();
		for(PTTransition transition: petriNet.getTransitions()){
			if(!transition.isSilent())
				labels.add(transition.getLabel());
		}
		for(String label: labels){
			stringListModel.addElement(label);
		}
	}
	
	private JList getActivityList(){
		if(stringList == null){
			stringList = new JList(stringListModel);
			stringList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			stringList.setCellRenderer(new CustomListRenderer());
			stringList.setFixedCellHeight(20);
			stringList.setVisibleRowCount(10);
			stringList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			stringList.setBorder(null);
			
			stringList.addListSelectionListener(
	        		new ListSelectionListener(){
	        			public void valueChanged(ListSelectionEvent e) {
	        			    if ((e.getValueIsAdjusting() == false) && (stringList.getSelectedValue() != null)) {
	        			    	
	        			    }
	        			}
	        		}
	        );
		}
		return stringList;
	}
	
	public List<String> getActivities(){
		return strings;
	}
	
	
	public static List<String> showDialog(Window owner, String title){
		DefineGenerateDialog activityDialog = new DefineGenerateDialog(owner, title);
		return activityDialog.getActivities();
	}
}
