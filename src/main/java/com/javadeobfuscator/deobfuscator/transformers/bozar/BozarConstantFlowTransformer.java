package com.javadeobfuscator.deobfuscator.transformers.bozar;

import com.javadeobfuscator.deobfuscator.config.TransformerConfig;
import com.javadeobfuscator.deobfuscator.transformers.Transformer;
import com.javadeobfuscator.deobfuscator.utils.SkamUtils;
import com.javadeobfuscator.javavm.utils.ASMHelper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.Arrays;

public class BozarConstantFlowTransformer extends Transformer<TransformerConfig> {

    @Override
    public boolean transform() throws Exception {
        classNodes().stream()
                .flatMap(classNode -> classNode.methods.stream())
                .forEach(methodNode -> Arrays.stream(methodNode.instructions.toArray())
                        .filter(SkamUtils::isNumber)
                        .filter(node -> node.getNext() instanceof LabelNode)
                        .filter(node -> node.getPrevious() instanceof LabelNode)
                        .filter(node -> node.getPrevious().getPrevious().getOpcode() == GOTO)
                        .filter(node -> SkamUtils.isNumber(node.getPrevious().getPrevious().getPrevious()))
                        .forEach(node -> {
                            int index = methodNode.instructions.indexOf(node);
                            if (index - 10 < 0 || methodNode.instructions.size() < index + 9)
                                return;

                            AbstractInsnNode beforeStart = methodNode.instructions.get(index - 10);
                            AbstractInsnNode beforeEnd = node.getPrevious(); //methodNode.instructions.get(index - 1); //node.getPrevious()

                            AbstractInsnNode afterStart = node.getNext(); //methodNode.instructions.get(index + 1); //node.getNext()
                            AbstractInsnNode afterEnd = methodNode.instructions.get(index + 9);

                            if (SkamUtils.isLong(beforeStart) && afterEnd instanceof LabelNode) {
                                SkamUtils.getInstructionsBetween(
                                        beforeStart,
                                        beforeEnd,
                                        true,
                                        true
                                ).forEach(methodNode.instructions::remove);

                                SkamUtils.getInstructionsBetween(
                                        afterStart,
                                        afterEnd,
                                        true,
                                        false
                                ).forEach(methodNode.instructions::remove);
                            }
                        }));
        return true;
    }
}
