package nextflow.file.http

import spock.lang.Specification

import java.nio.file.attribute.FileTime
import java.time.Instant

/**
 * Created by emilio on 08/11/16.
 */
class HttpFileSystemProviderTest extends Specification {

    def "should return input stream"() {
        given:
        def fs = Mock(HttpFileSystem)
        def fsp = new HttpFileSystemProvider()

        when:
        def stream = fsp.newInputStream(new HttpPath(fs, "http://www.google.com/index.html"))

        then:
        stream.text.startsWith("<!doctype html>")
    }

    def "should read file attributes from map"() {
        given:
        def fs = new HttpFileSystemProvider()
        def attrMap = ['Last-Modified': ['Fri, 04 Nov 2016 21:50:34 GMT'], 'Content-Length': ['21729'] ]

        when:
        def attrs = fs.readHttpAttributes(attrMap)

        then:
        attrs.lastModifiedTime() == FileTime.from(Instant.parse('2016-11-04T21:50:34Z'))
        attrs.size == 21729
    }

    def "should read file attributes from HttpPath"() {
        given:
        def fs = Mock(HttpFileSystem)
        def fsp = new HttpFileSystemProvider()

        when:
        def attrs = fsp.readHttpAttributes(new HttpPath(fs, "http://www.nextflow.io/index.html"))

        then:
        attrs.lastModifiedTime() == null
        attrs.size > 0
    }
}
