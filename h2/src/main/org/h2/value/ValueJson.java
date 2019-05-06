package org.h2.value;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.h2.api.ErrorCode;
import org.h2.message.DbException;
import org.h2.util.StringUtils;
import org.h2.util.json.JSONArray;
import org.h2.util.json.JSONObject;
import org.h2.util.json.JSONPath;
import org.h2.util.json.JSONStringSource;
import org.h2.util.json.JSONValue;
import org.h2.util.json.JSONValueTarget;

public class ValueJson extends Value {

    /**
     * {@code null} JSON value.
     */
    public static final ValueJson NULL = new ValueJson("null");

    /**
     * {@code true} JSON value.
     */
    public static final ValueJson TRUE = new ValueJson("true");

    /**
     * {@code false} JSON value.
     */
    public static final ValueJson FALSE = new ValueJson("false");

    /**
     * {@code 0} JSON value.
     */
    public static final ValueJson ZERO = new ValueJson("0");

    private final String value;
    private final JSONValue parsed;

    private ValueJson(String value) {
        this.value = value;
        JSONValueTarget target = new JSONValueTarget();
        JSONStringSource.parse(this.value, target);
        this.parsed = target.getResult();
    }

    private ValueJson(JSONValue value) {
        this.parsed = value;
        this.value = value.toString();
    }

    @Override
    public String getSQL() {
        return StringUtils.quoteStringSQL(value).concat("::JSON");
    }

    @Override
    public int getType() {
        return Value.JSON;
    }

    @Override
    public long getPrecision() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getDisplaySize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getString() {
        return value;
    }

    @Override
    public Object getObject() {
        return value;
    }

    @Override
    public void set(PreparedStatement prep, int parameterIndex) throws SQLException {
        prep.setString(parameterIndex, value);
    }

    @Override
    protected int compareSecure(Value v, CompareMode mode) {
        String other = ((ValueJson) v).value;
        return mode.compareString(value, other, false);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ValueJson && value.equals(((ValueJson) other).value);
    }
    
    public static ValueJson get(JSONValue v) {
        return new ValueJson(v);
    }

    public static ValueJson get(String s) {
        try {
            s = JSONStringSource.normalize(s);
        } catch (RuntimeException ex) {
            throw DbException.get(ErrorCode.DATA_CONVERSION_ERROR_1, s);
        }
        return getInternal(s);
    }

    public static ValueJson get(byte[] bytes) {
        String s;
        try {
            s = JSONStringSource.normalize(bytes);
        } catch (RuntimeException ex) {
            throw DbException.get(ErrorCode.DATA_CONVERSION_ERROR_1, StringUtils.convertBytesToHex(bytes));
        }
        return getInternal(s);
    }

    public static ValueJson get(boolean bool) {
        return bool ? TRUE : FALSE;
    }

    public static ValueJson get(int number) {
        return getInternal(Integer.toString(number));
    }

    public static ValueJson get(long number) {
        return getInternal(Long.toString(number));
    }

    public static ValueJson get(BigDecimal number) {
        return getInternal(number.toString());
    }

    private static ValueJson getInternal(String s) {
        int l = s.length();
        switch (l) {
        case 1:
            if ("0".equals(s)) {
                return ZERO;
            }
            break;
        case 4:
            if ("true".equals(s)) {
                return TRUE;
            } else if ("null".equals(s)) {
                return NULL;
            }
            break;
        case 5:
            if ("false".equals(s)) {
                return FALSE;
            }
        }
        return new ValueJson(s);
    }

    @Override
    public Value subtract(Value v) {
        if (parsed.getType() == JSONValue.OBJECT) {
            JSONObject o = (JSONObject) parsed;
            return ValueJson.get(o);
        }
        return this;
    }

    public boolean hasKey(String key) {
        return parsed.getType() == JSONValue.OBJECT && ((JSONObject) parsed).getFirst(key) != null;
    }

    public Value extract(String path) {
        JSONValue value = new JSONPath(this.parsed, path).extract();
        return ValueString.get(value.toString());
    }

    public Value query(String path) {
        JSONValue value = new JSONPath(this.parsed, path).extract();
        return new ValueJson(value.toString());
    }

    public Value exists(String path) {
        return ValueBoolean.get(new JSONPath(this.parsed, path).exists());
    }

    public Value getField(String key) {
        if (parsed.getType() == JSONValue.OBJECT) {
            JSONObject obj = (JSONObject) parsed;
            JSONValue res = obj.getFirst(key);
            return res == null ? ValueNull.INSTANCE : ValueJson.get(res);
        } else if (parsed.getType() == JSONValue.ARRAY && StringUtils.isNumber(key)) {
            JSONArray arr = (JSONArray) parsed;
            JSONValue res = arr.getElement(Integer.parseInt(key));
            return res == null ? ValueNull.INSTANCE : ValueJson.get(res);
        } else {
            return ValueNull.INSTANCE;
        }
    }

    public Value getFieldPath(ValueArray path) {
        ValueJson tmp = this;
        for (Value v : path.getList()) {
            Value next = tmp.getField(v.getString());
            if (next.getType() == Value.NULL) {
                return next;
            } else {
                tmp = (ValueJson) next;
            }
        }
        return tmp;
    }

    public ValueBoolean contains(ValueJson right) {
        JSONValue vl = parsed;
        JSONValue vr = right.parsed;

        if (vl.getType() == JSONValue.OBJECT && vr.getType() == JSONValue.OBJECT) {
            return ValueBoolean.get(((JSONObject) vl).contains((JSONObject) vr));
        }
        if (vl.getType() == JSONValue.ARRAY && vr.getType() == JSONValue.ARRAY) {
            return ValueBoolean.get(((JSONArray) vl).contains((JSONArray) vr));
        }
        return ValueBoolean.FALSE;
    }

}
