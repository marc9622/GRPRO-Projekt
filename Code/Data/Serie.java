package Code.Data;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** A series. */
public class Serie extends Media {

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
    Serie(String title, int releaseYear, boolean isEnded, int endYear, Category[] categories, float rating, int[] seasonLengths) {
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

    public int hashCode() {
        int result = 123;
        result = 37 * result + title.hashCode();
        result = 37 * result + releaseYear;
        result = 37 * result + (isEnded ? 1 : 0);
        result = 37 * result + endYear;
        result = 37 * result + Arrays.hashCode(categories);
        result = 37 * result + Float.floatToIntBits(rating);
        result = 37 * result + Arrays.hashCode(seasonLengths);
        return result;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Serie)) {
            return false;
        }
        Serie other = (Serie) obj;
        return title.equals(other.title) &&
               releaseYear == other.releaseYear &&
               isEnded == other.isEnded &&
               endYear == other.endYear &&
               Arrays.equals(categories, other.categories) &&
               rating == other.rating &&
               Arrays.equals(seasonLengths, other.seasonLengths);
    }
}