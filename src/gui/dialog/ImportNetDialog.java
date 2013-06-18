package gui.dialog;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;


public class ImportNetDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();

	public ImportNetDialog(Window owner) {
		setResizable(false);
		setModal(true);
		setLocationRelativeTo(owner);
		setTitle("Import Petri Net");
		setBounds(100, 100, 340, 408);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		JLabel lblActivities = new JLabel("Transitions:");
		lblActivities.setBounds(20, 20, 94, 16);
		contentPanel.add(lblActivities);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(20, 40, 300, 127);
		contentPanel.add(scrollPane);
		
		JList listTransitions = new JList();
		scrollPane.setViewportView(listTransitions);
		
		JLabel label = new JLabel("Activities:");
		label.setBounds(20, 179, 74, 16);
		contentPanel.add(label);
		
		JScrollPane scrollPane2 = new JScrollPane();
		scrollPane2.setBounds(20, 200, 300, 127);
		contentPanel.add(scrollPane2);
		
		JList listActivities = new JList();
		scrollPane2.setViewportView(listActivities);
			
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
		
		setVisible(true);
	}
	
	
	public static void main(String[] args) throws Exception{
		new ImportNetDialog(null);
	}
}
