package com.restfulbooker.tests.encryption;

import com.restfulbooker.config.environments.EnvironmentConfigConstants;
import com.restfulbooker.crypto.services.EnvironmentCryptoManager;
import com.restfulbooker.crypto.services.SecureKeyGenerator;
import com.restfulbooker.utils.Base64Utils;
import com.restfulbooker.utils.ErrorHandler;
import com.restfulbooker.utils.LoggerUtils;
import com.restfulbooker.utils.constants.Encryption;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.CryptoException;
import org.testng.annotations.Test;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;

public class EncryptionFlowTests {

    // to run test: mvn clean test -DskipBrowserInitialization=true -Dgroups=uat-encryption
    private static final Logger logger = LoggerUtils.getLogger(EncryptionFlowTests.class);


    @Test(groups = {"uat-encryption"}, priority = 1)
    public void generateSecretKey() throws IOException {
        try {
            // Generate a new secret key
            SecretKey generatedSecretKey = SecureKeyGenerator.generateSecretKey();

            // Save the secret key in the base environment
            EnvironmentCryptoManager.saveSecretKeyInBaseEnvironment(
                    EnvironmentConfigConstants.EnvironmentFilePath.BASE.getFullPath(),
                    EnvironmentConfigConstants.EnvironmentSecretKey.UAT.getKeyName(),
                    Base64Utils.encodeSecretKey(generatedSecretKey)

            );
            logger.info("Secret key generation process completed");
        } catch (Exception error) {
            ErrorHandler.logError(error, "generateSecretKey", "Failed to generate secret key");
            throw error;
        }
    }

    @Test(groups = {"uat-encryption"}, priority = 2)
    public void encryptCredentials() throws CryptoException {
        try {
            // Run Encryption
            EnvironmentCryptoManager.encryptEnvironmentVariables(
                    EnvironmentConfigConstants.Environment.UAT.getDisplayName(),
                    EnvironmentConfigConstants.EnvironmentFilePath.UAT.getFilename(),
                    EnvironmentConfigConstants.EnvironmentSecretKey.UAT.getKeyName(),
                    Encryption.getAuthenticationUsername(), Encryption.getAuthenticationPassword()

            );
            logger.info("Encryption process completed");
        } catch (Exception error) {
            ErrorHandler.logError(error, "encryptCredentials", "Failed to encrypt credentials");
            throw error;
        }
    }

    @Test(groups = {"uat-encryption"}, priority = 3)
    public void decryptionCredentials() throws CryptoException {
        try {
            // Run Encryption
            List<String> decryptedCredentials = EnvironmentCryptoManager.decryptEnvironmentVariables(
                    EnvironmentConfigConstants.Environment.UAT.getDisplayName(),
                    EnvironmentConfigConstants.EnvironmentFilePath.UAT.getFilename(),
                    EnvironmentConfigConstants.EnvironmentSecretKey.UAT.getKeyName(),
                    "AUTHENTICATION_USERNAME", "AUTHENTICATION_PASSWORD"
            );

            System.out.println("Decrypted UserName: " + decryptedCredentials.get(0));
            System.out.println("Decrypted Password: " + decryptedCredentials.get(1));

            logger.info("Decryption process completed");
        } catch (Exception error) {
            ErrorHandler.logError(error, "encryptCredentials", "Failed to encrypt credentials");
            throw error;
        }
    }
}
