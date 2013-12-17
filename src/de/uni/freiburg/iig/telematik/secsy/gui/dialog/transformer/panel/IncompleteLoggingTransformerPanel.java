package de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.IncompleteLoggingTransformer;

public class IncompleteLoggingTransformerPanel extends AbstractTransformerPanel<IncompleteLoggingTransformer>{
	
	private static final long serialVersionUID = -1037468355382519131L;
	
	private JList listSkipActivities;
	private JButton btnAddSkipActivity;
	private DefaultListModel listSkipActivitiesModel;
	
	private Set<String> candidateActivities;
	private Set<String> skipActivities;
	
	public IncompleteLoggingTransformerPanel(Set<String> activities) throws Exception {
		super(activities);
	}

	@Override
	public Dimension getPreferredSize(){
		return new Dimension(320, 200);
	}

	@Override
	protected void initialize() {
		this.candidateActivities = getActivities();
		listSkipActivitiesModel = new DefaultListModel();
		skipActivities = new HashSet<String>();
	}
	
	@Override
	protected void addComponents() {
		setLayout(new BorderLayout());
		add(new JLabel("Skipping allowed for activities:"), BorderLayout.PAGE_START);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(getListSkipActivities());
		add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		BoxLayout layout = new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS);
		buttonPanel.setLayout(layout);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(getButtonAddSkipActivity());
		buttonPanel.add(Box.createHorizontalGlue());
		add(buttonPanel, BorderLayout.PAGE_END);
	}
	
	private JList getListSkipActivities(){
		if(listSkipActivities == null){
			listSkipActivities = new JList(listSkipActivitiesModel);
			listSkipActivities.setCellRenderer(new AlternatingRowColorListCellRenderer());
			listSkipActivities.setFixedCellHeight(20);
			listSkipActivities.setVisibleRowCount(10);
			listSkipActivities.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			listSkipActivities.setBorder(null);
			listSkipActivities.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
						for (Object selectedObject : listSkipActivities.getSelectedValues()) {
							skipActivities.remove(selectedObject);
						}
						updateSkipActivityList();
					}
				}
				
				@Override
				public void keyPressed(KeyEvent e) {}
			});
			updateSkipActivityList();
		}
		return listSkipActivities;
	}
	
	private void updateSkipActivityList(){
		listSkipActivitiesModel.clear();
		for(String skipActivity: skipActivities){
			listSkipActivitiesModel.addElement(skipActivity);
		}
	}
	
	private JButton getButtonAddSkipActivity(){
		if(btnAddSkipActivity == null){
			btnAddSkipActivity = new JButton("Add activity");
			btnAddSkipActivity.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					Set<String> skipActivityCandidates = new HashSet<String>(candidateActivities);
					skipActivityCandidates.removeAll(skipActivities);
					
					List<String> newSkipActivities = null;
					try {
						newSkipActivities = ValueChooserDialog.showDialog(SwingUtilities.getWindowAncestor(IncompleteLoggingTransformerPanel.this), "Choose skip activities", skipActivityCandidates, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(IncompleteLoggingTransformerPanel.this), "<html>Cannot launch value chooser dialog dialog.<br>Reason: " + e1.getMessage() + "</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
					}
					
					if(newSkipActivities != null && !newSkipActivities.isEmpty()){
						for(String newSkipActivity: newSkipActivities){
							skipActivities.add(newSkipActivity);
						}
						updateSkipActivityList();
					}
				}
			});
		}
		return btnAddSkipActivity;
	}

	@Override
	public void initializeFields(IncompleteLoggingTransformer transformer) throws Exception {
		this.skipActivities = new HashSet<String>();
		this.skipActivities.addAll(transformer.getSkipActivities());
		updateSkipActivityList();
	}

	@Override
	public void validateFieldValues() throws ParameterException {
		if(skipActivities.isEmpty())
			throw new ParameterException("No activities for which skipping is permitted.");
	}

	@Override
	public Object[] getParameters() throws Exception {
		return new Object[]{skipActivities};
	}

	@Override
	public boolean isEmpty() {
		return false;
	}


}
