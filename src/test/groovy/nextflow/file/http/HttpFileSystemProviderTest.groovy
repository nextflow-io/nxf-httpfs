package nextflow.file.http
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
