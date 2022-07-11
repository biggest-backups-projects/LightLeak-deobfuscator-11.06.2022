package com.javadeobfuscator.deobfuscator.transformers.caesium;

import com.javadeobfuscator.deobfuscator.config.TransformerConfig;
import com.javadeobfuscator.deobfuscator.transformers.Transformer;
import com.javadeobfuscator.deobfuscator.utils.SkamUtils;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Arrays;

public class CaesiumCleanTransformer extends Transformer<TransformerConfig> {

    @Override
    public boolean transform() throws Exception {
        classNodes().forEach(classNode -> {
            classNode.fields.removeIf(fieldNode -> (fieldNode.desc.equals("J") || fieldNode.desc.equals("I")) &&
                    SkamUtils.INTEGER_PATTERN.matcher(fieldNode.name).matches());

            SkamUtils.findClInit(classNode).ifPresent(methodNode -> Arrays.stream(methodNode.instructions.toArray())
                    .filter(node -> node instanceof MethodInsnNode)
                    .filter(node -> node.getOpcode() == INVOKESTATIC)
                    .map(MethodInsnNode.class::cast)
                    .filter(node -> SkamUtils.INTEGER_PATTERN.matcher(node.name).matches())
                    .filter(node -> node.desc.equals("()V"))
                    .forEach(methodNode.instructions::remove));

            classNode.methods.removeIf(methodNode -> methodNode.desc.equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/Object;") &&
                    SkamUtils.INTEGER_PATTERN.matcher(methodNode.name).matches());
        });
        return true;
    }
}
