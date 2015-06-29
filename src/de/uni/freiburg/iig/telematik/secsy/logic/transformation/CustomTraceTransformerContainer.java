/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.secsy.logic.transformation;

import de.invation.code.toval.debug.SimpleDebugger;
import de.invation.code.toval.file.FileUtils;
import de.invation.code.toval.misc.wd.AbstractComponentContainer;
import de.invation.code.toval.misc.wd.ProjectComponentException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.AbstractTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.TransformerType;
import de.uni.freiburg.iig.telematik.secsy.gui.properties.SecSyProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.TransformerFactory;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.AbstractTraceTransformer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author stocker
 */
public class CustomTraceTransformerContainer extends AbstractComponentContainer<AbstractTraceTransformer> {

    public static final String CUSTOM_TRACE_TRANSFORMER_DESCRIPTOR = "Custom Transformer";
    public static final String CUSTOM_TRANSFORMER_PANEL_PACKAGE = "de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel.custom.";
    public static final String CUSTOM_TRANSFORMER_PACKAGE = "de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.custom.";

    private Set<TransformerType> customTransformerTypes = new HashSet<TransformerType>();
    private SecSyProperties secsyProperties = null;

    public CustomTraceTransformerContainer(SecSyProperties secsyProperties) throws Exception {
        this(secsyProperties, null);
    }

    public CustomTraceTransformerContainer(SecSyProperties secsyProperties, SimpleDebugger debugger) throws Exception {
        super(secsyProperties.getPathForCustomTransformers(), debugger);
        Validate.notNull(secsyProperties);
        this.secsyProperties = secsyProperties;
        loadCustomTransformerTypes();
    }
    
    @Override
    protected boolean mandatoryDirectory(){
        return false;
    }

    @Override
    public String getComponentDescriptor() {
        return CUSTOM_TRACE_TRANSFORMER_DESCRIPTOR;
    }

    @Override
    protected AbstractTraceTransformer loadComponentFromFile(String file) throws Exception {
        return TransformerFactory.loadCustomTransformer(file);
    }

    @Override
    protected void serializeComponent(AbstractTraceTransformer component, String serializationPath, String fileName) throws Exception {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(serializationPath.concat(fileName)));
        out.writeObject(component);
        out.close();
    }

    private void loadCustomTransformerTypes() throws Exception {
        if(!mandatoryDirectory() && !new File(secsyProperties.getPathForCustomTransformerTypes()).exists())
            return;

        List<File> transformerTypeDirectories = null;
        try {
            transformerTypeDirectories = FileUtils.getSubdirectories(SecSyProperties.getInstance().getPathForCustomTransformerTypes());
        } catch (IOException e1) {
            throw new ProjectComponentException("Cannot access custom transformer type directory.");
        }
        for (File transformerTypeDirectory : transformerTypeDirectories) {
            debugMessage("Loading custom transformer type: " + transformerTypeDirectory.getName());
            try {
                loadCustomTransformerType(transformerTypeDirectory);
                debugMessage("Done.");
            } catch (Exception e) {
                debugMessage("Error: " + e.getMessage());
            }
        }
        debugMessage(null);
    }

    public Set<TransformerType> getCustomTransformerTypes() {
        return Collections.unmodifiableSet(customTransformerTypes);
    }

    private void loadCustomTransformerType(File transformerTypeDirectory) throws ParameterException, IOException {

        String transformerName = transformerTypeDirectory.getName();
        // Check if all necessary files are there
        List<File> classFiles = null;
        classFiles = FileUtils.getFilesInDirectory(transformerTypeDirectory.getAbsolutePath(), "class");

        if (classFiles.size() != 2) {
            throw new ParameterException("Required files missing for custom transformer type\"" + transformerName + "\"");
        }

        // Try to load the class files
        // 1. Check if there is a transformer class
        File transformerClassFile = null;
        File transformerPanelClassFile = null;
        if (FileUtils.separateFileNameFromEnding(classFiles.get(0)).equals(transformerName)) {
            transformerClassFile = classFiles.get(0);
            transformerPanelClassFile = classFiles.get(1);
        } else if (FileUtils.separateFileNameFromEnding(classFiles.get(1)).equals(transformerName)) {
            transformerClassFile = classFiles.get(1);
            transformerPanelClassFile = classFiles.get(0);
        }
        if (transformerClassFile == null) {
            throw new ParameterException("Cannot find transformer class file.");
        }

        // 2. Try to load the transformer class
        // Prepare class loader
        ClassLoader loader = null;
        try {
            URL url = transformerTypeDirectory.toURI().toURL();
            URL[] urls = new URL[]{url};
            loader = new URLClassLoader(urls);
        } catch (MalformedURLException e) {
            throw new ParameterException("Error while preparing class loader: " + e.getMessage());
        }

        // Load transformer class
        Class<?> transformerClass = null;
        try {
            transformerClass = loader.loadClass(CUSTOM_TRANSFORMER_PACKAGE + FileUtils.separateFileNameFromEnding(transformerClassFile));
        } catch (NoClassDefFoundError e) {
            throw new ParameterException("Class loader exception, cannot find class: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new ParameterException("Class loader exception, cannot load class: " + e.getMessage());
        } catch (Exception e) {
            throw new ParameterException("Class loader exception: " + e.getMessage());
        }
        if (!AbstractTraceTransformer.class.isAssignableFrom(transformerClass)) {
            throw new ParameterException("Wrong type of loaded transformer class: " + transformerClass.getName());
        }
        // Try if class can be instantiated
        try {
            transformerClass.newInstance();
        } catch (Exception e) {
            throw new ParameterException("Transformer class cannot be instantiated: " + transformerClass.getName());
        }

        // Load transformer panel class
        Class<?> transformerPanelClass = null;
        try {
            transformerPanelClass = loader.loadClass(CUSTOM_TRANSFORMER_PANEL_PACKAGE + FileUtils.separateFileNameFromEnding(transformerPanelClassFile));
        } catch (NoClassDefFoundError e) {
            throw new ParameterException("Class loader exception, cannot find class: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new ParameterException("Class loader exception, cannot load class: " + e.getMessage());
        } catch (Exception e) {
            throw new ParameterException("Class loader exception: " + e.getMessage());
        }
        if (!AbstractTransformerPanel.class.isAssignableFrom(transformerPanelClass)) {
            throw new ParameterException("Wrong type of loaded transformer panel class: " + transformerPanelClass.getName());
        }

        customTransformerTypes.add(new TransformerType(transformerClass, transformerPanelClass));

        // Dateiname = Kassenname
        // Statische Methode gibt die Bezeichnung des Transformers zur�ck
    }

    public boolean isCustomTransformer(AbstractTraceTransformer transformer) {
        for (TransformerType customTransformerType : customTransformerTypes) {
            if (customTransformerType.getTransformerClass().isAssignableFrom(transformer.getClass())) {
                return true;
            }
        }
        return false;
    }

}
