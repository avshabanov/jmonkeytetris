package io.shabanov.jmonkeytetris.model.save;

import lombok.experimental.UtilityClass;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides constants and utilities for managing application's persistent state such as save/load functionality
 * as well as store/load application's preferences.
 */
@UtilityClass public class Persistence {

    public static final Path BASE_PATH = Paths.get(System.getProperty("user.home"), ".jmonkeytetris");

    public static final Path PREFERENCES_PATH = BASE_PATH.resolve("app-settings.properties");

    public static final Path QUICK_SAVE_PATH = BASE_PATH.resolve("saves").resolve("quick");
}
