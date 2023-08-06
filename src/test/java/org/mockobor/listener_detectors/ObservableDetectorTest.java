package org.mockobor.listener_detectors;

import org.junit.jupiter.api.Test;
import org.mockobor.mockedobservable.MockedObservable;
import org.mockobor.mockedobservable.ObservableNotifier;
import org.mockobor.utils.reflection.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Observer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


@SuppressWarnings( { "unused", "deprecation" } )
class ObservableDetectorTest {

	@Test
	void detect() {

		MockedObservable testObservable = mock( MockedObservable.class );
		Collection<Method> allMethods = ReflectionUtils.getReachableMethods( testObservable );

		ListenerDefinition listenerDefinition = new ObservableDetector().detect( allMethods );

		assertThat( listenerDefinition.hasListenerDetected() ).isTrue();

		assertThat( listenerDefinition.getRegistrations() )
				.as( "expected registration methods" )
				.extracting( RegistrationDelegate::getSource )
				.extracting( Method::getName )
				.containsExactlyInAnyOrder( "addObserver", "deleteObserver" );

		assertThat( listenerDefinition.getDetectedListeners() )
				.as( "detected listener" )
				.containsExactly( Observer.class );

		assertThat( listenerDefinition.getAdditionalInterfaces() )
				.as( "expected interfaces" )
				.containsExactly( ObservableNotifier.class );

		// no additional notifications expected because all methods of {@link ObservableNotifier} have the default implementation
		assertThat( listenerDefinition.getCustomNotificationMethodDelegates() ).isEmpty();
	}
}
