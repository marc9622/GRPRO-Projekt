package Code.Logic;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.junit.platform.commons.util.ReflectionUtils;

public class Testing {
    
    @Nested
    class TestUser {

        @Test
        void checkPassword() {
            User user = new User("TestUser", "TestPassword");

            assertTrue(user.checkPassword("TestPassword"));
        }

        @Test
        void checkPasswordEncryption() throws Exception {
            String testPassword = "TestPassword";

            User user = new User("TestUser", testPassword);

            Object password = ReflectionUtils.tryToReadFieldValue(User.class, "password", user).get();
            
            assertTrue(password.equals(XOREncryption.encrypt(testPassword)));
        }
    
    }

    @Nested
    class TestEncryption {

        @Test
        void encryptIsNew() {
            String testString = "TestString";

            String encrypted = XOREncryption.encrypt(testString);

            assertNotEquals(testString, encrypted);
        }

        @Test
        void encryptDecrypt() {
            String testString = "TestString";
            String encryptedString = XOREncryption.encrypt(testString);
            String decryptedString = XOREncryption.decrypt(encryptedString);

            assertTrue(decryptedString.equals(testString));
        }

    }
}
