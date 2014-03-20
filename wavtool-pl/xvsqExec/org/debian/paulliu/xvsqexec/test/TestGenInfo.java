/*
    Copyright(C) 2010 Ying-Chun Liu(PaulLiu). All rights reserved.

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

package org.debian.paulliu.xvsqexec.test;

import org.debian.paulliu.xvsqexec.Note;
import junit.framework.TestCase;
import junit.textui.TestRunner;
import junit.framework.TestSuite;
import java.io.*;

public class TestGenInfo extends TestCase {

    @Override
    public void setUp() {
    }

    @Override
    public void tearDown() {
    }

    public void testGenInfoFields() {
	org.debian.paulliu.xvsqexec.Note note = new org.debian.paulliu.xvsqexec.Note();
	org.debian.paulliu.xvsqexec.OtoFileRecord otoFileRecord = new org.debian.paulliu.xvsqexec.OtoFileRecord();
	org.debian.paulliu.xvsqexec.GenInfo genInfo = null;
	note.setAlias("T");
	note.setPitch(15);
	note.setDuration(10);
	note.setTempo(115);
	otoFileRecord.setFilename("a.wav");
	otoFileRecord.setAlias("T");
	otoFileRecord.setPath(new File(File.separator+"tmp"));
	otoFileRecord.setOffset(100.0);
	otoFileRecord.setConsonant(200.0);
	otoFileRecord.setBlank(-1000.0);
	otoFileRecord.setPrevoice(50.0);
	otoFileRecord.setOverlap(10.0);

	genInfo = new org.debian.paulliu.xvsqexec.GenInfo(note, otoFileRecord);

	assertEquals(note, genInfo.getNote());
	assertEquals(otoFileRecord, genInfo.getOtoFileRecord());

	genInfo.setNext(null);
	assertEquals(null, genInfo.getNext());
	genInfo.setNext(genInfo);
	assertEquals(genInfo, genInfo.getNext());
	genInfo.setNext(null);
	assertEquals(null, genInfo.getPrev());

	genInfo.setPrev(null);
	assertEquals(null, genInfo.getPrev());
	genInfo.setPrev(genInfo);
	assertEquals(genInfo, genInfo.getPrev());
	genInfo.setPrev(null);
	assertEquals(null, genInfo.getPrev());

    }

    public void testGenInfoStaticMethods() {
	String base64str = null;
	base64str = org.debian.paulliu.xvsqexec.GenInfo.pitchDiffBase64(120, 240);
	assertEquals("tR", base64str);
	base64str = org.debian.paulliu.xvsqexec.GenInfo.pitchDiffBase64(100, 300);
	assertEquals("iT", base64str);
	base64str = org.debian.paulliu.xvsqexec.GenInfo.pitchDiffBase64(240, 480);
	assertEquals("tR", base64str);
    }

}
