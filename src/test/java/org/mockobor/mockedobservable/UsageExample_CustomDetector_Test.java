package org.mockobor.mockedobservable;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockobor.Mockobor;
import org.mockobor.MockoborContext;
import org.mockobor.listener_detectors.AbstractDetector;
import org.mockobor.listener_detectors.NotificationMethodDelegate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


class UsageExample_CustomDetector_Test {

	/** Custom listener interface used by observer to receive notifications from {@link MyObservable}. */
	public interface MyObserver {
		void update( Object param );
	}

	/** Class of observable object. It is mocked in this test, because we don't need to have a class, interface is enough. */
	public interface MyObservable {
		void addMyObservable( MyObserver observable );
	}


	/**
	 * Additional interface, which should implement notifier created by {@link Mockobor#createNotifierFor}.
	 * <p></p>
	 * For test purpose it overrides<ul>
	 * <li>two methods of ListenersNotifier (numberOfRegisteredListeners and numberOfListenerRegistrations),</li>
	 * <li>adds one method with default implementation (notifyMyObserver(String)),</li>
	 * <li>adds one method without implementation (notifyMyObserver()) (implementation provided over custom notification delegate).</li>
	 * </ul>
	 * The method numberOfListenerDeregistrations() is overridden, but does not change default behavior.
	 */
	public interface AdditionalInterface extends ListenersNotifier {

		@Override
		default int numberOfRegisteredListeners() {
			return -10; // something, that can not be returned from default implementation 
		}

		@Override
		default int numberOfListenerRegistrations() {
			return -10; // something, that can not be returned from default implementation
		}

		@Override
		int numberOfListenerDeregistrations(); // override, but don't change behavior => original method must be invoked

		default void notifyMyObserver( String param ) {
			notifierFor( MyObserver.class ).update( param );
		}

		void notifyMyObserver();
	}


	/**
	 * Custom detector.
	 * <p></p>
	 * It detects {@link MyObserver} as listener and {@link MyObservable#addMyObservable} as registration method,
	 * sets custom notification delegates and adds additional interface ({@link AdditionalInterface}).
	 * <p></p>
	 * With {@link #getCustomNotificationMethodDelegates()} it registers
	 * required delegate for method {@link AdditionalInterface#notifyMyObserver()} (because it has no implementation)
	 * and delegate for overridden method {@link AdditionalInterface#numberOfListenerRegistrations} for test purpose.
	 */
	public static class CustomDetector extends AbstractDetector {

		@Override
		protected boolean isListenerClass( @NonNull Class<?> parameterType, @NonNull Method method ) {
			return parameterType.equals( MyObserver.class );
		}

		@Override
		protected boolean isAddMethods( @NonNull Method method ) {
			return method.getName().equals( "addMyObservable" );
		}

		@Override
		protected boolean isRemoveMethods( @NonNull Method method ) { // remove methods are not necessary
			return false;
		}

		@SneakyThrows
		@Override
		protected @NonNull List<NotificationMethodDelegate> getCustomNotificationMethodDelegates() {
			return Arrays.asList(
					// overridden implementation of numberOfListenerRegistrations()
					new NotificationMethodDelegate(
							ListenersNotifier.class.getMethod( "numberOfListenerRegistrations" ),
							( listenersNotifier, method, arguments ) -> -100 ), // something other, that can not be returned from other implementations
					// required implementation of notifyMyObserver()
					new NotificationMethodDelegate(
							AdditionalInterface.class.getMethod( "notifyMyObserver" ),
							( listenersNotifier, method, arguments ) -> {
								listenersNotifier.notifierFor( MyObserver.class ).update( null );
								return null;
							}
					)
			);
		}

		@Override
		protected @NonNull List<Class<?>> getAdditionalInterfaces() {
			return Collections.singletonList( AdditionalInterface.class );
		}
	}


	/** The class under test, which should observes a mocked {@link MyObservable}. */
	public static class TestedObject implements MyObserver {

		@Getter
		private final List<Object> invocations = new ArrayList<>();

		public TestedObject( MyObservable observable ) {
			observable.addMyObservable( this );
		}

		@Override
		public void update( Object param ) {
			invocations.add( param );
		}
	}


	// ==================================================================================
	// ==================================== tests =======================================
	// ==================================================================================

	private final MyObservable      mockedObservable = mock( MyObservable.class );
	private final ListenersNotifier notifier         = Mockobor.createNotifierFor( mockedObservable );
	private final TestedObject      testedObject     = new TestedObject( mockedObservable );


	@BeforeAll
	static void beforeAll() {
		// register custom listener detector
		MockoborContext.registerListenerDefinitionDetector( new CustomDetector() );
	}

	@AfterAll
	static void afterAll() {
		// reset context to avoid correlation with other tests
		MockoborContext.reset();
	}

	@Test
	void customListenerDefinitionDetector_notification() {
		// send events using additional interface
		AdditionalInterface additionalNotifier = (AdditionalInterface) this.notifier;
		additionalNotifier.notifyMyObserver( "v1" ); // over default implementation in interface
		additionalNotifier.notifyMyObserver(); // null, over custom notification delegate

		// send events using direct ListenerNotifier
		this.notifier.notifierFor( MyObserver.class ).update( "v2" );
		( (MyObserver) this.notifier ).update( 33 );

		// check events
		assertThat( testedObject.getInvocations() ).containsExactly( "v1", null, "v2", 33 );
	}


	@Test
	void customListenerDefinitionDetector_overridden_methods_take_precedence_over_default_implementation() {

		assertThat( notifier.numberOfListenerRegistrations() )
				.as( "custom notification delegate before all" )
				.isEqualTo( -100 );

		assertThat( notifier.numberOfRegisteredListeners() )
				.as( "default methods of AdditionalInterface before implementation of ListenersNotifier" )
				.isEqualTo( -10 );

		assertThat( notifier.numberOfListenerDeregistrations() )
				.as( "is overridden in AdditionalInterface, but does not change default behavior" )
				.isZero();
	}
}
