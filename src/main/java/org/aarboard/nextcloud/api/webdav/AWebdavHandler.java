/*
 * Copyright (C) 2018 a.schild
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.aarboard.nextcloud.api.webdav;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.aarboard.nextcloud.api.ServerConfig;
import org.aarboard.nextcloud.api.exception.NextcloudApiException;
import org.aarboard.nextcloud.api.provisioning.ProvisionConnector;
import org.aarboard.nextcloud.api.provisioning.User;
import org.aarboard.nextcloud.api.webdav.pathresolver.NextcloudVersion;
import org.aarboard.nextcloud.api.webdav.pathresolver.WebDavPathResolver;
import org.aarboard.nextcloud.api.webdav.pathresolver.WebDavPathResolverBuilder;
import org.aarboard.nextcloud.api.webdav.pathresolver.WebDavPathResolverBuilder.TYPE;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineImpl;

/**
 *
 * @author a.schild
 */
public abstract class AWebdavHandler
{

    private static final Logger LOG = LoggerFactory.getLogger(AWebdavHandler.class);

    public static final int  FILE_BUFFER_SIZE= 4096;
    public static String WEB_DAV_BASE_PATH = "remote.php/webdav/";
    
    private final ServerConfig _serverConfig;

    private WebDavPathResolver resolver;

    private String nextcloudServerVersion;

    public AWebdavHandler(ServerConfig serverConfig)
    {
        _serverConfig = serverConfig;
    }

    public void setWebDavPathResolver(final WebDavPathResolver resolver)
    {
        this.resolver = resolver;
    }

    /**
     * @return the nextcloud server instance version
     */
    public String getServerVersion()
    {
        if (null == nextcloudServerVersion)
        {
            resolveNextcloudServerVersion();
        }

        return nextcloudServerVersion;
    }

    private void resolveNextcloudServerVersion()
    {
        final WebDavPathResolver versionResolver = WebDavPathResolverBuilder.get(WebDavPathResolverBuilder.TYPE.VERSION).withBasePathPrefix(_serverConfig.getSubPathPrefix()).build();

        final String url = buildWebdavPath(versionResolver, "");
        final Sardine sardine = buildAuthSardine();

        try (final InputStream inputStream = sardine.get(url))
        {
            final String json = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));

            //TODO parse with proper json api
            nextcloudServerVersion = Arrays.asList(json.split(",")).stream().filter(x -> x.contains("version")).map(x -> x.split(":")[1]).findAny().orElse("20.0").replaceAll("\"", "");

        }
        catch (IOException ex)
        {
            throw new NextcloudApiException(ex);
        }
        finally
        {
            try
            {
                sardine.shutdown();
            }
            catch (IOException ex)
            {
                LOG.warn("error in closing sardine connector", ex);
            }
        }

    }

    /**
     * Defaults to FILES resolver
     *
     * @return the resolver
     * @since 11.5
     */
    protected WebDavPathResolver getWebDavPathResolver()
    {
        if (null == this.resolver) {
            this.resolver = getWebDavPathResolver(TYPE.FILES);
        }
        return this.resolver;
    }

    protected WebDavPathResolver getWebDavPathResolver(WebDavPathResolverBuilder.TYPE type) {
        try (ProvisionConnector pc = new ProvisionConnector(_serverConfig)) {
            User currentUser = pc.getCurrentUser();
            return WebDavPathResolverBuilder.get(type)//
                    .ofVersion(NextcloudVersion.get(getServerVersion()))
                    .withUserName(currentUser.getId())
                    .withBasePathSuffix(type.getSuffix())
                    .withBasePathPrefix(_serverConfig.getSubPathPrefix())
                    .build();
        } catch (IOException ex) {
            LOG.error("error in closing ProvisionConnector connector", ex);
        }
        return null;
    }

    /**
     * Build the full URL for the webdav access to a resource
     *
     * @param remotePath remote path for file (Not including remote.php/webdav/)
     * @return Full URL including http....
     */
    protected String buildWebdavPath(String remotePath)
    {
        return buildWebdavPath(getWebDavPathResolver(), remotePath);
    }

    protected String buildWebdavPath(WebDavPathResolver resolver, String remotePath)
    {
        URIBuilder uB = new URIBuilder()
                .setScheme(_serverConfig.isUseHTTPS() ? "https" : "http")
                .setHost(_serverConfig.getServerName())
                .setPort(_serverConfig.getPort())
                .setPath(resolver.getWebDavPath(remotePath));
        return uB.toString();
    }

    protected String getWebdavPathPrefix()
    {
        if (resolver != null)
        {
            return resolver.getWebDavPath();
        }
        else
        {
            return "/"+WEB_DAV_BASE_PATH;
        }
    }
    
    /**
     * Create a authenticate sardine connector
     *
     * @return sardine connector to server including authentication
     */
    protected Sardine buildAuthSardine()
    {
        if (_serverConfig.getAuthenticationConfig().usesBasicAuthentication()) {
            Sardine sardine = SardineFactory.begin();
            sardine.setCredentials(_serverConfig.getUserName(),
                    _serverConfig.getAuthenticationConfig().getPassword());
            sardine.enablePreemptiveAuthentication(_serverConfig.getServerName());
            return sardine;
        }
        Sardine sardine = new SardineImpl(_serverConfig.getAuthenticationConfig().getBearerToken());
        return sardine;
    }

    /**
     * method to check if a remote object already exists
     *
     * @param remotePath path of the file/folder
     * @return boolean value if the given file/folder exists or not
     */
    public boolean pathExists(String remotePath)
    {
        String path = buildWebdavPath(remotePath);
        Sardine sardine = buildAuthSardine();

        try
        {
            return sardine.exists(path);
        }
        catch (IOException e)
        {
            throw new NextcloudApiException(e);
        }
        finally
        {
            try
            {
                sardine.shutdown();
            }
            catch (IOException ex)
            {
                LOG.warn("error in closing sardine connector", ex);
            }
        }
    }

    /**
     * Deletes the file/folder at the specified path
     *
     * @param remotePath path of the file/folder
     */
    public void deletePath(String remotePath)
    {
        String path = buildWebdavPath(remotePath);

        Sardine sardine = buildAuthSardine();
        try
        {
            sardine.delete(path);
        }
        catch (IOException e)
        {
            throw new NextcloudApiException(e);
        }
        finally
        {
            try
            {
                sardine.shutdown();
            }
            catch (IOException ex)
            {
                LOG.warn("error in closing sardine connector", ex);
            }
        }
    }

    /**
     * Rename the file/folder at the specified path
     *
     * @param oldPath path of the original file/folder
     * @param newPath path of the new file/folder
     * @param overwriteExisting Should an existing target be overwritten?
     */
    public void renamePath(String oldPath, String newPath, boolean overwriteExisting)
    {
        String oldWEBDavpath=  buildWebdavPath( oldPath );
        String newWEBDavpath=  buildWebdavPath( newPath );

        Sardine sardine = buildAuthSardine();
        try {
            sardine.move(oldWEBDavpath, newWEBDavpath, overwriteExisting);
        } catch (IOException e) {
            throw new NextcloudApiException(e);
        }
        finally
        {
            try
            {
                sardine.shutdown();
            }
            catch (IOException ex)
            {
                LOG.warn("error in closing sardine connector", ex);
            }
        }
    }
}
