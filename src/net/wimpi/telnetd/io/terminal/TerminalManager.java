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

package net.wimpi.telnetd.io.terminal;

import net.wimpi.telnetd.BootException;
import net.wimpi.telnetd.util.StringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Class that manages all available terminal implementations.<br>
 * Configuration is stored in a properties file
 * (normally Terminals.properties).
 *
 * @author Dieter Wimberger
 * @version 2.0 (16/07/2006)
 */
public class TerminalManager {

    private static Log log = LogFactory.getLog(TerminalManager.class);
    private static TerminalManager c_Self; //Singleton reference
    private HashMap terminals; //datastructure for terminals
    private boolean m_WindoofHack = false;

    /**
     * Private constructor, instance can only be created
     * via the public factory method.
     */
    private TerminalManager() {
        c_Self = this;
        terminals = new HashMap(25);
    }//constructor

    private TerminalManager(Map terminals) {
        this();
        this.terminals=new HashMap(terminals);
    }

    /**
     * Returns a reference to a terminal that has
     * been set up, regarding to the key given as
     * parameter.<br>
     * If the key does not represent a terminal name or
     * any alias for any terminal, then the returned terminal
     * will be a default basic terminal (i.e. vt100 without
     * color support).
     *
     * @param key String that represents a terminal name or an alias.
     * @return Terminal instance or null if the key was invalid.
     */
    public Terminal getTerminal(String key) {

        Terminal term = null;

        try {
            //log.debug("Key:" + key);
            if (key.equals("ANSI") && m_WindoofHack) {
                //this is a hack, sorry folks but the *grmpflx* *censored*
                //windoof telnet application thinks its uppercase ansi *brr*
                term = (Terminal) terminals.get("windoof");
            } else {
                key = key.toLowerCase();
                //log.debug("Key:" + key);
                if (!terminals.containsKey(key)) {
                    term = (Terminal) terminals.get("default");
                } else {
                    term = (Terminal) terminals.get(key);
                }
            }
        } catch (Exception e) {
            log.error("getTerminal()", e);
        }

        return term;
    }//getTerminal

    public String[] getAvailableTerminals() {
        //unroll hashtable keys into string array
        //maybe not too efficient but well
        String[] tn = new String[terminals.size()];
        int i = 0;
        for (Iterator iter = terminals.keySet().iterator(); iter.hasNext(); i++) {
            tn[i] = (String) iter.next();
        }
        return tn;
    }//getAvailableTerminals

    public void setWindoofHack(boolean b) {
        m_WindoofHack = b;
    }//setWinHack

    /**
     * Loads the terminals and prepares an instance of each.
     */
    private void setupTerminals(HashMap terminalsMap) {

        String termname = "";
        String termclass = "";
        Terminal term = null;
        Object[] entry = null;

        for (Iterator iter = terminalsMap.keySet().iterator(); iter.hasNext();) {
            try {
                //first we get the name
                termname = (String) iter.next();

                //then the entry
                entry = (Object[]) terminalsMap.get(termname);

                //then the fully qualified class string
                termclass = (String) entry[0];
                log.debug("Preparing terminal [" + termname + "] " + termclass);

                //get a new class object instance (e.g. load class and instantiate it)
                term = (Terminal) Class.forName(termclass).newInstance();

                //and put an instance + references into myTerminals
                terminals.put(termname, term);
                String[] aliases = (String[]) entry[1];
                for (int i = 0; i < aliases.length; i++) {
                    //without overwriting existing !!!
                    if (!terminals.containsKey(aliases[i])) {
                        terminals.put(aliases[i], term);
                    }
                }

            } catch (Exception e) {
                log.error("setupTerminals()", e);
            }

        }
        //check if we got all
        log.debug("Terminals:");
        for (Iterator iter = terminals.keySet().iterator(); iter.hasNext();) {
            String tn = (String) iter.next();
            log.debug(tn + "=" + terminals.get(tn));
        }
    }//setupTerminals

    /**
     * Factory method for creating the Singleton instance of
     * this class.<br>
     * Note that this factory method is called by the
     * net.wimpi.telnetd.TelnetD class.
     *
     * @param settings Properties defining the terminals as described in the
     *                 class documentation.
     * @return TerminalManager Singleton instance.
     */
    public static TerminalManager createTerminalManager(Properties settings) throws BootException {

        HashMap terminals = new HashMap(20); //a storage for class
        //names and aliases

        boolean defaultFlag = false; //a flag for the default
        TerminalManager tmgr = new TerminalManager();

        //Loading and applying settings
        try {
            log.debug("Creating terminal manager.....");
            boolean winhack = new Boolean(settings.getProperty("terminals.windoof")).booleanValue();

            //Get the declared terminals
            String terms = settings.getProperty("terminals");
            if (terms == null) {
                log.debug("No terminals declared.");
                throw new BootException("No terminals declared.");
            }

            //split the names
            String[] tn = StringUtil.split(terms, ",");

            //load fully qualified class name and aliases for each
            //storing it in the Hashtable within an objectarray of two slots
            Object[] entry = null;
            String[] aliases = null;
            for (int i = 0; i < tn.length; i++) {
                entry = new Object[2];
                //load fully qualified classname
                entry[0] = settings.getProperty("term." + tn[i] + ".class");
                //load aliases and store as Stringarray
                aliases = StringUtil.split(settings.getProperty("term." + tn[i] + ".aliases"), ",");
                for (int n = 0; n < aliases.length; n++) {
                    //ensure default declared only once as alias
                    if (aliases[n].equalsIgnoreCase("default")) {
                        if (!defaultFlag) {
                            defaultFlag = true;
                        } else {
                            throw new BootException("Only one default can be declared.");
                        }
                    }
                }
                entry[1] = aliases;
                //store
                terminals.put(tn[i], entry);
            }
            if (!defaultFlag) {
                throw new BootException("No default terminal declared.");
            }

            //construct manager
            tmgr = new TerminalManager();
            tmgr.setWindoofHack(winhack);
            tmgr.setupTerminals(terminals);

            return tmgr;

        } catch (Exception ex) {
            log.error("createManager()", ex);
            throw new BootException("Creating TerminalManager Instance failed:\n" + ex.getMessage());
        }
    }//createManager
    
    /**
     * sets up a terminal manager with a ready made hashmap of terminals.
     * 
     * 
     * @param terminals: hashmap of terminal classes with their alias names
     * @param winhack: is this a windoof hack
     * @throws BootException 
     */
    public static TerminalManager createTerminalManager(Map terminals, boolean winhack) throws BootException{
        if(!terminals.containsKey("default"))
            throw new BootException("No default terminal declared.");
        
        TerminalManager tmgr;
        tmgr = new TerminalManager(terminals);
        tmgr.setWindoofHack(winhack);

        return tmgr;
    }
    /**
     * Accessor method for the Singleton instance of this class.<br>
     * Note that it returns null if the instance was not properly
     * created beforehand.
     *
     * @return TerminalManager Singleton instance reference.
     */
    public static TerminalManager getReference() {
        return c_Self;
    }//getReference

    public HashMap getTerminals() {
        return terminals;
    }

    public void setTerminals(HashMap terminals) {
        this.terminals = terminals;
    }

}//class TerminalManager
