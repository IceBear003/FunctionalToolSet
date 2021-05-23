package fts.cmd.actioncmd;

import java.util.List;

public class IActions {
    public final List<ActionType> types;
    public final List<Integer> values;
    public final List<String> cmds;
    public final List<String> messages;

    public IActions(List<ActionType> types, List<Integer> values, List<String> cmds, List<String> messages) {
        this.types = types;
        this.values = values;
        this.cmds = cmds;
        this.messages = messages;
    }
}