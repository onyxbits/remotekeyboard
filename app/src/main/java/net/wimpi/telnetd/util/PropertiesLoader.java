//License
/***
 * Java TelnetD library (embeddable telnet daemon)
 * Copyright (c) 2000-2005 Dieter Wimberger 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS ``AS
 * IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ***/

package net.wimpi.telnetd.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Utility class implementing two simple
 * yet powerful methods for loading properties
 * (i.e. settings/configurations).
 *
 * @author Dieter Wimberger
 * @version 2.0 (16/07/2006)
 */
public final class PropertiesLoader {

    /**
     * Prevent construction of instances.
     */
    private PropertiesLoader() {
    }//constructor

    /**
     * Loads a properties file from an URL given as
     * String.
     *
     * @param url the string representing the URL.
     * @return the properties instance loaded from the given URL.
     * @throws MalformedURLException if the URL is invalid.
     * @throws IOException           if the properties cannot be loaded from
     *                               the given URL.
     */
    public static Properties loadProperties(String url)
            throws MalformedURLException, IOException {

        return loadProperties(new URL(url));
    }//loadProperties(String)

    /**
     * Loads a properties file from a given URL.
     *
     * @param url an URL instance.
     * @return the properties instance loaded from the given URL.
     * @throws IOException if the properties cannot be loaded from
     *                     the given URL.
     */
    public static Properties loadProperties(URL url)
            throws IOException {

        Properties newprops = new Properties();
        InputStream in = url.openStream();
        newprops.load(in);
        in.close();

        return newprops;
    }//loadProperties(URL)

}//class PropertiesLoader