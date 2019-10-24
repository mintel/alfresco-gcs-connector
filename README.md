# alfresco-gcs-connector #

Google Cloud Connector for Alfresco.

## Introduction ##

This store implementation uses the `google-cloud-storage` library to store content in a google cloud storage bucket instead of the local FileSystem.

## Building ##

In order to build the project execute:

```
mvn clean package -DskipTests=true
```

This will generate the jar file.

## Installation ##

If the jar is included in the project it will automatically connect to google cloud storage. 
You will need to add your google cloud storage gpg configuration file to `shared/classes/alfresco/extension/google-cloud-storage` (or specify a different path) and
edit `alfresco-global.properties` to add the bucket information:

```
# Where is the key for the contentstore
gcs.keyPath=alfresco/extension/google-cloud-storage/
gcs.keyFileName=key.json

# Where is the key for the archive contenstore
gcs.keyPath.deleted=alfresco/extension/google-cloud-storage/
gcs.keyFileName.deleted=${gcs.keyFileName}

# The name of the buckets for the content store and the archive
gcs.bucketName=bucket
gcs.bucketName.deleted=${gcs.bucketName}

# The root path for the content store and the archive in the bucket
gcs.dir.contentstore=${dir.contentstore}
gcs.dir.contentstore.deleted=${dir.contentstore}
```
The project is configured to use a caching content store. The following properties can be changed in our alfresco-global.properties:

```
dir.root=./alf_data
dir.cachedcontent=${dir.root}/cachedcontent
system.content.caching.cacheOnInbound=true
system.content.caching.maxUsageMB=4096
# maxFileSizeMB - 0 means no max file size.
system.content.caching.maxFileSizeMB=0
# When the CachingContentStore is about to write a cache file but the disk usage is in excess of panicThresholdPct
# (default 90%) then the cache file is not written and the cleaner is started (if not already running) in a new thread.
system.content.caching.panicThresholdPct=90
# When a cache file has been written that results in cleanThresholdPct (default 80%) of maxUsageBytes
# being exceeded then the cached content cleaner is invoked (if not already running) in a new thread.
system.content.caching.cleanThresholdPct=80
# An aggressive cleaner is run till the targetUsagePct (default 70%) of maxUsageBytes is achieved
system.content.caching.targetUsagePct=70
# Threshold in seconds indicating a minimal gap between normal cleanup starts
system.content.caching.normalCleanThresholdSec=0
system.content.caching.minFileAgeInMillis=2000
system.content.caching.maxDeleteWatchCount=1
# Clean up every day at 3 am
system.content.caching.contentCleanup.cronExpression=0 0 3 * * ?
system.content.caching.timeToLiveSeconds=0
system.content.caching.timeToIdleSeconds=60
system.content.caching.maxElementsInMemory=5000
system.content.caching.maxElementsOnDisk=10000
```

For more information on these properties and caching content store configuration see http://docs.alfresco.com/6.1/tasks/ccs-config.html.