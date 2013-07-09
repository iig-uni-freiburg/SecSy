package de.uni.freiburg.iig.telematik.secsy.gui.permission;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.invation.code.toval.graphic.Position;
import de.invation.code.toval.types.DataUsage;



public class ObjectPermissionPanel extends JPanel implements MouseListener, ItemListener {
	
private static final long serialVersionUID = -6781176581026547212L;
	
	public static final int CHECKBOX_SIZE = 22;
	public static final int CHECKBOX_MARGIN = 5;
	public static final int MARGIN = 5;
	
	protected String attribute = null;
	protected Map<DataUsage,JCheckBox> checkBoxes = new HashMap<DataUsage,JCheckBox>();
	protected int selectedBoxes = 0;
	protected List<Position> positions = new ArrayList<Position>();
	protected Set<DataUsage> validDataUsageModes = null;
	
	private boolean initializing = false;
	
	private ObjectPermissionListenerSupport permissionListenerSupport = new ObjectPermissionListenerSupport();
	
	public ObjectPermissionPanel(String attribute, Set<DataUsage> validDataUsageModes){
		super();
		this.attribute = attribute;
		this.validDataUsageModes = validDataUsageModes;
		setOpaque(true);
		setLayout(null);
		determineCheckBoxCoordinates();
		int positionCount = 0;
		for(DataUsage dataUsageMode : validDataUsageModes){
			createCheckBox(dataUsageMode, positions.get(positionCount++));
			checkBoxes.get(dataUsageMode).addItemListener(this);
		}
		this.addMouseListener(this);
		this.setFocusable(true);
		setBackground(Color.green);
		setPreferredSize(getPreferredPanelSize());
	}
	
	protected void determineCheckBoxCoordinates(){
		for(int i=1; i<=validDataUsageModes.size(); i++){
			positions.add(new Position(MARGIN + (i-1) * (CHECKBOX_SIZE + CHECKBOX_MARGIN), MARGIN));
		}
//		setSize(getPreferredSize());
	}
	
	protected Dimension getPreferredPanelSize(){
		return new Dimension(new Dimension(MARGIN + validDataUsageModes.size() * (CHECKBOX_SIZE + CHECKBOX_MARGIN) + MARGIN, CHECKBOX_SIZE + 2 * MARGIN));
	}
	

	public void addPermissionItemListener(ObjectPermissionItemListener listener){
		permissionListenerSupport.addPermissionItemListener(listener);
	}
	
	public void removePermissionItemListener(ObjectPermissionItemListener listener){
		permissionListenerSupport.removePermissionItemListener(listener);
	}
	
	
	@Override
	public String getToolTipText() {
		return attribute;
	}

	public void setPermission(Collection<DataUsage> dataUsageModes){
		initializing = true;
		deselectAll();
		if(dataUsageModes == null)
			return;
		for(DataUsage dataUsageMode: dataUsageModes){
			checkBoxes.get(dataUsageMode).setSelected(true);
			selectedBoxes++;
		}
		initializing = false;
	}
	
	private void deselectAll(){
		for(JCheckBox checkBox: checkBoxes.values())
			checkBox.setSelected(false);
		selectedBoxes = 0;
		
	}
	
	private void selectAll(){
		for(JCheckBox checkBox: checkBoxes.values())
			checkBox.setSelected(true);
		selectedBoxes = checkBoxes.values().size();
	}
	
	private void createCheckBox(DataUsage dataUsageMode, Position position){
		ObjectPermissionCheckBox newCheckBox = new ObjectPermissionCheckBox(dataUsageMode);
		newCheckBox.setBounds(position.getX(), position.getY(), CHECKBOX_SIZE, CHECKBOX_SIZE);
		add(newCheckBox);
		checkBoxes.put(dataUsageMode, newCheckBox);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 2){
			if(selectedBoxes == 0){
				selectAll();
			} else {
				deselectAll();
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if(initializing)
			return;
		ObjectPermissionItemEvent permissionEvent = new ObjectPermissionItemEvent(e, ((ObjectPermissionCheckBox) e.getSource()).getDataUsageMode());
		permissionEvent.setAttribute(attribute);
		permissionListenerSupport.firePermissionChangedEvent(permissionEvent);
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		Set<DataUsage> testset = new HashSet<DataUsage>(Arrays.asList(DataUsage.values()));
//		testset.remove(DataUsage.CREATE);
//		testset.remove(DataUsage.DELETE);
		ObjectPermissionPanel panel = new ObjectPermissionPanel("eee", testset);
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);
	}

}
