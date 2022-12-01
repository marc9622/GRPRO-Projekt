package Code.Data;

import java.util.Arrays;

/** A movie. */
public class Movie extends Media {

    /**
     * @param title The title of the movie.
     * @param releaseYear The year the movie was released.
     * @param categories The categories the movie belongs to.
     * @param rating The rating of the movie.
     */
    Movie(String title, int releaseYear, Category[] categories, float rating) {
        super(title, releaseYear, categories, rating);
    }

    public String toString() {
        return title + "; " + releaseYear + "; " + getCategoriesString() + "; " + rating + ";";
    }

    public int hashCode() {
        int result = 123;
        result = 37 * result + title.hashCode();
        result = 37 * result + releaseYear;
        result = 37 * result + Arrays.hashCode(categories);
        result = 37 * result + Float.floatToIntBits(rating);
        return result;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Movie)) {
            return false;
        }
        Movie other = (Movie) obj;
        return title.equals(other.title) &&
               releaseYear == other.releaseYear &&
               Arrays.equals(categories, other.categories) &&
               rating == other.rating;
    }
}
