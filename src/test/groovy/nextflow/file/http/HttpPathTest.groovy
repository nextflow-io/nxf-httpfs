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

import spock.lang.Specification

/**
 * Created by emilio on 08/11/16.
 */
class HttpPathTest extends Specification {

    def 'should validate equals and hashCode' () {
        given:
        def fs = Mock(HttpFileSystem)
        when:
        def p1 = new HttpPath(fs, 'http://www.nextflow.io/a/b/c.txt')
        def p2 = new HttpPath(fs, 'http://www.nextflow.io/a/b/c.txt')
        def p3 = new HttpPath(fs, 'http://www.google.com/z.txt')

        then:
        p1 == p2
        p1 != p3
        p1.equals(p2)
        !p1.equals(p3)
        p1.hashCode() == p2.hashCode()
        p1.hashCode() != p3.hashCode()
    }


    def 'should convert to a string' () {
        given:
        def fs = Mock(HttpFileSystem)
        expect:
        new HttpPath(fs, 'http://www.nextflow.io/abc/d.txt').toString()== 'http://www.nextflow.io/abc/d.txt'
    }

    def "should return url root"() {

        given:
        def fs = Mock(HttpFileSystem)
        def p = new HttpPath(fs, "http://www.google.com/a/b/c")

        expect:
        p.getRoot() == new HttpPath(fs, "http://www.google.com/")
        p.getRoot() != new HttpPath(fs, "http://www.nextflow.io")

    }

    def 'should return file name from url' () {

        given:
        def fs = Mock(HttpFileSystem)

        when:
        def file1 = new HttpPath(fs, 'http://nextflow.io/a/b/c.txt').getFileName()
        then:
        file1 instanceof HttpPath
        file1 == new HttpPath(fs, 'c.txt')
        file1.toString() == 'c.txt'

        when:
        def file2 = new HttpPath(fs, 'http://nextflow.io/').getFileName()
        then:
        file2 == null

        when:
        def file3 = new HttpPath(fs, 'http://nextflow.io').getFileName()
        then:
        file3 == null

    }


    def 'should return if it is an absolute path'() {

        given:
        def fs = Mock(HttpFileSystem)

        expect:
        new HttpPath(fs, 'http://nextflow.io/a/b/c.txt').isAbsolute()
        new HttpPath(fs, 'http://nextflow.io/a/b/').isAbsolute()
        !new HttpPath(fs, 'http://nextflow.io/a/b/c.txt').getFileName().isAbsolute()

    }

    def 'should return parent path' () {

        given:
        def fs = Mock(HttpFileSystem)

        expect:
        new HttpPath(fs, 'http://nextflow.io/a/b/c.txt').getParent() == new HttpPath(fs, 'http://nextflow.io/a/b/')
        new HttpPath(fs, 'http://nextflow.io/a/').getParent() == new HttpPath(fs, 'http://nextflow.io/')
        new HttpPath(fs, 'http://nextflow.io/a').getParent() == new HttpPath(fs, 'http://nextflow.io/')
        new HttpPath(fs, 'http://nextflow.io/').getParent() == null
    }

    def 'should return name count' () {

        given:
        def fs = Mock(HttpFileSystem)

        expect:
        new HttpPath(fs, 'http://nextflow.io/a/b/c.txt').getNameCount() == 3
        new HttpPath(fs, 'http://nextflow.io/a/b/c.txt').getFileName().getNameCount() == 1
        new HttpPath(fs, 'http://nextflow.io/a/b/').getNameCount() == 2
        new HttpPath(fs, 'http://nextflow.io/a/b').getNameCount() == 2
        new HttpPath(fs, 'http://nextflow.io/a/').getNameCount() == 1
        new HttpPath(fs, 'http://nextflow.io/').getNameCount() == 0
        new HttpPath(fs, 'http://nextflow.io').getNameCount() == 0
    }

