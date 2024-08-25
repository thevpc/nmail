package net.thevpc.gomail.cli;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import net.thevpc.gomail.GoMailListener;
import net.thevpc.gomail.GoMailMessage;
import net.thevpc.nuts.*;

import net.thevpc.gomail.GoMail;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineContext;
import net.thevpc.nuts.cmdline.NCmdLineRunner;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

public class GomailCli implements NApplication {

    LinkedHashSet<String> messageIds = new LinkedHashSet<>();
    String db;

    public static void main(String[] args) {
        NApplication.main(GomailCli.class, args);
    }

    @Override
    public void run(NSession session) {
        session.runAppCmdLine(new NCmdLineRunner() {
            @Override
            public boolean nextOption(NArg option, NCmdLine cmdLine, NCmdLineContext context) {
                switch (option.getStringKey().get()) {
                    case "-d":
                    case "--db": {
                        option = cmdLine.nextEntry().get();
                        if (option.isActive()) {
                            db = option.getStringValue().get();
                        }
                        return true;
                    }
                }
                //no options for now
                return false;
            }

            @Override
            public boolean nextNonOption(NArg nonOption, NCmdLine cmdLine, NCmdLineContext context) {
                messageIds.add(cmdLine.next().get().getImage());
                return true;
            }

            @Override
            public void validate(NCmdLine cmdLine, NCmdLineContext context) {
                if (messageIds.isEmpty()) {
                    cmdLine.throwMissingArgument("messageId");
                }
            }

            @Override
            public void run(NCmdLine cmdLine, NCmdLineContext context) {
                NSession session = context.getSession();
                for (String messageId : messageIds) {
                    List<NPath> paths = getValidFilePaths(NPath.of(messageId, session), ".gomail",
                            NBlankable.isBlank(db) ? session.getAppConfFolder().toString() : db
                    );
                    if (paths.isEmpty()) {
                        cmdLine.throwError(NMsg.ofC("invalid messageId %s", messageId));
                    }
                    for (NPath path : paths) {
                        GoMail go = GoMail.load(path.toFile().get());
                        go.setDry(context.getSession().isDry());
                        int[] sendCount=new int[1];
                        go.send(new GoMailListener() {
                            @Override
                            public void onBeforeSend(GoMailMessage mail) {

                            }

                            @Override
                            public void onAfterSend(GoMailMessage mail) {
                                sendCount[0]++;
                            }

                            @Override
                            public void onSendError(GoMailMessage mail, Throwable exc) {
                                exc.printStackTrace();
                            }
                        });
                        System.out.println("####    sent "+ sendCount[0]+" using template "+path);
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
