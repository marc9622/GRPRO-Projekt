package Code.Data;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Super class for both movies and series. */
public abstract class Media implements Serializable {

    public final String title;
    public final int releaseYear;
    public final float rating;

    /** An enum of the different categories that movies and series can belong to
     * <p> TODO: Maybe make this consist of flags in a bit field?
     * An enum value is stored as an int, which is 4 bytes,
     * while of 23 categories, 3 bytes are used,
     * because 3 bytes contain 24 bits.
    */
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
        public static Optional<Category> fromString(String string) {
            return Optional.ofNullable(map.get(string.toLowerCase()));
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

    protected String getCategoriesString() {
        return Stream.of(categories)
                     .map(Category::toString)
                     .collect(Collectors.joining(", "));
    }

    public abstract String toString();

    public abstract int hashCode();

    public abstract boolean equals(Object obj);    
}
