/*
 * Copyright (c) 2011-2014 Pivotal Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * Copyright (c) 2004-2011 QOS.ch All rights reserved. <p> Permission is hereby granted, free  of charge, to any person
 * obtaining a  copy  of this  software  and  associated  documentation files  (the "Software"), to  deal in  the
 * Software without  restriction, including without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to permit persons to whom the Software  is
 * furnished to do so, subject to the following conditions: <p> The  above  copyright  notice  and  this permission
 * notice  shall  be included in all copies or substantial portions of the Software. <p> THE  SOFTWARE IS  PROVIDED  "AS
 * IS", WITHOUT  WARRANTY  OF ANY  KIND, EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package reactor.core.support;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.logging.Level;
import java.util.regex.Matcher;

import reactor.core.support.internal.PlatformDependent;

/**
 * Repackaged Logger for internal purposes. Will pick up the existing
 * logger implementation. Refer Factory for more information.
 */
public abstract class Logger {

	private final static LoggerFactory   defaultFactory  = newDefaultFactory(LoggerFactory.class.getName());

	private static final AtomicReferenceFieldUpdater<GlobalExtension, Extension> EXTENSION =
			PlatformDependent.newAtomicReferenceFieldUpdater(GlobalExtension.class, "extension");

	private static LoggerFactory newDefaultFactory(String name) {
		LoggerFactory f;
		try {
			f = new Slf4JLoggerFactory();
			f.getLogger(name)
			 .debug("Using Slf4j logging framework");
		}
		catch (Throwable t1) {
			f = new JdkLoggerFactory();
			f.getLogger(name)
			 .debug("Using JDK logging framework");
		}
		return f;
	}

	/**
	 *
	 * @param name
	 * @return
	 */
	public static Logger getLogger(String name) {
		return defaultFactory.getLogger(name);
	}

	/**
	 *
	 * @param klass
	 * @return
	 */
	public static Logger getLogger(Class<?> klass) {
		return defaultFactory.getLogger(klass.getName());
	}

	/**
	 *
	 * @param extension
	 */
	public static boolean enableExtension(Extension extension) {
		return EXTENSION.compareAndSet(LoggerFactory.globalExtension, null, extension);
	}

