package de.uni.freiburg.iig.telematik.secsy.gui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public abstract class AbstractSimulationDialog extends JDialog {
	
	private static final long serialVersionUID = -5864654213215817665L;
	
	private final JPanel panelContent = new JPanel();
	protected JPanel panelButtons = null;
	
	protected JButton btnOK = null;
	protected JButton btnCancel = null;
	
	protected boolean editMode = false;
	protected Object dialogObject = null;
	
	public AbstractSimulationDialog(Window owner){
		this(owner, (Object[])  null);
	}
	
	public AbstractSimulationDialog(Window owner, Object[] parameters){
		this(owner, false, parameters);
	}
	
	public AbstractSimulationDialog(Window owner, boolean editMode, Object[] parameters){
		super(owner);
		setBounds();
		this.setResizable(false);
		this.setModal(true);
		this.setLocationRelativeTo(owner);
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		this.editMode = editMode;
		
		this.addWindowListener(new WindowListener() {
			
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
				closingProcedure();
			}
			
			@Override
			public void windowClosed(WindowEvent e) {}
			
			@Override
			public void windowActivated(WindowEvent e) {}
			
		});
		
		initialize(parameters);
		
		setTitle();
		
		getContentPane().setLayout(new BorderLayout());
		panelContent.setBorder(new EmptyBorder(5, 5, 5, 5));
		panelContent.setLayout(null);
		getContentPane().add(panelContent, BorderLayout.CENTER);
		getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);
		getRootPane().setDefaultButton(getDefaultButton());
		
		addComponents();
		
		if(editMode){
			prepareEditing();
		}
		
		this.setVisible(true);
	}
	
	protected void setTitle(){}
	
	protected Object getDialogObject(){
		return dialogObject;
	}
	
	protected void setDialogObject(Object value){
		this.dialogObject = value;
	}
	
	protected void prepareEditing(){};
	
	protected void initialize(Object... parameters){};
	
	protected abstract void addComponents();
	
	protected abstract void setBounds();
	
	protected JPanel mainPanel(){
		return panelContent;
	}
	
	protected JPanel getButtonPanel(){
		if(panelButtons == null) {
			panelButtons= new JPanel();
			panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
			panelButtons.add(getOKButton());
			panelButtons.add(getCancelButton());
		}
		return panelButtons;
	}
	
	protected JButton getOKButton(){
		if(btnOK == null){
			btnOK = new JButton("OK");
			btnOK.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					okProcedure();
				}
			});
			btnOK.setActionCommand("OK");
		}
		return btnOK;
	}
	
	protected JButton getCancelButton(){
		if(btnCancel == null){
			btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cancelProcedure();
				}
			});
			btnCancel.setActionCommand("Cancel");
		}
		return btnCancel;
	}
	
	protected JButton getDefaultButton(){
		return btnOK;
	}
	
	protected void okProcedure(){
		dispose();
	}
	
	protected void cancelProcedure(){
		closingProcedure();
	}
	
	protected void closingProcedure(){
		if(!editMode){
			setDialogObject(null);
		}
		dispose();
	}

}
