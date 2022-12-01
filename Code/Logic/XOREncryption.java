package Code.Logic;

/** A class that encrypts and decrypts strings using XOR encryption. */
public class XOREncryption {
    
    private static final int defaultKey = 0x12345678;

    /** Encrypts the given string using XOR encryption.
     * Uses the default key.
     * @param string The string to encrypt.
     * @return The encrypted string.
     */
    public static String encrypt(String string) {
        return encrypt(string, defaultKey);
    }
    
    /** Encrypts the given string using XOR encryption.
     * @param string The string to encrypt.
     * @param key The key to use for encryption.
     * @return The encrypted string.
     */
    public static String encrypt(String string, int key) {
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (chars[i] ^ key);
        }
        return new String(chars);
    }
    
    /** Decrypts the given string using XOR encryption.
     * Uses the default key.
     * @param string The string to decrypt.
     * @return The decrypted string.
     */
    public static String decrypt(String string) {
        return decrypt(string, defaultKey);
    }
    
    /** Decrypts the given string using XOR encryption.
     * @param string The string to decrypt.
     * @param key The key to use for decryption.
     * @return The decrypted string.
     */
    public static String decrypt(String string, int key) {
        return encrypt(string, key);
    }
    
}
