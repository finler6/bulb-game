package lightbulb.model;

/**
 * Funkcionální rozhraní BoardListener definuje metodu, která je volána,
 * když dojde ke změně na herní desce (Board). Umožňuje ostatním částem
 * aplikace reagovat na tyto změny.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
@FunctionalInterface
public interface BoardListener {
    void onBoardChanged(Board board);
}
