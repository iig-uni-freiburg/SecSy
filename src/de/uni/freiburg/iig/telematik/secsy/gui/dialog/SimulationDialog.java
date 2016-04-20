package de.uni.freiburg.iig.telematik.secsy.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import de.invation.code.toval.graphic.dialog.AbstractEditCreateDialog;
import de.invation.code.toval.graphic.dialog.StringDialog;
import de.invation.code.toval.misc.ArrayUtils;
import de.invation.code.toval.misc.StringUtils;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ExceptionDialog;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.secsy.gui.GUIProperties;
import de.uni.freiburg.iig.telematik.secsy.gui.Hints;
import de.uni.freiburg.iig.telematik.secsy.gui.SimulationComponents;
import de.uni.freiburg.iig.telematik.secsy.gui.properties.SecSyProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.CaseDataContainer;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.context.SynthesisContext;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.DetailedLogEntryGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.LogEntryGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.TraceLogGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.context.SynthesisContextDialog;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.CaseTimeGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.properties.TimeGeneratorFactory;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.properties.TimeProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.ConfigurationException;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.Simulation;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.SimulationRun;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.EntryGenerationType;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.EventHandling;
import de.uni.freiburg.iig.telematik.sepia.util.PNUtils;
import de.uni.freiburg.iig.telematik.sewol.format.AbstractLogFormat;
import de.uni.freiburg.iig.telematik.sewol.format.LogFormatFactory;
import de.uni.freiburg.iig.telematik.sewol.format.LogFormatType;
import de.uni.freiburg.iig.telematik.sewol.writer.PerspectiveException;
import java.io.IOException;

public class SimulationDialog extends AbstractEditCreateDialog<Simulation> {

    private static final long serialVersionUID = 1962737975495538666L;
    public static final Dimension PREFERRED_SIZE = new Dimension(500, 580);

    private static final Dimension DEFAULT_COMBO_BOX_DIMENSION = new Dimension(220, GUIProperties.DEFAULT_LABEL_HEIGHT);
    private static final Dimension DEFAULT_EDIT_BUTTON_DIMENSION = new Dimension(60, GUIProperties.DEFAULT_LABEL_HEIGHT);

    private JComboBox<String> comboEntryGenerator;
    private JComboBox<String> comboContext;
    private JComboBox<String> comboDataContainer;
    private JComboBox<LogFormatType> comboLogFormat;
    private JComboBox<String> comboTimeGenerator;
    private JComboBox<String> comboEventType;

    private JRadioButton rdbtnOneEvent;
    private JRadioButton rdbtnBothEvents;

    private JButton btnAddTimeGenerator;
    private JButton btnAddContext;
    private JButton btnAddDataContainer;
    private JButton btnAddSimulationRun;
    private JButton btnRemoveSimulationRun;
    private JButton btnEditTimeGenerator;
    private JButton btnEditContext;
    private JButton btnEditDataContainer;
    private JButton btnEditSimulationRun;
    private JButton btnShowActivities;

    private JTextField txtLogName;
    private JTextField txtName;

    private JList<SimulationRun> listSimulationRuns;
    private DefaultListModel<SimulationRun> listSimulationRunsModel;
    
    private Map<String, SimulationRun> simulationRuns;

    public SimulationDialog(Window owner) throws Exception {
        super(owner);
    }

    public SimulationDialog(Window owner, Simulation simulation) throws Exception {
        super(owner, simulation);
    }

    @Override
    protected void initialize() {
        listSimulationRunsModel = new DefaultListModel<>();
        simulationRuns = new HashMap<>();
    }

    @Override
    protected void addComponents() {
        BoxLayout layout = new BoxLayout(mainPanel(), BoxLayout.PAGE_AXIS);
        mainPanel().setLayout(layout);

        int labelWidth = 120;

        JPanel panelSimulationName = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSimulationName.add(createLabel("Name:", labelWidth));
        panelSimulationName.add(getSimulationNameField());
        mainPanel().add(panelSimulationName);

        JPanel panelLogName = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelLogName.add(createLabel("File name:", labelWidth));
        panelLogName.add(getLogNameField());
        mainPanel().add(panelLogName);

        JPanel panelLogFormat = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelLogFormat.add(createLabel("Log format:", labelWidth));
        panelLogFormat.add(getComboLogFormat());
        mainPanel().add(panelLogFormat);

        mainPanel().add(Box.createHorizontalStrut(5));
        mainPanel().add(new JSeparator(SwingConstants.HORIZONTAL));
        mainPanel().add(Box.createHorizontalStrut(5));

        JPanel panelEntryGeneration = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelEntryGeneration.add(createLabel("Entry generation:", labelWidth));
        panelEntryGeneration.add(getComboEntryGenerator());
        mainPanel().add(panelEntryGeneration);

        JPanel panelContext = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelContext.add(createLabel("Context:", labelWidth));
        panelContext.add(getComboContext());
        panelContext.add(getAddContextButton());
        panelContext.add(getEditContextButton());
        mainPanel().add(panelContext);

        JPanel panelDataContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelDataContainer.add(createLabel("Date container:", labelWidth));
        panelDataContainer.add(getComboDataContainer());
        panelDataContainer.add(getAddDataContainerButton());
        panelDataContainer.add(getEditDataContainerButton());
        mainPanel().add(panelDataContainer);

        JPanel panelTimeGenerator = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTimeGenerator.add(createLabel("Time generator:", labelWidth));
        panelTimeGenerator.add(getComboTimeGenerator());
        panelTimeGenerator.add(getAddTimeGeneratorButton());
        panelTimeGenerator.add(getEditTimeGeneratorButton());
        mainPanel().add(panelTimeGenerator);

        mainPanel().add(Box.createHorizontalStrut(5));
        mainPanel().add(new JSeparator(SwingConstants.HORIZONTAL));
        mainPanel().add(Box.createHorizontalStrut(5));

        JPanel panelEventType1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rdbtnOneEvent = new JRadioButton("Generate only event");
        rdbtnOneEvent.setSelected(true);
        panelEventType1.add(rdbtnOneEvent);
        comboEventType = new JComboBox<>();
        comboEventType.setModel(new DefaultComboBoxModel<>(new String[]{"start", "complete"}));
        comboEventType.setSelectedIndex(1);
        panelEventType1.add(comboEventType);
        mainPanel().add(panelEventType1);
        JPanel panelEventType2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rdbtnBothEvents = new JRadioButton("Generate both, start and complete events");
        panelEventType2.add(rdbtnBothEvents);
        ButtonGroup group = new ButtonGroup();
        group.add(rdbtnOneEvent);
        group.add(rdbtnBothEvents);
        mainPanel().add(panelEventType2);

        mainPanel().add(Box.createHorizontalStrut(5));
        mainPanel().add(new JSeparator(SwingConstants.HORIZONTAL));
        mainPanel().add(Box.createHorizontalStrut(20));

        mainPanel().add(getSimulationRunsPanel());
    }

