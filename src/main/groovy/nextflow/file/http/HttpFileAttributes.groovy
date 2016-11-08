package nextflow.file.http

import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime

/**
 * Created by emilio on 08/11/16.
 */
class HttpFileAttributes implements BasicFileAttributes {

    private FileTime lastModifiedTime
    private long size

    HttpFileAttributes(FileTime lastModifiedTime, long size) {
        this.lastModifiedTime = lastModifiedTime
        this.size = size
    }

    @Override
    FileTime lastModifiedTime() {
        return lastModifiedTime
    }

    @Override
    FileTime lastAccessTime() {
        return null
    }

    @Override
    FileTime creationTime() {
        return null
    }

    @Override
    boolean isRegularFile() {
        return true
    }

    @Override
    boolean isDirectory() {
        return false
    }

    @Override
    boolean isSymbolicLink() {
        return false
    }

    @Override
    boolean isOther() {
        return false
    }

    @Override
    long size() {
        return size
    }

    @Override
    Object fileKey() {
        return null
    }
}
