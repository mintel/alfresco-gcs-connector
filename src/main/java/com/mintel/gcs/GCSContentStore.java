package com.mintel.gcs;

import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class GCSContentStore extends AbstractContentStore
{
    private Bucket bucket;
    private String rootDir;
    private static final Log logger = LogFactory.getLog(GCSContentStore.class);

    /**
     * 
     * @param keyFileName filename of the storage key
     * @param bucketName The name of the bucket
     * @param rootDir The root directory of the files in the bucket
     */
    public GCSContentStore(String keyFileName, String bucketName, String rootDir)
    {
        this.rootDir = rootDir;
        //String keyPath = "alfresco/extension/" + keyFileName;
        String keyPath = keyFileName;
        InputStream is = GCSContentStore.class.getClassLoader().getResourceAsStream(keyPath);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;
        try
        {
            jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(is, "UTF-8"));
        }
        catch (IOException | ParseException e)
        {
            logger.error("Error parsing the JSON in the key", e);
            return;
        }
        String projectId = (String) jsonObject.get("project_id");
        if (StringUtils.isBlank(projectId))
        {
            logger.error("Error in getting the project_id from key file");
        }
        GoogleCredentials credentials;
        try
        {
            credentials = GoogleCredentials.fromStream(GCSContentStore.class.getClassLoader().getResourceAsStream(keyPath));
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId(projectId).build().getService();
            this.bucket = storage.get(bucketName);
        }
        catch (IOException e)
        {
            logger.error("Error in reading credentials from the file", e);
        }
    }

    @Override
    public boolean isWriteSupported()
    {
        return true;
    }

    @Override
    public ContentReader getReader(String contentUrl)
    {
        return new GCSContentReader(getPath(contentUrl), contentUrl, bucket);
    }

    /**
     * Creates a new content URL.  This must be supported by all
     * stores that are compatible with Alfresco.
     * 
     * @return Returns a new and unique content URL
     */
    public static String createNewUrl()
    {
        Calendar calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        // create the URL
        StringBuilder sb = new StringBuilder(20);
        sb.append(FileContentStore.STORE_PROTOCOL).append(ContentStore.PROTOCOL_DELIMITER);
        sb.append(year).append('/').append(month).append('/').append(day);
        sb.append('/').append(hour).append('/').append(minute);
        sb.append('/').append(GUID.generate()).append(".bin");
        String newContentUrl = sb.toString();
        // done
        return newContentUrl;
    }

    /**
     * Extracts the path from the provided contentURL
     * <p>
     * @param contentUrl The url of the file
     * @return The path for the file
     * @see org.alfresco.repo.content.AbstractContentStore#getContentUrlParts
     */
    public String getPath(String contentUrl)
    {
        Pair<String, String> urlParts = super.getContentUrlParts(contentUrl);
        String relativePath = urlParts.getSecond();
        if (StringUtils.isBlank(this.rootDir))
        {
            return relativePath;
        }
        return this.rootDir + "/" + relativePath;
    }
}
