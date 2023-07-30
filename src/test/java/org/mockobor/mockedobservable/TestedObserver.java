package org.mockobor.mockedobservable;

import lombok.Getter;
import lombok.Value;
import org.mockobor.mockedobservable.MockedObservable.MyAnotherListener;
import org.mockobor.mockedobservable.MockedObservable.MyListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;


/** Test-object to simulate SUT-object, which registers itself in constructor by provided {@link MockedObservable}. */
@SuppressWarnings("unused")
@Getter
public class TestedObserver {

	private final MockedObservable[]         mockedObservables;
	private final ObserverIml                observer               = new ObserverIml();
	private final PropertyChangeListenerImpl propertyChangeListener = new PropertyChangeListenerImpl();
	private final MyListenerImpl             myListener             = new MyListenerImpl();
	private final MyAnotherListenerImpl      myAnotherListener      = new MyAnotherListenerImpl();

	public static class MyListenerImpl implements MyListener {

		@Getter
		private final List<InvocationDef> invocations = new ArrayList<>();

		@Override
		public void somethingChanged1( Object somethingNewValue ) {
			invocations.add( new InvocationDef( MyListener.class, "somethingChanged1", null, somethingNewValue ) );
		}

		@Override
		public int somethingChanged2( Object somethingNewValue ) {
			invocations.add( new InvocationDef( MyListener.class, "somethingChanged2", null, somethingNewValue ) );
			return invocations.size();
		}
	}

	public static class MyAnotherListenerImpl implements MyAnotherListener {

		@Getter
		private final List<InvocationDef> invocations = new ArrayList<>();

		@Override
		public void somethingOtherChanged( Object somethingOtherValue ) {
			invocations.add( new InvocationDef( MyAnotherListener.class, "somethingOtherChanged", null, somethingOtherValue ) );
		}
	}

	@SuppressWarnings("deprecation")
	public static class ObserverIml implements Observer {

		@Getter
		private final List<InvocationDef> invocations = new ArrayList<>();

		@Override
		public void update( Observable o, Object arg ) {
			invocations.add( new InvocationDef( Observer.class, "update", o, arg ) );
		}
	}

	public static class PropertyChangeListenerImpl implements PropertyChangeListener {

		@Getter
		private final List<InvocationDef> invocations = new ArrayList<>();

		@Override
		public void propertyChange( PropertyChangeEvent evt ) {
			invocations.add( new InvocationDef( PropertyChangeListener.class, "propertyChange", evt.getSource(), evt ) );
		}
	}

	@Value
	public static class InvocationDef {
		Class<?> clazz;
		String   method;
		Object   source;
		Object   param;
	}

	public TestedObserver( MockedObservable... mockedObservables ) {
		this.mockedObservables = mockedObservables;
		Arrays.stream( mockedObservables ).forEach( mockedObservable -> {
			mockedObservable.addObserver( observer );
			mockedObservable.addPropertyChangeListener( propertyChangeListener );
			mockedObservable.addPropertyChangeListener( "prop", propertyChangeListener );
			mockedObservable.addMyListener( myListener );
			mockedObservable.addMyListener( "presel", myListener, "postsel" );
			mockedObservable.addMyAnotherListener( myAnotherListener );
			mockedObservable.addTwoListeners( myListener, myAnotherListener );
		} );
	}


	public void destroy() {
		Arrays.stream( mockedObservables ).forEach( mockedObservable -> {
			mockedObservable.deleteObserver( observer );
			mockedObservable.removePropertyChangeListener( propertyChangeListener );
			mockedObservable.removePropertyChangeListener( "prop", propertyChangeListener );
			mockedObservable.removeMyListener( myListener );
			mockedObservable.removeMyListener( "presel", myListener, "postsel" );
			mockedObservable.removeMyAnotherListener( myAnotherListener );
			mockedObservable.removeTwoListeners( myListener, myAnotherListener );
		} );
	}
}
