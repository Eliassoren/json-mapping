package no.kantega.jsonmapping;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class ApplicationSecretStub {
  private final String salt;
  private final String hashed;
  private final AtomicReference<String> validationCache = new AtomicReference();

  public ApplicationSecretStub() {
    this.salt = null;
    this.hashed = null;
  }

  private ApplicationSecretStub(String salt, String hashValue) {
    this.salt = (String) Objects.requireNonNull(salt);
    this.hashed = (String) Objects.requireNonNull(hashValue);
  }

  public static ApplicationSecretStub create(String salt, String hashedValue) {
    return new ApplicationSecretStub(salt, hashedValue);
  }

  public static ApplicationSecretStub createHash(String salt, String plainTextValue) {
    String hash = hash(salt, plainTextValue);
    return new ApplicationSecretStub(salt, hash);
  }

  public String getSalt() {
    return this.salt;
  }

  public String getHashed() {
    return this.hashed;
  }

  public static String hash(String salt, String secret) {
    try {
      SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
      PBEKeySpec spec =
          new PBEKeySpec(secret.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), 100000, 512);
      SecretKey key = skf.generateSecret(spec);
      byte[] res = key.getEncoded();
      return (new BigInteger(res)).toString(36);
    } catch (InvalidKeySpecException | NoSuchAlgorithmException var6) {
      throw new RuntimeException(var6);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationSecretStub that = (ApplicationSecretStub) o;
    return Objects.equals(salt, that.salt) && Objects.equals(hashed, that.hashed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(salt, hashed);
  }

  public String toString() {
    return "ApplicationSecret{salt='"
        + this.salt
        + '\''
        + ", applicationSecretHashed='"
        + this.hashed
        + '\''
        + '}';
  }
}
