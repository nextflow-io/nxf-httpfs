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
class XPathTest extends Specification {

    FtpFileSystemProvider ftpProvider

    HttpFileSystemProvider httpProvider

    HttpsFileSystemProvider httpsProvider


    def path( String str ) {
        if( str.startsWith('http:') ) {
            if(!httpProvider) httpProvider = new HttpFileSystemProvider()
            return httpProvider.getPath(str)
        }

        if( str.startsWith('https:')) {
            if(!httpsProvider) httpsProvider = new HttpsFileSystemProvider()
            return httpsProvider.getPath(str)
        }

        if( str.startsWith('ftp:')) {
            if(!ftpProvider) ftpProvider = new FtpFileSystemProvider()
            return ftpProvider.getPath(str)
        }

        throw new IllegalArgumentException('Not a valid URI scheme')
    }

    def 'should validate equals and hashCode' () {
        given:
        def fs = Mock(XFileSystem)
        fs.getBaseUri() >> new URI('')

        def fs2 = Mock(XFileSystem)
        fs2.getBaseUri() >> new URI('http://www.google.com')

        when:
        def p1 = path('http://www.nextflow.io/a/b/c.txt')
        def p2 = path('http://www.nextflow.io/a/b/c.txt')
        def p3 = path('http://www.nextflow.io/z.txt')
        def p4 = path('http://www.google.com/a/b/c.txt')

        then:
        p1 == p2
        p1 != p3
        p1 != p4
        p1.equals(p2)
        !p1.equals(p3)
        !p1.equals(p4)
        p1.hashCode() == p2.hashCode()
        p1.hashCode() != p3.hashCode()
        p1.hashCode() != p4.hashCode()
    }


    def 'should convert to a string' () {
        given:
        def fs = Mock(XFileSystem)
        fs.getBaseUri() >> new URI('http://www.nextflow.io')

        expect:
        new XPath(fs, '/abc/d.txt').toString()== 'http://www.nextflow.io/abc/d.txt'
        new XPath(fs, '/abc/d.txt').toUri() == new URI('http://www.nextflow.io/abc/d.txt')
        new XPath(fs, '/abc/d.txt').toUri().toString()== 'http://www.nextflow.io/abc/d.txt'
    }

    def "should return url root"() {

        given:
        def fs = Mock(XFileSystem)
        fs.getBaseUri() >> new URI('http://www.google.com')

        def fs2 = Mock(XFileSystem)
        fs2.getBaseUri() >> new URI('http://www.nextflow.io')

        def p = new XPath(fs, "/a/b/c")

        expect:
        p.getRoot() == new XPath(fs, "/")
        p.getRoot().toUri().toString() == 'http://www.google.com/'
        p.getRoot() != fs2.getPath('/')

    }

    def 'should return file name from url' () {

        given:
        def fs = Mock(XFileSystem)
        fs.getBaseUri() >> new URI('http://nextflow.io')

        when:
        def file1 = new XPath(fs, '/a/b/c.txt').getFileName()
        then:
        file1 instanceof XPath
        file1 == new XPath(fs, null, 'c.txt')
        file1.toString() == 'c.txt'

        when:
        def file2 = new XPath(fs, '/').getFileName()
        then:
        file2 == null

        when:
        def file3 = new XPath(fs, '').getFileName()
        then:
        file3 == null

    }


    def 'should return if it is an absolute path'() {

        given:
        def fs = Mock(XFileSystem)
        fs.getBaseUri() >> new URI('http://nextflow.io')

        expect:
        new XPath(fs, '/a/b/c.txt').isAbsolute()
        new XPath(fs, '/a/b/').isAbsolute()
        !new XPath(fs, '/a/b/c.txt').getFileName().isAbsolute()

    }

    def 'should return parent path' () {

        given:


        expect:
        new XPath(fs, 'http://nextflow.io/a/b/c.txt').getParent() == new XPath(fs, 'http://nextflow.io/a/b/')
        new XPath(fs, 'http://nextflow.io/a/').getParent() == new XPath(fs, 'http://nextflow.io/')
        new XPath(fs, 'http://nextflow.io/a').getParent() == new XPath(fs, 'http://nextflow.io/')
        new XPath(fs, 'http://nextflow.io/').getParent() == null
    }

