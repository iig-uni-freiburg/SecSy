package gui.dialog;

import gui.SimulationComponents;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.invation.code.toval.misc.FormatUtils;
import de.invation.code.toval.misc.valuegeneration.StochasticValueGenerator;
import de.invation.code.toval.misc.valuegeneration.ValueGenerator;
import de.invation.code.toval.validate.InconsistencyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;

import logic.generator.AttributeValueGenerator;
import logic.generator.CaseDataContainer;

public class DataContainerDialog extends JDialog {

	private static final long serialVersionUID = -6256086169815107002L;
	
	private static final String valueFormat = "%s: %s%%";
	
	private final JPanel contentPanel = new JPanel();
	private JList listAttributes = null;
	private DefaultListModel listAttributesModel = new DefaultListModel();
	private JList listValues = null;
	private DefaultListModel listValueModel = new DefaultListModel();
	private JButton btnAddDataUsage = null;
	
	private CaseDataContainer dataContainer = null;
	private AttributeValueGenerator attValueGenerator = new AttributeValueGenerator();
	
	private boolean editMode = false;
	private JTextField txtName;
	private JTextField txtDefaultValue;
	private JComboBox comboDefaultValueType;
	private JComboBox comboValueType;
	
	private boolean dontComplainAboutTypeChange = false;
	
	private Object lastSelectedType = null;
	
	private JButton btnOK = null;
	/**
	 * @wbp.parser.constructor
	 */
	public DataContainerDialog(Window owner, Set<String> attributes, CaseDataContainer dataContainer) throws ParameterException {
		super(owner);
		Validate.notNull(dataContainer);
		this.dataContainer = dataContainer;
		this.attValueGenerator = dataContainer.getAttributeValueGenerator().clone();
		editMode = true;
		setUpGUI(owner, attributes);
	}

	public DataContainerDialog(Window owner, Set<String> attributes) throws ParameterException {
		super(owner);
		try {
			dataContainer = new CaseDataContainer(attValueGenerator);
		} catch (ParameterException e) {
			// Cannot happen, since attribute value generator parameter is not null
			e.printStackTrace();
		}
		setUpGUI(owner, attributes);
	}
	
	private void setUpGUI(Window owner, Set<String> attributes) throws ParameterException{
		
		Validate.notNull(attributes);
		for(String attribute: attributes)
			listAttributesModel.addElement(attribute);
		
		setResizable(false);
		setBounds(100, 100, 387, 486);
		setModal(true);
		setLocationRelativeTo(owner);
		
		addWindowListener(new WindowListener() {
			
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
				if(!editMode){
					dataContainer = null;
				}
			}
			
			@Override
			public void windowClosed(WindowEvent e) {}
			
			@Override
			public void windowActivated(WindowEvent e) {}
		});
		
