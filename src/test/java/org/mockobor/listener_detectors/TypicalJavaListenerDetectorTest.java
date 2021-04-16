package org.mockobor.listener_detectors;

import org.junit.jupiter.api.Test;
import org.mockobor.mockedobservable.MockedObservable;
import org.mockobor.mockedobservable.MockedObservable.MyAnotherListener;
import org.mockobor.mockedobservable.MockedObservable.MyListener;
import org.mockobor.utils.reflection.ReflectionUtils;

import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


class TypicalJavaListenerDetectorTest {

	@Test
	void detect() {

		MockedObservable testObservable = mock( MockedObservable.class );
		Collection<Method> allMethods = ReflectionUtils.getReachableMethods( testObservable );

		ListenersDefinition listenersDefinition = new TypicalJavaListenerDetector().detect( allMethods );

		assertThat( listenersDefinition.hasListenerDetected() ).isTrue();

		assertThat( listenersDefinition.getRegistrations() )
			.as( "expected registration methods" )
			.extracting( RegistrationDelegate::getSource )
			.extracting( Method::getName )
			.containsExactlyInAnyOrder( "addPropertyChangeListener", "addPropertyChangeListener",
			                            "addMyListener", "addMyListener", "addMyAnotherListener",
			                            "removePropertyChangeListener", "removePropertyChangeListener",
			                            "removeMyListener", "removeMyListener", "removeMyAnotherListener" );

		assertThat( listenersDefinition.getDetectedListeners() )
			.as( "detected listener" )
			.containsExactlyInAnyOrder( PropertyChangeListener.class, MyListener.class, MyAnotherListener.class );

		assertThat( listenersDefinition.getAdditionalInterfaces() ).isEmpty();
		assertThat( listenersDefinition.getCustomNotificationMethodDelegates() ).isEmpty();
	}
}
