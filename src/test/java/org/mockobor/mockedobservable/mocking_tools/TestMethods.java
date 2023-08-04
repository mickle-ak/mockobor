package org.mockobor.mockedobservable.mocking_tools;

interface TestMethods {

	void objectArguments( Object a1, Object a2, Object a3 );

	void stringArgument( String s );

	int int2int( Integer i );

	String returnType();

	void primitiveArguments( int i, long l, char c, short s, byte b, float f, double d, boolean bn, String st );

	void varargObject( String p1, Object... params );

	void varargInt( String p1, int... params );

	void arrayObject( String p1, Object[] params );

	void arrayLong( String p1, long[] params );
}
