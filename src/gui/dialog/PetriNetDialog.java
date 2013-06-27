package gui.dialog;
import gui.SimulationComponents;
import gui.misc.CustomListRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.invation.code.toval.validate.ParameterException;

import parser.PNMLFilter;
import parser.PNMLParser;
import petrinet.pt.PTNet;


public class PetriNetDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	
	private JList netList = null;
	private DefaultListModel netListModel = new DefaultListModel();
	
	private PTNet petriNet = null;

	/**
	 * Create the dialog.
	 */
	public PetriNetDialog(Window owner) {
		super(owner);
		setBounds(100, 100, 312, 241);
		setModal(true);
		setLocationRelativeTo(owner);
	
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(20, 20, 270, 142);
		scrollPane.setViewportView(getNetList());
		contentPanel.add(scrollPane);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				
				JButton btnImportPnml = new JButton("Import PNML");
				btnImportPnml.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JOptionPane.showMessageDialog(PetriNetDialog.this, "Transitions with names starting with \"_\" will be interpreted as silent transitions.\n" +
																		   "(Their firing will not result in the generation of s log event.)\n\n" +
																		   "Transition labels (not names/IDs!) will be interpreted as process activities.\n" +
																		   "This way, it is possible to consider duplicated activities in processes.", "Petri net import", JOptionPane.INFORMATION_MESSAGE);

						JFileChooser fc = new JFileChooser();
						fc.setAcceptAllFileFilterUsed(false);
						fc.addChoosableFileFilter(new PNMLFilter());
						int returnValue = fc.showOpenDialog(PetriNetDialog.this);
						if (returnValue == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();

							// Try to import the Petri net.
							PTNet petriNet = null;
							try {
								petriNet = PNMLParser.parsePNML(file.getAbsolutePath(), true);
							} catch (Exception ex) {
								JOptionPane.showMessageDialog(PetriNetDialog.this,"Cannot parse Petri net.\nReason: " + ex.getMessage(), "Parsing Exeption", JOptionPane.ERROR_MESSAGE);
								return;
							}
							String netName = petriNet.getName();
							try {
								while (netName == null || SimulationComponents.getInstance().getPetriNet(netName) != null) {
									netName = JOptionPane.showInputDialog(PetriNetDialog.this, "Name for the Petri net:", file.getName().substring(0,file.getName().lastIndexOf(".")));
								}
							} catch (ParameterException e1) {
								JOptionPane.showMessageDialog(PetriNetDialog.this,"Cannot check if net name is already in use.\nReason: " + e1.getMessage(),"Internal Exeption", JOptionPane.ERROR_MESSAGE);
								return;
							}
							try {
								if (!petriNet.getName().equals(netName))
									petriNet.setName(netName);
							} catch (ParameterException e2) {
								JOptionPane.showMessageDialog(PetriNetDialog.this,"Cannot change Petri net name to\""+ netName + "\".\nReason: "+ e2.getMessage(),"Internal Exeption",JOptionPane.ERROR_MESSAGE);
								return;
							}

							try {
								SimulationComponents.getInstance().addPetriNet(petriNet);
							} catch (Exception e1) {
								JOptionPane.showMessageDialog(PetriNetDialog.this,"Cannot add imported net to simulation components.\nReason: "	+ e1.getMessage(),"Internal Exeption",JOptionPane.ERROR_MESSAGE);
								return;
							}
							updateNetList();
							netList.setSelectedValue(netName, true);
						} else {
							// User aborted the dialog.
						}
					}
				});
				buttonPane.add(btnImportPnml);
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						petriNet = null;
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		setVisible(true);
	}
	
	private JList getNetList(){
		if(netList == null){
			netList = new JList(netListModel);
			netList.setCellRenderer(new CustomListRenderer());
			netList.setFixedCellHeight(20);
			netList.setVisibleRowCount(10);
			netList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			netList.setBorder(BorderFactory.createLineBorder(Color.black));
			netList.setBounds(109, 203, 151, -82);
			
			netList.addListSelectionListener(
	        		new ListSelectionListener(){
	        			public void valueChanged(ListSelectionEvent e) {
	        			    if ((e.getValueIsAdjusting() == false) && (netList.getSelectedValue() != null)) {
	        			    	try {
									petriNet = SimulationComponents.getInstance().getPetriNet(netList.getSelectedValue().toString());
								} catch (ParameterException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
	        			    }
	        			}
	        		}
	        );
			updateNetList();
		}
		return netList;
	}
	
	private void updateNetList(){
		netListModel.clear();
		if(SimulationComponents.getInstance().hasPetriNets()){
			for(PTNet ptNet: SimulationComponents.getInstance().getPetriNets()){
				netListModel.addElement(ptNet.getName());
			}
			netList.setSelectedIndex(0);
			try {
				petriNet = SimulationComponents.getInstance().getPetriNet(netList.getSelectedValue().toString());
			} catch (ParameterException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	public PTNet getPetriNet(){
		return petriNet;
	}
	
	public static PTNet showPetriNetDialog(Window parentWindow){
		PetriNetDialog dialog = new PetriNetDialog(parentWindow);
		return dialog.getPetriNet();
	}
	
	public static void main(String[] args) {
		new PetriNetDialog(null);
	}
}
