package server.cli;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

@Command(name = "add-channel", description = "Adds a new chat channel.", mixinStandardHelpOptions = true)
public class AddChannelCommand implements Callable<Integer> {
    private BiConsumer<String, Boolean> createChannel;
    public AddChannelCommand(BiConsumer<String, Boolean> createChannel){
        this.createChannel = createChannel;
    }

    @Option(names = "-p", description = "Make the channel private.")
    private boolean isPrivate;

    @Parameters(index = "0", description = "The name of the channel.")
    private String channelName;

    @Override
    public Integer call() {
        createChannel.accept(channelName, !isPrivate);
        System.out.printf("channel added\n");
        return 0;
    }
}
