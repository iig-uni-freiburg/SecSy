package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.BoDTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.DayDelayTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.IncompleteLoggingTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.ObfuscationTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.SkipActivitiesTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.SoDTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.UnauthorizedExecutionTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.BoDPropertyTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.DayDelayTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.IncompleteLoggingTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.ObfuscationTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.SkipActivitiesTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.SoDPropertyTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.UnauthorizedExecutionTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.AbstractTraceTransformer;

public class TransformerFactory {
	
//	public static final String TRANSFORMER_PACKAGE = "de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.";
	
	private static final Map<Class<?>, Class<?>> propertiesClass = new HashMap<Class<?>, Class<?>>();
	static{
		propertiesClass.put(DayDelayTransformer.class, DayDelayTransformerProperties.class);
		propertiesClass.put(IncompleteLoggingTransformer.class, IncompleteLoggingTransformerProperties.class);
		propertiesClass.put(ObfuscationTransformer.class, ObfuscationTransformerProperties.class);
		propertiesClass.put(SkipActivitiesTransformer.class, SkipActivitiesTransformerProperties.class);
		propertiesClass.put(UnauthorizedExecutionTransformer.class, UnauthorizedExecutionTransformerProperties.class);
		propertiesClass.put(SoDPropertyTransformer.class, SoDTransformerProperties.class);
		propertiesClass.put(BoDPropertyTransformer.class, BoDTransformerProperties.class);
	}
	
	public static AbstractTraceTransformer loadTransformer(String file) throws IOException, PropertyException{
		AbstractTransformerProperties properties = new AbstractTransformerProperties();
		properties.load(file);
		String transformerClassName = properties.getType();
		try {
			Class<?> transformerClass = Class.forName(transformerClassName);
			Constructor constructor = transformerClass.getConstructor(propertiesClass.get(transformerClass));
			properties = (AbstractTransformerProperties) propertiesClass.get(transformerClass).newInstance();
			properties.load(file);
			Object newTransformer = constructor.newInstance((propertiesClass.get(transformerClass)).cast(properties));
			return (AbstractTraceTransformer) newTransformer;
		} catch(Exception e){
			throw new ParameterException("Cannot create transformer: " + e.getMessage());
		}
	}
	
	public static AbstractTraceTransformer loadCustomTransformer(String file) throws IOException, PropertyException{
		AbstractTraceTransformer newTransformer = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
			Object deserializedObject = in.readObject();
			newTransformer = (AbstractTraceTransformer) deserializedObject;
			in.close();
		} catch(Exception e){
			throw new ParameterException("Cannot create transformer: " + e.getMessage());
		}
		return newTransformer;
	}

}
