package com.aestasit.infrastructure.ssh

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

/**
 * RemoteURL parsing test.
 *
 * @author Jason Darby
 */
@RunWith(Parameterized.class)
class RemoteURLParsingTest {

    @Parameters
    static Collection<Object[]> data() {
        [
                ['username:password@host:5', 'username', 'password', 'host', 5] as Object[],
                ['username:p@ssword@host:5', 'username', 'p@ssword', 'host', 5] as Object[],
                ['username:password@host', 'username', 'password', 'host', 22] as Object[],
                ['username:p@ssword@host', 'username', 'p@ssword', 'host', 22] as Object[],
                ['username@host:5', 'username', null, 'host', 5] as Object[],
                ['host', null, null, 'host', 22] as Object[]
        ]
    }

    String url
    String expectedUsername
    String expectedPassword
    String expectedHost
    Integer expectedPort

    RemoteURLParsingTest(String url, String username, String password, String host, Integer port) {
        this.url = url
        this.expectedUsername = username
        this.expectedPassword = password
        this.expectedHost = host
        this.expectedPort = port
    }

    @Test
    void someTest() {
        new RemoteURL(url).with {
            assert user == expectedUsername
            assert password == expectedPassword
            assert host == expectedHost
            assert port == expectedPort
        }
    }

}
