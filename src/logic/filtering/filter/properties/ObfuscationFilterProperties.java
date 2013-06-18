package logic.filtering.filter.properties;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import log.EntryField;
import misc.ArrayUtils;
import misc.StringUtils;
import properties.PropertyException;
import validate.ParameterException;
import validate.Validate;

public class ObfuscationFilterProperties  extends AbstractMultipleTraceFilterProperties {
	
	public static final EntryField[] defaultExcludedFields = {EntryField.TIME};

	public ObfuscationFilterProperties() {
		super();
	}

	public ObfuscationFilterProperties(String fileName) throws IOException {
		super(fileName);
	}
	
	public void setExcludedFields(EntryField... excludedFields) throws ParameterException {
		validateExcludedFields(excludedFields);
		props.setProperty(ObfuscationFilterProperty.EXCLUDED_FIELDS.toString(), ArrayUtils.toString(excludedFields));
	}
	
	public void setExcludedFields(Set<EntryField> excludedFields) throws ParameterException {
		Validate.notNull(excludedFields);
		Validate.noNullElements(excludedFields);
		props.setProperty(ObfuscationFilterProperty.EXCLUDED_FIELDS.toString(), ArrayUtils.toString(excludedFields.toArray()));
	}
	
	public Set<EntryField> getExcludedFields() throws PropertyException{
		Set<EntryField> result = new HashSet<EntryField>();
		String propertyValue = props.getProperty(ObfuscationFilterProperty.EXCLUDED_FIELDS.toString());
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
	
	public static void validateExcludedFields(EntryField... excludedFields) throws ParameterException{
		Validate.notNull(excludedFields);
		Validate.notEmpty(excludedFields);
		Validate.noNullElements(excludedFields);
	}

	@Override
	protected Properties getDefaultProperties(){
		Properties defaultProperties = super.getDefaultProperties();
		defaultProperties.setProperty(ObfuscationFilterProperty.EXCLUDED_FIELDS.toString(), ArrayUtils.toString(defaultExcludedFields));
		return defaultProperties;
	}

	private enum ObfuscationFilterProperty {
		EXCLUDED_FIELDS;
	}
	
}
