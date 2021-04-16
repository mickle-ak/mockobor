package org.mockobor.listener_detectors;

import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockobor.listener_detectors.AbstractDetector.RegistrationParameters;
import org.mockobor.mockedobservable.MockedObservable;
import org.mockobor.mockedobservable.MockedObservable.MyAnotherListener;
import org.mockobor.mockedobservable.MockedObservable.MyListener;
import org.mockobor.utils.reflection.ReflectionUtils;

import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockobor.listener_detectors.ListenerSelector.selector;


class AbstractDetectorTest {

	private final AbstractDetector detector = new AbstractDetector() {

		@Override
		protected boolean isListenerClass( Class<?> parameterType, Method method ) {
			return parameterType.getSimpleName().endsWith( "Listener" );
		}

		@Override
		protected boolean isAddMethods( @NonNull Method method ) {
			return method.getName().startsWith( "add" );
		}

		@Override
		protected boolean isRemoveMethods( @NonNull Method method ) {
			return method.getName().startsWith( "remove" );
		}
	};


	@SuppressWarnings( "unused" )
	interface Methods {

		void onlyListener( MyListener listener );

		void stringAndListener( String name, MyListener listener );

		void manyParametersAndListener( String s1, Object o2, MyListener l3, Integer i4 );

		void noListeners( Object o1, Object o2 );
	}

	interface OnlyRemoveMethod {

		@SuppressWarnings( "unused" )
		void removeMyListener( MyListener listener );
	}

	interface OnlyAddMethod {

		@SuppressWarnings( "unused" )
		void addMyListener( MyListener listener );
	}


	// ==================================================================================
	// ============================= detectRegistrations ================================
	// ==================================================================================

	@Test
	void detectRegistrations_detected() {
		Collection<Method> methods = ReflectionUtils.getReachableMethods( MockedObservable.class );
		ListenersDefinition listenersDefinition = detector.detectRegistrations( methods );

		assertThat( listenersDefinition.hasListenerDetected() ).isTrue();
		assertThat( listenersDefinition.getRegistrations() )
			.as( "10 add/removeXxxListener methods in MockedObservable" )
			.hasSize( 10 );
		assertThat( listenersDefinition.getDetectedListeners() )
			.as( "add/removeXxxListener methods for follow listeners in MockedObservable" )
			.containsExactlyInAnyOrder( MyListener.class, MyAnotherListener.class, PropertyChangeListener.class );
	}

	@Test
	void detectRegistrations_detected_even_only_add_method() {
		Collection<Method> methods = ReflectionUtils.getReachableMethods( OnlyAddMethod.class );
		ListenersDefinition listenersDefinition = detector.detectRegistrations( methods );

		assertThat( listenersDefinition.hasListenerDetected() ).isTrue();
		assertThat( listenersDefinition.getRegistrations() )
			.as( "only 1 addListener methods in OnlyAddMethod" )
			.hasSize( 1 );
		assertThat( listenersDefinition.getDetectedListeners() )
			.as( "addListener methods for follow listeners in OnlyAddMethod" )
			.containsExactly( MyListener.class );
	}


	@Test
	void detectRegistrations_nothing_detected_because_no_registration_methods() {
		Collection<Method> methods = ReflectionUtils.getReachableMethods( Method.class );
		ListenersDefinition listenersDefinition = detector.detectRegistrations( methods );

		assertThat( listenersDefinition.hasListenerDetected() ).isFalse();
		assertThat( listenersDefinition.getDetectedListeners() ).as( "no listener detected" ).isEmpty();
		assertThat( listenersDefinition.getRegistrations() ).as( "no registration found" ).isEmpty();
	}

	@Test
	void detectRegistrations_nothing_detected_because_only_remove_methods() {
		Collection<Method> methods = ReflectionUtils.getReachableMethods( OnlyRemoveMethod.class );
		ListenersDefinition listenersDefinition = detector.detectRegistrations( methods );

		assertThat( listenersDefinition.hasListenerDetected() ).isFalse();
		assertThat( listenersDefinition.getDetectedListeners() ).as( "no listener detected" ).isEmpty();
		assertThat( listenersDefinition.getRegistrations() ).as( "remove method found" ).hasSize( 1 );
	}


	// ==================================================================================
	// ============================= hasListenerParameter ===============================
	// ==================================================================================

	@ParameterizedTest( name = "[{index}] hasListenerParameter_listener_found({argumentsWithNames})" )
	@MethodSource( "hasListenerParameter_listener_found" )
	void hasListenerParameter_listener_found( String methodName, int expectedListenerIndex, Integer[] expectedSelectorIndexes ) {
		RegistrationParameters registrationParameters = detector.getListenerParameter( findMethod( methodName ) );
		assertThat( registrationParameters.getListenerIndex() ).isEqualTo( expectedListenerIndex );
		assertThat( registrationParameters.getSelectorIndexes() ).containsExactly( expectedSelectorIndexes );
	}

	@SuppressWarnings( "unused" ) // used as @MethodSource for hasListenerParameter_listener_found
	private static Stream<Arguments> hasListenerParameter_listener_found() {
		return Stream.of(
			arguments( "onlyListener", 0, new Integer[0] ),
			arguments( "stringAndListener", 1, new Integer[]{ 0 } ),
			arguments( "manyParametersAndListener", 2, new Integer[]{ 0, 1, 3 } )
		);
	}

	@Test
	void hasListenerParameter_listener_not_found() {
		RegistrationParameters registrationParameters = detector.getListenerParameter( findMethod( "noListeners" ) );
		assertThat( registrationParameters ).isNull();
	}


	// ==================================================================================
	// =========================== RegistrationParameters ===============================
	// ==================================================================================

	@ParameterizedTest( name = "[{index}] registrationParameters_selector({argumentsWithNames})" )
	@MethodSource( "registrationParameters_selector" )
	void registrationParameters_selector( String methodName, Object[] arguments, ListenerSelector expectedSelector ) {
		Method method = findMethod( methodName );
		RegistrationParameters registrationParameters = detector.getListenerParameter( method );
		assertThat( registrationParameters.createSelector( method, arguments ) ).isEqualTo( expectedSelector );
	}

	@SuppressWarnings( "unused" ) // used as @MethodSource for registrationParameters_selector
	private static Stream<Arguments> registrationParameters_selector() {
		return Stream.of(
			arguments( "onlyListener", new Object[]{ mock( MyListener.class ) }, selector() ),
			arguments( "stringAndListener", new Object[]{ "name", mock( MyListener.class ) }, selector( "name" ) ),
			arguments( "manyParametersAndListener", new Object[]{ "amen", 1d, mock( MyListener.class ), 3 }, selector( "amen", 1d, 3 ) )
		);
	}


	// ==================================================================================
	// =================================== helpers ======================================
	// ==================================================================================

	private static Method findMethod( String name ) {
		return Arrays.stream( Methods.class.getDeclaredMethods() ).filter( m -> m.getName().equals( name ) ).findFirst().orElse( null );
	}
}
