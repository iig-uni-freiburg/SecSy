package de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class SoDTransformerDialog extends JDialog {

	private static final long serialVersionUID = -3756374615684655497L;
	
	private final JPanel contentPanel = new JPanel();
	private JTextField txtSodTransformer;

	public SoDTransformerDialog() {
		setTitle("New SoD-Property Transformer");
		setBounds(100, 100, 300, 288);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JLabel lblPetriNet = new JLabel("Name:");
			lblPetriNet.setHorizontalAlignment(SwingConstants.TRAILING);
			lblPetriNet.setBounds(19, 23, 61, 16);
			contentPanel.add(lblPetriNet);
		}
		{
			JLabel lblTraverser = new JLabel("Violation:");
			lblTraverser.setHorizontalAlignment(SwingConstants.TRAILING);
			lblTraverser.setBounds(6, 53, 74, 16);
			contentPanel.add(lblTraverser);
		}
		{
			JLabel lblPasses = new JLabel("Separation activities:");
			lblPasses.setHorizontalAlignment(SwingConstants.TRAILING);
			lblPasses.setBounds(20, 105, 131, 16);
			contentPanel.add(lblPasses);
		}
		
		txtSodTransformer = new JTextField();
		txtSodTransformer.setText("SoD-Transformer 01");
		txtSodTransformer.setBounds(85, 17, 183, 28);
		contentPanel.add(txtSodTransformer);
		txtSodTransformer.setColumns(10);
		
		JSpinner spinner = new JSpinner();
		spinner.setBounds(85, 47, 111, 28);
		contentPanel.add(spinner);
		
		JLabel label = new JLabel("%");
		label.setBounds(202, 53, 17, 16);
		contentPanel.add(label);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(19, 125, 198, 87);
		contentPanel.add(scrollPane);
		
		JList list = new JList();
		list.setModel(new AbstractListModel() {
			private static final long serialVersionUID = -4596249052603627042L;
			String[] values = new String[] {"Sign Contract", "Acknowledge"};
			public int getSize() {
				return values.length;
			}
			public Object getElementAt(int index) {
				return values[index];
			}
		});
		scrollPane.setViewportView(list);
		
		JButton btnAdd = new JButton("Add");
		btnAdd.setBounds(218, 125, 68, 29);
		contentPanel.add(btnAdd);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(6, 86, 288, 12);
		contentPanel.add(separator);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		setVisible(true);
	}
	
	public static void main(String[] args) {
		try {
			SoDTransformerDialog dialog = new SoDTransformerDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
