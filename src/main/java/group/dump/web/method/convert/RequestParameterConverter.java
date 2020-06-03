package group.dump.web.method.convert;

import group.dump.exception.DumpException;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author yuanguangxin
 */
public class RequestParameterConverter {

    @SuppressWarnings("unchecked")
    public <T> T convert(Class<T> requiredType, Object convertedValue) {
        if (Object.class == requiredType || String.class == requiredType) {
            return (T) convertedValue.toString();
        }
        if (Boolean.class == requiredType) {
            return (T) convertToBoolean(convertedValue.toString());
        }
        if (Character.class == requiredType) {
            return (T) convertToCharacter(convertedValue.toString());
        }
        if (Number.class.isAssignableFrom(requiredType)) {
            convertedValue = convertToNumber(convertedValue.toString(), (Class<Number>) requiredType);
            return (T) convertedValue;
        }
        throw new DumpException("Type for RequestParam '" + requiredType.getName() + "' is not supported");
    }

    private Boolean convertToBoolean(String source) {
        String value = source.trim();
        if ("".equals(value)) {
            return null;
        }
        value = value.toLowerCase();
        if ("true".equals(value) || "1".equals(value)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private Character convertToCharacter(String source) {
        if (source.length() == 0) {
            return null;
        }
        if (source.length() > 1) {
            throw new IllegalArgumentException(
                    "Can only convert a [String] with length of 1 to a [Character]; string value '" + source + "'  has length of " + source.length());
        }
        return source.charAt(0);
    }

    @SuppressWarnings("unchecked")
    public <T extends Number> T convertToNumber(String text, Class<T> targetClass) {
        String trimmed = text.trim();

        if (Byte.class == targetClass) {
            return (T) (isHexNumber(trimmed) ? Byte.decode(trimmed) : Byte.valueOf(trimmed));
        }
        if (Short.class == targetClass) {
            return (T) (isHexNumber(trimmed) ? Short.decode(trimmed) : Short.valueOf(trimmed));
        }
        if (Integer.class == targetClass) {
            return (T) (isHexNumber(trimmed) ? Integer.decode(trimmed) : Integer.valueOf(trimmed));
        }
        if (Long.class == targetClass) {
            return (T) (isHexNumber(trimmed) ? Long.decode(trimmed) : Long.valueOf(trimmed));
        }
        if (BigInteger.class == targetClass) {
            return (T) (isHexNumber(trimmed) ? decodeBigInteger(trimmed) : new BigInteger(trimmed));
        }
        if (Float.class == targetClass) {
            return (T) Float.valueOf(trimmed);
        }
        if (Double.class == targetClass) {
            return (T) Double.valueOf(trimmed);
        }
        if (BigDecimal.class == targetClass || Number.class == targetClass) {
            return (T) new BigDecimal(trimmed);
        }
        throw new IllegalArgumentException(
                "Cannot convert String [" + text + "] to target class [" + targetClass.getName() + "]");
    }

    private static boolean isHexNumber(String value) {
        int index = (value.startsWith("-") ? 1 : 0);
        return (value.startsWith("0x", index) || value.startsWith("0X", index) || value.startsWith("#", index));
    }

    private static BigInteger decodeBigInteger(String value) {
        int radix = 10;
        int index = 0;
        boolean negative = false;

        if (value.startsWith("-")) {
            negative = true;
            index++;
        }

        if (value.startsWith("0x", index) || value.startsWith("0X", index)) {
            index += 2;
            radix = 16;
        } else if (value.startsWith("#", index)) {
            index++;
            radix = 16;
        } else if (value.startsWith("0", index) && value.length() > 1 + index) {
            index++;
            radix = 8;
        }

        BigInteger result = new BigInteger(value.substring(index), radix);
        return (negative ? result.negate() : result);
    }
}
