package Code.Logic;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import Code.Data.FileParsing;
import Code.Data.Media;

/** A class that represents a library of media. <ul>
 * <p> Use {@link #readFiles(String, String)} to read the media from the given files.
 * <p> Use {@link #getLibrary()} to get the library of media.
 * <p> Use {@link #search(String)} to search for media.
 * <p> Use {@link #add(Media)} to add media to the library.
 * <p> Use {@link #remove(Media)} to remove media from the library.
 */
public class MediaLibrary {

    private Set<Media> library = null;
    private MediaSearching.SearchCache searchResult = new MediaSearching.SearchCache();

    /** Creates an empty media library.*/
    public MediaLibrary() {}

    /** Creates a new media library that contains all media in the given files.
     * @param filePathMovies The path to the file containing movies.
     * @param filePathSeries The path to the file containing series.
     * @throws IOException If the files could not be read.
     * @throws FileParsing.InvalidStringFormatException If the files are not formatted correctly.
     */
    public MediaLibrary(String filePathMovies, String filePathSeries) throws IOException, FileParsing.InvalidStringFormatException {
        Media[] mediaArray = FileParsing.parseFiles(filePathMovies, filePathSeries);
        library = Stream.of(mediaArray).filter(m -> m != null).collect(Collectors.toSet());
    }

    /** Re-reads the media files and updates the media library, and clears the search cache.
     * @param filePathMovies The path to the file containing movies.
     * @param filePathSeries The path to the file containing series.
     * @throws IOException If the files could not be read.
     * @throws FileParsing.InvalidStringFormatException If the files are not formatted correctly.
     */
    public void readFiles(String filePathMovies, String filePathSeries) throws IOException, FileParsing.InvalidStringFormatException {
        Media[] mediaArray = FileParsing.parseFiles(filePathMovies, filePathSeries);
        library = Stream.of(mediaArray).filter(m -> m != null).collect(Collectors.toSet());
        searchResult.clear();
    }

    /** Returns the media that matches the given query.
     * Searches by title and category, <i>case insensitive</i>.
     * @param query The query to search for.
     * @return A set of media that matches the given query.
     */
    public Set<Media> search(String query) {
        return MediaSearching.searchByQueries(query.split(" "), library, searchResult, false);
    }

    /** Returns the media that matches the most of the given queries.
     * Searches by title and category, <i>case insensitive</i>.
     * @param query The query to search for.
     * @return A set of media that matches the given query.
     */
    public Optional<Media> searchOne(String query) {
        return MediaSearching.searchByQueriesTop(query.split(" "), library, searchResult, false);
    }

    /** Adds the given media to the library, and clears the search cache.
     * @param media The media to add.
     */
    public void add(Media media) {
        library.add(media);
        searchResult.clear();
    }

    /** Adds all media in the given set to the library, and clears the search cache.
     * @param media The set of media to add.
     */
    public void addAll(Set<Media> media) {
        library.addAll(media);
        searchResult.clear();
    }

    /** Removes the given media from the library, and clears the search cache.
     * @param media The media to remove.
     */
    public void remove(Media media) {
        library.remove(media);
        searchResult.clear();
    }

    /** Clears the library, and clears the search cache. */
    public void removeAll() {
        library.clear();
        searchResult.clear();
    }
}