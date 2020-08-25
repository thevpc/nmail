/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail.util;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class SerializedForm implements Serializable {

//    private String type;
//    private String value;
    private ExprList args;

//    public SerializedForm(String type, String value) {
//        this.type = type;
//        this.value = value;
//    }
    public SerializedForm(ExprList args) {
        this.args = args;
    }

    public ExprList getArgs() {
        return args;
    }

    public String getType() {
        for (ExprList.Expr arg : args.toList()) {
            ExprList.KeyValExpr t = arg.toKeyValExpr();
            if (t != null && t.getKey().toWordExpr() != null && "type".equals(t.getKey().toWordExpr().getValue())) {
                if (t.getValue().toStringExpr() != null) {
                    return t.getValue().toStringExpr().getValue();
                } else if (t.getValue().toWordExpr() != null) {
                    return t.getValue().toWordExpr().getValue();
                }
            }
        }
        return null;
    }

    public String getValue() {
        ExprList.Expr t = args.getValue("value");
        if (t != null) {
            if (t.toStringExpr() != null) {
                return t.toStringExpr().getValue();
            } else if (t.toWordExpr() != null) {
                return t.toWordExpr().getValue();
            }
        }
        for (ExprList.Expr arg : args.toList()) {
            ExprList.StringExpr e = arg.toStringExpr();
            if (e != null) {
                return e.getValue();
            }
        }
        return null;
    }

    public <T> T instantiate(SerializedFormConfig config, Class<T> expectedType) {
        if (expectedType.equals(String.class)) {
            return (T) this.getValue();
        }
        if (expectedType.equals(Integer.class)) {
            return (T) (Object) Integer.parseInt(this.getValue());
        }
        if (expectedType.equals(Double.class)) {
            return (T) (Object) Double.parseDouble(this.getValue());
        }

        Constructor<?> constructor = null;
        Class<?> instType = findType(config, this.getType());
        if (instType != null) {
            try {
                constructor = instType.getConstructor(SerializedForm.class);
            } catch (Exception ex) {
                //Logger.getLogger(IOUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (constructor != null) {
                try {
                    return (T) constructor.newInstance(this);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
            Method valueOf = null;
            try {
                valueOf = instType.getDeclaredMethod("valueOf", SerializedForm.class);
                if (!Modifier.isStatic(valueOf.getModifiers())) {
                    Logger.getLogger(SerializedForm.class.getName()).log(Level.SEVERE, "{0}.valueOf(SerializedForm) is not static, ignored!", instType.getName());
                    valueOf = null;
                }
            } catch (Exception ex) {
                //Logger.getLogger(IOUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (valueOf != null) {
                try {
                    return (T) valueOf.invoke(null, this);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new IllegalArgumentException("Unable to create instance " + instType);
        }

        throw new IllegalArgumentException("Unable to create instance " + this.getType());
    }

    public Class findType(SerializedFormConfig config, String name) {
        Class c = findType0(name);
        if (c != null) {
            return c;
        }
        int x = 100;
        while (true) {
            String alias = config.getAlias(name);
            if (alias != null) {
                name = alias;
            } else {
                break;
            }
            x--;
            if (x <= 0) {
                throw new IllegalArgumentException("Endless loop detected");
            }
        }
        List<String> allImports = new ArrayList<String>();
        allImports.add(null);
        for (String a : config.getImports()) {
            if (!allImports.contains(a)) {
                allImports.add(a);
            }
        }

        for (String a : new String[]{"java.lang"}) {
            if (!allImports.contains(a)) {
                allImports.add(a);
            }
        }
        for (String a : allImports) {
            if (a == null || a.equals("")) {
                a = name;
            } else {
                a = a + "." + name;
            }
            Class t = findType0(a);
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    public Class findType0(String name) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            return Class.forName(name, true, loader);
        } catch (Exception ex) {
            return null;
        }
    }

}
