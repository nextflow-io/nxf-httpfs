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

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomjankes.wiremock.WireMockGroovy
import org.junit.Rule

import java.nio.charset.Charset
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths

import spock.lang.Specification
import spock.lang.Stepwise

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Stepwise
class HttpFilesTests extends Specification {

    @Rule
    WireMockRule wireMockRule = new WireMockRule(18080)

    def wireMock = new WireMockGroovy(18080)

    def 'should read http file from WireMock' () {

        given:
        wireMock.stub {
            request {
                method "GET"
                url "/index.html"
            }
            response {
                status 200
                body """a
                 b
                 c
                 d
                 """
                headers {
                    "Content-Type" "text/html"
                    "Content-Length" "10"
                    "Last-Modified" "Fri, 04 Nov 2016 21:50:34 GMT"
                }
            }
        }

        when:
        def path = Paths.get(new URI('http://localhost:18080/index.html'))
        then:
        Files.size(path) == 10
        Files.getLastModifiedTime(path).toString() == "2016-11-04T21:50:34Z"
    }

    def 'should create a new file system ' () {

        given:
        def uri = new URI('http://www.nextflow.io/index.html')
        when:
        def fs = FileSystems.newFileSystem( uri, [:] )
        then:
        fs instanceof XFileSystem
        fs.provider() instanceof HttpFileSystemProvider
    }

    def 'should not create am existing file system ' () {

        given:
        def uri = new URI('http://www.nextflow.io/index.html')
        when:
        def fs = FileSystems.newFileSystem( uri, [:] )
        then:
        def ex = thrown(IllegalStateException)
        ex.message == "File system `http://www.nextflow.io` already exists"
    }

    def 'read a http file ' () {
        given:
        def uri = new URI('http://www.nextflow.io/index.html')
        when:
        def path = Paths.get(uri)
        then:
        path instanceof XPath

        when:
        def lines = Files.readAllLines(path, Charset.forName('UTF-8'))
        then:
        lines.size()>0
        lines[0] == '<html>'

//        when:
//        def bytes = Files.readAllBytes(path)
//        then:
//        new String(bytes) == ''
    }

    def 'should check if a file exits' () {

        when:
        def path1 = Paths.get(new URI('http://www.nextflow.io/index.html'))
        def path2 = Paths.get(new URI('http://www.google.com/unknown'))
        then:
        Files.exists(path1)
        Files.size(path1) > 0
        !Files.exists(path2)

    }

}
