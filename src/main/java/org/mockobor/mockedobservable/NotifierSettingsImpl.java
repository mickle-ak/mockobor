package org.mockobor.mockedobservable;

import lombok.Builder;
import org.eclipse.jdt.annotation.NonNull;


@Builder( toBuilder = true )
public class NotifierSettingsImpl implements NotifierSettingsUpdater {

	@Builder.Default
	private boolean strictCheckListenerList = true;

	@Builder.Default
	private boolean implementListeners = true;


	public static NotifierSettingsImpl createDefaultSettings() {
		return builder().build();
	}


	@Override
	public boolean getStrictCheckListenerList() {
		return strictCheckListenerList;
	}

	@Override
	public @NonNull NotifierSettingsUpdater strictListenerListCheck() {
		strictCheckListenerList = true;
		return this;
	}

	@Override
	public @NonNull NotifierSettingsUpdater lenientListenerListCheck() {
		strictCheckListenerList = false;
		return this;
	}


	@Override
	public boolean shouldNotifierImplementListenerInterfaces() {
		return implementListeners;
	}

	@Override
	public @NonNull NotifierSettingsUpdater implementListenerInterfaces() {
		implementListeners = true;
		return this;
	}
	
	@Override
	public @NonNull NotifierSettingsUpdater ignoreListenerInterfaces() {
		implementListeners = false;
		return this;
	}
}
