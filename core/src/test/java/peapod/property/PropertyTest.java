/*
 * Copyright 2015 Bay of Many
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * This project is derived from code in the Tinkerpop project under the following license:
 *
 *    Tinkerpop3
 *    http://www.apache.org/licenses/LICENSE-2.0
 */

package peapod.property;

import com.tinkerpop.gremlin.process.T;
import com.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;
import peapod.FramedGraph;
import peapod.GraphTest;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static junit.framework.TestCase.*;

/**
 * Test class to test primitive and object multiproperties.
 */
public class PropertyTest extends GraphTest {

    private Person p;
    private Vertex v;

    @Before
    public void init() {
        v = g.addVertex(T.label, "Person",
                "s", "hello",
                "b1", true, "b2", true,
                "s1", (short) 42, "s2", (short) 43,
                "i1", 42, "i2", 43,
                "l1", 42L, "l2", 43L,
                "f1", 42.0f, "f2", 43.0f,
                "d1", 42.0d, "d2", 43.0d,
                "c1", 'a', "c2", 'b',
                "by1", (byte) 0x11, "by2", (byte) 0x21);

        FramedGraph graph = new FramedGraph(g, Person.class.getPackage());
        p = graph.v(v.id());
    }

    @Test
    public void testGetString() {
        assertEquals("hello", p.getS());
    }

    @Test
    public void testSetString() {
        p.setS("goodbye");
        assertEquals("goodbye", p.getS());
        assertEquals("goodbye", v.value("s"));
    }

    @Test
    public void testSetStringNull() {
        p.setS(null);
        assertEquals(null, p.getS());
        assertFalse(v.property("s").isPresent());
    }

    @Test
    public void testSetDateNull() {
        p.setDate(null);
        assertEquals(null, p.getDate());
        assertFalse(v.property("date").isPresent());
    }

    @Test
    public void testGetBoolean() {
        assertEquals(TRUE, p.getB1());
    }

    @Test
    public void testSetBoolean() {
        p.setB1(false);
        assertEquals(FALSE, p.getB1());
        assertEquals(FALSE, v.value("b1"));
    }

    @Test
    public void testSetBooleanNull() {
        p.setB1(null);
        assertEquals(null, p.getB1());
        assertFalse(v.property("b1").isPresent());
    }

    @Test
    public void testGetPrimitiveBoolean() {
        assertEquals(true, p.getB2());
    }

    @Test
    public void testSetPrimitiveBoolean() {
        p.setB2(false);
        assertEquals(false, p.getB2());
        assertTrue(v.property("b2").isPresent());
        assertEquals(FALSE, v.value("b2"));
    }

    @Test
    public void testGetShort() {
        assertEquals(new Short((short) 42), p.getS1());
    }

    @Test
    public void testSetShort() {
        p.setS1((short) 142);
        assertEquals(new Short((short) 142), p.getS1());
        assertEquals(new Short((short) 142), v.value("s1"));
    }

    @Test
    public void testSetShortNull() {
        p.setS1(null);
        assertEquals(null, p.getS1());
        assertFalse(v.property("s1").isPresent());
    }

    @Test
    public void testGetPrimitiveShort() {
        assertEquals(43, p.getS2());
    }

    @Test
    public void testSetPrimitiveShort() {
        p.setS2((short) 143);
        assertEquals(143, p.getS2());
        assertTrue(v.property("s2").isPresent());
        assertEquals(new Short((short) 143), v.value("s2"));
    }

    @Test
    public void testGetInteger() {
        assertEquals(new Integer(42), p.getI1());
    }

    @Test
    public void testSetInteger() {
        p.setI1(142);
        assertEquals(new Integer(142), p.getI1());
        assertEquals(new Integer(142), v.value("i1"));
    }

    @Test
    public void testSetIntegerNull() {
        p.setI1(null);
        assertEquals(null, p.getI1());
        assertFalse(v.property("i1").isPresent());
    }

    @Test
    public void testGetPrimitiveInteger() {
        assertEquals(43, p.getI2());
    }

    @Test
    public void testSetPrimitiveInteger() {
        p.setI2(143);
        assertEquals(143, p.getI2());
        assertTrue(v.property("i2").isPresent());
        assertEquals(new Integer(143), v.value("i2"));
    }

