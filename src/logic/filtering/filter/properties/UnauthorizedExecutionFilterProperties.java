package logic.filtering.filter.properties;

import java.io.IOException;

public class UnauthorizedExecutionFilterProperties extends AbstractMultipleTraceFilterProperties {

	
	public UnauthorizedExecutionFilterProperties() {
		super();
	}

	public UnauthorizedExecutionFilterProperties(String fileName) throws IOException {
		super(fileName);
	}
	
}
