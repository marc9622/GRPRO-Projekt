package Code.Data;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.*;

import Code.Data.MediaParsing.InvalidStringFormatException;
import static Code.Data.MediaParsing.parseStringToMedia;

public class Testing {
    
    @Nested
    class TestMediaParsing {

        @Test
        void ignore() throws InvalidStringFormatException {
            Media expected = null;
            Media actual = parseStringToMedia("// This is a comment");

            assertEquals(expected, actual);
        }

        // Movies

        @Test
        void movieGeneral() throws InvalidStringFormatException {
            Object expected = new Movie("The Matrix", 1999, new Media.Category[] {Media.Category.Action, Media.Category.SciFi}, 8.7f);
            Object actual = parseStringToMedia("The Matrix; 1999; Action, Sci-fi; 8.7;");

            assertEquals(expected, actual);
        }

        // Series

        @Test
        void serieGeneral() throws InvalidStringFormatException {
            Object expected = new Serie("The Office", 2005, true, 2013, new Media.Category[] {Media.Category.Comedy}, 8.9f, new int[] {6, 22, 25, 19, 28, 26, 26, 24, 25});
            Object actual = parseStringToMedia("The Office; 2005-2013; Comedy; 8.9; 1-6, 2-22, 3-25, 4-19, 5-28, 6-26, 7-26, 8-24, 9-25;");
            
            assertEquals(expected, actual);
        }

        @Test
        void serieStillRunningWithDash() throws InvalidStringFormatException {
            Object expected = new Serie("The Office", 2005, false, 0, new Media.Category[] {Media.Category.Comedy}, 8.9f, new int[] {6, 22, 25, 19, 28, 26, 26, 24, 25});
            Object actual = parseStringToMedia("The Office; 2005-; Comedy; 8.9; 1-6, 2-22, 3-25, 4-19, 5-28, 6-26, 7-26, 8-24, 9-25;");
            
            assertEquals(expected, actual);
        }

        @Test
        void serieStillRunningWithoutDash() throws InvalidStringFormatException {
            Object expected = new Serie("The Office", 2005, false, 0, new Media.Category[] {Media.Category.Comedy}, 8.9f, new int[] {6, 22, 25, 19, 28, 26, 26, 24, 25});
            Object actual = parseStringToMedia("The Office; 2005; Comedy; 8.9; 1-6, 2-22, 3-25, 4-19, 5-28, 6-26, 7-26, 8-24, 9-25;");
            
            assertEquals(expected, actual);
        }
    
        // Exception testing

        @Test
        void invalidYear() {
            // Movie
            Exception exceptionMovie = assertThrows(InvalidStringFormatException.class, () -> {
                parseStringToMedia("The Matrix; 19a99; Action, Sci-fi; 8.7;");
            });

            // Serie
            Exception exceptionSerie = assertThrows(InvalidStringFormatException.class, () -> {
                parseStringToMedia("The Office; 20a05-20a13; Comedy; 8.9; 1-6, 2-22, 3-25, 4-19, 5-28, 6-26, 7-26, 8-24, 9-25;");
            });

            String expectedMessage = "Tried to parse Media, but could not parse year (int) from ";

            assertTrue(exceptionMovie.getMessage().startsWith(expectedMessage));
            assertTrue(exceptionSerie.getMessage().startsWith(expectedMessage));
        }

        @Test
        void invalidCategory() {
            // Movie
            Exception exceptionMovie = assertThrows(InvalidStringFormatException.class, () -> {
                parseStringToMedia("The Matrix; 1999; aAction; 8.7;");
            });

            // Serie
            Exception exceptionSerie = assertThrows(InvalidStringFormatException.class, () -> {
                parseStringToMedia("The Office; 2005-2013; ComedyA; 8.9; 1-6, 2-22, 3-25, 4-19, 5-28, 6-26, 7-26, 8-24, 9-25;");
            });

            String expectedMessage = "Tried to parse Media, but could not parse category from ";

            assertTrue(exceptionMovie.getMessage().startsWith(expectedMessage));
            assertTrue(exceptionSerie.getMessage().startsWith(expectedMessage));
        }

        @Test
        void invalidRating() {
            // Movie
            Exception exceptionMovie = assertThrows(InvalidStringFormatException.class, () -> {
                parseStringToMedia("The Matrix; 1999; Action, Sci-fi; 8a.7;");
            });

            // Serie
            Exception exceptionSerie = assertThrows(InvalidStringFormatException.class, () -> {
                parseStringToMedia("The Office; 2005-2013; Comedy; 8.a9; 1-6, 2-22, 3-25, 4-19, 5-28, 6-26, 7-26, 8-24, 9-25;");
            });

            String expectedMessage = "Could not parse rating (float) from ";

            assertTrue(exceptionMovie.getMessage().startsWith(expectedMessage));
            assertTrue(exceptionSerie.getMessage().startsWith(expectedMessage));
        }