		if(!editMode){
			setTitle("New Case Data Container");
		} else {
			setTitle("Edit Case Data Container");
		}
		
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setBounds(20, 159, 150, 240);
			contentPanel.add(scrollPane);
			{
				scrollPane.setViewportView(getAttributeList());
			}
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setBounds(185, 222, 181, 142);
			contentPanel.add(scrollPane);
			{
				scrollPane.setViewportView(getValueList());
				updateValueList();
			}
		}
		{
			JLabel lblActivities = new JLabel("Attributes:");
			lblActivities.setBounds(20, 139, 86, 16);
			contentPanel.add(lblActivities);
		}
		{
			btnAddDataUsage = new JButton("Add");
			btnAddDataUsage.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(listAttributes.getSelectedValue() == null){
						JOptionPane.showMessageDialog(DataContainerDialog.this, "Please choose an attribute first.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
					String attribute = listAttributes.getSelectedValue().toString();
					StochasticValueGenerator valueGenerator = null;
					try {
						valueGenerator = (StochasticValueGenerator) attValueGenerator.getValueGenerator(attribute);
					} catch (ParameterException e2) {
						JOptionPane.showMessageDialog(DataContainerDialog.this, "Cannot extract value generator for attribute \""+attribute+"\"", "Internal Exception", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					if(valueGenerator == null){
						try {
							valueGenerator = ValueProbabilityDialog.showDialog(DataContainerDialog.this, getValueType());
							attValueGenerator.setValueGeneration(attribute, valueGenerator);
						} catch (ParameterException e1) {
							JOptionPane.showMessageDialog(DataContainerDialog.this, "Cannot launch value probability dialog.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						}
					} else {
						try {
							ValueProbabilityDialog.showDialog(DataContainerDialog.this, valueGenerator, getValueType());
						} catch (ParameterException e1) {
							JOptionPane.showMessageDialog(DataContainerDialog.this, "Cannot launch value probability dialog.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
					updateValueList();
					
				}
			});
			btnAddDataUsage.setBounds(185, 369, 80, 29);
			contentPanel.add(btnAddDataUsage);
		}
		{
			JLabel lblDataUsage = new JLabel("Values and probabilities:");
			lblDataUsage.setBounds(185, 200, 185, 16);
			contentPanel.add(lblDataUsage);
		}
		{
			JButton btnRemove = new JButton("Remove");
			btnRemove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeSelectedValues();
				}
			});
			btnRemove.setBounds(267, 369, 80, 29);
			contentPanel.add(btnRemove);
		}
		{
			JButton btnDone = new JButton("Cancel");
			btnDone.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					DataContainerDialog.this.dispose();
				}
			});
			btnDone.setBounds(286, 427, 80, 29);
			contentPanel.add(btnDone);
		}
		
		JLabel lblName = new JLabel("Name:");
		lblName.setHorizontalAlignment(SwingConstants.TRAILING);
		lblName.setBounds(20, 19, 86, 16);
		contentPanel.add(lblName);
		
		txtName = new JTextField();
		txtName.setText("NewCaseDataContainer");
		txtName.setBounds(117, 13, 249, 28);
		contentPanel.add(txtName);
		txtName.setColumns(10);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(20, 52, 406, 12);
		contentPanel.add(separator);
		
		txtDefaultValue = new JTextField();
		txtDefaultValue.setText("null");
		txtDefaultValue.setColumns(10);
		txtDefaultValue.setBounds(115, 76, 86, 28);
		contentPanel.add(txtDefaultValue);
		
		JLabel lblDefaultValue = new JLabel("Default value:");
		lblDefaultValue.setBounds(18, 82, 98, 16);
		contentPanel.add(lblDefaultValue);
		
		JLabel lblType = new JLabel("Type:");
		lblType.setBounds(217, 82, 41, 16);
		contentPanel.add(lblType);
		
		comboDefaultValueType = new JComboBox();
		comboDefaultValueType.setModel(new DefaultComboBoxModel(new String[] {"String", "Integer", "Double"}));
		comboDefaultValueType.setBounds(259, 76, 107, 28);
		contentPanel.add(comboDefaultValueType);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(19, 116, 406, 12);
		contentPanel.add(separator_1);
		
		JLabel lblValueType = new JLabel("Value type:");
		lblValueType.setBounds(185, 165, 98, 16);
		contentPanel.add(lblValueType);
		
		comboValueType = new JComboBox();
		comboValueType.setModel(new DefaultComboBoxModel(new String[] {"String", "Integer", "Double"}));
		comboValueType.setBounds(259, 159, 107, 28);
		comboValueType.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.DESELECTED){
					lastSelectedType = e.getItem();
				} else if(e.getStateChange() == ItemEvent.SELECTED){
				
				
				if(dontComplainAboutTypeChange){
					return;
				}
				
				if(!listValueModel.isEmpty()){
					
					int dialogResult = JOptionPane.showConfirmDialog (null, "Changing the value type removes all existing value probabilities for this activity.\nDo you want to proceed?","Warning", JOptionPane.YES_NO_OPTION);
					if(dialogResult == JOptionPane.YES_OPTION){
						if(listAttributes.getSelectedValue() == null){
							JOptionPane.showMessageDialog(DataContainerDialog.this, "Please choose an attribute first.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
							return;
						}
						try {
							attValueGenerator.setValueGeneration(listAttributes.getSelectedValue().toString(), getNewValueGenerator());
						} catch (ParameterException e1) {
							JOptionPane.showMessageDialog(DataContainerDialog.this, "Cannot reset value generation.", "Internal Exception", JOptionPane.ERROR_MESSAGE);
							return;
						}
					} else {
						dontComplainAboutTypeChange = true;
						comboValueType.setSelectedItem(lastSelectedType);
						dontComplainAboutTypeChange = false;
					}
				}
				updateValueList();
				}
			}
		});
		contentPanel.add(comboValueType);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setBounds(0, 411, 451, 10);
		contentPanel.add(separator_2);

		btnOK = new JButton("OK");
		btnOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Map<String, ValueGenerator<?>> valueGeneratorMap = dataContainer.getAttributeValueGenerator().getValueGenerators();
				for(String attributeName: valueGeneratorMap.keySet()){
					ValueGenerator<?> valueGenerator = valueGeneratorMap.get(attributeName);
					if(!valueGenerator.isEmpty()){
						if(!valueGenerator.isValid()){
							JOptionPane.showMessageDialog(DataContainerDialog.this, "Value generator for attribute \""+attributeName+"\" in invalid state.\nProbabilities must sum up to 1", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
							return;
						}
					} else {
						//Don't care about empty value generators
					}
				}
				
				String containerName = txtName.getText();
				if(containerName == null || containerName.isEmpty()){
					JOptionPane.showMessageDialog(DataContainerDialog.this, "Affected field: Container name.\nReason: Null or empty value.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				Set<String> containerNames = new HashSet<String>(SimulationComponents.getInstance().getCaseDataContainerNames());
				containerNames.remove(dataContainer.getName());
				if(containerNames.contains(containerName)){
					JOptionPane.showMessageDialog(DataContainerDialog.this, "There is already a data container with name \""+containerName+"\"", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
					return;
				}
				dataContainer.setName(containerName);
				
				String defaultValueString = txtDefaultValue.getText();
				Object defaultValue = null;
				Class defaultValueClass = null;
				if(defaultValueString == null || defaultValueString.isEmpty()){
					JOptionPane.showMessageDialog(DataContainerDialog.this, "Affected field: Default value.\nReason: Empty value.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(defaultValueString.equals("null")){
					defaultValue = null;
					defaultValueClass = Object.class;
				} else {
					try{
					if(comboDefaultValueType.getSelectedItem().toString().equals("String")){
						defaultValue = defaultValueString;
						defaultValueClass = String.class;
					} else if(comboDefaultValueType.getSelectedItem().toString().equals("Integer")){
						defaultValue = Integer.parseInt(defaultValueString);
						defaultValueClass = Integer.class;
					} else if(comboDefaultValueType.getSelectedItem().toString().equals("Double")){
						defaultValue = Double.valueOf(defaultValueString);
						defaultValueClass = Double.class;
					}
					}catch(Exception e1){
						JOptionPane.showMessageDialog(DataContainerDialog.this, "Affected field: Default value or default value type.\nReason: "+e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				attValueGenerator.setDefaultValue(defaultValue);
				
				try {
					dataContainer.setAttributeValueGenerator(attValueGenerator);
				} catch (ParameterException e1) {
					JOptionPane.showMessageDialog(DataContainerDialog.this, "Cannot set Attribute Value Generator.\nReason: " + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				dispose();
			}
		});
		btnOK.setBounds(198, 427, 80, 29);
		contentPanel.add(btnOK);
		this.getRootPane().setDefaultButton(btnOK);
		
		if(editMode){
			initializeFields();
		}
		
		setVisible(true);
	}
	
	private void initializeFields(){
		txtName.setText(dataContainer.getName());
		Object defaultValue = attValueGenerator.getDefaultValue();
		if(defaultValue == null){
			txtDefaultValue.setText("null");
			comboDefaultValueType.setSelectedItem("Object");
		} else {
			txtDefaultValue.setText(attValueGenerator.getDefaultValue().toString());
			comboDefaultValueType.setSelectedItem(attValueGenerator.getDefaultValue().getClass().getSimpleName());
		}
	}
	
	private String getAttribute(){
		return listAttributes.getSelectedValue().toString();
	}
	
	private Object getValue(String valueString){
		Class valueType = getValueType();
		if(valueType.equals(String.class)){
			return String.valueOf(valueString);
		}
		if(valueType.equals(Integer.class)){
			return Integer.valueOf(valueString);
		}
		if(valueType.equals(Double.class)){
			return Double.valueOf(valueString);
		}
		return null;
	}
	
	private Class getValueType(){
		String typeString = comboValueType.getSelectedItem().toString();
		if(typeString.equals("String")){
			return String.class;
		}
		if(typeString.equals("Integer")){
			return Integer.class;
		}
		if(typeString.equals("Double")){
			return Double.class;
		}
		return null;
	}
	
	private StochasticValueGenerator<?> getNewValueGenerator() throws ParameterException{
		String typeString = comboValueType.getSelectedItem().toString();
		if(typeString.equals("String")){
			return new StochasticValueGenerator<String>();
		}
		if(typeString.equals("Integer")){
			return new StochasticValueGenerator<Integer>();
		}
		if(typeString.equals("Double")){
			return new StochasticValueGenerator<Double>();
		}
		return null;
	}
	
	private JList getAttributeList(){
		if(listAttributes == null){
			listAttributes = new JList(listAttributesModel);
			listAttributes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			listAttributes.setFixedCellHeight(20);
			listAttributes.setVisibleRowCount(10);
			listAttributes.setBorder(null);
			listAttributes.setSelectedIndex(0);
			
			listAttributes.addListSelectionListener(
	        		new ListSelectionListener(){
	        			public void valueChanged(ListSelectionEvent e) {
	        				if(!listAttributes.getValueIsAdjusting()){
	        			    if ((e.getValueIsAdjusting() == false) && (listAttributes.getSelectedValue() != null)) {
	        			    	StochasticValueGenerator<?> valueGenerator = null;
	        			    	try {
									valueGenerator = (StochasticValueGenerator<?>) dataContainer.getAttributeValueGenerator().getValueGenerator(listAttributes.getSelectedValue().toString());
								} catch (ParameterException e1) {
									JOptionPane.showMessageDialog(DataContainerDialog.this, "Cannot extract value generator for attribute \""+listAttributes.getSelectedValue().toString()+"\".", "Internal Exception", JOptionPane.ERROR_MESSAGE);
								}
	        			    	if(valueGenerator != null && !valueGenerator.getElements().isEmpty()){
	        			    		try {
	        			    			dontComplainAboutTypeChange = true;
	        			    			comboValueType.setSelectedItem(valueGenerator.getValueClass().getSimpleName());
	        			    			dontComplainAboutTypeChange = false;
	        			    		} catch (InconsistencyException e1) {
	        			    			JOptionPane.showMessageDialog(DataContainerDialog.this, "Cannot extract value type for attribute \""+listAttributes.getSelectedValue().toString()+"\".", "Internal Exception", JOptionPane.ERROR_MESSAGE);
	        			    		}
	        			    	} else {
	        			    		dontComplainAboutTypeChange = true;
	        			    		comboValueType.setSelectedItem("String");
	        			    		dontComplainAboutTypeChange = false;
	        			    	}
	     
	        			    	updateValueList();
	        			    }
	        				}
	        			}
	        		}
	        );
		}
		return listAttributes;
	}
	
	
	private JList getValueList(){
		if(listValues == null){
			listValues = new JList(listValueModel);
			listValues.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			listValues.setFixedCellHeight(20);
			listValues.setVisibleRowCount(10);
			listValues.setBorder(null);
			listValues.setSelectedIndex(0);
			listValues.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_DELETE){
						removeSelectedValues();
					}
				}

				@Override
				public void keyPressed(KeyEvent e) {
					removeSelectedValues();
				}
			});
		}
		return listValues;
	}
	
	private void removeSelectedValues(){
		if(listValues.getSelectedValue() != null){
			for(Object selectedObject: listValues.getSelectedValues()){
				String valueString = selectedObject.toString().substring(0, selectedObject.toString().indexOf(':'));
				try {
					((StochasticValueGenerator<?>) dataContainer.getAttributeValueGenerator().getValueGenerator(getAttribute())).removeElement(getValue(valueString));
				} catch (ParameterException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				updateValueList();
				if(listValueModel.isEmpty()){
					dataContainer.getAttributeValueGenerator().removeValueGenerator(getAttribute());
				}
			}
		}
	}
	
	private void updateValueList(){
		listValueModel.clear();
		if(listAttributes.getSelectedValue() != null){
			String attribute = listAttributes.getSelectedValue().toString();
			StochasticValueGenerator<?> valueGenerator = null;
			try {
				valueGenerator = (StochasticValueGenerator<?>) attValueGenerator.getValueGenerator(attribute);
			} catch (ParameterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e){
				//TODO
			}
			if(valueGenerator != null){
				for(Object element: valueGenerator.getElements()){
					listValueModel.addElement(String.format(valueFormat, element.toString(), FormatUtils.format(valueGenerator.getProbability(element)*100,2)));
				}
			}
		}
	}
	
	public CaseDataContainer getCaseDataContainer(){
		return dataContainer;
	}
	
	public static CaseDataContainer showDialog(Window owner, Set<String> attributes) throws ParameterException{
		DataContainerDialog dialog = new DataContainerDialog(owner, attributes);
		return dialog.getCaseDataContainer();
	}
	
	public static CaseDataContainer showDialog(Window owner, Set<String> attributes, CaseDataContainer dataContainer) throws ParameterException{
		DataContainerDialog dialog = new DataContainerDialog(owner, attributes, dataContainer);
		return dialog.getCaseDataContainer();
	}
	
	public static void main(String[] args) throws Exception{
		DataContainerDialog.showDialog(null, new HashSet<String>(Arrays.asList("att1", "att2")));
	}
}
