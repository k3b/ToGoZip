package de.k3b.io;

import java.io.File;
import java.net.URI;

/**
 * Created by EVE on 12.12.2017.
 */

public class IFile extends File {
    /**
     * Constructs a new file using the specified path.
     *
     * @param path the path to be used for the file.
     * @throws NullPointerException if {@code path} is {@code null}.
     */
    public IFile(String path) {
        super(path);
    }

    public IFile(IFile f, String path) {
        super(f, path);
    }
    /**
     * Constructs a new File using the path of the specified URI. {@code uri}
     * needs to be an absolute and hierarchical Unified Resource Identifier with
     * file scheme and non-empty path component, but with undefined authority,
     * query or fragment components.
     *
     * @param uri the Unified Resource Identifier that is used to construct this
     *            file.
     * @throws NullPointerException     if {@code uri == null}.
     * @throws IllegalArgumentException if {@code uri} does not comply with the conditions above.
     * @see #toURI
     * @see URI
     */
    public IFile(URI uri) {
        super(uri);
    }

    public boolean exists() {
        return super.exists();
    }

    public boolean delete() {
        return super.delete();
    }
}