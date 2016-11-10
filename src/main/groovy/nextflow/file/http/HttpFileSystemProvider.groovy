package nextflow.file.http

import groovy.transform.CompileStatic

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class HttpFileSystemProvider extends XFileSystemProvider {

    private static final String SCHEME = 'http'

    @Override
    String getScheme() {
        return SCHEME
    }
}
