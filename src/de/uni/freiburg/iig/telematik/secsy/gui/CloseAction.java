package de.uni.freiburg.iig.telematik.secsy.gui;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


public class CloseAction extends AbstractAction {

	private static final long serialVersionUID = 5671673979939142007L;
	
	public CloseAction(){
		super("Close");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.exit(0);
	}

}
