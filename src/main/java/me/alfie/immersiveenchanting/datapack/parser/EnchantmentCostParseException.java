package me.alfie.immersiveenchanting.datapack.parser;

import com.google.gson.JsonObject;

public class EnchantmentCostParseException extends RuntimeException {
    public EnchantmentCostParseException(String message) {
        super(message);
    }

    public EnchantmentCostParseException(String message, JsonObject jsonObject) {
        super(message);
    }

    public EnchantmentCostParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
