package org.mockobor.utils.reflection;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.reflect.Array;
import java.time.Duration;
import java.time.Period;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;


/**
 * To create default values for java types.
 */
@NoArgsConstructor( access = AccessLevel.PRIVATE )
public final class TypeUtils {

	private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = new HashMap<>();

	private static final Map<Class<?>, Object> DEFAULTS_AS_VALUES = new HashMap<>();

	private static final Map<Class<?>, Supplier<?>> DEFAULT_AS_SUPPLIER = new HashMap<>();


	/**
	 * To get the default value for the specified class.
	 * <p>
	 * It works correct for primitive types.
	 * <p>
	 * For string, it returns empty string.
	 * <p>
	 * For collections - empty modifiable collection.
	 *
	 * @param clazz class to get default value
	 * @param <T>   class to get default value
	 * @return default value for the specified class
	 */
	@SuppressWarnings( "unchecked" )
	public static <T> @Nullable T getDefaultReturnValue( Class<T> clazz ) {

		if( PRIMITIVE_DEFAULTS.containsKey( clazz ) ) {
			return (T) PRIMITIVE_DEFAULTS.get( clazz );
		}

		if( DEFAULTS_AS_VALUES.containsKey( clazz ) ) {
			return (T) DEFAULTS_AS_VALUES.get( clazz );
		}

		if( DEFAULT_AS_SUPPLIER.containsKey( clazz ) ) {
			return (T) DEFAULT_AS_SUPPLIER.get( clazz ).get();
		}

		// arrays -> return an empty array
		if( clazz.isArray() ) return (T) Array.newInstance( clazz.getComponentType(), 0 );

		// another collection type - try to do the best possible :-)
		if( Set.class.isAssignableFrom( clazz ) ) return (T) new HashSet<>();
		if( Map.class.isAssignableFrom( clazz ) ) return (T) new HashMap<>();
		if( Iterable.class.isAssignableFrom( clazz ) ) return (T) new ArrayList<>();
		if( Stream.class.isAssignableFrom( clazz ) ) return (T) Stream.of();

		return null;
	}


	static {
		// primitive types
		putPrimitive( Boolean.class, false );
		putPrimitive( Character.class, (char) 0 );
		putPrimitive( Byte.class, (byte) 0 );
		putPrimitive( Short.class, (short) 0 );
		putPrimitive( Integer.class, 0 );
		putPrimitive( Long.class, 0L );
		putPrimitive( Float.class, 0f );
		putPrimitive( Double.class, 0d );
		putPrimitive( boolean.class, false );
		putPrimitive( char.class, (char) 0 );
		putPrimitive( byte.class, (byte) 0 );
		putPrimitive( short.class, (short) 0 );
		putPrimitive( int.class, 0 );
		putPrimitive( long.class, 0L );
		putPrimitive( float.class, 0f );
		putPrimitive( double.class, 0d );

		// default values for some types
		putValue( String.class, "" );
		putValue( Duration.class, Duration.ZERO );
		putValue( Period.class, Period.ZERO );
		putValue( Optional.class, Optional.empty() );
		putValue( OptionalDouble.class, OptionalDouble.empty() );
		putValue( OptionalLong.class, OptionalLong.empty() );
		putValue( OptionalInt.class, OptionalInt.empty() );

		//  speed-up for common types
		putValue( Object.class, null );
		putValue( Void.class, null );
		putValue( void.class, null );

		// collections
		putSupplier( Iterable.class, ArrayList::new );
		putSupplier( Collection.class, LinkedList::new );
		putSupplier( List.class, LinkedList::new );
		putSupplier( AbstractCollection.class, ArrayList::new );
		putSupplier( AbstractList.class, ArrayList::new );
		putSupplier( ArrayList.class, ArrayList::new );
		putSupplier( AbstractSequentialList.class, LinkedList::new );
		putSupplier( LinkedList.class, LinkedList::new );
		putSupplier( Queue.class, LinkedList::new );
		putSupplier( Deque.class, LinkedList::new );
		putSupplier( AbstractQueue.class, PriorityQueue::new );
		putSupplier( PriorityQueue.class, PriorityQueue::new );
		putSupplier( Set.class, HashSet::new );
		putSupplier( SortedSet.class, TreeSet::new );
		putSupplier( NavigableSet.class, TreeSet::new );
		putSupplier( AbstractSet.class, TreeSet::new );
		putSupplier( TreeSet.class, TreeSet::new );
		putSupplier( HashSet.class, HashSet::new );
		putSupplier( LinkedHashSet.class, LinkedHashSet::new );
		putSupplier( LinkedHashMap.class, LinkedHashMap::new );
		putSupplier( WeakHashMap.class, WeakHashMap::new );
		putSupplier( Map.class, HashMap::new );
		putSupplier( SortedMap.class, TreeMap::new );
		putSupplier( NavigableMap.class, TreeMap::new );
		putSupplier( TreeMap.class, TreeMap::new );
		putSupplier( HashMap.class, HashMap::new );

		// stream
		putSupplier( Stream.class, Stream::of );
		putSupplier( DoubleStream.class, DoubleStream::of );
		putSupplier( LongStream.class, LongStream::of );
		putSupplier( IntStream.class, IntStream::of );

		// concurrent collections
		putSupplier( BlockingQueue.class, LinkedTransferQueue::new );
		putSupplier( TransferQueue.class, LinkedTransferQueue::new );
		putSupplier( LinkedTransferQueue.class, LinkedTransferQueue::new );
		putSupplier( BlockingDeque.class, LinkedBlockingDeque::new );
		putSupplier( LinkedBlockingDeque.class, LinkedBlockingDeque::new );
		putSupplier( LinkedBlockingQueue.class, LinkedBlockingQueue::new );
		putSupplier( CopyOnWriteArrayList.class, CopyOnWriteArrayList::new );
		putSupplier( CopyOnWriteArraySet.class, CopyOnWriteArraySet::new );
		putSupplier( DelayQueue.class, DelayQueue::new );
		putSupplier( SynchronousQueue.class, SynchronousQueue::new );
		putSupplier( PriorityBlockingQueue.class, PriorityBlockingQueue::new );
		putSupplier( ConcurrentMap.class, ConcurrentHashMap::new );
		putSupplier( ConcurrentHashMap.class, ConcurrentHashMap::new );
		putSupplier( ConcurrentNavigableMap.class, ConcurrentSkipListMap::new );
		putSupplier( ConcurrentSkipListMap.class, ConcurrentSkipListMap::new );
		putSupplier( ConcurrentSkipListSet.class, ConcurrentSkipListSet::new );
		putSupplier( ConcurrentLinkedQueue.class, ConcurrentLinkedQueue::new );
		putSupplier( ConcurrentLinkedDeque.class, ConcurrentLinkedDeque::new );

		// old collections
		putSupplier( Vector.class, Vector::new );
		putSupplier( Stack.class, Stack::new );
		putSupplier( Dictionary.class, Hashtable::new );
		putSupplier( Hashtable.class, Hashtable::new );
		putSupplier( Properties.class, Properties::new );
	}

	private static <T> void putPrimitive( Class<T> clazz, T defaultValue ) {
		PRIMITIVE_DEFAULTS.put( clazz, defaultValue );
	}

	private static <T> void putValue( Class<T> clazz, T defaultValue ) {
		DEFAULTS_AS_VALUES.put( clazz, defaultValue );
	}

	private static <T> void putSupplier( Class<T> clazz, Supplier<T> defaultValueSupplier ) {
		DEFAULT_AS_SUPPLIER.put( clazz, defaultValueSupplier );
	}
}
