package server.cli;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.util.TriConsumer;


@Command(name = "perms", description = "Gives permissions on a channel to a user.", mixinStandardHelpOptions = true)
public class AddUserToChannelCommand implements Callable<Integer>{
    private TriConsumer<String, String, Boolean> givePermission;
    public AddUserToChannelCommand(TriConsumer<String, String, Boolean> givePermission){
        this.givePermission = givePermission;
    }

    @Option(names = "-r", description = "Removes permission for channel if set.")
    private boolean removePermission;

    @Parameters(index = "0", description = "The name of the user.")
    private String userName;

    @Parameters(index = "1", description = "The name of the channel.")
    private String channelName;

    @Override
    public Integer call() {
        givePermission.accept(userName, channelName, !removePermission);
        System.out.printf("permissions given\n");
        return 0;
    }
}
