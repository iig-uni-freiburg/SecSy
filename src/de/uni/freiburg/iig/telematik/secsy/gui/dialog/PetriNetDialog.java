package de.uni.freiburg.iig.telematik.secsy.gui.dialog;

import de.invation.code.toval.graphic.dialog.AbstractDialog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.invation.code.toval.graphic.renderer.AlternatingRowColorListCellRenderer;
import de.invation.code.toval.validate.ExceptionDialog;
import de.uni.freiburg.iig.telematik.secsy.gui.SimulationComponents;
import de.uni.freiburg.iig.telematik.sepia.graphic.GraphicalPTNet;
import de.uni.freiburg.iig.telematik.sepia.petrinet.abstr.AbstractPetriNet;
import de.uni.freiburg.iig.telematik.sepia.petrinet.pt.PTNet;
import java.awt.Dimension;

public class PetriNetDialog<N extends AbstractPetriNet> extends AbstractDialog<N> {

    static final long serialVersionUID = -1556400321094068143L;
    private static final Dimension PREFERRED_SIZE = new Dimension(312, 241);

    private JList netList = null;
    private DefaultListModel netListModel = new DefaultListModel();

    private PTNet petriNet = null;

    public PetriNetDialog(Window owner) {
        super(owner);
    }

    @Override
    protected void addComponents() throws Exception {
        mainPanel().setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(20, 20, 270, 142);
        scrollPane.setViewportView(getNetList());
        mainPanel().add(scrollPane, BorderLayout.CENTER);

        updateNetList();
    }

    private JList getNetList() {
        if (netList == null) {
            netList = new JList(netListModel);
            netList.setCellRenderer(new AlternatingRowColorListCellRenderer());
            netList.setFixedCellHeight(20);
            netList.setVisibleRowCount(10);
            netList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            netList.setBorder(BorderFactory.createLineBorder(Color.black));
            netList.setBounds(109, 203, 151, -82);

            netList.addListSelectionListener(
                    new ListSelectionListener() {
                        public void valueChanged(ListSelectionEvent e) {
                            if ((e.getValueIsAdjusting() == false) && (netList.getSelectedValue() != null)) {
                                try {
                                    petriNet = SimulationComponents.getInstance().getContainerPTNets().getComponent(netList.getSelectedValue().toString()).getPetriNet();
                                } catch (Exception ex) {
                                    ExceptionDialog.showException(PetriNetDialog.this, "Internal Exception", new Exception("Cannot extract Petri net from simulation components", ex), true);
                                }
                            }
                        }
                    }
            );
        }
        return netList;
    }

    private void updateNetList() throws Exception {
        netListModel.clear();
        if (SimulationComponents.getInstance().getContainerPTNets().containsComponents()) {
            for (GraphicalPTNet ptNet : SimulationComponents.getInstance().getContainerPTNets().getComponents()) {
                netListModel.addElement(ptNet.getName());
            }
            netList.setSelectedIndex(0);
            try {
                petriNet = SimulationComponents.getInstance().getContainerPTNets().getComponent(netList.getSelectedValue().toString()).getPetriNet();
            } catch (Exception ex) {
                ExceptionDialog.showException(PetriNetDialog.this, "Internal Exception", new Exception("Cannot extract Petri net from simulation components", ex), true);
            }
        }
    }

    public PTNet getPetriNet() {
        return petriNet;
    }

    public static PTNet showPetriNetDialog(Window parentWindow) {
        PetriNetDialog dialog = new PetriNetDialog(parentWindow);
        return dialog.getPetriNet();
    }

    public static void main(String[] args) {
        new PetriNetDialog(null);
    }

    @Override
    protected void setTitle() {
        setTitle("Choose Petri net");
    }
}
