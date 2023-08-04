package org.mockobor.utils.reflection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@NoArgsConstructor( access = AccessLevel.PRIVATE )
public final class ReflectionUtils {

	// ==================================================================================
	// ============================= getReachableMethods ================================
	// ==================================================================================

	/**
	 * To get all methods that can be reached from an external object.
	 * <p>
	 * It returns all methods declared in the class/interface of the specified object and all its superclasses/implemented interfaces.
	 * <p>
	 * Exclude private and Object methods.
	 *
	 * @param object object to search for methods
	 * @return a mutable list of methods. Changes in this list have no effect on the specified class.
	 */
	public static @NonNull Collection<Method> getReachableMethods( @NonNull Object object ) {
		return getReachableMethods( object.getClass() );
	}


	/**
	 * To get all methods that can be reached from an external object.
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
		if( superclass != null && superclass != Object.class ) {
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
		return isMockitoMock( clazz ) || isEasymockMock( clazz );
	}

	/**
	 * To check if the specified class CAN be a class created by Mockito.
	 * <p>
	 * It does not use {@code MockUtil.isMock} because of Mockito can be not in classpath.
	 * Therefore, it is not 100% correct, but is OK for our purpose
	 * (for example, if inline mockito mock-maker used, it always returns false).
	 *
	 * @param clazz class to test
	 * @return true if the specified class CAN be a class created by Mockito
	 */
	public static boolean isMockitoMock( @NonNull Class<?> clazz ) {
		return clazz.getName().contains( "$MockitoMock$" ) || clazz.getName().startsWith( "org.mockito" );
	}


	/**
	 * To check if the specified class CAN be a class created by EasyMock.
	 * <p>
	 * It is not 100% correct, but is OK for our purpose...
	 *
	 * @param clazz class to test
	 * @return true if the specified class CAN be a class created by EasyMock
	 */
	public static boolean isEasymockMock( @NonNull Class<?> clazz ) {
		return clazz.getSimpleName().contains( "$EasyMock$" )
				|| clazz.getSimpleName().contains( "$EnhancerByCGLIB$" )
				|| clazz.getName().startsWith( "org.easymock" )
				|| Proxy.class.isAssignableFrom( clazz );
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
	 * @return true if the method has the same name and parameters types
	 */
	public static boolean methodMatch( @NonNull Method method, @NonNull String name, @NonNull Class<?>... paramTypes ) {
		return name.equals( method.getName() ) && Arrays.equals( method.getParameterTypes(), paramTypes );
	}


	/**
	 * To find method similar to the specified {@code invokedMethods} in the specified list of declared methods.
	 * <p>
	 * Methods are similar if both have same method signature (the name, return type and parameter types) - see {@link #isSimilar}.
	 * <p>
	 * It makes sense, for example, to use this method in invocation handler by creation of dynamic proxy,
	 * to find and invoke implementation of compatible method declared in another interface
	 * (if both interfaces are implemented in proxy class).
	 *
	 * @param declaredMethods list of declared methods to search in
	 * @param invokedMethods  invoked method to find similar declared one
	 * @return similar method from the specified list or null if not found
	 * @see #isSimilar
	 */
	public static @Nullable Method findSimilarMethod( @NonNull Collection<Method> declaredMethods,
	                                                  @NonNull Method invokedMethods ) {
		return declaredMethods.stream().filter( m -> isSimilar( m, invokedMethods ) ).findFirst().orElse( null );
	}

	/**
	 * To test if the methodA and methodB are "similar" - both have the same method signature:
	 * the equal names, return types, and parameter types.
	 *
	 * @param methodA first {@link Method} to check
	 * @param methodB second {@link Method} to check
	 * @return true if the methodA and methodB have the same name, return type and parameter types
	 */
	public static boolean isSimilar( @NonNull Method methodA, @NonNull Method methodB ) {
		return Objects.equals( methodA.getName(), methodB.getName() )
		       && Objects.equals( methodA.getReturnType(), methodB.getReturnType() )
		       && Arrays.equals( methodA.getParameterTypes(), methodB.getParameterTypes() );
	}


	// ==================================================================================
	// ============================ invoke default method ===============================
	// ==================================================================================

	/**
	 * To invoke the default method of an interface in dynamic proxy.
	 * <p>
	 * It makes sense to use this method in invocation handler by creation of dynamic proxy,
	 * to invoke the default implementation of methods declared in interfaces implemented in proxy class.
	 *
	 * @param proxy  dynamic proxy which implemented the interface with the default method
	 * @param method default method to invoke
	 * @param args   invocation arguments
	 * @return result of invocation
	 * @throws Throwable on error
	 */
	public static @Nullable Object invokeDefaultMethod( Object proxy, Method method, Object... args ) throws Throwable {
		assert method.isDefault() : "only default methods expected (method: " + method + ")"; // NOSONAR
		return isJava9plus()
		       ? invokeDefaultJava9plus( proxy, method, args )
		       : invokeDefaultJava8( proxy, method, args );
	}

	/** to invoke the default method if run with java 8. */
	private static @Nullable Object invokeDefaultJava8( Object proxy, Method method, Object... args ) throws Throwable {
		Class<?> clazz = method.getDeclaringClass();
		Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor( Class.class );
		constructor.setAccessible( true ); // NOSONAR
		return constructor.newInstance( clazz )
		                  .in( clazz )
		                  .unreflectSpecial( method, clazz )
		                  .bindTo( proxy )
		                  .invokeWithArguments( args );
	}

	/** to invoke the default method if run with java 9+. */
	private static @Nullable Object invokeDefaultJava9plus( Object proxy, Method method, Object... args ) throws Throwable {
		MethodType methodType = MethodType.methodType( method.getReturnType(), method.getParameterTypes() );
		return MethodHandles.lookup()
		                    .findSpecial( method.getDeclaringClass(), method.getName(), methodType, method.getDeclaringClass() )
		                    .bindTo( proxy )
		                    .invokeWithArguments( args );
	}


	// ==================================================================================
	// ================================ java version ====================================
	// ==================================================================================

	/**
	 * To check if the currently running java has version 9 or above.
	 * It is clearly faster as {@link #javaSpecificationVersion()}.
	 *
	 * @return true if the current java version is 9 or higher; false for java 8 or less
	 */
	private static boolean isJava9plus() {
		String javaSpecificationVersion = System.getProperty( "java.specification.version", "1.8" );
		return !javaSpecificationVersion.startsWith( "1." );
	}


	/** @return java version as int (1 for 1.1, ..., 5 for 1.5, ..., 8 for 1.8, 9 for 9 etc.) */
	public static int javaSpecificationVersion() {
		String javaSpecificationVersion = System.getProperty( "java.specification.version", "1.8" );
		return parseJavaSpecificationVersion( javaSpecificationVersion );
	}

	private static final Pattern JAVA_VERSION_REGEX = Pattern.compile( "^(1\\.)?(\\d+).*" );

	static int parseJavaSpecificationVersion( @NonNull String javaSpecificationVersion ) {
		Matcher versionMatcher = JAVA_VERSION_REGEX.matcher( javaSpecificationVersion );
		return versionMatcher.matches() ? Integer.parseInt( versionMatcher.group( 2 ) ) : 8;
	}
}
