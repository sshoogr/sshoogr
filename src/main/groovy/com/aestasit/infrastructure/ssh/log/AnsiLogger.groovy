/*
 * Copyright (C) 2011-2014 Aestas/IT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aestasit.infrastructure.ssh.log

import org.fusesource.jansi.AnsiConsole
import static org.fusesource.jansi.Ansi.*
import static org.fusesource.jansi.Ansi.Color.*

/**
 * LIST OF COLORS
 *
 * "BLACK"), 
 * "RED"), 
 * "GREEN"), 
 * "YELLOW"), 
 * "BLUE"), 
 * "MAGENTA"), 
 * "CYAN"), 
 * "WHITE"),
 * "DEFAULT");
 */

class AnsiLogger implements Logger {

  public AnsiLogger() {
  	AnsiConsole.systemInstall()
  }	
  
  def void info(String message) {

    println (ansi().fg(BLUE).a(message).reset())

  }

  def void warn(String message) {
  	println (ansi().fg(YELLOW).a(message).reset())
  }

  def void debug(String message) {
    println (ansi().fg(GREEN).a(message).reset())
  }

}