import java.applet.Applet;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//import netscape.javascript.JSObject;

/**
 * General applet for generic installs.
 */
public class Installer extends Applet {


    private static final long serialVersionUID = -3141611634409357059L;
    
    private static final boolean IS_OS_MAC_OSX = getOSMatches("Mac OS X");
    private static final boolean IS_OS_WINDOWS = getOSMatches("Windows");
    
    public void install() {
        System.out.println("Install called...");
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            public Void run() {
                System.out.println("Inside privileged call...");
                try {
                    final File tempFile = installPrivileged();
                    System.out.println("Opening Lantern");
                    final String cmd = cmd(tempFile);
                    System.out.println("Calling command "+cmd);
                    Runtime.getRuntime().exec(cmd);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return null;
            }
        });
    }
    
    public File installPrivileged() throws IOException {
        final String installerName = installerName();
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));
        final File tempFile = new File(tempDir, installerName);
        //final File tempFile = new File("/Users/afisk/lantern/lantern-osx-installer.zip");
        final String fullUrl = "http://cdn.bravenewsoftware.org/"+installerName;
        
        HttpURLConnection connection = null;
        URL serverAddress = null;
        InputStream is = null;
        OutputStream os = null;
        try {
            serverAddress = new URL(fullUrl);
            System.out.println("Opening connection...");
            //Set up the initial connection
            connection = (HttpURLConnection)serverAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(30000);
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(30 * 1000);
            connection.setUseCaches(false);
            connection.connect();
            System.out.println("Connected...");
            is = connection.getInputStream();
            
            os = new FileOutputStream(tempFile);
            System.out.println("Copying url: "+fullUrl);
            copyLarge(is, os);
            os.close();
            is.close();
            System.out.println("Wrote file to: "+tempFile);
            final ZipFile zip = new ZipFile(tempFile, ZipFile.OPEN_READ);
            final Enumeration<? extends ZipEntry> entries = zip.entries();
            File firstFile = null;
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                final String name = entry.getName();
                final File file = new File(name);
                if (firstFile == null) {
                    firstFile = file;
                }
                if (entry.isDirectory()) {
                    if (!file.mkdirs()) {
                        System.out.println("ERROR making directory: "+name);
                    }
                }
                else {
                    BufferedOutputStream dest = null;
                    BufferedInputStream bis = null;
                    try {
                        dest = new BufferedOutputStream(
                            new FileOutputStream(name), 2048);
                        bis = new BufferedInputStream(zip.getInputStream(entry));
                        copyLarge(bis, dest);
                        bis.close();
                        dest.flush();
                        dest.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    } finally {
                        closeQuietly(dest, bis);
                    }
                }
            }
            return firstFile;
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            closeQuietly(is, os);
        }
        
        throw new IOException("Did not successfully unzip the file.");
    }
    
    private void closeQuietly(final Closeable... closeables) {
        for (final Closeable c : closeables) {
            try {
                c.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    protected String cmd(final File tempFile) throws IOException {
        if (IS_OS_MAC_OSX) {
            return "open "+tempFile.getAbsolutePath();
        } else if (IS_OS_WINDOWS) {
            return tempFile.getAbsolutePath();
        } else {
            throw new IOException("Unsupported OS");
        }
        
    }
    
    protected String installerName() {
        if (IS_OS_MAC_OSX) {
            return "lantern-osx-installer.zip";
        } else if (IS_OS_WINDOWS) {
            //return "installer.exe";
            return "lantern-win-installer.zip";
        }
        return "installer.exe";
    }
    
    private static final String OS_NAME = System.getProperty("os.name");
    
    private static boolean getOSMatches(final String osNamePrefix) {
        if (OS_NAME == null) {
            return false;
        }
        return OS_NAME.startsWith(osNamePrefix);
    } 

    private static long copyLarge(final InputStream input, 
        final OutputStream output)
        throws IOException {
        final byte[] buffer = new byte[1024 * 4];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /*
    private void callJavaScript(final String func, final Object... args) {
        final JSObject window = JSObject.getWindow(Installer.this);
        if (window == null) {
            System.out.println("Could not get window from JSObject!!!");
            return;
        }
        System.out.println("Calling func through window");
        try {
            window.call(func, args);
        } catch (final Exception e) {
            System.out.println("Got error!!" + e.getMessage());
            e.printStackTrace();
            showError(e);
        }
        System.out.println("Finished JavaScript call...");
    }

    private void showError(final Exception e) {
        final String[] args = new String[] { e.getMessage() };
        final JSObject window = JSObject.getWindow(this);
        try {
            window.call("alert", args);
        } catch (final Exception ex) {
            System.out.println("Error showing error! " + ex);
        }
    }
    */

    @Override
    public void init() {
        System.out.println("Initing applet with polling...");
        super.init();
    }

    @Override
    public void start() {
        System.out.println("Starting applet with polling...");
        super.start();
    }

    @Override
    public void stop() {
        System.out.println("Stopping applet...");
        super.stop();
    }

    @Override
    public void destroy() {
        System.out.println("Destroying applet...");
        super.destroy();
    }
}
