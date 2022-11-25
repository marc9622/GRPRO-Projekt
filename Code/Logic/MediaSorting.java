package Code.Logic;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import Code.Data.Media;

/** Effectively functions as a namespace for functions that search for media.
 * <p> Use {@link #sortBySearchQueries(String[], Set, SearchCache, int, boolean, boolean))} to search for media.
 * <p> Use the {@link #SearchCache} class to cache search results.
 */
public class MediaSorting {

    /** Prevents instantiation of this class.
     * This class only contains static methods,
     * and exists only because Java forces you to use classes,
     * even when it is unnecessary.
     */
    private MediaSorting() {}

    /** A class that caches the results of search queries,
     * so that equivalent queries don't have to be searched for multiple times.
     * The cache is mapping from search queries to maps mapping from media to a search score.
     * Could be visualized like this:
     * <p> {@code Map: query -> (Map: media -> score)}.
     * <p> The cache is not stored locally in this class,
     * because it depends on the media library,
     * but is instead passed as a parameter to the search functions.
     * <p><i> The cache should be cleared whenever the media library is modified.</i>
     */
    public static class SearchCache {
        private final Map<String, Map<Media, Integer>> cache = new HashMap<>();

        /** Clears the cache. Should be used when media library is modified. */
        public void clear() {
            cache.clear();
        }

        /** Returns the cached map, that maps from media to a search score.
         * @param query The query to search for.
         * @return An optional containing the cached scoring map.
         * Use methods such as <code>.isPresent()</code> to check if the query has been cached.
         */
        private Optional<Map<Media, Integer>> get(String query) {
            return Optional.ofNullable(cache.get(query));
        }

        /** Caches the result of the given query.
         * @param query The query searched for.
         * @param result The scoring map.
         * @return The scoring map just added. <i>(For method chaining)</i>
         */
        private Map<Media, Integer> add(String query, Map<Media, Integer> result) {
            cache.put(query, result);
            return result;
        }
    }

    /** Similar to {@link #SearchCache} except it scores media categories instead of media.
     * This does not depend on the media library, and is therefore stored in a private static field.
    */
    private static final Map<String, Map<Media.Category, Integer>> searchCategoryCache = new HashMap<>();

    /** The default comparator for comparing Media.
     * First compares by title (alphabetically), then by year (newest first).
     * <p> TODO: Maybe make Media implement Comparable instead?
     */
    private static Comparator<Media> defaultComparator =
        Comparator.comparing((Function<Media, String>)(m -> m.title))
                  .thenComparing(Comparator.comparingInt((ToIntFunction<Media>)m -> m.releaseYear).reversed());


    /** Takes a set of media and returns a map mapping from media to a search score.
     * The searh score for each media is calculated based on how well
     * the query matches the title of the media. See {@link #calcSearchScore(String, String)}.
     * <p> Uses the given cache to avoid searching the same query multiple times.
     * Also caches the results of the search.
     * @param query The query to search for.
     * @param media The set of media to search in.
     * @param cache The cache to use.
     * @return A map mapping from media to a search score.
     */
    private static Map<Media, Integer> calcSearchScorerByTitle(String query, Set<Media> media, SearchCache cache, boolean useCache) {
        if(useCache) {
            // If the query is already cached, use the cached result
            Optional<Map<Media, Integer>> cachedResult = cache.get(query);
            if (cachedResult.isPresent())
                return cachedResult.get();
        }

        // For each media, calculate the search score,
        // and collect the mapping from media to score in a map
        Map<Media, Integer> result =
            media.stream()
                 .collect(Collectors.toMap(
                 /* Map Key: */         m -> m, // The map key is the media itself
                 /* Map Value: */       m -> Stream.of(m.title.toLowerCase().split(" ")) // First, split the title into words
                                                   .mapToInt(s -> calcSearchScore(query, s)) // Calculate the search score for each word
                                                   .max() // Get the highest score
                                                   .orElse(0) // If there are no words, the score is 0
                 ));
        
        if(useCache) cache.add(query, result);
        
        return result;
    }

