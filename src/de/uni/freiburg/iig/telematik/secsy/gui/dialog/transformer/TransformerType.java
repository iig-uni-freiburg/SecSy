package de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer;

public class TransformerType implements Comparable<TransformerType>{
	
	private Class<?> transformerClass = null;
	private Class<?> transformerPanelClass = null;
	
	public TransformerType(Class<?> transformerClass, Class<?> transformerPanelClass) {
		this.transformerClass = transformerClass;
		this.transformerPanelClass = transformerPanelClass;
	}

	public Class<?> getTransformerClass() {
		return transformerClass;
	}

	public Class<?> getTransformerPanelClass() {
		return transformerPanelClass;
	}

	@Override
	public int compareTo(TransformerType o) {
		return transformerClass.getName().compareTo(o.getTransformerClass().getName());
	}
	
	@Override
	public String toString(){
		String simpleCLassName = transformerClass.getSimpleName();
		return simpleCLassName.replace("Transformer", "");
	}

}
