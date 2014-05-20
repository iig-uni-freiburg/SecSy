package de.uni.freiburg.iig.telematik.secsy.logic.transformation;

import java.util.HashSet;
import java.util.Set;

import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.AbstractTransformer;


public class TransformerListenerSupport {
	
	private Set<TransformerListener> listeners = new HashSet<TransformerListener>();
	
	public void addTransformerListener(TransformerListener listener){
		Validate.notNull(listener);
		this.listeners.add(listener);
	}
	
	public void removeTransformerListener(TransformerListener listener){
		this.listeners.remove(listener);
	}
	
	public void fireTransformerMessage(String message){
		for(TransformerListener listener: listeners){
			listener.transformerMessage(message);
		}
	}
	
	public void fireTransformerSuccess(AbstractTransformer transformer){
		for(TransformerListener listener: listeners){
			listener.transformerSuccess(transformer);
		}
	}

	public void fireTransformerFailure(AbstractTransformer transformer){
		for(TransformerListener listener: listeners){
			listener.ransformerFailure(transformer);
		}
	}
	
}
