package logic.transformation;

import logic.transformation.transformer.AbstractTransformer;

public interface TransformerListener {
	
	public void transformerMessage(String message);
	
	public void transformerSuccess(AbstractTransformer transformer);
	
	public void ransformerFailure(AbstractTransformer transformer);

}
