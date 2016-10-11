package de.subcentral.core.parse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class ParsingServiceMultithreadingTest {
    private static final Logger log = LogManager.getLogger(ParsingServiceMultithreadingTest.class);

    @Test
    public void testParsingServiceMultithreading() throws InterruptedException {
        TypeBasedParsingService ps = new TypeBasedParsingService("test");
        ps.register(String.class, t -> {
            try {
                Thread.sleep(100);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            return t.toUpperCase();
        });

        Runnable parse = () -> {
            // log.info("Parsing ...");
            String s = ps.parse("hallo", String.class);
            // log.info("Parsed: " + s);
        };

        Runnable unregister = () -> {
            log.info("Unregistering ...");
            ps.unregisterAll();
            log.info("Unregistered!");
        };

        Runnable register = () -> {
            log.info("Registering ...");
            ps.register(String.class, t -> StringUtils.capitalize(t));
            log.info("Registered!");
        };

        Thread t1 = new Thread(parse, "Parsing-Thread#1");
        Thread t2 = new Thread(register, "Registering-Thread");
        Thread[] moreParsingThreads = new Thread[4096];
        for (int i = 0; i < moreParsingThreads.length; i++) {
            moreParsingThreads[i] = new Thread(parse, "Parsing-Thread#" + (i + 2));
        }

        t1.start();
        Thread.sleep(200);
        t2.start();
        Thread.sleep(200);
        for (Thread t : moreParsingThreads) {
            t.start();
        }

        t1.join();
        t2.join();
        for (Thread t : moreParsingThreads) {
            t.join();
        }
        Thread tn = new Thread(parse, "Parsing-Thread#n");
        tn.start();
        tn.join();
    }
}
