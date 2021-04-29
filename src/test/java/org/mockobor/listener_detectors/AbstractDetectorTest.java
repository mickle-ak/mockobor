package org.mockobor.listener_detectors;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockobor.exceptions.MockoborException;
import org.mockobor.exceptions.MockoborImplementationError;
import org.mockobor.listener_detectors.AbstractDetector.ListenerRegistrationParameters;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockobor.listener_detectors.ListenerSelector.selector;


class AbstractDetectorTest {

	private final AbstractDetector detector = new AbstractDetector() {

		@Override
		protected boolean isListenerClass( @NonNull Class<?> parameterType, @NonNull Method method ) {
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

		void twoListeners( int s1, MyListener listener, int s2, MyAnotherListener anotherListener, int s3 );

		void addVarargListener( MyListener listener, int... selectors );
	}

	interface OnlyRemoveMethod {
		@SuppressWarnings( "unused" )
		void removeMyListener( MyListener listener );
	}

	interface OnlyAddMethod {
		@SuppressWarnings( "unused" )
		void addMyListener( MyListener listener );
	}

	/** Invalid listener, because has a class type. Listeners can be only interfaces. */
	static abstract class InvalidListener {
		@SuppressWarnings( "unused" )
		abstract void onEvent( Object event );
	}

	interface InvalidRegistrationMethods {

		@SuppressWarnings( "unused" )
		void addInvalidListener( InvalidListener listener );

		@SuppressWarnings( "unused" )
		void addCorrectListener( MyListener listener );

		@SuppressWarnings( "unused" )
		void addArrayOfListeners( MyListener[] listeners );

		@SuppressWarnings( "unused" )
		void addVarargsOfListeners( MyListener... listeners );
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
				.as( "12 add/removeXxxListener methods in MockedObservable" )
				.hasSize( 12 );
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


	@Test
	void detect_only_correct_listeners() {
		InvalidRegistrationMethods testObservable = mock( InvalidRegistrationMethods.class );
		Collection<Method> allMethods = ReflectionUtils.getReachableMethods( testObservable );

		ListenersDefinition listenersDefinition = new TypicalJavaListenerDetector().detect( allMethods );

		assertThat( listenersDefinition.hasListenerDetected() ).isTrue();

		assertThat( listenersDefinition.getRegistrations() )
				.as( "expected registration methods" )
				.extracting( RegistrationDelegate::getSource )
				.extracting( Method::getName )
				.containsExactly( "addCorrectListener" );

		assertThat( listenersDefinition.getDetectedListeners() )
				.as( "detected listener" )
				.containsExactly( MyListener.class );
	}


	// ==================================================================================
	// ============================= hasListenerParameter ===============================
	// ==================================================================================

	@ParameterizedTest( name = "[{index}] hasListenerParameter_listener_found({argumentsWithNames})" )
	@MethodSource( "hasListenerParameter_listener_found" )
	void hasListenerParameter_listener_found( String methodName, Integer[] expectedListenerIndexes, Integer[] expectedSelectorIndexes ) {
		ListenerRegistrationParameters registrationParameters = detector.getListenerRegistrationParameter( findMethod( methodName ) );
		assertThat( registrationParameters ).isNotNull();
		assertThat( registrationParameters.getListenerIndexes() ).containsExactly( expectedListenerIndexes );
		assertThat( registrationParameters.getSelectorIndexes() ).containsExactly( expectedSelectorIndexes );
	}

	@SuppressWarnings( "unused" ) // used as @MethodSource for hasListenerParameter_listener_found
	private static Stream<Arguments> hasListenerParameter_listener_found() {
		return Stream.of(
				arguments( "onlyListener", new Integer[]{ 0 }, new Integer[0] ),
				arguments( "stringAndListener", new Integer[]{ 1 }, new Integer[]{ 0 } ),
				arguments( "manyParametersAndListener", new Integer[]{ 2 }, new Integer[]{ 0, 1, 3 } ),
				arguments( "twoListeners", new Integer[]{ 1, 3 }, new Integer[]{ 0, 2, 4 } ),
				arguments( "addVarargListener", new Integer[]{ 0 }, new Integer[]{ 1 } ),
				arguments( "addVarargListener", new Integer[]{ 0 }, new Integer[]{ 1 } ),
				arguments( "addVarargListener", new Integer[]{ 0 }, new Integer[]{ 1 } )
		);
	}

	@Test
	void hasListenerParameter_listener_not_found() {
		ListenerRegistrationParameters registrationParameters = detector.getListenerRegistrationParameter( findMethod( "noListeners" ) );
		assertThat( registrationParameters ).isNull();
	}


	// ==================================================================================
	// =========================== RegistrationParameters ===============================
	// ==================================================================================

	@SuppressWarnings( "ConstantConditions" )
	@ParameterizedTest( name = "[{index}] registrationParameters_selector({argumentsWithNames})" )
	@MethodSource( "registrationParameters_selector" )
	void registrationParameters_selector( String methodName, Object[] arguments, ListenerSelector expectedSelector ) {
		Method method = findMethod( methodName );
		ListenerRegistrationParameters registrationParameters = detector.getListenerRegistrationParameter( method );
		assertThat( registrationParameters.createSelector( method, arguments ) ).isEqualTo( expectedSelector );
	}

	@SuppressWarnings( "unused" ) // used as @MethodSource for registrationParameters_selector
	private static Stream<Arguments> registrationParameters_selector() {
		return Stream.of(
				arguments( "onlyListener", new Object[]{ mock( MyListener.class ) }, selector() ),
				arguments( "stringAndListener", new Object[]{ "name", mock( MyListener.class ) }, selector( "name" ) ),
				arguments( "manyParametersAndListener", new Object[]{ "amen", 1d, mock( MyListener.class ), 3 }, selector( "amen", 1d, 3 ) ),
				arguments( "twoListeners", new Object[]{ 1, mock( MyListener.class ), 2, mock( MyAnotherListener.class ), 3 }, selector( 1, 2, 3 ) ),
				arguments( "addVarargListener", new Object[]{ mock( MyListener.class ) }, selector() ),
				arguments( "addVarargListener", new Object[]{ mock( MyListener.class ), 1 }, selector( 1 ) ),
				arguments( "addVarargListener", new Object[]{ mock( MyListener.class ), 1, 2 }, selector( 1, 2 ) )
		);
	}

	@SuppressWarnings( "ConstantConditions" )
	@Test
	void registrationParameters_selector_errors_unexpectedMethod() {
		Method sourceMethod = findMethod( "stringAndListener" );
		ListenerRegistrationParameters registrationParameters = detector.getListenerRegistrationParameter( sourceMethod );

		Method realInvokedMethod = findMethod( "noListeners" );
		assertThatThrownBy( () -> registrationParameters.createSelector( realInvokedMethod, new Object[2] ) )
				.isInstanceOf( MockoborImplementationError.class )
				.hasMessageContainingAll( "unexpected method", "stringAndListener", "noListeners" );
	}

	@SuppressWarnings( "ConstantConditions" )
	@Test
	void registrationParameters_selector_errors_tooLittleArguments() {
		Method method = findMethod( "stringAndListener" );
		ListenerRegistrationParameters registrationParameters = detector.getListenerRegistrationParameter( method );

		assertThatThrownBy( () -> registrationParameters.createSelector( method, new Object[]{ mock( MyListener.class ) } ) )
				.isInstanceOf( MockoborImplementationError.class )
				.hasMessageContainingAll( "unexpected number of parameters", "stringAndListener" );
	}

	@SuppressWarnings( "ConstantConditions" )
	@Test
	void registrationParameters_selector_errors_tooMuchArguments() {
		Method method = findMethod( "stringAndListener" );
		ListenerRegistrationParameters registrationParameters = detector.getListenerRegistrationParameter( method );

		assertThatThrownBy( () -> registrationParameters.createSelector( method, new Object[]{ "s1", mock( MyListener.class ), "s2" } ) )
				.isInstanceOf( MockoborImplementationError.class )
				.hasMessageContainingAll( "unexpected number of parameters", "stringAndListener" );
	}

	@SuppressWarnings( "ConstantConditions" )
	@Test
	void registrationParameters_selector_errors_tooLittleArgumentsVararg() {
		Method method = findMethod( "addVarargListener" );
		ListenerRegistrationParameters registrationParameters = detector.getListenerRegistrationParameter( method );

		assertThatThrownBy( () -> registrationParameters.createSelector( method, new Object[0] ) )
				.isInstanceOf( MockoborImplementationError.class )
				.hasMessageContainingAll( "unexpected number of parameters", "vararg method", "addVarargListener",
				                          "expected>=1", "was: 0" );
	}


	// ==================================================================================
	// =================================== helpers ======================================
	// ==================================================================================

	private static Method findMethod( String name ) {
		Class<Methods> methodsClass = Methods.class;
		return Arrays.stream( methodsClass.getDeclaredMethods() )
		             .filter( m -> m.getName().equals( name ) )
		             .findFirst().orElseThrow( () -> new MockoborException( "Method '%s' not found in '%s'", name, methodsClass.getName() ) );
	}
}
