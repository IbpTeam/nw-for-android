package org.chromium.base.library_loader;import org.chromium.content_shell_apk.R;
public class NativeLibraries {
    public static boolean sUseLinker = false;
    public static boolean sUseLibraryInZipFile = false;
    public static boolean sEnableLinkerTests = false;
    public static final String[] LIBRARIES =
      {"ipc_mojo_perftests","osmesa","content_shell_content_view"};
    static String sVersionNumber =
      "";
}
