package org.incendo.nonerror;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.bukkit.Material;

public class MaterialNonErrorifier
{

    public static void main(final String[] args)
    {
      if ( args.length == 0 )
      {
        System.err.println( "No file specified..." );
        return;
      }
      final File file = new File( args[ 0 ] );
      if ( !file.exists() )
      {
        System.err.println( "There's no such file: " + file.getName() );
        return;
      }
      final Function<Material, String> transformer = material -> material.name().replaceFirst( "LEGACY_", "" );
      final Predicate<Material> filter = material ->
          material.name().contains( "LEGACY_" ) && !containsMaterial( transformer.apply( material ) );
      final MaterialNonErrorifier materialNonErrorifier = new MaterialNonErrorifier( filter );
      final Collection<String> illegalNames = materialNonErrorifier.fetchIllegalStrings( transformer );

      System.out.println( "Identified " + illegalNames.size() + " illegal names." );

      final String logFormat = "Line: %d contains illegal reference: %s" + System.lineSeparator();
      try ( final BufferedReader reader = new BufferedReader( new FileReader( file ) ) )
      {
        String line;

        int lineNum = 1;
        while ( ( line = reader.readLine() ) != null )
        {
          for ( final String illegalName : illegalNames )
          {
            if ( line.contains( illegalName ) ) // prevent ACACIA_LOG and LOG from firing
            {
              final int index = line.indexOf( illegalName );
              if ( index > 0 )
              {
                if ( line.charAt( index - 1 ) == '_' )
                {
                  continue;
                }
              }
              System.out.printf( logFormat, lineNum, illegalName );
            }
          }
          lineNum++;
        }
      } catch ( final Throwable throwable )
      {
        throwable.printStackTrace();
      }
    }

    private static boolean containsMaterial(final String name)
    {
      try
      {
        Material.valueOf( name );
      } catch ( final IllegalArgumentException e )
      {
        return false;
      }
      return true;
    }

    private final EnumSet<Material> illegalNames;

    private MaterialNonErrorifier(final Predicate<Material> filter)
    {
      final Collection<Material> materials = Arrays.stream( Material.values() )
          .filter( filter ).collect( Collectors.toList() );
      this.illegalNames = EnumSet.copyOf( materials );
    }

    private Collection<String> fetchIllegalStrings(final Function<Material, String> transformer)
    {
      return this.illegalNames.stream().map( transformer ).collect( Collectors.toList() );
    }

}
