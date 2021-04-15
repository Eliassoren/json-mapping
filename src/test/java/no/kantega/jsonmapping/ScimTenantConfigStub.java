package no.kantega.jsonmapping;

import java.util.Objects;
import no.kantega.jsonmapping.Core.JsonProperty;
import org.apache.commons.lang3.StringUtils;

public class ScimTenantConfigStub {


  /** Uniquely identifies the tenant and links the config and Internal directory */
  private String tenantId;

  /**
   * name is also stored on the Internal directory. As both can be independently deleted it's nice
   * to have a readable name available on both.
   */
  private String tenantName;

  /** Hashed application secret. */
  private ApplicationSecretStub applicationSecret;

  /** The type of directory. Not used yet but could be used to customized setup etc. */
  private ScimProviderKind kind;

  public ScimTenantConfigStub(
      @JsonProperty("tenantId") String tenantId,
      @JsonProperty("tenantName") String tenantName,
      @JsonProperty("applicationSecret") ApplicationSecretStub applicationSecret,
      @JsonProperty("kind") ScimProviderKind kind) {
    this.tenantId = requireNonBlank(tenantId);
    this.tenantName = requireNonBlank(tenantName);
    this.applicationSecret = Objects.requireNonNull(applicationSecret);
    this.kind = Objects.requireNonNull(kind);
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getTenantName() {
    return tenantName;
  }

  public ApplicationSecretStub getApplicationSecret() {
    return applicationSecret;
  }

  public ScimProviderKind getKind() {
    return kind;
  }

  private String requireNonBlank(String value) {
    if (StringUtils.isBlank(value)) {
      throw new IllegalArgumentException("Non-blank value required");
    }
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScimTenantConfigStub that = (ScimTenantConfigStub) o;
    return Objects.equals(tenantId, that.tenantId)
        && Objects.equals(tenantName, that.tenantName)
        && Objects.equals(applicationSecret, that.applicationSecret)
        && kind == that.kind;
  }

  @Override
  public int hashCode() {
    return Objects.hash(tenantId, tenantName, applicationSecret, kind);
  }

  public enum ScimProviderKind {
    AZURE("Azure AD"),
    ONELOGIN("OneLogin"),
    OKTA("Okta"),
    OKTA_V1("Okta", "Okta Provisioning Agent"),
    GENERIC("Generic", "Any SCIM provider");

    private final String name;
    private final String description;

    private ScimProviderKind(String name) {
      this(name, name);
    }

    private ScimProviderKind(String name, String description) {
      this.name = name;
      this.description = description;
    }

    public String getName() {
      return this.name;
    }

    public String getDescription() {
      return this.description;
    }
  }

}
