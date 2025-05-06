package lightbulb.model;

import lightbulb.model.Board;
import lightbulb.model.Difficulty;

/**
 * Záznam (record) LevelData slouží jako jednoduchý datový kontejner pro uchování
 * instance herní desky (Board) spolu s její úrovní obtížnosti (Difficulty).
 * Používá se především při serializaci a deserializaci úrovní.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
public record LevelData(Board board, Difficulty difficulty) {}