        @Test
        void missingData() {
            // Movie
            Exception exceptionMovie = assertThrows(InvalidStringFormatException.class, () -> {
                parseStringToMedia("The Matrix; 1999; Action, Sci-fi;");
            });

            // Serie
            Exception exceptionSerie = assertThrows(InvalidStringFormatException.class, () -> {
                parseStringToMedia("The Office; 2005-2013; Comedy; 8.9;");
            });

            String expectedMessageMovie = "Tried to parse Media, but string ended prematurely.";
            String expectedMessageSerie = "Tried to parse Serie, but string ended prematurely.";

            assertTrue(exceptionMovie.getMessage().startsWith(expectedMessageMovie));
            assertTrue(exceptionSerie.getMessage().startsWith(expectedMessageSerie));
        }

        @Test
        void invalidSeasons() {
            // Serie
            Exception exceptionSerie = assertThrows(InvalidStringFormatException.class, () -> {
                parseStringToMedia("The Office; 2005-2013; Comedy; 8.9; 1a-6, 2-22, 3-25, 4-19, 5-28, 6-26, 7-26, 8-24, 9-25, 10-25;");
            });

            String expectedMessage = "Tried to parse Serie, but could not parse season from ";

            assertTrue(exceptionSerie.getMessage().startsWith(expectedMessage));
        }

        @Test
        void tooLong() {
            // Movie
            Exception exceptionMovie = assertThrows(InvalidStringFormatException.class, () -> {
                parseStringToMedia("The Matrix; 1999; Action, Sci-fi; 8.7; 8.7;");
            });

            // Serie
            Exception exceptionSerie = assertThrows(InvalidStringFormatException.class, () -> {
                parseStringToMedia("The Office; 2005-2013; Comedy; 8.9; 1-6, 2-22, 3-25, 4-19, 5-28, 6-26, 7-26, 8-24, 9-25; 1-6, 2-22, 3-25, 4-19, 5-28, 6-26, 7-26, 8-24, 9-25;");
            });

            String expectedMessageMovie = "Tried to parse Serie, but could not parse season and length from ";
            String expectedMessageSerie = "Tried to parse Serie, but string contained more characters than expected.";

            assertTrue(exceptionMovie.getMessage().startsWith(expectedMessageMovie));
            assertTrue(exceptionSerie.getMessage().startsWith(expectedMessageSerie));
        }

    }

    @Nested
    class TestFileSerialization {

        // Check hashcode

        @Test
        void movieHashCode() {
            Movie movie1 = new Movie("The Matrix", 1999, new Media.Category[] {Media.Category.Action, Media.Category.SciFi}, 8.7f);
            Movie movie2 = new Movie("The Matrix", 1999, new Media.Category[] {Media.Category.Action, Media.Category.SciFi}, 8.7f);
        
            assertEquals(movie1.hashCode(), movie2.hashCode());
        }

        // Check serialization

        @Test
        void movieSerialization() throws IOException, ClassNotFoundException {
            Movie movie = new Movie("The Matrix", 1999, new Media.Category[] {Media.Category.Action, Media.Category.SciFi}, 8.7f);

            FileSerialization.saveTo(movie, "./testMovie.txt");

            Object parsedMovie = FileSerialization.loadFrom("./testMovie.txt");

            assertEquals(movie, parsedMovie);
        }

        @Test
        void serieSerialization() throws IOException, ClassNotFoundException  {
            Serie serie = new Serie("The Office", 2005, true, 2013, new Media.Category[] {Media.Category.Comedy}, 8.9f, new int[] {6, 22, 25, 19, 28, 26, 26, 24, 25});

            FileSerialization.saveTo(serie, "./testSerie.txt");

            Object parsedSerie = FileSerialization.loadFrom("./testSerie.txt");

            assertEquals(serie, parsedSerie);
        }
        
        @Test
        void mediaSetSerialization() throws IOException, ClassNotFoundException  {
            MediaSet mediaSet = new MediaSet();
            mediaSet.add(new Movie("The Matrix", 1999, new Media.Category[] {Media.Category.Action, Media.Category.SciFi}, 8.7f));
            mediaSet.add(new Serie("The Office", 2005, true, 2013, new Media.Category[] {Media.Category.Comedy}, 8.9f, new int[] {6, 22, 25, 19, 28, 26, 26, 24, 25}));

            FileSerialization.saveTo(mediaSet, "./testMediaSet.txt");

            Object parsedMediaSet = FileSerialization.loadFrom("./testMediaSet.txt");

            assertEquals(mediaSet, parsedMediaSet);
        }

    }
}
