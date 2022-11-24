import Code.Logic.MediaLibrary;

public class Main {
    
    public static void main(String[] args) throws Exception {
        MediaLibrary libManager = new MediaLibrary("./Data/film.txt", "./Data/serier.txt");
        libManager.search("V").forEach(System.out::println);
    }

}