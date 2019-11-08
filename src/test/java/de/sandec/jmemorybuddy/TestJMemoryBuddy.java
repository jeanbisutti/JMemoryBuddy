package de.sandec.jmemorybuddy;


import org.junit.Assert;
import org.junit.Test;

public class TestJMemoryBuddy {

    class A {

    }

    @Test
    public void simpleTest() {
        A referenced = new A();
        JMemoryBuddy.doMemTest(checker -> {
            A notReferenced = new A();
            checker.accept(notReferenced); // not referenced should be collectable
        });
    }

    @Test
    public void simpleTestRepeated() {
        for(int i = 0; i < 100; i += 1) {
            A referenced = new A();
            JMemoryBuddy.doMemTest(checker -> {
                A notReferenced = new A();
                checker.accept(notReferenced); // not referenced should be collectable
            });
        }
    }

    @Test
    public void negativeTest() {
        boolean exceptionThrown = false;
        try {
            A referenced = new A();
            JMemoryBuddy.doMemTest(checker -> {
                checker.accept(referenced);
            });
        } catch (Exception e) {
            exceptionThrown = true;
        }
        Assert.assertTrue("No exception was thrown!", exceptionThrown);
    }

    @Test
    public void testCreateHeapDump() {
        JMemoryBuddy.doHeapDump(); // shouldn't throw an exception
    }
}