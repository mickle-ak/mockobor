package org.mockobor.listener_detectors;

import org.eclipse.jdt.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Implementation of {@link ListenerDetectorsRegistry}.
 * <p><br>
 * Usually you don't need to use this.
 * Only if you want to register custom listener definition detectors.
 */
public class ListenerDetectorsRegistryImpl implements ListenerDetectorsRegistry {

	private final List<ListenerDefinitionDetector> detectors = new ArrayList<>();

	public ListenerDetectorsRegistryImpl() {
		registerDefaultDetectors();
	}

	@Override
	public @NonNull Collection<ListenerDefinitionDetector> getDetectors() {
		return Collections.unmodifiableCollection( detectors );
	}

	@Override
	public void registerListenerDefinitionDetector( @NonNull ListenerDefinitionDetector listenerDefinitionDetector ) {
		detectors.add( 0, listenerDefinitionDetector );
	}

	@Override
	public void reset() {
		detectors.clear();
		registerDefaultDetectors();
	}

	private void registerDefaultDetectors() {
		registerListenerDefinitionDetector( new TypicalJavaListenerDetector() );
		registerListenerDefinitionDetector( new ObservableDetector() );
		registerListenerDefinitionDetector( new PropertyChangeDetector() ); // should processed be before TypicalJavaListenerDetector
	}
}
