package peapod.impl;

import org.junit.Test;

import static org.junit.Assert.*;

public class NounHelperTest {

    @Test
    public void testSingularize() throws Exception {
        assertEquals("friend", NounHelper.singularize("friends"));
    }
}