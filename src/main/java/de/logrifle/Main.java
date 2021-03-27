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
import de.logrifle.data.parsing.LineParser;
import de.logrifle.data.parsing.LineParserTimestampedTextImpl;
import de.logrifle.data.parsing.TimeStampFormat;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        parser.addArgument("-h", "--help")
                .action(Arguments.storeTrue())
                .help("Print this help and exit");
        parser.addArgument("logfile")
                .nargs("*")
                .help("Path to logfile");
        parser.addArgument("-c", "--commands-file")
                .type(String.class)
                .help("File to read commands from");
        parser.addArgument("-C", "--charset")
                .type(String.class)
                .help("Character set for reading/writing files. Defaults to UTF-8");
        parser.addArgument("-f", "--follow")
                .type(Boolean.class)
                .help("Initially follow tail? Defaults to false");
        parser.addArgument("-r", "--timestamp-regex")
                .type(String.class)
                .help("Regular expression to find timestamps in log lines. Defaults to " + TimeStampFormat.DEFAULT_TIME_MATCH_REGEX);
        parser.addArgument("-t", "--timestamp-format")
                .type(String.class)
                .help("Format to parse timestamps. Defaults to " + TimeStampFormat.DEFAULT_DATE_FORMAT);
        parser.addArgument("--seconds")
                .action(Arguments.storeTrue())
                .help("Shorthand for --timestamp-regex \"" + TimeStampFormat.SECONDS_TIME_MATCH_REGEX + "\" --timestamp-format \""+TimeStampFormat.SECONDS_DATE_FORMAT+"\"");
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
                System.err.println("Error: Unknown charset: "+charsetName);
                System.exit(-1);
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
        String commandsFile = getOption(defaults, parserResult, "commands_file");
        List<String> commands = new ArrayList<>();
        if (commandsFile != null) {
            try {
                commands.addAll(Files.readAllLines(Paths.get(commandsFile)));
            } catch (IOException e) {
                System.err.println("Commands file "+ commandsFile + " could not be opened. Cause: " + e.toString());
                System.exit(-1);
            }
        }

        boolean followTail = getBooleanOption(defaults, parserResult, "follow", false);

        int maxSidebarWidthCols = getIntegerOption(defaults, parserResult, "sidebar_max_cols", SideBar.DEFAULT_MAX_ABSOLUTE_WIDTH);
        double maxSidebarWidthRatio = getDoubleOption(defaults, parserResult, "sidebar_max_ratio", SideBar.DEFAULT_MAX_RELATIVE_WIDTH);

        ExecutorService workerPool = Executors.newCachedThreadPool();
        ScheduledExecutorService timerPool = Executors.newScheduledThreadPool(10);

        LogDispatcher logDispatcher = new LogDispatcher();
        boolean timestampsSecondsFormat = getBooleanOption(defaults, parserResult, "seconds", false);
        String timestampRegex;
        String timestampFormat;
        if (timestampsSecondsFormat) {
            timestampRegex = TimeStampFormat.SECONDS_TIME_MATCH_REGEX;
            timestampFormat = TimeStampFormat.SECONDS_DATE_FORMAT;
        } else {
            timestampRegex = getOption(defaults, parserResult, "timestamp_regex");
            timestampFormat = getOption(defaults, parserResult, "timestamp_format");
        }
        LineParser lineParser = new LineParserTimestampedTextImpl(new TimeStampFormat(timestampRegex, timestampFormat));
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

        FileOpener fileOpener = new MainFileOpenerImpl(lineParser, textColorIterator, workerPool, logDispatcher, factory, charset);

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
            } catch (IOException e) {
                System.err.println("Logfile "+ logfile.toString() + " could not be opened. Cause: " + e.toString());
                System.exit(-1);
            }
        }
        LineLabelDisplayMode lineLabelDisplayMode;
        if (logReaders.size() == 1) {
            lineLabelDisplayMode = LineLabelDisplayMode.NONE;
        } else {
            lineLabelDisplayMode = LineLabelDisplayMode.SHORT;
        }
        DataViewMerged rootView = new DataViewMerged(logReaders, logDispatcher, factory);

        Bookmarks bookmarks = new Bookmarks(charset);
        ViewsTree viewsTree = new ViewsTree(rootView, bookmarks);
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
                fileOpener);
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
        if (value != null) {
            return value;
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
}
