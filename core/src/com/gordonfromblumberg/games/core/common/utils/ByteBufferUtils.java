package com.gordonfromblumberg.games.core.common.utils;

import java.nio.ByteBuffer;

public class ByteBufferUtils {
    private ByteBufferUtils() {}

    enum Type {
        BOOL, INT, LONG, FLOAT, DOUBLE, STRING;
        static Type[] TYPES = values();
    }

    public static void putString(String string, ByteBuffer bb) {
        bb.putInt(string.length());
        for (char ch : string.toCharArray()) {
            bb.putChar(ch);
        }
    }

    public static String getString(ByteBuffer bb) {
        int length = bb.getInt();
        StringBuilder sb = new StringBuilder(length);
        while (length-- > 0) {
            sb.append(bb.getChar());
        }
        return sb.toString();
    }

    public static void putObject(Object object, ByteBuffer bb) {
        if (object instanceof Boolean) {
            bb.putInt(Type.BOOL.ordinal());
            bb.put((byte) (((boolean) object) ? 1 : 0));
        } else if (object instanceof Integer) {
            bb.putInt(Type.INT.ordinal());
            bb.putInt((int) object);
        } else if (object instanceof Long) {
            bb.putInt(Type.LONG.ordinal());
            bb.putLong((long) object);
        } else if (object instanceof Float) {
            bb.putInt(Type.FLOAT.ordinal());
            bb.putFloat((float) object);
        } else if (object instanceof Double) {
            bb.putInt(Type.DOUBLE.ordinal());
            bb.putDouble((double) object);
        } else if (object instanceof String) {
            bb.putInt(Type.STRING.ordinal());
            putString((String) object, bb);
        } else {
            throw new IllegalArgumentException("Unexpected object " + object);
        }
    }

    public static Object getObject(ByteBuffer bb) {
        Type type = Type.TYPES[bb.getInt()];
        switch (type) {
            case BOOL: return bb.get() > 0;
            case INT: return bb.getInt();
            case LONG: return bb.getLong();
            case FLOAT: return bb.getFloat();
            case DOUBLE: return bb.getDouble();
            case STRING: return getString(bb);
            default: throw new IllegalStateException("Couldn't get object from byte buffer");
        }
    }
}
