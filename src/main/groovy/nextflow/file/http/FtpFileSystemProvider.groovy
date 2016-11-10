package nextflow.file.http

import groovy.transform.CompileStatic

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class FtpFileSystemProvider extends XFileSystemProvider {

    @Override
    String getScheme() {
        return 'ftp'
    }
}
