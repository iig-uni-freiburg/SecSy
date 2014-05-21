package de.uni.freiburg.iig.telematik.secsy.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

public class MessageDialog extends JFrame {

	private static final long serialVersionUID = 463955903504300506L;
	
	private static MessageDialog instance = null;
	
	private DefaultListModel messageListModel = new DefaultListModel();
	private MessageDialog(){
		super();
		setTitle("Message Dialog");
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(600,500));
		
		getContentPane().setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		topPanel.setPreferredSize(new Dimension(200,20));
		topPanel.setLayout(null);
		getContentPane().add(topPanel, BorderLayout.NORTH);
		
		JPanel midPanel = new JPanel();
		midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.X_AXIS));
		midPanel.add(Box.createRigidArea(new Dimension(20,0)));
		
		final JList list = new JList(messageListModel);
		list.setFont(new Font("Monospaced", Font.PLAIN, 12));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(200,100));
		scrollPane.setViewportView(list);
		
		midPanel.add(scrollPane);
		
		midPanel.add(Box.createRigidArea(new Dimension(20,0)));
		
		getContentPane().add(midPanel, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		bottomPanel.setPreferredSize(new Dimension(200,40));
		bottomPanel.add(Box.createHorizontalGlue());
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		bottomPanel.add(okButton);
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				messageListModel.clear();
			}
		});
		bottomPanel.add(clearButton);
		bottomPanel.add(Box.createHorizontalGlue());
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	public static MessageDialog getInstance(){
		if(instance == null){
			instance = new MessageDialog();
		}
		return instance;
	}
	
	public void addMessage(String message){
		messageListModel.addElement(message);
	}
	
//	public static void main(String[] args) {
//		MessageDialog.getInstance().addMessage("Trst1");
//		MessageDialog.getInstance().newLine();
//		MessageDialog.getInstance().addMessage("Trst2");
//		MessageDialog.getInstance().addMessage("Trst3");
//		MessageDialog.getInstance().addMessage("Trst4");
//	}
	
	public void newLine(){
		messageListModel.addElement(" ");
	}
}
