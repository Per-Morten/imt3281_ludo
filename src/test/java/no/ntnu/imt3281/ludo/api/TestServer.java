package no.ntnu.imt3281.ludo.api;

import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.server.Server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

class TestServer {
    private static Thread sServerThread;

    static String DATABASE_NAME = "ludo_tests.db";

    static void start() throws IOException, InterruptedException {
        Files.deleteIfExists(Paths.get(DATABASE_NAME));
        Server.setDatabase(DATABASE_NAME);
        Server.setPollTimeout(2);
        sServerThread = new Thread(() -> {
            try {
                Server.main(null);
            } catch (SQLException e) {
                System.out.println("Exception");
                Logger.logException(Logger.Level.WARN, e, "Unhandled Exception in Server");
            }
        });
        sServerThread.start();
        // We need to sleep a bit here to ensure that the thread is actually running.
        Thread.sleep(1000);
    }

    static void stop() throws InterruptedException {
        Server.stop();
        sServerThread.join();
    }
}
