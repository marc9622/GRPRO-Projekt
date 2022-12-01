package Code.Data;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/** Effectively just functions as a namespace for functions that parse files.
 * <p> Use {@link #parseFiles(String, String)} to read parse movie and series files.
 * <p> Contains {@link #InvalidStringFormatException} for when a string is formatted incorrectly.
 */
public class MediaParsing {

    /** Prevents instantiation of this class.
     * This class only contains static methods,
     * and exists only because Java forces you to use classes,
     * even when it is unnecessary.
     */
    private MediaParsing () {}

    /** Reads and parses the given files.
     * <p> Lines must be written in the format specified at {@link #parseStringToMedia}.
     * @param filePathMovies The path to the file containing movies. Fx. {@code ".Data/film.txt"}.
     * @param filePathSeries The path to the file containing series. Fx. {@code ".Data/serier.txt"}.
     * @return All the movies and series as a set of media.
     * @throws IOException If an I/O error occurs trying to read from the file.
     * @throws InvalidStringFormatException If a line in the file is not written in the correct format.
     */
    public static Media[] parseFiles(String filePathMovies, String filePathSeries) throws IOException, InvalidStringFormatException {
        // Read all lines from files
        String[] linesMovies = readLinesFromFile(filePathMovies);
        String[] linesSeries = readLinesFromFile(filePathSeries);

        // Parse all lines to media
        return parseLinesToMedia(linesMovies, linesSeries);
    }

    /** Finds a file in the resources folder, and returns its contents as a String array.
     * The file is assumed to be encoded in {@code ISO-8859-1}.
     * @param filePath The name of the text file to be read.
     * @return An {@code ISO-8859-1} string array of the contents of the file separated by lines.
     * @throws IOException If an I/O error occurs trying to read from the file.
     */
    private static String[] readLinesFromFile(String filePath) throws IOException {
        String string = Files.readString(Path.of(filePath), StandardCharsets.ISO_8859_1);
        return string.split("\n");
    }

    /** Takes a string array of movies and one of series and parses them into an array of movies and series.
     * @param linesMovies The string array of movies.
     * @param linesSeries The string array of series.
     * @return An array of movies and series.
     * @throws InvalidStringFormatException If a string could not be parsed.
     */
    static Media[] parseLinesToMedia(String[]... lines) throws InvalidStringFormatException {
        Media[] media = new Media[Stream.of(lines).mapToInt(l -> l.length).sum()];

        for(int i = 0; i < lines.length; i++)
            for(int j = 0; j < lines[i].length; j++)
                media[i * lines[i].length + j] = parseStringToMedia(lines[i][j]);

        return media;
    }

