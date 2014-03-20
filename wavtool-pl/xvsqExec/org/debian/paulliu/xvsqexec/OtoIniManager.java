/*
    Copyright(C) 2013 Ying-Chun Liu(PaulLiu). All rights reserved.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/

package org.debian.paulliu.xvsqexec;

import java.io.*;
import java.util.*;

/**
 * Manage multiple oto files
 */
public class OtoIniManager {

    private HashMap<String, OtoFileRecord> otoFileRecords = null;

    public OtoIniManager() {
	otoFileRecords = new LinkedHashMap<String, OtoFileRecord>();
    }

    /**
     * Add an oto.ini file into this manager
     *
     *@param otoFile the oto.ini file
     */
    public void addOtoFile(File otoFile) {
	OtoIniReader oReader = new OtoIniReader(otoFile);

	ArrayList<OtoFileRecord> records = oReader.getOtoFileRecords();

	for (int i=0; i<records.size(); i++) {
	    OtoFileRecord r = records.get(i);
	    if (otoFileRecords.containsKey(r.getAlias())) {
		continue;
	    }
	    otoFileRecords.put(r.getAlias(), r);
	}
    }

    /**
     * get a record by alias
     * @param alias the alias
     * @return the record
     */
    OtoFileRecord get(String alias) {
	OtoFileRecord ret=null;
	if (alias != null && otoFileRecords.containsKey(alias)) {
	    ret = otoFileRecords.get(alias);
	}
	return ret;
    }

    /**
     * get all the records as a HashMap of String -> OtoFileRecord
     * @return the hashmap.
     */
    HashMap<String, OtoFileRecord> getAll() {
	return otoFileRecords;
    }

}
