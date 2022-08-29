package org.yangcentral.yangkit.compiler;

import java.io.File;
import java.net.URI;

/**
 * @author : frank feng
 * @date : 8/27/2022 4:59 PM
 */
public class Settings {
    private URI remoteRepository;
    private String localRepository = System.getProperty("user.home") + File.separator + ".yang";

    public URI getRemoteRepository() {
        return remoteRepository;
    }

    public void setRemoteRepository(URI remoteRepository) {
        this.remoteRepository = remoteRepository;
    }

    public String getLocalRepository() {
        return localRepository;
    }

    public void setLocalRepository(String localRepository) {
        this.localRepository = localRepository;
    }
}
