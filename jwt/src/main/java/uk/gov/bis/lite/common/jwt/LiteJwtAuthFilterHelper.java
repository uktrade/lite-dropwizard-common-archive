package uk.gov.bis.lite.common.jwt;

import com.github.toastshaman.dropwizard.auth.jwt.JwtAuthFilter;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;

/**
 * Helper class for building a {@code JwtAuthFilter<LiteJwtUser>}object
 */
public class LiteJwtAuthFilterHelper {

  /**
   * Builds a {@link JwtAuthFilter} object which validates a JWT against the following specification.
   * <ul>
   *   <li>{@code type} (type) "JWT"</li>
   *   <li>{@code alg} (algorithm) "HS256" HMAC SHA256 with the key of {@code jwtSharedSecret}</li>
   *   <li>{@code exp} (expiration time) required with an allowed clock skew of 30 seconds</li>
   *   <li>{@code sub} (subject) required</li>
   *   <li>{@code aud} (audience) "lite"</li>
   * </ul>
   *
   * <p>
   *  With the HTTP {@code Authorization} header prefixed with "Bearer" and {@code realm} "realm" (if provided).
   *  <br>
   *  See the following valid example:
   * </p>
   *
   * <pre>
   * Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.
   * eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE1MDcxMjE1OD
   * AsImV4cCI6MTUzODY1NzU4NywiYXVkIjoibGl0ZSIsInN1YiI6IjEyMzQ1
   * NiIsImVtYWlsIjoiZXhhbXBsZUBleGFtcGxlLmNvbSJ9.wUKTzHkQoym-K
   * CWzFUFrXeEKRQ3y3to-CBeHbqOxW4s
   * </pre>
   *
   * Decodes to:
   *
   * <blockquote><pre>
   * {
   *  "typ": "JWT",
   *  "alg": "HS256"
   * }.
   * {
   *  "iss": "Online JWT Builder",
   *  "iat": 1507121580,
   *  "exp": 1538657587,
   *  "aud": "lite",
   *  "sub": "123456",
   *  "email": "example@example.com"
   * }
   * </pre></blockquote>
   *
   * With a shared secret of "demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement"
   *
   * @param jwtSharedSecret the shared secret to use with the HMAC SHA-256 algorithm, minimum of 64 bytes
   *
   * @return a new {@link JwtAuthFilter}, configured as described here
   */
  public static JwtAuthFilter<LiteJwtUser> buildAuthFilter(String jwtSharedSecret) {
    JwtConsumer consumer = new JwtConsumerBuilder()
        .setAllowedClockSkewInSeconds(30)
        .setRequireExpirationTime()
        .setRequireSubject()
        .setVerificationKey(new HmacKey(jwtSharedSecret.getBytes()))
        .setJwsAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST, AlgorithmIdentifiers.HMAC_SHA256))
        .setExpectedAudience("lite")
        .build();

    return new JwtAuthFilter.Builder<LiteJwtUser>()
        .setJwtConsumer(consumer)
        .setRealm("realm")
        .setPrefix("Bearer")
        .setAuthenticator(new LiteJwtAuthenticator())
        .buildAuthFilter();
  }
}
