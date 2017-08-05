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

package net.wimpi.telnetd;

import android.util.Log;

import net.wimpi.telnetd.io.terminal.TerminalManager;
import net.wimpi.telnetd.net.PortListener;
import net.wimpi.telnetd.shell.ShellManager;
import net.wimpi.telnetd.util.PropertiesLoader;
import net.wimpi.telnetd.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Class that implements a configurable and embeddable
 * telnet daemon.
 *
 * @author Dieter Wimberger
 * @version 2.0 (16/07/2006)
 */
public class TelnetD {


    private static final String TAG = TelnetD.class.getSimpleName();
    private static final String VERSION = "2.0";
    private static TelnetD c_Self = null;    //reference of the running singleton
    private List listeners;
    private ShellManager shellManager;

    /**
     * Constructor creating a TelnetD instance.<br>
     * Private so that only  the factory method can create the
     * singleton instance.
     */
    private TelnetD() {
        c_Self = this;    //sets the singleton reference
        listeners = new ArrayList(5);
    }//constructor

    /**
     * Factory method to create a TelnetD Instance.
     *
     * @param main Properties object with settings for the TelnetD.
     * @return TenetD instance that has been properly set up according to the
     * passed in properties, and is ready to start serving.
     * @throws BootException if the setup process fails.
     */
    public static TelnetD createTelnetD(Properties main)
            throws BootException {

        if (c_Self == null) {
            TelnetD td = new TelnetD();
            td.prepareShellManager(main);
            td.prepareTerminals(main);
            String[] listnames = StringUtil.split(main.getProperty("listeners"), ",");
            for (int i = 0; i < listnames.length; i++) {
                td.prepareListener(listnames[i], main);
            }
            return td;
        } else {
            throw new BootException("Singleton already instantiated.");
        }

    }//createTelnetD

    /**
     * Factory method to create a TelnetD singleton instance,
     * loading the standard properties files from the given
     * String containing an URL location.<br>
     *
     * @param urlprefix String containing an URL prefix.
     * @return TenetD instance that has been properly set up according to the
     * passed in properties, and is ready to start serving.
     * @throws BootException if the setup process fails.
     */
    public static TelnetD createTelnetD(String urlprefix)
            throws BootException {

        try {
            return createTelnetD(PropertiesLoader.loadProperties(urlprefix));
        } catch (IOException ex) {
            Log.e(TAG, String.valueOf(ex));
            throw new BootException("Failed to load configuration from given URL.");
        }
    }//createTelnetD

    /**
     * creates an independant instance of the telnetd object, no setup.
     */
    public static TelnetD createTelnetD() {
        TelnetD td = new TelnetD();

        return td;
    }

    /**
     * Accessor method for the Singleton instance of this class.<br>
     *
     * @return TelnetD Singleton instance reference.
     */
    public static TelnetD getReference() {
        if (c_Self != null) {
            return ((TelnetD) c_Self);
        } else {
            return null;
        }
    }//getReference

    /**
     * Implements a test startup of an example server.
     * It is supposed to demonstrate the easy deployment,
     * and usage of this daemon.<br>
     * <em>Usage:</em><br>
     * <code>java net.wimpi.telnetd.TelnetD [URL prefix pointing to properties]</code>
     *
     * @param args String array containing arguments.
     */
    public static void main(String[] args) {
        TelnetD myTD = null;

        try {
            //1. prepare daemon
            if (args.length == 0) {
                System.out.println("\nUsage: java net.wimpi.telnetd.TelnetD urlprefix\n");
                System.out.println("         java net.wimpi.telnetd.TelnetD url\n");
                System.exit(1);
            } else {
                myTD = TelnetD.createTelnetD(args[0]);
            }
            //2.start serving/accepting connections
            myTD.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }//main

    /**
     * Start this telnet daemon, respectively
     * all configured listeners.<br>
     */
    public void start() {
        Log.d(TAG, "start()");
        for (int i = 0; i < listeners.size(); i++) {
            PortListener plis = (PortListener) listeners.get(i);
            plis.start();
        }
    }//start

    /**
     * Stop this telnet daemon, respectively
     * all configured listeners.
     */
    public void stop() {
        for (int i = 0; i < listeners.size(); i++) {
            PortListener plis = (PortListener) listeners.get(i);
            //shutdown the Portlistener resources
            plis.stop();
        }
    }//stop

    /**
     * Accessor method to version information.
     *
     * @return String that contains version information.
     */
    public String getVersion() {
        return VERSION;
    }//getVersion

    /**
     * Method to prepare the ShellManager.<br>
     * Creates and prepares a Singleton instance of the ShellManager,
     * with settings from the passed in Properties.
     *
     * @param settings Properties object that holds main settings.
     * @throws BootException if preparation fails.
     */
    private void prepareShellManager(Properties settings)
            throws BootException {

        //use factory method  for creating mgr singleton
        shellManager = ShellManager.createShellManager(settings);
        if (shellManager == null) {
            System.exit(1);
        }
    }//prepareShellManager

    /**
     * Method to prepare the PortListener.<br>
     * Creates and prepares and runs a PortListener, with settings from the
     * passed in Properties. Yet the Listener will not accept any incoming
     * connections before startServing() has been called. this has the advantage
     * that whenever a TelnetD Singleton has been factorized, it WILL 99% not fail
     * any longer (e.g. serve its purpose).
     *
     * @param settings Properties object that holds main settings.
     * @throws BootException if preparation fails.
     */
    private void prepareListener(String name, Properties settings)
            throws BootException {

        //factorize PortListener
        PortListener listener = PortListener.createPortListener(name, settings);
        //start the Thread derived PortListener
        try {
            listeners.add(listener);
        } catch (Exception ex) {
            throw new BootException(
                    "Failure while starting PortListener thread: " + ex.getMessage());
        }

    }//prepareListener

    private void prepareTerminals(Properties terminals)
            throws BootException {

        TerminalManager.createTerminalManager(terminals);
    }//prepareTerminals

    /**
     * Returns a {@link PortListener} for the given
     * identifier.
     *
     * @param id the identifier of the {@link PortListener} instance.
     * @return {@link PortListener} instance or null if an instance
     * with the given identifier does not exist.
     */
    public PortListener getPortListener(String id) {
        if (id == null || id.length() == 0) {
            return null;
        }
        for (Iterator iterator = listeners.iterator(); iterator.hasNext(); ) {
            PortListener portListener = (PortListener) iterator.next();
            if (portListener.getName().equals(id)) {
                return portListener;
            }
        }
        return null;
    }//getPortListener

    public List getListeners() {
        return listeners;
    }


    public void setListeners(List listeners) {
        this.listeners = listeners;
    }


    public ShellManager getShellManager() {
        return shellManager;
    }


    public void setShellManager(ShellManager shellManager) {
        this.shellManager = shellManager;
    }

}//class TelnetD
