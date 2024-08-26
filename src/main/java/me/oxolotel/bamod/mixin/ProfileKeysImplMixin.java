package me.oxolotel.bamod.mixin;

import com.google.common.base.Strings;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import net.minecraft.client.session.ProfileKeysImpl;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.encryption.PlayerPublicKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Arrays;

@Mixin(ProfileKeysImpl.class)
public abstract class ProfileKeysImplMixin {

    /**
     * Print all information availible for the public key that is obtained from mojang during startup of the game
     */
    @Overwrite
    private static PlayerPublicKey.PublicKeyData decodeKeyPairResponse(KeyPairResponse keyPairResponse) throws NetworkEncryptionException {
        KeyPairResponse.KeyPair keyPair = keyPairResponse.keyPair();
        System.out.println("Decoding Key Pair Response:");
        System.out.println(keyPair.privateKey());
        System.out.println(keyPair.publicKey());
        if (!Strings.isNullOrEmpty(keyPair.publicKey()) && keyPairResponse.publicKeySignature() != null && keyPairResponse.publicKeySignature().array().length != 0) {
            try {
                Instant instant = Instant.parse(keyPairResponse.expiresAt());
                PublicKey publicKey = NetworkEncryptionUtils.decodeRsaPublicKeyPem(keyPair.publicKey());
                ByteBuffer byteBuffer = keyPairResponse.publicKeySignature();
                System.out.println("Decoded PublicKeyData.");
                System.out.println("refreshed after: " + keyPairResponse.refreshedAfter());
                System.out.println("expire at: " + instant);
                System.out.println("public key:");
                System.out.println(publicKey);
                System.out.println("key signature");
                System.out.println(Arrays.toString(byteBuffer.array()));

                return new PlayerPublicKey.PublicKeyData(instant, publicKey, byteBuffer.array());
            } catch (IllegalArgumentException | DateTimeException var5) {
                throw new NetworkEncryptionException(var5);
            }
        } else {
            throw new NetworkEncryptionException(new InsecurePublicKeyException.MissingException("Missing public key"));
        }
    }
}
