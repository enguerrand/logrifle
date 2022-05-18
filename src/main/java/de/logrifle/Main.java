/*
 *  Copyright 2019, Enguerrand de Rochefort
 *
 * This file is part of logrifle.
 *
 * logrifle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * logrifle is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with logrifle.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.logrifle;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.TextGUI;
import com.googlecode.lanterna.input.KeyStroke;
import de.logrifle.base.DefaultUncaughtExceptionHandler;
import de.logrifle.base.LogDispatcher;
import de.logrifle.base.RateLimiterFactory;
import de.logrifle.base.RateLimiterImpl;
import de.logrifle.data.bookmarks.Bookmarks;
import de.logrifle.data.highlights.HighlightsData;
import de.logrifle.data.io.FileOpener;
import de.logrifle.data.io.MainFileOpenerImpl;
import de.logrifle.data.parsing.LineParserProvider;
import de.logrifle.data.parsing.LineParserProviderDynamicImpl;
import de.logrifle.data.parsing.LineParserProviderStaticImpl;
import de.logrifle.data.parsing.TimeStampFormats;
import de.logrifle.data.views.DataView;
import de.logrifle.data.views.DataViewMerged;
import de.logrifle.data.views.ViewsTree;
import de.logrifle.ui.CommandView;
import de.logrifle.ui.LineLabelDisplayMode;
import de.logrifle.ui.MainController;
import de.logrifle.ui.MainWindow;
import de.logrifle.ui.MainWindowListener;
import de.logrifle.ui.RingIterator;
import de.logrifle.ui.SideBar;
import de.logrifle.ui.UI;
import de.logrifle.ui.cmd.CommandHandler;
import de.logrifle.ui.cmd.ExecutionResult;
import de.logrifle.ui.cmd.KeyMapFactory;
import de.logrifle.ui.cmd.KeyStrokeHandler;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class Main {
    private static final String DEFAULTS_FILE =
            System.getProperty("user.home") + System.getProperty("file.separator") + ".logriflerc";

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(null));

        KeyMapFactory keyMapFactory = new KeyMapFactory();
        CommandHandler commandHandler = new CommandHandler();

        Properties defaults = loadDefaults();

        ArgumentParser parser = ArgumentParsers.newFor("logrifle")
                .addHelp(false)
                .defaultFormatWidth(100)
                .build();
        parser.addArgument("-a", "--auto-detection-line-count")
                .type(Integer.class)
                .help("Attempt to detect the timestamp regex and format automatically using the file's first "
                        + "AUTO_DETECTION_LINE_COUNT lines. (Defaults to "+TimeStampFormats.DEFAULT_AUTO_DETECTION_LINE_COUNT+")");
        parser.addArgument("-h", "--help")
                .action(Arguments.storeTrue())
                .help("Print this help and exit");
        parser.addArgument("logfile")
                .nargs("*")
                .help("Path to logfile");
        parser.addArgument("-B", "--forced-bookmarks-display")
                .action(Arguments.storeTrue())
                .help("Always display lines that are bookmarked irrespectively of active filters");
        parser.addArgument("-c", "--commands-file")
                .type(String.class)
                .help("File to read commands from");
        parser.addArgument("-C", "--charset")
                .type(String.class)
                .help("Character set for reading/writing files. Defaults to UTF-8");
        parser.addArgument("-f", "--follow")
                .type(Boolean.class)
                .help("Initially follow tail? Defaults to false");
        parser.addArgument("--milliseconds")
                .action(Arguments.storeTrue())
                .help("Shorthand for --timestamp-regex \"" + TimeStampFormats.MILLIS_TIME_MATCH_REGEX + "\" --timestamp-format \""+ TimeStampFormats.MILLIS_DATE_FORMAT +"\". " +
                        "When specified, options --seconds and --auto-detection-line-count are ignored.");
        parser.addArgument("-r", "--timestamp-regex")
                .type(String.class)
                .help("Regular expression to find timestamps in log lines. Also requires --timestamp-format. " +
                        "When specified, --auto-detection-line-count, --seconds and --milliseconds are ignored.");
        parser.addArgument("--seconds")
                .action(Arguments.storeTrue())
                .help("Shorthand for --timestamp-regex \"" + TimeStampFormats.SECONDS_TIME_MATCH_REGEX + "\" --timestamp-format \""+ TimeStampFormats.SECONDS_DATE_FORMAT+"\". " +
                        "When specified, option --auto-detection-line-count is ignored.");
        parser.addArgument("-t", "--timestamp-format")
                .type(String.class)
                .help("Format to parse timestamps. Also requires --timestamp-regex. When specified, --auto-detection-line-count, --seconds and --milliseconds are ignored.");
        parser.addArgument("-v", "--version")
                .action(Arguments.storeTrue())
                .help("Print version info and exit");
        parser.addArgument("--sidebar-max-cols")
                .type(Integer.class)
                .help("The sidebar's initial maximum size in columns. Defaults to " + SideBar.DEFAULT_MAX_ABSOLUTE_WIDTH);
        parser.addArgument("--sidebar-max-ratio")
                .type(Double.class)
                .help("The sidebar's initial maximum size relative to the full window width. Defaults to " + SideBar.DEFAULT_MAX_RELATIVE_WIDTH);
        Namespace parserResult = parser.parseArgsOrFail(args);

        if (parserResult.getBoolean("help")) {
            parser.printHelp();
            System.out.print(commandHandler.getHelp(keyMapFactory.get(), CommandView.getKeyBinds()));
            System.exit(0);
        }
        if (parserResult.getBoolean("version")) {
            BuildProperties buildProperties = new BuildProperties();
            System.out.println("logrifle version " + buildProperties.getVersion());
            System.exit(0);
        }
        String charsetName = parserResult.getString("charset");
        Charset charset = StandardCharsets.UTF_8;
        if (charsetName != null) {
            try {
                charset = Charset.forName(charsetName);
            } catch (RuntimeException e) {
                errorOut("Error: Unknown charset: "+charsetName);
            }
        }
        List<Path> logfiles = parserResult.getList("logfile").stream()
                .map(f -> Paths.get((String)f))
                .collect(Collectors.toList());
        if (logfiles.isEmpty()) {
            System.err.println("Error: Arguments missing! Need at least one logfile!");
            parser.printUsage();
            return;
        }
        String directories = logfiles.stream().filter(Files::isDirectory).map(Path::toString).collect(Collectors.joining(", "));
        if (!directories.isEmpty()) {
            errorOut("Cannot open directories as files: "+ directories);
        }
        String commandsFile = getOption(defaults, parserResult, "commands_file");
        List<String> commands = new ArrayList<>();
        if (commandsFile != null) {
            try {
                commands.addAll(Files.readAllLines(Paths.get(commandsFile)));
            } catch (IOException e) {
                errorOut("Commands file "+ commandsFile + " could not be opened. Cause: " + e);
            }
        }

        boolean followTail = getBooleanOption(defaults, parserResult, "follow", false);

        int maxSidebarWidthCols = getIntegerOption(defaults, parserResult, "sidebar_max_cols", SideBar.DEFAULT_MAX_ABSOLUTE_WIDTH);
        double maxSidebarWidthRatio = getDoubleOption(defaults, parserResult, "sidebar_max_ratio", SideBar.DEFAULT_MAX_RELATIVE_WIDTH);

        ExecutorService workerPool = Executors.newCachedThreadPool();
        ScheduledExecutorService timerPool = Executors.newScheduledThreadPool(10);

        LogDispatcher logDispatcher = new LogDispatcher();
        boolean timestampsMillisFormat = getBooleanOption(defaults, parserResult, "milliseconds", false);
        boolean timestampsSecondsFormat = getBooleanOption(defaults, parserResult, "seconds", false);
        @Nullable String timestampRegex = getOption(defaults, parserResult, "timestamp_regex");
        @Nullable String timestampFormat = getOption(defaults, parserResult, "timestamp_format");
        if (timestampFormat == null && timestampRegex != null || timestampFormat != null && timestampRegex == null) {
            errorOut("if either timestamp_regex or timestamp_format are supplied, both of these options must be specified.");
        } else if (timestampFormat == null) {
            if (timestampsMillisFormat) {
                timestampRegex = TimeStampFormats.MILLIS_TIME_MATCH_REGEX;
                timestampFormat = TimeStampFormats.MILLIS_DATE_FORMAT;
            } else if (timestampsSecondsFormat){
                timestampRegex = TimeStampFormats.SECONDS_TIME_MATCH_REGEX;
                timestampFormat = TimeStampFormats.SECONDS_DATE_FORMAT;
            }
        }
        List<DataView> logReaders = new ArrayList<>();
        RingIterator<TextColor> textColorIterator = new RingIterator<>(Arrays.asList(
                TextColor.ANSI.BLUE,
                TextColor.ANSI.GREEN,
                TextColor.ANSI.MAGENTA,
                TextColor.ANSI.RED,
                TextColor.ANSI.CYAN,
                TextColor.ANSI.YELLOW,
                TextColor.ANSI.WHITE
        ));

        RateLimiterFactory factory = (task, singleThreadedExecutor) ->
                new RateLimiterImpl(task, singleThreadedExecutor, timerPool, 150);

        LineParserProvider lineParserProvider;
        if (timestampRegex != null && timestampFormat != null) {
            lineParserProvider = new LineParserProviderStaticImpl(timestampRegex, timestampFormat);
        } else {
            int autoDetectionLineCount = getIntegerOption(defaults, parserResult, "auto_detection_line_count", TimeStampFormats.DEFAULT_AUTO_DETECTION_LINE_COUNT);
            lineParserProvider = new LineParserProviderDynamicImpl(autoDetectionLineCount, new TimeStampFormats());
        }

        FileOpener fileOpener = new MainFileOpenerImpl(
                lineParserProvider,
                textColorIterator,
                workerPool,
                logDispatcher,
                factory,
                charset
        );

        for (Path logfile : logfiles) {
            try {
                Collection<DataView> openedViews = fileOpener.open(logfile);
                logReaders.addAll(openedViews);
                for (DataView openedView : openedViews) {
                    openedView.setCloseHook(() ->
                            UI.runLater(() ->
                                    logReaders.remove(openedView))
                    );
                }
            } catch (IOException | UncheckedIOException e) {
                errorOut("Logfile "+ logfile.toString() + " could not be opened. Cause: " + e);
            }
        }
        LineLabelDisplayMode lineLabelDisplayMode;
        if (logReaders.size() == 1) {
            lineLabelDisplayMode = LineLabelDisplayMode.NONE;
        } else {
            lineLabelDisplayMode = LineLabelDisplayMode.SHORT;
        }
        DataViewMerged rootView = new DataViewMerged(logReaders, logDispatcher, factory);

        boolean forcedBookmarksDisplay = getBooleanOption(defaults, parserResult, "forced_bookmarks_display", false);
        Bookmarks bookmarks = new Bookmarks(forcedBookmarksDisplay,logDispatcher);
        ViewsTree viewsTree = new ViewsTree(rootView, bookmarks);
        bookmarks.addListener(viewsTree.buildBookmarksListener());
        HighlightsData highlightsData = new HighlightsData();
        MainWindow mainWindow = new MainWindow(viewsTree, highlightsData, bookmarks, logDispatcher, followTail, maxSidebarWidthCols, maxSidebarWidthRatio, lineLabelDisplayMode);
        KeyStrokeHandler keyStrokeHandler = new KeyStrokeHandler(keyMapFactory.get(), commandHandler);
        MainController mainController = new MainController(
                mainWindow,
                commandHandler,
                keyStrokeHandler,
                logDispatcher,
                viewsTree,
                highlightsData,
                bookmarks,
                fileOpener,
                charset
        );
        commandHandler.setMainController(mainController);
        mainWindow.start(workerPool, new MainWindowListener() {
            @Override
            public boolean onUnhandledKeyStroke(TextGUI textGUI, KeyStroke keyStroke) {
                return mainController.handleKeyStroke(keyStroke);
            }

            @Override
            public void onClosed() {
                for (DataView logReader : logReaders) {
                    logReader.destroy();
                }
                workerPool.shutdown();
                System.exit(0);
            }
        });
        UI.awaitInitialized().thenRun(
                () -> {
                    UI.runLater(mainWindow::updateView);
                    for (String command : commands) {
                        if (command.isEmpty()) {
                            continue;
                        }
                        UI.runLater(() -> {
                            ExecutionResult result = commandHandler.handle(command.substring(1), true);
                            if (result.isUiUpdateRequired()) {
                                mainWindow.updateView();
                            }
                            result.getUserMessage().ifPresent(System.err::println);
                        });
                    }
                }
        );
    }

    @NotNull
    private static Properties loadDefaults() {
        Properties defaults = new Properties();
        try (InputStream input = new FileInputStream(DEFAULTS_FILE)) {
            defaults.load(input);
        } catch (IOException ignored) {
        }
        return defaults;
    }

    private static String getOption(Properties defaults, Namespace parserResult, String name) {
        String value = parserResult.getString(name);
        if (value != null) {
            return value;
        }
        return defaults.getProperty(name);
    }

    private static boolean getBooleanOption(Properties defaults, Namespace parserResult, String name, boolean fallBack) {
        Boolean value = parserResult.getBoolean(name);
        if (value != null && value) {
            return true;
        }
        String defaultValue = defaults.getProperty(name);
        if (defaultValue == null) {
            return fallBack;
        }
        return "true".equals(defaultValue);
    }

    private static int getIntegerOption(Properties defaults, Namespace parserResult, String name, int fallBack) {
        Integer value = parserResult.getInt(name);
        if (value != null) {
            return value;
        }
        String defaultValue = defaults.getProperty(name);
        if (defaultValue == null) {
            return fallBack;
        }
        return Integer.parseInt(defaultValue);
    }

    private static double getDoubleOption(Properties defaults, Namespace parserResult, String name, double fallBack) {
        Double value = parserResult.getDouble(name);
        if (value != null) {
            return value;
        }
        String defaultValue = defaults.getProperty(name);
        if (defaultValue == null) {
            return fallBack;
        }
        return Double.parseDouble(defaultValue);
    }

    private static void errorOut(String message) {
        System.err.println(message);
        System.exit(1);
    }
}
