package net.thevpc.gomail.cli;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import net.thevpc.gomail.GoMailListener;
import net.thevpc.gomail.GoMailMessage;
import net.thevpc.nuts.*;

import net.thevpc.gomail.GoMail;

public class GomailCli implements NutsApplication, NutsAppCmdProcessor {

    LinkedHashSet<String> messageIds = new LinkedHashSet<>();
    String db;

    public static void main(String[] args) {
        NutsApplication.main(GomailCli.class, args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        appContext.processCommandLine(this);
    }

    @Override
    public boolean onCmdNextOption(NutsArgument option, NutsCommandLine commandline, NutsApplicationContext context) {
        switch (option.getStringKey()) {
            case "-d":
            case "--db": {
                option = commandline.nextString();
                if (option.isActive()) {
                    this.db = option.getStringValue();
                }
                return true;
            }

        }
        //no options for now
        return false;
    }

    @Override
    public boolean onCmdNextNonOption(NutsArgument nonOption, NutsCommandLine commandline, NutsApplicationContext context) {
        messageIds.add(commandline.next().getString());
        return true;
    }

    @Override
    public void onCmdFinishParsing(NutsCommandLine commandline, NutsApplicationContext context) {
        if (this.messageIds.isEmpty()) {
            commandline.requiredNonOptions("messageId");
        }
    }

    @Override
    public void onCmdExec(NutsCommandLine commandline, NutsApplicationContext context) {
        NutsSession session = context.getSession();
        for (String messageId : messageIds) {
            List<NutsPath> paths = getValidFilePaths(NutsPath.of(messageId, session), ".gomail",
                    NutsBlankable.isBlank(db) ? context.getConfigFolder().toString() : db
            );
            if (paths.isEmpty()) {
                commandline.throwError(NutsMessage.cstyle("invalid messageId %s", messageId));
            }
            for (NutsPath path : paths) {
                GoMail go = GoMail.load(path.toFile().toFile());
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

    private List<NutsPath> getValidFilePaths(NutsPath path, String extension, String... folders) {
        NutsPath parentFolder = getValidFolderPath(path, folders);
        if (parentFolder != null) {
            return parentFolder.list().filter(x -> x.getName().toLowerCase().endsWith(extension.toLowerCase()),
                    elems -> elems.ofString("lowercase=" + extension.toLowerCase())
            ).toList();
        }
        NutsPath one = getValidFilePath(path, extension, folders);
        if (one == null) {
            return new ArrayList<>();
        }
        return Collections.singletonList(one);
    }

    private NutsPath getValidFilePath(NutsPath path, String extension, String... folders) {
        if (path.isName()) {
            List<String> all = new ArrayList<>();
            all.addAll(Arrays.asList(folders));
            all.add(new File(".").getAbsolutePath());
            for (String folder : all) {
                NutsPath a = path.toAbsolute(folder);
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

    private NutsPath getValidFolderPath(NutsPath path, String... folders) {
        if (path.isName()) {
            List<String> all = new ArrayList<>();
            all.addAll(Arrays.asList(folders));
            all.add(new File(".").getAbsolutePath());
            for (String folder : all) {
                NutsPath a = path.toAbsolute(folder);
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
