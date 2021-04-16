package org.mockobor.utils.reflection;

import lombok.NonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;


public final class ReflectionUtils {

	private ReflectionUtils() {}


	// ==================================================================================
	// ============================= getReachableMethods ================================
	// ==================================================================================

	/**
	 * To get all methods which can be reached from external object.
	 * <p>
	 * It returns all methods declared in the class/interface of the specified object and all its superclasses/implemented interfaces.
	 * <p>
	 * Exclude private and Object's methods.
	 *
	 * @param object object to search for methods
	 * @return a mutable list of methods. Changes in this list have no effect on the specified class.
	 */
	public static @NonNull Collection<Method> getReachableMethods( @NonNull Object object ) {
		return getReachableMethods( object.getClass() );
	}


	/**
	 * To get all methods which can be reached from external object.
	 * <p>
	 * It returns all methods declared in the specified class/interface and all its superclasses/implemented interfaces.
	 * Exclude private methods.
	 *
	 * @param clazz class to search for methods
	 * @return a mutable list of methods. Changes in this list have no effect on the specified class.
	 */
	public static @NonNull Collection<Method> getReachableMethods( @NonNull Class<?> clazz ) {
		Collection<Method> methods = new ArrayList<>();
		collectApplicableMethods( clazz, methods );
		return methods;
	}


	private static void collectApplicableMethods( @NonNull Class<?> clazz, @NonNull Collection<Method> collectedMethods ) {

		// collect declaring methods
		if( !isMockSubclass( clazz ) ) {
			for( Method m : clazz.getDeclaredMethods() ) {
				if( isApplicable( m ) && !isOverridden( collectedMethods, m ) ) {
					collectedMethods.add( m );
				}
			}
		}

		// collect in superclass
		Class<?> superclass = clazz.getSuperclass();
		if( superclass != null && !Objects.equals( superclass, Object.class ) ) {
			collectApplicableMethods( superclass, collectedMethods );
		}

		// collect default methods from interfaces
		for( Class<?> iface : clazz.getInterfaces() ) {
			collectApplicableMethods( iface, collectedMethods );
		}
	}

	private static boolean isOverridden( @NonNull Collection<Method> alreadyCollectedMethods, @NonNull Method method ) {
		return alreadyCollectedMethods.stream().anyMatch(
			alreadyCollected -> Objects.equals( alreadyCollected.getName(), method.getName() )
			                    && Objects.equals( alreadyCollected.getReturnType(), method.getReturnType() )
			                    && Arrays.equals( alreadyCollected.getParameterTypes(), method.getParameterTypes() ) );
	}

	private static boolean isApplicable( @NonNull Method method ) {
		return !Modifier.isPrivate( method.getModifiers() );
	}

	private static boolean isMockSubclass( @NonNull Class<?> clazz ) {
		return isMockitoMock( clazz );
	}

	private static boolean isMockitoMock( @NonNull Class<?> clazz ) {
		return clazz.getName().contains( "$MockitoMock$" ) || clazz.getName().startsWith( "org.mockito" );
	}


	// ==================================================================================
	// ============================= helpers for Method =================================
	// ==================================================================================

	/**
	 * To test if the specified method has the specified name and parameters extract with the specified types.
	 *
	 * @param method     {@link Method} to check
	 * @param name       expected method name
	 * @param paramTypes expected parameter types
	 * @return true if the method has same name and parameters types
	 */
	public static boolean methodMatch( @NonNull Method method, @NonNull String name, @NonNull Class<?>... paramTypes ) {
		return name.equals( method.getName() ) && Arrays.equals( method.getParameterTypes(), paramTypes );
	}


