package de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import de.invation.code.toval.graphic.component.BoxLayoutPanel;
import de.invation.code.toval.graphic.dialog.ValueChooserDialog;
import de.invation.code.toval.graphic.renderer.AlternatingRowColorListCellRenderer;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.gui.GUIProperties;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.AbstractTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.TransformerDialog;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.SoDBoDPropertyTransformer;

public abstract class SoDBoDPropertyTransformerPanel<T extends SoDBoDPropertyTransformer> extends AbstractTransformerPanel<T>{

	private static final long serialVersionUID = -2837303124127005710L;
	
	private JList listBindingActivities;
	private DefaultListModel listBindingActivitiesModel;
	private JButton btnAddBindingActivity;
	private JTextField txtViolation;
	
	protected Set<String> bindingActivities;

	public SoDBoDPropertyTransformerPanel(Set<String> activities) throws Exception {
		super(activities);
	}

	@Override
	protected void initialize() throws Exception {
		listBindingActivitiesModel = new DefaultListModel();
		bindingActivities = new HashSet<String>();
	}

	@Override
	protected void addComponents() throws Exception {
		BoxLayout layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
		setLayout(layout);
		
		JPanel activationPanel = new BoxLayoutPanel();
		JLabel lblViolation = new JLabel("Violation:");
		lblViolation.setHorizontalAlignment(JLabel.TRAILING);
		lblViolation.setPreferredSize(new Dimension(TransformerDialog.LABEL_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
		lblViolation.setMaximumSize(new Dimension(TransformerDialog.LABEL_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
		activationPanel.add(lblViolation);
		txtViolation = new JTextField();
		activationPanel.add(txtViolation);
		txtViolation.setText("0");
		txtViolation.setPreferredSize(new Dimension(TransformerDialog.FIELD_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
		txtViolation.setMaximumSize(new Dimension(TransformerDialog.FIELD_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
		activationPanel.add(txtViolation);
		JLabel lblPercentage = new JLabel("%");
		activationPanel.add(lblPercentage);
		activationPanel.add(Box.createHorizontalGlue());
		add(activationPanel);
		
		add(Box.createVerticalStrut(5));
		
		JPanel labelPanel = new BoxLayoutPanel();
		JLabel lblBinding = new JLabel("Scope of property:");
		labelPanel.add(lblBinding);
		labelPanel.add(Box.createHorizontalGlue());
		add(labelPanel);
		
		add(Box.createVerticalStrut(5));
		
		JPanel scrollPanel = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(getListBindingActivities());
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(300,100));
		scrollPane.setMaximumSize(new Dimension(300,100));
		scrollPanel.add(scrollPane, BorderLayout.CENTER);
		add(scrollPanel);
		
		add(Box.createVerticalStrut(5));
		
		JPanel buttonPanel = new BoxLayoutPanel();
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(getButtonAddBindingActivity());
		buttonPanel.add(Box.createHorizontalGlue());
		add(buttonPanel);
	}
	
	private JList getListBindingActivities(){
		if(listBindingActivities == null){
			listBindingActivities = new JList(listBindingActivitiesModel);
			listBindingActivities.setCellRenderer(new AlternatingRowColorListCellRenderer());
			listBindingActivities.setFixedCellHeight(20);
			listBindingActivities.setVisibleRowCount(10);
			listBindingActivities.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			listBindingActivities.setBorder(null);
			listBindingActivities.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
						for(Object selectedObject: listBindingActivities.getSelectedValues()){
							bindingActivities.remove(selectedObject.toString());
						}
						updateBindingActivitiesList();
					}
				}
				
				@Override
				public void keyPressed(KeyEvent e) {}
			});
			updateBindingActivitiesList();
		}
		return listBindingActivities;
	}
	
	private void updateBindingActivitiesList(){
		listBindingActivitiesModel.clear();
		for(String bindingActivity: bindingActivities){
			listBindingActivitiesModel.addElement(bindingActivity);
		}
	}
	
	private JButton getButtonAddBindingActivity(){
		if(btnAddBindingActivity == null){
			btnAddBindingActivity = new JButton("Add activity");
			btnAddBindingActivity.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Set<String> bindingCandidates = new HashSet<String>(activities);
					bindingCandidates.removeAll(bindingActivities);
					
					List<String> newBindingActivities = null;
					try {
						newBindingActivities = ValueChooserDialog.showDialog(SwingUtilities.getWindowAncestor(SoDBoDPropertyTransformerPanel.this), "Choose activities", bindingCandidates, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(SoDBoDPropertyTransformerPanel.this), "<html>Cannot launch value chooser dialog dialog.<br>Reason: " + e1.getMessage() + "</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
					}
					
					if(newBindingActivities != null && !newBindingActivities.isEmpty()){
						for(String newSkipActivity: newBindingActivities){
							bindingActivities.add(newSkipActivity);
						}
						updateBindingActivitiesList();
					}
				}
			});
		}
		return btnAddBindingActivity;
	}
	
	@Override
	public void initializeFields(T transformer) throws Exception {
		txtViolation.setText(new Double(transformer.getViolationProbability()*100.0).toString());
		bindingActivities.addAll((Set<String>) transformer.getActivityGroups().get(0));
		updateBindingActivitiesList();
	}

	@Override
	public void validateFieldValues() throws ParameterException {
		Validate.percentage(txtViolation.getText());
		if(bindingActivities.isEmpty())
			throw new ParameterException("No binding activities chosen.");
		if(bindingActivities.size()<2)
			throw new ParameterException("Please choose at least two activities.");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getParameters() throws Exception {
		return new Object[]{Double.parseDouble(txtViolation.getText()) / 100.0, new HashSet<Set<String>>(Arrays.asList(bindingActivities))};
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

}
