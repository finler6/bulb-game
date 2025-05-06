package lightbulb.model.command;

/**
 * Rozhraní Command definuje kontrakt pro objekty příkazů v rámci
 * návrhového vzoru Command. Specifikuje metody pro vykonání (execute)
 * a zrušení (undo) příkazu.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
public interface Command {
    void execute();
    void undo();
}