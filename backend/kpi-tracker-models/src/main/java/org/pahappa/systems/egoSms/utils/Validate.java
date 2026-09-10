package org.pahappa.systems.egoSms.utils;

import static java.lang.String.format;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.sers.webutils.model.exception.ValidationFailedException;

/**
 * Adapted from Spring Assert class but throws BadRequest exceptions
 */
public abstract class Validate {

	/**
	 * Assert a boolean expression, throwing an
	 * {@code ValidationFailedException} if the expression evaluates to
	 * {@code false}.
	 * <p>
	 * Call {@link #isTrue} if you wish to throw an
	 * {@code ValidationFailedException} on an assertion failure.
	 * 
	 * <pre class="code">
	 * Assert.state(id == null, &quot;The id property must not already be initialized&quot;);
	 * </pre>
	 * 
	 * @param expression
	 *            a boolean expression
	 * @param message
	 *            the exception message to use if the assertion fails
	 * @throws ValidationFailedException
	 *             if {@code expression} is {@code false}
	 */
	public static void check(Boolean expression, String message, Object... params) throws ValidationFailedException {
		isTrue(expression, format(message, params));
	}

	public static void state(Boolean expression, String message) throws ValidationFailedException {
		if (!Boolean.TRUE.equals(expression)) {
			throw new ValidationFailedException(message);
		}
	}

	/**
	 * Assert a boolean expression, throwing an
	 * {@code ValidationFailedException} if the expression evaluates to
	 * {@code false}.
	 * 
	 * <pre class="code">
	 * Assert.isTrue(i &gt; 0, &quot;The value must be greater than zero&quot;);
	 * </pre>
	 * 
	 * @param expression
	 *            a boolean expression
	 * @param message
	 *            the exception message to use if the assertion fails
	 * @throws ValidationFailedException
	 *             if {@code expression} is {@code false}
	 */
	public static void isTrue(Boolean expression, String message, Object... params) throws ValidationFailedException {
		if (!Boolean.TRUE.equals(expression)) {
			throw new ValidationFailedException(format(message, params));
		}
	}

	/**
	 * Assert that an object is {@code null}.
	 * 
	 * <pre class="code">
	 * Assert.isNull(value, &quot;The value must be null&quot;);
	 * </pre>
	 * 
	 * @param object
	 *            the object to check
	 * @param message
	 *            the exception message to use if the assertion fails
	 * @throws ValidationFailedException
	 *             if the object is not {@code null}
	 */
	public static void isNull(Object object, String message, Object... args) throws ValidationFailedException {
		if (object != null) {
			throw new ValidationFailedException(format(message, args));
		}
	}

	/**
	 * Assert that an object is not {@code null}.
	 * 
	 * <pre class="code">
	 * Assert.notNull(clazz, &quot;The class must not be null&quot;);
	 * </pre>
	 * 
	 * @param object
	 *            the object to check
	 * @param message
	 *            the exception message to use if the assertion fails
	 * @throws ValidationFailedException
	 * @throws ValidationFailedException
	 *             if the object is {@code null}
	 */
	public static void notNull(Object object, String message, Object... args) throws ValidationFailedException {
		if (object == null) {
			throw new ValidationFailedException(format(message, args));
		}
	}

	/**
	 * Assert that the given String is not empty; that is, it must not be
	 * {@code null} and not the empty String.
	 * 
	 * <pre class="code">
	 * Assert.hasLength(name, &quot;Name must not be empty&quot;);
	 * </pre>
	 * 
	 * @param text
	 *            the String to check
	 * @param message
	 *            the exception message to use if the assertion fails
	 * @see StringUtils#isNoneEmpty(CharSequence...)
	 * @throws ValidationFailedException
	 *             if the text is empty
	 */
	public static void hasLength(String text, String message) throws ValidationFailedException {
		if (!StringUtils.isNotEmpty(text)) {
			throw new ValidationFailedException(message);
		}
	}

	/**
	 * Assert that the given String contains valid text content; that is, it
	 * must not be {@code null} and must contain at least one non-whitespace
	 * character.
	 * 
	 * <pre class="code">
	 * Assert.hasText(name, &quot;'name' must not be empty&quot;);
	 * </pre>
	 * 
	 * @param text
	 *            the String to check
	 * @param message
	 *            the exception message to use if the assertion fails
	 * @see StringUtils#isNoneEmpty(CharSequence...)
	 * @throws ValidationFailedException
	 *             if the text does not contain valid text content
	 */
	public static void hasText(String text, String message) throws ValidationFailedException {
		if (!StringUtils.isNotBlank(text)) {
			throw new ValidationFailedException(message);
		}
	}

