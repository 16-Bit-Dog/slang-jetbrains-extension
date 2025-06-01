package slanglsp;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.EnvironmentUtil;
import com.redhat.devtools.lsp4ij.LanguageServerFactory;
import com.redhat.devtools.lsp4ij.LanguageServerManager;
import com.redhat.devtools.lsp4ij.ServerStatus;
import com.redhat.devtools.lsp4ij.client.LanguageClientImpl;
import com.redhat.devtools.lsp4ij.server.ProcessStreamConnectionProvider;
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider;
import net.schmizz.sshj.common.Base64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.textmate.configuration.TextMateUserBundlesSettings;
import org.jetbrains.plugins.textmate.TextMateService;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.concurrent.LinkedBlockingDeque;

public class SlangLanguageServerFactory implements LanguageServerFactory
{
    String getSlangTextMateBundlePath()
    {
        return slanglsp.SlangUtils.getPluginDir() + "slang-vscode-extension";
    }

    static boolean IS_FIRST_INIT = true;

    void loadTextMate(Project project)
    {
        File path = Paths.get(getSlangTextMateBundlePath()).toFile();
//        NotificationGroupManager.getInstance().getNotificationGroup("Slang LSP").createNotification(
//            "Slang LSP",
//            "Can Read The Slang TextMate Info Folder ==> "+path.canRead(),
//            NotificationType.ERROR
//        ).notify(project);
        try
        {
            TextMateUserBundlesSettings.getInstance().addBundle(getSlangTextMateBundlePath(), "slang-vscode-extension");
        } catch (Exception e)
        {
            NotificationGroupManager.getInstance().getNotificationGroup("Slang LSP").createNotification(
                "Slang LSP",
                "The Slang-TextMate-json file is not embedded into the lsp plugin",
                NotificationType.ERROR
            ).notify(project);
        }
        TextMateService.getInstance().reloadEnabledBundles();
    }

    void updateExtensionVersionCache(Project project)
    {
        // set value of extension version cache
        File versionCacheFile = SlangUtils.getVersionCacheFile();
        try
        {
            SlangVersion cachedVersion = SlangUtils.getVersion();
            SlangVersion.writeSlangVersionFile(versionCacheFile, cachedVersion.getMajor(), cachedVersion.getMinor(), cachedVersion.getPatch());
        } catch (Exception e)
        {
            NotificationGroupManager.getInstance().getNotificationGroup("Slang LSP").createNotification(
                "Slang LSP",
                "failed to create to versionCache.txt. Requires ability to create+read+write files",
                NotificationType.ERROR
            ).notify(project);
        }
    }

    boolean checkIfVSCodeExtensionRequiresExtraction(Project project)
    {
        // If cache is missing, return true
        File versionCacheFile = SlangUtils.getVersionCacheFile();

        if (!versionCacheFile.exists())
            return true;

        // If cache version != current version, return true
        try
        {
            SlangVersion cachedVersion = new SlangVersion(versionCacheFile.toURI().toURL().openStream());
            if (!cachedVersion.equals(SlangUtils.getVersion()))
                return true;
        } catch (Exception e)
        {
            updateExtensionVersionCache(project);
            return true;
        }

        return false;
    }

    void failedToMakeFolder(Project project)
    {
        NotificationGroupManager.getInstance().getNotificationGroup("Slang LSP").createNotification(
            "Slang LSP",
            "Failed to create folder",
            NotificationType.ERROR
        ).notify(project);
    }
    // This function assumes if a file is lacking an extension, it is a directory
    void extractZip(InputStream zipToUnpack, Path dstDir, Project project)
    {
        File dir = dstDir.toFile();
        // create output directory if it doesn't exist
        if(!dir.exists())
        {
            if(!dir.mkdirs())
                failedToMakeFolder(project);
        }
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try
        {
            ZipInputStream zis = new ZipInputStream(zipToUnpack);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null)
            {
                String fileName = ze.getName().replace("\\", "/");
                Path newFilePath = dstDir.resolve(fileName);
                File newFile = newFilePath.toFile();
                if(!fileName.contains("."))
                {
                    if(!newFile.mkdirs())
                        failedToMakeFolder(project);
                }
                else
                {
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0)
                    {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }

                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            zipToUnpack.close();
        }
        catch (IOException e)
        {
            NotificationGroupManager.getInstance().getNotificationGroup("Slang LSP").createNotification(
                "Slang LSP",
                "Invalid slang-vscode-extension zip file(s)",
                NotificationType.ERROR
            ).notify(project);
            e.printStackTrace();
        }
    }

