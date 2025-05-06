package lightbulb.model.command;
public interface Command {
    void execute();
    void undo();
}