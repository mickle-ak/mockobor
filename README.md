# Mockobor

**Moc**ked **Ob**servable **O**bse**r**vation - library to simplifying some aspects of unit testing
with java.

If you write a lot of unit tests for a big java enterprise application, you can see, that some
problems come again and again - you offen need to:

1. simulate sending of events (via java listeners) from mocked collaborator to tested object,
1. check completely deregistration of listeners registered by mocked collaborator,
1. collect and verify events sent from test object
1. synchrone start of `Feature`, `SwingWorker` etc. to simpler verify of result of (in production
   asynchrone) invocations.

And all of this should be done without changing of production code.

Of course, it can be done manually, but with Mockobor it is simpler, faster and needs less implementation details in
your tests.

- [Dependencies](#Dependencies)
- [Usage](#Usage)
- [Examples](#examples)
  - [simulate sending of events from mocked collaborator to tested object](#simulate-sending-of-events-from-mocked-collaborator-to-tested-object)
  - [check completely deregistration of listeners](#check-completely-deregistration-of-listeners)
  - [collect and verify events from test object](#collect-and-verify-events-from-test-object)
  - [synchrone start of asynchrone subprocesses](#synchrone-start-of-asynchrone-subprocesses)
- [Extension](#Extension)
  - [Custom listener detector](#Custom-listener-detector)
  - [Another mocking tool](#another-mocking-tool)
- [Restrictions](#Restrictions)
- [Installation](#Installation)
  - [maven](#maven)
  - [gradle](#gradle)
- [Changelog](#Changelog)


## Dependencies

Mockobor propagates no dependencies.

To use Mockobor in your unit tests, you need to:
- start your test with _java 8+_
- use at least one of follow mocking tools in your tests:
  - _Mockito 2.20.1+_
  - _EasyMock 3.4+_


## Usage

### listen for mocked observable object + check completely deregistration of listeners

To simulate sending of events (via java listeners) from mocked collaborator to tested object, Mockobor creates for
mocked observable object(s) a special notifier object:
```java
ListenersNotifier notifier=Mockobor.createNotifierFor(mockedObservableObject)
```
_Note: It is important, that your test object listens for the same mocked observable object as specified by invocation
of `Mockobor.createNotifierFor`._

This notifier object implements follow interfaces (depended on methods found in the specified mocked observable object):
- `ListenerNotifer` - always
- `XxxListener` (typical java style listener) if found methods like `addXxxListener(XxxListener)`
- `PropertyChangeNotifier` + `PropertyChangeListener` if found methods like `addPropertyChangeListener
  (PropertyChangeListener)`
- `ObservableNotifier` + `Observer` if found methods like `addObserver(Observer)`

This notifier object can be used to
- send events to test object:
  + using `notifierFor` method: `notifier.notifierFor(XxxListener.class).<listner method>(arguments)`
  + or direct using listener interface: `((XxxListener) notifier).<listner method>(arguments)`
  + using `PropertyChangeNotifier` (if suitable): `((PropertyChangeNotifier) notifier).firePropertyChange(...)`
  + using `ObservableNotifier`  (if suitable): `((ObservableNotifier) notifier).notifyObservers(...)`
- check completely deregistration of listeners
  + `Mockobor.assertThatAllListenersAreUnregistered( notifier1, ..., notifierN )`
  + or for single notifier `assertThat( notifier.allListenersAreUnregistered() ).isTrue()`

For more details see JavaDoc for `Mockobor`, `Mocobor.createNotifierFor`, `ListenerNotifer`,
`Mockobor.assertThatAllListenersAreUnregistered` and [Examples](#examples) bellow.


#### listener selectors

Sometimes listener should/can be registered not for all events, but only for qualified events (the best example is
`java.beans.PropertyChangeSupport.addPropertyChangeListener(String propertyName, PropertyChangeListener listener)` -
here `propertyName` is a qualifier). In such cases, Mockobor uses `selector`. It recognises 'selector'-arguments in
registration methods and allows adding selectors by sending of notifications:
```java
        ...
        // in production code - object under test registers its listener
        observable.addMyListener("q1","q2",listner1);  // ("q1", "q2") is selector here
        observable.addMyListener("q3",listener2); // "q3" is selector here
        observable.addMyListener(listener3); // here is selector empty
        ...
        // somewhere in tests send notification only to qualified listener in object under test
        notifier.notiferFor(listner1.class,selector("q1","q2")).listener_method();

        // same as notifier.notifierFor(listener2.class,selector("q3")).listener_method()
        notifier.notiferFor("q3",listner2.class).listener_method();

        // same as notifier.notifierFor(listener3.class,selector()).listener_method()
        notifier.notiferFor(listner3.class).listener_method();
        ...
```
For more detail see [Examples / typical java style listeners](#typical-java-style-listeners)


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
  MockoborContext.updateNotifierSettings()
        .ignoreListenersInterfaces()
        .lenientListenerListCheck();
```
or for one creation only:
```java
  ListenersNotifier notifier=Mockobor.createNotifierFor(
        mockedObservable,
        Mockobor.notifierSettings().ignoreListenersInterfaces().lenientListenerListCheck();
```
For more detail see [Examples / NotifierSettings](#notifiersettings)



### collect and verify events from test object
(not implemented jet, probably in version 1.1)



### synchrone start of asynchrone subprocesses
(not implemented jet, probably in version 1.2)



## Examples

### simulate sending of events from mocked collaborator to tested object

Given you have follow classes in your java application.

- your class under test, that adds (for example in constructor) some listener to the specified observable object und
  removes them in another method (for example in `destroy()`):
```java
/** Object that you want to test. */
public class TestedObserver {

  private final MockedObservable       mockedObservable;
  private final Observer               observer               = new ObserverIml();
  private final PropertyChangeListener propertyChangeListener = new PropertyChangeListenerImpl();
  private final MyListener             myListener             = new MyListenerImpl();
  private final MyAnotherListener      myAnotherListener      = new MyAnotherListenerImpl();

  /** It registers some listeners by the specified (in tests - mocked) observable object. */
  public TestedObserver( MockedObservable mockedObservable ) {
    this.mockedObservable = mockedObservable;
    mockedObservable.addObserver( observer );
    mockedObservable.addPropertyChangeListener( "prop", propertyChangeListener );
    mockedObservable.addTwoListeners( myListener, myAnotherListener );
    mockedObservable.addMyListener( "sel", myListener );
  }

  /** And removes all listeners on destroy. */
  public void destroy() {
    mockedObservable.deleteObserver( observer );
    mockedObservable.removePropertyChangeListener( "prop", propertyChangeListener );
    mockedObservable.removeTwoListeners( myListener, myAnotherListener );
    mockedObservable.removeMyListener( "sel", myListener );
  }
}
```
_Note: It is not strictly necessary, to register listeners using direct invocation of addXxxListener methods, it can be
any kind of registration - using annotations, aspects or other mechanisms. Important is, that the registration methods
of observable object will be invoked somewhere in the end._

- a collaborator of the tested object, that fires some events to registered listeners:
```java
/** Test-interface to simulate mocked object with ability to register listeners/observers. */
public interface MockedObservable {

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

In tests, we mock the collaborator (`MockedObservable`) using one of supported mocking tools
(see [Dependencies](#Dependencies)) and create a notifier object (`ListenersNotifier`), used to send events:
```java
class TestedObserver_Test {
  // create mock of MockedObservable 
  private final MockedObservable mockedObservable = Mockito.mock( MockedObservable.class );

  // create notifier for MockedObservable
  private final ListenersNotifier notifier = Mockobor.createNotifierFor( mockedObservable );

  // Object under tested. It registers listeners by the specified MockedObservable object.
  private final TestedObserver testObject = new TestedObserver( mockedObservable );
}
```

#### typical java style listeners

How you can see, `MockedObservable` uses (inter alia) typical java style listeners like `MyListener` with registration
methods like `addMyListener` and `removeMyListener`. Mockobor considers such classes as "use typical java listener"
and creates a base notifier - `ListenersNotifier` - to send events.

Now (to simulate processes in `MockedObservable`) we will send events to listener `MyListener` and
`MyAnotherListener` registered in constructor of `TestedObserver`:
```java
class TestedObserver_Test {
  private final MockedObservable  mockedObservable = Mockito.mock( MockedObservable.class );
  private final ListenersNotifier notifier         = Mockobor.createNotifierFor( mockedObservable );
  private final TestedObserver    testObject       = new TestedObserver( mockedObservable );

  @Test
  void testSendEventToJavaStyleListeners() {
    // send events to testObject (first way):
    ( (MyListener) notifier ).somethingChanged1( newValue );
    int answer1 = ( (MyListener) notifier ).somethingChanged2( newValue2 );
    ( (MyAnotherListener) notifier ).somethingOtherChanged( newValue3 );

    // send events to testObject (another way, it is exactly the same as above):
    notifier.notifierFor( MyListener.class ).somethingChanged1( newValue );
    int answer2 = notifier.notifierFor( MyListener.class ).somethingChanged2( newValue2 );
    notifier.notifierFor( MyAnotherListener.class ).somethingOtherChanged( newValue3 );

    // if you need to select listeners registered with certain qualifier 
    // (see TestedObserver's constructor: mockedObservable.addMyListener( "sel", myListener )),
    // then you can do it with "selector":
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
See
also [UsageExample_TypicalJavaListener_Test.java](https://github.com/mickle-ak/mockobor/blob/master/src/test/java/org/mockobor/mockedobservable/UsageExample_TypicalJavaListener_Test.java)


#### PropertyChangeListener

If your collaborator (observable object) has methods like
`void addPropertyChangeListener(PropertyChangeListener listener)` or
`void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)`, then it is considered as
"like PropertyChangeSupport" and Mockobor creates for such classes a special notifier - `PropertyChangeNotifier`:
```java
class TestedObserver_Test {
  private final MockedObservable  mockedObservable = Mockito.mock( MockedObservable.class );
  private final ListenersNotifier notifier         = Mockobor.createNotifierFor( mockedObservable );
  private final TestedObserver    testObject       = new TestedObserver( mockedObservable );

  @Test
  void testSendEventToPropertyChangeListeners() {
    // using PropertyChangeNotifier
    PropertyChangeNotifier propertyChangeNotifier = (PropertyChangeNotifier) notifier;
    propertyChangeNotifier.firePropertyChange( "prop", "o1", "n1" );
    propertyChangeNotifier.firePropertyChange( null, "o2", "n2" );

    // or direct using ListenersNotifier
    notifier.notifierFor( PropertyChangeListener.class )
            .propertyChange( new PropertyChangeEvent( mockedObservable, "p4", "o4", "n4" ) );

    notifier.notifierFor( PropertyChangeListener.class, selector(), selector( "prop" ) )
            .as( "exactly the same as propertyChangeNotifier.firePropertyChange( 'prop', 'o5', 'n5')" )
            .propertyChange( new PropertyChangeEvent( mockedObservable, "prop", "o5", "n5" ) );

    ( (PropertyChangeListener) notifier )
            .propertyChange( new PropertyChangeEvent( mockedObservable, "prop", "o3", "n3" ) );
  }
}
```
See
also [UsageExample_BeanPropertyChange_Test.java](https://github.com/mickle-ak/mockobor/blob/master/src/test/java/org/mockobor/mockedobservable/UsageExample_BeanPropertyChange_Test.java)


#### Observable, Observer

If your collaborator (observable object) has methods like `void addObserver(Observer observer)`, then it is considered
as "like Observable" and Mockobor creates for such classes a special notifier - `ObservableNotifier`:
```java
class TestedObserver_Test {
  private final MockedObservable  mockedObservable = Mockito.mock( MockedObservable.class );
  private final ListenersNotifier notifier         = Mockobor.createNotifierFor( mockedObservable );
  private final TestedObserver    testObject       = new TestedObserver( mockedObservable );

  @Test
  void testSendEventToPropertyChangeListeners() {
    // using ObservableNotifier
    ObservableNotifier observableNotifier = (ObservableNotifier) notifier;
    observableNotifier.notifyObservers();
    observableNotifier.notifyObservers( "v1" );

    // or direct using ListenersNotifier
    ( (Observer) notifier ).update( null, "v2" );
    notifier.notifierFor( Observer.class ).update( null, "v3" );
  }
}
```
See
also [UsageExample_Observable_Test.java](https://github.com/mickle-ak/mockobor/blob/master/src/test/java/org/mockobor/mockedobservable/UsageExample_Observable_Test.java)


### check completely deregistration of listeners

You can use Mockobor to check if all registered by mocked observable object listeners are unregistered:
```java
class TestedObserver_Test {
  private final MockedObservable  mockedObservable = Mockito.mock( MockedObservable.class );
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
See
also [UsageExample_allListenersAreUnregistered_Test.java](https://github.com/mickle-ak/mockobor/blob/master/src/test/java/org/mockobor/mockedobservable/UsageExample_allListenersAreUnregistered_Test.java)


### NotifierSettings

See [UsageExample_NotifierSettings_Test.java](https://github.com/mickle-ak/mockobor/blob/master/src/test/java/org/mockobor/mockedobservable/UsageExample_NotifierSettings_Test.java)



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

For more details see
javadoc, [Custom listener detector example](https://github.com/mickle-ak/mockobor/blob/master/src/test/java/org/mockobor/mockedobservable/UsageExample_CustomDetector_Test.java)
and [PropertyChangeDetector.java](https://github.com/mickle-ak/mockobor/blob/master/src/main/java/org/mockobor/mockedobservable/PropertyChangeDetector.java)
or
[TypicalJavaListenerDetector.java](https://github.com/mickle-ak/mockobor/blob/master/src/main/java/org/mockobor/mockedobservable/TypicalJavaListenerDetector.java)
as implementation examples.


### Another mocking tool

To redirect listener registration methods from mocked observable object to internal list of listeners, Mockobor needs,
first, to understand what a mocking tool was used to mock the specified mocked observable object and, second, to be able
to create redirection using the detected mocking tool.

Out of the box Mockobor supports some mocking tools (see [Dependencies](#Dependencies)). If you use another mocking
tool, it is possible to add support for them:
- create custom implementation of `ListenerRegistrationHandler`
- register it using `MockoborContext.registerListenerRegistrationHandler`

For more details see
javadoc, [ListenerRegistrationHandler .java](https://github.com/mickle-ak/mockobor/blob/master/src/main/java/org/mockobor/mockedobservable/mocking_tools/ListenerRegistrationHandler.java)
and [MockitoListenerRegistrationHandler.java](https://github.com/mickle-ak/mockobor/blob/master/src/main/java/org/mockobor/mockedobservable/mocking_tools/MockitoListenerRegistrationHandler.java)
or [EasymockListenerRegistrationHandler.java](https://github.com/mickle-ak/mockobor/blob/master/src/main/java/org/mockobor/mockedobservable/mocking_tools/EasymockListenerRegistrationHandler.java)
as implementation examples.



## Restrictions

- only interfaces accepted as listeners - you can't use methods like `addMyListener(MyListener)`, where `MyListener` has
  a class type - such methods will be not recognised as registration methods. `MyListener` can be an interface only.
  That is in fact standard practice in java.

- registration of array of listeners (as vararg too) is not supported - methods like
  `addMyListener(MyListener[] listeners)` or `addMyListener(MyListener... listeners)` will be not recognised as
  registration methods.

- It can have troubles by compiling with java 16+, because lombok actually has a
  [problem with Java 16](https://github.com/rzwitserloot/lombok/issues/2681). Compiled with java 8+ it runs with java 16
  without problems.


### _EasyMock_ restrictions

If you mock a collaborator object using _EasyMock_:

- a notifier object must be created (by calling `Mocobor.createNotifierFor`) before a tested object registers its
  listener by the mocked collaborator! It is necessary, because registration methods must be redirected to the notifier
  before they will be called from the tested object. As consequence, it is not possible to inject mocks to the observer
  test object using `@TestSubject` annotation.

- If listener registration methods of the mocked collaborator object have _varargs_ (
  like `addListener(MyListener l, Object...selector)`), then it can be problematic - during recording mode Mockobor
  can't forecast how many arguments will be used by real invocation of such method, therefore recorded and real
  invocations don't have to match (see [here](https://github.com/easymock/easymock/issues/130)).


## Installation

!!! **Not deployed yet** !!! Probably in a few days.

To use the Mockobor in your unit tests, use this dependency entry:

### maven

in `pom.xml`:
```xml 
<dependency>
  <groupId>io.github.mickle-ak.mockobor</groupId>
  <artifactId>mockobor</artifactId>
  <version>1.0</version>
  <scope>test</scope>
</dependency>
```

### gradle

in `build.gradle.kts`:
```kotlin
testImplementation( "io.github.mickle-ak.mockobor:mockobor:1.0" )
```

## Changelog

- **1.0** (??.0?.2021)
  - simulation of sending of events from mocked collaborator to tested object
  - take over listeners registered before notifier object created (Mockito only)
  - checking of completely deregistration of listeners
  - listener notifier settings
  - support for Mockito and EasyMock
