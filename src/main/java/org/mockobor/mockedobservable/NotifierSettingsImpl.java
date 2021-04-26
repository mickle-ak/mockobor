package org.mockobor.mockedobservable;

import lombok.Builder;


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
	public NotifierSettingsUpdater strickListenerListCheck() {
		strictCheckListenerList = true;
		return this;
	}

	@Override
	public NotifierSettingsUpdater lenientListenerListCheck() {
		strictCheckListenerList = false;
		return this;
	}


	@Override
	public boolean shouldNotifierImplementListenersInterfaces() {
		return implementListeners;
	}

	@Override
	public NotifierSettingsUpdater implementListenersInterfaces() {
		implementListeners = true;
		return this;
	}
	@Override
	public NotifierSettingsUpdater ignoreListenersInterfaces() {
		implementListeners = false;
		return this;
	}
}
