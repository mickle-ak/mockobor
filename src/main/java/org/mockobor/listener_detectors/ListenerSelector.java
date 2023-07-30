package org.mockobor.listener_detectors;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * It represents selectors (a set of values) used to register or found listener.
 * <p><br>
 * For example - the String-parameter (property name) in the methods
 * {@code PropertyChangeSupport.addPropertyChangeListener(String, PropertyChangeListener)} or
 * {@code PropertyChangeSupport.firePropertyChange(String, Object, Object)} is a selector.
 * <p><br>
 * If you need more values as selector, you can pass more values into {@link #selector(Object...)} factory method.
 * Example:
 * <pre class="code"><code class="java">
 *
 * public interface MockedObservable {
 *     void addListener( MyListener listener );
 *     void addListener( Long selectorValue, MyListener listener );
 *     void addListener( String selectorValue1, Object selectorValue2, MyListener listener );
 * }
 *
 * public class Observer {
 *     public Observer( MockedObservable mockedObservableObject ) {
 *
 *         // register listener without selector (empty selector - selector())
 *         mockedObservable.addListener( new MyListener() );
 *
 *         // register listener with one-value selector (one value selector - selector( 123L ))
 *         mockedObservable.addListener( 123L, new MyListener() );
 *
 *         // register listener with multi-value selector (multi value selector - selector( "selector-value1", "selector-value2" ))
 *         mockedObservable.addListener( "selector-value1", ""selector-value2", new MyListener() );
 *     }
 * }
 *
 * class ObserverTest {
 * ...
 *      // create notifier for mocked MockedObservable
 *      MockedObservable mockedObservableObject = mock( MockedObservable.class )
 *      ListenersNotifier notifier = Mockobor.createNotifierFor( mockedObservableObject );
 *
 *      ...
 *      // find listener registered without selector
 *      notifier.notifierFor( MyListener.class, selector() );  // the same as notifier.notifierFor( MyListener.class )
 *      ...
 *      // find listener registered with one-value selector
 *      notifier.notifierFor( MyListener.class, selector( 123L ) );  // the same as notifier.notifierFor( 123L, MyListener.class )
 *      ...
 *      // find listener registered with multi-value selector
 *      notifier.notifierFor( MyListener.class, selector( "selector-value1", ""selector-value2" ) );
 * ...
 * }
 * </code></pre>
 */
@EqualsAndHashCode
@RequiredArgsConstructor( access = AccessLevel.PRIVATE )
public final class ListenerSelector {

	private final Object[] objects;

	public static @NonNull ListenerSelector selector( @Nullable Object... objects ) {
		return new ListenerSelector( objects != null ? objects : new Object[]{ null } );
	}

	@Override
	public String toString() {
		return Arrays.stream( objects )
		             .map( Objects::toString )
		             .collect( Collectors.joining( ", ", "selector(", ")" ) );
	}
}
