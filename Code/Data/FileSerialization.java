package Code.Data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class FileSerialization {
    
    public static void saveTo(Serializable object, String filePath) throws IOException {
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(object);
        }
    }

    public static Object loadFrom(String filePath) throws IOException, ClassNotFoundException {
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            return in.readObject();
        }
    }

}
