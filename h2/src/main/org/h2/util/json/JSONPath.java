package org.h2.util.json;

public class JSONPath {
    
    private JSONValue value;
    private String path;
    private int index;
    
    private static final int GET_ARR = 1;
    private static final int GET_OBJ = 2;
    
    public JSONPath(JSONValue value, String path) {
        this.value = value;
        this.path = path;
        this.index = 0;
    }
    
    public boolean exists() {
        int length = path.length();
        begin();
        while (index < length) {
            int op = readOp();
            switch(op) {
            case GET_ARR: {
                if (value.getType() != JSONValue.ARRAY) {
                    return false;
                } else {
                    int idx = readIdx(length);
                    endIdx();
                    value = ((JSONArray) value).getElement(idx);
                }
                break;
            }
            case GET_OBJ: {
                if (value.getType() != JSONValue.OBJECT) {
                    return false;
                } else {
                    String key = readKey(length);
                    value = ((JSONObject) value).getFirst(key);
                }
                break;
            }
            default:
                if (index == length - 1)
                    return true;
                else 
                    throw new IllegalArgumentException();
            }
        }
        
        return value == null ? false : true ;
    }
    
    public JSONValue extract() {
        int length = path.length();
        begin();
        while (index < length) {
            int op = readOp();
            switch(op) {
            case GET_ARR: {
                if (value.getType() != JSONValue.ARRAY) {
                    return JSONNull.NULL;
                } else {
                    int idx = readIdx(length);
                    endIdx();
                    value = ((JSONArray) value).getElement(idx);
                }
                break;
            }
            case GET_OBJ: {
                if (value.getType() != JSONValue.OBJECT) {
                    return JSONNull.NULL;
                } else {
                    String key = readKey(length);
                    value = ((JSONObject) value).getFirst(key);
                }
                break;
            }
            default:
                if (index == length - 1)
                    return value == null ? JSONNull.NULL : value;
                else 
                    throw new IllegalArgumentException();
            }
        }
        
        return value == null ? JSONNull.NULL : value;
    }
    private void begin() {
        if (path.charAt(index++) != '$') {
            throw new IllegalArgumentException();
        }
    }
    
    private int readOp() {
        char c = path.charAt(index++);
        switch(c){
        case '[':
            return GET_ARR;
        case '.':
            return GET_OBJ;
        default:
            throw new IllegalArgumentException();
        }
    }
    
    private int readIdx(int length) {
        int start = index;
        index = skipInt(length);
//        this.index =  
        return Integer.parseInt(path.substring(start, index));
    }
    
    private int skipInt(int length) {
        while (index < length) {
            char ch = path.charAt(index);
            if (ch >= '0' && ch <= '9') {
                index++;
            } else {
                break;
            }
        }
        return index;
    }

    private String readKey(int length) {
        StringBuilder builder = new StringBuilder();
        for(char ch = path.charAt(index); index < length;) {
            if (ch == '.' || ch == '[') {
                return builder.toString();
            } else if (index == length - 1) {
                builder.append(ch);
                index++;
                return builder.toString();
            }
            builder.append(ch);
            index++;
            ch = path.charAt(index);
        }
        throw new IllegalArgumentException();
    }
    


    private void endIdx() {
        char ch = path.charAt(index++);
        if(ch != ']') {
            throw new IllegalArgumentException();
        }
    }


}
