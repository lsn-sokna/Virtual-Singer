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

public class TestNote extends TestCase {

    private org.debian.paulliu.xvsqexec.Note note;

    @Override
    public void setUp() {
	note = new org.debian.paulliu.xvsqexec.Note();
    }

    @Override
    public void tearDown() {
	note = null;
    }

    public void testNoteFields() {
	String alias="T";
	int pitch=15;
	int duration=10;
	int tempo=115;

	note.setAlias(alias);
	note.setPitch(pitch);
	note.setDuration(duration);
	note.setTempo(tempo);

	assertEquals(alias, note.getAlias());
	assertEquals(pitch, note.getPitch());
	assertEquals(duration, note.getDuration());
	assertEquals(tempo, note.getTempo());
    }

}
