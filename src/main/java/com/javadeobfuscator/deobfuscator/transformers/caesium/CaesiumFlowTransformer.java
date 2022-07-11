package com.javadeobfuscator.deobfuscator.transformers.caesium;

import com.javadeobfuscator.deobfuscator.config.TransformerConfig;
import com.javadeobfuscator.deobfuscator.transformers.Transformer;
import com.javadeobfuscator.deobfuscator.utils.SkamUtils;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CaesiumFlowTransformer extends Transformer<TransformerConfig> {

    @Override
    public boolean transform() throws Exception {
        List<FieldInsnNode> toRemove = new ArrayList<>();

        classNodes().stream()
                .flatMap(classNode -> classNode.methods.stream())
                .forEach(methodNode -> Arrays.stream(methodNode.instructions.toArray())
                        .filter(node -> SkamUtils.check(node, GETSTATIC))
                        .filter(node -> SkamUtils.check(node.getNext(), JumpInsnNode.class))
                        .filter(node -> SkamUtils.check(node.getNext().getNext(), ACONST_NULL))
                        .filter(node -> SkamUtils.check(node.getNext().getNext().getNext(), ATHROW))
                        .forEach(node -> {
                            toRemove.add((FieldInsnNode) node);

                            methodNode.instructions.remove(node.getNext().getNext().getNext());
                            methodNode.instructions.remove(node.getNext().getNext());
                            methodNode.instructions.set(node, new JumpInsnNode(GOTO, ((JumpInsnNode) node.getNext()).label));
                        }));

        toRemove.forEach(fieldInsnNode -> {
            ClassNode classNode = classes.get(fieldInsnNode.owner);
            if (classNode == null)
                return;

            classNode.fields.removeIf(fieldNode -> fieldNode.name.equals(fieldInsnNode.name) && fieldNode.desc.equals(fieldInsnNode.desc));
        });
        toRemove.clear();
        return true;
    }
}
