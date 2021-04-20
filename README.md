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
- [Examples](#Examples)
  - [simulate sending of events from mocked collaborator to tested object](#simulate-sending-of-events-from-mocked-collaborator-to-tested-object)
  - [check completely deregistration of listeners](#check-completely-deregistration-of-listeners)
  - [collect and verify events from test object](#collect-and-verify-events-from-test-object)
  - [synchrone start of asynchrone subprocesses](#synchrone-start-of-asynchrone-subprocesses)
  - [Other examples](#other-examples)
- [Extension](#Extension)
  - [Custom listeners](#custom-listeners)
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
  - _Mockito 2.1.0+_ (Mockobor itself uses 3.8.0)
  - _EasyMock 3.4+_ (Mockobor itself uses 4.3)


## Usage

Create Notifier, returned interfaces, special support for beans `PropertyChangeListener` and `Observer` ...

...

...


## Examples

Given you have follow classes in your java application.

- your class under test, that adds (for example in constructor) some listener to the specified observable object und
  removes they in another methods (for example in `destroy()`):
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

### simulate sending of events from mocked collaborator to tested object

In tests, we mock the collaborator (`MockedObservable`) using one of supported mocking tools
(see [Dependencies](#Dependencies)) and create a special notifier object, used to send events:
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

#### java style listeners

How you can see, `MockedObservable` uses (inter alia) typical java style listeners like `MyListener` with registration
methods like `addMyListener` and `removeMyListener`.

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

    // if you need to select listeners registered with certain values 
    // (see TestedObserver's constructor: mockedObservable.addMyListener( "sel", myListener )),
    // then you can do it with "selector":
    notifier.notifierFor( "sel", MyListener.class ).somethingChanged1( newValue );
    notifier.notifierFor( MyListener.class, selector( "sel" ) ).somethingChanged1( newValue ); // exactly as above

    // to notify (send the same event to) listeners registered with at least on of the specified selectors 
    // (here - without selector OR with "sel" as selector):
    notifier.notifierFor( MyListener.class, selector(), selector( "sel" ) )
            .somethingChanged1( newValue );
  }
}
```

#### PropertyChangeListener

For `PropertyChangeListener` Mockobor has special support - `PropertyChangeNotifier`:
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

#### Observable / Observer

For `Observable`/`Observer` Mockobor has special support - `ObservableNotifier`:
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
    assertThat( notifier.allListenersAreUnregistered() ).as( "all listeners are unregistered" ).isTrue();
  }
}
```

### collect and verify events from test object
(not implemented jet, probably in version 1.1)


### synchrone start of asynchrone subprocesses
(not implemented jet, probably in version 1.2)


### Other examples

Running examples can be found here:
- [Typical java style listeners example](https://github.com/mickle-ak/mockobor/blob/master/src/test/java/org/mockobor/mockedobservable/UsageExample_TypicalJavaListener_Test.java)
- [BeanPropertyChange example](https://github.com/mickle-ak/mockobor/blob/master/src/test/java/org/mockobor/mockedobservable/UsageExample_BeanPropertyChange_Test.java)
- [Observable example](https://github.com/mickle-ak/mockobor/blob/master/src/test/java/org/mockobor/mockedobservable/UsageExample_Observable_Test.java)
- [Custom listener detector example](https://github.com/mickle-ak/mockobor/blob/master/src/test/java/org/mockobor/mockedobservable/UsageExample_CustomDetector_Test.java)



## Extension

### Custom listeners

### Another mocking tool




## Restrictions

- to simulate sending of events from mocked collaborator to tested object, the notifier object must be created (by
  calling `Mocobor.createNotifierFor`) before tested object registers its listener by mocked collaborator! It is
  necessary, because registration methods must be redirected to notifier before they will be called from tested object.
  As consequence, it is not possible to inject mocks using `@InjectMocks` annotation. In future versions it can be
  changed.

- only interfaces accepted as listeners - you can't use methods like `addMyListener(MyListener)`, where `MyListener` has
  a class type - such methods will be not recognised as registration methods. `MyListener` can be only an interface.
  That is in fact standard practice in java.

- registration of array of listeners (as vararg too) is not supported - methods like
  `addMyListener(MyListener[] listeners)` or `addMyListener(MyListener... listeners)` will be not recognised as
  registration methods. Is it necessary at all?

- It can have troubles by compiling with java 16+, because lombok actually has a
  [problem with Java 16](https://github.com/rzwitserloot/lombok/issues/2681). Compiled with java 8+ it runs with java 16
  without problems.

- If you mock collaborator with _EasyMock_ and its listener registration methods have _varargs_
  (like `addListener(MyListener l, Object...selector)`), then it can be problematic - during recording mode Mockobor
  can't forecast how many arguments will be used by real invocation of such method, therefore recorded and real
  invocations don't have to match (see [here](https://github.com/easymock/easymock/issues/130)). Probably it is only
  needed to write custom argument matcher for varargs, but I don't know, is it possible at all, and if I'll do it
  sometimes. Patches (here for `EasymockListenerRegistrationHandler`) are welcome! ;-).



## Installation

!!! **Not deployed yet** !!! Probably in a few days.

To use the Mockobor in your unit tests, use this dependency entry:

### maven

in your `pom.xml`:
```xml 
<dependency>
  <groupId>io.github.mickle-ak.mockobor</groupId>
  <artifactId>mockobor</artifactId>
  <version>1.0</version>
  <scope>test</scope>
</dependency>
```

### gradle

in your `build.gradle.kts`:
```
testImplementation( "io.github.mickle-ak.mockobor:mockobor:1.0" )
```

## Changelog

- **1.0** (??.0?.2021)
  - simulation of sending of events from mocked collaborator to tested object
  - checking of completely deregistration of listeners
  - support of Mockito and EasyMock