    def 'should return name part by index' () {
        given:
        def fs = Mock(HttpFileSystem)

        expect:
        new HttpPath(fs, 'http://nextflow.io/a/b/c.txt').getName(0) == new HttpPath(fs, 'a')
        new HttpPath(fs, 'http://nextflow.io/a/b/c.txt').getName(1) == new HttpPath(fs, 'b')
        new HttpPath(fs, 'http://nextflow.io/a/b/c.txt').getName(2) == new HttpPath(fs, 'c.txt')

        when:
        new HttpPath(fs, 'http://nextflow.io/a/b/c.txt').getName(3)
        then:
        thrown(IllegalArgumentException)

    }

    def 'should return a subpath' () {
        given:
        def fs = Mock(HttpFileSystem)
        expect:
        new HttpPath(fs, 'http://nextflow.io/a/b/c/d.txt').subpath(0,3) == new HttpPath(fs, 'a/b/c' )
        new HttpPath(fs, 'http://nextflow.io/a/b/c/d.txt').subpath(1,3) == new HttpPath(fs, 'b/c' )
        new HttpPath(fs, 'http://nextflow.io/a/b/c/d.txt').subpath(3,4) == new HttpPath(fs, 'd.txt' )
    }

    def 'should resolve a path' () {
        given:
        def fs = Mock(HttpFileSystem)

        expect:
        new HttpPath(fs, base).resolve(ext) == new HttpPath(fs,expected)
        new HttpPath(fs, base).resolve(new HttpPath(fs,ext)) == new HttpPath(fs,expected)

        where:
        base                        | ext                   | expected
        'http://nextflow.io/abc'    | 'd.txt'               | 'http://nextflow.io/abc/d.txt'
        'http://nextflow.io/abc/'   | 'd.txt'               | 'http://nextflow.io/abc/d.txt'
        'http://nextflow.io'        | 'file.txt'            | 'http://nextflow.io/file.txt'
        'http://nextflow.io/'       | 'file.txt'            | 'http://nextflow.io/file.txt'
        'alpha'                     | 'beta.txt'            | 'alpha/beta.txt'
        '/alpha'                    | 'beta.txt'            | '/alpha/beta.txt'
        'http://nextflow.io/abc/'   | '/z.txt'              | 'http://nextflow.io/z.txt'
        'http://nextflow.io/abc/'   | 'http://x.com/z.txt'  | 'http://x.com/z.xyz'

    }

    def 'should resolve sibling' () {
        given:
        def fs = Mock(HttpFileSystem)

        expect:
        new HttpPath(fs, base).resolveSibling(ext) == new HttpPath(fs,expected)
        new HttpPath(fs, base).resolveSibling(new HttpPath(fs,ext)) == new HttpPath(fs,expected)

        where:
        base                         | ext           | expected
        'http://nextflow.io/ab/c'    | 'd.txt'       | 'http://nextflow.io/ab/d.txt'
        'http://nextflow.io/ab/c/'   | 'd.txt'       | 'http://nextflow.io/ab/d.txt'
        'http://nextflow.io/ab'      | 'd.txt'       | 'http://nextflow.io/d.txt'
        'http://nextflow.io/ab/'     | 'd.txt'       | 'http://nextflow.io/d.txt'
        'http://nextflow.io/'        | 'd.txt'       | 'd.txt'
        'http://nextflow.io/a/b/c/'  | '/p/q.txt'    | 'http://nextflow.io/p/q.txt'
        'http://nextflow.io/abc/'    | 'http://x.com/z.txt'  | 'http://x.com/z.txt'
    }

    def 'should iterate over a path' () {
        given:
        def fs = Mock(HttpFileSystem)

        when:
        def itr = new HttpPath(fs, 'http://nextflow.io/a/b/c.txt').iterator()
        then:
        itr.hasNext()
        itr.next() == new HttpPath(fs,'a')
        itr.hasNext()
        itr.next() == new HttpPath(fs,'b')
        itr.hasNext()
        itr.next() == new HttpPath(fs,'c.txt')
        !itr.hasNext()
        itr.next() == null
    }

}

