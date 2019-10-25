package com.mintel.gcs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
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
    /**
     * The bucket where the content should be
     */
    private Bucket bucket;
    /**
     * The GCS storage where the bucket is
     */
    private Storage storage;
    /**
     * The root folder where the content is in the bucket
     */
    private String rootDir;

    /**
     * Initialises a GCS content store.
     * 
     * @param keyPath The path to the Google cloud storage key. Default is <code>alfresco/extension/google-cloud-storage/<code>
     * @param keyFileName Google cloud storage key file name. Default is <code>key.json<code>
     * @param bucketName Name of  Google cloud storage bucket to store content into. Default is <code>bucket<code>
     * @param rootDir The root directory of the files in the bucket.
     * 
     * @throws Exception If the connection to GCS was unsuccessful.
     */
    public GCSContentStore(String keyPath, String keyFileName, String bucketName, String rootDir) throws Exception
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("\u21E8 keyPath: " + keyPath);
            LOG.debug("\u21E8 keyFileName: " + keyFileName);
            LOG.debug("\u21E8 bucketName: " + bucketName);
            LOG.debug("\u21E8 rootDir: " + rootDir);
        }
        this.rootDir = rootDir;

        InputStream is = this.getCredentials(keyPath, keyFileName);
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
        try
        {
            GoogleCredentials credentials = GoogleCredentials.fromStream(this.getCredentials(keyPath, keyFileName));
            this.storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId(projectId).build().getService();
            this.bucket = storage.get(bucketName);
            if (bucket == null)
            {
                throw new Exception("Couldn't get bucket with name " + bucketName);
            }
        }
        catch (IOException e)
        {
            throw new Exception("Error in reading credentials from the file", e);
        }
    }

    /**
     * {@inheritDoc}
     * Always returns true for the GCS connector.
     */
    @Override
    public boolean isWriteSupported()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContentReader getReader(String contentUrl)
    {
        String path = getPath(contentUrl);
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Creating reader with path: " + path + "; contentUrl: " + contentUrl);
        }
        return new GCSContentReader(path, contentUrl, bucket);
    }

    /**
     * Deletes the content from the GCS. 
     */
    @Override
    public boolean delete(String contentUrl)
    {
        String path = getPath(contentUrl);
        BlobId blobId = BlobId.of(this.bucket.getName(), path);
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Deleting blobId: " + blobId);
        }
        this.storage.delete(blobId);

        //If the content no longer exists, then true is returned
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContentWriter getWriterInternal(ContentReader existingContentReader, String newContentUrl) throws ContentIOException
    {
        try
        {
            String contentUrl;
            if (newContentUrl == null || StringUtils.isBlank(newContentUrl))
            {
                contentUrl = createNewUrl();
            }
            else
            {
                contentUrl = newContentUrl;
            }
            return new GCSContentWriter(getPath(contentUrl), contentUrl, existingContentReader, this.bucket);
        }
        catch (Throwable e)
        {
            throw new ContentIOException("GCSContentStore.getWriterInternal(): Failed to get writer.");
        }
    }

    /**
     * Searches for the credentials file and reads it.
     * <ul>
     *   <li>First based on the file directly on the classpath, most likely shipped within a java jar</li>
     *   <li>If not found, searches in the path given</li>
     * </ul>
     * @param keyPath path where to search for the key
     * @param keyFileName filename of the key
     * @return json key file as inputstream
     * @throws Exception
     */
    private InputStream getCredentials(String keyPath, String keyFileName) throws Exception
    {
        /*
         * We first try to get the file directly. If it can't be found we search the extension folder
         */
        InputStream is = GCSContentStore.class.getClassLoader().getResourceAsStream(keyFileName);
        if (is == null)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("The file couldn't be found in the classpath. Trying " + keyPath + keyFileName);
            }
            is = GCSContentStore.class.getClassLoader().getResourceAsStream(keyPath + keyFileName);
        }
        if (is == null)
        {
            throw new Exception("The file " + keyFileName + " was not found in the classpath.");
        }
        return is;
    }

    /**
     * Creates a new content URL path that will be used to save the content to.
     * For example for something created on the 5th of October of 2019 at 13:02 the URL generated will be
     * <code>
     *    store://2019/10/05/13/02/[uuid].bin
     * <code>
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
        /*
         * Create the URL in the format
         */
        StringBuilder sb = new StringBuilder(20);
        sb.append(FileContentStore.STORE_PROTOCOL).append(ContentStore.PROTOCOL_DELIMITER);
        sb.append(String.format("%04d", year)).append('/');
        sb.append(String.format("%02d", month)).append('/');
        sb.append(String.format("%02d", day)).append('/');
        sb.append(String.format("%02d", hour)).append('/');
        sb.append(String.format("%02d", minute)).append('/');
        sb.append(GUID.generate()).append(".bin");
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Generated new content URL: " + sb.toString());
        }
        return sb.toString();
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
