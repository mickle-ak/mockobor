package org.mockobor.utils.reflection;

import org.mockobor.utils.reflection.mockito.DisableForStandardMockitoMockMaker;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collection;

import static org.mockobor.utils.reflection.ReflectionUtils.getReachableMethods;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


@SuppressWarnings( "unused" )
class ReflectionUtils_ReachableMethods_Test {

	private interface Interface {

		void publicDerivedMethod();

		default void defaultInterfaceMethod() {}
	}

	private static class Base {

		private void privateBaseMethods() {}

		protected void protectedBaseMethods() {}

		public void publicBaseMethod() {}

		protected void protectedOverriddenMethod() {}

		void packageBaseMethod() {}

		protected static void protectedStaticBaseMethod() {}
	}

	private static class Derived extends Base implements Interface {

		private void privateDerivedMethods() {}

		protected void protectedDerivedMethods() {}

		@Override
		public void publicDerivedMethod() {}

		public void publicDerivedMethod( String param ) {}

		@Override
		protected void protectedOverriddenMethod() {}

		void packageDerivedMethod() {}

		public static void publicStaticDerivedMethod() {}
	}

	private final static class FinalDerived extends Derived implements Interface {
		@Override
		protected void protectedOverriddenMethod() {}

		public final void finalMethod() {}
	}

	private interface Interface2 {
		void interfaceMethod();

		void publicDerivedMethod();
	}

	private interface DerivedInterface extends Interface, Interface2 {
		void derivedInterfaceMethod();

		@Override
		void publicDerivedMethod();
	}


	private final String[] EXPECTED_LIST_OF_METHOD_NAMES_IN_DERIVED_CLASS = {
		"defaultInterfaceMethod",
		"protectedBaseMethods",
		"publicBaseMethod",
		"protectedOverriddenMethod",
		"packageBaseMethod",
		"protectedStaticBaseMethod",
		"protectedDerivedMethods",
		"publicDerivedMethod", // overloaded Method - without parameters
		"publicDerivedMethod", // overloaded Method - with parameters
		"packageDerivedMethod",
		"publicStaticDerivedMethod" };


	@Test
	void getReachableMethods_Derived() {
		Collection<Method> methods = getReachableMethods( mock( Derived.class ) );

		assertThat( methods ).extracting( Method::getName ).containsExactlyInAnyOrder( EXPECTED_LIST_OF_METHOD_NAMES_IN_DERIVED_CLASS );
	}

	@Test
	void getReachableMethods_DerivedInterface() {
		Collection<Method> methods = getReachableMethods( mock( DerivedInterface.class ) );

		assertThat( methods )
			.extracting( Method::getName )
			.containsExactlyInAnyOrder( "publicDerivedMethod",
			                            "defaultInterfaceMethod",
			                            "interfaceMethod",
			                            "derivedInterfaceMethod" );
	}

	@Test
	@DisableForStandardMockitoMockMaker
	void getReachableMethods_FinalDerived() {
		Collection<Method> methods = getReachableMethods( mock( FinalDerived.class ) );

		assertThat( methods ).extracting( Method::getName )
		                     .contains( EXPECTED_LIST_OF_METHOD_NAMES_IN_DERIVED_CLASS )
		                     .contains( "finalMethod" )
		                     .hasSize( EXPECTED_LIST_OF_METHOD_NAMES_IN_DERIVED_CLASS.length + 1 );
	}
}
