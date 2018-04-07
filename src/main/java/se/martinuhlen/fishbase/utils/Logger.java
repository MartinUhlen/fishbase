package se.martinuhlen.fishbase.utils;

import static java.lang.Math.max;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.rightPad;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class Logger
{
	private static final Map<Class<?>, Logger> LOGGERS = new ConcurrentHashMap<>();
	private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder().appendPattern("YYYY-MM-dd HH:mm:ss.SSS").toFormatter();
	private static final Map<String, String> THREAD_ALIASES = Map.of("JavaFX Application Thread", "JavaFX");

	private static int maxLogNameLength;
	private static int maxThreadNameLength;

	public static Logger getLogger(Class<?> logClass) // TODO Get caller class, with StackWalker?
	{
		return LOGGERS.computeIfAbsent(logClass, lc -> new Logger(lc.getSimpleName()));
	}

	private final String logName;

	private Logger(String logName)
	{
		this.logName = logName;
		maxLogNameLength = max(maxLogNameLength, logName.length());
	}

	public void log(String message)
	{
		String logMessage = Stream.of(
				logName(),
				DATE_TIME_FORMATTER.format(LocalDateTime.now()),
				threadName(),
				message)
			.map(str -> "[" + str + "]")
			.collect(joining(" "));

		System.out.println(logMessage);
	}

	private String logName()
	{
		return rightPad(logName, maxLogNameLength, ' ');
	}

	private String threadName()
	{
		String threadName = Thread.currentThread().getName();
		threadName = THREAD_ALIASES.getOrDefault(threadName, threadName);
		maxThreadNameLength = max(maxThreadNameLength, threadName.length());
		return rightPad(threadName, maxThreadNameLength, ' ');
	}
}
