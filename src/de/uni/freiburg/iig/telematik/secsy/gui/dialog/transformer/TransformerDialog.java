package de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import de.invation.code.toval.graphic.component.BoxLayoutPanel;
import de.invation.code.toval.graphic.dialog.AbstractEditCreateDialog;
import de.invation.code.toval.validate.ExceptionDialog;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.gui.GUIProperties;
import de.uni.freiburg.iig.telematik.secsy.gui.SimulationComponents;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TransformerComponents;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.AbstractMultipleTraceTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.AbstractTraceTransformer;
import java.util.List;

public class TransformerDialog extends AbstractEditCreateDialog<AbstractTraceTransformer> {
    
    private static final long serialVersionUID = -6315050477292478094L;
    
    public static final int LABEL_WIDTH = 130;
    public static final int FIELD_WIDTH = 220;
    public static final int HINT_SIZE = 4;
    public static final Double DEFAULT_ACTIVATION_PROBABILITY = 1.0;
    public static final Integer DEFAULT_MAX_APPLIANCES = 1;
    private static final String hintFormat = "<html><font size=%s>%s</html>";
    private static final int MIN_AREA_HEIGHT = 120;
    
    private JComboBox comboTransformerType;
    private JTextField txtTransformerName;
    private JTextField txtTransformerActivation;
    private JTextField txtMaxAppliances;
    private JSeparator separator;
    private JTextPane hintArea;
    
    @SuppressWarnings("rawtypes")
    private AbstractTransformerPanel transformerPanel;
    private JPanel panelGlobalParameters;
    private JPanel panelCustomParameters;
    private JPanel appliancesPanel;
    private JPanel activationPanel;
    
    private Set<String> activities;
    
    public TransformerDialog(Window owner, Set<String> activities) throws Exception {
        super(owner);
        setActivities(activities);
    }
    
    public TransformerDialog(Window owner, Set<String> activities, AbstractTraceTransformer transformer) throws Exception {
        super(owner, transformer);
        setActivities(activities);
    }
    
    private void setActivities(Set<String> activities){
       Validate.notNull(activities);
       Validate.notEmpty(activities);
       Validate.noNullElements(activities);
       this.activities = activities;
    }
    
    @Override
    protected void addComponents() {
        setResizable(true);
        mainPanel().setLayout(new BorderLayout());
        
        mainPanel().add(getPanelGlobalParameters(), BorderLayout.NORTH);
        panelCustomParameters = new JPanel(new BorderLayout());
        mainPanel().add(panelCustomParameters, BorderLayout.CENTER);
        
        updatePanelParameter();
    }
    