    def 'should return name count' () {

        given:
        def fs = Mock(XFileSystem)

        expect:
        new XPath(fs, 'http://nextflow.io/a/b/c.txt').getNameCount() == 3
        new XPath(fs, 'http://nextflow.io/a/b/c.txt').getFileName().getNameCount() == 1
        new XPath(fs, 'http://nextflow.io/a/b/').getNameCount() == 2
        new XPath(fs, 'http://nextflow.io/a/b').getNameCount() == 2
        new XPath(fs, 'http://nextflow.io/a/').getNameCount() == 1
        new XPath(fs, 'http://nextflow.io/').getNameCount() == 0
        new XPath(fs, 'http://nextflow.io').getNameCount() == 0
    }

    def 'should return name part by index' () {
        given:
        def fs = Mock(XFileSystem)

        expect:
        new XPath(fs, 'http://nextflow.io/a/b/c.txt').getName(0) == new XPath(fs, 'a')
        new XPath(fs, 'http://nextflow.io/a/b/c.txt').getName(1) == new XPath(fs, 'b')
        new XPath(fs, 'http://nextflow.io/a/b/c.txt').getName(2) == new XPath(fs, 'c.txt')

        when:
        new XPath(fs, 'http://nextflow.io/a/b/c.txt').getName(3)
        then:
        thrown(IllegalArgumentException)

    }

    def 'should return a subpath' () {
        given:
        def fs = Mock(XFileSystem)
        expect:
        new XPath(fs, 'http://nextflow.io/a/b/c/d.txt').subpath(0,3) == new XPath(fs, 'a/b/c' )
        new XPath(fs, 'http://nextflow.io/a/b/c/d.txt').subpath(1,3) == new XPath(fs, 'b/c' )
        new XPath(fs, 'http://nextflow.io/a/b/c/d.txt').subpath(3,4) == new XPath(fs, 'd.txt' )
    }

    def 'should resolve a path' () {
        given:
        def fs = Mock(XFileSystem)

        expect:
        new XPath(fs, base).resolve(ext) == new XPath(fs,expected)
        new XPath(fs, base).resolve(new XPath(fs,ext)) == new XPath(fs,expected)

        where:
        base                        | ext                   | expected
        'http://nextflow.io/abc'    | 'd.txt'               | 'http://nextflow.io/abc/d.txt'
        'http://nextflow.io/abc/'   | 'd.txt'               | 'http://nextflow.io/abc/d.txt'
        'http://nextflow.io'        | 'file.txt'            | 'http://nextflow.io/file.txt'
        'http://nextflow.io/'       | 'file.txt'            | 'http://nextflow.io/file.txt'
        'alpha'                     | 'beta.txt'            | 'alpha/beta.txt'
        '/alpha'                    | 'beta.txt'            | '/alpha/beta.txt'
        'http://nextflow.io/abc/'   | '/z.txt'              | 'http://nextflow.io/z.txt'
        'http://nextflow.io/abc/'   | 'http://x.com/z.txt'  | 'http://x.com/z.txt'

    }

    def 'should resolve sibling' () {
        given:
        def fs = Mock(XFileSystem)

        expect:
        new XPath(fs, base).resolveSibling(ext) == new XPath(fs,expected)
        new XPath(fs, base).resolveSibling(new XPath(fs,ext)) == new XPath(fs,expected)

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
        def fs = Mock(XFileSystem)

        when:
        def itr = new XPath(fs, 'http://nextflow.io/a/b/c.txt').iterator()
        then:
        itr.hasNext()
        itr.next() == new XPath(fs,'a')
        itr.hasNext()
        itr.next() == new XPath(fs,'b')
        itr.hasNext()
        itr.next() == new XPath(fs,'c.txt')
        !itr.hasNext()
        itr.next() == null
    }

}

