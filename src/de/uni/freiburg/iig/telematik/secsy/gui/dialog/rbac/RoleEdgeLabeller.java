package de.uni.freiburg.iig.telematik.secsy.gui.dialog.rbac;
import org.apache.commons.collections15.Transformer;


public class RoleEdgeLabeller implements Transformer<String,String>{

	@Override
	public String transform(String input) {
//		return input.replaceAll("-", " dominates ");
		return "";
	}

}
