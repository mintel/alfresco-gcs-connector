# alfresco-gcs-connector #

Google Cloud Connector for Alfresco.

## Introduction ##

This store implementation uses the `google-cloud-storage` library to store content in a google cloud storage bucket instead of the local FileSystem.

## Building ##

In order to build the project execute:
```
mvn clean install -DskipTests=true
```

This will generate the jar file.

## Installation ##

If the jar is included in the project it will automatically connect to google cloud storage. 
You will need to add your google cloud storage gpg configuration file to `shared/classes/alfresco/extension/google-cloud-storage` and
edit `alfresco-global.properties` to add the file name and the bucket name:
```
gcs.keyFileName=name.gpg
gcs.bucketName=bucket
```
The project is configured to use a caching content store. The following properties will need to be added to your alfresco-global.properties.

```
dir.cachedcontent=/path/to/cache
system.content.caching.cacheOnInbound=true
system.content.caching.maxDeleteWatchCount=1
system.content.caching.contentCleanup.cronExpression=0 0 3 * * ?
system.content.caching.timeToLiveSeconds=0
system.content.caching.timeToIdleSeconds=60
system.content.caching.maxElementsInMemory=5000
system.content.caching.maxElementsOnDisk=10000
system.content.caching.minFileAgeInMillis=2000
system.content.caching.maxUsageMB=4096
system.content.caching.maxFileSizeMB=0
```

For more information on these properties and caching content store configuration see http://docs.alfresco.com/5.0/tasks/ccs-config.html.