	/**
	 * Assert that the given text does not contain the given substring.
	 * 
	 * <pre class="code">
	 * Assert.doesNotContain(name, &quot;rod&quot;, &quot;Name must not contain 'rod'&quot;);
	 * </pre>
	 * 
	 * @param textToSearch
	 *            the text to search
	 * @param substring
	 *            the substring to find within the text
	 * @param message
	 *            the exception message to use if the assertion fails
	 * @throws ValidationFailedException
	 *             if the text contains the substring
	 */
	public static void doesNotContain(String textToSearch, String substring, String message)
			throws ValidationFailedException {
		if (StringUtils.isNotEmpty(textToSearch) && StringUtils.isNotEmpty(substring)
				&& textToSearch.contains(substring)) {
			throw new ValidationFailedException(message);
		}
	}

	/**
	 * Assert that an array contains elements; that is, it must not be
	 * {@code null} and must contain at least one element.
	 * 
	 * <pre class="code">
	 * Assert.notEmpty(array, &quot;The array must contain elements&quot;);
	 * </pre>
	 * 
	 * @param array
	 *            the array to check
	 * @param message
	 *            the exception message to use if the assertion fails
	 * @throws ValidationFailedException
	 *             if the object array is {@code null} or contains no elements
	 */
	public static void notEmpty(Object[] array, String message) throws ValidationFailedException {
		if (ArrayUtils.isEmpty(array)) {
			throw new ValidationFailedException(message);
		}
	}

	/**
	 * Assert that an array contains no {@code null} elements.
	 * <p>
	 * Note: Does not complain if the array is empty!
	 * 
	 * <pre class="code">
	 * Assert.noNullElements(array, &quot;The array must contain non-null elements&quot;);
	 * </pre>
	 * 
	 * @param array
	 *            the array to check
	 * @param message
	 *            the exception message to use if the assertion fails
	 * @throws ValidationFailedException
	 *             if the object array contains a {@code null} element
	 */
	public static void noNullElements(Object[] array, String message) throws ValidationFailedException {
		if (array != null) {
			for (Object element : array) {
				if (element == null) {
					throw new ValidationFailedException(message);
				}
			}
		}
	}

	/**
	 * Assert that a collection contains elements; that is, it must not be
	 * {@code null} and must contain at least one element.
	 * 
	 * <pre class="code">
	 * Assert.notEmpty(collection, &quot;Collection must contain elements&quot;);
	 * </pre>
	 * 
	 * @param collection
	 *            the collection to check
	 * @param message
	 *            the exception message to use if the assertion fails
	 * @throws ValidationFailedException
	 *             if the collection is {@code null} or contains no elements
	 */
	public static void notEmpty(Collection<?> collection, String message) throws ValidationFailedException {
		if (CollectionUtils.isEmpty(collection)) {
			throw new ValidationFailedException(message);
		}
	}

	/**
	 * Assert that a Map contains entries; that is, it must not be {@code null}
	 * and must contain at least one entry.
	 * 
	 * <pre class="code">
	 * Assert.notEmpty(map, &quot;Map must contain entries&quot;);
	 * </pre>
	 * 
	 * @param map
	 *            the map to check
	 * @param message
	 *            the exception message to use if the assertion fails
	 * @throws ValidationFailedException
	 *             if the map is {@code null} or contains no entries
	 */
	public static void notEmpty(Map<?, ?> map, String message) throws ValidationFailedException {
		if (MapUtils.isEmpty(map)) {
			throw new ValidationFailedException(message);
		}
	}

	/**
	 * Assert that the provided object is an instance of the provided class.
	 * 
	 * <pre class="code">
	 * Assert.instanceOf(Foo.class, foo, &quot;Foo expected&quot;);
	 * </pre>
	 * 
	 * @param type
	 *            the type to check against
	 * @param obj
	 *            the object to check
	 * @param message
	 *            a message which will be prepended to provide further context.
	 *            If it is empty or ends in ":" or ";" or "," or ".", a full
	 *            exception message will be appended. If it ends in a space, the
	 *            name of the offending object's type will be appended. In any
	 *            other case, a ":" with a space and the name of the offending
	 *            object's type will be appended.
	 * @throws ValidationFailedException
	 * @throws ValidationFailedException
	 *             if the object is not an instance of type
	 */
	public static void isInstanceOf(Class<?> type, Object obj, String message) throws ValidationFailedException {
		notNull(type, "Type to check against must not be null");
		if (!type.isInstance(obj)) {
			instanceCheckFailed(type, obj, message);
		}
	}

