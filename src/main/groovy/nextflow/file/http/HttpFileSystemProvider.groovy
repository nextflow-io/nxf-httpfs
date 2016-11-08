package nextflow.file.http

import groovy.transform.CompileStatic
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

import java.nio.channels.Channels
import java.nio.channels.SeekableByteChannel
import java.nio.file.AccessMode
import java.nio.file.CopyOption
import java.nio.file.DirectoryStream
import java.nio.file.FileStore
import java.nio.file.FileSystem
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
import java.time.Instant

/**
 * Created by emilio on 08/11/16.
 */
@CompileStatic
class HttpFileSystemProvider extends FileSystemProvider {

    static final String SCHEME = 'http'

    private HttpFileSystem httpfs

    @Override
    String getScheme() {
        return SCHEME
    }

    @Override
    FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        if (uri.getScheme() != SCHEME)
            throw new IllegalArgumentException("Illegal uri scheme")
        if (httpfs)
            throw new IllegalStateException("File system already exists")
        httpfs = new HttpFileSystem(uri)
        return httpfs
    }

    @Override
    FileSystem getFileSystem(URI uri) {
        if (uri.getScheme() != SCHEME)
            throw new IllegalArgumentException("Illegal uri scheme")
        return httpfs
    }

    @Override
    Path getPath(URI uri) {
        return new HttpPath(httpfs, uri)
    }

    @Override
    SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException("NewByteChannel not supported by HttpFileSystem")
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
        if (path.class != HttpPath) {
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
        throw new UnsupportedOperationException("Write not supported by HttpFileSystem")
    }

    @Override
    DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        throw new UnsupportedOperationException("Direcotry listing unsupported by HttpFileSystem")
    }

    @Override
    void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException("Create directory unsupportedby HttpFileSystem")
    }

    @Override
    void delete(Path path) throws IOException {
        throw new UnsupportedOperationException("Delete unsupported by HttpFileSystem")
    }

    @Override
    void copy(Path source, Path target, CopyOption... options) throws IOException {

    }

    @Override
    void move(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException("Move not supported by HttpFileSystem")
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
        throw new UnsupportedOperationException("File store not supported by HttpFileSystem")
    }

    @Override
    void checkAccess(Path path, AccessMode... modes) throws IOException {

    }

    @Override
    def <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        return null
    }

    @Override
    def <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        if ( type == BasicFileAttributes || type == HttpFileAttributes) {
            def p = (HttpPath) path
            return (A)readHttpAttributes(p)
        }
        throw new UnsupportedOperationException("Not a valid HTTP file attribute type: $type")
    }

    @Override
    Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Read file attributes no supported by HttpFileSystem")
    }

    @Override
    void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Set file attributes not supported by HttpFileSystem")
    }

    protected HttpFileAttributes readHttpAttributes(HttpPath path) {
        def conn = path.toUri().toURL().openConnection()
        def header = conn.getHeaderFields()
        readHttpAttributes(header)
    }

    protected HttpFileAttributes readHttpAttributes(Map<String,List<String>> header) {
        def lastMod = header.get("Last-Modified")?.get(0)
        def contentLen = header.get("Content-Length")?.get(0)?.toLong()
        def dateFormat = new SimpleDateFormat('E, dd MMM yyyy HH:mm:ss Z')
        def modTime = lastMod ? FileTime.from(dateFormat.parse(lastMod).toInstant()) : (FileTime)null
        new HttpFileAttributes(modTime, contentLen)
    }
}
