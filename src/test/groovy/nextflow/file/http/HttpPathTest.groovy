package nextflow.file.http

import spock.lang.Specification

/**
 * Created by emilio on 08/11/16.
 */
class HttpPathTest extends Specification {
    def "should return url root"() {

        given:
        def fs = Mock(HttpFileSystem)
        def p = new HttpPath(fs, "http://www.google.com/a/b/c")

        expect:
        p.getRoot() != new HttpPath(fs, "http://www.googlecom/")
        p.getRoot() == new HttpPath(fs, "http://www.google.com/")
    }
}
