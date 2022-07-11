package com.javadeobfuscator.deobfuscator.transformers.bozar;

import com.javadeobfuscator.deobfuscator.config.TransformerConfig;
import com.javadeobfuscator.deobfuscator.transformers.Transformer;

import java.util.function.Predicate;

public class BozarWatermarkRemoverTransformer extends Transformer<TransformerConfig> {

    private final Predicate<String> predicate = s -> s.split("\u0001/", 69).length > 3 || s.split("\u0020").length > 3 || s.contains("OBFUSCATED WITH BOZAR");

    @Override
    public boolean transform() throws Exception {
        classNodes().removeIf(classNode -> predicate.test(classNode.name));
        getDeobfuscator().getClasses().entrySet().removeIf(entry -> predicate.test(entry.getKey()));
        return true;
    }
}
