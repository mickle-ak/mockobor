package org.mockobor.mockedobservable;


import java.beans.PropertyChangeListener;
import java.util.Observer;


/** Test-interface to simulate mocked object with ability to register listeners/observers. */
@SuppressWarnings( "unused" )
public interface MockedObservable {


	// ==================================================================================
	// =========================== typical java listeners ===============================
	// ==================================================================================

	interface MyListener {
		void somethingChanged1( Object somethingNewValue );
		int somethingChanged2( Object somethingNewValue );
	}

	interface MyAnotherListener {
		void somethingOtherChanged( Object somethingOtherValue );
	}


	void addMyListener( MyListener listener );

	void addMyListener( String preSelector, MyListener listener, String postSelector );

	void addMyAnotherListener( MyAnotherListener listener );

	void addTwoListeners( MyListener myListener, MyAnotherListener myAnotherListener );

	void removeMyListener( MyListener listener );

	void removeMyListener( String preSelector, MyListener listener, String postSelector );

	void removeMyAnotherListener( MyAnotherListener listener );

	void removeTwoListeners( MyListener myListener, MyAnotherListener myAnotherListener );


	// ==================================================================================
	// =========================== property change support ==============================
	// ==================================================================================

	void addPropertyChangeListener( PropertyChangeListener listener );

	void addPropertyChangeListener( String propertyName, PropertyChangeListener listener );

	void removePropertyChangeListener( PropertyChangeListener listener );

	void removePropertyChangeListener( String propertyName, PropertyChangeListener listener );


	// ==================================================================================
	// ================================== Observer ======================================
	// ==================================================================================

	void addObserver( Observer o );

	void deleteObserver( Observer o );
}
