package org.mockobor.listener_detectors;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Implementation of {@link ListenerDetectorsRegistry}.
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
		detectors.add( new PropertyChangeDetector() ); // should processed be before TypicalJavaListenerDetector
		detectors.add( new TypicalJavaListenerDetector() );
		detectors.add( new ObservableDetector() );
	}
}
