package gui.dialog;
import gui.misc.CustomListRenderer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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


public class ValueChooserDialog extends JDialog {
	
	private static final long serialVersionUID = 2306027725394345926L;

	private final JPanel contentPanel = new JPanel();
	
	private JList stringList = null;
	private DefaultListModel stringListModel = new DefaultListModel();
	private List<String> values = null;
	private Collection<String> possibleValues = null;
	private int selectionMode = ListSelectionModel.SINGLE_SELECTION;
	
	public ValueChooserDialog(Window owner, String title, Collection<String> possibleValues) {
		this(owner, title, possibleValues, ListSelectionModel.SINGLE_SELECTION);
	}

	public ValueChooserDialog(Window owner, String title, Collection<String> possibleValues, int selectionMode) {
		super(owner);
		this.selectionMode = selectionMode;
		setTitle(title);
		setBounds(100, 100, 250, 260);
		setModal(true);
		setLocationRelativeTo(owner);
		
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
				values = null;
			}
			
			@Override
			public void windowClosed(WindowEvent e) {}
			
			@Override
			public void windowActivated(WindowEvent e) {}
		});
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(20, 20, 210, 160);
		contentPanel.add(scrollPane);
		this.possibleValues = possibleValues;
		scrollPane.setViewportView(getValueList());
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(!stringListModel.isEmpty()){
							values = new ArrayList<String>();
							for(Object o: stringList.getSelectedValues())
								values.add((String) o);
							dispose();
						} else {
							JOptionPane.showMessageDialog(ValueChooserDialog.this, "Value list is empty.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
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
						values = null;
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		setVisible(true);
	}
	
	private JList getValueList(){
		if(stringList == null){
			stringList = new JList(stringListModel);
			stringList.setCellRenderer(new CustomListRenderer());
			stringList.setFixedCellHeight(20);
			stringList.setVisibleRowCount(10);
			stringList.getSelectionModel().setSelectionMode(selectionMode);
			stringList.setBorder(null);
			for(String possibleValue: possibleValues){
				stringListModel.addElement(possibleValue);
			}
		}
		return stringList;
	}
	
	public List<String> getValues(){
		return values;
	}
	
	
	public static List<String> showDialog(Window owner, String title, Collection<String> values){
		ValueChooserDialog activityDialog = new ValueChooserDialog(owner, title, values);
		return activityDialog.getValues();
	}
	
	public static List<String> showDialog(Window owner, String title, Collection<String> values, int selectionMode){
		ValueChooserDialog activityDialog = new ValueChooserDialog(owner, title, values, selectionMode);
		return activityDialog.getValues();
	}
}
