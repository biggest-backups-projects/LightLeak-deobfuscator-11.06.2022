package com.javadeobfuscator.deobfuscator.transformers.bozar;

import com.javadeobfuscator.deobfuscator.config.TransformerConfig;
import com.javadeobfuscator.deobfuscator.transformers.Transformer;
import com.javadeobfuscator.deobfuscator.utils.SkamUtils;
import org.objectweb.asm.tree.*;

import java.util.Arrays;

public class BozarLightControlFlowTransformer extends Transformer<TransformerConfig> {

    @Override
    public boolean transform() throws Exception {
        classNodes().forEach(classNode -> {
            classNode.fields.removeIf(field -> field.name.equals("Ꮸ") && field.desc.equals("J"));
            classNode.methods.forEach(methodNode -> {

                //Light type 2
                Arrays.stream(methodNode.instructions.toArray())
                        .filter(node -> node instanceof FieldInsnNode)
                        .map(FieldInsnNode.class::cast)
                        .filter(node -> node.name.equals("Ꮸ"))
                        .filter(node -> node.desc.equals("J"))
                        .filter(node -> node.owner.contains(classNode.name))
                        .filter(node -> SkamUtils.isNumber(node.getNext()))
                        .filter(node -> node.getPrevious().getPrevious().getPrevious().getPrevious().getOpcode() == GOTO)
                        .forEach(node -> {
                            int index = methodNode.instructions.indexOf(node);
                            AbstractInsnNode icmpne = methodNode.instructions.get(index + 6);
                            AbstractInsnNode gotoEnd = methodNode.instructions.get(index + 8);

                            if (icmpne.getOpcode() == IF_ICMPNE && gotoEnd.getOpcode() == GOTO && ((JumpInsnNode) gotoEnd).label.getPrevious().getOpcode() == GOTO) {
                                SkamUtils.getInstructionsBetween(
                                        node.getPrevious().getPrevious().getPrevious().getPrevious(),
                                        icmpne,
                                        true,
                                        true
                                ).forEach(methodNode.instructions::remove);

                                SkamUtils.getInstructionsBetween(
                                        gotoEnd,
                                        ((JumpInsnNode) gotoEnd).label,
                                        true,
                                        false
                                ).forEach(methodNode.instructions::remove);
                            }
                        });

                //Light type 2
                Arrays.stream(methodNode.instructions.toArray())
                        .filter(node -> node instanceof FieldInsnNode)
                        .map(FieldInsnNode.class::cast)
                        .filter(node -> node.name.equals("Ꮸ"))
                        .filter(node -> node.desc.equals("J"))
                        .filter(node -> node.owner.contains(classNode.name))
                        .filter(node -> node.getNext().getOpcode() == GOTO)
                        .forEach(node -> {
                            AbstractInsnNode switchNode = ((JumpInsnNode) node.getNext()).label.getNext().getNext();
                            if (switchNode instanceof LookupSwitchInsnNode) {
                                LabelNode before = ((LookupSwitchInsnNode) switchNode).dflt;
                                if (is(before.getNext()) && before.getPrevious().getOpcode() == ATHROW) {
                                    SkamUtils.getInstructionsBetween(
                                            node,
                                            before,
                                            true,
                                            false
                                    ).forEach(methodNode.instructions::remove);
                                }
                            }
                        });
            });
        });
        return true;
    }

    private boolean is(AbstractInsnNode node) {
        return node.getOpcode() == NEW || node instanceof FieldInsnNode || node instanceof MethodInsnNode || node instanceof InvokeDynamicInsnNode;
    }
}
