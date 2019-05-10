package com.mintel.gcs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.GregorianCalendar;

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

/**
 * Google cloud storage content store implementation.
 * See also {@link org.alfresco.repo.content.ContentStore}.
 * 
 * @author Ana Gouveia
 * @author Matteo Mazzola
 * @author Sam Cheshire
 * @author Rob Mackay
 */
public class GCSContentStore extends AbstractContentStore
{
    private static final Log LOG = LogFactory.getLog(GCSContentStore.class);
    private Bucket bucket;
    private String rootDir;

    /**
     * Initialises a GCS content store.
     * 
     * @param keyFileName Google cloud storage key file name.
     * @param bucketName Name of GCS bucket to store content into.
     * @param rootDir The root directory of the files in the bucket.
     * @throws Exception If the connection to GCS was unsuccessful.
     */
    public GCSContentStore(String keyFileName, String bucketName, String rootDir) throws Exception
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("keyFileName: " + keyFileName);
            LOG.debug("bucketName: " + bucketName);
            LOG.debug("rootDir: " + rootDir);
        }
        this.rootDir = rootDir;
        String keyPath = keyFileName;
        /*
         * We first try to get the file directly. If it can't be found we search the extension folder
         */
        InputStream is = GCSContentStore.class.getClassLoader().getResourceAsStream(keyPath);
        if (is == null)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("The file couldn't be found. Trying " + "alfresco/extension/google-cloud-storage/" + keyFileName);
            }
            is = GCSContentStore.class.getClassLoader().getResourceAsStream("alfresco/extension/google-cloud-storage/" + keyFileName);
        }
        if (is == null)
        {
            throw new Exception("The file " + keyFileName + " was not found in the classpath.");
        }
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;
        try
        {
            jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(is, "UTF-8"));
        }
        catch (IOException | ParseException e)
        {
            throw e;
        }
        String projectId = (String) jsonObject.get("project_id");
        if (StringUtils.isBlank(projectId))
        {
            throw new Exception("Error in getting the project_id from key file");
        }
        if (LOG.isDebugEnabled())
        {
            LOG.debug("projectId: " + projectId);
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
            throw new Exception("Error in reading credentials from the file", e);
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
