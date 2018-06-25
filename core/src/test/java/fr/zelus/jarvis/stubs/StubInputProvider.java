package fr.zelus.jarvis.stubs;

import fr.zelus.jarvis.core.JarvisCore;
import fr.zelus.jarvis.io.InputProvider;
import org.apache.commons.configuration2.Configuration;

public class StubInputProvider extends InputProvider {

    public StubInputProvider(JarvisCore jarvisCore) {
        super(jarvisCore);
    }

    public StubInputProvider(JarvisCore jarvisCore, Configuration configuration) {
        this(jarvisCore);
    }

    public void write(String message) {
        jarvisCore.handleMessage(message);
    }

    @Override
    public void run() {
        synchronized(this) {
            try {
                wait();
            } catch (InterruptedException e) {

            }
        }
    }

}