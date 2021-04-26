package org.mockobor.mockedobservable;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mockobor.exceptions.ListenersNotFoundException;
import org.mockobor.listener_detectors.ListenerContainer;
import org.mockobor.listener_detectors.ListenerSelector;
import org.mockobor.utils.reflection.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * To manage listeners registration and find and send notifications to required listener.
 * <p>
 * It used in "runtime" in background to store registered listeners and send notifications to these listeners.
 */
@RequiredArgsConstructor
public class ListenersManager implements ListenerContainer, ListenersNotifier {

	/**
	 * Observable mock used to create this notifier
	 * (mockedObservable passed to the {@code createNotifierFor( mockedObservable )}).
	 * Ordinarily it used as source by event generation.
	 */
	@NonNull
	private final Object observable;

	private boolean strictCheckListenerList = true;

	private int registeredListenersCount;

	private int registrationsCount;

	private int deregistrationsCount;


	/** listener key (selector + class) -> list of listeners. */
	private final Map<ListenerKey<?>, List<Object>> listeners = new ConcurrentHashMap<>();


	@Override
	@NonNull
	public Object getObservableMock() {
		return observable;
	}


	// ==================================================================================
	// ============================== ListenerContainer =================================
	// ==================================================================================

	@Override
	public <L> void addListener( @NonNull ListenerSelector selector, @NonNull Class<L> listenerClass, @NonNull L listener ) {
		ListenerKey<L> key = new ListenerKey<>( listenerClass, selector );
		listeners.computeIfAbsent( key, k -> new ArrayList<>() ).add( listener );
		++registrationsCount;
		++registeredListenersCount;
	}

	@Override
	public <L> void removeListener( @NonNull ListenerSelector selector, @NonNull Class<L> listenerClass, @NonNull L listener ) {
		ListenerKey<L> key = new ListenerKey<>( listenerClass, selector );
		listeners.computeIfPresent( key, ( k, list ) -> {
			if( list.remove( listener ) ) {
				++deregistrationsCount;
				--registeredListenersCount;
			}
			return list.isEmpty() ? null : list;
		} );
	}


	// ==================================================================================
	// ============================== ListenersNotifier =================================
	// ==================================================================================

	@Override
	@NonNull
	public <L> L notifierFor( @NonNull Class<L> listenerClass, @NonNull ListenerSelector... selectors ) {
		Collection<L> listenersToNotify = getListeners( listenerClass, selectors );
		if( strictCheckListenerList && listenersToNotify.isEmpty() ) throw new ListenersNotFoundException( listenerClass, selectors );

		return listenerClass.cast( Proxy.newProxyInstance(
				getClass().getClassLoader(),
				new Class[]{ listenerClass },
				( ( proxy, method, args ) -> sendNotifications( listenersToNotify, method, args ) ) ) );
	}

	@SneakyThrows
	private static <L> Object sendNotifications( @NonNull Collection<L> listenersToNotify, @NonNull Method method, @NonNull Object[] args ) {
		Object result = null;
		for( L listener : listenersToNotify ) {
			result = method.invoke( listener, args );
		}
		return result != null ? result : TypeUtils.getDefaultReturnValue( method.getReturnType() );
	}


	@Override
	public void setStrictCheckListenerList( boolean strict ) {
		strictCheckListenerList = strict;
	}

	@Override
	public int numberOfRegisteredListeners() {
		return registeredListenersCount;
	}

	@Override
	public int numberOfListenerRegistrations() {
		return registrationsCount;
	}

	@Override
	public int numberOfListenerDeregistrations() {
		return deregistrationsCount;
	}

	@Override
	@NonNull
	public Collection<Object> getAllListeners() {
		return listeners.values().stream().flatMap( List::stream ).collect( Collectors.toList() );
	}

	@Override
	public @NonNull <L> Collection<L> getListeners( @NonNull Class<L> listenerClass ) {
		return listeners.entrySet().stream()
		                .filter( entry -> Objects.equals( entry.getKey().getListenerClass(), listenerClass ) )
		                .flatMap( entry -> entry.getValue().stream() )
		                .map( listenerClass::cast )
		                .collect( Collectors.toList() );
	}

	@Override
	@NonNull
	public <L> Collection<L> getListeners( Class<L> listenerClass, ListenerSelector... selectors ) {
		Set<ListenerKey<L>> requiredKeys = Arrays.stream( selectors )
		                                         .map( s -> new ListenerKey<>( listenerClass, s ) )
		                                         .collect( Collectors.toSet() );
		return listeners.entrySet().stream()
		                .filter( entry -> requiredKeys.contains( entry.getKey() ) )
		                .flatMap( entry -> entry.getValue().stream() )
		                .map( listenerClass::cast )
		                .collect( Collectors.toList() );
	}

	@Override
	public @NonNull Collection<ListenerKey<?>> getListenersWithSelector() {
		return Collections.unmodifiableCollection( listeners.keySet() );
	}
}
