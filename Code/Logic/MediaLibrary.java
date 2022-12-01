package Code.Logic;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import Code.Data.MediaParsing;
import Code.Data.MediaSet;
import Code.Data.MediaParsing.InvalidStringFormatException;
import Code.Data.FileSerialization;
import Code.Data.Media;

/** A class that represents a library of media. <ul>
 * <p> Use {@link #readFiles(String, String)} to read the media from the given files.
 * <p> Use {@link #getMediaSet()} to get the library of media.
 * <p> Use {@link #search(String)} to search for media.
 * <p> Use {@link #add(Media)} to add media to the library.
 * <p> Use {@link #remove(Media)} to remove media from the library. </ul>
 */
public class MediaLibrary {

    private MediaSet library = null;
    private MediaSorting.SearchCache searchCache = new MediaSorting.SearchCache();

    /** Creates an empty media library.*/
    public MediaLibrary() {}

    /** Creates a new copy of the given media library */
    public MediaLibrary(MediaLibrary library) {
        this.library = (MediaSet) library.getMediaSet().clone();
    }

    /** Creates a new media library that contains all media in the given files.
     * @param filePathMovies The path to the file containing movies.
     * @param filePathSeries The path to the file containing series.
     * @throws IOException If the files could not be read.
     * @throws MediaParsing.InvalidStringFormatException If the files are not formatted correctly.
     */
    public static MediaLibrary readMediaLibrary(String filePathMovies, String filePathSeries) throws IOException, InvalidStringFormatException {
        MediaLibrary library = new MediaLibrary();
        library.readFiles(filePathMovies, filePathSeries);
        return library;
    }

    /** Loads a previously saved media library from the given file path.
     * @param filePath The path to the file containing the media library.
     * @return The media library.
     * @throws ClassNotFoundException If the file is not formatted correctly.
     * @throws IOException If the file could not be read.
     */
    public static MediaLibrary loadSavedMediaLibrary(String filePath) throws ClassNotFoundException, IOException {
        return (MediaLibrary) FileSerialization.loadFrom(filePath);
    }

    /** Re-reads the media files and updates the media library, and clears the search cache.
     * @param filePathMovies The path to the file containing movies.
     * @param filePathSeries The path to the file containing series.
     * @throws IOException If the files could not be read.
     * @throws MediaParsing.InvalidStringFormatException If the files are not formatted correctly.
     */
    public void readFiles(String filePathMovies, String filePathSeries) throws IOException, InvalidStringFormatException {
        Media[] mediaArray = MediaParsing.parseFiles(filePathMovies, filePathSeries);
        library = new MediaSet(Stream.of(mediaArray).filter(m -> m != null).collect(Collectors.toSet()));
        searchCache.clear();
    }

    /** Returns the media library sorted by the given search string.
     * Searches by title and category, <i>case insensitive</i>.
     * @param query The query to search for.
     * @param useCache Whether to use the search cache.
     * @param parallel Whether to use concurrent search.
     * @return A set of media that matches the given query.
     */
    public List<Media> sortBySearch(String query, boolean useCache, boolean parallel) {
        return sortBySearch(query, library.size(), useCache, parallel);
    }

    /** Returns the media library sorted by the given search string.
     * Searches by title and category, <i>case insensitive</i>.
     * @param query The query to search for.
     * @param count The maximum number of results to return. Best results are returned first.
     * @param useCache Whether to use the search cache.
     * @param parallel Whether to use concurrent search.
     * @return A set of media that matches the given query.
     */
    public List<Media> sortBySearch(String query, int count, boolean useCache, boolean parallel) {
        return MediaSorting.sortBySearchQueries(library, query.split(" "), searchCache, count, useCache, parallel);
    }

    /** Returns a sorted list of the library,
     * using a specified sorting method.
     * @param sortBy The property to sort by.
     * @param sortOrder The order to sort in.
     * @return The sorted list of media.
     */
    public List<Media> sortBy(MediaSorting.SortBy sortBy, MediaSorting.SortOrder sortOrder) {
        return MediaSorting.sortMedia(library, sortBy, sortOrder);
    }

    /** Adds the given media to the library, and clears the search cache.
     * @param media The media to add.
     */
    public void add(Media media) {
        library.add(media);
        searchCache.clear();
    }

    /** Adds all media in the given library to this library, and clears the search cache.
     * @param media The set of media to add.
     */
    public void addAll(MediaLibrary media) {
        library.addAll(media.getMediaSet());
        searchCache.clear();
    }

    /** Removes the given media from the library, and clears the search cache.
     * @param media The media to remove.
     */
    public void remove(Media media) {
        library.remove(media);
        searchCache.clear();
    }

    /** Clears the library, and clears the search cache. */
    public void removeAll() {
        library.clear();
        searchCache.clear();
    }

    /** Returns the library of media.
     * @return The library of media.
     */
    private MediaSet getMediaSet() {
        return library;
    }

}