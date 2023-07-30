package org.mockobor.utils.reflection;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockobor.utils.reflection.mockito.DisableForStandardMockitoMockMaker;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockobor.utils.reflection.ReflectionUtils.getReachableMethods;


@SuppressWarnings( "unused, RedundantMethodOverride")
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
		public void finalMethod() {}
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


	@ParameterizedTest( name = "getReachableMethods_Derived - {0}" )
	@MethodSource( "mockingTools" )
	void getReachableMethods_Derived( String name, Function<Class<?>, Object> mocker ) {
		Assumptions.assumeFalse( name.equals( "EasyMock" ) && ReflectionUtils.javaSpecificationVersion() >= 17, "EasyMock and Java 17+" );

		Collection<Method> methods = getReachableMethods( mocker.apply( Derived.class ) );

		assertThat( methods ).extracting( Method::getName ).containsExactlyInAnyOrder( EXPECTED_LIST_OF_METHOD_NAMES_IN_DERIVED_CLASS );
	}

	@ParameterizedTest( name = "getReachableMethods_DerivedInterface - {0}" )
	@MethodSource( "mockingTools" )
	void getReachableMethods_DerivedInterface( String name, Function<Class<?>, Object> mocker ) {
		Collection<Method> methods = getReachableMethods( mocker.apply( DerivedInterface.class ) );

		assertThat( methods )
				.extracting( Method::getName )
				.containsExactlyInAnyOrder( "publicDerivedMethod",
				                            "defaultInterfaceMethod",
				                            "interfaceMethod",
				                            "derivedInterfaceMethod" );
	}

	@Test
	@DisableForStandardMockitoMockMaker
	void getReachableMethods_FinalDerived_MockitoOnly() {
		Collection<Method> methods = getReachableMethods( Mockito.mock( FinalDerived.class ) );

		assertThat( methods ).extracting( Method::getName )
		                     .contains( EXPECTED_LIST_OF_METHOD_NAMES_IN_DERIVED_CLASS )
		                     .contains( "finalMethod" )
		                     .hasSize( EXPECTED_LIST_OF_METHOD_NAMES_IN_DERIVED_CLASS.length + 1 );
	}


	static Stream<Arguments> mockingTools() {
		return Stream.of(
				arguments( "Mockito", (Function<Class<?>, Object>) Mockito::mock ),
				arguments( "EasyMock", (Function<Class<?>, Object>) EasyMock::mock )
		);
	}
}
