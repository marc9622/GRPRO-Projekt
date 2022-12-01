package Code.Logic;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    
    /** The user's name. */
    private String username;

    /** The user's password. (Encrypted) */
    private String password;

    /** A library of the users favorite media. */
    @SuppressWarnings("unused")
    private MediaLibrary favorites;

    /** A library of the users watched media. */
    //private MediaLibrary watched; // TODO: Maybe add this??

    /** Creates a new user with the given username and password.
     * @param username The user's name.
     * @param password The user's password.
     * @throws IllegalArgumentException If the username or password is invalid.
     */
    public User(String username, String password) throws IllegalArgumentException {
        this.username = username;
        this.password = encrypt(password);
        favorites = new MediaLibrary();
    }

    /** Creates a new user with the given username and password.
     * Gives the user a copy of the given favorites library.
     * @param username The user's name.
     * @param password The user's password.
     * @param favorites The user's favorites library.
     * @throws IllegalArgumentException If the username or password is invalid.
     */
    public User(String username, String password, MediaLibrary favorites) throws IllegalArgumentException {
        Objects.requireNonNull(favorites);
        this.username = username;
        this.password = encrypt(password);
        favorites = new MediaLibrary(favorites);
    }

    /** Returns the user's name.
     * @return The user's name.
     */
    public String getUsername() {
        return username;
    }

    /** Returns whether the given password matches the user's password.
     * @param password The password to check.
     * @return Whether the given password matches the user's password.
     */
    public boolean checkPassword(String password) {
        return this.password.equals(encrypt(password));
    }

    /** Encrypts the given password.
     * @param password The password to encrypt.
     * @return The encrypted password.
     */
    private String encrypt(String password) {
        return XOREncryption.encrypt(password);
    }

    /** Saves the user's data to the given file.
     * @param filePath The path to the file to save to.
     * @throws IOException If the file could not be written to.
     */
    public void saveToFile(String filePath) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(this);
        }
    }

    /** Returns a string representation of the user.
     * @return The user's name.
     */
    public String toString() {
        return username;
    }
}