	/**
	 *
	 * @param extension
	 */
	public static boolean disableExtension(Extension extension) {
		if(EXTENSION.compareAndSet(LoggerFactory.globalExtension, extension, null)){
			LoggerFactory.globalExtension.cachedExtension = null;
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param from
	 * @param arguments
	 * @return
	 */
	public static String format(String from, Object... arguments){
		if(from != null) {
			String computed = from;
			if (arguments != null && arguments.length != 0) {
				for (Object argument : arguments) {
					computed = computed.replaceFirst("\\{\\}", Matcher.quoteReplacement(argument.toString()));
				}
			}
		}
		return null;
	}

	/**
	 *
	 */
	public interface LoggerFactory {
		
		GlobalExtension globalExtension = new GlobalExtension();
		
		Logger getLogger(String name);
	}

	/**
	 *
	 */
	public interface Extension {

		void log(String category, java.util.logging.Level level, String msg, Object... arguments);
	}

	/**
	 * Return the name of this <code>Logger</code> instance.
	 * @return name of this logger instance
	 */
	public abstract String getName();

	/**
	 * Is the logger instance enabled for the TRACE level?
	 *
	 * @return True if this Logger is enabled for the TRACE level,
	 *         false otherwise.
	 */
	public abstract boolean isTraceEnabled();

	/**
	 * Log a message at the TRACE level.
	 *
	 * @param msg the message string to be logged
	 */
	public abstract void trace(String msg);

	/**
	 * Log a message at the TRACE level according to the specified format
	 * and arguments.
	 * <p/>
	 * <p>This form avoids superfluous string concatenation when the logger
	 * is disabled for the TRACE level. However, this variant incurs the hidden
	 * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
	 * even if this logger is disabled for TRACE.</p>
	 *
	 * @param format    the format string
	 * @param arguments a list of 3 or more arguments
	 */
	public abstract void trace(String format, Object... arguments);

	/**
	 * Log an exception (throwable) at the TRACE level with an
	 * accompanying message.
	 *
	 * @param msg the message accompanying the exception
	 * @param t   the exception (throwable) to log
	 */
	public abstract void trace(String msg, Throwable t);

	/**
	 * Is the logger instance enabled for the DEBUG level?
	 *
	 * @return True if this Logger is enabled for the DEBUG level,
	 *         false otherwise.
	 */
	public abstract boolean isDebugEnabled();

	/**
	 * Log a message at the DEBUG level.
	 *
	 * @param msg the message string to be logged
	 */
	public abstract void debug(String msg);

	/**
	 * Log a message at the DEBUG level according to the specified format
	 * and arguments.
	 * <p/>
	 * <p>This form avoids superfluous string concatenation when the logger
	 * is disabled for the DEBUG level. However, this variant incurs the hidden
	 * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
	 * even if this logger is disabled for DEBUG. </p>
	 *
	 * @param format    the format string
	 * @param arguments a list of 3 or more arguments
	 */
	public abstract void debug(String format, Object... arguments);

	/**
	 * Log an exception (throwable) at the DEBUG level with an
	 * accompanying message.
	 *
	 * @param msg the message accompanying the exception
	 * @param t   the exception (throwable) to log
	 */
	public abstract void debug(String msg, Throwable t);

	/**
	 * Is the logger instance enabled for the INFO level?
	 *
	 * @return True if this Logger is enabled for the INFO level,
	 *         false otherwise.
	 */
	public abstract boolean isInfoEnabled();

	/**
	 * Log a message at the INFO level.
	 *
	 * @param msg the message string to be logged
	 */
	public abstract void info(String msg);

	/**
	 * Log a message at the INFO level according to the specified format
	 * and arguments.
	 * <p/>
	 * <p>This form avoids superfluous string concatenation when the logger
	 * is disabled for the INFO level. However, this variant incurs the hidden
	 * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
	 * even if this logger is disabled for INFO. </p>
	 *
	 * @param format    the format string
	 * @param arguments a list of 3 or more arguments
	 */
	public abstract void info(String format, Object... arguments);

	/**
	 * Log an exception (throwable) at the INFO level with an
	 * accompanying message.
	 *
	 * @param msg the message accompanying the exception
	 * @param t   the exception (throwable) to log
	 */
	public abstract void info(String msg, Throwable t);

	/**
	 * Is the logger instance enabled for the WARN level?
	 *
	 * @return True if this Logger is enabled for the WARN level,
	 *         false otherwise.
	 */
	public abstract boolean isWarnEnabled();

	/**
	 * Log a message at the WARN level.
	 *
	 * @param msg the message string to be logged
	 */
	public abstract void warn(String msg);

	/**
	 * Log a message at the WARN level according to the specified format
	 * and arguments.
	 * <p/>
	 * <p>This form avoids superfluous string concatenation when the logger
	 * is disabled for the WARN level. However, this variant incurs the hidden
	 * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
	 * even if this logger is disabled for WARN. </p>
	 *
	 * @param format    the format string
	 * @param arguments a list of 3 or more arguments
	 */
	public abstract void warn(String format, Object... arguments);

	/**
	 * Log an exception (throwable) at the WARN level with an
	 * accompanying message.
	 *
	 * @param msg the message accompanying the exception
	 * @param t   the exception (throwable) to log
	 */
	public abstract void warn(String msg, Throwable t);

	/**
	 * Is the logger instance enabled for the ERROR level?
	 *
	 * @return True if this Logger is enabled for the ERROR level,
	 *         false otherwise.
	 */
	public abstract boolean isErrorEnabled();

	/**
	 * Log a message at the ERROR level.
	 *
	 * @param msg the message string to be logged
	 */
	public abstract void error(String msg);

	/**
	 * Log a message at the ERROR level according to the specified format
	 * and arguments.
	 * <p/>
	 * <p>This form avoids superfluous string concatenation when the logger
	 * is disabled for the ERROR level. However, this variant incurs the hidden
	 * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
	 * even if this logger is disabled for ERROR. </p>
	 *
	 * @param format    the format string
	 * @param arguments a list of 3 or more arguments
	 */
	public abstract void error(String format, Object... arguments);

	/**
	 * Log an exception (throwable) at the ERROR level with an
	 * accompanying message.
	 *
	 * @param msg the message accompanying the exception
	 * @param t   the exception (throwable) to log
	 */
	public abstract void error(String msg, Throwable t);

	private static class Slf4JLoggerFactory implements Logger.LoggerFactory {

		@Override
		public Logger getLogger(String name) {
			return new Slf4JLogger(org.slf4j.LoggerFactory.getLogger(name));
		}
	}

	/**
	 * Wrapper over Slf4j Logger
	 */
	private static class Slf4JLogger extends Logger {

		private final org.slf4j.Logger logger;

		public Slf4JLogger(org.slf4j.Logger logger) {
			this.logger = logger;
		}

		@Override
		public String getName() {
			return logger.getName();
		}

		@Override
		public boolean isTraceEnabled() {
			return logger.isTraceEnabled();
		}

		@Override
		public void trace(String msg) {
			logger.trace(msg);
			LoggerFactory.globalExtension.log(logger.getName(), Level.FINEST, msg);
		}

		@Override
		public void trace(String format, Object... arguments) {
			logger.trace(format, arguments);
			LoggerFactory.globalExtension.log(logger.getName(), Level.FINEST, format, arguments);
		}

		@Override
		public void trace(String msg, Throwable t) {
			logger.trace(msg, t);
			LoggerFactory.globalExtension.log(logger.getName(), Level.FINEST, msg, t);
		}

		@Override
		public boolean isDebugEnabled() {
			return logger.isDebugEnabled();
		}

		@Override
		public void debug(String msg) {
			logger.debug(msg);
			LoggerFactory.globalExtension.log(logger.getName(), Level.FINE, msg);
		}

		@Override
		public void debug(String format, Object... arguments) {
			logger.debug(format, arguments);
			LoggerFactory.globalExtension.log(logger.getName(), Level.FINE, format, arguments);
		}

		@Override
		public void debug(String msg, Throwable t) {
			logger.debug(msg, t);
			LoggerFactory.globalExtension.log(logger.getName(), Level.FINE, msg, t);
		}

		@Override
		public boolean isInfoEnabled() {
			return logger.isInfoEnabled();
		}

		@Override
		public void info(String msg) {
			logger.info(msg);
			LoggerFactory.globalExtension.log(logger.getName(), Level.INFO, msg);
		}

		@Override
		public void info(String format, Object... arguments) {
			logger.info(format, arguments);
			LoggerFactory.globalExtension.log(logger.getName(), Level.INFO, format, arguments);
		}

		@Override
		public void info(String msg, Throwable t) {
			logger.info(msg, t);
			LoggerFactory.globalExtension.log(logger.getName(), Level.INFO, msg, t);
		}

		@Override
		public boolean isWarnEnabled() {
			return logger.isWarnEnabled();
		}

		@Override
		public void warn(String msg) {
			logger.warn(msg);
			LoggerFactory.globalExtension.log(logger.getName(), Level.WARNING, msg);
		}

		@Override
		public void warn(String format, Object... arguments) {
			logger.warn(format, arguments);
			LoggerFactory.globalExtension.log(logger.getName(), Level.WARNING, format, arguments);
		}

		@Override
		public void warn(String msg, Throwable t) {
			logger.warn(msg, t);
			LoggerFactory.globalExtension.log(logger.getName(), Level.WARNING, msg, t);
		}

		@Override
		public boolean isErrorEnabled() {
			return logger.isErrorEnabled();
		}

		@Override
		public void error(String msg) {
			logger.error(msg);
			LoggerFactory.globalExtension.log(logger.getName(), Level.SEVERE, msg);
		}

		@Override
		public void error(String format, Object... arguments) {
			logger.error(format, arguments);
			LoggerFactory.globalExtension.log(logger.getName(), Level.SEVERE, format, arguments);
		}

		@Override
		public void error(String msg, Throwable t) {
			logger.error(msg, t);
			LoggerFactory.globalExtension.log(logger.getName(), Level.SEVERE, msg, t);
		}
	}

	private static class JdkLoggerFactory implements Logger.LoggerFactory {

		@Override
		public Logger getLogger(String name) {
			return new JdkLogger(java.util.logging.Logger.getLogger(name));
		}
	}

	/**
	 * Wrapper over JDK logger
	 */
	private static class JdkLogger extends Logger {

		private final java.util.logging.Logger logger;

		public JdkLogger(java.util.logging.Logger logger) {
			this.logger = logger;
		}

		@Override
		public String getName() {
			return logger.getName();
		}

		@Override
		public boolean isTraceEnabled() {
			return logger.isLoggable(Level.FINEST);
		}

		@Override
		public void trace(String msg) {
			if (logger.isLoggable(Level.FINEST)) {
				logger.log(Level.FINEST, msg);
				LoggerFactory.globalExtension.log(logger.getName(), Level.FINEST, msg);
			}
		}

		@Override
		public void trace(String format, Object... arguments) {
			if (logger.isLoggable(Level.FINEST)) {
				logger.log(Level.FINEST, format(format, arguments));
				LoggerFactory.globalExtension.log(logger.getName(), Level.FINEST, format, arguments);
			}
		}

		@Override
		public void trace(String msg, Throwable t) {
			if (logger.isLoggable(Level.FINEST)) {
				logger.log(Level.FINEST, msg, t);
				LoggerFactory.globalExtension.log(logger.getName(), Level.FINEST, msg, t);
			}
		}

		@Override
		public boolean isDebugEnabled() {
			return logger.isLoggable(Level.FINE);
		}

		@Override
		public void debug(String msg) {
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, msg);
				LoggerFactory.globalExtension.log(logger.getName(), Level.FINE, msg);
			}
		}

		@Override
		public void debug(String format, Object... arguments) {
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, format(format, arguments));
				LoggerFactory.globalExtension.log(logger.getName(), Level.FINE, format, arguments);
			}
		}

		@Override
		public void debug(String msg, Throwable t) {
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, msg, t);
				LoggerFactory.globalExtension.log(logger.getName(), Level.FINE, msg, t);
			}
		}

		@Override
		public boolean isInfoEnabled() {
			return logger.isLoggable(Level.INFO);
		}

		@Override
		public void info(String msg) {
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, msg);
				LoggerFactory.globalExtension.log(logger.getName(), Level.INFO, msg);
			}
		}

		@Override
		public void info(String format, Object... arguments) {
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, format(format, arguments));
				LoggerFactory.globalExtension.log(logger.getName(), Level.INFO, format, arguments);
			}
		}

		@Override
		public void info(String msg, Throwable t) {
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, msg, t);
				LoggerFactory.globalExtension.log(logger.getName(), Level.INFO, msg, t);
			}
		}

		@Override
		public boolean isWarnEnabled() {
			return logger.isLoggable(Level.WARNING);
		}

		@Override
		public void warn(String msg) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, msg);
				LoggerFactory.globalExtension.log(logger.getName(), Level.WARNING, msg);
			}
		}

		@Override
		public void warn(String format, Object... arguments) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, format(format, arguments));
				LoggerFactory.globalExtension.log(logger.getName(), Level.WARNING, format, arguments);
			}
		}

		@Override
		public void warn(String msg, Throwable t) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, msg, t);
				LoggerFactory.globalExtension.log(logger.getName(), Level.WARNING, msg, t);
			}
		}

		@Override
		public boolean isErrorEnabled() {
			return logger.isLoggable(Level.SEVERE);
		}

		@Override
		public void error(String msg) {
			if (logger.isLoggable(Level.SEVERE)) {
				logger.log(Level.SEVERE, msg);
				LoggerFactory.globalExtension.log(logger.getName(), Level.SEVERE, msg);
			}
		}

		@Override
		public void error(String format, Object... arguments) {
			if (logger.isLoggable(Level.SEVERE)) {
				logger.log(Level.SEVERE, format(format, arguments));
				LoggerFactory.globalExtension.log(logger.getName(), Level.SEVERE, format, arguments);
			}
		}

		@Override
		public void error(String msg, Throwable t) {
			if (logger.isLoggable(Level.SEVERE)) {
				logger.log(Level.SEVERE, msg, t);
				LoggerFactory.globalExtension.log(logger.getName(), Level.SEVERE, msg, t);
			}
		}

	}

	private static class GlobalExtension implements Extension {

		@SuppressWarnings("unused")
		private volatile Extension extension;
		private          Extension cachedExtension;

		@Override
		public void log(String category, Level level, String msg, Object... arguments) {
			if (cachedExtension == null) {
				cachedExtension = extension;
				if (cachedExtension == null) {
					return;
				}
			}

			cachedExtension.log(category, level, msg, arguments);
		}
	}
}
