package gomule.gui;

import com.google.common.io.Closeables;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class WorkspaceProperties { //TODO combine with FileManagerProperties
    private static File getWorkspacePropertiesFile(File workspaceDir) throws IOException {
        File lProps = new File(workspaceDir, "workspace.properties");
        if (!lProps.exists()) {
            lProps.createNewFile();
        }
        return lProps;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Properties loadWorkspaceProperties(File workspaceDir) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(getWorkspacePropertiesFile(workspaceDir));
            Properties properties = new Properties();
            properties.load(fileInputStream);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Closeables.closeQuietly(fileInputStream);
        }
    }

    public static void saveWorkspaceProperties(File workspaceDir, Properties properties) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(getWorkspacePropertiesFile(workspaceDir));
            properties.store(out, null);
        } catch (IOException ex) {
            D2FileManager.displayErrorDialog(ex);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
