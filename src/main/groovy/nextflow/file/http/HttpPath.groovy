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

import groovy.transform.CompileStatic

import java.nio.file.FileSystem
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService

/**
 * Created by emilio on 08/11/16.
 */
@CompileStatic
class HttpPath implements Path {

    private URI uri
    private HttpFileSystem fs
    private Path path

    HttpPath(HttpFileSystem fs, String url) {
        this(fs, new URI(url))
    }

    HttpPath(HttpFileSystem fs, URI uri) {
        this.uri = uri
        this.fs = fs
        this.path = Paths.get(uri.path)
    }

    private HttpPath createHttpPath(String path) {
        return new HttpPath(fs, new URI("${uri.scheme}://${uri.authority}${path}"))
    }

    @Override
    FileSystem getFileSystem() {
        return fs
    }

    @Override
    boolean isAbsolute() {
        return uri.absolute
    }

    @Override
    Path getRoot() {
        return createHttpPath("/")
    }

    @Override
    Path getFileName() {
        return path.fileName
    }

    @Override
    Path getParent() {
        return getRoot().resolve(path.parent)
    }

    @Override
    int getNameCount() {
        return path.nameCount
    }

    @Override
    Path getName(int index) {
        return path.getName(index)
    }

    @Override
    Path subpath(int beginIndex, int endIndex) {
        return path.subpath(beginIndex, endIndex)
    }

    @Override
    boolean startsWith(Path other) {
        return startsWith(other.toString())
    }

    @Override
    boolean startsWith(String other) {
        return path.startsWith(other)
    }

    @Override
    boolean endsWith(Path other) {
        return endsWith(other.toString())
    }

    @Override
    boolean endsWith(String other) {
        return path.endsWith(other)
    }

    @Override
    Path normalize() {
        return path.normalize()
    }

    @Override
    Path resolve(Path other) {
        return resolve(other.toString())
    }

    @Override
    Path resolve(String other) {
        return createHttpPath(path.resolve(other).toString())
    }

    @Override
    Path resolveSibling(Path other) {
        return resolveSibling(other.toString())
    }

    @Override
    Path resolveSibling(String other) {
        return createHttpPath(path.resolveSibling(other).toString())
    }

    @Override
    Path relativize(Path other) {
        def otherPath = ((HttpPath)other).path
        return createHttpPath(path.relativize(otherPath).toString())
    }

    @Override
    URI toUri() {
        return uri
    }

    @Override
    Path toAbsolutePath() {
        return this
    }

    @Override
    Path toRealPath(LinkOption... options) throws IOException {
        return this
    }

    @Override
    File toFile() {
        throw new UnsupportedOperationException()
    }

    @Override
    String toString() {
        return uri.toString()
    }

    @Override
    WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException("Register not supported by HttpFileSystem")
    }

    @Override
    WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
        throw new UnsupportedOperationException("Register not supported by HttpFileSystem")
    }

    @Override
    Iterator<Path> iterator() {
        return null
    }

    @Override
    int compareTo(Path other) {
        return this.toString() <=> other.toString()
    }

    @Override
    boolean equals(Object other) {
        if (other.class != HttpPath) {
            return false
        }
        def that = (HttpPath)other
        if (this.fs != that.fs) {
            return false
        }
        return this.uri == that.uri
    }

    @Override
    int hashCode() {
        return Objects.hash(fs, uri)
    }
}
