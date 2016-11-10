/*
 * Copyright (c) 2013-2016, Centre for Genomic Regulation (CRG).
 * Copyright (c) 2013-2016, Paolo Di Tommaso and the respective authors.
 *
 *   This file is part of 'Nextflow'.
 *
 *   Nextflow is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Nextflow is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Nextflow.  If not, see <http://www.gnu.org/licenses/>.
 */

package nextflow.file.http
import java.nio.channels.SeekableByteChannel
import java.nio.file.AccessMode
import java.nio.file.CopyOption
import java.nio.file.DirectoryStream
import java.nio.file.FileStore
import java.nio.file.FileSystem
import java.nio.file.FileSystemNotFoundException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileAttributeView
import java.nio.file.attribute.FileTime
import java.nio.file.spi.FileSystemProvider
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

import groovy.transform.CompileStatic
import groovy.transform.PackageScope

/**
 * Created by emilio on 08/11/16.
 */
@PackageScope
@CompileStatic
abstract class XFileSystemProvider extends FileSystemProvider {

    private Map<URI, FileSystem> fileSystemMap = [:]

    private URI key(String s, String a) {
        new URI("$s://$a")
    }

    private URI key(URI uri) {
        new URI("${uri.scheme}://${uri.authority}")
    }

    @Override
    FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        final scheme = uri.scheme.toLowerCase()
        final authority = uri.authority.toLowerCase()

        if( scheme != this.getScheme() )
            throw new IllegalArgumentException("Not a valid ${getScheme().toUpperCase()} scheme: $scheme")

        final base = key(scheme,authority)
        if (fileSystemMap.containsKey(base))
            throw new IllegalStateException("File system `$base` already exists")
        def result = new XFileSystem(this, base)
        fileSystemMap[base] = result
        return result
    }

    @Override
    FileSystem getFileSystem(URI uri) {
        assert fileSystemMap != null

        def scheme = uri.scheme.toLowerCase()
        def authority = uri.authority.toLowerCase()

        if( scheme != this.getScheme() )
            throw new IllegalArgumentException("Not a valid ${getScheme().toUpperCase()} scheme: $scheme")

        def key = new URI("$scheme://$authority")
        def result = fileSystemMap[key]
        if( !result )
            throw new FileSystemNotFoundException("File system not found: $key")

        return result
    }

    @Override
    Path getPath(URI uri) {
        return getFileSystem(uri).getPath(uri.path)
    }

    @Override
    SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException("NewByteChannel not supported by XFileSystem")
    }

    /**
     * Opens a file, returning an input stream to read from the file. This
     * method works in exactly the manner specified by the {@link
     * Files#newInputStream} method.
     *
     * <p> The default implementation of this method opens a channel to the file
     * as if by invoking the {@link #newByteChannel} method and constructs a
     * stream that reads bytes from the channel. This method should be overridden
     * where appropriate.
     *
     * @param   path
     *          the path to the file to open
     * @param   options
     *          options specifying how the file is opened
     *
     * @return  a new input stream
     *
     * @throws  IllegalArgumentException
     *          if an invalid combination of options is specified
     * @throws  UnsupportedOperationException
     *          if an unsupported option is specified
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file.
     */
    @Override
    public InputStream newInputStream(Path path, OpenOption... options)
            throws IOException
    {
        if (path.class != XPath) {
            throw new IllegalArgumentException("Illegal path")
        }
        if (options.length > 0) {
            for (OpenOption opt: options) {
                // All OpenOption values except for APPEND and WRITE are allowed
                if (opt == StandardOpenOption.APPEND ||
                        opt == StandardOpenOption.WRITE)
                    throw new UnsupportedOperationException("'" + opt + "' not allowed");
            }
        }
        return new URL(path.toUri().toString()).newInputStream()
    }

    /**
     * Opens or creates a file, returning an output stream that may be used to
     * write bytes to the file. This method works in exactly the manner
     * specified by the {@link Files#newOutputStream} method.
     *
     * <p> The default implementation of this method opens a channel to the file
     * as if by invoking the {@link #newByteChannel} method and constructs a
     * stream that writes bytes to the channel. This method should be overridden
     * where appropriate.
     *
     * @param   path
     *          the path to the file to open or create
     * @param   options
     *          options specifying how the file is opened
     *
     * @return  a new output stream
     *
     * @throws  IllegalArgumentException
     *          if {@code options} contains an invalid combination of options
     * @throws  UnsupportedOperationException
     *          if an unsupported option is specified
     * @throws  IOException
     *          if an I/O error occurs
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
     *          method is invoked to check write access to the file. The {@link
     *          SecurityManager#checkDelete(String) checkDelete} method is
     *          invoked to check delete access if the file is opened with the
     *          {@code DELETE_ON_CLOSE} option.
     */
    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options)
            throws IOException
    {
        throw new UnsupportedOperationException("Write not supported by XFileSystem")
    }

    @Override
    DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        throw new UnsupportedOperationException("Direcotry listing unsupported by XFileSystem")
    }

    @Override
    void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException("Create directory unsupportedby XFileSystem")
    }

    @Override
    void delete(Path path) throws IOException {
        throw new UnsupportedOperationException("Delete unsupported by XFileSystem")
    }

    @Override
    void copy(Path source, Path target, CopyOption... options) throws IOException {

    }

    @Override
    void move(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException("Move not supported by XFileSystem")
    }

    @Override
    boolean isSameFile(Path path, Path path2) throws IOException {
        return path == path2
    }

    @Override
    boolean isHidden(Path path) throws IOException {
        return path.getFileName().startsWith('.')
    }

    @Override
    FileStore getFileStore(Path path) throws IOException {
        throw new UnsupportedOperationException("File store not supported by XFileSystem")
    }

    @Override
    void checkAccess(Path path, AccessMode... modes) throws IOException {
        readAttributes(path, XFileAttributes)
    }

    @Override
    def <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        return null
    }

    @Override
    def <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        if ( type == BasicFileAttributes || type == XFileAttributes) {
            def p = (XPath) path
            def attrs = (A)readHttpAttributes(p)
            if (attrs == null) {
                throw new IOException("Unable to access path: ${p.toString()}")
            }
            return attrs
        }
        throw new UnsupportedOperationException("Not a valid HTTP file attribute type: $type")
    }

    @Override
    Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Read file attributes no supported by XFileSystem")
    }

    @Override
    void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Set file attributes not supported by XFileSystem")
    }

    protected XFileAttributes readHttpAttributes(XPath path) {
        def conn = (HttpURLConnection)path.toUri().toURL().openConnection()
        if ( conn.getResponseCode() in [200, 301, 302]) {
            def header = conn.getHeaderFields()
            return readHttpAttributes(header)
        }
        return null
    }

    protected XFileAttributes readHttpAttributes(Map<String,List<String>> header) {
        def lastMod = header.get("Last-Modified")?.get(0)
        long contentLen = header.get("Content-Length")?.get(0)?.toLong() ?: 0
        def dateFormat = new SimpleDateFormat('E, dd MMM yyyy HH:mm:ss Z')
        def modTime = lastMod ? FileTime.from(dateFormat.parse(lastMod).time, TimeUnit.MILLISECONDS) : (FileTime)null
        new XFileAttributes(modTime, contentLen)
    }


    XPath getPath(String str) {
        def uri = new URI(str)

        if( !fileSystemMap.containsKey(key(uri)) ) {
            newFileSystem(uri,[:])
        }

        return (XPath)getPath(uri)
    }
}
