package org.mockobor.mockedobservable;

import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockobor.exceptions.ListenersNotFoundException;
import org.mockobor.listener_detectors.ListenerDetectorsRegistryImpl;
import org.mockobor.mockedobservable.mocking_tools.MockingToolsRegistryImpl;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockobor.listener_detectors.ListenerSelector.selector;


class NotifierFactory_ListenerRegisteredBeforeNotifierCreation_Mockito_Test {

	public interface MyListener {
		void onChange( Object value );
	}

	public interface MockedObservable {
		void addListener( MyListener listener );
		void removeListener( MyListener listener );
		void addVarargListener( MyListener listener, Object... selector );
		void removeVarargListener( MyListener listener, Object... selector );
	}

	private abstract static class BaseTestObject implements MyListener {
		@Getter private final List<Object> invocations = new ArrayList<>();
		@Override
		public void onChange( Object value ) {
			invocations.add( value );
		}
	}

	private static class OnlyAddTestObject extends BaseTestObject {
		public OnlyAddTestObject( MockedObservable mockedObservable ) {
			mockedObservable.addListener( this );
		}
	}

	private static class AddThenRemoveTestObject extends BaseTestObject {
		public AddThenRemoveTestObject( MockedObservable mockedObservable ) {
			mockedObservable.addListener( this );
			mockedObservable.removeListener( this );
		}
	}

	private static class RemoveThenAddTestObject extends BaseTestObject {
		public RemoveThenAddTestObject( MockedObservable mockedObservable ) {
			mockedObservable.removeListener( this );
			mockedObservable.addListener( this );
		}
	}

	private static class RemoveThenAddVarargTestObject extends BaseTestObject {
		public RemoveThenAddVarargTestObject( MockedObservable mockedObservable ) {
			mockedObservable.removeVarargListener( this, "s1", "s2", "s3" );
			mockedObservable.addVarargListener( this, "s1", "s2", "s3" );
		}
	}


	// ==================================================================================
	// ==================================== tests =======================================
	// ==================================================================================

	private final NotifierSettings defaultSettings = NotifierSettingsImpl.createDefaultSettings();

	private final NotifierFactory factory = new NotifierFactory( new ListenerDetectorsRegistryImpl(),
	                                                             new MockingToolsRegistryImpl() );

	private final MockedObservable mockedObservable = Mockito.mock( MockedObservable.class );


	@Test
	void add_before_create_notifier() {
		BaseTestObject createdBeforeNotifier = new OnlyAddTestObject( mockedObservable );
		ListenersNotifier notifier = factory.create( mockedObservable, defaultSettings );
		BaseTestObject createdAfterNotifier = new OnlyAddTestObject( mockedObservable );

		// should send notification to both test objects
		notifier.notifierFor( MyListener.class ).onChange( "new-value" );

		// check that both listeners receive notification
		assertAll(
				() -> assertThat( createdBeforeNotifier.getInvocations() ).as( "in before creation" ).containsExactly( "new-value" ),
				() -> assertThat( createdAfterNotifier.getInvocations() ).as( "in after creation" ).containsExactly( "new-value" )
		);
	}

	@Test
	void removeThenAdd_before_create_notifier() {
		BaseTestObject createdBeforeNotifier = new RemoveThenAddTestObject( mockedObservable );
		ListenersNotifier notifier = factory.create( mockedObservable, defaultSettings );
		BaseTestObject createdAfterNotifier = new RemoveThenAddTestObject( mockedObservable );

		// should send notification to both test objects
		notifier.notifierFor( MyListener.class ).onChange( "new-value" );

		// check that both listeners receive notification
		assertAll(
				() -> assertThat( createdBeforeNotifier.getInvocations() ).as( "in before creation" ).containsExactly( "new-value" ),
				() -> assertThat( createdAfterNotifier.getInvocations() ).as( "in after creation" ).containsExactly( "new-value" )
		);
	}

	@SuppressWarnings( "unused" )
	@Test
	void addThenRemove_before_create_notifier() {
		BaseTestObject createdBeforeNotifier = new AddThenRemoveTestObject( mockedObservable );
		ListenersNotifier notifier = factory.create( mockedObservable, defaultSettings );
		BaseTestObject createdAfterNotifier = new AddThenRemoveTestObject( mockedObservable );

		assertThatThrownBy( () -> notifier.notifierFor( MyListener.class ) )
				.as( "both listener removed immediately after add" )
				.isInstanceOf( ListenersNotFoundException.class );
	}

	@Test
	void varargRegistrationMethods() {
		BaseTestObject createdBeforeNotifier = new RemoveThenAddVarargTestObject( mockedObservable );
		ListenersNotifier notifier = factory.create( mockedObservable, defaultSettings );
		BaseTestObject createdAfterNotifier = new RemoveThenAddVarargTestObject( mockedObservable );

		// should send notification to both test objects
		notifier.notifierFor( MyListener.class, selector( "s1", "s2", "s3" ) ).onChange( "new-value" );

		// check that both listeners receive notification
		assertAll(
				() -> assertThat( createdBeforeNotifier.getInvocations() ).as( "in before creation" ).containsExactly( "new-value" ),
				() -> assertThat( createdAfterNotifier.getInvocations() ).as( "in after creation" ).containsExactly( "new-value" )
		);
	}
}