    /** Takes a set of media and returns a map mapping from media to a search score.
     * The searh score for each media is calculated based on how well
     * the query matches the categories of the media. See {@link #calcSearchScore(String, String)}.
     * <p> Uses private cache to avoid searching the same query multiple times.
     * Also caches the results of the search.
     * @param query The query to search for.
     * @param media The set of media to search in.
     * @return A map mapping from media to a search score.
     */
    private static Map<Media, Integer> calcSearchScorerByCategory(String query, Set<Media> media) {
        final Map<Media.Category, Integer> categories;

        // If the query is already in the category cache, use it.
        if(searchCategoryCache.containsKey(query))
            categories = searchCategoryCache.get(query);
            
        // Otherwise, calculate how well the query matches each category
        else {
            categories = Stream.of(Media.Category.values())
                               .collect(Collectors.toMap(
                               /* Map Key: */       c -> c, // The map key is the category itself
                               /* Map Value: */     c -> calcSearchScore(query, c.toString().toLowerCase()) // The map value is the search score
                               ));
            // And add the result to the cache
            searchCategoryCache.put(query, categories);
        }

        // For each media, calculate the maximum search score of its categories,
        // and collect the mapping from media to score in a map
        return media.stream()
                    .collect(Collectors.toMap(
                    /* Map Key: */       m -> m, // The map key is the media itself
                    /* Map Value: */     m -> Stream.of(m.categories) // First, get the categories of the media
                                                    .mapToInt(c -> categories.get(c)) // Calculate the search score for each category
                                                    .max() // Get the highest score
                                                    .orElse(0) // If there are no categories, the score is 0
                    ));
    }

    /** Returns the media that matches the given queries.
     * Searches by title and category, <i>case insensitive</i>.
     * The media is firstly sorted by how well it matches the queries,
     * and then by the default comparator.
     * <p> Supports concurrent searching of multiple queries,
     * which might be useful for larger amounts of queries.
     * Though, due to the small size of the media library,
     * concurrent searching probably won't be much faster, if at all.
     * <p> Uses the cache to avoid searching the same query multiple times.
     * Also caches the results of the search.
     * @param queries The queries to search for.
     * @param media The set of media to search in.
     * @param cache The cache to use.
     * @param count The number of results to return.
     * @param useCache Whether to use the cache.
     * @param parallel Whether to use parallel streams.
     * @return A sorted list of media that matches the given queries.
     */
    public static List<Media> sortBySearchQueries(Set<Media> media, String[] queries, SearchCache cache, int count, boolean useCache, boolean parallel) {
        
        // Stores the search score of each media. The search score is the number of queries that the media matches.
        final Map<Media, Integer> scoreMap = parallel ? new ConcurrentHashMap<>() : new HashMap<>(); // Uses ConcurrentHashMap if parallel.

        // A stream of all the queries.
        Stream<String> stream = parallel ? Stream.of(queries).parallel() : Stream.of(queries); // Uses parallel stream if parallel.
        
        // For each query...
        stream.map(String::toLowerCase)
            // First search by title, calculate the score mapping...
            .peek(query -> calcSearchScorerByTitle(query, media, cache, useCache)
                           // And for each media in the mapping, add the score to the score map.
                           .forEach((m, score) -> scoreMap.merge(m, score, Integer::sum)))
            // Then search by category, calculate the score mapping...
            .forEach(query -> calcSearchScorerByCategory(query, media)
                              // And for each media in the mapping, add the score to the score map.
                              .forEach((m, score) -> scoreMap.merge(m, score, Integer::sum)));

        // Creates a comparator that uses the score map to compare media.
        final Comparator<Media> scoreComparator =
            Comparator.comparingInt((ToIntFunction<Media>)scoreMap::get) //  Have to cast to ToIntFunction for some reason. Compiler bug? >:(
                      .reversed()
                      .thenComparing(defaultComparator);

        // If only one result is needed, return the media with the highest score.
        if(count == 1)
            return scoreMap.keySet()
                           .stream()
                           .min(scoreComparator)
                           .stream()
                           .collect(Collectors.toList());
        
        // If multiple results are needed, sort and return the results with the comparator.
        else
            return scoreMap.keySet()
                           .stream()
                           .sorted(scoreComparator)
                           .limit(count)
                           .collect(Collectors.toList());
    }

    /** Returns the media that matches the given queries.
     * Searches by title and category, <i>case insensitive</i>.
     * The media is firstly sorted by how well it matches the queries,
     * and then by the default comparator.
     * <p> Supports concurrent searching of multiple queries,
     * which might be useful for larger amounts of queries.
     * Though, due to the small size of the media library,
     * concurrent searching probably won't be faster.
     * <p> Uses the cache to avoid searching the same query multiple times.
     * Also caches the results of the search.
     * @param queries The queries to search for.
     * @param media The set of media to search in.
     * @param cache The cache to use.
     * @param useCache Whether to use the cache.
     * @param parallel Whether to use parallel streams.
     * @return A sorted list of media that matches the given queries.
     */
    public static List<Media> sortBySearchQueries(Set<Media> media, String[] queries, SearchCache cache, boolean useCache, boolean parallel) {
        return sortBySearchQueries(media, queries, cache, media.size(), useCache, parallel);
    }

