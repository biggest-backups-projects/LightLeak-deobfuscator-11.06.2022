package com.javadeobfuscator.deobfuscator.transformers.bozar;

import com.javadeobfuscator.deobfuscator.config.TransformerConfig;
import com.javadeobfuscator.deobfuscator.transformers.Transformer;
import com.javadeobfuscator.deobfuscator.utils.SkamUtils;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Arrays;

public class BozarStringTransformer extends Transformer<TransformerConfig> {

    @Override
    public boolean transform() throws Exception {
        classNodes().stream()
                .flatMap(classNode -> classNode.methods.stream())
                .forEach(methodNode -> {
                    Arrays.stream(methodNode.instructions.toArray())
                            .filter(node -> node.getOpcode() == NEW)
                            .map(TypeInsnNode.class::cast)
                            .filter(node -> node.desc.equals("java/lang/String"))
                            .filter(node -> node.getNext().getOpcode() == DUP)
                            .filter(node -> node.getNext().getNext().getOpcode() == ALOAD)
                            .filter(node -> node.getNext().getNext().getNext().getOpcode() == INVOKESPECIAL)
                            .forEach(node -> {
                                AbstractInsnNode current = node;

                                int endIndex = methodNode.instructions.indexOf(node);
                                int startIndex = -1;
                                int storeIndex = ((VarInsnNode) node.getNext().getNext()).var;

                                while (current.getPrevious() != null && !((current = current.getPrevious()).getOpcode() == ASTORE && ((VarInsnNode) current).var == storeIndex)) {
                                }

                                startIndex = methodNode.instructions.indexOf(current);
                                if (startIndex == -1 || !SkamUtils.isInteger(current.getPrevious().getPrevious()))
                                    return;

                                byte[] bytes = new byte[SkamUtils.getInteger(current.getPrevious().getPrevious())];
                                for (int i = startIndex; i < endIndex; i++) {
                                    AbstractInsnNode insn = methodNode.instructions.get(i);
                                    if (SkamUtils.isInteger(insn) && SkamUtils.isInteger(insn.getNext()) && insn.getNext().getNext().getOpcode() == BASTORE) {
                                        bytes[SkamUtils.getInteger(insn)] = (byte) SkamUtils.getInteger(insn.getNext());
                                    }
                                }

                                AbstractInsnNode insertBefore = node.getNext().getNext().getNext().getNext();
                                SkamUtils.getInstructionsBetween(current.getPrevious().getPrevious(), node.getNext().getNext().getNext(), true, true)
                                        .forEach(methodNode.instructions::remove);

                                methodNode.instructions.insertBefore(insertBefore, new LdcInsnNode(new String(bytes)));
                            });

                });
        return true;
    }
}
