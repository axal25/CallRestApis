# CallRestApis

Call REST APIs

### MAQ API

https://maqtextanalytics.azurewebsites.net/#/DevelopersZone
https://github.com/maqsoftware/MAQTextAnalyticsSDK

### How to run

1. Create files containing secrets in folder [secrets](secrets) and source them using: \
   `source "./load_env_vars.sh"` \
   Or replace in file [application.properties](src%2Fmain%2Fresources%2Fapplication.properties) properties beginning
   with `secrets.` as in pattern: `secrets.<secret_property_name>`. \
   For example replace `secrets.maq_api_key_value=${MAQ_API_KEY_VALUE}` with `secrets.maq_api_key_value=<some_value>`.