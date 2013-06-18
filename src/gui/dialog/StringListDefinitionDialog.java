package gui.dialog;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import misc.StringUtils;

public class StringListDefinitionDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	
	private JTextField inputField;
	
	private List<String> stringList = null;


	public StringListDefinitionDialog(Window owner, String title) {
		super(owner);
		setTitle(title);
		setBounds(100, 100, 400, 131);
		setModal(true);
		setLocationRelativeTo(owner);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			inputField = new JTextField();
			inputField.setText("Valid separators: (semi-)colon, space");
			inputField.setBounds(97, 27, 268, 28);
			contentPanel.add(inputField);
			inputField.setColumns(10);
		}
		
		JLabel lblNumber = new JLabel("Number:");
		lblNumber.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNumber.setBounds(26, 33, 61, 16);
		contentPanel.add(lblNumber);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(inputField.getText().isEmpty()){
							JOptionPane.showMessageDialog(StringListDefinitionDialog.this, "Cannot proceed wiht empty String.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
							return;
						}
						int colons = StringUtils.countOccurrences(inputField.getText(), ',');
						int semicolons = StringUtils.countOccurrences(inputField.getText(), ';');
						if(colons > 0 && semicolons > 0){
							JOptionPane.showMessageDialog(StringListDefinitionDialog.this, "String contains more than one possible delimiter.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
							return;
						}
						String delimiter = " ";
						if(colons>0)
							delimiter = ",";
						if(semicolons>0)
							delimiter = ";";
						
						stringList = new ArrayList<String>();
						StringTokenizer tokenizer = new StringTokenizer(inputField.getText(), delimiter);
						while(tokenizer.hasMoreTokens()){
							String token = tokenizer.nextToken();
							if(!delimiter.equals(" "))
								token = StringUtils.removeSurrounding(token, ' ');
							stringList.add(token);
						}
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
						StringListDefinitionDialog.this.dispose();
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
		StringListDefinitionDialog dialog = new StringListDefinitionDialog(owner, title);
		return dialog.getStringList();
	}
}
