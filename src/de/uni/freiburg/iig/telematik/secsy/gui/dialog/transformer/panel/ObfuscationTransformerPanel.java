package de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import de.invation.code.toval.graphic.dialog.ValueChooserDialog;
import de.invation.code.toval.graphic.renderer.AlternatingRowColorListCellRenderer;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.AbstractTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.ObfuscationTransformer;
import de.uni.freiburg.iig.telematik.sewol.log.EntryField;

public class ObfuscationTransformerPanel extends AbstractTransformerPanel<ObfuscationTransformer>{

	private static final long serialVersionUID = 4146000746429527576L;

	private JList listObfuscationFields;
	private DefaultListModel listObfuscationFieldsModel;
	private JButton btnAddObfuscationField;
	private Set<EntryField> excludedFields;

	public ObfuscationTransformerPanel(Set<String> activities) throws Exception {
		super(activities);
	}

	@Override
	protected void initialize() throws Exception {
		excludedFields = new HashSet<EntryField>();
		excludedFields.add(EntryField.TIME);
		listObfuscationFieldsModel = new DefaultListModel();
	}

	@Override
	protected void addComponents() throws Exception {
		setLayout(new BorderLayout());
		JLabel lblObfuscation = new JLabel("Obfuscation forbidden for fields:");
		add(lblObfuscation, BorderLayout.PAGE_START);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(getListObfuscationFields());
		add(scrollPane, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		BoxLayout layout = new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS);
		buttonPanel.setLayout(layout);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(getButtonAddObfuscationField());
		buttonPanel.add(Box.createHorizontalGlue());
		add(buttonPanel, BorderLayout.PAGE_END);
	}
	
	private JList getListObfuscationFields(){
		if(listObfuscationFields == null){
			listObfuscationFields = new JList(listObfuscationFieldsModel);
			listObfuscationFields.setCellRenderer(new AlternatingRowColorListCellRenderer());
			listObfuscationFields.setFixedCellHeight(20);
			listObfuscationFields.setVisibleRowCount(10);
			listObfuscationFields.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			listObfuscationFields.setBorder(null);
			listObfuscationFields.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
						for (Object selectedObject : listObfuscationFields.getSelectedValues()) {
							EntryField field = EntryField.valueOf(selectedObject.toString());
							if (field != null && field != EntryField.TIME)
								excludedFields.remove(field);
						}
						updateObfuscationFieldsList();
					}
				}
				
				@Override
				public void keyPressed(KeyEvent e) {}
			});
			updateObfuscationFieldsList();
		}
		return listObfuscationFields;
	}
	
	private void updateObfuscationFieldsList(){
		listObfuscationFieldsModel.clear();
		for(EntryField obfuscationField: excludedFields){
			listObfuscationFieldsModel.addElement(obfuscationField.toString());
		}
	}
	
	private JButton getButtonAddObfuscationField(){
		if(btnAddObfuscationField == null){
			btnAddObfuscationField = new JButton("Add field");
			btnAddObfuscationField.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					Set<EntryField> obfuscationFieldCandidates = new HashSet<EntryField>(Arrays.asList(EntryField.values()));
					
					obfuscationFieldCandidates.remove(EntryField.ORIGINATOR_CANDIDATES);
					obfuscationFieldCandidates.remove(EntryField.META);
					
					obfuscationFieldCandidates.removeAll(excludedFields);
					
					Set<String> remainingCandidates = new HashSet<String>();
					for(EntryField remainingField: obfuscationFieldCandidates){
						remainingCandidates.add(remainingField.toString());
					}
					
					List<String> newObfuscationFields = null;
					try {
						newObfuscationFields = ValueChooserDialog.showDialog(SwingUtilities.getWindowAncestor(ObfuscationTransformerPanel.this), "Choose obfuscation fields", remainingCandidates, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(ObfuscationTransformerPanel.this), "<html>Cannot launch value chooser dialog dialog.<br>Reason: " + e1.getMessage() + "</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
					}
					
					if(newObfuscationFields != null && !newObfuscationFields.isEmpty()){
						for(String newObfuscationField: newObfuscationFields){
							excludedFields.add(EntryField.valueOf(newObfuscationField));
						}
						updateObfuscationFieldsList();
					}
				}
			});
			btnAddObfuscationField.setBounds(20,170,120,28);
		}
		return btnAddObfuscationField;
	}

	@Override
	public void initializeFields(ObfuscationTransformer transformer) throws Exception {
		excludedFields = new HashSet<EntryField>();
		excludedFields.addAll(transformer.getExcludedFields());
		updateObfuscationFieldsList();
	}

	@Override
	public void validateFieldValues() throws ParameterException {}

	@Override
	public Object[] getParameters() throws Exception {
		return new Object[]{excludedFields};
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

}
