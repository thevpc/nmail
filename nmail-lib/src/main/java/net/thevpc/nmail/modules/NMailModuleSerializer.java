package net.thevpc.nmail.modules;

import net.thevpc.nmail.NMail;
import net.thevpc.nmail.NMailFormat;
import net.thevpc.nmail.NMailMessage;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;

import java.io.*;

public class NMailModuleSerializer {

    public static final NMailFormat DEFAULT_FORMAT = NMailFormat.TSON;

    public NMail read(Reader reader) {
        return null;
    }

    public NMailFormat detectFormat(BufferedReader br) {
        //BufferedReader br=new BufferedReader(reader);
        try {
            br.mark(4096);
            char[] buffer = new char[1024];
            int i = br.read(buffer);
            String s = new String(buffer, 0, i);
            s = s.trim();
            if (s.startsWith(NMailModuleSerializerAsText.SER_HEADER)) {
                br.reset();
                return NMailFormat.TEXT;
            }
            return NMailFormat.TSON;
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    public NMail read(NPath file) {
        NMailFormat f=null;
        if(!file.isRegularFile()){
            if(file.resolveSibling(file.getName()+".nmail.tson").isRegularFile()) {
                file = file.resolveSibling(file.getName() + ".nmail.tson");
                f= NMailFormat.TSON;
            }else if(file.resolveSibling(file.getName()+".tson").isRegularFile()) {
                file =file.resolveSibling(file.getName()+".tson");
                f= NMailFormat.TSON;
            }else if(file.resolveSibling(file.getName()+".nmail").isRegularFile()) {
                file =file.resolveSibling(file.getName()+".nmail");
            }
        }
        try(BufferedReader r=file.getBufferedReader()){
            if(f==null){
                f = detectFormat(r);
            }
            if(f== NMailFormat.TEXT){
                return new NMailModuleSerializerAsText().read(r);
            }
            if(f== DEFAULT_FORMAT){
                return new NMailModuleSerializerAsTson().read(r);
            }
        } catch (IOException e) {
            throw new NIOException(e);
        }
        throw new IllegalArgumentException("Unsupported file " + file + ". accepted extensions are *.nmail, *.nmail.tson, *.tson");
    }

    public String toString(NMail gm, NMailFormat format) {
        if(format==null){
            format=DEFAULT_FORMAT;
        }
        if(format== NMailFormat.TEXT){
            return new NMailModuleSerializerAsText().nToString(gm);
        }
        return new NMailModuleSerializerAsTson().nmailToString(gm);
    }

    public String toString(NMailMessage gm, NMailFormat format) {
        if(format==null){
            format=DEFAULT_FORMAT;
        }
        if(format== NMailFormat.TEXT){
            return new NMailModuleSerializerAsText().nToString(gm);
        }
        return new NMailModuleSerializerAsTson().nmailToString(gm);
    }

    public String toString(NMail gm) {
        return toString(gm, DEFAULT_FORMAT);
    }

    public String toString(NMailMessage nMailMessage) {
        return toString(nMailMessage, DEFAULT_FORMAT);
    }
}
