package org.mockobor.mockedobservable;

import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockobor.Mockobor;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings( "unused" )
@ExtendWith( MockitoExtension.class )
class UsageExample_MockitoAnnotation_Test {

	public interface MyListener {
		void onChange( Object value );
	}

	public interface MockedObservable {
		void addListener( MyListener listener );
	}

	private static class AnnotationsTestObject implements MyListener {
		@Getter
		private final List<Object> invocations = new ArrayList<>();

		@Override
		public void onChange( Object value ) {
			invocations.add( value );
		}

		public AnnotationsTestObject( MockedObservable mockedObservable ) {
			mockedObservable.addListener( this );
		}
	}

	@Mock
	private MockedObservable mockedObservable;

	@InjectMocks
	private AnnotationsTestObject testedObserver;

	private ListenersNotifier notifier;

	@BeforeEach
	void setUp() {
		notifier = Mockobor.createNotifierFor( mockedObservable );
	}

	@Test
	void test_notifications() {
		notifier.notifierFor( MyListener.class ).onChange( 1f );

		assertThat( testedObserver.getInvocations() ).containsExactly( 1f );
	}
}
