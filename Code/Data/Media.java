package Code.Data;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/** Super class for both movies and series.
*/
public abstract class Media {

    public final String title;
    public final int releaseYear;
    public final float rating;

    /** The different categories that movies and series can belong to */
    public static enum Category {
        Action, Adventure, Biography, Comedy , Crime, Drama, // Ignoring uppercase convention ¯\_(ツ)_/¯
        Family, Fantasy, History, Horror,  Mystery, Romance,
        SciFi("Sci-fi"), Sport, Thriller, War, Western,

        FilmNoir("Film-Noir"), Music, Musical, // Unique to movies
        Animation, Documentary, TalkShow("Talk-show"); // Unique to series

        private String realName = null;

        private Category() {}
        private Category(String realName) {
            this.realName = realName;
        }
        
        private static Map<String, Category> map =
            Stream.of(Category.values()).collect(
                Collectors.toMap(
                    c -> c.toString().toLowerCase(), // Map key
                    c -> c                           // Map value
                )
            );

        /** Returns the category that corresponds to the given string, <i>case insensitive</i>.
         * @param string The string to parse.
         * @return The category that corresponds to the given string.
         */
        public static Category fromString(String string) {
            return map.get(string.toLowerCase());
        }

        /** Returns a set of string representing the categories, <i>in lowercase</i>.
         * @return The set of strings.
         */
        public static Set<String> getStringsLowerCase() {
            return map.keySet();
        }
    
        /** Returns the string representation of this category.
         * Some categories have a different string representation than their name.
         * For example, the category <code>SciFi</code> has the string representation <code>"Sci-fi"</code>.
         * @return The string representation of this category.
         */
        public String toString() {
            return realName == null ? name() : realName;
        }
    }

    /** The categories this media belongs to */
    public final Category[] categories;

    protected Media(String title, int releaseYear, Category[] categories, float rating) {
        this.title = title;
        this.releaseYear = releaseYear;
        this.categories = categories;
        this.rating = rating;
    }

    public abstract String toString();

    protected String getCategoriesString() {
        return Stream.of(categories)
                     .map(Category::toString)
                     .collect(Collectors.joining(", "));
    }
}

class Movie extends Media {

    /**
     * @param title The title of the movie.
     * @param releaseYear The year the movie was released.
     * @param categories The categories the movie belongs to.
     * @param rating The rating of the movie.
     */
    public Movie(String title, int releaseYear, Category[] categories, float rating) {
        super(title, releaseYear, categories, rating);
    }

    public String toString() {
        return title + "; " + releaseYear + "; " + getCategoriesString() + "; " + rating + ";";
    }
}

class Serie extends Media {

    private final boolean isEnded;
    
    /** <i>Should only be used if {@link #isEnded} is {@code true}</i>.*/
    private final int endYear;

    /** The number of episodes per season in order. The indices are therefore the seasons numbers.*/
    private final int[] seasonLengths;

    /**
     * @param title The title of the serie.
     * @param releaseYear The year the serie started.
     * @param isEnded Whether the serie has ended.
     * @param endYear The year the serie ended. <i>Only relevant if {@link #isEnded} is {@code true}</i>.
     * @param categories The categories the serie belongs to.
     * @param rating The rating of the serie.
     * @param seasonLengths The number of episodes per season in order. The indices are therefore the seasons numbers.
     */
    public Serie(String title, int releaseYear, boolean isEnded, int endYear, Category[] categories, float rating, int[] seasonLengths) {
        super(title, releaseYear, categories, rating);
        this.isEnded = isEnded;
        this.endYear = endYear;
        this.seasonLengths = seasonLengths;
    }

    private String getSeasonLengthsString() {
        return IntStream.range(0, seasonLengths.length)
                        .mapToObj(i -> (i + 1) + "-" + seasonLengths[i])
                        .collect(Collectors.joining(", "));
    }

    public String toString() {
        return title + "; " +
               releaseYear + "- " +
               (isEnded ? endYear : "") + "; " +
               getCategoriesString() + "; " +
               rating + "; " +
               getSeasonLengthsString() + ";";
    }
}
