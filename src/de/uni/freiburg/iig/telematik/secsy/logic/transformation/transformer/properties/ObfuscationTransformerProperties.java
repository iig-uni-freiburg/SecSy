package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import de.invation.code.toval.misc.ArrayUtils;
import de.invation.code.toval.misc.StringUtils;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.log.EntryField;


public class ObfuscationTransformerProperties  extends AbstractMultipleTraceTransformerProperties {
	
	public static final EntryField[] defaultExcludedFields = {EntryField.TIME};

	public ObfuscationTransformerProperties() {
		super();
	}

	public ObfuscationTransformerProperties(String fileName) throws IOException {
		super(fileName);
	}
	
	public void setExcludedFields(EntryField... excludedFields){
		validateExcludedFields(excludedFields);
		props.setProperty(ObfuscationTransformerProperty.EXCLUDED_FIELDS.toString(), ArrayUtils.toString(excludedFields));
	}
	
	public void setExcludedFields(Set<EntryField> excludedFields){
		Validate.notNull(excludedFields);
		Validate.noNullElements(excludedFields);
		props.setProperty(ObfuscationTransformerProperty.EXCLUDED_FIELDS.toString(), ArrayUtils.toString(excludedFields.toArray()));
	}
	
	public Set<EntryField> getExcludedFields() throws PropertyException{
		Set<EntryField> result = new HashSet<EntryField>();
		String propertyValue = props.getProperty(ObfuscationTransformerProperty.EXCLUDED_FIELDS.toString());
		if(propertyValue == null)
			return result;
		StringTokenizer fieldTokens = StringUtils.splitArrayString(propertyValue, " ");
		while(fieldTokens.hasMoreTokens()){
			String nextToken = fieldTokens.nextToken();
			try{
				EntryField nextField = EntryField.valueOf(nextToken);
				result.add(nextField);
			}catch(Exception e){
				throw new PropertyException("EntryField", nextToken);
			}
		}
		return result;
	}
	
	public static void validateExcludedFields(EntryField... excludedFields){
		Validate.notNull(excludedFields);
		Validate.notEmpty(excludedFields);
		Validate.noNullElements(excludedFields);
	}

	@Override
	protected Properties getDefaultProperties(){
		Properties defaultProperties = super.getDefaultProperties();
		defaultProperties.setProperty(ObfuscationTransformerProperty.EXCLUDED_FIELDS.toString(), ArrayUtils.toString(defaultExcludedFields));
		return defaultProperties;
	}

	private enum ObfuscationTransformerProperty {
		EXCLUDED_FIELDS;
	}
	
}
