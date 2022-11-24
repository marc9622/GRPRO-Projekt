package Code.Logic;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import Code.Data.Media;

/** Effectively functions as a namespace for functions that search for media.
 * <p> Use {@link #searchByQueries(String[], Set, Map, boolean)} to search for media.
 * <p> Use the {@link #SearchCache} class to cache search results.
 */
public class MediaSearching {

    private MediaSearching() {}

    /** An object that caches the results of search queries.
     * The cache is a map from search queries to the results of the queries.
     * This cache is used to avoid searching the same query multiple times.
     * <p><i> The cache should be cleared whenever the media library is modified.</i>
     */
    public static class SearchCache {
        private final Map<String, Set<Media>> cache = new HashMap<>();

        /** Clears the cache. Should be used when media library is modified. */
        public void clear() {
            cache.clear();
        }

        /** Returns the cached result of the given query.
         * @param query The query to search for.
         * @return An optional containing the cached result of the given query.
         * Use methods such as <code>.isPresent()</code> to check if the query has been cached.
         */
        private Optional<Set<Media>> get(String query) {
            return Optional.ofNullable(cache.get(query));
        }

        /** Caches the result of the given query.
         * @param query The query searched for.
         * @param result The result of the query.
         * @return The result of the query. Used for method chaining.
         */
        private Set<Media> add(String query, Set<Media> result) {
            cache.put(query, result);
            return result;
        }
    }

    /** A map that caches which search strings that correlates to which categories */
    private static final Map<String, Set<Media.Category>> searchCategoryCache = new HashMap<>();

    /** Takes a set of media and returns the media
     * whose title matches the given query, <i>case insensitive</i>.
     * <p> Uses the given cache to avoid searching the same query multiple times.
     * Also caches the results of the search.
     * @param query The query to search for.
     * @param media The set of media to search in.
     * @param cache The cache to use.
     * @return A set of media that matches the given query.
     */
    private static Set<Media> searchAfterTitle(String query, Set<Media> media, SearchCache cache) {
        // If the query is already cached, use the cached result
        Optional<Set<Media>> cachedResult = cache.get(query);
        if (cachedResult.isPresent())
            return cachedResult.get();
        
        // Search for media that match the query.
        return cache.add(query, media.stream()
                                     .filter(m -> searchMatchesString(query, m.title))
                                     .collect(Collectors.toSet()));
    }

    /** Takes a set of media and returns the media
     * whose categories matches the given query, <i>case insensitive</i>.
     * <p> Uses private cache to avoid searching the same query multiple times.
     * Also caches the results of the search.
     * @param query The query to search for.
     * @param media The set of media to search in.
     * @return A set of media that matches the given query.
     */
    private static Set<Media> searchAfterCategory(String query, Set<Media> media) {
        final Set<Media.Category> categories;

        // If the query is already in the category cache, use it.
        if(searchCategoryCache.containsKey(query))
            categories = searchCategoryCache.get(query);
            
        // Otherwise, search for the categories that contain the query, and add them to the cache.
        else {
            categories = Media.Category.getStringsLowerCase().stream()
                .filter(c -> searchMatchesString(query, c))
                .map(Media.Category::fromString)
                .collect(Collectors.toSet());

            searchCategoryCache.put(query, categories);
        }

        // Search for media that match the query.
        return media.stream().filter(m -> Stream.of(m.categories).anyMatch(categories::contains)).collect(Collectors.toSet());
    }

    /** Checks how many queries that each media matches.
     * Returns a mapping from each media to its search score,
     * meaning the number of queries that it matches.
     * @param queries The queries to search for.
     * @param media The set of media to search in.
     * @param cache The cache to use.
     * @param parallel Whether to use parallel streams.
     * @return A map from each media to its search score.
     */
    private static Map<Media, Integer> getSearchScoreMap(String[] queries, Set<Media> media, SearchCache cache, boolean parallel) {
        // Stores the search score of each media. The search score is the number of queries that the media matches.
        final Map<Media, Integer> scoreMap = parallel ? new ConcurrentHashMap<>() : new HashMap<>(); // Uses ConcurrentHashMap if parallel.
        
        // A stream of all the queries.
        var stream = parallel ? Stream.of(queries).parallel() : Stream.of(queries); // Uses parallel stream if parallel.
        
        // For each query, increment the score of each media that matches the query.
        stream.map(String::toLowerCase)
              .peek(query -> searchAfterTitle(query, media, cache).forEach(m -> scoreMap.merge(m, 1, Integer::sum)))
              .forEach(query -> searchAfterCategory(query, media).forEach(m -> scoreMap.merge(m, 1, Integer::sum)));

        return scoreMap;
    }

    /** A comparator that sorts media by their search score,
     * then by their title, and then by their release year.
     * @param scoreMap A map from media to their search score.
     * @return The comparator.
     */
    private static Comparator<Media> getSearchScoreComparator(Map<Media, Integer> scoreMap) {
        Comparator<Media> comparator = Comparator.comparingInt(scoreMap::get);                     // Not allowed to chain methods here because otherwise the compiler thinks the generic type of the Comparator is Object. >:(
        return comparator.reversed().thenComparing(m -> m.title).thenComparing(m -> m.releaseYear); // But it works if you assign the result of the first method to a variable? Why?? Compiler bug???
    }

    /** Returns the media that matches the given queries.
     * Searches by title and category, <i>case insensitive</i>.
     * <p> Supports concurrent searching of multiple queries,
     * though the media library is not large enough for this to be useful.
     * <p> Uses the cache to avoid searching the same query multiple times.
     * Also caches the results of the search.
     * @param queries The queries to search for.
     * @param media The set of media to search in.
     * @param cache The cache to use.
     * @param parallel Whether to use parallel streams.
     * @return A set of media that matches the given queries.
     */
    public static SortedSet<Media> searchByQueries(String[] queries, Set<Media> media, SearchCache cache, boolean parallel) {
        var scoreMap = getSearchScoreMap(queries, media, cache, parallel);
        var comparator = getSearchScoreComparator(scoreMap);

        // Sorts media by their search score and returns.
        return scoreMap.keySet().stream().collect(Collectors.toCollection(() -> new TreeSet<>(comparator)));
    }

    /** Returns the media that matches the most of the given queries.
     * If multiple media have the same score, they are sorted by title
     * and then by release year, and the first one is returned.
     * Searches by title and category, <i>case insensitive</i>.
     * <p> Supports concurrent searching of multiple queries,
     * though the media library is not large enough for this to be useful.
     * <p> Uses the cache to avoid searching the same query multiple times.
     * Also caches the results of the search.
     * @param queries The queries to search for.
     * @param media The set of media to search in.
     * @param cache The cache to use.
     * @param parallel Whether to use parallel streams.
     * @return A set of media that matches the given queries.
     */
    public static Optional<Media> searchByQueriesTop(String[] queries, Set<Media> media, SearchCache cache, boolean parallel) {
        var scoreMap = getSearchScoreMap(queries, media, cache, parallel);
        var comparator = getSearchScoreComparator(scoreMap);
        
        // Sort and return the results with the comparator.
        return scoreMap.keySet().stream().min(comparator);
    }

    /** Returns whether a string contains another string.
     * <p><b> TODO: Make this more advanced.
     * Strings that are similar to the search string should also be considered a match.
     * Special characters should be ignored.
     * Maybe make it return an int that represents how good the match is? </b>
     * @param search The string to search for. <i>Should be single lowercase word</i>.
     * @param string The string to search in.
     * @return Whether the string contains the search string.
     */
    private static boolean searchMatchesString(String search, String string) {
        return string.toLowerCase().contains(search);
    }
}