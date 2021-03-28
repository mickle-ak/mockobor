package de.mockobor.mockedobservable.api;


import lombok.NonNull;


/**
 * Static facade for mockito-dependent functionality of Mockobor library.
 */
@SuppressWarnings( "unused" )
public class Mockitobor extends Mockobor {

	/**
	 * To start observation of the specified mockito-mocked observable.
	 * <p>
	 * It founds registrations of possible observer/listeners and creates corresponding notifier.
	 * <p>
	 * The returned notifier implements interfaces according to the specified mocked observable.
	 * See {@link Mockobor#addListenerDefinitionDetector} for details.
	 *
	 * @param mockitoMockedObservable mockito-based mock of observable object
	 * @return notifier used to simulate notification calls from the specified mocked observable
	 */
	@NonNull
	public static <L extends ListenerNotifier> L startObservation( @NonNull Object mockitoMockedObservable ) {
		return null;
	}

}
