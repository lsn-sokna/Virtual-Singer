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

import org.debian.paulliu.xvsqexec.OtoFileRecord;
import junit.framework.TestCase;
import junit.textui.TestRunner;
import junit.framework.TestSuite;
import java.io.*;

public class TestOtoFileRecord extends TestCase {

    private org.debian.paulliu.xvsqexec.OtoFileRecord otoFileRecord;

    @Override
    public void setUp() {
	otoFileRecord = new org.debian.paulliu.xvsqexec.OtoFileRecord();
    }

    @Override
    public void tearDown() {
	otoFileRecord = null;
    }

    public void testOtoFileRecordFields() {
	String filename="a.wav";
	String path=File.separator+"tmp";
	String alias="T";
	double offset=100.0;
	double consonant=200.0;
	double blank=-1000.0;
	double prevoice=50.0;
	double overlap=10.0;

	otoFileRecord.setFilename(filename);
	otoFileRecord.setAlias(alias);
	otoFileRecord.setPath(new File(path));
	otoFileRecord.setOffset(offset);
	otoFileRecord.setConsonant(consonant);
	otoFileRecord.setBlank(blank);
	otoFileRecord.setPrevoice(prevoice);
	otoFileRecord.setOverlap(overlap);

	assertEquals(filename, otoFileRecord.getFilename().getName());
	assertEquals(alias, otoFileRecord.getAlias());
	assertEquals("tmp", otoFileRecord.getPath().getName());
	assertEquals(offset, otoFileRecord.getOffset());
	assertEquals(consonant, otoFileRecord.getConsonant());
	assertEquals(blank, otoFileRecord.getBlank());
	assertEquals(prevoice, otoFileRecord.getPrevoice());
	assertEquals(overlap, otoFileRecord.getOverlap());
    }

}
