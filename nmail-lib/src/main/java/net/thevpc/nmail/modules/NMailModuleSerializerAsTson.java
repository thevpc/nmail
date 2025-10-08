/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail.modules;

import net.thevpc.nmail.*;
import net.thevpc.nmail.datasource.factories.ServiceNMailDataSourceFactory;
import net.thevpc.nmail.expr.*;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NNameFormat;

import java.io.*;
import java.util.*;

/**
 * @author taha.bensalah@gmail.com
 */
public class NMailModuleSerializerAsTson {

    public String nmailToString(NMail mail) {
        try {
            ByteArrayOutputStream s = new ByteArrayOutputStream();
            writeText(mail, s);
            return new String(s.toByteArray());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String nmailToString(NMailMessage mail) {
        try {
            ByteArrayOutputStream s = new ByteArrayOutputStream();
            writeText(mail, s);
            return new String(s.toByteArray());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public NMail read(Reader stream) {
        NElement elem = NElementParser.ofTson().parse(stream);
        if (elem.isAnyArray() || elem.isAnyUplet()) {
            elem = elem.toObject().get();
        } else if (elem.isAnyObject()) {
            //
        } else {
            elem = elem.wrapIntoObject();
        }
        NObjectElement eo = elem.asObject().get();
        NMail m = new NMail();
        for (NElement sub : eo) {
            if (sub.isNamedPair()) {
                NPairElement p = sub.asPair().get();
                String name = p.name().get();
                NElement value = p.value();
                putProperty(m, name, value);
            } else {
                throw new NIllegalArgumentException(NMsg.ofC("unsupported %s", sub));
            }
        }
        return m;
    }

    public NMail read(NPath stream) {
        NElement elem = NElementParser.ofTson().parse(stream);
        if (elem.isAnyArray() || elem.isAnyUplet()) {
            elem = elem.toObject().get();
        } else if (elem.isAnyObject()) {
            //
        } else {
            elem = elem.wrapIntoObject();
        }
        NObjectElement eo = elem.asObject().get();
        NMail m = new NMail();
        for (NElement sub : eo) {
            if (sub.isNamedPair()) {
                NPairElement p = sub.asPair().get();
                String name = p.name().get();
                NElement value = p.value();
                putProperty(m, name, value);
            } else {
                throw new NIllegalArgumentException(NMsg.ofC("unsupported %s", sub));
            }
        }
        return m;
    }

    private void putProperty(NMail m, String key, NElement value) {
        switch (NNameFormat.LOWER_KEBAB_CASE.format(key)) {
            case "from": {
                m.from(value.asStringValue().get());
                break;
            }
            case "to": {
                m.to(value.asStringValue().get());
                break;
            }
            case "provider": {
                m.provider(value.asStringValue().get());
                break;
            }
            case "tracker": {
                m.tracker(new NTrackerFile(value.asStringValue().get()));
                break;
            }
            case "toeach": {
                m.toeach(value.asStringValue().get());
                break;
            }
            case "cc": {
                m.cc(value.asStringValue().get());
                break;
            }
            case "bcc": {
                m.bcc(value.asStringValue().get());
                break;
            }
            case "subject": {
                m.subject(value.asStringValue().get());
                break;
            }
            case "user":
            case "username":
            case "user-name": {
                m.getProperties().put("app.mail.user", value.asStringValue().get());
                break;
            }
            case "password": {
                m.getProperties().put("app.mail.password", value.asStringValue().get());
                break;
            }
            case "repeat": {
                Expr baseExpr = new ExprParser(value.asStringValue().get()).parseStatementList();
                m.repeatDatasource(ServiceNMailDataSourceFactory.getInstance().create(baseExpr));
                break;
            }
            case "ask-password": {
                m.getProperties().put("app.mail.ask-password", asBoolean(key, value));
                break;
            }
            case "dry": {
                m.setDry(NLiteral.of(asBoolean(key, value)).asBoolean().orNull());
                break;
            }
            case "datasource":
            case "data-source": {
                int added = 0;
                if (value.isAnyString()) {
                    addDs(null, new StringExpr(key), m);
                    added++;
                } else if (value.isListContainer()) {
                    for (NElement child : value.asListContainer().get().children()) {
                        if (child.isNamedPair()) {
                            NPairElement up = child.asPair().get();
                            String name = up.name().get();
                            NElement value2 = up.value();
                            addDs(name, new StringExpr(value2.asStringValue().get()), m);
                            added++;
                        }
                    }
                }
                if (added == 0) {
                    throw new IllegalArgumentException("missing datasource args");
                }
                break;
            }
            case "object":
            case "attachment":
            case "footer":
            case "header": {
                NMailBodyPosition pos = NMailBodyPosition.valueOf(NNameFormat.LOWER_KEBAB_CASE.format(key).toUpperCase());
                addBody(m, pos, value);
                break;
            }
            default: {
                if (key.startsWith("property.")) {
                    m.getProperties().put(key.substring("property.".length()), value.asStringValue().get());
                } else {
                    throw new IllegalArgumentException("Unexpected property " + key + " : " + value);
                }
            }
        }
    }

    private void addBody(NMail m, NMailBodyPosition pos, NElement value) {
        boolean expandable = true;
        String contentType = NMail.HTML_CONTENT_TYPE;
        String charSet = "";
        String contentString = null;
        int order = 0;
        boolean base64 = false;
        boolean stringAsPath = false;

        if (value.isAnyString()) {
            for (NElementAnnotation annotation : value.annotations()) {
                switch (NNameFormat.LOWER_KEBAB_CASE.format(annotation.name())) {
                    case "html": {
                        contentType = NMail.HTML_CONTENT_TYPE;
                        break;
                    }
                    case "text": {
                        contentType = NMail.TEXT_CONTENT_TYPE;
                        break;
                    }
                    case "utf8": {
                        charSet = "UTF-8";
                        break;
                    }
                    case "bytes":
                    case "binary":
                    case "base64": {
                        contentType = NMail.BYTES_CONTENT_TYPE;
                        break;
                    }
                    case "order": {
                        order = annotation.param(0).asIntValue().get();
                        break;
                    }
                    case "charset": {
                        charSet = annotation.param(0).asStringValue().get();
                    }
                    case "content-type": {
                        contentType = annotation.param(0).asStringValue().get();
                        break;
                    }
                    case "expandable": {
                        expandable = true;
                        break;
                    }
                    case "non-expandable":
                    case "not-expandable": {
                        expandable = false;
                        break;
                    }
                    case "path": {
                        stringAsPath = true;
                        break;
                    }
                    default: {
                        throw new NIllegalArgumentException(NMsg.ofC("expected html,text,utf8,bytes,binary,base64,expandable,non-expandable, found %s", annotation.name()));
                    }
                }
            }
            contentString = value.asStringValue().get();
        } else if (value.isNamedUplet()) {
            NUpletElement u = value.asUplet().get();
            switch (NNameFormat.LOWER_KEBAB_CASE.format(u.name().get())) {
                case "content": {
                    stringAsPath = false;
                    break;
                }
                case "path": {
                    stringAsPath = true;
                    break;
                }
                default: {
                    throw new NIllegalArgumentException(NMsg.ofC("expected content,path, found %s", u.name().get()));
                }
            }
            for (NElement pp : u.params()) {
                if (pp.isName()) {
                    switch (NNameFormat.LOWER_KEBAB_CASE.format(pp.asStringValue().get())) {
                        case "html": {
                            contentType = NMail.HTML_CONTENT_TYPE;
                            break;
                        }
                        case "text": {
                            contentType = NMail.TEXT_CONTENT_TYPE;
                            break;
                        }
                        case "utf8": {
                            charSet = "UTF-8";
                            break;
                        }
                        case "bytes":
                        case "binary":
                        case "base64": {
                            contentType = NMail.BYTES_CONTENT_TYPE;
                            break;
                        }
                        case "expandable": {
                            expandable = true;
                            break;
                        }
                        case "non-expandable":
                        case "not-expandable": {
                            expandable = false;
                            break;
                        }
                        default: {
                            throw new NIllegalArgumentException(NMsg.ofC("expected html,text,utf8,bytes,binary,base64,expandable,non-expandable, found %s", pp.asStringValue().get()));
                        }
                    }
                } else if (pp.isNamedPair()) {
                    NPairElement nu = pp.asPair().get();
                    switch (NNameFormat.LOWER_KEBAB_CASE.format(nu.key().asStringValue().get())) {
                        case "expandable": {
                            expandable = nu.value().asBooleanValue().get();
                            break;
                        }
                        case "order": {
                            order = nu.value().asIntValue().get();
                            break;
                        }
                        case "charset": {
                            charSet = nu.value().asStringValue().get();
                        }
                        case "content-type": {
                            contentType = nu.value().asStringValue().get();
                            break;
                        }
                        default: {
                            throw new NIllegalArgumentException(NMsg.ofC("expected expandable,order,charset,content-type, found %s", nu.key().asStringValue().get()));
                        }
                    }
                } else if (pp.isAnyString()) {
                    contentString = pp.asStringValue().get();
                } else {
                    throw new NIllegalArgumentException(NMsg.ofC("expected pair or name, found %s", pp.type().id()));
                }
            }
        } else if (value.isNamedObject() || value.isNamedParametrizedObject()) {
            NObjectElement u = value.asObject().get();
            switch (NNameFormat.LOWER_KEBAB_CASE.format(u.name().get())) {
                case "content": {
                    stringAsPath = false;
                    break;
                }
                case "path": {
                    stringAsPath = true;
                    break;
                }
                default: {
                    throw new NIllegalArgumentException(NMsg.ofC("expected content,path, found %s", u.name().get()));
                }
            }
            List<NElement> children = new ArrayList<>();
            if(u.params().isPresent()) {
                children.addAll(u.params().orNull());
            }
            children.addAll(u.children());
            for (NElement pp : children) {
                if (pp.isName()) {
                    switch (NNameFormat.LOWER_KEBAB_CASE.format(pp.asStringValue().get())) {
                        case "html": {
                            contentType = NMail.HTML_CONTENT_TYPE;
                            break;
                        }
                        case "text": {
                            contentType = NMail.TEXT_CONTENT_TYPE;
                            break;
                        }
                        case "utf8": {
                            charSet = "charset=UTF-8";
                            break;
                        }
                        case "bytes":
                        case "binary":
                        case "base64": {
                            contentType = NMail.BYTES_CONTENT_TYPE;
                            break;
                        }
                        case "expandable": {
                            expandable = true;
                            break;
                        }
                        case "non-expandable":
                        case "not-expandable": {
                            expandable = false;
                            break;
                        }
                        default: {
                            throw new NIllegalArgumentException(NMsg.ofC("expected html,text,utf8,bytes,binary,base64,expandable,non-expandable, found %s", pp.asStringValue().get()));
                        }
                    }
                } else if (pp.isNamedPair()) {
                    NPairElement nu = pp.asPair().get();
                    switch (NNameFormat.LOWER_KEBAB_CASE.format(nu.key().asStringValue().get())) {
                        case "expandable": {
                            expandable = nu.value().asBooleanValue().get();
                            break;
                        }
                        case "order": {
                            order = nu.value().asIntValue().get();
                            break;
                        }
                        case "charset": {
                            charSet = nu.value().asStringValue().get();
                        }
                        case "content-type": {
                            contentType = nu.value().asStringValue().get();
                            break;
                        }
                        default: {
                            throw new NIllegalArgumentException(NMsg.ofC("expected expandable,order,charset,content-type, found %s", nu.key().asStringValue().get()));
                        }
                    }
                } else if (pp.isAnyString()) {
                    contentString = pp.asStringValue().get();
                } else {
                    throw new NIllegalArgumentException(NMsg.ofC("expected pair or name, found %s", pp.type().id()));
                }
            }
        } else {
            throw new NIllegalArgumentException(NMsg.ofC("expected pair, found %s", value.type().id()));
        }
        if (contentString == null) {
            throw new NIllegalArgumentException(NMsg.ofC("missing content"));
        }
        Object bodyObject;
        if (stringAsPath) {
            bodyObject = NPath.of(contentString);
        } else {
            if(charSet.isEmpty()){
                charSet = "UTF-8";
            }
            if (base64) {
                bodyObject = Base64.getDecoder().decode(contentString);
            } else {
                bodyObject = contentString;
            }
        }

        String cc = contentType;
        if (!charSet.isEmpty()) {
            cc = cc + ";charset=" + charSet;
        }
        m.body(bodyObject, cc, pos);
        NMailBody last = m.body().get(m.body().size() - 1);
        last.setOrder(order);
        last.setExpandable(expandable);
        last.setPosition(pos);

    }

    private void addDs(String name, Expr expr, NMail m) {
        if (name == null) {
            name = "";
        }
        if (m.namedDataSources().containsKey(name)) {
            //this is a filtered ds
            throw new IllegalArgumentException("Datasource is already defined '" + name + "'");
        }
        NMailDataSource d = ServiceNMailDataSourceFactory.getInstance().create(expr);
        m.namedDataSources().put(name, d);
    }

    private static Object asBoolean(String key, NElement value) {
        Object val = null;
        if (value.isAnyString()) {
            val = value.asStringValue().get();
        } else if (value.isBoolean()) {
            val = value.asBooleanValue().get();
        } else {
            throw new IllegalArgumentException("expected boolean or boolean expression for " + key + ". got " + value);
        }
        return val;
    }

    private void writeText(NMail mail, OutputStream stream) throws IOException {
        NObjectElementBuilder b = NElement.ofObjectBuilder();
        if (mail.from() != null) {
            b.add("from", mail.from());
        }
        if (mail.to() != null && !mail.to().isEmpty()) {
            b.add("to", NElement.ofStringArray(mail.to().toArray(new String[0])));
        }
        if (mail.cc() != null && !mail.cc().isEmpty()) {
            b.add("cc", NElement.ofStringArray(mail.cc().toArray(new String[0])));
        }
        if (mail.bcc() != null && !mail.bcc().isEmpty()) {
            b.add("bcc", NElement.ofStringArray(mail.bcc().toArray(new String[0])));
        }

        if (mail.toeach() != null && !mail.toeach().isEmpty()) {
            b.add("toeach", NElement.ofStringArray(mail.toeach().toArray(new String[0])));
        }

        {
            Properties f = mail.getProperties();
            if (f != null && f.size() > 0) {
                for (Map.Entry v : f.entrySet()) {
                    b.add("property." + v.getKey(), NElement.ofString(v.getValue() == null ? null : v.getValue().toString()));
                }
            }
        }
        {
            NMailDataSource f = mail.repeatDataSource();
            if (f != null) {
                b.add("repeat", f.toExpr().toString());
            }
        }
        {
            if (!mail.namedDataSources().isEmpty()) {
                b.add(
                        "dataSource",
                        NElement.ofArray(mail.namedDataSources().entrySet().stream().map(x -> NElement.ofPair(x.getKey(), x.getValue().toExpr().toString())).toArray(NElement[]::new))
                );
            }
        }
        {
            b.add("dry", mail.isDry());
        }
        {
            if (mail.subject() != null) {
                b.add("subject", mail.subject());
            }
        }

        {
            NMailBodyList f = mail.body();
            if (f != null) {
                for (NMailBody bb : f) {
                    NObjectElementBuilder nob = NElement.ofObjectBuilder();
                    String contentType = bb.getContentType();
                    if (contentType == null) {
                        contentType = NMail.BYTES_CONTENT_TYPE;
                    }
                    if (NMail.HTML_CONTENT_TYPE.equals(contentType)) {
                        nob.name("html");
                    } else if (NMail.TEXT_CONTENT_TYPE.equals(contentType)) {
                        nob.name("text");
                    } else {
                        nob.name("base64");
                        if (contentType != null) {
                            nob.addParam("contentType", contentType);
                        }
                    }
                    boolean base64 = false;
                    if (contentType.startsWith("text/")) {
                        base64 = true;
                    }
                    if (bb.getOrder() != 0) {
                        nob.addParam("order", bb.getOrder());
                    }
                    if (bb.isExpandable()) {
                        nob.addParam(NElement.ofName("expandable"));
                    }

                    if (bb instanceof NMailBodyPath) {
                        nob.add("path", ((NMailBodyPath) bb).getPath());
                    } else {
                        NMailBodyContent c = (NMailBodyContent) bb;
                        if (base64) {
                            nob.addParam(NElement.ofName("base64"));
                            nob.add(NElement.ofString(Base64.getEncoder().encodeToString(c.getByteArray()), NElementType.TRIPLE_DOUBLE_QUOTED_STRING));
                        } else {
                            String str = new String(c.getByteArray());
                            if (!str.contains("\"\"\"")) {
                                nob.add(NElement.ofString(str, NElementType.TRIPLE_DOUBLE_QUOTED_STRING));
                            } else if (!str.contains("'''")) {
                                nob.add(NElement.ofString(str, NElementType.TRIPLE_SINGLE_QUOTED_STRING));
                            } else if (!str.contains("```")) {
                                nob.add(NElement.ofString(str, NElementType.TRIPLE_ANTI_QUOTED_STRING));
                            } else {
                                base64 = true;
                                nob.addParam(NElement.ofName("base64"));
                                nob.add(NElement.ofString(Base64.getEncoder().encodeToString(c.getByteArray()), NElementType.TRIPLE_DOUBLE_QUOTED_STRING));
                            }
                        }
                    }
                    b.add(bb.getPosition().name().toLowerCase(), nob.build());
                }
            }
        }

        NElementWriter.ofTson().write(b, stream);
    }

    private void writeText(NMailMessage mail, OutputStream stream) throws IOException {
        NObjectElementBuilder b = NElement.ofObjectBuilder();
        if (mail.from() != null) {
            b.add("from", mail.from());
        }
        if (mail.to() != null && !mail.to().isEmpty()) {
            b.add("to", NElement.ofStringArray(mail.to().toArray(new String[0])));
        }
        if (mail.cc() != null && !mail.cc().isEmpty()) {
            b.add("cc", NElement.ofStringArray(mail.cc().toArray(new String[0])));
        }
        if (mail.bcc() != null && !mail.bcc().isEmpty()) {
            b.add("bcc", NElement.ofStringArray(mail.bcc().toArray(new String[0])));
        }

        {
            Properties f = mail.getProperties();
            if (f != null && f.size() > 0) {
                for (Map.Entry v : f.entrySet()) {
                    b.add("property." + v.getKey(), NElement.ofString(v.getValue() == null ? null : v.getValue().toString()));
                }
            }
        }

        {
            b.add("dry", mail.isDry());
        }
        {
            if (mail.subject() != null) {
                b.add("subject", mail.subject());
            }
        }

        {
            NMailBodyList f = mail.body();
            if (f != null) {
                for (NMailBody bb : f) {
                    NObjectElementBuilder nob = NElement.ofObjectBuilder();
                    String contentType = bb.getContentType();
                    if (contentType == null) {
                        contentType = NMail.BYTES_CONTENT_TYPE;
                    }
                    if (NMail.HTML_CONTENT_TYPE.equals(contentType)) {
                        nob.name("html");
                    } else if (NMail.TEXT_CONTENT_TYPE.equals(contentType)) {
                        nob.name("text");
                    } else {
                        nob.name("base64");
                        if (contentType != null) {
                            nob.addParam("contentType", contentType);
                        }
                    }
                    boolean base64 = false;
                    if (contentType.startsWith("text/")) {
                        base64 = true;
                    }
                    if (bb.getOrder() != 0) {
                        nob.addParam("order", bb.getOrder());
                    }
                    if (bb.isExpandable()) {
                        nob.addParam(NElement.ofName("expandable"));
                    }

                    if (bb instanceof NMailBodyPath) {
                        nob.add("path", ((NMailBodyPath) bb).getPath());
                    } else {
                        NMailBodyContent c = (NMailBodyContent) bb;
                        if (base64) {
                            nob.addParam(NElement.ofName("base64"));
                            nob.add(NElement.ofString(Base64.getEncoder().encodeToString(c.getByteArray()), NElementType.TRIPLE_DOUBLE_QUOTED_STRING));
                        } else {
                            String str = new String(c.getByteArray());
                            if (!str.contains("\"\"\"")) {
                                nob.add(NElement.ofString(str, NElementType.TRIPLE_DOUBLE_QUOTED_STRING));
                            } else if (!str.contains("'''")) {
                                nob.add(NElement.ofString(str, NElementType.TRIPLE_SINGLE_QUOTED_STRING));
                            } else if (!str.contains("```")) {
                                nob.add(NElement.ofString(str, NElementType.TRIPLE_ANTI_QUOTED_STRING));
                            } else {
                                base64 = true;
                                nob.addParam(NElement.ofName("base64"));
                                nob.add(NElement.ofString(Base64.getEncoder().encodeToString(c.getByteArray()), NElementType.TRIPLE_DOUBLE_QUOTED_STRING));
                            }
                        }
                    }
                    b.add(bb.getPosition().name().toLowerCase(), nob.build());
                }
            }
        }

        NElementWriter.ofTson().write(b, stream);
    }

    private String lineEscape(String v) {
        StringBuilder sb = new StringBuilder();
        for (char c : v.toCharArray()) {
            switch (c) {
                case '\\': {
                    sb.append('\\');
                    sb.append(c);
                    break;
                }
                case '\n': {
                    sb.append('\\');
                    sb.append('n');
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }


}
