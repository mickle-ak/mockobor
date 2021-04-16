package org.mockobor.utils.reflection.mockito;

import lombok.NonNull;
import org.mockito.plugins.PluginSwitch;


/**
 * To check if Mockito's inline mock maker is enabled.
 * <p>
 * Mockito's inline mock maker (against standard mock maker) allows to mock of final classes/methods and static classes.
 * But is incubating as yet.
 * <p>
 * Per default (in this tests) inline mock maker is already configured.
 * To configure Mockito (in this tests) to use standard mock maker instead of inline set system property "mockito-mock-maker" to "standard".
 */
public class MockMakerSwitch implements PluginSwitch {

	@Override
	public boolean isEnabled( @NonNull String pluginClassName ) {
		if( !pluginClassName.equals( "mock-maker-inline" ) ) return true; // allow other plugins

		boolean standardMockMaker = "standard".equalsIgnoreCase( System.getProperty( "mockito-mock-maker" ) );
		System.out.println( "\n\n\n*** use Mockito-Mock-Maker: " + ( standardMockMaker ? "standard" : "inline" ) + "\n\n" );
		return !standardMockMaker;
	}
}
