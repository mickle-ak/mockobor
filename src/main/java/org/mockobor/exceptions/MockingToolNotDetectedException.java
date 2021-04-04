package org.mockobor.exceptions;


/**
 * Thrown if mocking tool used to mocking of the mocked observable can not be detected.
 */
public class MockingToolNotDetectedException extends MockoborException {

	public MockingToolNotDetectedException( Object mockedObservable ) {
		super( "Can not detect mocking toll for %s. \n "
		       + "of class %s. \n"
		       + "Either it is not a mock, or used mocking tool does not supported by Mockobor.",
		       mockedObservable,
		       mockedObservable != null ? mockedObservable.getClass().getSimpleName() : "null" );
	}
}
