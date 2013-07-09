package de.uni.freiburg.iig.telematik.secsy.logic.transformation;

import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.AbstractTransformer;

public interface TransformerListener {
	
	public void transformerMessage(String message);
	
	public void transformerSuccess(AbstractTransformer transformer);
	
	public void ransformerFailure(AbstractTransformer transformer);

}
