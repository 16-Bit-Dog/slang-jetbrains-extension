package slanglsp;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SlangFileType extends LanguageFileType {
    public static final SlangFileType INSTANCE = new SlangFileType();

    private SlangFileType() {
        super(SlangLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Slang";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Slang shading language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "slang";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return IconLoader.getIcon("/icons/slang.svg", SlangFileType.class);
    }
}