package de.uni.freiburg.iig.telematik.secsy.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import de.invation.code.toval.graphic.dialog.AbstractEditCreateDialog;
import de.invation.code.toval.graphic.dialog.ValueChooserDialog;
import de.invation.code.toval.graphic.renderer.AlternatingRowColorListCellRenderer;
import de.invation.code.toval.validate.ExceptionDialog;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.ParameterException.ErrorCode;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.gui.GUIProperties;
import de.uni.freiburg.iig.telematik.secsy.gui.Hints;
import de.uni.freiburg.iig.telematik.secsy.gui.SimulationComponents;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.TransformerDialog;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.SimulationRun;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.SimulationRunProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerManager;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TransformerComponents;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.AbstractTraceTransformer;
import de.uni.freiburg.iig.telematik.sepia.graphic.AbstractGraphicalPN;
import de.uni.freiburg.iig.telematik.sepia.graphic.GraphicalPTNet;
import de.uni.freiburg.iig.telematik.sepia.parser.graphic.PNParserDialog;
import de.uni.freiburg.iig.telematik.sepia.petrinet.abstr.AbstractPetriNet;
import de.uni.freiburg.iig.telematik.sepia.petrinet.pt.PTNet;
import de.uni.freiburg.iig.telematik.sepia.util.PNUtils;

public class SimulationRunDialog extends AbstractEditCreateDialog<SimulationRun> {

    private static final long serialVersionUID = 5957764226864259142L;

    public static final Dimension PREFERRED_SIZE = new Dimension(420, 460);

    private JButton btnAddTransformer;
    private JButton btnNewTransformer;
    private JButton btnEditTransformer;
    private JButton btnTransformerUp;
    private JButton btnTransformerDown;
    private JButton btnImportNet;
    private JTextField txtName;

    private JComboBox comboNet;

    private JSpinner spinPasses;

    private JList listTransformers;
    private DefaultListModel listTransformersModel;

	//---------------------------------------------------
    private List<AbstractTraceTransformer> transformers;

    public SimulationRunDialog(Window owner) throws Exception {
        super(owner);
    }

    public SimulationRunDialog(Window owner, SimulationRun simulationRun) throws Exception {
        super(owner, simulationRun);
    }

    @Override
    protected void initialize() {
        listTransformersModel = new DefaultListModel();
    }

    @Override
    public Dimension getPreferredSize() {
        return PREFERRED_SIZE;
    }

    @Override
    protected void setTitle() {
        if (!editMode()) {
            setTitle("New Simulation Run");
        } else {
            setTitle("Edit Simulation Run");
        }
    }

    @Override
    protected void prepareEditing() {
        txtName.setText(getDialogObject().getName());
        comboNet.setSelectedItem(getDialogObject().getPetriNet().getName());
        spinPasses.setValue(getDialogObject().getPasses());
        for (AbstractTraceTransformer transformer : getDialogObject().getTraceTransformerManager().getTraceTransformers()) {
            transformers.add(transformer);
        }
        updateListTransformers();
    }

    @Override
    protected void addComponents() {
        mainPanel().setBorder(GUIProperties.DEFAULT_DIALOG_BORDER);
        BoxLayout layout = new BoxLayout(mainPanel(), BoxLayout.PAGE_AXIS);
        mainPanel().setLayout(layout);
        mainPanel().add(getParameterPanel());
        mainPanel().add(Box.createVerticalStrut(5));
        mainPanel().add(new JSeparator(SwingConstants.HORIZONTAL));
        mainPanel().add(Box.createVerticalStrut(5));
        mainPanel().add(getTransformerPanel());
        setResizable(true);
    }

