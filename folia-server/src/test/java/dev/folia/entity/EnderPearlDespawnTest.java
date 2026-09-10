package dev.folia.entity;

import java.io.InputStream;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.support.environment.Normal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Normal
class EnderPearlDespawnTest {
    @ParameterizedTest
    @CsvSource({
        "20, false, false", "20, true, false",
        "1, false, false", "1, true, false",
        "0, false, true", "0, true, true",
        "-1, false, true", "-1, true, true"
    })
    void actualDeathDoesNotDependOnTemporaryWorldRemoval(float health, boolean removed, boolean dying) {
        ServerPlayer owner = mock(ServerPlayer.class);
        when(owner.getHealth()).thenReturn(health);
        when(owner.isRemoved()).thenReturn(removed);
        when(owner.isDeadOrDying()).thenCallRealMethod();
        when(owner.isAlive()).thenCallRealMethod();
        assertEquals(dying, owner.isDeadOrDying());
        if (health > 0 && removed) {
            assertFalse(owner.isAlive(), "Old despawn predicate incorrectly matches a transferring player");
            assertFalse(owner.isDeadOrDying(), "The replacement must not classify transfer as death");
        }
    }

    @Test
    void compiledPearlTickUsesActualDeathAndRetainsGameruleAndCreditsChecks() throws Exception {
        String resource = "net/minecraft/world/entity/projectile/throwableitemprojectile/ThrownEnderpearl.class";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(input);
            ClassNode type = new ClassNode();
            new ClassReader(input).accept(type, 0);
            var tick = type.methods.stream().filter(m -> m.name.equals("tick") && m.desc.equals("()V")).findFirst().orElseThrow();
            int death = -1, credits = -1, gamerule = -1, despawn = -1;
            for (int i = 0; i < tick.instructions.size(); i++) {
                var instruction = tick.instructions.get(i);
                if (instruction instanceof MethodInsnNode call && call.name.equals("isDeadOrDying")) death = i;
                if (instruction instanceof FieldInsnNode field) {
                    if (field.name.equals("wonGame")) credits = i;
                    if (field.name.equals("ENDER_PEARLS_VANISH_ON_DEATH")) gamerule = i;
                    if (field.name.equals("DESPAWN")) despawn = i;
                }
            }
            assertTrue(death >= 0 && credits > death && gamerule > credits && despawn > gamerule,
                "Expected actual death -> End credits exemption -> death gamerule -> DESPAWN");
        }
    }
}