    @Test
    public void testGetLong() {
        assertEquals(new Long(42L), p.getL1());
    }

    @Test
    public void testSetLong() {
        p.setL1(142L);
        assertEquals(new Long(142), p.getL1());
        assertEquals(new Long(142), v.value("l1"));
    }

    @Test
    public void testSetLongNull() {
        p.setL1(null);
        assertEquals(null, p.getL1());
        assertFalse(v.property("l1").isPresent());
    }

    @Test
    public void testGetPrimitiveLong() {
        assertEquals(43L, p.getL2());
    }

    @Test
    public void testSetPrimitiveLong() {
        p.setL2(143L);
        assertEquals(143L, p.getL2());
        assertTrue(v.property("l2").isPresent());
        assertEquals(new Long(143), v.value("l2"));
    }

    @Test
    public void testGetFloat() {
        assertEquals(new Float(42f), p.getF1());
    }

    @Test
    public void testSetFloat() {
        p.setF1(142.0f);
        assertEquals(new Float(142.0f), p.getF1());
        assertEquals(new Float(142.0f), v.value("f1"));
    }

    @Test
    public void testSetFloatNull() {
        p.setF1(null);
        assertEquals(null, p.getF1());
        assertFalse(v.property("f1").isPresent());
    }

    @Test
    public void testGetPrimitiveFloat() {
        assertEquals(43.0f, p.getF2());
    }

    @Test
    public void testSetPrimitiveFloat() {
        p.setF2(143.0f);
        assertEquals(143.0f, p.getF2());
        assertTrue(v.property("f2").isPresent());
        assertEquals(new Float(143.0f), v.value("f2"));
    }

    @Test
    public void testGetDouble() {
        assertEquals(new Double(42d), p.getD1());
    }

    @Test
    public void testSetDouble() {
        p.setD1(142d);
        assertEquals(new Double(142d), p.getD1());
        assertEquals(new Double(142d), v.value("d1"));
    }

    @Test
    public void testSetDoubleNull() {
        p.setD1(null);
        assertEquals(null, p.getD1());
        assertFalse(v.property("d1").isPresent());
    }

    @Test
    public void testGetPrimitiveDouble() {
        assertEquals(43d, p.getD2());
    }

    @Test
    public void testSetPrimitiveDouble() {
        p.setD2(143d);
        assertEquals(143d, p.getD2());
        assertTrue(v.property("d2").isPresent());
        assertEquals(new Double(143d), v.value("d2"));
    }

    @Test
    public void testGetCharacter() {
        assertEquals(new Character('a'), p.getC1());
    }

    @Test
    public void testSetCharacter() {
        p.setC1('c');
        assertEquals(new Character('c'), p.getC1());
        assertEquals(new Character('c'), v.value("c1"));
    }

    @Test
    public void testSetCharacterNull() {
        p.setC1(null);
        assertEquals(null, p.getC1());
        assertFalse(v.property("c1").isPresent());
    }

    @Test
    public void testGetPrimitiveCharacter() {
        assertEquals('b', p.getC2());
    }

    @Test
    public void testSetPrimitiveCharacter() {
        p.setC2('d');
        assertEquals('d', p.getC2());
        assertTrue(v.property("c2").isPresent());
        assertEquals(new Character('d'), v.value("c2"));
    }

    @Test
    public void testGetByte() {
        assertEquals(new Byte((byte) 0x11), p.getBy1());
    }

    @Test
    public void testSetByte() {
        p.setBy1((byte) 0x12);
        assertEquals(new Byte((byte) 0x12), p.getBy1());
        assertEquals(new Byte((byte) 0x12), v.value("by1"));
    }

    @Test
    public void testSetByteNull() {
        p.setBy1(null);
        assertEquals(null, p.getBy1());
        assertFalse(v.property("by1").isPresent());
    }

    @Test
    public void testGetPrimitiveByte() {
        assertEquals((byte) 0x21, p.getBy2());
    }

    @Test
    public void testSetPrimitiveByte() {
        p.setBy2((byte) 0x22);
        assertEquals((byte) 0x22, p.getBy2());
        assertTrue(v.property("by2").isPresent());
        assertEquals(new Byte((byte) 0x22), v.value("by2"));
    }
}
