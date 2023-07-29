# CallRestApis

Call REST APIs

### How to run

1. Populate environment variables required.
    1. Create files containing secrets in folder [secrets](secrets) and source them using: \
       `source "./load_env_vars.sh"`. \
       Folder [secrets](secrets) contains [template.env](secrets%2Ftemplate.env) to be used as a template for required
       files. \
    2. Or replace in file [application.properties](src%2Fmain%2Fresources%2Fapplication.properties) properties beginning
       with `secrets.` as in pattern: `secrets.<secret_property_name>`. \
       For example replace `secrets.maq_api_key_value=${MAQ_API_KEY_VALUE}`
       with `secrets.maq_api_key_value=<some_value>`.
2. `mvn clean test`
3. Start application
    1. `mvn clean package -DskipTests`
    2. `java -jar target/CallRestApis_complete_standalone.jar`

### MAQ API

1. https://maqtextanalytics.azurewebsites.net/#/DevelopersZone > "Get Free Trial" button. \
   [MaQ API key - "Get Free Trial button" ![maq_api_key_get_free_trial.png](secrets%2Fmaq_api_key_get_free_trial.png)](secrets%2Fmaq_api_key_get_free_trial.png)
2. https://github.com/maqsoftware/MAQTextAnalyticsSDK

### GIT

1. remove all files from being tracked \
   `git rm -r --cached .`
2. add all files to be tracked (taking into account changes to .gitignore) \
   `git add .`
3. check what files are now tracked \
   `git status`