	/**
	 * Assert that the provided object is an instance of the provided class.
	 * 
	 * <pre class="code">
	 * Assert.instanceOf(Foo.class, foo);
	 * </pre>
	 * 
	 * @param type
	 *            the type to check against
	 * @param obj
	 *            the object to check
	 * @throws ValidationFailedException
	 * @throws ValidationFailedException
	 *             if the object is not an instance of type
	 */
	public static void isInstanceOf(Class<?> type, Object obj) throws ValidationFailedException {
		isInstanceOf(type, obj, "");
	}

	/**
	 * Assert that {@code superType.isAssignableFrom(subType)} is {@code true}.
	 * 
	 * <pre class="code">
	 * Assert.isAssignable(Number.class, myClass, &quot;Number expected&quot;);
	 * </pre>
	 * 
	 * @param superType
	 *            the super type to check against
	 * @param subType
	 *            the sub type to check
	 * @param message
	 *            a message which will be prepended to provide further context.
	 *            If it is empty or ends in ":" or ";" or "," or ".", a full
	 *            exception message will be appended. If it ends in a space, the
	 *            name of the offending sub type will be appended. In any other
	 *            case, a ":" with a space and the name of the offending sub
	 *            type will be appended.
	 * @throws ValidationFailedException
	 * @throws ValidationFailedException
	 *             if the classes are not assignable
	 */
	public static void isAssignable(Class<?> superType, Class<?> subType, String message)
			throws ValidationFailedException {
		notNull(superType, "Super type to check against must not be null");
		if (subType == null || !superType.isAssignableFrom(subType)) {
			assignableCheckFailed(superType, subType, message);
		}
	}

	/**
	 * Assert that {@code superType.isAssignableFrom(subType)} is {@code true}.
	 * 
	 * <pre class="code">
	 * Assert.isAssignable(Number.class, myClass);
	 * </pre>
	 * 
	 * @param superType
	 *            the super type to check
	 * @param subType
	 *            the sub type to check
	 * @throws ValidationFailedException
	 * @throws ValidationFailedException
	 *             if the classes are not assignable
	 */
	public static void isAssignable(Class<?> superType, Class<?> subType) throws ValidationFailedException {
		isAssignable(superType, subType, "");
	}

	private static void instanceCheckFailed(Class<?> type, Object obj, String msg) throws ValidationFailedException {
		String className = (obj != null ? obj.getClass().getName() : "null");
		String result = "";
		boolean defaultMessage = true;
		if (StringUtils.isNotEmpty(msg)) {
			if (endsWithSeparator(msg)) {
				result = msg + " ";
			} else {
				result = messageWithTypeName(msg, className);
				defaultMessage = false;
			}
		}
		if (defaultMessage) {
			result = result + ("Object of class [" + className + "] must be an instance of " + type);
		}
		throw new ValidationFailedException(result);
	}

	private static void assignableCheckFailed(Class<?> superType, Class<?> subType, String msg)
			throws ValidationFailedException {
		String result = "";
		boolean defaultMessage = true;

		if (StringUtils.isNotEmpty(msg)) {
			if (endsWithSeparator(msg)) {
				result = msg + " ";
			} else {
				result = messageWithTypeName(msg, subType);
				defaultMessage = false;
			}
		}
		if (defaultMessage) {
			result = result + (subType + " is not assignable to " + superType);
		}
		throw new ValidationFailedException(result);
	}

	private static boolean endsWithSeparator(String msg) {
		return (msg.endsWith(":") || msg.endsWith(";") || msg.endsWith(",") || msg.endsWith("."));
	}

	private static String messageWithTypeName(String msg, Object typeName) {
		return msg + (msg.endsWith(" ") ? "" : ": ") + typeName;
	}

	public static void validateDayMonth(String dateString, String format, String message)
			throws ValidationFailedException {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			sdf.parse(dateString);
		} catch (Exception exc) {
			throw new ValidationFailedException(message + " " + exc.getMessage());
		}
	}

	/**
	 * Assert that an objects are not {@code null} and that they're equal.
	 * 
	 * @param object1
	 *            the object to check
	 * @param object2
	 *            the object to check
	 * @param message
	 *            the exception message to use if the assertion fails
	 * @throws ValidationFailedException
	 *             if the object is {@code null} and not equal to the other
	 */
	public static void equalObjects(Object object1, Object object2, String message) throws ValidationFailedException {
		if (object1 == null || object2 == null) {
			throw new ValidationFailedException(message);
		} else {
			if (!object1.equals(object2)) {
				throw new ValidationFailedException(message);
			}
		}
	}

	public static void isFalse(Boolean expression, String message, Object... params) throws ValidationFailedException {

		isTrue(!expression, message, params);

	}

}
