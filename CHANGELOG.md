- **In the next Version**

- **1.1.0** (07.08.2023)
    - Changed
        - Update the minimum supported Java version to 11
        - use latest Mockito version (5.4.0)
        - add code style definition

- **1.0.5** (04.08.2023)
    - Changed
        - use latest EasyMock version (5.1.0)

- **1.0.4** (04.08.2023)
    - Correct publishing to Maven Central

- **1.0.3** (04.08.2023)
    - Changed
        - add defaults values for further types
        - automatically create release on version tag
        - automatically update the version in Changelog
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
          recognized as listener registration methods with a special support

- **1.0** (27.04.2021)
    - Added
        - simulation of sending of events from mocked collaborator to a tested object
        - take over listeners registered before notifier object created (Mockito only)
        - checking of complete deregistration of listeners
        - listener notifier settings
        - support for Mockito and EasyMock