    private JPanel getPanelGlobalParameters() {
        if (panelGlobalParameters == null) {
            
            panelGlobalParameters = new BoxLayoutPanel(BoxLayout.PAGE_AXIS);
            
            panelGlobalParameters.add(Box.createVerticalStrut(20));
            
            JPanel typePanel = new BoxLayoutPanel();
            JLabel lblTransformerType = new JLabel("Transformer Type:");
            lblTransformerType.setPreferredSize(new Dimension(LABEL_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
            lblTransformerType.setMaximumSize(new Dimension(LABEL_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
            lblTransformerType.setHorizontalAlignment(JLabel.TRAILING);
            typePanel.add(lblTransformerType);
            typePanel.add(getComboTransformerType());
            typePanel.add(Box.createHorizontalGlue());
            panelGlobalParameters.add(typePanel);
            
            panelGlobalParameters.add(Box.createVerticalStrut(10));
            
            hintArea = new JTextPane();
            hintArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            hintArea.setContentType("text/html");
            hintArea.setEditable(false);
            hintArea.setPreferredSize(new Dimension(LABEL_WIDTH + FIELD_WIDTH, MIN_AREA_HEIGHT));
            hintArea.setMaximumSize(new Dimension(LABEL_WIDTH + FIELD_WIDTH, MIN_AREA_HEIGHT));
            hintArea.setMinimumSize(new Dimension(LABEL_WIDTH + FIELD_WIDTH, MIN_AREA_HEIGHT));
            JScrollPane scrollPane = new JScrollPane(hintArea);            
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setBorder(BorderFactory.createLineBorder(Color.black, 1));
            hintArea.setEditable(false);
            panelGlobalParameters.add(scrollPane);
            
            panelGlobalParameters.add(Box.createVerticalStrut(10));
            
            JPanel namePanel = new BoxLayoutPanel();
            JLabel lblTransformerName = new JLabel("Transformer Name:");
            lblTransformerName.setHorizontalAlignment(JLabel.TRAILING);
            lblTransformerName.setPreferredSize(new Dimension(LABEL_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
            lblTransformerName.setMaximumSize(new Dimension(LABEL_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
            namePanel.add(lblTransformerName);
            txtTransformerName = new JTextField();
            txtTransformerName.setText("New Transformer");
            txtTransformerName.setPreferredSize(new Dimension(FIELD_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
            txtTransformerName.setMaximumSize(new Dimension(FIELD_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
            namePanel.add(txtTransformerName);
            namePanel.add(Box.createHorizontalGlue());
            panelGlobalParameters.add(namePanel);
            
            panelGlobalParameters.add(Box.createVerticalStrut(5));
            
            activationPanel = new BoxLayoutPanel();
            JLabel lblTransformerActivation = new JLabel("Activation:");
            lblTransformerActivation.setHorizontalAlignment(JLabel.TRAILING);
            lblTransformerActivation.setPreferredSize(new Dimension(LABEL_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
            lblTransformerActivation.setMaximumSize(new Dimension(LABEL_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
            activationPanel.add(lblTransformerActivation);
            txtTransformerActivation = new JTextField();
            activationPanel.add(txtTransformerActivation);
            txtTransformerActivation.setText("100");
            txtTransformerActivation.setPreferredSize(new Dimension(FIELD_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
            txtTransformerActivation.setMaximumSize(new Dimension(FIELD_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
            activationPanel.add(txtTransformerActivation);
            JLabel lblPercentage = new JLabel("%");
            activationPanel.add(lblPercentage);
            activationPanel.add(Box.createHorizontalGlue());
            panelGlobalParameters.add(activationPanel);
            
            panelGlobalParameters.add(Box.createVerticalStrut(10));
            
            appliancesPanel = new BoxLayoutPanel();
            JLabel lblMaxAppliances = new JLabel("Appliances:");
            lblMaxAppliances.setHorizontalAlignment(JLabel.TRAILING);
            lblMaxAppliances.setPreferredSize(new Dimension(LABEL_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
            lblMaxAppliances.setMaximumSize(new Dimension(LABEL_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
            appliancesPanel.add(lblMaxAppliances);
            txtMaxAppliances = new JTextField();
            txtMaxAppliances.setText(DEFAULT_MAX_APPLIANCES.toString());
            txtMaxAppliances.setPreferredSize(new Dimension(FIELD_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
            txtMaxAppliances.setMaximumSize(new Dimension(FIELD_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
            appliancesPanel.add(txtMaxAppliances);
            JLabel lblMax = new JLabel("(max)");
            appliancesPanel.add(lblMax);
            appliancesPanel.add(Box.createHorizontalGlue());
            panelGlobalParameters.add(appliancesPanel);
            
            panelGlobalParameters.add(Box.createVerticalStrut(5));
            
            separator = new JSeparator(SwingConstants.HORIZONTAL);
            panelGlobalParameters.add(separator);
            panelGlobalParameters.add(Box.createVerticalStrut(10));
        }
        return panelGlobalParameters;
    }
    
    private JComboBox getComboTransformerType() {
        if (comboTransformerType == null) {
            comboTransformerType = new JComboBox();
            comboTransformerType.setPreferredSize(new Dimension(FIELD_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
            comboTransformerType.setMaximumSize(new Dimension(FIELD_WIDTH, GUIProperties.DEFAULT_LABEL_HEIGHT));
            DefaultComboBoxModel comboTransformerTypeModel = new DefaultComboBoxModel();
            List<TransformerType> transformerTypes = null;
            try{
                transformerTypes = TransformerComponents.getInstance().getAllTransformerTypes();
            } catch(Exception e){
                ExceptionDialog.showException(TransformerDialog.this, "Internal Exception", new Exception("Cannot extract transformer types", e), true);
            }
            for (TransformerType transformerType : transformerTypes) {
                comboTransformerTypeModel.addElement(transformerType);
            }
            comboTransformerType.setModel(comboTransformerTypeModel);
            comboTransformerType.addItemListener(new ItemListener() {
                
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        updatePanelParameter();
                        pack();
                        repaint();
                    }
                }
            });
        }
        return comboTransformerType;
    }
    
    @SuppressWarnings("rawtypes")
    private void updatePanelParameter() {
        
        if (!editMode()) {
            // Try to create a new instance of the active transformer type
            AbstractTraceTransformer newTransformer = null;
            try {
                if (AbstractMultipleTraceTransformer.class.isAssignableFrom(getSelectedTransformerType().getTransformerClass())) {
                    Constructor transformerConstructor = getSelectedTransformerType().getTransformerClass().getConstructor(Double.class, Integer.class);
                    newTransformer = (AbstractTraceTransformer) transformerConstructor.newInstance(DEFAULT_ACTIVATION_PROBABILITY, DEFAULT_MAX_APPLIANCES);
                } else {
                    Constructor transformerConstructor = getSelectedTransformerType().getTransformerClass().getConstructor(Double.class);
                    newTransformer = (AbstractTraceTransformer) transformerConstructor.newInstance(DEFAULT_ACTIVATION_PROBABILITY);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(TransformerDialog.this, "<html>Cannot create new instance of chosen transformer type.<br>Reason: " + e.getMessage() + "</html>", "Parameter Exception", JOptionPane.ERROR_MESSAGE);
            }
            setDialogObject(newTransformer);
        }
        
        String hint = getDialogObject().getHint();
        hint = hint.replace("<html>", "");
        hint = hint.replace("</html>", "");
        hint = String.format(hintFormat, HINT_SIZE, hint);
        hintArea.setText(hint);

        // Check transformer type (multiple trace transformer or simple trace transformer)
        boolean multipleTraceTransformer = isMultipleTraceTransformer();
        appliancesPanel.setVisible(multipleTraceTransformer);
        activationPanel.setVisible(!getDialogObject().isMandatory());
//		txtMaxAppliances.setEnabled(multipleTraceTransformer);
//		lblMaxAppliances.setEnabled(multipleTraceTransformer);
//		lblMax.setEnabled(multipleTraceTransformer);

        // Create a new properties panel for the active transformer type.
        try {
            Constructor constructor = getSelectedTransformerType().getTransformerPanelClass().getConstructor(Set.class);
            transformerPanel = (AbstractTransformerPanel) constructor.newInstance(activities);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(TransformerDialog.this, "<html>Cannot create new instance of property panel for chosen transformer type.<br>Reason: " + e.getMessage() + "</html>", "Parameter Exception", JOptionPane.ERROR_MESSAGE);
        }
        separator.setVisible(!transformerPanel.isEmpty());
        panelCustomParameters.removeAll();
        if (!transformerPanel.isEmpty()) {
            panelCustomParameters.add(transformerPanel, BorderLayout.CENTER);
        }
    }
    
    private TransformerType getSelectedTransformerType() {
        return (TransformerType) comboTransformerType.getSelectedItem();
    }
    
    private boolean isMultipleTraceTransformer() {
        return AbstractMultipleTraceTransformer.class.isAssignableFrom(getSelectedTransformerType().getTransformerClass());
    }
    
    @Override
    protected boolean validateAndSetFieldValues() {

        //Validate transformer name
        String transformerName = txtTransformerName.getText();
        if (transformerName == null || transformerName.isEmpty())
            throw new ParameterException("Invalid transformer name");
        
        transformerName = transformerName.replace(' ', '_');
        
        Set<String> transformerNames = null;
        try{
            transformerNames = new HashSet<>(TransformerComponents.getInstance().getAllTransformerNames());
        } catch(Exception e){
            ExceptionDialog.showException(TransformerDialog.this, "Internal Exception", new Exception("Cannot extract transformer names", e), true);
            return false;
        }
        if (editMode()) {
            transformerNames.remove(getDialogObject().getName());
        }
        if (transformerNames.contains(transformerName))
            throw new ParameterException("There is already a transformer with the same name");
        
        getDialogObject().setName(transformerName);

		//Validate activation probability
        //Set deviation for cases per day
        Double activationProbability = null;
        try {
            activationProbability = Validate.percentage(txtTransformerActivation.getText());
        } catch (Exception e1){
            throw new ParameterException("Invalid activation probability");
        }
        activationProbability = activationProbability / 100.0;
        
        getDialogObject().setActivationProbability(activationProbability);
        
        if (isMultipleTraceTransformer()) {
            // Check max appliances
            Integer maxAppliances = null;
            try {
                maxAppliances = Validate.positiveInteger(txtMaxAppliances.getText());
            } catch (Exception e1) {
                throw new ParameterException("Invalid value for max. appliances.");
            }
            ((AbstractMultipleTraceTransformer) getDialogObject()).setMaxAppliances(maxAppliances);
        }

        // Validate and set transformer-specific fields
        try {
            transformerPanel.validateFieldValues();
            getDialogObject().setProperties(transformerPanel.getParameters());
        } catch (Exception ex) {
            throw new ParameterException("Invalid field value: " + ex.getMessage(), ex); 
        }
        return true;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void prepareEditing() {
        // Find transformer type to choose
        Object selectedItem = null;
        Set<TransformerType> customTransformerTypes = null;
        try{
            customTransformerTypes = TransformerComponents.getInstance().getContainerCustomTraceTransformers().getCustomTransformerTypes();
        } catch(Exception e){
            ExceptionDialog.showException(TransformerDialog.this, "Internal Exception", new Exception("Cannot extract custom transformer types", e), true);
        }
        for (TransformerType customTransformerType : customTransformerTypes) {
            if (customTransformerType.getTransformerClass().isAssignableFrom(getDialogObject().getClass())) {
                selectedItem = customTransformerType;
            }
        }
        if (selectedItem == null) {
            Set<TransformerType> transformerTypes = null;
            try{
                transformerTypes = TransformerComponents.getInstance().getContainerRegularTraceTransformers().getTransformerTypes();
            } catch(Exception e){
                ExceptionDialog.showException(TransformerDialog.this, "Internal Exception", new Exception("Cannot extract transformer types", e), true);
            }
            for (TransformerType transformerType : transformerTypes) {
                if (transformerType.getTransformerClass().isAssignableFrom(getDialogObject().getClass())) {
                    selectedItem = transformerType;
                }
            }
        }
        getComboTransformerType().setSelectedItem(selectedItem);
        getComboTransformerType().setEnabled(false);
        txtTransformerName.setText(getDialogObject().getName());
        txtTransformerActivation.setText(new Double((getDialogObject().getActivationProbability() * 100.0)).toString());
        
        if (isMultipleTraceTransformer()) {
            txtMaxAppliances.setText(((AbstractMultipleTraceTransformer) getDialogObject()).getMaxAppliances().toString());
        }

        // Tell transformer panel to initialize field values
        try {
            transformerPanel.initializeFields(getDialogObject());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(TransformerDialog.this, "Cannot initialize transformer panel\nReason: " + e.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    protected void setTitle() {
        if (editMode()) {
            setTitle("Edit Transformer");
        } else {
            setTitle("New Transformer");
        }
    }
    
    public static AbstractTraceTransformer showDialog(Window owner, Set<String> activities) throws Exception {
        TransformerDialog dialog = new TransformerDialog(owner, activities);
        dialog.setUpGUI();
        return dialog.getDialogObject();
    }
    
    public static AbstractTraceTransformer showDialog(Window owner, Set<String> activities, AbstractTraceTransformer traceTransformer) throws Exception {
        TransformerDialog dialog = new TransformerDialog(owner, activities, traceTransformer);
        dialog.setUpGUI();
        return dialog.getDialogObject();
    }
    

//	public static void main(String[] args) throws Exception {
//		Set<String> activities = new HashSet<String>(Arrays.asList("act 1", "act 2", "act 3", "act 4"));
//		
//		UnauthorizedExecutionTransformer unTransformer = new UnauthorizedExecutionTransformer(0.5, 10);
//		unTransformer.setName("un Transformer");
//		
//		DayDelayTransformer dayTransformer = new DayDelayTransformer(0.2, 12, 2, 3);
//		dayTransformer.setName("day Transformer");
//		
//		Set<String> skipActivities = new HashSet<String>(Arrays.asList("act 1", "act 2"));
//		SkipActivitiesTransformer skipTransformer = new SkipActivitiesTransformer(0.4, 3, skipActivities);
//		skipTransformer.setName("skip Transformer");
//		
//		IncompleteLoggingTransformer inTransformer = new IncompleteLoggingTransformer(0.4, 3, skipActivities);
//		inTransformer.setName("in Transformer");
//		
//		ObfuscationTransformer obTransformer = new ObfuscationTransformer(0.6, 5, EntryField.ACTIVITY);
//		obTransformer.setName("ob Transformer");
//		
//		SoDPropertyTransformer sodTransformer = new SoDPropertyTransformer(0.1, skipActivities);
//		sodTransformer.setName("sod");
//		
//		BoDPropertyTransformer bodTransformer = new BoDPropertyTransformer(0.1, skipActivities);
//		bodTransformer.setName("bod");
//		
//		AbstractTraceTransformer transformer = TransformerDialog.showDialog(null, activities, bodTransformer);
//	}

    @Override
    protected AbstractTraceTransformer newDialogObject(Object... parameters) {
        throw new UnsupportedOperationException("Not supported by transformer dialogs.");
    }
}
