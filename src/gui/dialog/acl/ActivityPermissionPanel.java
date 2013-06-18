package gui.dialog.acl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class ActivityPermissionPanel extends JPanel implements ItemListener, MouseListener {
	
	private static final long serialVersionUID = -6781176581026547212L;
	
	private int checkboxSize = 22;
	private int margin = 5;
	private String name = null;
	private List<ItemListener> listeners = new ArrayList<ItemListener>();
	private JCheckBox checkbox = null;
	
	public ActivityPermissionPanel(String name){
		this.name = name;
		setOpaque(true);
		setLayout(null);
		this.setPreferredSize(new Dimension(margin + margin + checkboxSize, margin + margin + checkboxSize));
		this.setSize(getPreferredSize());
		
		checkbox = new JCheckBox("");
		checkbox.addItemListener(this);
		checkbox.setBounds(margin-3,margin-2,checkboxSize,checkboxSize);
		checkbox.setVisible(true);
		this.add(checkbox);
		
		this.addMouseListener(this);
		this.setBackground(Color.green);
		this.setFocusable(true);
	}
	
	public void addItemListener(ItemListener listener){
		this.listeners.add(listener);
	}
	
	
	@Override
	public String getToolTipText() {
		return name;
	}

	public void setPermission(boolean permissionGranted){
		checkbox.setSelected(permissionGranted);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		for(ItemListener listener: listeners){
			listener.itemStateChanged(e);
		}
	}
	
	public Dimension preferredCellSize(){
		return getPreferredSize();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		checkbox.setSelected(!checkbox.isSelected());
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
	
}