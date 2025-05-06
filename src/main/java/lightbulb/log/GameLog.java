// log/GameLog.java
package lightbulb.log;

import java.io.*;

public class GameLog implements Closeable {
    private final BufferedWriter out;

    public GameLog(String path) throws IOException {
        out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
    }

    /** writes a string and a line feed */
    public void write(String jsonLine) {
        try {
            out.write(jsonLine);
            out.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** resets buffer to disk */
    public void flush() {
        try { out.flush(); } catch (IOException e) { e.printStackTrace(); }
    }

    @Override public void close() throws IOException { out.close(); }
}