    private JPanel getSimulationRunsPanel() {
        JPanel panelSimulationRuns = new JPanel(new BorderLayout());
        mainPanel().add(panelSimulationRuns);

        JPanel headerPanel = new JPanel();
        BoxLayout headerLayout = new BoxLayout(headerPanel, BoxLayout.LINE_AXIS);
        headerPanel.setLayout(headerLayout);
        headerPanel.add(new JLabel("Simulation runs:"));
        panelSimulationRuns.add(headerPanel, BorderLayout.PAGE_START);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(getSimulationRunList());
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panelSimulationRuns.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        BoxLayout buttonPanelLayout = new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanel.add(getAddSimulationRunButton());
        buttonPanel.add(getEditSimulationRunButton());
        buttonPanel.add(getRemoveSimulationRunButton());
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(getButtonShowActivities());
        panelSimulationRuns.add(buttonPanel, BorderLayout.PAGE_END);

        return panelSimulationRuns;
    }

    private JLabel createLabel(String text, int labelWidth) {
        JLabel newLabel = new JLabel(text);
        newLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        newLabel.setPreferredSize(new Dimension(labelWidth, GUIProperties.DEFAULT_LABEL_HEIGHT));
        return newLabel;
    }

    //------- COMBO BOXES --------------------------------------------------------------------------------------------------------------------------------
    private JComboBox<String> getComboContext() {
        if (comboContext == null) {
            comboContext = new JComboBox<>();
            comboContext.setPreferredSize(DEFAULT_COMBO_BOX_DIMENSION);
            comboContext.setEnabled(false);
            comboContext.setToolTipText(Hints.hintContext);
            comboContext.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    updateComboDataContainer();
                }
            });
            updateComboContext();
        }
        return comboContext;
    }

    private void updateComboContext() {
        List<String> contextNames = new ArrayList<>();
        try {
            for (SynthesisContext context : SimulationComponents.getInstance().getContainerSynthesisContexts().getComponents()) {
                contextNames.add(context.getName());
            }
        } catch (Exception ex) {
            ExceptionDialog.showException(SimulationDialog.this, "Internal Exception", new Exception("Cannot extract contexts from simulation components"), true);
        }
        comboContext.setModel(new DefaultComboBoxModel(contextNames.toArray()));
    }

    private JComboBox<String> getComboTimeGenerator() {
        if (comboTimeGenerator == null) {
            comboTimeGenerator = new JComboBox<>();
            comboTimeGenerator.setPreferredSize(DEFAULT_COMBO_BOX_DIMENSION);
            comboTimeGenerator.setToolTipText(Hints.hintTimeGenerator);
            updateComboTimeGenerator();
        }
        return comboTimeGenerator;
    }

    private void updateComboTimeGenerator() {
        List<String> generatorNames = new ArrayList<>();
        try {
            for (CaseTimeGenerator timeGenerator : SimulationComponents.getInstance().getContainerTimeGenerators().getComponents()) {
                generatorNames.add(timeGenerator.getName());
            }
        } catch (Exception ex) {
            ExceptionDialog.showException(SimulationDialog.this, "Internal Exception", new Exception("Cannot extract case time generators from simulation components"), true);
        }
        comboTimeGenerator.setModel(new DefaultComboBoxModel(generatorNames.toArray()));
    }

    private JComboBox<String> getComboDataContainer() {
        if (comboDataContainer == null) {
            comboDataContainer = new JComboBox<>();
            comboDataContainer.setPreferredSize(DEFAULT_COMBO_BOX_DIMENSION);
            comboDataContainer.setEnabled(false);
            comboDataContainer.setToolTipText(Hints.hintDataContainer);
            updateComboDataContainer();
        }
        return comboDataContainer;
    }

    private void updateComboDataContainer() {
        List<String> containerNames = new ArrayList<>();
        try {
            if (getSelectedContext() == null) {
                return;
            }
            try {
                for (CaseDataContainer container : SimulationComponents.getInstance().getContainerCaseDataContainers().getComponents()) {
                    containerNames.add(container.getName());
                }
            } catch (Exception ex) {
                ExceptionDialog.showException(SimulationDialog.this, "Internal Exception", new Exception("Cannot extract case data generators from simulation components"), true);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception: Cannot extract contexts from simulation components:\n" + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
        }
        comboDataContainer.setModel(new DefaultComboBoxModel(containerNames.toArray()));
    }

    private JComboBox<String> getComboEntryGenerator() {
        if (comboEntryGenerator == null) {
            comboEntryGenerator = new JComboBox<>();
            comboEntryGenerator.setPreferredSize(DEFAULT_COMBO_BOX_DIMENSION);
            comboEntryGenerator.setModel(new DefaultComboBoxModel<>(new String[]{"SIMPLE (Activity, Time)", "DETAILED (More attributes)"}));
            comboEntryGenerator.setToolTipText(Hints.hintSIMPLEGeneration);
            comboEntryGenerator.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (comboEntryGenerator.getSelectedItem().equals("SIMPLE (Activity, Time)")) {
                        comboEntryGenerator.setToolTipText(Hints.hintSIMPLEGeneration);
                    } else {
                        comboEntryGenerator.setToolTipText(Hints.hintDETAILEDGeneration);
                    }
                    updateVisibility();
                }
            });
        }
        return comboEntryGenerator;
    }

    private JComboBox<LogFormatType> getComboLogFormat() {
        if (comboLogFormat == null) {
            comboLogFormat = new JComboBox<>();
            comboLogFormat.setPreferredSize(DEFAULT_COMBO_BOX_DIMENSION);
            comboLogFormat.setModel(new DefaultComboBoxModel(LogFormatType.values()));
            comboLogFormat.setToolTipText(Hints.hintMXMLFormat);
            comboLogFormat.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent arg0) {
                    if (comboLogFormat.getSelectedItem().equals("MXML")) {
                        comboLogFormat.setToolTipText(Hints.hintMXMLFormat);
                    } else {
                        comboLogFormat.setToolTipText(Hints.hintPLAINFormat);
                    }
                }
            });
        }
        return comboLogFormat;
    }

    //------- BUTTONS -----------------------------------------------------------------------------------------------------------------------------------
    private JButton getAddContextButton() {
        if (btnAddContext == null) {
            btnAddContext = new JButton("Add");
            btnAddContext.setPreferredSize(DEFAULT_EDIT_BUTTON_DIMENSION);
            btnAddContext.addActionListener(new ActionListener() {
		@Override
                public void actionPerformed(ActionEvent e) {
                    SynthesisContext newContext;
                    try {
                        newContext = SynthesisContextDialog.showDialog(SimulationDialog.this);
                    } catch(Exception ex){
                        internalException("Cannot launch SynthesisContexDialog.", ex);
                        return;
                    }
                    if (newContext == null) {
                        // Happens only when context is null
                        // In this case the user aborted the context dialog.
                        return;
                    }

                    try {
                        SimulationComponents.getInstance().getContainerSynthesisContexts().addComponent(newContext);
                        updateComboContext();
                        comboContext.setSelectedItem(newContext.getName());
                    } catch (Exception e1) {
                        // Error on generating context properties or storing the properties to disk.
                        JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception, cannot store new context:\n" + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    updateVisibility();
                }
            });
            btnAddContext.setEnabled(false);
            btnAddContext.setActionCommand("OK");
        }
        return btnAddContext;
    }

    private JButton getEditContextButton() {
        if (btnEditContext == null) {
            btnEditContext = new JButton("Edit");
            btnEditContext.setPreferredSize(DEFAULT_EDIT_BUTTON_DIMENSION);
            btnEditContext.addActionListener(new ActionListener() {
		@Override
                public void actionPerformed(ActionEvent e) {
                    if (comboContext.getSelectedItem() == null) {
                        JOptionPane.showMessageDialog(SimulationDialog.this, "No context chosen.", "Invalid parameter", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String chosenContext = comboContext.getSelectedItem().toString();

                    SynthesisContext context;
                    try {
                        context = SimulationComponents.getInstance().getContainerSynthesisContexts().getComponent(chosenContext);
                    } catch (Exception e2) {
                        // Cannot happen, since "chosenContext" is not null.
                        JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract context: " + chosenContext, "Internal Exception", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (context == null) {
                        JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract context \"" + chosenContext + "\" from simulation components", "Internal Exception", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String oldContextName = context.getName();
                    boolean dialogAccepted = false;
                    try{
                        dialogAccepted = context.showDialog(SimulationDialog.this);
                    } catch(Exception ex){
                        ExceptionDialog.showException(SimulationDialog.this, "Internal Exception", new Exception("Cannot launch context dialog", ex), true);
                    }
                    if (dialogAccepted) {
                        if (!context.getName().equals(oldContextName)) {
                            //Context name changed
                            //-> Remove old context from simulation components and add it again under the new name.
                            //   Since the reference stays the same, this should not change anything with other components referring to this context.
                            try {
                                SimulationComponents.getInstance().getContainerSynthesisContexts().renameComponent(oldContextName, context.getName());
                            } catch (Exception e1) {
                                JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot change name of context.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }

                        updateComboContext();
                        comboContext.setSelectedItem(context.getName());

                        updateVisibility();
                    } else {
                        //User cancelled the edit-dialog
                    }
                }
            });
            btnEditContext.setEnabled(false);
            btnEditContext.setActionCommand("OK");
        }
        return btnEditContext;
    }

    private JButton getAddDataContainerButton() {
        if (btnAddDataContainer == null) {
            btnAddDataContainer = new JButton("Add");
            btnAddDataContainer.setPreferredSize(DEFAULT_EDIT_BUTTON_DIMENSION);
            btnAddDataContainer.addActionListener(new ActionListener() {
		@Override
                public void actionPerformed(ActionEvent arg0) {
                    //Even, when the chosen context contains no attributes,
                    //the case data container dialog is launched.
                    //-> Default value can be set anyway

                    CaseDataContainer newContainer;
                    try {
                        newContainer = DataContainerDialog.showDialog(SimulationDialog.this, getContextAttributes());
                    } catch (ParameterException e) {
                        JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot launch case data container dialog.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (newContainer == null) {
                        //The user aborted the case data container dialog
                        return;
                    }

                    try {
                        SimulationComponents.getInstance().getContainerCaseDataContainers().addComponent(newContainer);
                        updateComboDataContainer();
                        comboDataContainer.setSelectedItem(newContainer.getName());
                    } catch (ParameterException e1) {
                        e1.printStackTrace();
                        // Happens only when container is null
                        // In this case the user aborted the container dialog.
                        return;
                    } catch (Exception e1) {
                        // Error on generating container properties or storing the properties to disk.
                        JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception, cannot store new case data container:\n" + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    updateVisibility();
                }
            });
            btnAddDataContainer.setEnabled(false);
            btnAddDataContainer.setActionCommand("OK");
        }
        return btnAddDataContainer;
    }

    private Set<String> getContextAttributes() {
        Set<String> attributes = new HashSet<>();
        //Check if context is set
        if (comboContext.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(SimulationDialog.this, "Please choose a context first.", "Missing Requirement", JOptionPane.ERROR_MESSAGE);
            return attributes;
        }
        String contextName = comboContext.getSelectedItem().toString();
        SynthesisContext context;
        try {
            context = SimulationComponents.getInstance().getContainerSynthesisContexts().getComponent(contextName);
        } catch (Exception e1) {
            JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract context from simulation components to get attribute information.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
            return attributes;
        }
        return context.getAttributes();
    }

    private JButton getEditDataContainerButton() {
        if (btnEditDataContainer == null) {
            btnEditDataContainer = new JButton("Edit");
            btnEditDataContainer.setPreferredSize(DEFAULT_EDIT_BUTTON_DIMENSION);
            btnEditDataContainer.addActionListener(new ActionListener() {
		@Override
                public void actionPerformed(ActionEvent e) {
                    if (comboDataContainer.getSelectedItem() == null) {
                        JOptionPane.showMessageDialog(SimulationDialog.this, "No data container chosen.", "Invalid parameter", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String chosenContainer = comboDataContainer.getSelectedItem().toString();

                    CaseDataContainer container = null;
                    try {
                        container = SimulationComponents.getInstance().getContainerCaseDataContainers().getComponent(chosenContainer);
                    } catch (Exception e2) {
                        // Cannot happen, since "chosenContainer" is not null.
                        JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract case data container: " + chosenContainer, "Internal Exception", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (container == null) {
                        JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract case data container \"" + chosenContainer + "\" from simulation components", "Internal Exception", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String oldContainerName = container.getName();
                    CaseDataContainer editedContainer;
                    try {
                        editedContainer = DataContainerDialog.showDialog(SimulationDialog.this, getContextAttributes(), container);
                    } catch (ParameterException e2) {
                        JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot launch case data container dialog.\nReason: " + e2.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (editedContainer != null) {
                        if (!editedContainer.getName().equals(oldContainerName)) {
                            //Container name changed
                            //-> Remove old container from simulation components and add it again under the new name.
                            //   Since the reference stays the same, this should not change anything with other components referring to this context.
                            try {
                                SimulationComponents.getInstance().getContainerCaseDataContainers().renameComponent(oldContainerName, editedContainer.getName());
                            } catch (Exception e1) {
                                JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot change name of data container.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        container.takeOverValues(editedContainer);

                        updateComboDataContainer();
                        comboDataContainer.setSelectedItem(editedContainer.getName());

                        updateVisibility();
                    }
                }
            });
            btnEditDataContainer.setEnabled(false);
            btnEditDataContainer.setActionCommand("OK");
        }
        return btnEditDataContainer;
    }

    private JButton getAddSimulationRunButton() {
        if (btnAddSimulationRun == null) {
            btnAddSimulationRun = new JButton("Add");
            btnAddSimulationRun.setPreferredSize(DEFAULT_EDIT_BUTTON_DIMENSION);
            btnAddSimulationRun.addActionListener(new ActionListener() {
		@Override
                public void actionPerformed(ActionEvent arg0) {
                    SimulationRun newSimulationRun = null;
                    try {
                        newSimulationRun = SimulationRunDialog.showDialog(SimulationDialog.this);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(SimulationDialog.this, "<html>Cannot launch simulation run dialog.<br>Reason: " + e.getMessage() + "</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
                    }
                    if (newSimulationRun == null) {
                        // Happens only when context is null
                        // In this case the user aborted the context dialog.
                        return;
                    }

                    while (newSimulationRun.getName() != null && simulationRuns.containsKey(newSimulationRun.getName())) {
                        JOptionPane.showMessageDialog(SimulationDialog.this, "<html>Simulation run name is already in use.<br>You will be propmted to change the name.</html>", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
                        try {
                            SimulationRunDialog.showDialog(SimulationDialog.this, newSimulationRun);
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(SimulationDialog.this, "<html>Cannot launch simulation run dialog.<br>Reason: " + e.getMessage() + "</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    SimulationDialog.this.simulationRuns.put(newSimulationRun.getName(), newSimulationRun);

                    updateSimulationRunList();
                    listSimulationRuns.setSelectedValue(newSimulationRun.getName(), true);
                    updateVisibility();
                }
            });
        }
        return btnAddSimulationRun;
    }

    private JButton getEditSimulationRunButton() {
        if (btnEditSimulationRun == null) {
            btnEditSimulationRun = new JButton("Edit");
            btnEditSimulationRun.setPreferredSize(DEFAULT_EDIT_BUTTON_DIMENSION);
            btnEditSimulationRun.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    if (listSimulationRuns.getSelectedValue() == null) {
                        JOptionPane.showMessageDialog(SimulationDialog.this, "No simulation run chosen.", "Invalid parameter", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    SimulationRun simulationRun;

                    try {
                        simulationRun = (SimulationRun) listSimulationRuns.getSelectedValue();
                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract simulation run from list.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String oldSimulationRunName = simulationRun.getName();
                    try {
                        SimulationRunDialog.showDialog(SimulationDialog.this, simulationRun);
                    } catch (Exception e2) {
                        JOptionPane.showMessageDialog(SimulationDialog.this, "<html>Cannot start simulation run dialog.<br>Reason: " + e2.getMessage() + "</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
                    }

                    if (!simulationRun.getName().equals(oldSimulationRunName)) {
                        //Simulation run name changed
                        //-> Remove old simulation run from simulation run map and add it again under the new name.
                        try {
                            simulationRuns.remove(oldSimulationRunName);
                            simulationRuns.put(simulationRun.getName(), simulationRun);
                        } catch (Exception e1) {
                            JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot change name of simulation run.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    updateSimulationRunList();
                    listSimulationRuns.setSelectedValue(simulationRun, true);

                    updateVisibility();
                }
            });
        }
        return btnEditSimulationRun;
    }

    private JButton getRemoveSimulationRunButton() {
        if (btnRemoveSimulationRun == null) {
            btnRemoveSimulationRun = new JButton("Remove");
            btnRemoveSimulationRun.addActionListener(new ActionListener() {
		@Override
                public void actionPerformed(ActionEvent arg0) {
                    removeSelectedSimulationRuns();
                }
            });
        }
        return btnRemoveSimulationRun;
    }

    private JButton getAddTimeGeneratorButton() {
        if (btnAddTimeGenerator == null) {
            btnAddTimeGenerator = new JButton("Add");
            btnAddTimeGenerator.setPreferredSize(DEFAULT_EDIT_BUTTON_DIMENSION);
            btnAddTimeGenerator.addActionListener(new ActionListener() {
		@Override
                public void actionPerformed(ActionEvent e) {

                    TimeProperties timeProperties;
                    try {
                        timeProperties = TimeGeneratorDialog.showTimeGeneratorDialog(SimulationDialog.this, getAllKnownProcessActivities());
                    } catch (ParameterException e2) {
                        JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception, cannot launch time generator dialog.\nReason: " + e2.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (timeProperties != null) {
                        CaseTimeGenerator newTimeGenerator;
                        try {
                            newTimeGenerator = TimeGeneratorFactory.createCaseTimeGenerator(timeProperties);
                        } catch (Exception e1) {
                            JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception on creating case time generator.\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        try {
                            SimulationComponents.getInstance().getContainerTimeGenerators().addComponent(newTimeGenerator, true);
                        } catch (ParameterException e2) {
                            JOptionPane.showMessageDialog(SimulationDialog.this, "Parameter Exception on creating new time generator.\nReason: " + e2.getMessage(), "Parameter Exception", JOptionPane.ERROR_MESSAGE);
                            return;
                        } catch (Exception e2) {
                            JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception on adding case time generator to simulation components.\nReason: " + e2.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        updateComboTimeGenerator();
                        try {
                            comboTimeGenerator.setSelectedItem(timeProperties.getName());
                        } catch (PropertyException e1) {
                            // Should not happen, since the name value was successfully set in the time generator dialog.
                            e1.printStackTrace();
                        }
                    }

                }
            });
        }
        return btnAddTimeGenerator;
    }

    private JButton getEditTimeGeneratorButton() {
        if (btnEditTimeGenerator == null) {
            btnEditTimeGenerator = new JButton("Edit");
            btnEditTimeGenerator.setPreferredSize(DEFAULT_EDIT_BUTTON_DIMENSION);
            btnEditTimeGenerator.addActionListener(new ActionListener() {
		@Override
                public void actionPerformed(ActionEvent e) {
                    if (comboTimeGenerator.getSelectedItem() == null) {
                        JOptionPane.showMessageDialog(SimulationDialog.this, "No time generator chosen.", "Invalid parameter", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String chosenTimeGeneratorName = comboTimeGenerator.getSelectedItem().toString();

                    CaseTimeGenerator timeGenerator = null;
                    try {
                        timeGenerator = SimulationComponents.getInstance().getContainerTimeGenerators().getComponent(chosenTimeGeneratorName);
                    } catch (Exception e2) {
                        // Cannot happen, since "chosenContext" is not null.
                        JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract time generator: " + chosenTimeGeneratorName, "Internal Exception", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (timeGenerator == null) {
                        JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract time generator \"" + chosenTimeGeneratorName + "\" from simulation components", "Internal Exception", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String oldTimeGeneratorName = timeGenerator.getName();
                    CaseTimeGenerator editedTimeGenerator;
                    try {
                        editedTimeGenerator = TimeGeneratorFactory.createCaseTimeGenerator(TimeGeneratorDialog.showTimeGeneratorDialog(SimulationDialog.this, getAllKnownProcessActivities(), timeGenerator.getProperties()));
                    } catch (ParameterException e3) {
                        JOptionPane.showMessageDialog(SimulationDialog.this, e3.getMessage(), "Parameter Exception", JOptionPane.ERROR_MESSAGE);
                        return;
                    } catch (PropertyException e3) {
                        JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract time generator properties for editing.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (editedTimeGenerator != null) {

                        if (!editedTimeGenerator.getName().equals(oldTimeGeneratorName)) {
                            //Time generator name changed
                            //-> Remove old time generator from simulation components and add it again under the new name.
                            //   Since the reference stays the same, this should not change anything with other components referring to this time generator.
                            try {
                                SimulationComponents.getInstance().getContainerTimeGenerators().renameComponent(oldTimeGeneratorName, editedTimeGenerator.getName());
                            } catch (Exception e1) {
                                JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot change name of time generator.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }

                        //Find all simulations with reference to edited time generator
                        Set<Simulation> simulationsWithReferenceToEditedTimeGenerator = new HashSet<>();
                        try {
                            for (Simulation simulation : SimulationComponents.getInstance().getContainerSimulations().getComponents()) {
                                if (simulation.getCaseTimeGenerator() == timeGenerator) {
                                    simulationsWithReferenceToEditedTimeGenerator.add(simulation);
                                }
                            }
                        } catch(Exception ex){
                            ExceptionDialog.showException(SimulationDialog.this, "Internal Exception", new Exception("Cannot extract simulations from simulation components", ex), true);
                        }

                        Set<Simulation> simulationsWithNewTimeGeneratorReference = new HashSet<>();
                        for (Simulation simulation : simulationsWithReferenceToEditedTimeGenerator) {
                            try {
                                simulation.setCaseTimeGenerator(editedTimeGenerator);
                                simulationsWithNewTimeGeneratorReference.add(simulation);
                            } catch (Exception e1) {
                                JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot update the time generator reference for simulation \"" + simulation + "\"", "Internal Exception", JOptionPane.ERROR_MESSAGE);
                                //Revert changes
                                for (Simulation adjustedSimulation : simulationsWithNewTimeGeneratorReference) {
                                    try {
                                        adjustedSimulation.setCaseTimeGenerator(timeGenerator);
                                    } catch (Exception e2) {
                                        //Should not happen, since this is only the old reference the simulation had before
                                        JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot revert update of time generator reference for \"" + adjustedSimulation + "\"", "Internal Exception", JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }
                                }
                                return;
                            }
                        }

                        updateComboTimeGenerator();
                        comboTimeGenerator.setSelectedItem(editedTimeGenerator.getName());

                        updateVisibility();
                    }
                }
            });
        }
        return btnEditTimeGenerator;
    }

    @Override
    protected void setTitle() {
        if (editMode()) {
            setTitle("Edit Simulation");
        } else {
            setTitle("New Simulation");
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return PREFERRED_SIZE;
    }

    @Override
    protected Simulation getDialogObject() {
        return (Simulation) super.getDialogObject();
    }

    //------- OTHER GUI COMPONENTS ----------------------------------------------------------------------------------------------------------
    private JList getSimulationRunList() {
        if (listSimulationRuns == null) {
            listSimulationRuns = new JList();
            listSimulationRuns.setCellRenderer(new SimulationRunListCellRenderer());
            listSimulationRuns.setModel(listSimulationRunsModel);
            listSimulationRuns.addKeyListener(new KeyListener() {

                @Override
                public void keyTyped(KeyEvent e) {
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                        removeSelectedSimulationRuns();
                    }
                }

                @Override
                public void keyPressed(KeyEvent e) {
                }
            });
            updateSimulationRunList();
        }
        return listSimulationRuns;
    }

    private void removeSelectedSimulationRuns() {
        if (listSimulationRuns.getSelectedValues() != null) {
            for (Object selectedObject : listSimulationRuns.getSelectedValues()) {
                simulationRuns.remove(((SimulationRun) selectedObject).getName());
            }
            updateSimulationRunList();
        }
    }

    private void updateSimulationRunList() {
        listSimulationRunsModel.clear();

        for (SimulationRun simulationRun : simulationRuns.values()) {
            listSimulationRunsModel.addElement(simulationRun);
        }
        if (!listSimulationRunsModel.isEmpty()) {
            listSimulationRuns.setSelectedIndex(0);
        }
    }

    private JTextField getSimulationNameField() {
        if (txtName == null) {
            txtName = new JTextField();
            txtName.setText("NewSimulation");
            txtName.setPreferredSize(DEFAULT_COMBO_BOX_DIMENSION);
        }
        return txtName;
    }

    private JTextField getLogNameField() {
        if (txtLogName == null) {
            txtLogName = new JTextField();
            txtLogName.setText("ProcessLog");
            txtLogName.setPreferredSize(DEFAULT_COMBO_BOX_DIMENSION);
        }
        return txtLogName;
    }

    //------- FUNCTIONALTY ---------------------------------------------------------------------------
    @Override
    protected void prepareEditing() {
        txtName.setText(getSimulation().getName());
        String fileName = new File(getSimulation().getLogGenerator().getFileName()).getName();
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        txtLogName.setText(fileName);
//		txtLogPath.setText(simulation.getLogGenerator().getLogPath());
        comboLogFormat.setSelectedItem(LogFormatFactory.getType(getSimulation().getLogGenerator().getLogFormat()));
        if (getSimulation().getLogEntryGenerator() instanceof DetailedLogEntryGenerator) {
            comboEntryGenerator.setSelectedIndex(1);
            DetailedLogEntryGenerator entryGenerator = (DetailedLogEntryGenerator) getSimulation().getLogEntryGenerator();
            comboContext.setSelectedItem(entryGenerator.getContext().getName());
            comboDataContainer.setSelectedItem(entryGenerator.getCaseDataContainer().getName());
        }
        comboTimeGenerator.setSelectedItem(getSimulation().getCaseTimeGenerator().getName());
        try {
            for (SimulationRun simulationRun : getSimulation().getSimulationRuns()) {
                simulationRuns.put(simulationRun.getName(), simulationRun.clone());
            }
        } catch (ConfigurationException e) {
            JOptionPane.showMessageDialog(SimulationDialog.this, "Configuration exception during extraction of simulation runs.\n" + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
        }
        updateSimulationRunList();
        updateVisibility();
    }

    private void updateVisibility() {
        comboContext.setEnabled(comboEntryGenerator.getSelectedIndex() == 1);
        btnAddContext.setEnabled(comboEntryGenerator.getSelectedIndex() == 1);
        btnEditContext.setEnabled(comboEntryGenerator.getSelectedIndex() == 1);
        comboDataContainer.setEnabled(comboEntryGenerator.getSelectedIndex() == 1 && comboContext.getModel().getSelectedItem() != null);
        btnAddDataContainer.setEnabled(comboEntryGenerator.getSelectedIndex() == 1 && comboContext.getModel().getSelectedItem() != null);
        btnEditDataContainer.setEnabled(comboEntryGenerator.getSelectedIndex() == 1 && comboContext.getModel().getSelectedItem() != null);
//		btnAddSimulationRun.setEnabled(comboContext.getModel().getSelectedItem() != null);
//		btnEditSimulationRun.setEnabled(comboContext.getModel().getSelectedItem() != null);
    }

    @SuppressWarnings("unchecked")
    private Set<SimulationRun> getIncompatibleSimulationRuns() {
        String contextName = comboContext.getSelectedItem().toString();
        SynthesisContext context;
        try {
            context = SimulationComponents.getInstance().getContainerSynthesisContexts().getComponent(contextName);
        } catch (Exception e1) {
            JOptionPane.showMessageDialog(SimulationDialog.this, "Exception during simulation consistency check: Cannot extract context.\n" + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        Set<SimulationRun> incompatibleSimulationRuns = new HashSet<>();
        Set<String> contextActivities = context.getActivities();
        for (SimulationRun simulationRun : simulationRuns.values()) {
            Set<String> simulationRunActivities;
            try {
                simulationRunActivities = PNUtils.getLabelSetFromTransitions(simulationRun.getPetriNet().getTransitions(), false);
            } catch (ParameterException e1) {
                JOptionPane.showMessageDialog(SimulationDialog.this, "Exception during simulation consistency check: Cannot extract activities from net in simulation run\"" + simulationRun.getName() + "\"", "Internal exception", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (!contextActivities.containsAll(simulationRunActivities)) {
                incompatibleSimulationRuns.add(simulationRun);
            }
        }
        return incompatibleSimulationRuns;
    }

    /**
     * Returns all known process activities, i.e.<br>
     * the union of all transitions of all Petri nets within all simulation
     * runs.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    private Set<String> getAllKnownProcessActivities() {
        Set<String> allKnownProcessActivities = new HashSet<>();
        for (SimulationRun simulationRun : simulationRuns.values()) {
            try {
                allKnownProcessActivities.addAll(PNUtils.getLabelSetFromTransitions(simulationRun.getPetriNet().getTransitions(), false));
            } catch (ParameterException e1) {
                JOptionPane.showMessageDialog(SimulationDialog.this, "Internal exception on collecting known activities from simulation runs:\n" + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        return allKnownProcessActivities;
    }

    private SynthesisContext getSelectedContext() throws Exception {
        if (comboContext.getSelectedItem() == null) {
            return null;
        }
        return SimulationComponents.getInstance().getContainerSynthesisContexts().getComponent(comboContext.getSelectedItem().toString());
    }

    private LogFormatType getLogFormatType() {
        return LogFormatType.valueOf(comboLogFormat.getSelectedItem().toString());
    }

    public Simulation getSimulation() {
        return getDialogObject();
    }

    public static Simulation showDialog(Window owner) throws Exception {
        SimulationDialog dialog = new SimulationDialog(owner);
        dialog.setUpGUI();
        return dialog.getSimulation();
    }

    public static Simulation showDialog(Window owner, Simulation simulation) throws Exception {
        SimulationDialog dialog = new SimulationDialog(owner, simulation);
        dialog.setUpGUI();
        return dialog.getSimulation();
    }

    @Override
    protected Simulation newDialogObject(Object... parameters) {
        return new Simulation();
    }

    @Override
    protected boolean validateAndSetFieldValues() throws Exception {
        //Set simulation name	
        String simulationName = txtName.getText();
        if (simulationName == null || simulationName.isEmpty()) {
            throw new Exception("Simulation name empty");
        }

        Set<String> simulationNames;
        try{
            simulationNames = new HashSet<>(SimulationComponents.getInstance().getContainerSimulations().getComponentNames());
        } catch(Exception ex){
            ExceptionDialog.showException(SimulationDialog.this, "Internal Exception", new Exception("Cannot extract simulation names from simulation components", ex), true);
            return false;
        }
        if (simulationNames.contains(simulationName)) {
            throw new Exception("There is already a simulation with name \"" + simulationName + "\"");
        }
        getDialogObject().setName(simulationName);

        String fileName = txtLogName.getText();
        if (fileName == null || fileName.isEmpty()) {
            throw new Exception("Empty file name");
        }

        AbstractLogFormat logFormat = LogFormatFactory.getFormat(getLogFormatType());

        // Create log generator
        TraceLogGenerator logGenerator;
        try {
            logGenerator = new TraceLogGenerator(logFormat, SecSyProperties.getInstance().getWorkingDirectory(), fileName);
        } catch (IOException | PropertyException | PerspectiveException e1) {
            ExceptionDialog.showException(SimulationDialog.this, "Internal Exception", new Exception("Cannot create log generator", e1), true);
            return false;
        }
        try {
            getDialogObject().setLogGenerator(logGenerator);
        } catch (ConfigurationException e1) {
            throw new Exception("Configuration exception while setting log generator.", e1);
        } catch (Exception e1) {
            ExceptionDialog.showException(SimulationDialog.this, "Internal Exception", new Exception("Exception while setting log generator", e1), true);
            return false;
        }
        if (rdbtnOneEvent.isSelected()) {
            // Event handling: only one event
            if (comboEventType.getSelectedItem().equals("start")) {
                logGenerator.setEventHandling(EventHandling.START);
            } else {
                logGenerator.setEventHandling(EventHandling.END);
            }
        } else {
            // Event handling: both events
            logGenerator.setEventHandling(EventHandling.BOTH);
        }

        //Get entry generation type and create entry generator accordingly.
        EntryGenerationType generationType = (comboEntryGenerator.getSelectedIndex() == 0) ? EntryGenerationType.SIMPLE : EntryGenerationType.DETAILED;

        LogEntryGenerator entryGenerator = null;
        if (generationType.equals(EntryGenerationType.DETAILED)) {

            // Extract context
            if (comboContext.getSelectedItem() == null) {
                throw new Exception("No context chosen");
            }
            String contextName = comboContext.getSelectedItem().toString();
            SynthesisContext context;
            try {
                context = SimulationComponents.getInstance().getContainerSynthesisContexts().getComponent(contextName);
            } catch (Exception e1) {
                ExceptionDialog.showException(SimulationDialog.this, "Internal Exception", new Exception("Exception while extracting context", e1), true);
                return false;
            }
            if (context == null) {
                ExceptionDialog.showException(SimulationDialog.this, "Internal Exception", new Exception("Context extraction resulted in NULL value"), true);
                return false;
            }

            //check if there are incompatible simulation runs
            Set<SimulationRun> incompatibleSimulationRuns = getIncompatibleSimulationRuns();
            if (!incompatibleSimulationRuns.isEmpty()) {
                //There is at least one incompatible simulation run
                throw new Exception("Context is not compatible with at least one simulation run: Missing activities.\nAffected simulation runs: " + ArrayUtils.toString(incompatibleSimulationRuns.toArray()));

//				//Let the user choose if he wants to delete it.
//				int dialogResult = JOptionPane.showOptionDialog(SimulationDialog.this, "Proceed with deletion of incompatible simulation run?","Incompatible Simulation Run",
//		                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
//				if(dialogResult == JOptionPane.YES_OPTION){
//					//Remove incompatible simulation run
//					simulationRuns.remove(incompatibleSimulationRun);
//				}
            }

            // Extract Case Data Container
            if (comboDataContainer.getSelectedItem() == null) {
                throw new Exception("No case data container chosen.");
            }
            String containerName = comboDataContainer.getSelectedItem().toString();
            CaseDataContainer container;
            try {
                container = SimulationComponents.getInstance().getContainerCaseDataContainers().getComponent(containerName);
            } catch (Exception e1) {
                ExceptionDialog.showException(SimulationDialog.this, "Internal Exception", new Exception("Exception while extracting case data container"), true);
                return false;
            }
            if (container == null) {
                ExceptionDialog.showException(SimulationDialog.this, "Internal Exception", new Exception("Case data container extraction resulted in NULL value"), true);
                return false;
            }

            // Create detailed case data container using the previously extracted context and data container
            try {
                entryGenerator = new DetailedLogEntryGenerator(context, container);
            } catch (ConfigurationException e1) {
                throw new Exception("Configuration exception while creating log entry generator.");
            } catch (ParameterException e1) {
                ExceptionDialog.showException(SimulationDialog.this, "Internal Exception", new Exception("Exception while creating log entry generator."), true);
                return false;
            }
        } else {
            entryGenerator = new LogEntryGenerator();
        }

        try {
            getDialogObject().setLogEntryGenerator(entryGenerator);
        } catch (ConfigurationException e1) {
            throw new Exception("Configuration exception while setting log entry generator.");
        } catch (ParameterException e1) {
            ExceptionDialog.showException(SimulationDialog.this, "Internal Exception", new Exception("Exception while setting log entry generator."), true);
            return false;
        }

        //Set time generator
        if (comboTimeGenerator.getSelectedItem() == null) {
            throw new ParameterException("No case time generator chosen");
        }
        String timeGeneratorName = comboTimeGenerator.getSelectedItem().toString();
        CaseTimeGenerator timeGenerator;
        try {
            timeGenerator = SimulationComponents.getInstance().getContainerTimeGenerators().getComponent(timeGeneratorName);
        } catch (Exception e1) {
            ExceptionDialog.showException(SimulationDialog.this, "Internal Exception", new Exception("Exception while extracting time generator."), true);
            return false;
        }
        if (timeGeneratorName == null) {
            ExceptionDialog.showException(SimulationDialog.this, "Internal Exception", new Exception("Time generator extraction resulted in NULL value."), true);
            return false;
        }
        try {
            getDialogObject().setCaseTimeGenerator(timeGenerator);
        } catch (ConfigurationException e1) {
            throw new Exception("Configuration exception while setting  time generator");
        } catch (Exception e1) {
            ExceptionDialog.showException(SimulationDialog.this, "Internal Exception", new Exception("Exception while setting time generator."), true);
            return false;
        }

        // Set simulation runs
        try {
            getDialogObject().setSimulationRuns(simulationRuns.values());
        } catch (Exception e1) {
            ExceptionDialog.showException(SimulationDialog.this, "Internal Exception", new Exception("Exception while setting simulation runs."), true);
            return false;
        }

        // Check if simulation is valid
        try {
            getDialogObject().checkValidity();
        } catch (ConfigurationException e1) {
            throw new Exception("Actual configuration causes invalid simulation state");
        }
        return true;
    }

    private class SimulationRunListCellRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 3946538578411097430L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            try {
                setText(StringUtils.convertToHTML(value.toString()));
            } catch (ParameterException e) {
                setText(value.toString());
            }
            return result;
        }

    }

    private JButton getButtonShowActivities() {
        if (btnShowActivities == null) {
            btnShowActivities = new JButton("Show activities");
            btnShowActivities.addActionListener(new ActionListener() {
		@Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (listSimulationRuns.getSelectedValue() == null) {
                            return;
                        }
                        SimulationRun run = (SimulationRun) listSimulationRuns.getSelectedValue();
                        StringBuilder builder = new StringBuilder();
                        try {
                            List<String> activityList = new ArrayList<>(PNUtils.getLabelSetFromTransitions(run.getPetriNet().getTransitions(), false));
                            Collections.sort(activityList);
                            for (String activity : activityList) {
                                builder.append(activity);
                                builder.append('\n');
                            }
                        } catch (ParameterException e1) {
                            JOptionPane.showMessageDialog(SimulationDialog.this, "Cannot extract transition labels.\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        StringDialog.showDialog(SimulationDialog.this, builder.toString());
                    } catch (Exception ex) {
                        ExceptionDialog.showException("Internal Exception", new Exception("Cannot launch StringDialog", ex));
                    }
                }
            });
        }
        return btnShowActivities;
    }

    @Override
    protected Border getBorder() {
        return GUIProperties.DEFAULT_DIALOG_BORDER;
    }

    public static void main(String[] args) throws ParameterException {
        try {
            new SimulationDialog(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