    /** Takes a single string line and parses it into either a Movie or Serie.
     * <p> The line format for movies is:
     * <p> <code>title; releaseYear; category1, category2 ...; rating;</code>
     * <p> The line format for series is:
     * <p> <code>title; releaseYear-endYear; category1, category2 ...; rating; 1-season1Length, 2-season2Length ...;</code>
     * <p> Some of the given data from the assignment is actually formatted incorrectly.
     * In those cases, <code>//</code> at the start of a line is used to indicate that the line should be ignored.
     * @param string The string to be parsed.
     * @param isMovie Whether the string is a movie or a serie.
     * @return Either a Movie or Serie object. (Or null if the string is ignored.)
     * @throws InvalidStringFormatException If the string is not formatted correctly.
     */
    static Media parseStringToMedia(String string) throws InvalidStringFormatException {
        // If the line starts with "//", it is ignored.
        if(string.startsWith("//")) return null;

        // The string is parsed in one pass because it is faster.
        // This enum is used to keep track of the current parsing state.
        enum ParsingState { TITLE, RELEASE_YEAR, END_YEAR, CATEGORIES, RATING, SEASONS, DONE }
        ParsingState isParsing = ParsingState.TITLE;

        // These variables are used to store the parsed data.
        boolean knowItIsSerie = false; // This is unknown at this point, but it is set later.
        String title = null;
        int releaseYear = 0;
        boolean isEnded = false;
        int endYear = 0;
        List<Movie.Category> categories = new ArrayList<>(4);
        float rating = 0;
        List<Integer> seasonLengths = null;

        // Simply a index variable used to keep track of what the last parsed character was.
        int lastParsed = 0;

        // This loop parses the string.
        for(int i = 0; i < string.length() && isParsing != ParsingState.DONE; i++) {
            char c = string.charAt(i);
            boolean isSemicolon = c == ';';

            // Parse the title
            if(isParsing == ParsingState.TITLE) {
                if(!isSemicolon) continue;
                title = string.substring(0, i).strip();

                lastParsed = i + 1;
                isParsing = ParsingState.RELEASE_YEAR;
                continue;
            }

            // Parse the release year
            if(isParsing == ParsingState.RELEASE_YEAR) {

                // If we haven't reached a semicolon or hyphen, continue.
                if(!(isSemicolon || c == '-')) continue;

                // If we have reached a hyphen, we know it is a serie.
                if(!isSemicolon) knowItIsSerie = true;

                // Try to parse the release year.
                try {
                    releaseYear = Integer.parseInt(string.substring(lastParsed, i).strip());
                } catch (NumberFormatException e) {
                    throw new InvalidStringFormatException("Tried to parse Media, but could not parse year (int) from '" + string.substring(lastParsed, i).strip() + "'.", string);
                }

                // Update the last parsed index.
                lastParsed = i + 1;

                // If we reached a semicolon, then we parse the categories.
                if(isSemicolon) isParsing = ParsingState.CATEGORIES;
                
                // If we reached a hyphen, then we parse the end year.
                else isParsing = ParsingState.END_YEAR;
                
                continue;
            }

            // Parse the end year
            if(isParsing == ParsingState.END_YEAR) {
                if(!isSemicolon) continue;

                String endYearString = string.substring(lastParsed, i).strip();

                // Only parse the end year if it is not empty.
                if(!endYearString.isBlank()) {
                    isEnded = true;
                    try {
                        endYear = Integer.parseInt(endYearString);
                    } catch (NumberFormatException e) {
                        throw new InvalidStringFormatException("Tried to parse Media, but could not parse end year (int) from '" + endYearString + "'.", string);
                    }
                }

                lastParsed = i + 1;
                isParsing = ParsingState.CATEGORIES;
                continue;
            }
        
            // Parse the categories
            if(isParsing == ParsingState.CATEGORIES) {

                // If we haven't reached a semicolon or comma, continue.
                if(!isSemicolon && c != ',') continue;

                // Parse the category and add it to the list.
                String categoryString = string.substring(lastParsed, i).strip();
                Optional<Media.Category> category = Media.Category.fromString(categoryString);

                // If the category is present, add it to the list.
                if(category.isPresent()) categories.add(category.get());

                // If the category is not present, then it is invalid.
                else throw new InvalidStringFormatException("Tried to parse Media, but could not parse category from '" + categoryString + "'.", string);

                // Update the last parsed index.
                lastParsed = i + 1;

                // If we reached a semicolon, then we go parse the rating.
                if(isSemicolon)
                    isParsing = ParsingState.RATING;
                
                continue;
            }

            // Parse the rating
            if(isParsing == ParsingState.RATING) {
                if(c != ';') continue;

                try {
                    rating = Float.parseFloat(string.substring(lastParsed, i).strip().replace(",", "."));
                } catch (NumberFormatException e) {
                    throw new InvalidStringFormatException("Could not parse rating (float) from '" + string.substring(lastParsed, i).strip() + "'.", string);
                }

                // If the media didn't contain a hyphen after the release year,
                // then we still don't know if it is a movie or a series.
                // Therefore, we just continue to parsing the seasons.
                lastParsed = i + 1;
                isParsing = ParsingState.SEASONS;
                seasonLengths = new ArrayList<>(6);
                continue;
            }

            // Parse the season
            if(isParsing == ParsingState.SEASONS) {

                // If we haven't reached a semicolon or comma, continue.
                if(!isSemicolon && c != ',') continue;
                
                // Find the index of the hyphen.
                int hyphen = string.indexOf('-', lastParsed);
                if(hyphen == -1)
                    throw new InvalidStringFormatException("Tried to parse Serie, but could not parse season and length from '" + string.substring(lastParsed, i).strip() + "'.", string);
                
                // Parse the season and length and add it to the list.
                try {
                    // The season is the first part of the string. From lastParsed to hyphen.
                    int season = Integer.parseInt(string.substring(lastParsed, hyphen).strip());
                    if(season != seasonLengths.size() + 1)
                        throw new InvalidStringFormatException("Tried to parse Serie, but season numbers are not in order.", string);

                    // The length is the second part of the string. From hyphen+1 to i.
                    int seasonLength = Integer.parseInt(string.substring(hyphen + 1, i).strip());
                    seasonLengths.add(seasonLength);
                } catch (NumberFormatException numberFormat) {
                    throw new InvalidStringFormatException("Tried to parse Serie, but could not parse season from '" + string.substring(lastParsed, i).strip() + "'.", string);
                }

                lastParsed = i + 1;
                if(isSemicolon) {
                    isParsing = ParsingState.DONE;
                    break;
                }
                
                continue;
            }
        }

        // If we knew the media was a serie, and we didn't end in the DONE state,
        // then we know that the string was invalid.
        if(knowItIsSerie && isParsing != ParsingState.DONE)
            throw new InvalidStringFormatException("Tried to parse Serie, but string ended prematurely.", string);

        // If we didn't know the whether it was a movie or serie...
        if(!knowItIsSerie) {
            // and we didn't end in the DONE state...
            if(isParsing != ParsingState.DONE) {
                // and we didn't end in the SEASONS state either, then we know that the string was invalid.
                if(isParsing != ParsingState.SEASONS)
                    throw new InvalidStringFormatException("Tried to parse Media, but string ended prematurely.", string);
                
                // If we did end in the SEASONS state, but the seasons list is not empty, then it was an invalid serie.
                else if(!seasonLengths.isEmpty())
                    throw new InvalidStringFormatException("Tried to parse Serie, but string ended prematurely.", string);
                
                // Otherwise, we know it is a movie.
            }

            // If we ended in the DONE state, and the seasons list is not empty, then we know it is a series.
            else if(!seasonLengths.isEmpty()) {
                knowItIsSerie = true;
            }

            // Otherwise, we know it is a movie.
        }

        // *At this point, we are sure whether it is a movie or series.*

        // If the string is longer than expected, throw an exception.
        if(!string.substring(lastParsed).isBlank())
            throw new InvalidStringFormatException("Tried to parse " + (knowItIsSerie ? "Serie" : "Movie") + ", " +
                                                   "but string contained more characters than expected.", string);

        // Create the media object and return it.
        if(!knowItIsSerie) return new Movie(title, releaseYear, categories.toArray(Media.Category[]::new), rating);
        else               return new Serie(title, releaseYear, isEnded, endYear, categories.toArray(Media.Category[]::new), rating,
                                            seasonLengths.stream().mapToInt(i -> i).toArray());
    }

    /** Thrown when a string cannot be parsed to a movie or a serie.
     * Contains the string that could not be parsed.
     */
    public static class InvalidStringFormatException extends Exception {
        private final String fileString;
        public InvalidStringFormatException(String message, String fileString) {
            super(message + " String: '" + fileString + "'.");
            this.fileString = fileString;
        }
        public String fileString() {
            return fileString;
        }
    }
    
}