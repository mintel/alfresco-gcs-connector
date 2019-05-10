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

    public GCSContentStore(String keyFileName, String bucketName, String rootDir) throws IOException, ParseException
    {
        this.rootDir = rootDir;
        String keyPath = "alfresco/extension/" + keyFileName;
        InputStream is = GCSContentStore.class.getClassLoader().getResourceAsStream(keyPath);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(is, "UTF-8"));
        String projectId = (String) jsonObject.get("project_id");
        if (StringUtils.isBlank(projectId))
        {
            //throw error
        }
        GoogleCredentials credentials = GoogleCredentials.fromStream(is);
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId(projectId).build().getService();
        this.bucket = storage.get(bucketName);
    }

    @Override
    public boolean isWriteSupported()
    {
        return true;
    }

    @Override
    public ContentReader getReader(String contentUrl)
    {
        return new GCSContentReader(contentUrl);
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
        sb.append(FileContentStore.STORE_PROTOCOL).append(ContentStore.PROTOCOL_DELIMITER).append(year).append('/').append(month).append('/').append(day).append('/').append(hour).append('/').append(minute).append('/').append(GUID.generate()).append(".bin");
        String newContentUrl = sb.toString();
        // done
        return newContentUrl;
    }

    public String getRelativePath(String contentUrl)
    {
        Pair<String, String> urlParts = super.getContentUrlParts(contentUrl);
        String protocol = urlParts.getFirst();
        String relativePath = urlParts.getSecond();
        return this.rootDir + '/' + relativePath;
    }
}
