package org.aarboard.nextcloud.api.webdav;


import java.io.IOException;

import org.aarboard.nextcloud.api.ServerConfig;
import org.aarboard.nextcloud.api.exception.NextcloudApiException;
import org.aarboard.nextcloud.api.webdav.pathresolver.WebDavPathResolver;
import org.aarboard.nextcloud.api.webdav.pathresolver.WebDavPathResolverBuilder.TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sardine.Sardine;

/**
 *
 * @author tott
 *
 */
public class Uploads extends Files {
    private static final String COMMIT_UPLOAD_SUFFIX = "/.file";
    private static final Logger LOG = LoggerFactory.getLogger(Uploads.class);

    public Uploads(ServerConfig serverConfig) {
        super(serverConfig);
    }

    /**
     * Defaults to FILES resolver
     *
     * @return the resolver
     * @since 11.5
     */
    @Override
    protected WebDavPathResolver getWebDavPathResolver() {
        return getWebDavPathResolver(TYPE.UPLOADS);
    }

    public void beginUpload(String uploadName) {
        String path = buildWebdavPath(uploadName);
        Sardine sardine = buildAuthSardine();

        try {
            sardine.createDirectory(path);
        } catch (IOException e) {
            throw new NextcloudApiException(e);
        } finally {
            try {
                sardine.shutdown();
            } catch (IOException ex) {
                LOG.warn("error in closing sardine connector", ex);
            }
        }
    }

    public void commitUpload(String uploadName, String destinationPath) {
        String sourcePath = buildWebdavPath(uploadName + COMMIT_UPLOAD_SUFFIX);
        String targetPath = buildWebdavPath(getWebDavPathResolver(TYPE.FILES), destinationPath);
        Sardine sardine = buildAuthSardine();

        try {
            sardine.move(sourcePath, targetPath);
        } catch (IOException e) {
            throw new NextcloudApiException(e);
        } finally {
            try {
                sardine.shutdown();
            } catch (IOException ex) {
                LOG.warn("error in closing sardine connector", ex);
            }
        }
    }

    public void abortUpload(String uniqueTemporaryFolderName) {
        String path = buildWebdavPath(uniqueTemporaryFolderName);
        Sardine sardine = buildAuthSardine();
        try {
            sardine.delete(path);
        } catch (IOException e) {
            throw new NextcloudApiException(e);
        } finally {
            try {
                sardine.shutdown();
            } catch (IOException ex) {
                LOG.warn("error in closing sardine connector", ex);
            }
        }
    }
}
