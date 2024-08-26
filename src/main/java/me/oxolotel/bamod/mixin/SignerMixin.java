package me.oxolotel.bamod.mixin;

import net.minecraft.network.encryption.Signer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;

@Mixin(Signer.class)
public interface SignerMixin {

    /**
     * replace used key when signing chat messages with a new random Key. This is done to test if both client and server verify
     * the signature of a message
     */
    @Overwrite
    public static Signer create(PrivateKey privateKey, String algorithm) {
        return updatable -> {
            KeyPairGenerator kpg = null;
            try {
                kpg = KeyPairGenerator.getInstance("RSA");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            PrivateKey pvt = kp.getPrivate();

            try {
                Signature signature = Signature.getInstance(algorithm);
                signature.initSign(pvt);
                updatable.update(signature::update);
                return signature.sign();
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to sign message", exception);
            }
        };
    }
}
