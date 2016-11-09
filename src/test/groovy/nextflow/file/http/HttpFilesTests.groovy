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

import java.nio.charset.Charset
import java.nio.file.FileSystems
import java.nio.file.Files

import spock.lang.Specification
import spock.lang.Stepwise
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Stepwise
class HttpFilesTests extends Specification {

    def 'should create a new file system ' () {

        given:
        def uri = new URI('http://www.google.com/index.html')
        when:
        def fs = FileSystems.newFileSystem( uri, [:] )
        then:
        fs instanceof HttpFileSystem
        fs.provider() instanceof HttpFileSystemProvider
    }

    def 'read a http file ' () {
        given:
        def uri = new URI('http://www.nextflow.io/index.html')

        when:
        def fs = FileSystems.getFileSystem(uri)
        def path = fs.getPath('http://www.nextflow.io/index.html')
        then:
        path instanceof HttpPath

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

        given:
        def uri = new URI('http://www.nextflow.io/index.html')
        def fs = FileSystems.getFileSystem(uri)

        when:
        def path1 = fs.getPath('http://www.nextflow.io/index.html')
        def path2 = fs.getPath('http://www.google.com/unknown')
        then:
        Files.exists(path1)
        Files.size(path1) > 0
        !Files.exists(path2)

    }

}
