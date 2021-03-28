package de.mockobor.mockedobservable.api;

import de.mockobor.mockedobservable.core.ListenerDefinitionDetector;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Observable;
import java.util.Observer;


/**
 * Static facade for mock library independent functionality of Mockobor library.
 */
@SuppressWarnings( "unused" )
public class Mockobor {

	/**
	 * To add custom listener definition detector.
	 * <p>
	 * A new registered detector have priority over previously registered detectors (inclusive detectors registered per default).
	 * <p>
	 * Out of the box (per default) follow detectors are registered:<ol>
	 * <li>XXX for {@link Observer}/{@link Observable}. Detected per methods:<ul>
	 * <li><c>addObserver({@link Observer})</c></li>
	 * <li><c>deleteObserver({@link Observer})</c></li>
	 * </ul>
	 * It returns an instance of {@link ObservableNotifier} as listener notifier.
	 * </li>
	 * <li>XXX for beans that support bound properties (see {@link PropertyChangeSupport}). Detected per methods:<ul>
	 * <li><c>addPropertyChangeListener({@link PropertyChangeListener})</c></li>
	 * <li><c>addPropertyChangeListener(String, {@link PropertyChangeListener})</c></li>
	 * <li><c>removePropertyChangeListener({@link PropertyChangeListener})</c></li>
	 * <li><c>removePropertyChangeListener(String, {@link PropertyChangeListener})</c></li>
	 * </ul>
	 * It returns an instance of {@link PropertyChangeNotifier} as listener notifier.
	 * </li>
	 * <li>XXX for typical Java listener. Detected per methods:<ul>
	 * <li><c>addXxxListener(XxxListener)</c></li>
	 * <li><c>removeXxxListener(XxxListener)</c></li>
	 * </ul>
	 * It returns an instance of XxxListener as listener notifier.
	 * </li>
	 * </ol>
	 *
	 * @param listenerDefinitionDetector custom listener definition detector
	 */
	public static void addListenerDefinitionDetector( ListenerDefinitionDetector listenerDefinitionDetector ) {
		throw new UnsupportedOperationException( "Not implemented yet." );
	}
}
