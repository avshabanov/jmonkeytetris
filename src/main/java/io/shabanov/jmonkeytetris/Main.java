package io.shabanov.jmonkeytetris;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import io.shabanov.jmonkeytetris.app.DebugApplication;
import io.shabanov.jmonkeytetris.app.TetrisApplication;
import io.shabanov.jmonkeytetris.model.save.Persistence;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j @UtilityClass public class Main {

    public static void main(String[] args) {
        // reroute all the JUL log lines to SLF4J
        SLF4JBridgeHandler.install();

        log.info("Starting application");

        final List<String> argList = Arrays.asList(args);
        final SimpleApplication app = argList.contains("--debug") ? new DebugApplication() : new TetrisApplication();
        final boolean autoPersistSettings = argList.contains("--persist-settings");

        final AppSettings settings = new AppSettings(true);
        app.setShowSettings(false);

        if (tryLoadSettings(settings)) {
            log.info("Successfully loaded existing application settings");
        } else {
            settings.put("Width", 1024);
            settings.put("Height", 768);
            settings.put("Title", "jMonkey Tetris");

            if (autoPersistSettings) {
                tryPersistSettings(settings);
            }
        }

        app.setSettings(settings);
        app.start();
    }

    private static void tryPersistSettings(AppSettings settings) {
        final File settingsFile = Persistence.PREFERENCES_PATH.toFile();
        if (!settingsFile.exists()) {
            if (!Persistence.PREFERENCES_PATH.getParent().toFile().mkdirs()) {
                log.debug("Preferences dir already exist at destination or insufficient permissions");
            }
        }
        try (final FileOutputStream outputStream = new FileOutputStream(settingsFile)) {
            settings.save(outputStream);
        } catch (IOException e) {
            log.error("Unable to store settings to {}", settingsFile.getAbsolutePath(), e);
        }
    }

    private static boolean tryLoadSettings(AppSettings settings) {
        final File settingsFile = Persistence.PREFERENCES_PATH.toFile();
        if (!settingsFile.exists()) {
            log.info("Skip loading settings file as it does not exist at {}", settingsFile.getAbsolutePath());
            return false;
        }
        try (final FileInputStream inputStream = new FileInputStream(settingsFile)) {
            settings.load(inputStream);
            return true;
        } catch (IOException e) {
            log.error("Unable to load settings at {}", settingsFile.getAbsolutePath(), e);
            return false;
        }
    }
}
