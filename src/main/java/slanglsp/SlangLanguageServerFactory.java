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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.textmate.configuration.TextMateUserBundlesSettings;
import org.jetbrains.plugins.textmate.TextMateService;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingDeque;

public class SlangLanguageServerFactory implements LanguageServerFactory
{
    @NotNull
    public StreamConnectionProvider createConnectionProvider(Project project)
    {
        return new SlangLanguageServer(project);
    }

    @NotNull
    public LanguageClientImpl createLanguageClient(Project project)
    {
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
        var state = SlangPersistentStateConfig.getInstance(project);
        if (state != null && !state.getExplicitSlangdLocation().isEmpty())
        {
            var dirFiles = Paths.get(state.getExplicitSlangdLocation()).toFile().listFiles(new FindLspExeFilter());
            if(dirFiles == null)
                return Optional.empty();
            for(var i : dirFiles)
                return Optional.of(i.getAbsolutePath());
        }

        return Optional.empty();
    }

    private Optional<String> findExecutableInPATH()
    {
        var path = EnvironmentUtil.getValue("PATH");
        if (path != null && !path.isEmpty()) {
            String[] paths = path.split(File.pathSeparator);
            for (var pathString : paths) {
                var dirFiles = Paths.get(pathString).toFile().listFiles(new FindLspExeFilter());
                if (dirFiles == null)
                    continue;
                for (var i : dirFiles)
                    return Optional.of(i.getAbsolutePath());
            }
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

    @Override
    public void handleServerStatusChanged(com.redhat.devtools.lsp4ij.ServerStatus serverStatus)
    {
        if (serverStatus == com.redhat.devtools.lsp4ij.ServerStatus.started)
        {
            // Re-enable Semantic Tokens and deeply map them to JetBrains IDE colors
            getClientFeatures().setSemanticTokensFeature(new com.redhat.devtools.lsp4ij.client.features.LSPSemanticTokensFeature() {
                @Override
                public com.intellij.openapi.editor.colors.TextAttributesKey getTextAttributesKey(String type, java.util.List<String> modifiers, com.intellij.psi.PsiFile file) {
                    if (type == null) return super.getTextAttributesKey(type, modifiers, file);
                    
                    switch (type) {
                        case "type":
                        case "class":
                        case "struct":
                        case "interface":
                        case "enum":
                        case "typeParameter":
                            return slanglsp.highlighting.SlangSyntaxHighlighterColors.TYPE_NAME;
                        case "parameter":
                        case "variable":
                        case "property":
                            return slanglsp.highlighting.SlangSyntaxHighlighterColors.VARIABLE;
                        case "enumMember":
                            return slanglsp.highlighting.SlangSyntaxHighlighterColors.CONSTANT;
                        case "function":
                        case "method":
                            return slanglsp.highlighting.SlangSyntaxHighlighterColors.FUNCTION_CALL;
                        case "macro":
                            return slanglsp.highlighting.SlangSyntaxHighlighterColors.MACRO_KEYWORD;
                        case "keyword":
                        case "modifier":
                            return slanglsp.highlighting.SlangSyntaxHighlighterColors.KEYWORD;
                        case "comment":
                            return slanglsp.highlighting.SlangSyntaxHighlighterColors.LINE_COMMENT;
                        case "string":
                            return slanglsp.highlighting.SlangSyntaxHighlighterColors.STRING;
                        case "number":
                            return slanglsp.highlighting.SlangSyntaxHighlighterColors.NUMBER;
                        case "operator":
                            return com.intellij.openapi.editor.DefaultLanguageHighlighterColors.OPERATION_SIGN; // Keep default for operator
                    }
                    
                    return super.getTextAttributesKey(type, modifiers, file);
                }
            });
            triggerChangeConfiguration();
        }
        if(serverStatus == com.redhat.devtools.lsp4ij.ServerStatus.stopped)
        {
            maybeAliveClients.remove(this);
        }
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


}