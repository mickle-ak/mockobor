package org.mockobor.utils.reflection;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockobor.utils.reflection.ReflectionUtils.*;


@SuppressWarnings( "unused" )
class ReflectionUtils_Methods_Test {

	private interface TestMethods {
		void method( String s );
		void method( Object s );
		void method( Integer s );
		void method();
		default String defaultMethod( String param ) {
			return param;
		}
	}


	// ==================================================================================
	// ============================ invoke default method ===============================
	// ==================================================================================

	@Test
	void testInvokeDefaultMethod() {
		TestMethods testMethods = (TestMethods) Proxy.newProxyInstance(
			Thread.currentThread().getContextClassLoader(),
			new Class[]{ TestMethods.class },
			ReflectionUtils::invokeDefaultMethod );

		assertThat( testMethods.defaultMethod( "paramValue" ) ).isEqualTo( "paramValue" );
	}


	// ==================================================================================
	// ================================= methodMatch ====================================
	// ==================================================================================

	@Test
	void testMethodMatch() throws NoSuchMethodException {
		Class<TestMethods> clazz = TestMethods.class;

		assertThat( methodMatch( clazz.getMethod( "method", String.class ), "method", String.class ) )
			.isTrue();

		assertThat( methodMatch( clazz.getMethod( "method", Integer.class ), "method", Integer.class ) )
			.isTrue();

		assertThat( methodMatch( clazz.getMethod( "method", Object.class ), "method", Object.class ) )
			.isTrue();

		assertThat( methodMatch( clazz.getMethod( "method" ), "method" ) )
			.isTrue();

		assertThat( methodMatch( clazz.getMethod( "method", Object.class ), "method", List.class ) )
			.isFalse();
	}


	// ==================================================================================
	// ============================= compatible methods =================================
	// ==================================================================================

	private interface TestMethods2 {
		void method( String s );
		void method( Object s );
		int method( Integer s );
		void method();
		void anotherMethod();
	}


	@Test
	void testFindCompatibleMethod_found() throws NoSuchMethodException {
		Collection<Method> declaredMethods = Arrays.asList( TestMethods.class.getDeclaredMethods() );

		Method invokedMethod = TestMethods2.class.getMethod( "method", String.class );
		assertThat( declaredMethods ).doesNotContain( invokedMethod ); // to be sure

		Method declaredMethod = findSimilarMethod( declaredMethods, invokedMethod );

		assertThat( declaredMethods ).as( "result from the methods list" ).contains( declaredMethod );
		assertThat( isSimilar( declaredMethod, invokedMethod ) ).as( "compatible methods" ).isTrue();
		assertThat( declaredMethod ).as( "compatible method is another method" ).isNotEqualTo( invokedMethod );
		assertThat( declaredMethod.getDeclaringClass() ).as( "because of another declaring class" ).isNotEqualTo( invokedMethod.getDeclaringClass() );
	}

	@Test
	void testFindCompatibleMethod_not_found() throws NoSuchMethodException {
		Collection<Method> methods = Arrays.asList( TestMethods.class.getDeclaredMethods() );

		Method methodToCompare = TestMethods2.class.getMethod( "anotherMethod" );
		assertThat( methods ).doesNotContain( methodToCompare ); // to be sure

		assertThat( findSimilarMethod( methods, methodToCompare ) ).as( "not found" ).isNull();
	}


	@Test
	void testIsSimilar() throws NoSuchMethodException {
		Class<TestMethods> clazz1 = TestMethods.class;
		Class<TestMethods2> clazz2 = TestMethods2.class;

		assertThat( isSimilar( clazz1.getMethod( "method" ),
		                       clazz2.getMethod( "method" ) ) )
				.isTrue();

		assertThat( isSimilar( clazz1.getMethod( "method", String.class ),
		                       clazz2.getMethod( "method", String.class ) ) )
				.isTrue();

		assertThat( isSimilar( clazz1.getMethod( "method" ),
		                       clazz2.getMethod( "anotherMethod" ) ) )
				.as( "different method name" )
				.isFalse();

		assertThat( isSimilar( clazz1.getMethod( "method", Integer.class ),
		                       clazz2.getMethod( "method", Object.class ) ) )
				.as( "different parameter type" )
				.isFalse();

		assertThat( isSimilar( clazz1.getMethod( "method", Integer.class ),
		                       clazz2.getMethod( "method", Integer.class ) ) )
				.as( "different return type" )
				.isFalse();
	}
}
