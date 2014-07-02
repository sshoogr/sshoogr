package com.aestasit.ssh

import com.aestasit.ssh.dsl.SshDslEngine
import org.junit.Before

/**
 * Created by aad on 30-06-2014.
 */
class BaseIntegrationTest {

  SshOptions options
  SshDslEngine engine

  @Before
  void defineOptions() {
    options = new SshOptions()
    options.with {

      logger = systemOut()

      defaultHost = '192.168.33.144'
      defaultUser = 'vagrant'
      defaultPassword = 'vagrant'
      defaultPort = 22

      reuseConnection = true
      trustUnknownHosts = true

      verbose = true

      execOptions.with {
        showOutput = true
      }

    }
    engine = new SshDslEngine(options)
  }

}
