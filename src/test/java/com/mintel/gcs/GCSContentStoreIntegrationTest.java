package com.mintel.gcs;

import org.alfresco.model.ContentModel;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.cleanup.EagerContentStoreCleaner;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


@RunWith(value = AlfrescoTestRunner.class)
public class GCSContentStoreIntegrationTest extends AbstractAlfrescoIT {

    private GCSContentStore store;

    /**
     *  Initialises the contentstore
     */
    @Before
    public void setupContentStore(){
        this.store = (GCSContentStore) getApplicationContext().getBean("gcsContentStore");
    }

    /**
     * A simple read and write test.
     */
    @Test
    public void readWriteTest(){
        NodeRef parentRef = this.getCompanyHomeNodeRef();
        FileInfo node = this.getServiceRegistry().getFileFolderService().create(
                parentRef,
                "readWriteTest" + System.currentTimeMillis() + ".txt",
                ContentModel.TYPE_CONTENT
        );
        NodeRef nodeRef = node.getNodeRef();
        assertNotNull(nodeRef);
        String textContent = "node content";
        addFileContent(node.getNodeRef(), textContent);

        assertEquals(textContent,readTextContent(node.getNodeRef()));

        getServiceRegistry().getNodeService().deleteNode(nodeRef);
    }

    /**
     * Tests if we can write some weird characters.
     */
    @Test
    public void emojiTest(){
        String emoji = "☊☋☌☍☎☏☐☑☒☓☔☕☖☗☘☙☚☛☜☝☞☟☠☡☢☣☤☥☦☧☨☩☪☫☬☭☮☯☰☱☲☳☴☵☶☷☸☹☺☻☼☽☾☿♀♁♂♃♄♅♆♇♈♉♊♋♌♍♎♏♐♑♒♓♔♕♖♗♘♙♚♛♜♝♞♟♠♡♢♣♤♥♦♧♨♩♪♫♬♭♮♯≰≱≲≳≴≵≶≷≸≹≺≻≼≽≾≿⊀⊁⊂⊃⊄⊅⊆⊇⊈⊉⊊⊋⊌⊍⊎⊏⊐⊑⊒⊓⊔⊕⊖⊗⊘⊙⊚⊛⊜⊝⊞⊟⊠⊡⊢⊣⊤⊥⊦⊧⊨⊩⊪⊫⊬⊭⊮⊯⊰⊱⊲⊳⊴⊵⊶⊷⊸⊹";
        NodeRef parentRef = this.getCompanyHomeNodeRef();
        FileInfo node = this.getServiceRegistry().getFileFolderService().create(
            parentRef,
            "emojiTest" + System.currentTimeMillis() + ".txt",
            ContentModel.TYPE_CONTENT
        );
        NodeRef nodeRef = node.getNodeRef();
        assertNotNull(nodeRef);
        addFileContent(node.getNodeRef(), emoji);

        assertEquals(emoji,readTextContent(node.getNodeRef()));

        getServiceRegistry().getNodeService().deleteNode(nodeRef);
    }

    /**
     * Tests if we can delete a node's content
     */
    @Test
    public void deleteNodeTest() {
        NodeRef nodeRef = this.createTestNode("testDeleteNode");
        ContentData contentData = (ContentData) this.getServiceRegistry().getNodeService().getProperty(nodeRef, ContentModel.PROP_CONTENT);
        String contentUrl = contentData.getContentUrl();

        assertTrue(store.getReader(contentUrl).exists());

        store.delete(contentUrl);

        assertFalse(store.getReader(contentUrl).exists());
    }

    /**
     * Tests if handles made-up contenturls as we could have forgotten objects during migration
     */
    @Test
    public void existsTest() {
        assertFalse("Made up contenturl shouldn't exist", store.getReader("store://2099/9/17/3/58/182ad6bb-ec25-4012-8bcf-f370b2452d3e.bin").exists());
    }

    /**
     * Tests if the integration with the eagerContentStoreCleaner is working correctly.
     */
    @Test
    public void eagerContentStoreCleanerTest(){
        EagerContentStoreCleaner eagerContentStoreCleaner = (EagerContentStoreCleaner) getApplicationContext().getBean("eagerContentStoreCleaner");
        NodeRef nodeRef = this.createTestNode("eagerContentStoreCleanerTest");
        ContentData contentData = (ContentData) this.getServiceRegistry().getNodeService().getProperty(nodeRef, ContentModel.PROP_CONTENT);
        String contentUrl = contentData.getContentUrl();
        boolean deleted = eagerContentStoreCleaner.deleteFromStores(contentUrl);
        assertTrue(deleted);
    }

    /**
     * Creates a node with some content.
     *  Will be of type cm:content and located directly under Company Home.
     *  Additionally it will have a timestamp to avoid conflicts.
     *
     * @param content Text to write as content of the node
     * @return NodeRef of created node
     */
    private NodeRef createTestNode(String content){
        NodeRef parentRef = this.getCompanyHomeNodeRef();
        FileInfo node = this.getServiceRegistry().getFileFolderService().create(
                parentRef,
                "node content test " + System.currentTimeMillis() + ".txt",
                ContentModel.TYPE_CONTENT
        );
        addFileContent(node.getNodeRef(), content);
        return node.getNodeRef();
    }

    /**
     * Add some text content to a file node
     *
     * @param nodeRef the node reference for the file that should have some text content added to it
     * @param fileContent the text content
     */
    private void addFileContent(NodeRef nodeRef, String fileContent) {
        boolean updateContentPropertyAutomatically = true;
        ContentWriter writer = getServiceRegistry().getContentService().getWriter(nodeRef, ContentModel.PROP_CONTENT,
                updateContentPropertyAutomatically);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(fileContent);
    }


    /**
     * Get the node reference for the /Company Home top folder in Alfresco.
     * Use the standard node locator service.
     *
     * @return the node reference for /Company Home
     */
    private NodeRef getCompanyHomeNodeRef() {
        return getServiceRegistry().getNodeLocatorService().getNode(CompanyHomeNodeLocator.NAME, null, null);
    }

    /**
     * Read text content for passed in file Node Reference
     *
     * @param nodeRef the node reference for a file containing text
     * @return the text content
     */
    private String readTextContent(NodeRef nodeRef) {
        ContentReader reader = getServiceRegistry().getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT);
        if (reader == null) {
            return ""; // Maybe it was a folder after all
        }

        InputStream is = reader.getContentInputStream();
        try {
            return IOUtils.toString(is, "UTF-8");
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

}