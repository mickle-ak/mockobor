package org.mockobor.mockedobservable;

import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockobor.Mockobor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;


class Mockito_ListenerNotifier_Test {

    private static class ClassUnderTest {

        @Getter
        private final List<PropertyChangeEvent> invocations = new ArrayList<>();

        public ClassUnderTest( PropertyChangeSupport observable ) {
            observable.addPropertyChangeListener( invocations::add );
        }
    }


    @Test
    void createNotifierFor() {
        PropertyChangeSupport mockedObservable = Mockito.mock( PropertyChangeSupport.class );
        PropertyChangeNotifier notifierFor = (PropertyChangeNotifier) Mockobor.createNotifierFor( mockedObservable );
        ClassUnderTest classUnderTest = new ClassUnderTest( mockedObservable );

        notifierFor.firePropertyChange( "prop", "o1", "n1" );

        assertThat( classUnderTest.getInvocations() )
                .hasSize( 1 )
                .extracting( PropertyChangeEvent::getPropertyName, PropertyChangeEvent::getOldValue, PropertyChangeEvent::getNewValue )
                .containsExactly( tuple( "prop", "o1", "n1" ) );
    }
}