	/**
	 * To find method compatible to the {@code methodToCompare} in the specified methods list.
	 * <p>
	 * Methods are compatible if both have same method signature (the name, return type and parameter types).
	 * <p>
	 * It makes sense to use this method in invocation handler by creation of dynamic proxy,
	 * to find and invoke implementation of compatible method declared in another interface
	 * (if both interface implemented in proxy class).
	 *
	 * @param methods         list of methods to search in
	 * @param methodToCompare method to compare
	 * @return compatible methods from the specified list of null if not found
	 * @see #areMethodsCompatible
	 */
	public static Method findCompatibleMethod( Collection<Method> methods, Method methodToCompare ) {
		return methods.stream().filter( m -> areMethodsCompatible( m, methodToCompare ) ).findFirst().orElse( null );
	}

	/**
	 * To test if the method1 and method2 are "compatible" - have same method signature
	 * (the method1 and method2 have same name, return type and parameter types).
	 * <p>
	 * It makes sense to use this method in invocation handler by creation of dynamic proxy,
	 * to find and invoke implementation of compatible method declared in another interface
	 * (if both interface implemented in proxy class).
	 *
	 * @param method1 first {@link Method} to check
	 * @param method2 second {@link Method} to check
	 * @return true if the method1 and method2 have same name, return type and parameter types
	 */
	public static boolean areMethodsCompatible( @NonNull Method method1, @NonNull Method method2 ) {
		return Objects.equals( method1.getName(), method2.getName() )
		       && Objects.equals( method1.getReturnType(), method2.getReturnType() )
		       && Arrays.equals( method1.getParameterTypes(), method2.getParameterTypes() );
	}


	/**
	 * To get default value for the specified class.
	 * <p>
	 * It works correct for primitive types too!
	 *
	 * @param clazz class to get default value
	 * @param <T>   class to get default value
	 * @return default value for the specified class
	 */
	@SuppressWarnings( "unchecked" )
	public static <T> T getDefaultValue( Class<T> clazz ) {
		return clazz.isPrimitive() && clazz != void.class
		       ? (T) Array.get( Array.newInstance( clazz, 1 ), 0 )
		       : null;
	}


	// ==================================================================================
	// ============================ invoke default method ===============================
	// ==================================================================================

	/**
	 * To invoke default method of an interface in o dynamic proxy.
	 * <p>
	 * It makes sense to use this method in invocation handler by creation of dynamic proxy,
	 * to invoke default implementation of methods declared in interfaces implemented in proxy class.
	 *
	 * @param proxy  dynamic proxy which implemented the interface with the default method
	 * @param method default method to invoke
	 * @param args   invocation arguments
	 * @return result of invocation
	 * @throws Throwable on error
	 */
	public static Object invokeDefaultMethod( Object proxy, Method method, Object... args ) throws Throwable {
		assert method.isDefault() : "only default methods expected (method: " + method + ")"; // NOSONAR
		return isJava9plus()
		       ? invokeDefaultJava9plus( proxy, method, args )
		       : invokeDefaultJava8( proxy, method, args );
	}

	/** to invoke default method if run with java 8. */
	private static Object invokeDefaultJava8( Object proxy, Method method, Object... args ) throws Throwable {
		Class<?> clazz = method.getDeclaringClass();
		Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor( Class.class );
		constructor.setAccessible( true ); // NOSONAR
		return constructor.newInstance( clazz )
		                  .in( clazz )
		                  .unreflectSpecial( method, clazz )
		                  .bindTo( proxy )
		                  .invokeWithArguments( args );
	}

	/** to invoke default method if run with java 9+. */
	private static Object invokeDefaultJava9plus( Object proxy, Method method, Object... args ) throws Throwable {
		MethodType methodType = MethodType.methodType( method.getReturnType(), method.getParameterTypes() );
		return MethodHandles.lookup()
		                    .findSpecial( method.getDeclaringClass(), method.getName(), methodType, method.getDeclaringClass() )
		                    .bindTo( proxy )
		                    .invokeWithArguments( args );
	}

	public static boolean isJava9plus() {
		String javaSpecificationVersion = System.getProperty( "java.specification.version", "1." );
		return !javaSpecificationVersion.startsWith( "1." );
	}
}
