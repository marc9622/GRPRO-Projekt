package Code;
import Code.Logic.MediaLibrary;

@SuppressWarnings("unused")
public class Main {

    private static MediaLibrary library;
    
    public static void main(String[] args) throws Exception {
        library = MediaLibrary.readMediaLibrary("./Data/film.txt", "./Data/serier.txt");
    }
}