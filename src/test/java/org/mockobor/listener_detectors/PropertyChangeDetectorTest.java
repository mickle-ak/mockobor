package org.mockobor.listener_detectors;

import org.junit.jupiter.api.Test;
import org.mockobor.mockedobservable.MockedObservable;
import org.mockobor.mockedobservable.PropertyChangeNotifier;
import org.mockobor.utils.reflection.ReflectionUtils;

import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


class PropertyChangeDetectorTest {

	@Test
	void detect() {

		MockedObservable testObservable = mock( MockedObservable.class );
		Collection<Method> allMethods = ReflectionUtils.getReachableMethods( testObservable );

		ListenersDefinition listenersDefinition = new PropertyChangeDetector().detect( allMethods );

		assertThat( listenersDefinition.hasListenerDetected() ).isTrue();

		assertThat( listenersDefinition.getRegistrations() )
			.as( "expected registration methods" )
			.extracting( RegistrationDelegate::getSource )
			.extracting( Method::getName )
			.containsExactlyInAnyOrder( "addPropertyChangeListener", "addPropertyChangeListener",
			                            "removePropertyChangeListener", "removePropertyChangeListener" );

		assertThat( listenersDefinition.getDetectedListeners() )
			.as( "detected listener" )
			.containsExactly( PropertyChangeListener.class );

		assertThat( listenersDefinition.getAdditionalInterfaces() )
			.as( "expected interfaces" )
			.containsExactly( PropertyChangeNotifier.class );

		// no additional notifications expected
		// because all methods of {@link PropertyChangeNotifier} have the default implementation
		assertThat( listenersDefinition.getCustomNotificationMethodDelegates() ).isEmpty();
	}
}
