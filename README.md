[![GitHub release (latest by date)](https://img.shields.io/github/v/release/mickle-ak/mockobor)][releases]
[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/mickle-ak/mockobor/Java%20CI%20with%20Gradle)](https://github.com/mickle-ak/mockobor/actions/workflows/gradle_CI.yml)
[![javadoc](https://javadoc.io/badge2/io.github.mickle-ak.mockobor/mockobor/javadoc.svg)](https://javadoc.io/doc/io.github.mickle-ak.mockobor/mockobor)
[![GitHub](https://img.shields.io/github/license/mickle-ak/mockobor)](LICENSE)
[![codecov](https://codecov.io/gh/mickle-ak/mockobor/branch/master/graph/badge.svg?token=E7MOZ69GSA)](https://codecov.io/gh/mickle-ak/mockobor)


# Mockobor

Mocked Observable Observation - library to simplify some aspects of unit testing with java.

If you write a lot of unit tests for a big java enterprise application, you can see, that some problems come again and
again - you offen need to:

- simulate sending of events (via java listeners) from mocked collaborator to tested object,
- check completely deregistration of listeners registered by mocked collaborator,
- collect and verify events sent from test object (not implemented jet)

And all of this should be done without changing of production code.

Of course, it can be done manually, but with Mockobor it is simpler, faster and needs less implementation details in
your tests.

- [Dependencies](#dependencies)
- [Usage](#usage)
  - [listen for mocked observable object and check completely deregistration of listeners](#listen-for-mocked-observable-object-and-check-completely-deregistration-of-listeners)
    - [listener selectors](#listener-selectors)
    - [registration order](#registration-order)
    - [listener notifier settings](#listener-notifier-settings)
  - [collect and verify events from test object (not implemented yet)](#collect-and-verify-events-from-test-object)
- [Examples](#examples)
  - [simulate sending of events from mocked collaborator to tested object](#simulate-sending-of-events-from-mocked-collaborator-to-tested-object)
  - [check completely deregistration of listeners](#check-completely-deregistration-of-listeners)
  - [use Mockito annotations and Mockobor together](#use-mockito-annotations-and-mockobor-together)
- [Extension](#extension)
  - [Custom listener detector](#custom-listener-detector)
  - [Another mocking tool](#another-mocking-tool)
- [Restrictions](#restrictions)
- [Installation](#installation)
- [Changelog](CHANGELOG.md)
- [Releasing](RELEASING.md)


## Dependencies

To use Mockobor in unit tests:
- start test with _java 8+_
- use at least one of follow mocking tools in your tests:
  - _[Mockito](https://github.com/mockito/mockito) 2.20.1+_
  - _[EasyMock](https://github.com/easymock/easymock) 3.4+_

Mockobor propagates follow dependencies:
- eclipse non-null
  annotations ([org.eclipse.jdt.annotation](https://search.maven.org/artifact/org.eclipse.jdt/org.eclipse.jdt.annotation))


## Usage

### listen for mocked observable object and check completely deregistration of listeners

To simulate sending of events (via java listeners) from mocked collaborator to tested object, Mockobor creates for
mocked observable object a special notifier object:
```java
ListenersNotifier notifier = Mockobor.createNotifierFor(mockedObservableObject)
```
_Note: It is important, that your test object listens for the same mocked observable object as specified by invocation
of `Mockobor.createNotifierFor`._

This notifier object implements follow interfaces (depended on methods found in the specified mocked observable object):
- `ListenerNotifer` - always
- `XxxListener` (typical java style listener) if methods like `addXxxListener(XxxListener)` are found
- `PropertyChangeNotifier` + `PropertyChangeListener` if methods like `addPropertyChangeListener
  (PropertyChangeListener)` are found
- `ObservableNotifier` + `Observer` if methods like `addObserver(Observer)` are found

This notifier object can be used to:
- send events to test object:
  + using `notifierFor` method: `notifier.notifierFor(XxxListener.class).<listner method>(arguments)`
  + or direct using listener interface: `((XxxListener) notifier).<listner method>(arguments)`
  + using `PropertyChangeNotifier` (if suitable): `((PropertyChangeNotifier) notifier).firePropertyChange(...)`
  + using `ObservableNotifier`  (if suitable): `((ObservableNotifier) notifier).notifyObservers(...)`
- check completely deregistration of listeners
  + `Mockobor.assertThatAllListenersAreUnregistered( notifier1, ..., notifierN )`
  + or for single notifier `assertThat( notifier.allListenersAreUnregistered() ).isTrue()`

For more details see [JavaDoc][javadoc] ([Mockobor][Mockobor], [Mockobor.createNotifierFor][Mockobor.createNotifierFor]
, [Mockobor.assertThatAllListenersAreUnregistered][Mockobor.assertThatAllListenersAreUnregistered],
[ListenerNotifier][ListenerNotifier])
and [Examples](#examples) bellow.


#### listener selectors

Sometimes listener should/can be registered not for all events, but only for qualified events (the best example is
`java.beans.PropertyChangeSupport.addPropertyChangeListener(String propertyName, PropertyChangeListener listener)` -
here `propertyName` is a qualifier). In such cases, Mockobor uses `selector`. It recognises 'selector'-arguments in
registration methods and allows adding selectors by sending of notifications:
```java
        ...
        // in production code - object under test registers its listener
        observable.addMyListener( "q1","q2",listner1 );  // ("q1", "q2") is selector here
        observable.addMyListener( "q3",listener2 ); // "q3" is selector here
        observable.addMyListener( listener3 ); // here is selector empty
        ...
        ...
        // somewhere in tests send notification to listeners in object under test

        // send to listener registered with selector ("q1" "q2):
        notifier.notiferFor( listner1.class, selector( "q1","q2" )).listener_method();

        // send to listener registered with selector "q3":
        notifier.notiferFor( "q3", listner2.class ).listener_method();
        notifier.notifierFor( listener2.class, selector( "q3" )).listener_method(); // the same as above

        // send to listener registered with empty selector: 
        notifier.notiferFor( listner3.class ).listener_method();
        notifier.notifierFor( listener3.class, selector() ).listener_method();  // the same as above

        // send to listeners registered with one of the specified selectors (in this case - to all three listeners):
        notifier.notiferFor( listner1.class, selector("q1","q2"), selector("q3"), selector() ).listener_method();
        ...
```
For more detail see [Examples / typical java style listeners](#typical-java-style-listeners)


#### registration order

In normal practice, you create listener notifier object BEFORE tested object registers its listeners by mocked
observable. It works well, and it works with all mocking tool:
```java
private final SomeObservable mockedObservable = EasyMock.mock( SomeObservable.class );
private final ListenersNotifier notifier = Mockobor.createNotifierFor( mockedObservable );
private final TestedObserver testObject = new TestedObserver( mockedObservable );
```

At the same time, **if you use Mockito**, you can create listener notifier object whenever you like:
```java
private final SomeObservable mockedObservable = Mockito.mock( SomeObservable.class );
private final TestedObserver testObject = new TestedObserver( mockedObservable );

@Test
void test_notifications(){
        ...
        notifie = ockobor.createNotifierFor( mockedObservable );
        ...
        notifier.notifierFor( XxxListener.class ).onChange(...);
        ...
        }
```
It allows usage of Mockito annotations together with Mockobor.
See [Examples / use Mockito annotations and Mockobor together](#use-mockito-annotations-and-mockobor-together)

_Note: It does not work with EasyMock! See [Restrictions / EasyMock restrictions](#easymock-restrictions)_


#### listener notifier settings

`NotifierSettings` can be used to control follow aspects of creation and working of listener notifier :

- strict or lenient checking if list of listeners selected to send notification contains any listener
  + strick (default) - throw `ListenersNotFoundException` if no listener selected to send notification
  + lenient - do nothing in this case
- should a new listener notifier implements interfaces of detected listeners
  + true (default) - all new `ListenersNotifier` returned from `Mockobor.createNotifierFor` implement all detected
    listener interfaces. So events can be fired using both ways:
    * `((MyListener) notifier).somethingChanged(...)` or
    * `notifier.notifierFor( MyListener.class ).somethingChanged(...)`
  + false - all new `ListenersNotifier` **does not** implement listener interfaces. So there is only one way to fire
    events: `notifier.notifierFor( MyListener.class ).somethingChanged(...);`

`NotifierSettings` can be changed globally - for all next created `ListenersNotifier` - using settings stored statically
in `MockoborContext`:
```java
  MockoborContext.updateNotifierSettings().ignoreListenersInterfaces().lenientListenerListCheck();
```
or for one creation only:
```java
  ListenersNotifier notifier = Mockobor.createNotifierFor(
        mockedObservable,
        Mockobor.notifierSettings().ignoreListenersInterfaces().lenientListenerListCheck();
```
For more detail see [Examples / NotifierSettings](#notifiersettings)



### collect and verify events from test object
not implemented jet, probably later


## Examples

### simulate sending of events from mocked collaborator to tested object

Given you have follow classes in your java application.

- your class under test, that adds (for example in constructor) some listener to the specified observable object und
  removes them in another method (for example in `destroy()`):
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
    someObservable.addObserver( observer );
    someObservable.addPropertyChangeListener( "prop", propertyChangeListener );
    someObservable.addTwoListeners( myListener, myAnotherListener );
    someObservable.addMyListener( "sel", myListener );
  }

  /** And removes all listeners on destroy. */
  public void destroy() {
    someObservable.deleteObserver( observer );
    someObservable.removePropertyChangeListener( "prop", propertyChangeListener );
    someObservable.removeTwoListeners( myListener, myAnotherListener );
    someObservable.removeMyListener( "sel", myListener );
  }
}
```
_Note: It is not strictly necessary, to register listeners using direct invocation of addXxxListener methods, it can be any kind of
registration - using annotations, aspects or other mechanisms. Important is, that the registration methods of observable object will be
invoked somewhere._

- a collaborator of the tested object, that fires some events to registered listeners:
```java
/** Some observable object with ability to register listeners/observers. */
public interface SomeObservable {

  // typical java listeners
  void addTwoListeners( MyListener myListener, MyAnotherListener myAnotherListener );
  void removeTwoListeners( MyListener myListener, MyAnotherListener myAnotherListener );
  void addMyListener( String selector, MyListener myAnotherListener );
  void removeMyListener( String selector, MyListener myAnotherListener );

  // property change support
  void addPropertyChangeListener( String propertyName, PropertyChangeListener listener );
  void removePropertyChangeListener( String propertyName, PropertyChangeListener listener );

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

In tests, we mock the collaborator (`SomeObservable`) using one of supported mocking tools
(see [Dependencies](#Dependencies)) and create a notifier object (`ListenersNotifier`), used to send events:
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

How you can see, `SomeObservable` uses (inter alia) typical java style listeners like `MyListener` with registration
methods like `addMyListener` and `removeMyListener`. Mockobor considers such classes as "use typical java listener"
and creates a base notifier - `ListenersNotifier` - to send events.

Now (to simulate processes in `SomeObservable`) we will send events to listener `MyListener` and
`MyAnotherListener` registered in constructor of `TestedObserver`:
```java
class TestedObserver_Test {
  private final SomeObservable    mockedObservable = Mockito.mock( SomeObservable.class );
  private final ListenersNotifier notifier         = Mockobor.createNotifierFor( mockedObservable );
  private final TestedObserver    testObject       = new TestedObserver( mockedObservable );

  @Test
  void testSendEventToJavaStyleListeners() {
    // send events to testObject using listener interfaces (first way):
    ( (MyListener) notifier ).somethingChanged1( newValue );
    int answer1 = ( (MyListener) notifier ).somethingChanged2( newValue2 );
    ( (MyAnotherListener) notifier ).somethingOtherChanged( newValue3 );

    // send events to testObject using ListenersNotifier (another way, it is exactly the same as above):
    notifier.notifierFor( MyListener.class ).somethingChanged1( newValue );
    int answer2 = notifier.notifierFor( MyListener.class ).somethingChanged2( newValue2 );
    notifier.notifierFor( MyAnotherListener.class ).somethingOtherChanged( newValue3 );

    // if you have listeners registered with certain non-empty qualifier 
    // (see TestedObserver's constructor: someObservable.addMyListener( "sel", myListener )),
    // then you can send events to such listeners:
    notifier.notifierFor( "sel", MyListener.class ).somethingChanged1( newValue );
    notifier.notifierFor( MyListener.class, selector( "sel" ) ).somethingChanged1( newValue ); // exactly as above

    // to notify (send the same event to) listeners (with the same class) 
    // registered with at least one of the specified selectors
    // (here - without selector OR with "sel" as selector):
    notifier.notifierFor( MyListener.class, selector(), selector( "sel" ) )
            .somethingChanged1( newValue );
  }
}
```
See also [UsageExample_TypicalJavaListener_Test.java][UsageExample_TypicalJavaListener_Test]


#### PropertyChangeListener

If your collaborator (observable object) has methods like
`void addPropertyChangeListener(PropertyChangeListener listener)` or
`void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)`, then it is considered as
"like PropertyChangeSupport" and Mockobor creates for such classes a special notifier - `PropertyChangeNotifier`:
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

    // using ListenersNotifier with selectors
    // exactly the same as 
    // - propertyChangeNotifier.firePropertyChange( null, 'o5', 'n5')
    // - propertyChangeNotifier.firePropertyChange( 'prop', 'o5', 'n5')
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

If your collaborator (observable object) has methods like `void addObserver(Observer observer)`, then it is considered
as "like Observable" and Mockobor creates for such classes a special notifier - `ObservableNotifier`:
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


### check completely deregistration of listeners

You can use Mockobor to check if all registered by mocked observable object listeners are unregistered:
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

If you use Mockito as mocking tool, you can simply use Mockito annotations together with Mockobor:
```java
@Mock
private SomeObservable mockedObservable;

@InjectMocks
private AnnotationsTestObject testedObserver;

private ListenersNotifier notifier;

@Test
void test_notifications(){
        ...
        notifie = ockobor.createNotifierFor(mockedObservable);
        ...
        notifier.notifierFor(MyListener.class).onChange(1f);
        ...
        }
```
See [UsageExample_MockitoAnnotation_Test.java][UsageExample_MockitoAnnotation_Test]


### NotifierSettings
See [UsageExample_NotifierSettings_Test.java][UsageExample_NotifierSettings_Test]


## Extension

### Custom listener detector

Out of the box Mockobor supports three kinds of listeners:
- typical java style listeners, where listener classes names are like `XxxListener` and registration methods names are
  like `addXxxListener` and `removeXxxListener` (
  see [typical java style listeners example](#typical-java-style-listeners))
- `java.beans.PropertyChangeListener` (as a subclass of typical java style listeners,
  see [PropertyChangeListener example](#PropertyChangeListener))
- `java.utilObservable`/`java.util.Observer` (see [Observable / Observer example](#observable-observer))


if you have another kind of listeners, you can add support for your listeners:
- create custom implementation of `ListenerDefinitionDetector`
- register it using `MockoborContext.registerListenerDefinitionDetector(yourListenerdetector)`.

Typically, to implement `ListenerDefinitionDetector` you only need to extend `AbstractDetector` and implement/override
follow methods:
- `isListenerClass(Class, Method)` - to check if the specified parameter type is a listener type
- `isAddMethods(Method)` - to check if the specified method is a registration method to add listener
- `isRemoveMethods(Method)` - to check if the specified method is a registration method to remove listener
- `getAdditionalInterfaces()` - only if you want to provide special support for your listeners
- `getNotificationDelegates()` - only if you need to
  + implement some methods of your additional interfaces (better use default implementations in interface itself)
  + override some methods of `ListenersNotifier` (do you want it indeed?)

For more details see [JavaDoc][javadoc] ([ListenerDefinitionDetector][ListenerDefinitionDetector]
, [AbstractDetector][AbstractDetector]
and [MockoborContext.registerListenerDefinitionDetector][MockoborContext.registerListenerDefinitionDetector])

See also [Custom listener detector example][Custom listener detector example],
[PropertyChangeDetector.java][PropertyChangeDetector] or [TypicalJavaListenerDetector.java][TypicalJavaListenerDetector]
as implementation examples.


### Another mocking tool

To redirect listener registration methods from mocked observable object to internal list of listeners, Mockobor needs,
first, to understand what a mocking tool was used to mock the specified mocked observable object and, second, to be able
to create redirection using the detected mocking tool.

Out of the box Mockobor supports some mocking tools (see [Dependencies](#Dependencies)). If you use another mocking
tool, it is possible to add support for them:
- create custom implementation of `ListenerRegistrationHandler`
- register it using `MockoborContext.registerListenerRegistrationHandler`

For more details see [JavaDoc][javadoc] ([ListenerRegistrationHandler][ListenerRegistrationHandler]
and [MockoborContext.registerListenerRegistrationHandler][MockoborContext.registerListenerRegistrationHandler])

See also [MockitoListenerRegistrationHandler.java][MockitoListenerRegistrationHandler]
and [EasymockListenerRegistrationHandler.java][EasymockListenerRegistrationHandler]
as implementation examples.



## Restrictions

- only interfaces accepted as listeners - you can't use methods like `addMyListener(MyListener)`, where `MyListener` has
  a class type - such methods will be not recognised as registration methods. `MyListener` can be an interface only.
  That is in fact standard practice in java.

- registration of array of listeners (as vararg too) is not supported - methods like
  `addMyListener(MyListener[] listeners)` or `addMyListener(MyListener... listeners)` will be not recognised as
  registration methods.


### EasyMock restrictions

If you mock a collaborator object using _EasyMock_:

- a notifier object must be created (by calling `Mockobor.createNotifierFor`) before a tested object registers its listener by the mocked
  collaborator! It is necessary, because registration methods must be redirected to the notifier before they will be called from the tested
  object. As consequence, it is not possible to inject mocks to the observer test object using `@TestSubject` annotation.

- If listener registration methods of the mocked collaborator object have _varargs_ (
  like `addListener(MyListener l, Object...selector)`), then it can be problematic - during recording mode Mockobor can't forecast how many
  arguments will be used by real invocation of such method, therefore recorded and real invocations don't have to match (
  see [here](https://github.com/easymock/easymock/issues/130)).

- EasyMock (<=4.3) does not work with Java 17


## Installation

To use the Mockobor in your unit tests, you can download it from maven central: [mockobor][maven-central-mockobor] or
add this dependency entry:

### maven

in `pom.xml`:
```xml 
<dependency>
  <groupId>io.github.mickle-ak.mockobor</groupId>
  <artifactId>mockobor</artifactId>
  <version>1.0.2</version>
  <scope>test</scope>
</dependency>
```

### gradle

in `build.gradle.kts`:
```kotlin
testImplementation("io.github.mickle-ak.mockobor:mockobor:1.0.2")
```


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

[maven-central-mockobor]: https://central.sonatype.com/search?smo=true&q=mockobor
