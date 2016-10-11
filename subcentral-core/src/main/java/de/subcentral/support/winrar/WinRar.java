package de.subcentral.support.winrar;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class WinRar {
    private static final Logger log      = LogManager.getLogger(WinRar.class.getName());

    private static final WinRar instance = initInstance();

    private static WinRar initInstance() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return new WindowsWinRar();
        }
        else if (SystemUtils.IS_OS_UNIX) {
            return new UnixWinRar();
        }
        return null;
    }

    public static WinRar getInstance() throws UnsupportedOperationException {
        if (instance == null) {
            throw new UnsupportedOperationException("Unsupported operating system: " + SystemUtils.OS_NAME + " " + SystemUtils.OS_VERSION + " " + SystemUtils.OS_ARCH
                    + " not supported. Only Windows and Unix like systems are supported.");
        }
        return instance;
    }

    // non-static
    protected ExecutorService processExecutor;

    public ExecutorService getProcessExecutor() {
        return processExecutor;
    }

    public void setProcessExecutor(ExecutorService processExecutor) {
        this.processExecutor = processExecutor;
    }

    public abstract Path getRarExecutableFilename();

    /**
     *
     * 
     * @return the located rar executable or <code>null</code> if it could not be located.
     */
    public abstract Path locateRarExecutable();

    protected Path returnFirstValidRarExecutable(Iterable<Path> possibleWinRarDirectories) {
        for (Path path : possibleWinRarDirectories) {
            Path candidate = path.resolve(getRarExecutableFilename());
            try {
                validateRarExecutable(candidate);
                log.debug("Found RAR executable at {}", candidate);
                return candidate;
            }
            catch (Exception e) {
                log.trace("{} was no valid RAR executable: {}", candidate, e.toString());
            }
        }
        return null;
    }

    public Path validateRarExecutable(Path exe) throws NullPointerException, NoSuchFileException, SecurityException {
        if (exe == null) {
            throw new NullPointerException("Rar executable cannot be null");
        }
        if (!Files.isRegularFile(exe, LinkOption.NOFOLLOW_LINKS)) {
            throw new NoSuchFileException(exe.toString());
        }
        if (!Files.isExecutable(exe)) {
            throw new SecurityException("Rar executable is not executable: " + exe);
        }
        return exe;
    }

    public WinRarPackager getPackager() {
        Path locatedRarExe = locateRarExecutable();
        if (locatedRarExe == null) {
            throw new IllegalStateException("Could not locate rar executable");
        }
        return getPackager(locatedRarExe);
    }

    public abstract WinRarPackager getPackager(Path rarExecutable);
}
