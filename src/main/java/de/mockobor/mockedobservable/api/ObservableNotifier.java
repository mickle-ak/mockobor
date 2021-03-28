package de.mockobor.mockedobservable.api;

import java.util.Observable;
import java.util.Observer;


/**
 * Interface to simulate notification calls from mocked {@link Observable}.
 */
@SuppressWarnings( "unused" )
public interface ObservableNotifier extends ListenerNotifier {

	/**
	 * To simulate call of {@link Observer#update} from {@link Observable#notifyObservers()}.
	 * <p>
	 * It ignores <c>Observable.changed</c> flag and calls <c>Observer.update(observable, null)</c> one time for each calls of <c>notifyObservers</c>.
	 * <p>
	 * It uses correct observable object as first parameter of {@link Observer#update(Observable, Object)}.
	 *
	 * @see Observable#notifyObservers()
	 */
	void notifyObservers();


	/**
	 * To simulate call of {@link Observer#update} from {@link Observable#notifyObservers(Object)}.
	 * <p>
	 * It ignores <c>Observable.changed</c> flag and calls <c>Observer.update(observable, updateParameter)</c>
	 * one time for each calls of <c>notifyObservers(updateParameter)</c>.
	 * <p>
	 * It uses correct observable object as first parameter of {@link Observer#update(Observable, Object)}.
	 *
	 * @param updateParameter any object used as parameter by call of {@link Observer#update(Observable, Object)}.
	 * @see Observable#notifyObservers(Object)
	 */
	void notifyObservers( Object updateParameter );


	/**
	 * Returns the number of observers of the mocked {@link Observable}.
	 *
	 * @return the number of observers of the mocked {@link Observable}.
	 * @see Observable#countObservers()
	 */
	default int countObservers() {
		return numberOfRegisteredListeners();
	}
}
