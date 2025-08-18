package io.composeflow.auth.google

import org.junit.Assert.assertEquals
import kotlin.test.Test

class GoogleJsonUtilTest {
    @Test
    fun testExtractClientIdFromGoogleServicesJson() {
        val clientId =
            extractClientIdFromGoogleServicesJson(
                EXAMPLE_GOOGLE_JSON,
                packageName = "com.example.test3",
            )
        assertEquals(
            "\"571511600000-kno1uo3d7hr5351fbeud4pq6fur_test3_redacted.apps.googleusercontent.com\"",
            clientId,
        )
    }
}

private const val EXAMPLE_GOOGLE_JSON = """
{
  "project_info": {
    "project_number": "571511600000",
    "project_id": "kmp-auth-test3",
    "storage_bucket": "kmp-auth-test3.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:571511600000:android:dbb5c8869de98a86xxxxxxx",
        "android_client_info": {
          "package_name": "com.example.test2"
        }
      },
      "oauth_client": [
        {
          "client_id": "571511638152-s5kdvut8hsq9t5e34ac8k_redacted.apps.googleusercontent.com",
          "client_type": 1,
          "android_info": {
            "package_name": "com.example.authTest",
            "certificate_hash": "cadd0fbcf4dd35eae8a81a077a56bdccf81668a6"
          }
        },
        {
          "client_id": "571511600000-kno1uo3d7hr5351fbeud4pq6fur_redacted.apps.googleusercontent.com",
          "client_type": 3
        }
      ],
      "api_key": [
        {
          "current_key": "AIzaSyCxFgbg_4kH2449uX71oKhFB3J17_redacted"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": [
            {
              "client_id": "571511600000-kno1uo3d7hr5351fbeud4pq6fur_redacted.apps.googleusercontent.com",
              "client_type": 3
            },
            {
              "client_id": "571511600000-fqh50t055d7hostl59nhis3uilf68lkt.apps.googleusercontent.com",
              "client_type": 2,
              "ios_info": {
                "bundle_id": "com.example.test2"
              }
            }
          ]
        }
      }
    },
    {
      "client_info": {
        "mobilesdk_app_id": "1:571511600000:android:c35e5068f53fdcc64xxxxx",
        "android_client_info": {
          "package_name": "com.example.test3"
        }
      },
      "oauth_client": [
        {
          "client_id": "571511600000-kno1uo3d7hr5351fbeud4pq6fur_test3_redacted.apps.googleusercontent.com",
          "client_type": 3
        }
      ],
      "api_key": [
        {
          "current_key": "AIzaSyCxFgbg_4kH2449uX71oKhFB3J17_redacted"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": [
            {
              "client_id": "571511600000-kno1uo3d7hr5351fbeud4pq6fur_redacted.apps.googleusercontent.com",
              "client_type": 3
            },
            {
              "client_id": "571511600000-fqh50t055d7hostl59nh_redacted.apps.googleusercontent.com",
              "client_type": 2,
              "ios_info": {
                "bundle_id": "com.example.test2"
              }
            }
          ]
        }
      }
    }
  ],
  "configuration_version": "1"
}
"""
