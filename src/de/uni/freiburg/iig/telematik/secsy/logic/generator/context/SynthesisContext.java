/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.secsy.logic.generator.context;

import de.invation.code.toval.misc.soabase.SOABase;
import static de.invation.code.toval.misc.soabase.SOABase.createFromProperties;
import de.invation.code.toval.misc.soabase.SOABaseProperties;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.types.DataUsage;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.acl.ACLModel;
import de.uni.freiburg.iig.telematik.sewol.context.constraint.ConstraintContext;
import de.uni.freiburg.iig.telematik.sewol.context.constraint.ConstraintContextProperties;
import java.awt.Window;
import java.io.File;
import java.util.Arrays;

/**
 *
 * @author stocker
 */
public class SynthesisContext extends ConstraintContext {

    public SynthesisContext() {
    }

    public SynthesisContext(String name) {
        super(name);
    }

    public SynthesisContext(ConstraintContextProperties properties) throws PropertyException {
        super(properties);
    }

    public static SynthesisContext createFromFile(File file) throws Exception {
        SOABaseProperties properties = ConstraintContextProperties.loadPropertiesFromFile(file);
        if (!(properties instanceof ConstraintContextProperties)) {
            throw new Exception("Loaded properties are not compatible with synthesis context");
        }
        SOABase newContext = createFromProperties(properties);
        if (!(newContext instanceof SynthesisContext)) {
            throw new Exception("Created context of wrong type, expected \"SynthesisContext\" but was \"" + newContext.getClass().getSimpleName() + "\"");
        }
        return (SynthesisContext) newContext;
    }

    @Override
    public boolean showDialog(Window parent) throws Exception {
        return SynthesisContextDialog.showDialog(parent, this);
    }

    public static void main(String[] args) throws Exception {
        SynthesisContext c = new SynthesisContext("Context");
        c.setSubjects(Arrays.asList("U1", "U2", "U3", "U4"));
        c.setObjects(Arrays.asList("O1", "O2", "O3", "O4"));
        c.setActivities(Arrays.asList("T1", "T2", "T3", "T4"));

        ACLModel acl = new ACLModel("acl1", c);
        acl.setActivityPermission("U1", Arrays.asList("T1", "T2"));
        acl.setActivityPermission("U2", Arrays.asList("T3", "T2"));
        acl.addObjectPermission("U1", "O2", DataUsage.CREATE, DataUsage.READ);
        acl.addObjectPermission("U2", "O3", DataUsage.WRITE, DataUsage.READ);
        acl.addObjectPermission("U2", "O4", DataUsage.CREATE, DataUsage.DELETE);
        System.out.println(acl);

        c.setACModel(acl);
        c.getProperties().store("/Users/stocker/Desktop/Context");
    }
}
