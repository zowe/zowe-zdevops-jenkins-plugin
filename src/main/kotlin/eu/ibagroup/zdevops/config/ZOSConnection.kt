package eu.ibagroup.zdevops.config;

import com.cloudbees.plugins.credentials.CredentialsMatchers
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.StandardCredentials
import com.cloudbees.plugins.credentials.common.StandardListBoxModel
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder
import eu.ibagroup.r2z.zowe.client.sdk.zosfiles.ZosDsnList
import eu.ibagroup.r2z.zowe.client.sdk.zosfiles.input.ListParams
import eu.ibagroup.zdevops.Messages
import eu.ibagroup.zdevops.declarative.jobs.zMessages
import hudson.Extension
import hudson.model.AbstractDescribableImpl
import hudson.model.Descriptor
import hudson.model.Item
import hudson.security.ACL
import hudson.util.FormValidation
import hudson.util.ListBoxModel
import jenkins.model.Jenkins
import net.sf.json.JSONObject
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.QueryParameter
import org.kohsuke.stapler.StaplerRequest
import java.net.URL
import java.util.*


class ZOSConnection
@DataBoundConstructor
constructor(
  val name: String,
  val url: String,
  val credentialsId: String
) : AbstractDescribableImpl<ZOSConnection>() {
  @Extension
  class ZOSConnectionDescriptor : Descriptor<ZOSConnection>() {
    override fun configure(req: StaplerRequest, json: JSONObject): Boolean {
      req.bindJSON(this, json)

      return true
    }

    fun doCheckName(@QueryParameter value: String?): FormValidation {
      if (value.isNullOrBlank()) {
        return FormValidation.error(Messages.zdevops_config_ZOSConnection_validation_empty())
      }

      return FormValidation.ok()
    }

    fun doCheckUrl(@QueryParameter value: String?): FormValidation {
      if (value.isNullOrBlank()) {
        return FormValidation.error(Messages.zdevops_config_ZOSConnection_validation_empty())
      }

      return FormValidation.ok()
    }

    fun doCheckUsername(@QueryParameter value: String?): FormValidation {
      if (value.isNullOrBlank()) {
        return FormValidation.error(Messages.zdevops_config_ZOSConnection_validation_empty())
      }
      if (value.length >= 8) {
        return FormValidation.error(Messages.zdevops_config_ZOSConnection_validation_username_length())
      }

      return FormValidation.ok()
    }


    fun doFillCredentialsIdItems(): ListBoxModel? {
      return if (Jenkins.get().hasPermission(Item.CONFIGURE)) {
        StandardListBoxModel()
          .includeEmptyValue()
          .includeMatchingAs(
            ACL.SYSTEM,
            Jenkins.get(),
            StandardCredentials::class.java,
            URIRequirementBuilder.fromUri("").build()
          ) { it is StandardUsernamePasswordCredentials }
      } else StandardListBoxModel()
    }

    fun doValidateConnection(
      @QueryParameter("name") name : String,
      @QueryParameter("url") url : String,
      @QueryParameter("credentialsId") credentialsId : String
    ): FormValidation {
      runCatching {
        val credentials = CredentialsMatchers.firstOrNull(
          CredentialsProvider.lookupCredentials(
            StandardCredentials::class.java,
            Jenkins.get(),
            ACL.SYSTEM,
            URIRequirementBuilder.fromUri("").build()
          ),
          CredentialsMatchers.withId(credentialsId)
        )

        if (credentials !is StandardUsernamePasswordCredentials) {
          return FormValidation.error(zMessages.zdevops_config_ZOSConnection_validation_wrong_credential_type())
        }

        val connURL = URL(url)
        val testConnection = eu.ibagroup.r2z.zowe.client.sdk.core.ZOSConnection(
          connURL.host, connURL.port.toString(), credentials.username, credentials.password.plainText, connURL.protocol
        )
        ZosDsnList(testConnection).listDsn(zMessages.zdevops_config_ZOSConnection_validation_testDS(), ListParams())
      }.onFailure {
        return FormValidation.error(zMessages.zdevops_config_ZOSConnection_validation_error());
      }
      return FormValidation.ok(zMessages.zdevops_config_ZOSConnection_validation_success())
    }
  }
}