    private JPanel getTransformerPanel() {
        JPanel transformerPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblTransformer = new JLabel("Trace Transformer:");
        topPanel.add(lblTransformer);
        transformerPanel.add(topPanel, BorderLayout.PAGE_START);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(getListTransformers());
        transformerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        BoxLayout layout = new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS);
        buttonPanel.setLayout(layout);
        buttonPanel.add(getButtonAddTransformer());
        buttonPanel.add(getButtonNewTransformer());
        buttonPanel.add(getButtonEditTransformer());
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(getButtonTransformerUp());
        buttonPanel.add(getButtonTransformerDown());
        transformerPanel.add(buttonPanel, BorderLayout.PAGE_END);

        return transformerPanel;
    }

    private JPanel getParameterPanel() {
        JPanel parameterPanel = new JPanel();
        BoxLayout layout = new BoxLayout(parameterPanel, BoxLayout.PAGE_AXIS);
        parameterPanel.setLayout(layout);

        int labelWidth = 120;
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblName = new JLabel("Name:");
        lblName.setPreferredSize(new Dimension(labelWidth, GUIProperties.DEFAULT_LABEL_HEIGHT));
        lblName.setHorizontalAlignment(SwingConstants.TRAILING);
        namePanel.add(lblName);
        txtName = new JTextField();
        txtName.setText(SimulationRunProperties.defaultName);
        namePanel.add(txtName);
        parameterPanel.add(namePanel);

        JPanel netPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblPetriNet = new JLabel("Petri net:");
        lblPetriNet.setPreferredSize(new Dimension(labelWidth, GUIProperties.DEFAULT_LABEL_HEIGHT));
        lblPetriNet.setHorizontalAlignment(SwingConstants.TRAILING);
        netPanel.add(lblPetriNet);
        netPanel.add(getComboNet());
        netPanel.add(getButtonImportNet());
        parameterPanel.add(netPanel);

        JPanel traverserPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblTraverser = new JLabel("Traverser:");
        lblTraverser.setPreferredSize(new Dimension(labelWidth, GUIProperties.DEFAULT_LABEL_HEIGHT));
        lblTraverser.setHorizontalAlignment(SwingConstants.TRAILING);
        traverserPanel.add(lblTraverser);
        JComboBox comboBox = new JComboBox();
        comboBox.setModel(new DefaultComboBoxModel(new String[]{"RANDOM TRAVERSAL"}));
        comboBox.setToolTipText(Hints.hintRandomTraversal);
        traverserPanel.add(comboBox);
        parameterPanel.add(traverserPanel);

        JPanel passesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblPasses = new JLabel("Passes:");
        lblPasses.setPreferredSize(new Dimension(labelWidth, GUIProperties.DEFAULT_LABEL_HEIGHT));
        lblPasses.setHorizontalAlignment(SwingConstants.TRAILING);
        passesPanel.add(lblPasses);
        spinPasses = new JSpinner();
        SpinnerModel model = new SpinnerNumberModel(1000, 1, 1000000, 10);
        spinPasses.setModel(model);
        passesPanel.add(spinPasses);
        parameterPanel.add(passesPanel);

        return parameterPanel;
    }

    private JComboBox getComboNet() {
        if (comboNet == null) {
            comboNet = new JComboBox();
            updateComboNet();
        }
        return comboNet;
    }

    private void updateComboNet() {
        List<String> netNames = new ArrayList<String>();
        try {
            for (GraphicalPTNet net : SimulationComponents.getInstance().getContainerPTNets().getComponents()) {
                netNames.add(net.getName());
            }
        } catch (Exception e) {
            ExceptionDialog.showException(SimulationRunDialog.this, "Internal Exception", new Exception("Cannot extract Petri nets from simulation components", e), true);
        }
        comboNet.setModel(new DefaultComboBoxModel(netNames.toArray()));
    }

    private JList getListTransformers() {
        if (listTransformers == null) {
            listTransformers = new JList(listTransformersModel);
            listTransformers.setToolTipText(Hints.hintTransformerList);
            listTransformers.setCellRenderer(new AlternatingRowColorListCellRenderer());
            listTransformers.setFixedCellHeight(20);
            listTransformers.setVisibleRowCount(10);
            listTransformers.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            listTransformers.setBorder(null);

            listTransformers.addKeyListener(new KeyListener() {

                @Override
                public void keyTyped(KeyEvent e) {
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                        removeSelectedTransformers();
                        updateListTransformers();
                    }
                }

                @Override
                public void keyPressed(KeyEvent e) {
                }
            });
        }
        updateListTransformers();
        return listTransformers;
    }

    private void updateListTransformers() {
        listTransformersModel.clear();
        for (AbstractTraceTransformer transformer : transformers) {
            listTransformersModel.addElement(transformer);
        }
    }

    private void removeSelectedTransformers() {
        if (listTransformers.getSelectedValues() == null || listTransformers.getSelectedValues().length == 0) {
            return;
        }
        for (Object selectedTransformer : listTransformers.getSelectedValues()) {
            transformers.remove((AbstractTraceTransformer) selectedTransformer);
        }
    }

    private JButton getButtonAddTransformer() {
        if (btnAddTransformer == null) {
            btnAddTransformer = new JButton("Add");
            btnAddTransformer.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    TransformerComponents components = null;
                    try{
                        components = TransformerComponents.getInstance();
                    } catch (Exception ex) {
                        ExceptionDialog.showException(SimulationRunDialog.this, "Internal Exception", new Exception("Cannot access simulation components", ex), true);
                    }
                    
                    if (components.containsTransformers()) {
                        List<String> transformerNames = new ArrayList<>(components.getAllTransformerNames());
                        Collections.sort(transformerNames);
                        List<String> chosenTransformers = null;
                        try {
                            chosenTransformers = ValueChooserDialog.showDialog(SimulationRunDialog.this, "Choose Existing Transformer", transformerNames, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                        } catch (Exception e2) {
                            ExceptionDialog.showException(SimulationRunDialog.this, "Internal Exception", new Exception("Cannot launch value chooser dialog dialog", e2), true);
                            return;
                        }
                        if (chosenTransformers != null) {
                            for (String transformerName : chosenTransformers) {
                                try {
                                    transformers.add(components.getTransformer(transformerName));
                                } catch (Exception e1) {
                                    ExceptionDialog.showException(SimulationRunDialog.this, "Internal Exception", new Exception("Cannot add transformer \"" + transformerName + "\"", e1), true);
                                    return;
                                }
                            }
                            updateListTransformers();
                        }
                    } else {
                        JOptionPane.showMessageDialog(SimulationRunDialog.this, "Simulation components do not contain any transformers yet.", "Simulation Components", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });
        }
        return btnAddTransformer;
    }

    private JButton getButtonNewTransformer() {
        if (btnNewTransformer == null) {
            btnNewTransformer = new JButton("New");
            btnNewTransformer.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (comboNet.getSelectedItem() == null) {
                        JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot add new transformer without Petri net.", "Missing Requirement", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    AbstractTraceTransformer newTransformer = null;
                    try {
                        newTransformer = TransformerDialog.showDialog(SimulationRunDialog.this, getActivities());
                    } catch (Exception e2) {
                        JOptionPane.showMessageDialog(SimulationRunDialog.this, "<html>Cannot launch transformer dialog.<br>Reason: " + e2.getMessage() + "</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
                    }
                    if (newTransformer == null) {
                        //User cancelled transformer dialog.
                        return;
                    }

                    try {
                        TransformerComponents.getInstance().addTransformer(newTransformer, true);
                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(SimulationRunDialog.this, "<html>Cannot add new transformer \"" + newTransformer.getName() + "\" to simulation components.<br>Reason: " + e1.getMessage() + "</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    transformers.add(newTransformer);
                    updateListTransformers();
                }
            });
        }
        return btnNewTransformer;
    }

    private JButton getButtonEditTransformer() {
        if (btnEditTransformer == null) {
            btnEditTransformer = new JButton("Edit");
            btnEditTransformer.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (listTransformers.getSelectedValue() == null) {
                        return;
                    }
                    AbstractTraceTransformer selectedTransformer = (AbstractTraceTransformer) listTransformers.getSelectedValue();

                    String oldTransformerName = selectedTransformer.getName();
                    AbstractTraceTransformer adjustedTransformer = null;
                    try {
                        adjustedTransformer = TransformerDialog.showDialog(SimulationRunDialog.this, getActivities(), selectedTransformer);
                    } catch (Exception e2) {
                        JOptionPane.showMessageDialog(SimulationRunDialog.this, "<html>Cannot launch transformer dialog.<br>Reason: " + e2.getMessage() + "</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
                    }
                    if (adjustedTransformer == null) {
                        //User cancelled the transformer dialog
                        return;
                    }
                    if (!oldTransformerName.equals(adjustedTransformer.getName())) {
                        try {
                            TransformerComponents.getInstance().removeTransformer(oldTransformerName, true);
                            TransformerComponents.getInstance().addTransformer(adjustedTransformer, true);
                        } catch (Exception e1) {
                            JOptionPane.showMessageDialog(SimulationRunDialog.this, "Cannot add transformer\"" + adjustedTransformer.getName() + "\" under new name to simulation components.\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    updateListTransformers();
                }
            });
        }
        return btnEditTransformer;
    }

    private JButton getButtonTransformerUp() {
        if (btnTransformerUp == null) {
            btnTransformerUp = new JButton("Up");
            btnTransformerUp.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (listTransformers.getSelectedValue() == null) {
                        return;
                    }
                    int selectedIndex = listTransformers.getSelectedIndex();
                    if (listTransformersModel.size() > 1 && selectedIndex > 0) {
                        Collections.swap(transformers, selectedIndex, selectedIndex - 1);
                        updateListTransformers();
                        listTransformers.setSelectedIndex(selectedIndex - 1);
                    }
                }
            });
        }
        return btnTransformerUp;
    }

    private JButton getButtonTransformerDown() {
        if (btnTransformerDown == null) {
            btnTransformerDown = new JButton("Down");
            btnTransformerDown.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (listTransformers.getSelectedValue() == null) {
                        return;
                    }
                    int selectedIndex = listTransformers.getSelectedIndex();
                    if (listTransformersModel.size() > 1 && selectedIndex < listTransformersModel.size() - 1) {
                        Collections.swap(transformers, selectedIndex, selectedIndex + 1);
                        updateListTransformers();
                        listTransformers.setSelectedIndex(selectedIndex + 1);
                    }
                }
            });
        }
        return btnTransformerDown;
    }

    private JButton getButtonImportNet() {
        if (btnImportNet == null) {
            btnImportNet = new JButton("Import...");
            btnImportNet.addActionListener(new ActionListener() {

                @SuppressWarnings("rawtypes")
                @Override
                public void actionPerformed(ActionEvent e) {

                    AbstractGraphicalPN importedNet = PNParserDialog.showPetriNetDialog(SimulationRunDialog.this);
                    if (importedNet != null) {

                        AbstractPetriNet loadedNet = importedNet.getPetriNet();
                        if (!(loadedNet instanceof PTNet)) {
                            ExceptionDialog.showException(SimulationRunDialog.this, "Unexpected Petri net type", new Exception("Loaded Petri net is not a P/T Net, cannot proceed"), true);
                            return;
                        }
                        PTNet petriNet = (PTNet) loadedNet;

                        String netName = petriNet.getName();
                        try {
                            while (netName == null || SimulationComponents.getInstance().getContainerPTNets().containsComponent(netName)) {
                                netName = JOptionPane.showInputDialog(SimulationRunDialog.this, "Name for the Petri net:", "");
                            }
                        } catch (Exception e1) {
                            ExceptionDialog.showException(SimulationRunDialog.this, "Internal Exception", new Exception("Cannot check if net name is already in use", e1), true);
                            return;
                        }
                        try {
                            if (!petriNet.getName().equals(netName)) {
                                petriNet.setName(netName);
                            }
                        } catch (Exception e2) {
                            ExceptionDialog.showException(SimulationRunDialog.this, "Internal Exception", new Exception("Cannot change Petri net name to\"" + netName + "\"", e2), true);
                            return;
                        }

                        try {
                            SimulationComponents.getInstance().getContainerPTNets().addComponent(new GraphicalPTNet(petriNet, null));
                        } catch (Exception e1) {
                            ExceptionDialog.showException(SimulationRunDialog.this, "Internal Exception", new Exception("Cannot add imported net to simulation components", e1), true);
                            return;
                        }
                        updateComboNet();
                    } else {
                        //User aborted the dialog.
                    }
                }
            });
        }
        return btnImportNet;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Set<String> getActivities() {
        AbstractPetriNet ptNet = null;
        try {
            ptNet = getSelectedPetriNet();
        } catch (Exception e1) {
            ExceptionDialog.showException(SimulationRunDialog.this, "Internal Exception", new Exception("Cannot extract Petri net from simulation run", e1), true);
            return null;
        }
        if (ptNet == null) {
            ExceptionDialog.showException(SimulationRunDialog.this, "Internal Exception", new Exception("Petri net reference in simulation run is NULL"), true);
            return null;
        }

        Set<String> activities = null;
        try {
            activities = PNUtils.getLabelSetFromTransitions(ptNet.getTransitions(), false);
        } catch (Exception e1) {
            ExceptionDialog.showException(SimulationRunDialog.this, "Internal Exception", new Exception("Cannot extract activity names from Petri net \"" + ptNet.getName() + "\".", e1), true);
            return null;
        }
        return activities;
    }

    public static SimulationRun showDialog(Window owner) throws Exception {
        SimulationRunDialog dialog = new SimulationRunDialog(owner);
        dialog.setUpGUI();
        return dialog.getDialogObject();
    }

    public static SimulationRun showDialog(Window owner, SimulationRun simulationRun) throws Exception {
        SimulationRunDialog dialog = new SimulationRunDialog(owner, simulationRun);
        dialog.setUpGUI();
        return dialog.getDialogObject();
    }

    @Override
    protected Border getBorder() {
        return GUIProperties.DEFAULT_DIALOG_BORDER;
    }

    @Override
    protected SimulationRun newDialogObject(Object... parameters) {
        return new SimulationRun();
    }

    @Override
    protected boolean validateAndSetFieldValues() throws Exception {
        //Validate transformer name
        String runName = txtName.getText();
        Validate.notEmpty(runName);
        getDialogObject().setName(runName);
        
        //Get number of passes
        if (spinPasses.getValue() == null)
            throw new ParameterException("Invalid number of passes");

        Integer passes = null;
        try {
            passes = Integer.valueOf(spinPasses.getValue().toString());
        } catch (Exception ex) {
            throw new ParameterException("Invalid number of passes");
        }
        getDialogObject().setPasses(passes);

        // Get transformers
        TraceTransformerManager transformerManager = new TraceTransformerManager();
        //Add transformers to transformer manager.
        for (AbstractTraceTransformer transformer : transformers) {
            try {
                transformerManager.addTransformer(transformer);
            } catch (Exception ex) {
                throw new ParameterException(ErrorCode.INCOMPATIBILITY, "Incompatible trace transformer.", ex);
            }
        }
        getDialogObject().setTraceTransformerManager(transformerManager);
        
        getDialogObject().setPetriNet(getSelectedPetriNet());
        return true;
    }
    
    private AbstractPetriNet getSelectedPetriNet() throws Exception{
        if (comboNet.getSelectedItem() == null)
             throw new Exception("No Petri net chosen");
   
        PTNet petriNet = null;
        try {
            petriNet = SimulationComponents.getInstance().getContainerPTNets().getComponent(comboNet.getSelectedItem().toString()).getPetriNet();
        } catch (Exception ex) {
            throw new Exception("Cannot extract Petri net from simulation components.", ex);
        }
        if (petriNet == null) {
            throw new Exception("Cannot extract Petri net from simulation components.");
        }
        return petriNet;
    }

}
