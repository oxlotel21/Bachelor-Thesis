package me.oxolotel.bamod.mixin;

import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.SignatureVerifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.UUID;

@Mixin(PlayerPublicKey.class)
public class PlayerPublicKeyMixin {
    /**
     * Randomize key that is send to servers on login. To test if signature is verified
     */
    @Overwrite
    public static PlayerPublicKey verifyAndDecode(SignatureVerifier servicesSignatureVerifier, UUID playerUuid, PlayerPublicKey.PublicKeyData publicKeyData) throws
        PlayerPublicKey.PublicKeyException {
        return new PlayerPublicKey(publicKeyData);
    }
}
