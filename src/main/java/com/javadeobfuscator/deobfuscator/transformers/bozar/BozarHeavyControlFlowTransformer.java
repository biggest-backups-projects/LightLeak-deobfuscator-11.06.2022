package com.javadeobfuscator.deobfuscator.transformers.bozar;

import com.javadeobfuscator.deobfuscator.config.TransformerConfig;
import com.javadeobfuscator.deobfuscator.transformers.Transformer;
import com.javadeobfuscator.deobfuscator.utils.SkamUtils;
import org.objectweb.asm.tree.*;

import java.util.Arrays;

public class BozarHeavyControlFlowTransformer  extends Transformer<TransformerConfig> {

    private final boolean fuckingASMDogShit;

    public BozarHeavyControlFlowTransformer(boolean fuckingASMDogShit) {
        this.fuckingASMDogShit = fuckingASMDogShit;
    }

    @Override
    public boolean transform() throws Exception {
        classNodes().forEach(classNode -> {
            classNode.fields.removeIf(field -> field.name.equals("Ꮹ") && field.desc.equals("J"));
            classNode.methods.forEach(methodNode -> {
                if (!fuckingASMDogShit) {
                    Arrays.stream(methodNode.instructions.toArray())
                            .filter(node -> node instanceof FieldInsnNode)
                            .map(FieldInsnNode.class::cast)
                            .filter(node -> node.name.equals("Ꮹ"))
                            .filter(node -> node.desc.equals("J"))
                            .filter(node -> node.owner.contains(classNode.name))
                            .filter(node -> node.getNext().getOpcode() == L2I)
                            .filter(node -> node.getNext().getNext() instanceof LookupSwitchInsnNode)
                            .forEach(node -> {
                                LookupSwitchInsnNode switchNode = (LookupSwitchInsnNode) node.getNext().getNext();
                                LabelNode labelNode = switchNode.labels.stream().filter(label -> !label.equals(switchNode.dflt)).findFirst().orElseThrow();
                                if (labelNode.getNext().getOpcode() == GOTO) {
                                    SkamUtils.getInstructionsBetween(
                                            node,
                                            ((JumpInsnNode) labelNode.getNext()).label,
                                            true,
                                            true
                                    ).forEach(methodNode.instructions::remove);
                                }
                            });

                    Arrays.stream(methodNode.instructions.toArray())
                            .filter(node -> node instanceof FieldInsnNode)
                            .map(FieldInsnNode.class::cast)
                            .filter(node -> node.name.equals("Ꮹ"))
                            .filter(node -> node.desc.equals("J"))
                            .filter(node -> node.owner.contains(classNode.name))
                            .filter(node -> node.getNext() instanceof LabelNode)
                            .filter(node -> SkamUtils.isNumber(node.getNext().getNext()))
                            .filter(node -> node.getNext().getNext().getNext().getOpcode() == GOTO)
                            .forEach(node -> {
                                JumpInsnNode gotoSwitch = (JumpInsnNode) node.getNext().getNext().getNext();
                                LookupSwitchInsnNode switchNode = (LookupSwitchInsnNode) gotoSwitch.label.getNext().getNext().getNext();
                                LabelNode labelNode = switchNode.labels.stream().filter(label -> !label.equals(switchNode.dflt)).findFirst().orElseThrow();

                                JumpInsnNode gotoSecondPart = ((JumpInsnNode) labelNode.getNext().getNext().getNext());
                                LabelNode secondPartLabel = gotoSecondPart.label;

                                int index = methodNode.instructions.indexOf(secondPartLabel);
                                if (secondPartLabel.getNext().getOpcode() == LXOR) {
                                    AbstractInsnNode end = methodNode.instructions.get(index + 11);
                                    if (end instanceof LabelNode) {
                                        SkamUtils.getInstructionsBetween(
                                                node,
                                                end,
                                                true,
                                                true
                                        ).forEach(methodNode.instructions::remove);
                                    }
                                }
                            });

                    Arrays.stream(methodNode.instructions.toArray())
                            .filter(node -> node instanceof FieldInsnNode)
                            .map(FieldInsnNode.class::cast)
                            .filter(node -> node.name.equals("Ꮹ"))
                            .filter(node -> node.desc.equals("J"))
                            .filter(node -> node.owner.contains(classNode.name))
                            .filter(node -> SkamUtils.isNumber(node.getNext()))
                            .filter(node -> node.getNext().getNext().getOpcode() == GOTO)
                            .forEach(node -> {
                                JumpInsnNode gotoSwitch = (JumpInsnNode) node.getNext().getNext();
                                LookupSwitchInsnNode switchNode = (LookupSwitchInsnNode) gotoSwitch.label.getNext().getNext().getNext();
                                LabelNode labelNode = switchNode.labels.stream().filter(label -> !label.equals(switchNode.dflt)).findFirst().orElseThrow();

                                JumpInsnNode gotoSecondPart = ((JumpInsnNode) labelNode.getNext().getNext().getNext());
                                LabelNode secondPartLabel = gotoSecondPart.label;

                                int index = methodNode.instructions.indexOf(secondPartLabel);
                                if (secondPartLabel.getNext().getOpcode() == LCMP) {
                                    AbstractInsnNode end = methodNode.instructions.get(index + 4);
                                    if (end.getOpcode() == IFEQ) {
                                        end = methodNode.instructions.get(index + 7);
                                    }

                                    SkamUtils.getInstructionsBetween(
                                            node,
                                            end,
                                            true,
                                            true
                                    ).forEach(methodNode.instructions::remove);
                                } else if (secondPartLabel.getNext().getOpcode() == LAND) {
                                    AbstractInsnNode end = methodNode.instructions.get(index + 4);
                                    AbstractInsnNode afterStart = methodNode.instructions.get(index + 6);
                                    AbstractInsnNode afterEnd = methodNode.instructions.get(index + 12);

                                    SkamUtils.getInstructionsBetween(
                                            node,
                                            end,
                                            true,
                                            true
                                    ).forEach(methodNode.instructions::remove);

                                    SkamUtils.getInstructionsBetween(
                                            afterStart,
                                            afterEnd,
                                            true,
                                            true
                                    ).forEach(methodNode.instructions::remove);
                                }
                            });
                } else {
                    Arrays.stream(methodNode.instructions.toArray())
                            .filter(this::is)
                            .filter(node -> node.getNext().getOpcode() == GOTO)
                            .filter(node -> node.getPrevious().getOpcode() == POP)
                            .forEach(node -> {
                                SkamUtils.getInstructionsBetween(
                                        node.getNext(),
                                        ((JumpInsnNode) node.getNext()).label,
                                        true,
                                        true
                                ).stream().filter(xd -> !(xd instanceof LabelNode)).forEach(methodNode.instructions::remove);

                                int index = methodNode.instructions.indexOf(node);
                                AbstractInsnNode start = methodNode.instructions.get(index - 13);

                                SkamUtils.getInstructionsBetween(
                                        start,
                                        node.getPrevious(),
                                        true,
                                        true
                                ).stream().filter(xd -> !(xd instanceof LabelNode)).forEach(methodNode.instructions::remove);
                            });

                    if (methodNode.instructions.size() > 0) {
                        AbstractInsnNode start = methodNode.instructions.getFirst() != null ? methodNode.instructions.getFirst() : methodNode.instructions.get(0);
                        if (start.getOpcode() == ACONST_NULL && start.getNext().getOpcode() == ASTORE) {
                            methodNode.instructions.remove(start.getNext());
                            methodNode.instructions.remove(start);

                            start = methodNode.instructions.getFirst();
                        }

                        if (start.getOpcode() == ICONST_1 && start.getNext().getOpcode() == GOTO) {
                            methodNode.instructions.remove(start.getNext().getNext().getNext().getNext().getNext().getNext());
                            methodNode.instructions.remove(start.getNext().getNext().getNext().getNext().getNext());
                            methodNode.instructions.remove(start.getNext().getNext().getNext().getNext());
                            methodNode.instructions.remove(start.getNext().getNext().getNext());
                            methodNode.instructions.remove(start.getNext().getNext());
                            methodNode.instructions.remove(start.getNext());
                            methodNode.instructions.remove(start);
                        }
                    }
                }
            });
        });
        return true;
    }

    private boolean is(AbstractInsnNode node) {
        return node.getOpcode() == NEW || node instanceof FieldInsnNode || node instanceof MethodInsnNode || node instanceof InvokeDynamicInsnNode;
    }
}
