package nextflow.file.http

import groovy.transform.CompileStatic

import java.nio.file.FileSystem
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService

/**
 * Created by emilio on 08/11/16.
 */
@CompileStatic
class HttpPath implements Path {

    private URI uri

    HttpPath(String url) {
        this(new URI(url))
    }

    HttpPath(URI uri) {
        this.uri = uri

    }

    @Override
    FileSystem getFileSystem() {
        return null
    }

    @Override
    boolean isAbsolute() {
        return false
    }

    @Override
    Path getRoot() {
        return null
    }

    @Override
    Path getFileName() {
        return null
    }

    @Override
    Path getParent() {
        return null
    }

    @Override
    int getNameCount() {
        return 0
    }

    @Override
    Path getName(int index) {
        return null
    }

    @Override
    Path subpath(int beginIndex, int endIndex) {
        return null
    }

    @Override
    boolean startsWith(Path other) {
        return false
    }

    @Override
    boolean startsWith(String other) {
        return false
    }

    @Override
    boolean endsWith(Path other) {
        return false
    }

    @Override
    boolean endsWith(String other) {
        return false
    }

    @Override
    Path normalize() {
        return null
    }

    @Override
    Path resolve(Path other) {
        return null
    }

    @Override
    Path resolve(String other) {
        return null
    }

    @Override
    Path resolveSibling(Path other) {
        return null
    }

    @Override
    Path resolveSibling(String other) {
        return null
    }

    @Override
    Path relativize(Path other) {
        return null
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
        return null
    }

    @Override
    WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
        return null
    }

    @Override
    Iterator<Path> iterator() {
        return null
    }

    @Override
    int compareTo(Path other) {
        return 0
    }
}
