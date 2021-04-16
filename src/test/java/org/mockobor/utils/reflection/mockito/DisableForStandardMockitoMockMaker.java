package org.mockobor.utils.reflection.mockito;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * To signal that the annotated test class or test method is disabled if Mockito configured to use standard mock maker.
 *
 * @see MockMakerSwitch
 */
@Target( { ElementType.TYPE, ElementType.METHOD } )
@Retention( RetentionPolicy.RUNTIME )
@DisabledIfSystemProperty( named = "mockito-mock-maker", matches = "standard" )
public @interface DisableForStandardMockitoMockMaker {}
