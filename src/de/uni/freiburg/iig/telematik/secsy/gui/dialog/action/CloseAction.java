/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.secsy.gui.dialog.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 *
 * @author stocker
 */
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