    /** Returns an integer score representing how well the target string matches the query string.
     * <i> The strings should be in lowercase and trimmed. (Meaning no leading or trailing whitespace)</i>
     * <p> Scoring: <ul>
     * <li> +1 for each shared character. <i>(Not necessarily at the same index)</i>
     * <li> +1 for each two characters that are next to each other in both strings.
     * <li> +2 if the length of the strings match. </ul>
     * <li> +3 if the first character of both strings match.
     * <li> +3 if the last character of both strings match.
     * <p> The returned score is the sum of the scoring rules.
     * TODO: Maybe higher rated movies should also be prioritized?
     * @param query The string to search for. <i>Should be single lowercase word</i>.
     * @param target The string to search in.
     * @return Whether the string contains the search string.
     */
    private static int calcSearchScore(String query, String target) {
        // If any of the strings are empty, return 0.
        if(query.isEmpty() || target.isEmpty())
            return 0;

        int score = 0;

        // Check if strings are same length
        if(query.length() == target.length())
            score += 2;

        // Check if first characters match
        if(query.charAt(0) == target.charAt(0))
            score += 3;

        // Check if last characters match
        if(query.charAt(query.length() - 1) == target.charAt(target.length() - 1))
            score += 3;

        // Check for shared characters and shared character pairs
        {   
            // Stores which characters that have been used
            Set<Character> queryChars = new HashSet<>();
            Set<Character> targetChars = new HashSet<>();

            // Stores which character pairs that have been used
            record Pair(char a, char b) {}
            Set<Pair> queryPairs = new HashSet<>();
            Set<Pair> targetPairs = new HashSet<>();

            // Add the first character of each string to the character sets
            queryChars.add(query.charAt(0));
            targetChars.add(target.charAt(0));

            // Loop through the query string
            for(int i = 1; i < query.length(); i++) {
                // Add the current character
                queryChars.add(query.charAt(i));

                // Add the current character pair
                queryPairs.add(new Pair(query.charAt(i-1), query.charAt(i)));
            }

            // Loop through the target string
            for(int i = 1; i < target.length(); i++) {
                // Add the current character
                targetChars.add(target.charAt(i));

                // Add the current character pair
                targetPairs.add(new Pair(target.charAt(i-1), target.charAt(i)));
            }
        
            // Check for shared characters
            for(char c : queryChars)
                if(targetChars.contains(c))
                    score++;

            // Check for shared character pairs
            for(Pair p : queryPairs)
                if(targetPairs.contains(p))
                    score++;
        }

        return score;
    }

    /** An enum expressing what to sort the media by. <ul>
     * <p> {@link #TITLE} sorts by title (alphabetically).
     * <p> {@link #YEAR} sorts by year (newest first).
     * <p> {@link #RATING} sorts by rating (highest first).
     * <p> {@link #DEFAULT} sorts by the {@link #defaultComparator}. </ul>
     * TODO: Should sorting by category be allowed?
     * How would sorting by category work?
     * Would it sort by the first category, or by all categories?
    */
    public static enum SortBy {
        /** Sorts by title (alphabetically). */
        TITLE,
        /** Sorts by year (newest first). */
        RELEASE_YEAR,
        /** Sorts by rating (highest first). */
        RATING,
        /** Sorts by the default comparator. */
        DEFAULT,
    }

    /** An enum expression which direction to sort the media in. <ul>
     * <p> {@link #DEFAULT} sorts in the default order.
     * <p> {@link #REVERSE} sorts in the reverse order. </ul>
     */
    public static enum SortOrder {
        /** The default sorting order. */
        DEFAULT,
        /** The reverse sorting order. */
        REVERSE,
    }

    /** Returns the media sorted in the given order.
     * @param media The media to sort.
     * @param sortBy The property to sort by.
     * @param sortOrder The order to sort in.
     * @return A sorted list of media.
     */
    public static List<Media> sortMedia(Collection<Media> media, SortBy sortBy, SortOrder sortOrder) {
        Comparator<Media> comparator = switch (sortBy) {
            case TITLE -> Comparator.comparing(m -> m.title); // Alphabeticallly
            case RELEASE_YEAR -> Comparator.comparingInt((ToIntFunction<Media>)m -> m.releaseYear).reversed(); // Newest first.
            case RATING -> Comparator.comparingDouble((ToDoubleFunction<Media>)m -> m.rating).reversed(); // Highest first.
            case DEFAULT -> defaultComparator;
        };

        if (sortOrder == SortOrder.REVERSE) comparator = comparator.reversed();

        return media.stream().sorted(comparator).collect(Collectors.toList());
    }

}