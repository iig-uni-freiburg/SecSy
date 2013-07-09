package de.uni.freiburg.iig.telematik.secsy.gui.dialog;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;


public class StringListGeneratorDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	
	private JTextField numberField;
	private JTextField prefixField;
	private JTextField postfixField;
	
	private List<String> stringList = null;


	public StringListGeneratorDialog(Window owner, String title) {
		super(owner);
		setTitle(title);
		setBounds(100, 100, 250, 215);
		setModal(true);
		setLocationRelativeTo(owner);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			numberField = new JTextField();
			numberField.setText("10");
			numberField.setBounds(97, 27, 134, 28);
			contentPanel.add(numberField);
			numberField.setColumns(10);
		}
		{
			prefixField = new JTextField();
			prefixField.setBounds(97, 67, 134, 28);
			contentPanel.add(prefixField);
			prefixField.setColumns(10);
		}
		{
			postfixField = new JTextField();
			postfixField.setBounds(97, 107, 134, 28);
			contentPanel.add(postfixField);
			postfixField.setColumns(10);
		}
		
		JLabel lblNumber = new JLabel("Number:");
		lblNumber.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNumber.setBounds(26, 33, 61, 16);
		contentPanel.add(lblNumber);
		
		JLabel lblPrefix = new JLabel("Prefix:");
		lblPrefix.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPrefix.setBounds(26, 73, 61, 16);
		contentPanel.add(lblPrefix);
		
		JLabel lblPostfix = new JLabel("Postfix:");
		lblPostfix.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPostfix.setBounds(24, 113, 61, 16);
		contentPanel.add(lblPostfix);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Generate");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int number = 0;
						try{
							number = Integer.parseInt(numberField.getText());
						}catch(Exception exception){
							JOptionPane.showMessageDialog(StringListGeneratorDialog.this, "Content in number field is not a natural number!", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
							return;
						}
						stringList = createStringList(number, prefixField.getText(), postfixField.getText());
						dispose();
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
						StringListGeneratorDialog.this.dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		setVisible(true);
	}
	
	public List<String> getStringList(){
		return stringList;
	}
	
	private List<String> createStringList(int number, String prefix, String postfix) {
		return createStringList(number, prefix + "%s" + postfix);
	}
	
	private List<String> createStringList(int number, String stringFormat) {
		List<String> result = new ArrayList<String>(number);
		for(int i=1; i<=number; i++){
			result.add(String.format(stringFormat, i));
		}
		return result;
	}
	
	
	public static List<String> showDialog(Window owner, String title){
		StringListGeneratorDialog dialog = new StringListGeneratorDialog(owner, title);
		return dialog.getStringList();
	}
}
