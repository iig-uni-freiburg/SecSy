package gui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class JButtonTableExample extends JFrame {

	public JButtonTableExample() {
		super("JButtonTable Example");

		DefaultTableModel dm = new DefaultTableModel();
		dm.setDataVector(new Object[][] { { "1" }, { "2" } },
				new Object[] { "Buttons" });

		JTable table = new JTable(dm);
		table.getColumn("Buttons").setCellRenderer(new PanelRenderer());
		table.getColumn("Buttons").setCellEditor(
				new PanelEditor(new JCheckBox()));
		JScrollPane scroll = new JScrollPane(table);
		table.setRowHeight(30);
		getContentPane().add(scroll);
		setSize(400, 150);
		setVisible(true);
	}

	public static void main(String[] args) {
		JButtonTableExample frame = new JButtonTableExample();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	class PanelRenderer extends JPanel implements TableCellRenderer {

		public PanelRenderer() {
			setOpaque(true);
			init();
		}

		private void init() {
			setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
			add(new JButton("A"));
			add(new JButton("B"));
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(UIManager.getColor("Button.background"));
			}
			return this;
		}
	}

	class PanelEditor extends DefaultCellEditor {
		protected JPanel panel;

		protected JButton button1;

		protected JButton button2;

		public PanelEditor(JCheckBox checkBox) {
			super(checkBox);
			panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
			button1 = new JButton("1");
			button1.setOpaque(true);
			button1.setActionCommand("Action1");
			button1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(button1, button1.getText()
							+ ": Ouch!");
					fireEditingStopped();
				}
			});
			button2 = new JButton("2");
			button2.setOpaque(true);
			button2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(button2, button2.getText()
							+ ": Ouch!");
					fireEditingStopped();
				}
			});
			panel.add(button1);
			panel.add(button2);
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			if (isSelected) {
				button1.setForeground(table.getSelectionForeground());
				button1.setBackground(table.getSelectionBackground());
			} else {
				button1.setForeground(table.getForeground());
				button1.setBackground(table.getBackground());
			}
			return panel;
		}
	}
}