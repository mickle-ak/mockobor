- **In the next Version**
	- Changed
		- add defaults values for further types
		- automatically create release on version tag
		- automatically update version in Changelog
		- bump dependencies versions
		- update documentation
		- code cleaning

- **1.0.2** (21.09.2021)
	- Changed
		- Test with Java 17 (exclude EasyMock)
		- Documentation + javadoc
		- use axion-release gradle plugin for versioning

- **1.0.1** (30.04.2021)
	- Changed
		- Documentation + javadoc
	- Removed
		- subclasses of `Observer` or `PropertyChangeListener` as types of listener parameters no more supported. Only
		  methods with exactly the same argument type (`Observer.class`, `PropertyChangeListener.class`) can be
		  recognised as listener registration methods with a special support

- **1.0** (27.04.2021)
	- Added
		- simulation of sending of events from mocked collaborator to tested object
		- take over listeners registered before notifier object created (Mockito only)
		- checking of completely deregistration of listeners
		- listener notifier settings
		- support for Mockito and EasyMock
