package shell.ast;

import beaver.Symbol;

/**
 * Created by Guus on 25-05-16.
 */
public class Command extends Symbol {

    private String commandType;

    public Command(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandType() {
        return commandType;
    }
}
