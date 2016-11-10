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

import groovy.transform.PackageScope

/**
 * @author Emilio Palumbo
 * @author Paolo Di Tommaso
 */
@PackageScope
@CompileStatic
class XPath implements Path {

    private XFileSystem fs

    private URI base

    private Path path

    XPath(XFileSystem fs, URI base, String path, String... more) {
        this.fs = fs
        this.base = base
        this.path = Paths.get(path,more)
    }

    XPath(XFileSystem fs, String path, String... more) {
        this.fs = fs
        this.base = fs.getBaseUri()
        this.path = Paths.get(path,more)
    }


    private XPath createHttpPath(String path) {
        return (base && path.startsWith('/')
                ? new XPath(fs, base, path)
                : new XPath(fs, null, path)
                )
    }

    @Override
    FileSystem getFileSystem() {
        return fs
    }

    @Override
    boolean isAbsolute() {
        return path.isAbsolute()
    }

    @Override
    Path getRoot() {
        return createHttpPath("/")
    }

    @Override
    Path getFileName() {
        final result = path?.getFileName()?.toString()
        return result ? new XPath(fs, null, result) : null
    }

    @Override
    Path getParent() {
        String result = path.parent ? path.parent.toString() : null
        if( result ) {
            if( result != '/' ) result += '/'
            return createHttpPath(result)
        }
        return null
    }

    @Override
    int getNameCount() {
        return path ? path.nameCount : 0
    }

    @Override
    Path getName(int index) {
        return new XPath(fs, null, path.getName(index).toString())
    }

    @Override
    Path subpath(int beginIndex, int endIndex) {
        return new XPath(fs, null, path.subpath(beginIndex, endIndex).toString())
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
        null
    }

    @Override
    Path resolveSibling(Path other) {
        return resolveSibling(other.toString())
    }

    @Override
    Path resolveSibling(String other) {
        return null
    }

    @Override
    Path relativize(Path other) {
        def otherPath = ((XPath)other).path
        return createHttpPath(path.relativize(otherPath).toString())
    }

    @Override
    URI toUri() {
        base ? new URI("$base$path") : new URI("$path")
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
        return path.toString()
    }

    @Override
    WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException("Register not supported by XFileSystem")
    }

    @Override
    WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
        throw new UnsupportedOperationException("Register not supported by XFileSystem")
    }

    @Override
    Iterator<Path> iterator() {
        final len = getNameCount()
        new Iterator<Path>() {
            int index
            Path current = len ? getName(index++) : null

            @Override
            boolean hasNext() {
                return current != null
            }

            @Override
            Path next() {
                final result = current
                current = index<len ? getName(index++) : null
                return result
            }

            @Override
            void remove() {
                throw new UnsupportedOperationException("Remove operation not supported")
            }
        }
    }

    @Override
    int compareTo(Path other) {
        return this.toUri().toString() <=> other.toUri().toString()
    }

    @Override
    boolean equals(Object other) {
        if (other.class != XPath) {
            return false
        }
        final that = (XPath)other
        return this.fs == that.fs && this.base == that.base && this.path == that.path
    }

    @Override
    int hashCode() {
        return Objects.hash(fs,base,path)
    }
}
