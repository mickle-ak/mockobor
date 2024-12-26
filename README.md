[![GitHub release (latest by date)](https://img.shields.io/github/v/release/mickle-ak/mockobor)][releases]
[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/mickle-ak/mockobor/Java%20CI%20with%20Gradle)](https://github.com/mickle-ak/mockobor/actions/workflows/gradle_CI.yml)
[![javadoc](https://javadoc.io/badge2/io.github.mickle-ak.mockobor/mockobor/javadoc.svg)](https://javadoc.io/doc/io.github.mickle-ak.mockobor/mockobor)
[![GitHub](https://img.shields.io/github/license/mickle-ak/mockobor)](LICENSE)
[![codecov](https://codecov.io/gh/mickle-ak/mockobor/branch/master/graph/badge.svg?token=E7MOZ69GSA)](https://codecov.io/gh/mickle-ak/mockobor)
[![Maven Centrale](https://img.shields.io/maven-metadata/v.svg?label=maven-central&metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fio%2Fgithub%2Fmickle-ak%2Fmockobor%2Fmockobor%2Fmaven-metadata.xml)][maven-central-mockobor]

# Mockobor

**Moc**ked **Ob**servable **Ob**servation - a java library to simulate sending of events (via java listeners) from a
mocked collaborator to a tested object.

When writing a lot of unit tests for large Java enterprise applications, certain problems tend to occur repeatedly:

- Simulating the sending of events (via Java listeners) from mocked collaborators to tested objects.
- Verifying the complete deregistration of listeners registered by mocked collaborators.

And all of these needs to be done without modifying production code.

While this process can be done manually, Mockobor makes it simpler, faster, and requires fewer implementation details in your tests.

- [Dependencies](#dependencies)
- [Examples](#examples)
	- [simulate the sending of events from a mocked collaborator to the tested object](#simulate-the-sending-of-events-from-a--mocked-collaborator-to-the-tested-object)
	- [ensure complete deregistration of listeners](#ensure-complete-deregistration-of-listeners)
	- [use Mockito annotations and Mockobor together](#use-mockito-annotations-and-mockobor-together)
- [Detailed Usage](#detailed-usage)
	- [monitor a mocked observable object and verify the complete deregistration of its listeners](#monitor-a-mocked-observable-object-and-verify-the-complete-deregistration-of-its-listeners)
	- [listener selectors](#listener-selectors)
	- [registration order](#registration-order)
	- [listener notifier settings](#listener-notifier-settings)
- [Extensions](#extensions)
	- [Defining a Custom Listener Detector](#defining-a-custom-listener-detector)
	- [Using unsupported mocking tools](#using-unsupported-mocking-tools)
- [Restrictions](#restrictions)
- [Installation](#installation)
- [Changelog](CHANGELOG.md)
- [Contributing](CONTRIBUTING.md)

## Dependencies

Mockobor depends only on the following libraries:

- eclipse non-null annotations ([org.eclipse.jdt.annotation](https://search.maven.org/artifact/org.eclipse.jdt/org.eclipse.jdt.annotation))

To use [Mockobor][maven-central-mockobor] in unit tests:

- Ensure you are using **Java 11** or more recent version.
- Include at least one of the following mocking tools in your tests:
	- [Mockito](https://github.com/mockito/mockito) 5.0.0+
	- [EasyMock](https://github.com/easymock/easymock) 3.4+
	- _Other mocking tools can also be added — refer to [Using unsupported mocking tools](#using-unsupported-mocking-tools)._

if you use _java 8_ or _Mockito 2.20.1 - 4.11.0_, you can still
use [Mockobor 1.0.5](https://github.com/mickle-ak/mockobor/releases/tag/v1.0.5) ([Maven Central](https://central.sonatype.com/artifact/io.github.mickle-ak.mockobor/mockobor/1.0.5))

## Examples

### simulate the sending of events from a  mocked collaborator to the tested object

Given that you have the following classes in your Java application:

- Your class under test, which adds (for example, in the constructor) a listener to a specified observable object and removes it in another method
  (for example, in `destroy()`):

```java
/** Object that you want to test. */
public class TestedObserver {

	private final SomeObservable         someObservable;
	private final Observer               observer               = new ObserverIml();
	private final PropertyChangeListener propertyChangeListener = new PropertyChangeListenerImpl();
	private final MyListener             myListener             = new MyListenerImpl();
	private final MyAnotherListener      myAnotherListener      = new MyAnotherListenerImpl();

	/** It registers some listeners by the specified (in tests - mocked) observable object. */
	public TestedObserver( SomeObservable someObservable ) {
		this.someObservable = someObservable;
		someObservable.addPropertyChangeListener( "prop", propertyChangeListener );
		someObservable.addMyListener( "sel", myListener );
		someObservable.addTwoListeners( myListener, myAnotherListener );
		someObservable.addObserver( observer );
	}

	/** And removes all listeners on destroy. */
	public void destroy() {
		someObservable.deleteObserver( observer );
		someObservable.removeTwoListeners( myListener, myAnotherListener );
		someObservable.removeMyListener( "sel", myListener );
		someObservable.removePropertyChangeListener( "prop", propertyChangeListener );
	}
}
```

_Note: It is not strictly necessary to register listeners by directly invoking `addXxxListener` methods. Registration can be performed using annotations,
aspects, or other mechanisms. What
is important is that the registration methods of the observable object are invoked somewhere._

- A collaborator of the tested object that fires events to the registered listeners:

```java
/** Some observable object with ability to register listeners/observers. */
public interface SomeObservable {

	// property change support
	void addPropertyChangeListener( String propertyName, PropertyChangeListener listener );
	void removePropertyChangeListener( String propertyName, PropertyChangeListener listener );

	// typical java listeners
	void addMyListener( String selector, MyListener myAnotherListener );
	void removeMyListener( String selector, MyListener myAnotherListener );

	// another typical java listeners
	void addTwoListeners( MyListener myListener, MyAnotherListener myAnotherListener );
	void removeTwoListeners( MyListener myListener, MyAnotherListener myAnotherListener );

	// Observable
	void addObserver( Observer o );
	void deleteObserver( Observer o );
}
```

- and listeners:

```java
public interface MyListener {
	void somethingChanged1( Object somethingNewValue );
	int somethingChanged2( Object somethingNewValue );
}

public interface MyAnotherListener {
	void somethingOtherChanged( Object somethingOtherValue );
}
```

In tests, we mock the collaborator (`SomeObservable`) using one of the supported mocking tools
(see [Dependencies](#dependencies)) and create a notifier object (`ListenersNotifier`) to send events:

```java
class TestedObserver_Test {
	// create mock of SomeObservable 
	private final SomeObservable mockedObservable = Mockito.mock( SomeObservable.class );

	// create notifier for SomeObservable
	private final ListenersNotifier notifier = Mockobor.createNotifierFor( mockedObservable );

	// Object under tested. It registers listeners by the specified SomeObservable object.
	private final TestedObserver testObject = new TestedObserver( mockedObservable );
  
  ...
}
```

#### typical java style listeners

As you can see, `SomeObservable` uses (among other things) typical Java-style listeners, such as `MyListener`, with registration methods like `addMyListener`
and `removeMyListener`. Mockobor
treats such classes as "using a typical Java listener" and creates a base notifier—`ListenersNotifier`—to send events.

Now, to simulate processes in `SomeObservable`, we will send events to the listeners `MyListener` and `MyAnotherListener`, which are registered in the
constructor of `TestedObserver`:

```java
class TestedObserver_Test {
	private final SomeObservable    mockedObservable = Mockito.mock( SomeObservable.class );
	private final ListenersNotifier notifier         = Mockobor.createNotifierFor( mockedObservable );
	private final TestedObserver    testObject       = new TestedObserver( mockedObservable );

	@Test
	void testSendEventToJavaStyleListeners() {
		// Send events to testObject using listener interfaces (first method):
		( (MyListener) notifier ).somethingChanged1( newValue );
		int answer1 = ( (MyListener) notifier ).somethingChanged2( newValue2 );
		( (MyAnotherListener) notifier ).somethingOtherChanged( newValue3 );

		// Send events to testObject using ListenersNotifier (another approach, identical to the above):
		notifier.notifierFor( MyListener.class ).somethingChanged1( newValue );
		int answer2 = notifier.notifierFor( MyListener.class ).somethingChanged2( newValue2 );
		notifier.notifierFor( MyAnotherListener.class ).somethingOtherChanged( newValue3 );

		// If listeners are registered with a specific non-empty qualifier  
		// (e.g., in the `TestedObserver` constructor: someObservable.addMyListener("sel", myListener)),
		// you can then send events to those listeners:
		notifier.notifierFor( "sel", MyListener.class ).somethingChanged1( newValue );
		notifier.notifierFor( MyListener.class, selector( "sel" ) ).somethingChanged1( newValue ); // exactly as above

		// To notify (send the same event to) listeners of the same class 
		// that are registered with at least one of the specified selectors 
		// (in this case: either without a selector or with "sel" as the selector):
		notifier.notifierFor( MyListener.class, selector(), selector( "sel" ) ).somethingChanged1( newValue );
	}
}
```

See also [UsageExample_TypicalJavaListener_Test.java][UsageExample_TypicalJavaListener_Test]

#### PropertyChangeListener

If your collaborator (observable object) provides methods such as  
`void addPropertyChangeListener(PropertyChangeListener listener)` or  
`void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)`,  
it is treated as "similar to PropertyChangeSupport."
In such cases, Mockobor creates a specialized notifier called `PropertyChangeNotifier`:

```java
class TestedObserver_Test {
	private final SomeObservable    mockedObservable = Mockito.mock( SomeObservable.class );
	private final ListenersNotifier notifier         = Mockobor.createNotifierFor( mockedObservable );
	private final TestedObserver    testObject       = new TestedObserver( mockedObservable );

	@Test
	void testSendEventToPropertyChangeListeners() {
		// using PropertyChangeNotifier
		PropertyChangeNotifier propertyChangeNotifier = (PropertyChangeNotifier) notifier;
		propertyChangeNotifier.firePropertyChange( null, "o2", "n2" );
		propertyChangeNotifier.firePropertyChange( "prop", "o1", "n1" );

		// using ListenersNotifier
		notifier.notifierFor( PropertyChangeListener.class )
		        .propertyChange( new PropertyChangeEvent( mockedObservable, "p4", "o4", "n4" ) );


		// Using `ListenersNotifier` with selectors  
		// behaves exactly the same as:  
		// - `propertyChangeNotifier.firePropertyChange(null, 'o5', 'n5')`  
		// - `propertyChangeNotifier.firePropertyChange('prop', 'o5', 'n5')`    
		notifier.notifierFor( PropertyChangeListener.class, selector(), selector( "prop" ) )
		        .propertyChange( new PropertyChangeEvent( mockedObservable, "prop", "o5", "n5" ) );

		// direct using listener interface (PropertyChangeListener) 
		( (PropertyChangeListener) notifier )
				.propertyChange( new PropertyChangeEvent( mockedObservable, "prop", "o3", "n3" ) );
	}
}
```

See also [UsageExample_BeanPropertyChange_Test.java][UsageExample_BeanPropertyChange_Test]

#### Observable, Observer

If your collaborator (observable object) provides a method such as `void addObserver(Observer observer)`, it is treated as "similar to Observable."
In such cases, Mockobor creates a specialized notifier called `ObservableNotifier`:

```java
class TestedObserver_Test {
	private final SomeObservable    mockedObservable = Mockito.mock( SomeObservable.class );
	private final ListenersNotifier notifier         = Mockobor.createNotifierFor( mockedObservable );
	private final TestedObserver    testObject       = new TestedObserver( mockedObservable );

	@Test
	void testSendEventToPropertyChangeListeners() {
		// using ObservableNotifier
		ObservableNotifier observableNotifier = (ObservableNotifier) notifier;
		observableNotifier.notifyObservers();
		observableNotifier.notifyObservers( "v1" );

		// using ListenersNotifier
		notifier.notifierFor( Observer.class ).update( null, "v3" );

		// direct using listener interface (Observer)
		( (Observer) notifier ).update( null, "v2" );
	}
}
```

See also [UsageExample_Observable_Test.java][UsageExample_Observable_Test]

### ensure complete deregistration of listeners

You can use Mockobor to verify whether all listeners registered by the mocked observable object have been unregistered:

```java
class TestedObserver_Test {
	private final SomeObservable    mockedObservable = Mockito.mock( SomeObservable.class );
	private final ListenersNotifier notifier         = Mockobor.createNotifierFor( mockedObservable );
	private final TestedObserver    testObject       = new TestedObserver( mockedObservable );

	@Test
	void testAllListenersAreRemoved() {

		// tested object should remove itself from the specified PropertyChangeSupport object on close.
		testObject.destroy(); // or close() or dispose() etc.

		// check that all listeners are unregistered
		Mockobor.assertThatAllListenersAreUnregistered( notifier );
	}
}
```

See also [UsageExample_allListenersAreUnregistered_Test.java][UsageExample_allListenersAreUnregistered_Test]

### use Mockito annotations and Mockobor together

If you are using Mockito as your mocking tool, you can seamlessly combine Mockito annotations with Mockobor:

```java

@Mock
private SomeObservable mockedObservable;

@InjectMocks
private AnnotationsTestObject testedObserver;

private ListenersNotifier notifier;

@Test
void test_notifications() {
  ...
	notifie = Mockobor.createNotifierFor( mockedObservable );
  ...
	notifier.notifierFor( MyListener.class ).onChange( 1f );
  ...
}
```

See [UsageExample_MockitoAnnotation_Test.java][UsageExample_MockitoAnnotation_Test]

## Detailed Usage

### monitor a mocked observable object and verify the complete deregistration of its listeners

To simulate event sending (via Java listeners) from a mocked collaborator to the tested object, Mockobor generates a special notifier object for the mocked
observable:

```java
ListenersNotifier notifier = Mockobor.createNotifierFor( mockedObservableObject )
```

_Note: Your test object must listen to the same instance of the mocked observable object specified in the `Mockobor.createNotifierFor` invocation._

The notifier object implements the following interfaces, depending on the methods found in the specified mocked observable object:

- `ListenerNotifier` – always implements.
- `XxxListener` (standard Java-style listener) – implements if methods like `addXxxListener(XxxListener)` are detected.
- `PropertyChangeNotifier` + `PropertyChangeListener` – implements if methods like `addPropertyChangeListener(PropertyChangeListener)` are detected.
- `ObservableNotifier` + `Observer` – implements if methods like `addObserver(Observer)` are detected.

This notifier object can be utilized to perform the following actions:

- **Send events to the test object:**

	- Using the `notifierFor` method: `notifier.notifierFor(XxxListener.class).<listener method>(arguments)`
	- Directly via the listener interface: `((XxxListener) notifier).<listener method>(arguments)`
	- Using `PropertyChangeNotifier` (when applicable): `((PropertyChangeNotifier) notifier).firePropertyChange(...)`
	- Using `ObservableNotifier` (when applicable): `((ObservableNotifier) notifier).notifyObservers(...)`

- **Verify complete deregistration of listeners:**

	- For a single notifier:  
	  `assertThat(notifier.allListenersAreUnregistered()).isTrue()`
	- For multiple notifiers:  
	  `Mockobor.assertThatAllListenersAreUnregistered(notifier1, ..., notifierN)`

For more details see [JavaDoc][javadoc] ([Mockobor][Mockobor], [Mockobor.createNotifierFor][Mockobor.createNotifierFor]
, [Mockobor.assertThatAllListenersAreUnregistered][Mockobor.assertThatAllListenersAreUnregistered],
[ListenerNotifier][ListenerNotifier]) and [Examples](#examples).

### listener selectors

Sometimes, a listener should or can be registered not for all events, but only for specific qualified events.
For example, in `java.beans.PropertyChangeSupport.addPropertyChangeListener(String propertyName, PropertyChangeListener listener)`,
the `propertyName` serves as a qualifier. In such cases, Mockobor utilizes a `selector`.
It identifies 'selector' arguments in registration methods and allows the addition of selectors when sending notifications:

```java
// In production code, the object under test registers its listener
public class ClassUnderTest {
  ...

	void someInitMethod() {
    ...
		observable.addMyListener( "q1", "q2", listner1 );  // ("q1", "q2") is the selector here
		observable.addMyListener( "q3", listener2 ); // "q3" is the selector here
		observable.addMyListener( listener3 ); // the selector is empty here
    ...
	}
 ...
}

// In tests, send notifications to the listeners in the object under test.
class SomeTest {
  ...

	@Test
	void someTestMethod() {
    ...
		// Send a notification to the listener registered with the selector ('q1', 'q2'):
		notifier.notiferFor( listner1.class, selector( "q1", "q2" ) ).listener_method();

		// Send a notification to the listener registered with the selector "q3":
		notifier.notiferFor( "q3", listner2.class ).listener_method();
		notifier.notifierFor( listener2.class, selector( "q3" ) ).listener_method(); // identical to the line above

		// Send a notifications to the listener registered without a selector or with an empty selector: 
		notifier.notiferFor( listner3.class ).listener_method();
		notifier.notifierFor( listener3.class, selector() ).listener_method();  // identical to the line above

		// Send a notifications to the listeners registered with any of the specified selectors
		// (here, all three: listener1, listener2 and listener3; see "someInitMethod" above)
		notifier.notiferFor( listner1.class, selector( "q1", "q2" ), selector( "q3" ), selector() ).listener_method();
    ...
	}
  ...
}
```

For more details see [Examples / typical java style listeners](#typical-java-style-listeners)

### registration order

Typically, the listener notifier object is created _prior to_ the tested object registering its listeners via the mocked observable.
This method is reliable and compatible with all mocking tools:

```java
class SomeTest {
	private final SomeObservable    mockedObservable = EasyMock.mock( SomeObservable.class );
	private final ListenersNotifier notifier         = Mockobor.createNotifierFor( mockedObservable );  // (1) the listener notifier created
	private final TestedObserver    testObject       = new TestedObserver( mockedObservable ); // (2) the tested object registers its listeners
  ...
}
```

Alternatively, **if you are using Mockito**, you have the flexibility to create a listener notifier object whenever needed:

```java
class SomeTest {
	private final SomeObservable mockedObservable = Mockito.mock( SomeObservable.class );
	private final TestedObserver testObject       = new TestedObserver( mockedObservable );

	@Test
	void test_notifications() {
    ...
		notifie = ockobor.createNotifierFor( mockedObservable );
    ...
		notifier.notifierFor( XxxListener.class ).onChange(...);
    ...
	}
}
```

It allows usage of Mockito annotations together with Mockobor.
See [Examples / use Mockito annotations and Mockobor together](#use-mockito-annotations-and-mockobor-together)

_Note: This is not compatible with EasyMock! See [Restrictions / EasyMock restrictions](#easymock-restrictions)_

### listener notifier settings

`NotifierSettings` can be used to control the following aspects of the creation and behavior of a listener notifier:

- **Strict or lenient checking** to determine if the list of listeners selected for sending notifications contains any listeners:
	+ **strict** (default) - Throws a `ListenersNotFoundException` if no listener is selected to receive the notification.
	+ **lenient** - Does nothing if no listener is selected.
- **Interface implementation by a new listener notifier**:
	+ **true** (default) - All new `ListenersNotifier` instances returned from `Mockobor.createNotifierFor` implement all detected
	  listener interfaces. This allows events to be fired using either of the following approaches:
		* `((MyListener) notifier).somethingChanged(...)`
		* `notifier.notifierFor(MyListener.class).somethingChanged(...)`
	+ **false** - New `ListenersNotifier` instances **do not** implement listener interfaces. As a result, events can only be fired using the following
	  approach:
		* `notifier.notifierFor(MyListener.class).somethingChanged(...)`

`NotifierSettings` can be changed globally for all future `ListenersNotifier` instances by modifying the settings stored statically in `MockoborContext`:

```java
  MockoborContext.updateNotifierSettings().

ignoreListenerInterfaces().

lenientListenerListCheck();
```

or exclusively for one-time use during creation:

```java
  ListenersNotifier notifier = Mockobor.createNotifierFor(
		mockedObservable,
		Mockobor.notifierSettings().ignoreListenerInterfaces().lenientListenerListCheck();
```

For more details see [UsageExample_NotifierSettings_Test.java][UsageExample_NotifierSettings_Test]

## Extensions

### Defining a Custom Listener Detector

Out of the box Mockobor supports three kinds of listeners:

- **Typical Java-style listeners**, where listener class names follow the pattern `XxxListener`, and registration method names follow conventions like
  `addXxxListener` and `removeXxxListener` (see [Typical Java-style Listeners Example](#typical-java-style-listeners)).
- **`java.beans.PropertyChangeListener`**, considered a subclass of typical Java-style listeners (
  see [PropertyChangeListener Example](#propertychangelistener)).
- **`java.util.Observable` / `java.util.Observer`** (see [Observable / Observer Example](#observable-observer)).

If you have a different type of listener, you can add support for it by following these steps:

1. **Create a custom implementation** of `ListenerDefinitionDetector`.
2. **Register it** using the method `MockoborContext.registerListenerDefinitionDetector(yourListenerDetector)`.

To implement `ListenerDefinitionDetector`, you typically need to extend `AbstractDetector` and implement/override the following methods:

- **`isListenerClass(Class, Method)`**: Verifies if the specified parameter type is a listener type.
- **`isAddMethods(Method)`**: Determines if the specified method is a listener registration method (for adding listeners).
- **`isRemoveMethods(Method)`**: Determines if the specified method is a listener deregistration method (for removing listeners).
- **`getAdditionalInterfaces()`**: (Optional) Use this if you want to provide special support for your listeners.
- **`getNotificationDelegates()`**: (Optional) Use this if needed to:
	- Implement specific methods for your additional interfaces (it's often better to rely on default implementations in the interface itself).
	- Override certain methods of `ListenersNotifier` (but consider if this is truly necessary).

For more details see [JavaDoc][javadoc] ([ListenerDefinitionDetector][ListenerDefinitionDetector]
, [AbstractDetector][AbstractDetector]
and [MockoborContext.registerListenerDefinitionDetector][MockoborContext.registerListenerDefinitionDetector])

See also [Custom listener detector example][Custom listener detector example],
[PropertyChangeDetector.java][PropertyChangeDetector] or [TypicalJavaListenerDetector.java][TypicalJavaListenerDetector]
as implementation examples.

### Using unsupported mocking tools

To redirect listener registration methods from a mocked observable object to the internal list of listeners, Mockobor requires the following:

1. Understand which mocking tool was used to mock the specified observable object.
2. Create a redirection mechanism using the detected mocking tool.

Mockobor provides out-of-the-box support for some mocking tools (see [Dependencies](#dependencies)).

If you are using a different mocking tool, you can add support for it by following these steps:

- **Create a custom implementation** of `ListenerRegistrationHandler`.
- **Register it** using `MockoborContext.registerListenerRegistrationHandler`.

For more details see [JavaDoc][javadoc] ([ListenerRegistrationHandler][ListenerRegistrationHandler]
and [MockoborContext.registerListenerRegistrationHandler][MockoborContext.registerListenerRegistrationHandler])

See also [MockitoListenerRegistrationHandler.java][MockitoListenerRegistrationHandler]
and [EasymockListenerRegistrationHandler.java][EasymockListenerRegistrationHandler]
as implementation examples.

## Restrictions

- **Only interfaces are accepted as listeners**:  
  Methods like `addMyListener(MyListener)` will not be recognized as registration methods if `MyListener` is a class.  
  `MyListener` must be an interface. This is standard practice in Java.

- **Listener arrays and varargs are not supported**:  
  Registration methods such as `addMyListener(MyListener[] listeners)` or `addMyListener(MyListener... listeners)` will also not be recognized as valid
  registration methods.

### EasyMock restrictions

If you mock a collaborator object using _EasyMock_:

When mocking a collaborator object using **EasyMock**, keep the following restrictions in mind:

- **Notifier object creation**:  
  A notifier object must be created (via `Mockobor.createNotifierFor`) **before** the tested object registers its listener through the mocked collaborator.  
  This is because registration methods must be redirected to the notifier before being invoked by the tested object.  
  As a result, it is not possible to inject mocks into the observer test object using the `@TestSubject` annotation.

- **Issues with varargs in listener methods**:  
  If listener registration methods of the mocked collaborator object utilize _varargs_ (e.g., `addListener(MyListener l, Object... selector)`),
  issues may arise. 
  During recording mode, Mockobor cannot predict how many arguments will be passed during the actual invocation of such a method. 
  Consequently, recorded and real invocations may not match. For more details, refer to [this issue](https://github.com/easymock/easymock/issues/130).

## Installation

To use **Mockobor** in your unit tests, add the following dependency to your project.

### maven

```xml 
<dependency>
	<groupId>io.github.mickle-ak.mockobor</groupId>
	<artifactId>mockobor</artifactId>
	<version>1.1.4</version>
	<scope>test</scope>
</dependency>
```

### gradle

```groovy
testImplementation 'io.github.mickle-ak.mockobor:mockobor:1.1.4'  // groovy
```
```kotlin
testImplementation("io.github.mickle-ak.mockobor:mockobor:1.1.4")  // kotlin
```

For more details, refer to [Maven Central][maven-central-mockobor].


[releases]: https://github.com/mickle-ak/mockobor/releases

[UsageExample_TypicalJavaListener_Test]: src/test/java/org/mockobor/mockedobservable/UsageExample_TypicalJavaListener_Test.java

[UsageExample_BeanPropertyChange_Test]: src/test/java/org/mockobor/mockedobservable/UsageExample_BeanPropertyChange_Test.java

[UsageExample_Observable_Test]: src/test/java/org/mockobor/mockedobservable/UsageExample_Observable_Test.java

[UsageExample_allListenersAreUnregistered_Test]: src/test/java/org/mockobor/mockedobservable/UsageExample_allListenersAreUnregistered_Test.java

[UsageExample_MockitoAnnotation_Test]:src/test/java/org/mockobor/mockedobservable/UsageExample_MockitoAnnotation_Test.java

[UsageExample_NotifierSettings_Test]: src/test/java/org/mockobor/mockedobservable/UsageExample_NotifierSettings_Test.java

[Custom listener detector example]: src/test/java/org/mockobor/mockedobservable/UsageExample_CustomDetector_Test.java

[PropertyChangeDetector]: src/main/java/org/mockobor/listener_detectors/PropertyChangeDetector.java

[TypicalJavaListenerDetector]: src/main/java/org/mockobor/listener_detectors/TypicalJavaListenerDetector.java

[MockitoListenerRegistrationHandler]: src/main/java/org/mockobor/mockedobservable/mocking_tools/MockitoListenerRegistrationHandler.java

[EasymockListenerRegistrationHandler]: src/main/java/org/mockobor/mockedobservable/mocking_tools/EasymockListenerRegistrationHandler.java


[javadoc]: https://javadoc.io/doc/io.github.mickle-ak.mockobor/mockobor/latest/index.html?overview-summary.html

[Mockobor]: https://javadoc.io/doc/io.github.mickle-ak.mockobor/mockobor/latest/org/mockobor/Mockobor.html

[Mockobor.createNotifierFor]: https://javadoc.io/doc/io.github.mickle-ak.mockobor/mockobor/latest/org/mockobor/Mockobor.html#createNotifierFor-java.lang.Object-

[Mockobor.assertThatAllListenersAreUnregistered]: https://javadoc.io/doc/io.github.mickle-ak.mockobor/mockobor/latest/org/mockobor/Mockobor.html#assertThatAllListenersAreUnregistered-org.mockobor.mockedobservable.ListenersNotifier...-

[ListenerNotifier]: https://javadoc.io/doc/io.github.mickle-ak.mockobor/mockobor/latest/org/mockobor/mockedobservable/ListenersNotifier.html

[AbstractDetector]: https://javadoc.io/doc/io.github.mickle-ak.mockobor/mockobor/latest/org/mockobor/listener_detectors/AbstractDetector.html

[ListenerDefinitionDetector]: https://javadoc.io/doc/io.github.mickle-ak.mockobor/mockobor/latest/org/mockobor/listener_detectors/ListenerDefinitionDetector.html

[MockoborContext.registerListenerDefinitionDetector]: https://javadoc.io/doc/io.github.mickle-ak.mockobor/mockobor/latest/org/mockobor/MockoborContext.html#registerListenerDefinitionDetector-org.mockobor.listener_detectors.ListenerDefinitionDetector-

[ListenerRegistrationHandler]: https://javadoc.io/doc/io.github.mickle-ak.mockobor/mockobor/latest/org/mockobor/mockedobservable/mocking_tools/ListenerRegistrationHandler.html

[MockoborContext.registerListenerRegistrationHandler]: https://javadoc.io/doc/io.github.mickle-ak.mockobor/mockobor/latest/org/mockobor/MockoborContext.html#registerListenerRegistrationHandler-org.mockobor.mockedobservable.mocking_tools.ListenerRegistrationHandler-

[maven-central-mockobor]: https://central.sonatype.com/artifact/io.github.mickle-ak.mockobor/mockobor
