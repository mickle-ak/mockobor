package org.mockobor.mockedobservable;

import org.eclipse.jdt.annotation.Nullable;
import org.mockobor.Mockobor;
import org.mockobor.exceptions.ListenersNotFoundException;

import java.util.Observable;
import java.util.Observer;


/**
 * Interface to simulate notification calls from mocked {@link Observable}.
 * <p></p>
 * Created from {@link Mockobor#createNotifierFor} if the specified observable object has method like:<br>
 * {@code void addObserver(Observer observer)}.
 * <p></p>
 * Example:
 *
 * <pre class="code"><code class="java">
 *
 * // create notifier for mocked Observable
 * Observable mockedObservable = mock( Observable.class )
 * ObservableNotifier notifier = (ObservableNotifier) Mockobor.createNotifierFor( mockedObservable );
 *
 * // tested object registers itself as listener by the specified Observable object
 * TestObject testObject = new TestObject( mockedObservable );
 *
 * // send events to testObject
 * notifier.notifyObservers( updateParameter );
 * notifier.notifyObservers();
 *
 * </code></pre>
 */
@SuppressWarnings( "unused" )
public interface ObservableNotifier extends ListenersNotifier {

	/**
	 * To simulate call of {@link Observer#update} from {@link Observable#notifyObservers()}.
	 * <p>
	 * It ignores {@code Observable.changed} flag and calls {@code Observer.update(observable, null)} one time
	 * for each calls of {@code notifyObservers}.
	 * <p>
	 * If mocked observable object is an instance of {@link Observable}, then it uses correct observable object
	 * as first parameter of {@link Observer#update(Observable, Object)}; otherwise it is null.
	 * <p>
	 * This method is equivalent to: {@code notifyObservers(null)}
	 *
	 * @see Observable#notifyObservers()
	 * @throws ListenersNotFoundException if no {@code Observer} registered by the mocked observable and
	 *                                    strictCheckListenerList-flag set to true (default)
	 * @see #notifyObservers(Object)
	 */
	default void notifyObservers() throws ListenersNotFoundException {
		notifyObservers( null );
	}


	/**
	 * To simulate call of {@link Observer#update} from {@link Observable#notifyObservers(Object)}.
	 * <p>
	 * It ignores {@code Observable.changed} flag and calls {@code Observer.update(observable, updateParameter)}
	 * one time for each calls of {@code notifyObservers(updateParameter)}.
	 * <p>
	 * If mocked observable object is an instance of {@link Observable}, then it uses correct observable object
	 * as first parameter of {@link Observer#update(Observable, Object)}; otherwise it is null.
	 *
	 * @param updateParameter any object used as parameter by call of {@link Observer#update(Observable, Object)}.
	 * @throws ListenersNotFoundException if no {@code Observer} registered by the mocked observable and
	 *                                    strictCheckListenerList-flag set to true (default)
	 * @see Observable#notifyObservers(Object)
	 */
	default void notifyObservers( @Nullable Object updateParameter )
			throws ListenersNotFoundException {
		Object observableMock = getObservableMock();
		notifierFor( Observer.class ).update( observableMock instanceof Observable ? (Observable) observableMock : null,
		                                      updateParameter );
	}


	/**
	 * Returns the number of observers of the mocked {@link Observable}.
	 *
	 * @return the number of observers of the mocked {@link Observable}.
	 * @see Observable#countObservers()
	 */
	default int countObservers() {
		return getListeners( Observer.class ).size();
	}
}