    void extractSlangVSCodeExtension(Project project)
    {
        InputStream zipToUnpack;
        try
        {
            zipToUnpack = getClass().getClassLoader().getResourceAsStream("slang-vscode-extension.zip");
        }
        catch(Exception e)
        {
            NotificationGroupManager.getInstance().getNotificationGroup("Slang LSP").createNotification(
                "Slang LSP",
                "Missing slang-vscode-extension.zip resource, build.gradle.kts task is not working",
                NotificationType.ERROR
            ).notify(project);
            return;
        }

        boolean requiresExtraction = checkIfVSCodeExtensionRequiresExtraction(project);
        if(requiresExtraction)
        {
            updateExtensionVersionCache(project);

            extractZip(zipToUnpack, slanglsp.SlangUtils.getPluginDir(), project);
        }
    }

    void tryRunInitLogic(Project project)
    {
        if(IS_FIRST_INIT)
        {
            IS_FIRST_INIT = false;
            extractSlangVSCodeExtension(project);
            loadTextMate(project);
        }
    }


    @NotNull
    public StreamConnectionProvider createConnectionProvider(Project project)
    {
        tryRunInitLogic(project);
        return new SlangLanguageServer(project);
    }

    @NotNull
    public LanguageClientImpl createLanguageClient(Project project)
    {
        tryRunInitLogic(project);
        return new SlangLanguageClient(project);
    }
};

class SlangLanguageServer extends ProcessStreamConnectionProvider
{
    Project project;
    SlangLanguageServer(Project project)
    {
        this.project = project;

        // First try to get EXE from the project settings
        var exePath = findExecutableUsingExplicitSlangdLocation();
        if(exePath.isEmpty())
        {
            // Next try to get EXE from PATH
            exePath = findExecutableInPATH();
        }
        if (exePath.isPresent())
        {
            super.setCommands(List.of(exePath.get(), ""));
            super.setWorkingDirectory(project.getBasePath());
        }
        else
        {
            NotificationGroupManager.getInstance().getNotificationGroup("Slang LSP").createNotification(
                "Slang LSP",
                "`slangd`/`slangd.exe` was not found in the `PATH` environment variable. It is preferable to add (once the latest vulkan SDK is installed) `$VK_SDK_PATH/bin` to your `PATH` environment variable (on linux the paths *may* differ slightly) to use `slangd` bundled with the Vulkan SDK. After these steps, restart this IDE.",
                NotificationType.ERROR
            ).notify(project);
            LanguageServerManager.getInstance(project).stop("slangLanguageServer");
        }
    }

    static String getLspExeName()
    {
        if (SystemInfo.isWindows)
            return "slangd.exe";
        return "slangd";
    }
    static class FindLspExeFilter implements FilenameFilter
    {
        @Override
        public boolean accept(File dir, String name)
        {
            return dir.canExecute() && name.contentEquals(getLspExeName());
        }
    }

    private Optional<String> findExecutableUsingExplicitSlangdLocation()
    {
        var dirFiles = Paths.get(SlangPersistentStateConfig.getInstance(project).getExplicitSlangdLocation()).toFile().listFiles(new FindLspExeFilter());

        if(dirFiles == null)
            return Optional.empty();
        for(var i : dirFiles)
            return Optional.of(i.getAbsolutePath());
        return Optional.empty();
    }

    private Optional<String> findExecutableInPATH()
    {
        String[] paths = EnvironmentUtil.getValue("PATH").split(File.pathSeparator);
        for(var pathString : paths)
        {
            var dirFiles = Paths.get(pathString).toFile().listFiles(new FindLspExeFilter());
            if(dirFiles == null)
                continue;
            for(var i : dirFiles)
                return Optional.of(i.getAbsolutePath());
        }
        return Optional.empty();
    }
}

class SlangLanguageClient extends LanguageClientImpl
{
    static LinkedBlockingDeque<SlangLanguageClient> maybeAliveClients = new LinkedBlockingDeque<>();

    Project project;
    SlangLanguageClient(Project project)
    {
        super(project);
        this.project = project;
        maybeAliveClients.add(this);
    }

    public Object createSettings()
    {
        var state = SlangPersistentStateConfig.getInstance(project).getState();
        return state.createJSONFromObject();
    }

    public void triggerChangeConfiguration()
    {
        super.triggerChangeConfiguration();
    }

    public void handleServerStatusChanged(ServerStatus serverStatus)
    {
        if (serverStatus == ServerStatus.started)
        {
            triggerChangeConfiguration();
        }
        if(serverStatus == ServerStatus.stopped)
        {
            maybeAliveClients.remove(this);
        }
    }
}