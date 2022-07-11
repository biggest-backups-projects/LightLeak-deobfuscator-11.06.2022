package com.javadeobfuscator.deobfuscator.transformers.caesium;

import com.javadeobfuscator.deobfuscator.config.TransformerConfig;
import com.javadeobfuscator.deobfuscator.transformers.Transformer;
import com.javadeobfuscator.deobfuscator.utils.SkamUtils;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static com.javadeobfuscator.deobfuscator.utils.SkamUtils.getInteger;

public class CaesiumNumberTransformer extends Transformer<TransformerConfig> {

    @Override
    public boolean transform() throws Exception {
        classNodes().stream()
                .flatMap(classNode -> classNode.methods.stream())
                .forEach(methodNode -> {
                    boolean modified;
                    do {
                        modified = false;

                        for (AbstractInsnNode node : methodNode.instructions.toArray()) {
                            if (node instanceof MethodInsnNode
                                    && ((MethodInsnNode) node).name.equals("reverse")
                                    && ((MethodInsnNode) node).owner.equals("java/lang/Integer")
                                    && ((MethodInsnNode) node).desc.equals("(I)I")
                                    && SkamUtils.isInteger(node.getPrevious())) {

                                int number = Integer.reverse(getInteger(node.getPrevious()));
                                methodNode.instructions.remove(node.getPrevious());
                                methodNode.instructions.set(node, SkamUtils.getNumber(number));
                                modified = true;
                            } else if (node instanceof MethodInsnNode
                                    && ((MethodInsnNode) node).name.equals("reverse")
                                    && ((MethodInsnNode) node).owner.equals("java/lang/Long")
                                    && ((MethodInsnNode) node).desc.equals("(J)J")
                                    && SkamUtils.isLong(node.getPrevious())) {

                                long number = Long.reverse(SkamUtils.getLong(node.getPrevious()));
                                methodNode.instructions.remove(node.getPrevious());
                                methodNode.instructions.set(node, SkamUtils.getNumber(number));
                                modified = true;
                            } else if (node instanceof MethodInsnNode
                                    && ((MethodInsnNode) node).name.equals("floatToIntBits")
                                    && ((MethodInsnNode) node).owner.equals("java/lang/Float")
                                    && ((MethodInsnNode) node).desc.equals("(F)I")
                                    && SkamUtils.isFloat(node.getPrevious()) && SkamUtils.isInteger(node.getNext()) && (node.getNext().getNext().getOpcode() >= IADD && node.getNext().getNext().getOpcode() <= LXOR)) {

                                float number = Float.floatToIntBits(SkamUtils.getFloat(node.getPrevious()));
                                methodNode.instructions.remove(node.getPrevious());
                                methodNode.instructions.set(node, SkamUtils.getNumber(number));
                                modified = true;
                            } else if (node instanceof MethodInsnNode
                                    && ((MethodInsnNode) node).name.equals("doubleToLongBits")
                                    && ((MethodInsnNode) node).owner.equals("java/lang/Double")
                                    && ((MethodInsnNode) node).desc.equals("(D)J")
                                    && SkamUtils.isDouble(node.getPrevious()) && SkamUtils.isLong(node.getNext()) && (node.getNext().getNext().getOpcode() >= IADD && node.getNext().getNext().getOpcode() <= LXOR)) {

                                long number = Double.doubleToLongBits(SkamUtils.getDouble(node.getPrevious()));
                                methodNode.instructions.remove(node.getPrevious());
                                methodNode.instructions.set(node, SkamUtils.getNumber(number));
                                modified = true;
                            } else if (node instanceof MethodInsnNode
                                    && ((MethodInsnNode) node).name.equals("intBitsToFloat")
                                    && ((MethodInsnNode) node).owner.equals("java/lang/Float")
                                    && ((MethodInsnNode) node).desc.equals("(I)F")
                                    && SkamUtils.isInteger(node.getPrevious())) {

                                float number = Float.intBitsToFloat(getInteger(node.getPrevious()));
                                methodNode.instructions.remove(node.getPrevious());
                                methodNode.instructions.set(node, SkamUtils.getNumber(number));
                                modified = true;
                            } else if (node instanceof MethodInsnNode
                                    && ((MethodInsnNode) node).name.equals("longBitsToDouble")
                                    && ((MethodInsnNode) node).owner.equals("java/lang/Double")
                                    && ((MethodInsnNode) node).desc.equals("(J)D")
                                    && SkamUtils.isLong(node.getPrevious())) {

                                double number = Double.longBitsToDouble(SkamUtils.getLong(node.getPrevious()));
                                methodNode.instructions.remove(node.getPrevious());
                                methodNode.instructions.set(node, SkamUtils.getNumber(number));
                                modified = true;
                            }
                        }
                    } while (modified);
                });
        return true;
    }
}
