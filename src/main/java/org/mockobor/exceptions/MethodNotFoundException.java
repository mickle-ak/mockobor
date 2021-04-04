package org.mockobor.exceptions;

import java.lang.reflect.Method;
import java.util.Arrays;


/**
 * Thrown when no invocation handle can be found for a method.
 */
public class MethodNotFoundException extends MockoborException {

	public MethodNotFoundException( Method method, Object[] args ) {
		super( "can not find invocation handle for method <%s> \n, "
		       + "arguments: %s",
		       method, args != null ? Arrays.asList( args ) : "[]" );
	}

}
