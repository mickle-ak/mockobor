package org.mockobor.utils.reflection;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Period;
import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockobor.utils.reflection.TypeUtils.getDefaultReturnValue;


class TypeUtilsTest {


	// ==================================================================================
	// =============================== getDefaultValue ==================================
	// ==================================================================================

	@Test
	void defaultValue_primitive() {
		assertThat( getDefaultReturnValue( byte.class ) ).isZero();
		assertThat( getDefaultReturnValue( short.class ) ).isZero();
		assertThat( getDefaultReturnValue( int.class ) ).isZero();
		assertThat( getDefaultReturnValue( long.class ) ).isZero();
		assertThat( getDefaultReturnValue( char.class ) ).isEqualTo( (char) 0 );
		assertThat( getDefaultReturnValue( float.class ) ).isZero();
		assertThat( getDefaultReturnValue( double.class ) ).isZero();
		assertThat( getDefaultReturnValue( boolean.class ) ).isFalse();
		assertThat( getDefaultReturnValue( void.class ) ).isNull();
		assertThat( getDefaultReturnValue( Byte.class ) ).isZero();
		assertThat( getDefaultReturnValue( Short.class ) ).isZero();
		assertThat( getDefaultReturnValue( Integer.class ) ).isZero();
		assertThat( getDefaultReturnValue( Long.class ) ).isZero();
		assertThat( getDefaultReturnValue( Character.class ) ).isEqualTo( (char) 0 );
		assertThat( getDefaultReturnValue( Float.class ) ).isZero();
		assertThat( getDefaultReturnValue( Double.class ) ).isZero();
		assertThat( getDefaultReturnValue( Boolean.class ) ).isFalse();
		assertThat( getDefaultReturnValue( Void.class ) ).isNull();
	}

	@Test
	void defaultValue_flat_objects() {
		assertThat( getDefaultReturnValue( String.class ) ).isEmpty();
		assertThat( getDefaultReturnValue( Duration.class ) ).isEqualTo( Duration.ZERO );
		assertThat( getDefaultReturnValue( Period.class ) ).isEqualTo( Period.ZERO );

		assertThat( (Optional<?>) getDefaultReturnValue( Optional.class ) ).isNotPresent();
		assertThat( getDefaultReturnValue( OptionalDouble.class ) ).isNotPresent();
		assertThat( getDefaultReturnValue( OptionalLong.class ) ).isNotPresent();
		assertThat( getDefaultReturnValue( OptionalInt.class ) ).isNotPresent();

		assertThat( getDefaultReturnValue( Object.class ) ).isNull();
		assertThat( getDefaultReturnValue( TypeUtils.class ) ).as( "null for 'unknown' classes" ).isNull();
	}

	@Test
	void defaultValue_arrays() {
		assertThat( getDefaultReturnValue( Object[].class ) ).isEqualTo( new Object[0] );
		assertThat( getDefaultReturnValue( String[].class ) ).isEqualTo( new String[0] );
		assertThat( getDefaultReturnValue( Integer[].class ) ).isEqualTo( new Integer[0] );
		assertThat( getDefaultReturnValue( int[].class ) ).isEqualTo( new int[0] );
	}

	@Test
	void defaultValue_collections() {
		assertThat( (Collection<?>) getDefaultReturnValue( Collection.class ) ).isEmpty();
		assertThat( (List<?>) getDefaultReturnValue( List.class ) ).isEmpty();
		assertThat( (List<?>) getDefaultReturnValue( LinkedList.class ) ).isEmpty();
		assertThat( (List<?>) getDefaultReturnValue( ArrayList.class ) ).isEmpty();
		assertThat( (Map<?, ?>) getDefaultReturnValue( Map.class ) ).isEmpty();
		assertThat( (Map<?, ?>) getDefaultReturnValue( HashMap.class ) ).isEmpty();
		assertThat( (Map<?, ?>) getDefaultReturnValue( TreeMap.class ) ).isEmpty();
		assertThat( (Set<?>) getDefaultReturnValue( Set.class ) ).isEmpty();
		assertThat( (Set<?>) getDefaultReturnValue( HashSet.class ) ).isEmpty();
		assertThat( (Iterable<?>) getDefaultReturnValue( Iterable.class ) ).isEmpty();
	}

	@SuppressWarnings( { "unchecked", "ConstantConditions" } )
	@Test
	void defaultValue_collections_are_mutable() {
		assertThatNoException().isThrownBy( () -> getDefaultReturnValue( LinkedList.class ).add( "" ) );
		assertThatNoException().isThrownBy( () -> getDefaultReturnValue( TreeMap.class ).put( "", new Object() ) );
	}

	@SuppressWarnings( "RedundantOperationOnEmptyContainer" )
	@Test
	void getDefaultValue_streams() {
		assertThat( ( (Stream<?>) getDefaultReturnValue( Stream.class ) ) ).isEmpty();
		assertThat( ( (Stream<?>) getDefaultReturnValue( new HashMap<String, Object>().entrySet().stream().getClass() ) ) ).isEmpty();
		assertThat( getDefaultReturnValue( IntStream.class ) ).isEmpty();
		assertThat( getDefaultReturnValue( LongStream.class ) ).isEmpty();
		assertThat( getDefaultReturnValue( DoubleStream.class ) ).isEmpty();
	}
}
