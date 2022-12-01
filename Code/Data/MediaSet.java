package Code.Data;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class MediaSet extends HashSet<Media> {

    public MediaSet() {
        super();
    }

    public MediaSet(Set<Media> media) {
        super(media);
    }

    public static MediaSet loadMediaSet(String filePath) throws ClassNotFoundException, IOException {
        return (MediaSet) FileSerialization.loadFrom(filePath);
    }

    public Object clone() {
        MediaSet clone = new MediaSet();
        clone.addAll(this);
        return clone;
    }
    
}
