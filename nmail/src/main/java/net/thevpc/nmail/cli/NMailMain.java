package net.thevpc.nmail.cli;

import java.io.File;
import java.util.*;

import net.thevpc.nmail.NMailListener;
import net.thevpc.nmail.NMailMessage;
import net.thevpc.nuts.*;

import net.thevpc.nmail.NMail;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineRunner;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

@NApp.Info
public class NMailMain {

    LinkedHashSet<String> files = new LinkedHashSet<>();
    String db;

    public static void main(String[] args) {
        NApp.builder(args).run();
    }

    @NApp.Runner
    public void run() {
        NApp.of().runCmdLine(new NCmdLineRunner() {
            @Override
            public boolean next(NArg arg, NCmdLine cmdLine) {
                if(arg.isOption()){
                    switch (arg.getStringKey().get()) {
                        case "-d":
                        case "--db": {
                            arg = cmdLine.nextEntry().get();
                            if (arg.isUncommented()) {
                                db = arg.getStringValue().get();
                            }
                            return true;
                        }
                    }
                    //no options for now
                    return false;
                }else{
                    files.add(cmdLine.next().get().image());
                    return true;
                }
            }

            @Override
            public void validate(NCmdLine cmdLine) {
                if (files.isEmpty()) {
                    cmdLine.throwMissingArgument("messageId");
                }
            }

            @Override
            public void run(NCmdLine cmdLine) {
                for (String f : files) {
                    List<NPath> paths = getValidFilePaths(NPath.of(f), ".nmail",
                            NBlankable.isBlank(db) ? NApp.of().getConfFolder().toString() : db
                    );
                    if (paths.isEmpty()) {
                        cmdLine.throwError(NMsg.ofC("invalid messageId %s", f));
                    }
                    for (NPath path : paths) {
                        NMail go = NMail.load(path.toFile().get());
                        if (!go.isDry()) {
                            go.setDry(NSession.of().isDry());
                        }
                        int[] sendCount = new int[1];
                        go.send(new NMailListener() {
                            @Override
                            public void onBeforeSend(NMailMessage mail) {

                            }

                            @Override
                            public void onAfterSend(NMailMessage mail) {
                                sendCount[0]++;
                            }

                            @Override
                            public void onSendError(NMailMessage mail, Throwable exc) {
                                exc.printStackTrace();
                            }
                        });
                        System.out.println("####    sent " + sendCount[0] + " using template " + path);
                    }
                }
            }
        });
    }


    private List<NPath> getValidFilePaths(NPath path, String extension, String... folders) {
        NPath parentFolder = getValidFolderPath(path, folders);
        if (parentFolder != null) {
            return parentFolder.stream().filter(x -> x.getName().toLowerCase().endsWith(extension.toLowerCase())).toList();
        }
        NPath one = getValidFilePath(path, extension, folders);
        if (one == null) {
            return new ArrayList<>();
        }
        return Collections.singletonList(one);
    }

    private NPath getValidFilePath(NPath path, String extension, String... folders) {
        if (path.isName()) {
            List<String> all = new ArrayList<>();
            all.addAll(Arrays.asList(folders));
            all.add(new File(".").getAbsolutePath());
            for (String folder : all) {
                NPath a = path.toAbsolute(folder);
                if (a.isRegularFile()) {
                    return a;
                }
                if (!path.getName().endsWith(extension)) {
                    a = path.resolveSibling(path.getName() + extension).toAbsolute(folder);
                    if (a.isRegularFile()) {
                        return a;
                    }
                }
            }
            return null;
        }
        if (path.isRegularFile()) {
            return path;
        }
        return null;
    }

    private NPath getValidFolderPath(NPath path, String... folders) {
        if (path.isName()) {
            List<String> all = new ArrayList<>();
            all.addAll(Arrays.asList(folders));
            all.add(new File(".").getAbsolutePath());
            for (String folder : all) {
                NPath a = path.toAbsolute(folder);
                if (a.isDirectory()) {
                    return a;
                }
            }
            return null;
        }
        if (path.isDirectory()) {
            return path;
        }
        return null;
    }

}
