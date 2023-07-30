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

		ListenerDefinition listenerDefinition = new TypicalJavaListenerDetector().detect( allMethods );

		assertThat( listenerDefinition.hasListenerDetected() ).isTrue();

		assertThat( listenerDefinition.getRegistrations() )
				.as( "expected registration methods" )
				.extracting( RegistrationDelegate::getSource )
				.extracting( Method::getName )
				.containsExactlyInAnyOrder( "addPropertyChangeListener", "addPropertyChangeListener",
				                            "addMyListener", "addMyListener", "addMyAnotherListener",
				                            "removePropertyChangeListener", "removePropertyChangeListener",
				                            "removeMyListener", "removeMyListener", "removeMyAnotherListener",
				                            "addTwoListeners", "removeTwoListeners" );

		assertThat( listenerDefinition.getDetectedListeners() )
				.as( "detected listener" )
				.containsExactlyInAnyOrder( PropertyChangeListener.class, MyListener.class, MyAnotherListener.class );

		assertThat( listenerDefinition.getAdditionalInterfaces() ).isEmpty();
		assertThat( listenerDefinition.getCustomNotificationMethodDelegates() ).isEmpty();
	}
}
